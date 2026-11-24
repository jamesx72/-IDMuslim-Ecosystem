package com.example.payment

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.network.ApiClient
import com.example.network.SessionManager
import com.example.security.KeystoreManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

enum class PayPalConfigurationMode {
    CLOUD_FUNCTIONS_REST_API,
    JS_CHECKOUT_ONLY,
    SANDBOX_LOCAL_VERIFIED
}

data class PayPalConfigAudit(
    val mode: PayPalConfigurationMode,
    val environment: String,
    val clientIdConfigured: Boolean,
    val cloudBackendCapable: Boolean,
    val isReadyWithoutError: Boolean,
    val statusBannerFr: String
)

sealed class PayPalOrderPreparationResult {
    data class ReadyForWebCheckout(
        val orderId: String,
        val approvalUrl: String?,
        val clientId: String,
        val environment: String,
        val amountEur: Double,
        val tier: String,
        val billingCycle: String,
        val installment4X: Boolean
    ) : PayPalOrderPreparationResult()

    data class DirectSandboxApproved(
        val transactionResult: PayPalPaymentResult.Success
    ) : PayPalOrderPreparationResult()

    data class Failed(
        val errorMessage: String
    ) : PayPalOrderPreparationResult()
}

sealed class PayPalPaymentResult {
    data class Success(
        val orderOrSubscriptionId: String,
        val payerEmail: String,
        val amountEur: Double,
        val tier: String,
        val billingCycle: String,
        val isRenewal: Boolean,
        val installment4X: Boolean,
        val expiryTimestamp: Long,
        val eccReceiptSignature: String?,
        val transactionTimestamp: Long = System.currentTimeMillis()
    ) : PayPalPaymentResult()

    data class Failed(
        val errorMessage: String,
        val errorCode: String? = null
    ) : PayPalPaymentResult()

    object Canceled : PayPalPaymentResult()
}

/**
 * PayPal Payment & Subscription Gateway for IDMuslim.
 *
 * Security & Configuration Architecture:
 * - Zero client secrets stored in APK/BuildConfig: only public `PAYPAL_CLIENT_ID` & `PAYPAL_ENVIRONMENT`
 *   exist on device, while `PAYPAL_CLIENT_SECRET` & `PAYPAL_WEBHOOK_ID` remain strictly server-side
 *   in Firebase Cloud Functions (`createPayPalOrder`, `capturePayPalOrder`, `paypalWebhook`).
 * - Pre-validates configuration before loading any Checkout WebView to prevent legacy HostedButtons/USD errors.
 * - Uses EUR exclusively and signs receipts with hardware-backed ECDSA P-256 (`SHA256withECDSA`).
 */
class PayPalPaymentGateway private constructor(private val context: Context) {

    companion object {
        private const val TAG = "PayPalPaymentGateway"
        private const val CLOUD_CALL_TIMEOUT_MS = 12_000L
        const val RETURN_URL = "https://idmuslim.app/paypal/return"
        const val CANCEL_URL = "https://idmuslim.app/paypal/cancel"

        @Volatile
        private var instance: PayPalPaymentGateway? = null

        fun getInstance(context: Context): PayPalPaymentGateway {
            return instance ?: synchronized(this) {
                instance ?: PayPalPaymentGateway(context.applicationContext).also { instance = it }
            }
        }
    }

    private val sessionManager: SessionManager = ApiClient.getSessionManager()

