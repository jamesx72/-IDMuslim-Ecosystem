package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.PayoutConfig
import com.example.data.ReceivedPaymentRecord
import com.example.utils.HapticHelper
import com.example.ui.viewmodels.EventViewModel
import com.example.utils.QRCodeGenerator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentReceptionConfigDialog(
    viewModel: EventViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current

    val currentPayoutConfig by viewModel.payoutConfig.collectAsState()
    val receivedPayments by viewModel.receivedPayments.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Config, 1: Encaisser (QR / POS), 2: Historique

    // Form fields for Tab 0 (Config)
    var accountHolder by remember(currentPayoutConfig) { mutableStateOf(currentPayoutConfig.accountHolderName.ifEmpty { "Association / Mosquée IDMuslim" }) }
    var iban by remember(currentPayoutConfig) { mutableStateOf(currentPayoutConfig.iban.ifEmpty { "FR76 3000 4000 0100 0123 4567 890" }) }
    var bic by remember(currentPayoutConfig) { mutableStateOf(currentPayoutConfig.bicSwift.ifEmpty { "BNPAFRPPXXX" }) }
    var bankName by remember(currentPayoutConfig) { mutableStateOf(currentPayoutConfig.bankName.ifEmpty { "Banque Éthique & Participative" }) }

    var stripeAcct by remember(currentPayoutConfig) { mutableStateOf(currentPayoutConfig.stripeAccountId.ifEmpty { "acct_1N928xKqZ81" }) }
    var stripeLink by remember(currentPayoutConfig) { mutableStateOf(currentPayoutConfig.stripePaymentLink.ifEmpty { "https://buy.stripe.com/test_idmuslim" }) }

    var paypalEmail by remember(currentPayoutConfig) { mutableStateOf(currentPayoutConfig.paypalEmail.ifEmpty { "paiement@idmuslim.org" }) }
    var paypalMe by remember(currentPayoutConfig) { mutableStateOf(currentPayoutConfig.paypalMeUsername.ifEmpty { "idmuslimorg" }) }

    var cryptoUsdt by remember(currentPayoutConfig) { mutableStateOf(currentPayoutConfig.cryptoUsdtAddress.ifEmpty { "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t" }) }
    var cryptoUsdtNet by remember(currentPayoutConfig) { mutableStateOf(currentPayoutConfig.cryptoUsdtNetwork.ifEmpty { "TRC-20" }) }
    var cryptoBtc by remember(currentPayoutConfig) { mutableStateOf(currentPayoutConfig.cryptoBtcAddress.ifEmpty { "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh" }) }
    var cryptoEth by remember(currentPayoutConfig) { mutableStateOf(currentPayoutConfig.cryptoEthAddress.ifEmpty { "0x71C...B29472" }) }

    var defaultCurrency by remember(currentPayoutConfig) { mutableStateOf(currentPayoutConfig.defaultCurrency.ifEmpty { "EUR" }) }
    var orgType by remember(currentPayoutConfig) { mutableStateOf(currentPayoutConfig.organizationType.ifEmpty { "ASSOCIATION_MOSQUEE" }) }
    var autoReceipt by remember(currentPayoutConfig) { mutableStateOf(currentPayoutConfig.autoIssueReceipt) }
    var isSaving by remember { mutableStateOf(false) }

    // Instant POS / Collection state (Tab 1)
    var collectAmount by remember { mutableStateOf("20") }
    var collectMotive by remember { mutableStateOf("Cotisation Adhésion") }
    var collectPayerName by remember { mutableStateOf("") }
    var collectMethod by remember { mutableStateOf("SEPA_VIREMENT") } // SEPA_VIREMENT, STRIPE_CARD, PAYPAL, CRYPTO_USDT
    var generatedQrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showSuccessToast by remember { mutableStateOf(false) }

    // Generate dynamic QR Code for Tab 1
    LaunchedEffect(collectAmount, collectMotive, collectMethod, iban, stripeLink, paypalMe, cryptoUsdt) {
        val payload = when (collectMethod) {
            "SEPA_VIREMENT" -> "BCD\n002\n1\nSCT\n$bic\n$accountHolder\n${iban.replace(" ", "")}\nEUR$collectAmount\n\n$collectMotive"
            "STRIPE_CARD" -> if (stripeLink.isNotBlank()) "$stripeLink?amount=$collectAmount&ref=${collectMotive.replace(" ", "_")}" else "https://buy.stripe.com"
            "PAYPAL" -> if (paypalMe.isNotBlank()) "https://paypal.me/$paypalMe/$collectAmount" else "https://paypal.com"
            "CRYPTO_USDT" -> "tron:$cryptoUsdt?amount=$collectAmount&token=USDT"
            else -> "IDMUSLIM:PAY:$collectAmount:EUR:$collectMotive"
        }
        generatedQrBitmap = QRCodeGenerator.generateQRCode(payload, 450)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp, bottom = 12.dp, start = 12.dp, end = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF047857).copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = Color(0xFF047857),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Réception & Encaissements",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Passerelle de paiement & Collecte",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = {
                        HapticHelper.performClick(context, haptic)
                        onDismiss()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            HapticHelper.performClick(context, haptic)
                            selectedTab = 0
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Canaux & RIB", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            HapticHelper.performClick(context, haptic)
                            selectedTab = 1
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Encaisser", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            HapticHelper.performClick(context, haptic)
                            selectedTab = 2
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Registre (${receivedPayments.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content View for Selected Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> PayoutConfigTab(
                            accountHolder = accountHolder,
                            onAccountHolderChange = { accountHolder = it },
                            iban = iban,
                            onIbanChange = { iban = it },
                            bic = bic,
                            onBicChange = { bic = it },
                            bankName = bankName,
                            onBankNameChange = { bankName = it },
                            stripeAcct = stripeAcct,
                            onStripeAcctChange = { stripeAcct = it },
                            stripeLink = stripeLink,
                            onStripeLinkChange = { stripeLink = it },
                            paypalEmail = paypalEmail,
                            onPaypalEmailChange = { paypalEmail = it },
                            paypalMe = paypalMe,
                            onPaypalMeChange = { paypalMe = it },
                            cryptoUsdt = cryptoUsdt,
                            onCryptoUsdtChange = { cryptoUsdt = it },
                            cryptoUsdtNet = cryptoUsdtNet,
                            onCryptoUsdtNetChange = { cryptoUsdtNet = it },
                            cryptoBtc = cryptoBtc,
                            onCryptoBtcChange = { cryptoBtc = it },
                            cryptoEth = cryptoEth,
                            onCryptoEthChange = { cryptoEth = it },
                            defaultCurrency = defaultCurrency,
                            onDefaultCurrencyChange = { defaultCurrency = it },
                            orgType = orgType,
                            onOrgTypeChange = { orgType = it },
                            autoReceipt = autoReceipt,
                            onAutoReceiptChange = { autoReceipt = it },
                            isSaving = isSaving,
                            onSave = {
                                isSaving = true
                                HapticHelper.performClick(context, haptic)
                                val updatedConfig = PayoutConfig(
                                    accountHolderName = accountHolder,
                                    iban = iban,
                                    bicSwift = bic,
                                    bankName = bankName,
                                    stripeAccountId = stripeAcct,
                                    stripePaymentLink = stripeLink,
                                    paypalEmail = paypalEmail,
                                    paypalMeUsername = paypalMe,
                                    cryptoUsdtAddress = cryptoUsdt,
                                    cryptoUsdtNetwork = cryptoUsdtNet,
                                    cryptoBtcAddress = cryptoBtc,
                                    cryptoEthAddress = cryptoEth,
                                    defaultCurrency = defaultCurrency,
                                    organizationType = orgType,
                                    instantQrEnabled = true,
                                    autoIssueReceipt = autoReceipt,
                                    isConfigured = true,
                                    updatedAt = System.currentTimeMillis()
                                )
                                viewModel.updatePayoutConfig(updatedConfig) {
                                    isSaving = false
                                    Toast.makeText(context, "✅ Paramètres de réception sauvegardés avec succès", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        1 -> InstantCollectTab(
                            collectAmount = collectAmount,
                            onCollectAmountChange = { collectAmount = it },
                            collectMotive = collectMotive,
                            onCollectMotiveChange = { collectMotive = it },
                            collectPayerName = collectPayerName,
                            onCollectPayerNameChange = { collectPayerName = it },
                            collectMethod = collectMethod,
                            onCollectMethodChange = { collectMethod = it },
                            qrBitmap = generatedQrBitmap,
                            currency = defaultCurrency,
                            accountHolder = accountHolder,
                            iban = iban,
                            stripeLink = stripeLink,
                            paypalMe = paypalMe,
                            cryptoAddress = cryptoUsdt,
                            onRecordManualPayment = {
                                val amountVal = collectAmount.toDoubleOrNull() ?: 10.0
                                val newPayment = ReceivedPaymentRecord(
                                    id = "REC-${System.currentTimeMillis().toString().takeLast(6)}",
                                    amount = amountVal,
                                    currency = defaultCurrency,
                                    payerName = collectPayerName.ifBlank { "Donateur Anonyme" },
                                    motive = collectMotive,
                                    paymentMethod = collectMethod,
                                    timestamp = System.currentTimeMillis(),
                                    status = "CONFIRMED",
                                    referenceCode = "ENC-${UUID.randomUUID().toString().take(8).uppercase()}",
                                    taxReceiptEligible = autoReceipt
                                )
                                viewModel.recordReceivedPayment(newPayment) {
                                    HapticHelper.performClick(context, haptic)
                                    Toast.makeText(context, "🎉 Encaissement de $amountVal $defaultCurrency enregistré !", Toast.LENGTH_LONG).show()
                                    collectPayerName = ""
                                    selectedTab = 2 // Move to registry
                                }
                            }
                        )

                        2 -> ReceivedPaymentsRegistryTab(
                            payments = receivedPayments,
                            onDeletePayment = { paymentId ->
                                HapticHelper.performClick(context, haptic)
                                viewModel.deleteReceivedPayment(paymentId)
                            }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 0: Configuration des Canaux de Paiement & RIB
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PayoutConfigTab(
    accountHolder: String,
    onAccountHolderChange: (String) -> Unit,
    iban: String,
    onIbanChange: (String) -> Unit,
    bic: String,
    onBicChange: (String) -> Unit,
    bankName: String,
    onBankNameChange: (String) -> Unit,
    stripeAcct: String,
    onStripeAcctChange: (String) -> Unit,
    stripeLink: String,
    onStripeLinkChange: (String) -> Unit,
    paypalEmail: String,
    onPaypalEmailChange: (String) -> Unit,
    paypalMe: String,
    onPaypalMeChange: (String) -> Unit,
    cryptoUsdt: String,
    onCryptoUsdtChange: (String) -> Unit,
    cryptoUsdtNet: String,
    onCryptoUsdtNetChange: (String) -> Unit,
    cryptoBtc: String,
    onCryptoBtcChange: (String) -> Unit,
    cryptoEth: String,
    onCryptoEthChange: (String) -> Unit,
    defaultCurrency: String,
    onDefaultCurrencyChange: (String) -> Unit,
    orgType: String,
    onOrgTypeChange: (String) -> Unit,
    autoReceipt: Boolean,
    onAutoReceiptChange: (Boolean) -> Unit,
    isSaving: Boolean,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status & Organization Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Statut : Passerelle de Réception Active",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46)
                    )
                    Text(
                        text = "Les montants payés par les fidèles ou adhérents sont crédités directement sur vos comptes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Structure Type Selector
        Text(
            text = "Type de Structure Bénéficiaire",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val orgTypes = listOf(
                "ASSOCIATION_MOSQUEE" to "Mosquée / Asso",
                "WAQF_CHARITY" to "Fonds Waqf",
                "COMMERCE_HALAL" to "Commerce",
                "PARTICULIER" to "Particulier"
            )
            orgTypes.forEach { (typeKey, label) ->
                val isSelected = orgType == typeKey
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOrgTypeChange(typeKey) }
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // SECTION 1: SEPA / VIREMENT BANCAIRE
        Text(
            text = "1. Compte Bancaire & Virement SEPA (RIB)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = accountHolder,
            onValueChange = onAccountHolderChange,
            label = { Text("Titulaire du Compte / Nom officiel") },
            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = iban,
            onValueChange = onIbanChange,
            label = { Text("Numéro IBAN (Format International)") },
            leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                IconButton(onClick = {
                    clipboard.setText(AnnotatedString(iban))
                    Toast.makeText(context, "IBAN copié dans le presse-papier", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copier IBAN", modifier = Modifier.size(20.dp))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = bic,
                onValueChange = onBicChange,
                label = { Text("Code BIC / SWIFT") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
            )
            OutlinedTextField(
                value = bankName,
                onValueChange = onBankNameChange,
                label = { Text("Établissement") },
                modifier = Modifier.weight(1.2f),
                singleLine = true
            )
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // SECTION 2: STRIPE & CARTE BANCAIRE
        Text(
            text = "2. Passerelle Stripe & Encaissement Carte CB",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = stripeAcct,
            onValueChange = onStripeAcctChange,
            label = { Text("ID Compte Stripe Marchand (acct_...)") },
            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF6366F1)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = stripeLink,
            onValueChange = onStripeLinkChange,
            label = { Text("Lien de Paiement Stripe Direct (buy.stripe.com)") },
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF6366F1)) },
            trailingIcon = {
                IconButton(onClick = {
                    clipboard.setText(AnnotatedString(stripeLink))
                    Toast.makeText(context, "Lien Stripe copié", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copier", modifier = Modifier.size(20.dp))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // SECTION 3: PAYPAL
        Text(
            text = "3. Compte PayPal Récepteur",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = paypalEmail,
            onValueChange = onPaypalEmailChange,
            label = { Text("Email PayPal de Réception") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF0284C7)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = paypalMe,
            onValueChange = onPaypalMeChange,
            label = { Text("Identifiant PayPal.Me (ex: idmuslimorg)") },
            leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = Color(0xFF0284C7)) },
            prefix = { Text("paypal.me/", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // SECTION 4: CRYPTO & STABLECOINS HALAL
        Text(
            text = "4. Portefeuilles Cryptomonnaies (USDT / BTC / ETH)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val networks = listOf("TRC-20", "ERC-20", "Polygon")
            networks.forEach { net ->
                val isSelected = cryptoUsdtNet == net
                FilterChip(
                    selected = isSelected,
                    onClick = { onCryptoUsdtNetChange(net) },
                    label = { Text(net, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        OutlinedTextField(
            value = cryptoUsdt,
            onValueChange = onCryptoUsdtChange,
            label = { Text("Adresse USDT ($cryptoUsdtNet)") },
            leadingIcon = { Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = Color(0xFF10B981)) },
            trailingIcon = {
                IconButton(onClick = {
                    clipboard.setText(AnnotatedString(cryptoUsdt))
                    Toast.makeText(context, "Adresse USDT copiée", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copier", modifier = Modifier.size(20.dp))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = cryptoBtc,
            onValueChange = onCryptoBtcChange,
            label = { Text("Adresse Bitcoin (BTC)") },
            leadingIcon = { Icon(Icons.Default.Token, contentDescription = null, tint = Color(0xFFF59E0B)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // Auto Receipt Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Reçu Fiscal & Attestation Automatique",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Délivre immédiatement un justificatif numérique conforme au donateur.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = autoReceipt,
                onCheckedChange = onAutoReceiptChange
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Save Button
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isSaving,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Enregistrement en cours...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enregistrer les Paramètres de Réception", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// -------------------------------------------------------------
// TAB 1: Encaisser / Collecte Directe (POS QR Code)
// -------------------------------------------------------------
@Composable
private fun InstantCollectTab(
    collectAmount: String,
    onCollectAmountChange: (String) -> Unit,
    collectMotive: String,
    onCollectMotiveChange: (String) -> Unit,
    collectPayerName: String,
    onCollectPayerNameChange: (String) -> Unit,
    collectMethod: String,
    onCollectMethodChange: (String) -> Unit,
    qrBitmap: Bitmap?,
    currency: String,
    accountHolder: String,
    iban: String,
    stripeLink: String,
    paypalMe: String,
    cryptoAddress: String,
    onRecordManualPayment: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Presets for Amount
        Text(
            text = "Montant à Encaisser ($currency)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("10", "20", "50", "100", "250").forEach { preset ->
                val isSelected = collectAmount == preset
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFF047857) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF059669) else Color.Transparent),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCollectAmountChange(preset) }
                ) {
                    Text(
                        text = "$preset€",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = collectAmount,
            onValueChange = onCollectAmountChange,
            label = { Text("Montant personnalisé") },
            leadingIcon = { Text("€", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 14.dp)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Motive Selector
        Text(
            text = "Motif de l'Encaissement",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val motives = listOf("Cotisation", "Don Waqf", "Zakat Al-Maal", "Sadaqah")
            motives.forEach { motive ->
                val isSelected = collectMotive.contains(motive, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCollectMotiveChange(motive) }
                ) {
                    Text(
                        text = motive,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp),
                        maxLines = 1
                    )
                }
            }
        }

        // Payer Name (Optional)
        OutlinedTextField(
            value = collectPayerName,
            onValueChange = onCollectPayerNameChange,
            label = { Text("Nom du payeur / fidèle (Facultatif)") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Payment Channel Selector
        Text(
            text = "Canal Présenté sur le QR Code",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val channels = listOf(
                "SEPA_VIREMENT" to "Virement SEPA",
                "STRIPE_CARD" to "Carte CB",
                "PAYPAL" to "PayPal",
                "CRYPTO_USDT" to "USDT"
            )
            channels.forEach { (chanId, label) ->
                val isSelected = collectMethod == chanId
                FilterChip(
                    selected = isSelected,
                    onClick = { onCollectMethodChange(chanId) },
                    label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // QR Code Display Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Scannez pour payer $collectAmount $currency",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Motif : $collectMotive • Bénéficiaire : $accountHolder",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (qrBitmap != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(200.dp)
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code de paiement",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        )
                    }
                } else {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                }

                // Action Buttons under QR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareText = "Demande de paiement IDMuslim\nMontant : $collectAmount €\nMotif : $collectMotive\nBénéficiaire : $accountHolder\nIBAN : $iban"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Partager la demande de paiement"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Partager", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val info = "IBAN: $iban | Montant: $collectAmount € | Réf: $collectMotive"
                            clipboard.setText(AnnotatedString(info))
                            Toast.makeText(context, "Données de paiement copiées", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copier", fontSize = 12.sp)
                    }
                }

                // Instant Confirm Button
                Button(
                    onClick = onRecordManualPayment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Valider & Enregistrer l'Encaissement", fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// -------------------------------------------------------------
// TAB 2: Registre des Encaissements & Reçus
// -------------------------------------------------------------
@Composable
private fun ReceivedPaymentsRegistryTab(
    payments: List<ReceivedPaymentRecord>,
    onDeletePayment: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val totalCollected = payments.filter { it.status == "CONFIRMED" }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary KPI Banner
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F172A)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "TOTAL DES ENCAISSEMENTS REÇUS",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF34D399)
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "%.2f".format(totalCollected),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "EUR",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
                Text(
                    text = "${payments.size} transactions enregistrées • Synchronisation Cloud & Justificatifs conformes",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Text(
            text = "Historique Chronologique des Paiements",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        if (payments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Aucun paiement encaissé pour le moment.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            payments.forEach { payment ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = when (payment.paymentMethod) {
                                    "SEPA_VIREMENT" -> Color(0xFF0284C7).copy(alpha = 0.15f)
                                    "STRIPE_CARD" -> Color(0xFF6366F1).copy(alpha = 0.15f)
                                    "PAYPAL" -> Color(0xFF0284C7).copy(alpha = 0.15f)
                                    else -> Color(0xFF10B981).copy(alpha = 0.15f)
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (payment.paymentMethod) {
                                            "SEPA_VIREMENT" -> Icons.Default.AccountBalance
                                            "STRIPE_CARD" -> Icons.Default.CreditCard
                                            "PAYPAL" -> Icons.Default.Payment
                                            else -> Icons.Default.Token
                                        },
                                        contentDescription = null,
                                        tint = when (payment.paymentMethod) {
                                            "SEPA_VIREMENT" -> Color(0xFF0284C7)
                                            "STRIPE_CARD" -> Color(0xFF6366F1)
                                            "PAYPAL" -> Color(0xFF0284C7)
                                            else -> Color(0xFF10B981)
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = payment.motive,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${payment.payerName} • ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(payment.timestamp))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Réf: ${payment.referenceCode}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "+%.2f %s".format(payment.amount, payment.currency),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF059669)
                            )

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF059669).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = payment.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