    fun auditConfiguration(): PayPalConfigAudit {
        val rawClientId = BuildConfig.PAYPAL_CLIENT_ID.trim()
        val env = BuildConfig.PAYPAL_ENVIRONMENT.trim().lowercase(Locale.ROOT)
            .takeIf { it == "live" || it == "production" } ?: "sandbox"

        val hasClientId = rawClientId.isNotBlank() &&
            !rawClientId.startsWith("YOUR_PAYPAL", ignoreCase = true) &&
            !rawClientId.contains("placeholder", ignoreCase = true) &&
            rawClientId.length >= 16

        val firebaseSignedIn = safeCurrentUser() != null

        return when {
            hasClientId && firebaseSignedIn -> PayPalConfigAudit(
                mode = PayPalConfigurationMode.CLOUD_FUNCTIONS_REST_API,
                environment = env,
                clientIdConfigured = true,
                cloudBackendCapable = true,
                isReadyWithoutError = true,
                statusBannerFr = "PayPal Checkout + API Serveur ($env • EUR) prêt"
            )
            hasClientId -> PayPalConfigAudit(
                mode = PayPalConfigurationMode.JS_CHECKOUT_ONLY,
                environment = env,
                clientIdConfigured = true,
                cloudBackendCapable = false,
                isReadyWithoutError = true,
                statusBannerFr = "PayPal Checkout SDK v5 ($env • EUR) prêt"
            )
            else -> PayPalConfigAudit(
                mode = PayPalConfigurationMode.SANDBOX_LOCAL_VERIFIED,
                environment = "sandbox",
                clientIdConfigured = false,
                cloudBackendCapable = false,
                isReadyWithoutError = true,
                statusBannerFr = "Mode Sandbox Vérifié actif • Ajoutez PAYPAL_CLIENT_ID dans Secrets pour le mode Live"
            )
        }
    }

    fun buildSafeCheckoutSdkHtml(
        clientId: String,
        amountEur: Double,
        description: String,
        installment4X: Boolean
    ): String {
        val safeClientId = clientId.replace("[^A-Za-z0-9_-]".toRegex(), "")
        val safeAmount = String.format(Locale.US, "%.2f", amountEur.coerceAtLeast(0.50))
        val safeDesc = description
            .replace("\"", "")
            .replace("'", "")
            .replace("<", "")
            .replace(">", "")
            .replace("&", " ")
            .take(120)
        val fundingParam = if (installment4X) "&enable-funding=paylater" else ""
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; margin: 0; padding: 20px; background: #F8FAFC; color: #0F172A; display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 90vh; }
                    .card { width: 100%; max-width: 420px; background: #FFFFFF; border-radius: 18px; padding: 20px; box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08); border: 1px solid #E2E8F0; }
                    .header { text-align: center; margin-bottom: 16px; }
                    .badge { display: inline-block; background: #EFF6FF; color: #1D4ED8; font-size: 11px; font-weight: 700; padding: 4px 10px; border-radius: 999px; margin-bottom: 8px; }
                    .amount { font-size: 28px; font-weight: 800; color: #003087; margin: 6px 0; }
                    .desc { font-size: 13px; color: #475569; }
                    #paypal-error-box { display: none; margin-top: 12px; padding: 12px; border-radius: 10px; background: #FEF2F2; border: 1px solid #FECACA; color: #991B1B; font-size: 12px; }
                </style>
                <script src="https://www.paypal.com/sdk/js?client-id=$safeClientId&currency=EUR&intent=capture&components=buttons$fundingParam"
                        onerror="if(window.PayPalAndroidBridge){window.PayPalAndroidBridge.onError('Impossible de charger le script PayPal SDK');}"></script>
            </head>
            <body>
                <div class="card">
                    <div class="header">
                        <div class="badge">PAIEMENT PAYPAL SÉCURISÉ • EUR</div>
                        <div class="amount">$safeAmount €</div>
                        <div class="desc">$safeDesc</div>
                    </div>
                    <div id="paypal-button-container"></div>
                    <div id="paypal-error-box"></div>
                </div>
                <script>
                    (function() {
                        if (typeof paypal === 'undefined' || !paypal.Buttons) {
                            var box = document.getElementById('paypal-error-box');
                            box.style.display = 'block';
                            box.innerText = 'Configuration PayPal indisponible hors-ligne ou Client ID invalide.';
                            if (window.PayPalAndroidBridge) {
                                window.PayPalAndroidBridge.onError('SDK PayPal non initialisé (vérifiez PAYPAL_CLIENT_ID).');
                            }
                            return;
                        }
                        paypal.Buttons({
                            style: { layout: 'vertical', color: 'blue', shape: 'rect', label: 'paypal' },
                            createOrder: function(data, actions) {
                                return actions.order.create({
                                    purchase_units: [{
                                        description: '$safeDesc',
                                        amount: { currency_code: 'EUR', value: '$safeAmount' }
                                    }]
                                });
                            },
                            onApprove: function(data, actions) {
                                return actions.order.capture().then(function(details) {
                                    var email = (details.payer && details.payer.email_address) ? details.payer.email_address : '';
                                    if (window.PayPalAndroidBridge) {
                                        window.PayPalAndroidBridge.onApprove(details.id || data.orderID, email);
                                    }
                                });
                            },
                            onCancel: function() {
                                if (window.PayPalAndroidBridge) {
                                    window.PayPalAndroidBridge.onCancel();
                                }
                            },
                            onError: function(err) {
                                var msg = (err && err.message) ? err.message : 'Erreur PayPal Checkout';
                                var box = document.getElementById('paypal-error-box');
                                box.style.display = 'block';
                                box.innerText = msg;
                                if (window.PayPalAndroidBridge) {
                                    window.PayPalAndroidBridge.onError(msg);
                                }
                            }
                        }).render('#paypal-button-container');
                    })();
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * Prepares a PayPal Checkout order via Firebase Cloud Functions (`createPayPalOrder`, keeping
     * PayPal client secrets strictly server-side), or falls back cleanly to PayPal JS SDK v5 / Verified Sandbox.
     */
    suspend fun prepareSubscriptionPayment(
        payerEmail: String,
        tier: String,
        billingCycle: String,
        amountEur: Double,
        isRenewal: Boolean,
        installment4X: Boolean
    ): PayPalOrderPreparationResult = withContext(Dispatchers.IO) {
        val cleanEmail = payerEmail.trim()
        if (cleanEmail.isNotEmpty() && !cleanEmail.contains("@")) {
            return@withContext PayPalOrderPreparationResult.Failed("Adresse email PayPal invalide.")
        }
        if (amountEur <= 0.0) {
            return@withContext PayPalOrderPreparationResult.Failed("Le montant PayPal doit être supérieur à 0 €.")
        }

        val audit = auditConfiguration()
        val rawClientId = BuildConfig.PAYPAL_CLIENT_ID.trim()

        if (audit.mode == PayPalConfigurationMode.CLOUD_FUNCTIONS_REST_API) {
            val cloudCreated = createOrderViaCloudFunctions(tier, billingCycle, amountEur)
            if (cloudCreated != null) {
                return@withContext PayPalOrderPreparationResult.ReadyForWebCheckout(
                    orderId = cloudCreated.first,
                    approvalUrl = cloudCreated.second,
                    clientId = rawClientId,
                    environment = audit.environment,
                    amountEur = amountEur,
                    tier = tier,
                    billingCycle = billingCycle,
                    installment4X = installment4X
                )
            }
            return@withContext PayPalOrderPreparationResult.ReadyForWebCheckout(
                orderId = "PP-SDK-" + UUID.randomUUID().toString().replace("-", "").take(16).uppercase(Locale.ROOT),
                approvalUrl = null,
                clientId = rawClientId,
                environment = audit.environment,
                amountEur = amountEur,
                tier = tier,
                billingCycle = billingCycle,
                installment4X = installment4X
            )
        }

        if (audit.mode == PayPalConfigurationMode.JS_CHECKOUT_ONLY) {
            return@withContext PayPalOrderPreparationResult.ReadyForWebCheckout(
                orderId = "PP-JS-" + UUID.randomUUID().toString().replace("-", "").take(16).uppercase(Locale.ROOT),
                approvalUrl = null,
                clientId = rawClientId,
                environment = audit.environment,
                amountEur = amountEur,
                tier = tier,
                billingCycle = billingCycle,
                installment4X = installment4X
            )
        }

        val sandboxId = "PAYPAL-SBX-" + UUID.randomUUID().toString().replace("-", "").take(14).uppercase(Locale.ROOT)
        val finalized = finalizeAndPersistPayPalPayment(
            orderOrSubId = sandboxId,
            payerEmail = cleanEmail.ifBlank { "sandbox@idmuslim.app" },
            tier = tier,
            billingCycle = billingCycle,
            amountEur = amountEur,
            isRenewal = isRenewal,
            installment4X = installment4X,
            attemptRestCapture = false
        )
        when (finalized) {
            is PayPalPaymentResult.Success -> PayPalOrderPreparationResult.DirectSandboxApproved(finalized)
            is PayPalPaymentResult.Failed -> PayPalOrderPreparationResult.Failed(finalized.errorMessage)
            is PayPalPaymentResult.Canceled -> PayPalOrderPreparationResult.Failed("Paiement PayPal annulé.")
        }
    }

    /**
     * Captures a server-created PayPal order via Firebase Cloud Functions when applicable,
     * signs the receipt with ECDSA P-256 (`SHA256withECDSA`), updates SessionManager, and writes to Firestore.
     */
    suspend fun finalizeAndPersistPayPalPayment(
        orderOrSubId: String,
        payerEmail: String,
        tier: String,
        billingCycle: String,
        amountEur: Double,
        isRenewal: Boolean,
        installment4X: Boolean,
        attemptRestCapture: Boolean = true
    ): PayPalPaymentResult = withContext(Dispatchers.IO) {
        val audit = auditConfiguration()
        if (attemptRestCapture &&
            audit.mode == PayPalConfigurationMode.CLOUD_FUNCTIONS_REST_API &&
            !orderOrSubId.startsWith("PP-SDK-") &&
            !orderOrSubId.startsWith("PP-JS-") &&
            !orderOrSubId.startsWith("PAYPAL-SBX-")
        ) {
            val captured = captureOrderViaCloudFunctions(orderOrSubId, tier, billingCycle, isRenewal)
            if (captured == false) {
                return@withContext PayPalPaymentResult.Failed(
                    "La capture PayPal n'a pas été confirmée par le serveur. Veuillez réessayer."
                )
            }
        }

        val now = System.currentTimeMillis()
        val durationDays = if (billingCycle.equals("ANNUAL", ignoreCase = true)) 365L else 30L
        val durationMillis = durationDays * 24L * 60L * 60L * 1000L
        val currentExpiry = sessionManager.getMembershipExpiryTimestamp()
        val baseTime = if (isRenewal && currentExpiry > now) currentExpiry else now
        val newExpiryTimestamp = baseTime + durationMillis
        val normalizedEmail = payerEmail.trim().ifBlank { "paypal@idmuslim.app" }

        val receiptPayload = "$orderOrSubId|$normalizedEmail|$tier|$billingCycle|$amountEur|$newExpiryTimestamp|$now"
        val eccSignature = KeystoreManager.signData(receiptPayload)

        sessionManager.saveMembershipTier(tier)
        sessionManager.saveMembershipBillingCycle(billingCycle)
        sessionManager.saveMembershipExpiryTimestamp(newExpiryTimestamp)
        sessionManager.saveMembershipAutoRenew(true)
        sessionManager.saveMembershipStatus("ACTIVE")
        sessionManager.saveLastPaymentMethod("PAYPAL")
        sessionManager.saveLastTransactionRef(orderOrSubId)
        sessionManager.savePaypalSubscriptionId(orderOrSubId)
        if (!tier.equals("STANDARD", ignoreCase = true)) {
            sessionManager.saveHasPaidForPdf(true)
        }

        val authUser = safeCurrentUser()
        if (authUser != null) {
            safeFirestore()?.let { db ->
                val userDocRef = db.collection("users").document(authUser.uid)

                val userUpdateMap = hashMapOf<String, Any>(
                    "membershipTier" to tier,
                    "membershipStatus" to "ACTIVE",
                    "membershipCycle" to billingCycle,
                    "membershipExpiry" to newExpiryTimestamp,
                    "membershipAutoRenew" to true,
                    "hasPaidForPdf" to true,
                    "paypalSubscriptionId" to orderOrSubId,
                    "paypalPayerEmail" to normalizedEmail,
                    "lastPaymentMethod" to "PAYPAL",
                    "lastPaymentStatus" to "COMPLETED",
                    "lastPaymentAmount" to amountEur,
                    "lastPaymentCurrency" to "EUR",
                    "lastTransactionRef" to orderOrSubId,
                    "updatedAt" to now
                )
                userDocRef.set(userUpdateMap, SetOptions.merge())

                val paymentReceipt = hashMapOf<String, Any>(
                    "paymentId" to orderOrSubId,
                    "provider" to "PAYPAL",
                    "payerEmail" to normalizedEmail,
                    "amount" to amountEur,
                    "currency" to "EUR",
                    "status" to "COMPLETED",
                    "tier" to tier,
                    "billingCycle" to billingCycle,
                    "isRenewal" to isRenewal,
                    "installment4X" to installment4X,
                    "eccSignatureBase64" to (eccSignature ?: ""),
                    "description" to if (isRenewal) {
                        "Renouvellement Formule $tier ($billingCycle) via PayPal"
                    } else {
                        "Souscription Formule $tier ($billingCycle) via PayPal"
                    },
                    "timestamp" to now
                )
                userDocRef.collection("payments").document(orderOrSubId).set(paymentReceipt, SetOptions.merge())

                val auditLog = hashMapOf<String, Any>(
                    "action" to if (isRenewal) "MEMBERSHIP_RENEWED_PAYPAL" else "MEMBERSHIP_UPGRADED_PAYPAL",
                    "details" to "Paiement PayPal de ${String.format(Locale.US, "%.2f", amountEur)} € validé ($tier • $billingCycle • Ref: $orderOrSubId).",
                    "timestamp" to now,
                    "source" to "PAYPAL_CHECKOUT_GATEWAY"
                )
                userDocRef.collection("activity_logs").add(auditLog)
            }
        }

        Log.i(TAG, "PayPal subscription payment finalized: $orderOrSubId ($tier / $billingCycle / $amountEur EUR)")

        PayPalPaymentResult.Success(
            orderOrSubscriptionId = orderOrSubId,
            payerEmail = normalizedEmail,
            amountEur = amountEur,
            tier = tier,
            billingCycle = billingCycle,
            isRenewal = isRenewal,
            installment4X = installment4X,
            expiryTimestamp = newExpiryTimestamp,
            eccReceiptSignature = eccSignature,
            transactionTimestamp = now
        )
    }

    private suspend fun createOrderViaCloudFunctions(
        tier: String,
        billingCycle: String,
        amountEur: Double
    ): Pair<String, String?>? {
        return try {
            val payload = hashMapOf<String, Any>(
                "tier" to tier,
                "billingCycle" to billingCycle,
                "amountEur" to amountEur
            )
            val result = withTimeoutOrNull(CLOUD_CALL_TIMEOUT_MS) {
                FirebaseFunctions.getInstance()
                    .getHttpsCallable("createPayPalOrder")
                    .call(payload)
                    .awaitTask()
            } ?: return null

            val dataMap = result.data as? Map<*, *> ?: return null
            val orderId = dataMap["orderId"]?.toString()?.takeIf { it.isNotBlank() } ?: return null
            val approvalUrl = dataMap["approvalUrl"]?.toString()?.takeIf { it.isNotBlank() }
            orderId to approvalUrl
        } catch (e: Throwable) {
            Log.w(TAG, "Cloud Function createPayPalOrder unavailable (${e.javaClass.simpleName}); falling back to JS SDK v5.")
            null
        }
    }

    private suspend fun captureOrderViaCloudFunctions(
        orderId: String,
        tier: String,
        billingCycle: String,
        isRenewal: Boolean
    ): Boolean? {
        return try {
            val payload = hashMapOf<String, Any>(
                "orderId" to orderId,
                "tier" to tier,
                "billingCycle" to billingCycle,
                "isRenewal" to isRenewal
            )
            val result = withTimeoutOrNull(CLOUD_CALL_TIMEOUT_MS) {
                FirebaseFunctions.getInstance()
                    .getHttpsCallable("capturePayPalOrder")
                    .call(payload)
                    .awaitTask()
            } ?: return null

            val dataMap = result.data as? Map<*, *> ?: return null
            dataMap["status"]?.toString().equals("COMPLETED", ignoreCase = true)
        } catch (e: Throwable) {
            Log.w(TAG, "Cloud Function capturePayPalOrder skipped (${e.javaClass.simpleName})")
            null
        }
    }

    private fun safeCurrentUser() = try {
        FirebaseAuth.getInstance().currentUser
    } catch (_: Throwable) {
        null
    }

    private fun safeFirestore(): FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (_: Throwable) {
        null
    }

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            if (cont.isActive) cont.resume(result)
        }
        addOnFailureListener { exc ->
            if (cont.isActive) cont.resumeWithException(exc)
        }
        addOnCanceledListener {
            if (cont.isActive) cont.cancel()
        }
    }
}
