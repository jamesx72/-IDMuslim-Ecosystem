package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.locales.Translations
import com.example.ui.viewmodels.EventViewModel
import com.example.utils.HapticHelper
import java.text.SimpleDateFormat
import java.util.*

enum class MembershipPlan(
    val id: String,
    val titleFr: String,
    val titleEn: String,
    val subtitleFr: String,
    val monthlyPriceEur: Double,
    val annualPriceEur: Double,
    val primaryColor: Color,
    val secondaryColor: Color,
    val badgeLabelFr: String?,
    val isPopular: Boolean = false
) {
    STANDARD(
        id = "STANDARD",
        titleFr = "Standard",
        titleEn = "Standard",
        subtitleFr = "Pour découvrir l'identité numérique islamique",
        monthlyPriceEur = 0.0,
        annualPriceEur = 0.0,
        primaryColor = Color(0xFF64748B),
        secondaryColor = Color(0xFF94A3B8),
        badgeLabelFr = "GRATUIT",
        isPopular = false
    ),
    EMERALD(
        id = "EMERALD",
        titleFr = "Émeraude Privilège",
        titleEn = "Emerald Privilege",
        subtitleFr = "L'expérience complète & sécurité maximale certifiée",
        monthlyPriceEur = 2.99,
        annualPriceEur = 29.90,
        primaryColor = Color(0xFF059669),
        secondaryColor = Color(0xFF10B981),
        badgeLabelFr = "LE PLUS POPULAIRE",
        isPopular = true
    ),
    WAQF_PATRON(
        id = "WAQF_PATRON",
        titleFr = "Bienfaiteur Waqf",
        titleEn = "Waqf Patron",
        subtitleFr = "Mécénat communautaire, pack famille & impact solidaire",
        monthlyPriceEur = 9.99,
        annualPriceEur = 99.00,
        primaryColor = Color(0xFFD97706),
        secondaryColor = Color(0xFFFFD700),
        badgeLabelFr = "MÉCÈNE WAQF",
        isPopular = false
    )
}

enum class PaymentMethod(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    CARD(
        id = "CARD",
        title = "Carte Bancaire",
        subtitle = "CB, Visa, MasterCard, Amex (3D Secure)",
        icon = Icons.Default.CreditCard,
        accentColor = Color(0xFF2563EB)
    ),
    PAYPAL(
        id = "PAYPAL",
        title = "PayPal",
        subtitle = "Paiement en 1 clic ou 4X sans frais",
        icon = Icons.Default.AccountBalanceWallet,
        accentColor = Color(0xFF0079C1)
    ),
    CRYPTO(
        id = "CRYPTO",
        title = "Crypto Monnaie",
        subtitle = "USDT (TRC20/ERC20), BTC, ETH",
        icon = Icons.Default.CurrencyBitcoin,
        accentColor = Color(0xFFF59E0B)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipSubscriptionDialog(
    viewModel: EventViewModel,
    language: String = "fr",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current

    val currentTier by viewModel.membershipTier.collectAsState()
    val currentBillingCycle by viewModel.membershipBillingCycle.collectAsState()
    val expiryTimestamp by viewModel.membershipExpiryTimestamp.collectAsState()
    val autoRenew by viewModel.membershipAutoRenew.collectAsState()
    val waqfContribution by viewModel.waqfContributionTotal.collectAsState()

    var selectedPlan by remember {
        mutableStateOf(
            when (currentTier.uppercase()) {
                "WAQF_PATRON", "WAQF" -> MembershipPlan.WAQF_PATRON
                "EMERALD", "VERIFIED_PREMIUM" -> MembershipPlan.EMERALD
                else -> MembershipPlan.EMERALD
            }
        )
    }

    var isAnnualBilling by remember {
        mutableStateOf(currentBillingCycle.equals("ANNUAL", ignoreCase = true))
    }

    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CARD) }
    var isProcessingPayment by remember { mutableStateOf(false) }
    var processingPhase by remember { mutableStateOf("") }
    var showPaymentSuccess by remember { mutableStateOf(false) }
    var showWaqfDonationSheet by remember { mutableStateOf(false) }

    // Card Input States
    var cardHolderName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var showCvvPassword by remember { mutableStateOf(false) }

    // Card Input Errors
    var cardHolderError by remember { mutableStateOf<String?>(null) }
    var cardNumberError by remember { mutableStateOf<String?>(null) }
    var cardExpiryError by remember { mutableStateOf<String?>(null) }
    var cardCvvError by remember { mutableStateOf<String?>(null) }

    // PayPal Input States
    var payPalEmail by remember { mutableStateOf("") }
    var payPalEmailError by remember { mutableStateOf<String?>(null) }
    var payPalInstallment4X by remember { mutableStateOf(false) }

    // Crypto Input States
    var selectedCryptoNetwork by remember { mutableStateOf("USDT_TRC20") } // USDT_TRC20, USDT_ERC20, BTC, ETH
    var cryptoTxHash by remember { mutableStateOf("") }
    var cryptoTxHashError by remember { mutableStateOf<String?>(null) }
    var cryptoAddressCopied by remember { mutableStateOf(false) }

    val formattedExpiry = remember(expiryTimestamp) {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
        sdf.format(Date(expiryTimestamp))
    }

    val activePrice = if (isAnnualBilling) selectedPlan.annualPriceEur else selectedPlan.monthlyPriceEur

    // Formatting & Validation Helpers
    fun formatCardNumber(input: String): String {
        val clean = input.replace("[^\\d]".toRegex(), "")
        val sb = StringBuilder()
        for (i in clean.indices) {
            if (i > 0 && i % 4 == 0) sb.append(" ")
            sb.append(clean[i])
        }
        return sb.toString().take(19) // Max 16 digits + 3 spaces
    }

    fun formatExpiry(input: String): String {
        val clean = input.replace("[^\\d]".toRegex(), "")
        return if (clean.length >= 2) {
            clean.substring(0, 2) + "/" + clean.substring(2).take(2)
        } else {
            clean
        }.take(5)
    }

    fun getCardBrand(num: String): String {
        val clean = num.replace("\\s".toRegex(), "")
        return when {
            clean.startsWith("4") -> "Visa"
            clean.startsWith("5") || clean.startsWith("2") -> "MasterCard"
            clean.startsWith("34") || clean.startsWith("37") -> "American Express"
            clean.startsWith("6") -> "CB / Discover"
            else -> "Carte Bancaire"
        }
    }

    fun isValidLuhn(number: String): Boolean {
        val cleanNumber = number.replace("\\s".toRegex(), "")
        if (cleanNumber.length < 13 || cleanNumber.length > 19) return false
        var sum = 0
        var alternate = false
        for (i in cleanNumber.length - 1 downTo 0) {
            var n = cleanNumber[i] - '0'
            if (n < 0 || n > 9) return false
            if (alternate) {
                n *= 2
                if (n > 9) n = (n % 10) + 1
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    fun isValidExpiryDate(expiry: String): Boolean {
        val clean = expiry.replace("/", "").replace("\\s".toRegex(), "")
        if (clean.length != 4) return false
        val month = clean.substring(0, 2).toIntOrNull() ?: return false
        val yearSuffix = clean.substring(2, 4).toIntOrNull() ?: return false
        if (month < 1 || month > 12) return false
        
        val currentYear = Calendar.getInstance().get(Calendar.YEAR) % 100
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        if (yearSuffix < currentYear) return false
        if (yearSuffix == currentYear && month < currentMonth) return false
        return true
    }

    fun validateInputs(): Boolean {
        var isValid = true

        when (selectedPaymentMethod) {
            PaymentMethod.CARD -> {
                if (cardHolderName.trim().length < 3) {
                    cardHolderError = "Veuillez saisir le nom complet sur la carte."
                    isValid = false
                } else {
                    cardHolderError = null
                }

                val cleanCard = cardNumber.replace("\\s".toRegex(), "")
                if (cleanCard.length < 15 || cleanCard.length > 16 || !isValidLuhn(cardNumber)) {
                    cardNumberError = "Numéro de carte invalide (vérification Luhn)."
                    isValid = false
                } else {
                    cardNumberError = null
                }

                if (!isValidExpiryDate(cardExpiry)) {
                    cardExpiryError = "Date d'expiration invalide (MM/AA)."
                    isValid = false
                } else {
                    cardExpiryError = null
                }

                val isAmex = getCardBrand(cardNumber) == "American Express"
                val expectedCvvLen = if (isAmex) 4 else 3
                if (cardCvv.replace("[^\\d]".toRegex(), "").length != expectedCvvLen) {
                    cardCvvError = "Le CVV doit comporter $expectedCvvLen chiffres."
                    isValid = false
                } else {
                    cardCvvError = null
                }
            }
            PaymentMethod.PAYPAL -> {
                val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)\$".toRegex()
                if (payPalEmail.trim().isEmpty() || !payPalEmail.matches(emailRegex)) {
                    payPalEmailError = "Veuillez saisir une adresse email PayPal valide."
                    isValid = false
                } else {
                    payPalEmailError = null
                }
            }
            PaymentMethod.CRYPTO -> {
                if (cryptoTxHash.trim().isNotEmpty() && cryptoTxHash.length < 10) {
                    cryptoTxHashError = "Le Hash de transaction (TXID) doit comporter au moins 10 caractères."
                    isValid = false
                } else {
                    cryptoTxHashError = null
                }
            }
        }

        return isValid
    }

    val infiniteTransition = rememberInfiniteTransition(label = "subInfinite")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Dialog(
        onDismissRequest = {
            if (!isProcessingPayment) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(28.dp))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF10B981), Color(0xFFFFD700), Color(0xFF059669))
                    ),
                    shape = RoundedCornerShape(28.dp)
                ),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Dialog Top Bar
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF059669).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF10B981)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "ADHÉSION & PAIEMENT SÉCURISÉ",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "CB • PayPal • Crypto (USDT, BTC) • Waqf",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (!isProcessingPayment) {
                                    HapticHelper.performClick(context, haptic)
                                    onDismiss()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer")
                        }
                    }
                }

                // Main Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Current Status Header Card
                    item {
                        CurrentSubscriptionStatusCard(
                            currentTier = currentTier,
                            billingCycle = currentBillingCycle,
                            expiryDate = formattedExpiry,
                            autoRenew = autoRenew,
                            waqfContribution = waqfContribution,
                            onToggleAutoRenew = {
                                HapticHelper.performClick(context, haptic)
                                viewModel.updateMembershipAutoRenew(!autoRenew)
                            },
                            onOpenWaqfDonate = {
                                HapticHelper.performClick(context, haptic)
                                showWaqfDonationSheet = true
                            }
                        )
                    }

                    // Billing Cycle Toggle (Monthly vs Annual with discount badge)
                    item {
                        BillingCycleSelector(
                            isAnnual = isAnnualBilling,
                            onCycleChanged = { annual ->
                                HapticHelper.performClick(context, haptic)
                                isAnnualBilling = annual
                            }
                        )
                    }

                    // Plan Selection Cards
                    item {
                        Text(
                            text = "1. Choisissez votre formule",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            MembershipPlan.values().forEach { plan ->
                                PlanCard(
                                    plan = plan,
                                    isSelected = selectedPlan == plan,
                                    isCurrent = currentTier.equals(plan.id, ignoreCase = true) ||
                                            (plan.id == "EMERALD" && currentTier.equals("VERIFIED_PREMIUM", ignoreCase = true)),
                                    isAnnual = isAnnualBilling,
                                    shimmerAlpha = shimmerAlpha,
                                    onSelect = {
                                        HapticHelper.performClick(context, haptic)
                                        selectedPlan = plan
                                    }
                                )
                            }
                        }
                    }

                    // Feature Matrix Breakdown
                    item {
                        PlanFeaturesBreakdown(selectedPlan = selectedPlan)
                    }

                    // Payment Method Configuration Section (Only for paid plans)
                    if (selectedPlan != MembershipPlan.STANDARD) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "2. Mode de Règlement Sécurisé",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "SSL 256-bit",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = Color(0xFF059669),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Payment Method Tabs
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        PaymentMethod.values().forEach { method ->
                                            val isSelected = selectedPaymentMethod == method
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) method.accentColor else MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isSelected) method.accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        HapticHelper.performClick(context, haptic)
                                                        selectedPaymentMethod = method
                                                    }
                                                    .padding(vertical = 2.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = method.icon,
                                                        contentDescription = method.title,
                                                        tint = if (isSelected) Color.White else method.accentColor,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = when (method) {
                                                            PaymentMethod.CARD -> "Carte CB"
                                                            PaymentMethod.PAYPAL -> "PayPal"
                                                            PaymentMethod.CRYPTO -> "Crypto"
                                                        },
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        fontSize = 11.sp,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                                    // Payment Method Form Details
                                    when (selectedPaymentMethod) {
                                        PaymentMethod.CARD -> {
                                            CardPaymentConfigForm(
                                                cardHolderName = cardHolderName,
                                                cardNumber = cardNumber,
                                                cardExpiry = cardExpiry,
                                                cardCvv = cardCvv,
                                                showCvvPassword = showCvvPassword,
                                                cardHolderError = cardHolderError,
                                                cardNumberError = cardNumberError,
                                                cardExpiryError = cardExpiryError,
                                                cardCvvError = cardCvvError,
                                                onCardHolderChange = { cardHolderName = it; cardHolderError = null },
                                                onCardNumberChange = { cardNumber = formatCardNumber(it); cardNumberError = null },
                                                onCardExpiryChange = { cardExpiry = formatExpiry(it); cardExpiryError = null },
                                                onCardCvvChange = { cardCvv = it.replace("[^\\d]".toRegex(), "").take(4); cardCvvError = null },
                                                onToggleCvvVisibility = { showCvvPassword = !showCvvPassword },
                                                getCardBrand = ::getCardBrand
                                            )
                                        }
                                        PaymentMethod.PAYPAL -> {
                                            PayPalPaymentConfigForm(
                                                activePrice = activePrice,
                                                payPalEmail = payPalEmail,
                                                payPalEmailError = payPalEmailError,
                                                payPalInstallment4X = payPalInstallment4X,
                                                onEmailChange = { payPalEmail = it; payPalEmailError = null },
                                                onToggleInstallment = { payPalInstallment4X = it }
                                            )
                                        }
                                        PaymentMethod.CRYPTO -> {
                                            CryptoPaymentConfigForm(
                                                activePrice = activePrice,
                                                selectedNetwork = selectedCryptoNetwork,
                                                cryptoTxHash = cryptoTxHash,
                                                cryptoTxHashError = cryptoTxHashError,
                                                cryptoAddressCopied = cryptoAddressCopied,
                                                onSelectNetwork = { selectedCryptoNetwork = it },
                                                onTxHashChange = { cryptoTxHash = it.trim(); cryptoTxHashError = null },
                                                onCopyAddress = { address ->
                                                    clipboardManager.setText(AnnotatedString(address))
                                                    cryptoAddressCopied = true
                                                    HapticHelper.performClick(context, haptic)
                                                    Toast.makeText(context, "Adresse de dépôt copiée !", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Security & Islamic Ethics Guarantee
                    item {
                        IslamicEthicsBanner()
                    }
                }

                // Dialog Footer CTA
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isCurrentSelected = currentTier.equals(selectedPlan.id, ignoreCase = true) ||
                                (selectedPlan.id == "EMERALD" && currentTier.equals("VERIFIED_PREMIUM", ignoreCase = true))

                        if (selectedPlan == MembershipPlan.STANDARD) {
                            if (isCurrentSelected) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Plan Standard Actif • Fermer")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        HapticHelper.performClick(context, haptic)
                                        viewModel.cancelMembership {
                                            onDismiss()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Revenir au plan Standard Gratuit")
                                }
                            }
                        } else {
                            // Premium / Waqf Upgrade Button
                            Button(
                                onClick = {
                                    HapticHelper.performClick(context, haptic)
                                    if (validateInputs()) {
                                        isProcessingPayment = true
                                        val cycleStr = if (isAnnualBilling) "ANNUAL" else "MONTHLY"
                                        val isRenewal = !currentTier.equals("STANDARD", ignoreCase = true)

                                        if (selectedPaymentMethod == PaymentMethod.CARD) {
                                            processingPhase = "Traitement sécurisé Stripe Android SDK (3DS/SCA)..."
                                            val expMonth = cardExpiry.take(2).toIntOrNull() ?: 12
                                            val expYear = ("20" + cardExpiry.takeLast(2)).toIntOrNull() ?: 2028

                                            viewModel.processStripeSubscriptionRenewal(
                                                context = context,
                                                cardHolderName = cardHolderName,
                                                cardNumber = cardNumber,
                                                expMonth = expMonth,
                                                expYear = expYear,
                                                cvc = cardCvv,
                                                tier = selectedPlan.id,
                                                billingCycle = cycleStr,
                                                amountEur = activePrice,
                                                isRenewal = isRenewal
                                            ) { result ->
                                                isProcessingPayment = false
                                                when (result) {
                                                    is com.example.payment.StripePaymentResult.Success -> {
                                                        showPaymentSuccess = true
                                                    }
                                                    is com.example.payment.StripePaymentResult.Failed -> {
                                                        Toast.makeText(context, "Erreur Stripe: ${result.errorMessage}", Toast.LENGTH_LONG).show()
                                                    }
                                                    is com.example.payment.StripePaymentResult.Canceled -> {
                                                        Toast.makeText(context, "Paiement annulé.", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        } else {
                                            processingPhase = "Chiffrement SSL 256-bit de la transaction..."
                                            val generatedTxRef = "IDM-" + UUID.randomUUID().toString().take(8).uppercase()

                                            viewModel.updateMembershipTier(
                                                tier = selectedPlan.id,
                                                billingCycle = cycleStr,
                                                paymentMethod = selectedPaymentMethod.id,
                                                transactionRef = if (selectedPaymentMethod == PaymentMethod.CRYPTO && cryptoTxHash.isNotEmpty()) cryptoTxHash else generatedTxRef
                                            ) {
                                                isProcessingPayment = false
                                                showPaymentSuccess = true
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Veuillez vérifier les informations de paiement.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .shadow(6.dp, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedPlan == MembershipPlan.WAQF_PATRON) Color(0xFFD97706) else Color(0xFF059669)
                                ),
                                enabled = !isProcessingPayment
                            ) {
                                if (isProcessingPayment) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = processingPhase,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (selectedPlan == MembershipPlan.WAQF_PATRON) Icons.Default.VolunteerActivism else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val priceText = if (isAnnualBilling) {
                                        "${selectedPlan.annualPriceEur} € / an"
                                    } else {
                                        "${selectedPlan.monthlyPriceEur} € / mois"
                                    }
                                    val methodLabel = when (selectedPaymentMethod) {
                                        PaymentMethod.CARD -> "par Carte"
                                        PaymentMethod.PAYPAL -> "par PayPal"
                                        PaymentMethod.CRYPTO -> "en Crypto"
                                    }
                                    Text(
                                        text = "Payer $priceText $methodLabel",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Paiement sécurisé sans engagement • Résiliation en 1 clic • Conforme aux standards éthiques",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Success Dialog
    if (showPaymentSuccess) {
        AlertDialog(
            onDismissRequest = {
                showPaymentSuccess = false
                onDismiss()
            },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    border = BorderStroke(2.dp, Color(0xFF10B981)),
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Mabrouk ! Adhésion Confirmée 🎉",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Votre formule ${selectedPlan.titleFr} est désormais active et certifiée.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF059669).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("✅ Hologramme dynamique débloqué", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            Text("✅ Export Google Wallet & Certificat PDF illimités", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            Text("✅ Badge officiel sur votre carte d'identité", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            Text("✅ Émulation NFC HCE sans-contact prête", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            if (selectedPlan == MembershipPlan.WAQF_PATRON) {
                                Text("✅ Reçu de don Waqf disponible", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color(0xFFD97706))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPaymentSuccess = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Accéder à mes avantages")
                }
            }
        )
    }

    // Waqf Contribution Modal
    if (showWaqfDonationSheet) {
        WaqfDonationDialog(
            onDismiss = { showWaqfDonationSheet = false },
            onConfirmDonate = { amount ->
                viewModel.addWaqfContribution(amount) {
                    showWaqfDonationSheet = false
                }
            }
        )
    }
}

@Composable
fun CardPaymentConfigForm(
    cardHolderName: String,
    cardNumber: String,
    cardExpiry: String,
    cardCvv: String,
    showCvvPassword: Boolean,
    cardHolderError: String?,
    cardNumberError: String?,
    cardExpiryError: String?,
    cardCvvError: String?,
    onCardHolderChange: (String) -> Unit,
    onCardNumberChange: (String) -> Unit,
    onCardExpiryChange: (String) -> Unit,
    onCardCvvChange: (String) -> Unit,
    onToggleCvvVisibility: () -> Unit,
    getCardBrand: (String) -> String
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Holographic Live Card Preview
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(16.dp)),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF2563EB))
                        )
                    )
                    .border(1.dp, Color(0xFF60A5FA).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFD700).copy(alpha = 0.85f),
                            modifier = Modifier.size(width = 30.dp, height = 22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("EMV", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                        Text(
                            text = getCardBrand(cardNumber),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = if (cardNumber.isEmpty()) "•••• •••• •••• ••••" else cardNumber,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TITULAIRE", fontSize = 7.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            Text(
                                text = if (cardHolderName.isEmpty()) "VOTRE NOM" else cardHolderName.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("EXPIRE", fontSize = 7.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            Text(
                                text = if (cardExpiry.isEmpty()) "MM/AA" else cardExpiry,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Form Fields
        OutlinedTextField(
            value = cardHolderName,
            onValueChange = onCardHolderChange,
            label = { Text("Nom sur la carte") },
            placeholder = { Text("Ex: Mohammed Al-Mansoor") },
            isError = cardHolderError != null,
            supportingText = cardHolderError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = cardNumber,
            onValueChange = onCardNumberChange,
            label = { Text("Numéro de carte") },
            placeholder = { Text("4532 •••• •••• ••••") },
            isError = cardNumberError != null,
            supportingText = cardNumberError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = {
                Icon(Icons.Default.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                Text(
                    text = getCardBrand(cardNumber),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = cardExpiry,
                onValueChange = onCardExpiryChange,
                label = { Text("Exp. (MM/AA)") },
                placeholder = { Text("12/28") },
                isError = cardExpiryError != null,
                supportingText = cardExpiryError?.let { { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 10.sp) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = cardCvv,
                onValueChange = onCardCvvChange,
                label = { Text("CVV / CVC") },
                placeholder = { Text("123") },
                isError = cardCvvError != null,
                supportingText = cardCvvError?.let { { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 10.sp) } },
                visualTransformation = if (showCvvPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onToggleCvvVisibility) {
                        Icon(
                            imageVector = if (showCvvPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "CVV toggle",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF6366F1).copy(alpha = 0.08f),
            border = BorderStroke(0.8.dp, Color(0xFF6366F1).copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(16.dp)
                )
                Column {
                    Text(
                        text = "Passerelle Stripe Android SDK • 3D Secure / SCA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color(0xFF4338CA)
                    )
                    Text(
                        text = "Renouvellement automatique chiffré de bout en bout • Webhook Firestore synchronisé",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 8.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PayPalPaymentConfigForm(
    activePrice: Double,
    payPalEmail: String,
    payPalEmailError: String?,
    payPalInstallment4X: Boolean,
    onEmailChange: (String) -> Unit,
    onToggleInstallment: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0079C1).copy(alpha = 0.1f),
            border = BorderStroke(1.dp, Color(0xFF0079C1).copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF0079C1),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("P", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                    }
                }
                Column {
                    Text(
                        text = "Paiement Rapide et Sécurisé PayPal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF003087)
                    )
                    Text(
                        text = "Protection des Achats PayPal pendant 180 jours.",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = Color(0xFF0079C1)
                    )
                }
            }
        }

        OutlinedTextField(
            value = payPalEmail,
            onValueChange = onEmailChange,
            label = { Text("Adresse email de votre compte PayPal") },
            placeholder = { Text("votre-compte@paypal.com") },
            isError = payPalEmailError != null,
            supportingText = payPalEmailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF0079C1))
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Option 4X sans frais
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (payPalInstallment4X) Color(0xFF0079C1).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                1.dp,
                if (payPalInstallment4X) Color(0xFF0079C1) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleInstallment(!payPalInstallment4X) }
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = payPalInstallment4X,
                        onCheckedChange = onToggleInstallment,
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0079C1))
                    )
                    Column {
                        Text(
                            text = "Payer en 4X sans frais",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val monthlySplit = String.format(Locale.US, "%.2f", activePrice / 4.0)
                        Text(
                            text = "Soit 4 mensualités de $monthlySplit € sans aucun frais additionnel",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CryptoPaymentConfigForm(
    activePrice: Double,
    selectedNetwork: String,
    cryptoTxHash: String,
    cryptoTxHashError: String?,
    cryptoAddressCopied: Boolean,
    onSelectNetwork: (String) -> Unit,
    onTxHashChange: (String) -> Unit,
    onCopyAddress: (String) -> Unit
) {
    val networkData = when (selectedNetwork) {
        "USDT_TRC20" -> Triple(
            "USDT (Tron TRC-20)",
            "TYDzsYbmQgZwt8xN1qHq7R8vKjN2oPn9xL",
            String.format(Locale.US, "%.2f USDT", activePrice * 1.08)
        )
        "USDT_ERC20" -> Triple(
            "USDT (Ethereum ERC-20)",
            "0x71C8364237F026a7e08Db86401088716b1EdaD89",
            String.format(Locale.US, "%.2f USDT", activePrice * 1.08)
        )
        "BTC" -> Triple(
            "Bitcoin (BTC)",
            "bc1qxy2kg3f8pxw69rw0btpnwhh0g30ep58etmxkm",
            String.format(Locale.US, "%.6f BTC", (activePrice / 62000.0))
        )
        else -> Triple(
            "Ethereum (ETH)",
            "0x71C8364237F026a7e08Db86401088716b1EdaD89",
            String.format(Locale.US, "%.5f ETH", (activePrice / 2450.0))
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Sélectionnez la monnaie / réseau :",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "USDT_TRC20" to "USDT (TRC20)",
                "USDT_ERC20" to "USDT (ERC20)",
                "BTC" to "Bitcoin (BTC)",
                "ETH" to "Ethereum (ETH)"
            ).forEach { (id, label) ->
                val isSelected = selectedNetwork == id
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFFF59E0B) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectNetwork(id) }
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
                    )
                }
            }
        }

        // Crypto Payment Details Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFFEF3C7).copy(alpha = 0.6f),
            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("MONTANT EXACT À ENVOYER", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                        Text(
                            text = networkData.third,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFB45309)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF59E0B)
                    ) {
                        Text(
                            text = networkData.first,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Divider(color = Color(0xFFF59E0B).copy(alpha = 0.3f))

                Text("ADRESSE DE DÉPÔT SÉCURISÉE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = networkData.second,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(
                        onClick = { onCopyAddress(networkData.second) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (cryptoAddressCopied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                            contentDescription = "Copier",
                            tint = if (cryptoAddressCopied) Color(0xFF10B981) else Color(0xFFD97706),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // TXID Input (Optional or simulated instant verification)
        OutlinedTextField(
            value = cryptoTxHash,
            onValueChange = onTxHashChange,
            label = { Text("Hash de transaction (TXID / Facultatif)") },
            placeholder = { Text("Ex: 0x4f8a... (ou valider pour confirmation automatique)") },
            isError = cryptoTxHashError != null,
            supportingText = cryptoTxHashError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun CurrentSubscriptionStatusCard(
    currentTier: String,
    billingCycle: String,
    expiryDate: String,
    autoRenew: Boolean,
    waqfContribution: Float,
    onToggleAutoRenew: () -> Unit,
    onOpenWaqfDonate: () -> Unit
) {
    val isEmerald = currentTier.equals("EMERALD", ignoreCase = true) || currentTier.equals("VERIFIED_PREMIUM", ignoreCase = true)
    val isWaqf = currentTier.equals("WAQF_PATRON", ignoreCase = true) || currentTier.equals("WAQF", ignoreCase = true)

    val gradientColors = when {
        isWaqf -> listOf(Color(0xFF78350F), Color(0xFFB45309), Color(0xFFD97706))
        isEmerald -> listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF059669))
        else -> listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF475569))
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .border(1.5.dp, if (isWaqf) Color(0xFFFFD700) else if (isEmerald) Color(0xFF34D399) else Color(0xFF94A3B8), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isWaqf) Icons.Default.VolunteerActivism else if (isEmerald) Icons.Default.Verified else Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = if (isWaqf) Color(0xFFFFD700) else if (isEmerald) Color(0xFF34D399) else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "STATUT ACTUEL",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = when {
                                    isWaqf -> "Membre Mécène Waqf"
                                    isEmerald -> "Membre Émeraude Privilège"
                                    else -> "Membre Standard Gratuit"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = if (isWaqf || isEmerald) "ACTIF" else "BASIQUE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isWaqf) Color(0xFFFFD700) else if (isEmerald) Color(0xFF34D399) else Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.2f), thickness = 0.8.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isWaqf || isEmerald) "Échéance : $expiryDate" else "Accès gratuit permanent",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        if (isWaqf || isEmerald) {
                            Text(
                                text = if (autoRenew) "• Renouvellement automatique activé" else "• Expire à l'échéance",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    if (isWaqf || isEmerald) {
                        TextButton(
                            onClick = onToggleAutoRenew,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                        ) {
                            Text(
                                text = if (autoRenew) "Désactiver" else "Activer auto",
                                style = MaterialTheme.typography.labelSmall,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }
                }

                if (waqfContribution > 0f) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFD700).copy(alpha = 0.15f),
                        border = BorderStroke(0.8.dp, Color(0xFFFFD700).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Dons Waqf cumulés : $waqfContribution €",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFEAA7)
                                )
                            }
                            TextButton(
                                onClick = onOpenWaqfDonate,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("+ Faire un don", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BillingCycleSelector(
    isAnnual: Boolean,
    onCycleChanged: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Monthly
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (!isAnnual) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onCycleChanged(false) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Mensuel",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (!isAnnual) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Annual (with -20% badge)
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isAnnual) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onCycleChanged(true) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Annuel",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAnnual) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isAnnual) Color(0xFFFFD700) else Color(0xFF10B981),
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Text(
                            text = "-20% (2 mois offerts)",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isAnnual) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlanCard(
    plan: MembershipPlan,
    isSelected: Boolean,
    isCurrent: Boolean,
    isAnnual: Boolean,
    shimmerAlpha: Float,
    onSelect: () -> Unit
) {
    val borderColor = when {
        isSelected -> plan.secondaryColor
        plan.isPopular -> Color(0xFF10B981).copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) plan.primaryColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .shadow(if (isSelected) 4.dp else 0.dp, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = onSelect,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = plan.primaryColor
                        )
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = plan.titleFr,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (isCurrent) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF059669),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "VOTRE PLAN",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = plan.subtitleFr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.5.sp
                        )
                    }
                }

                // Price Badge
                Column(horizontalAlignment = Alignment.End) {
                    if (plan.monthlyPriceEur == 0.0) {
                        Text(
                            text = "0 €",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gratuit à vie",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val displayPrice = if (isAnnual) plan.annualPriceEur else plan.monthlyPriceEur
                        val periodLabel = if (isAnnual) "/ an" else "/ mois"
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$displayPrice €",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = plan.primaryColor
                            )
                            Text(
                                text = " $periodLabel",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isAnnual) {
                            val monthlyEquivalent = String.format(Locale.US, "%.2f", plan.annualPriceEur / 12)
                            Text(
                                text = "soit $monthlyEquivalent €/mois",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.5.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (plan.badgeLabelFr != null && (plan.isPopular || plan == MembershipPlan.WAQF_PATRON)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (plan.isPopular) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFFFD700).copy(alpha = 0.2f),
                    border = BorderStroke(0.8.dp, if (plan.isPopular) Color(0xFF10B981) else Color(0xFFFFD700))
                ) {
                    Text(
                        text = "⭐ ${plan.badgeLabelFr}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black,
                        color = if (plan.isPopular) Color(0xFF047857) else Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PlanFeaturesBreakdown(selectedPlan: MembershipPlan) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Avantages inclus dans ${selectedPlan.titleFr}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = selectedPlan.primaryColor
            )

            val features = when (selectedPlan) {
                MembershipPlan.STANDARD -> listOf(
                    "Carte d'identité numérique IDMuslim de base" to true,
                    "Horaires de prières précis & boussole Qibla en direct" to true,
                    "Accès aux événements communautaires et mosquées" to true,
                    "Stockage local chiffré sur l'appareil" to true,
                    "Hologramme dynamique de sécurité & NFC HCE" to false,
                    "Export Google Wallet Pass officiel" to false,
                    "Certificat d'identité PDF cryptographique sans filigrane" to false,
                    "Badge de certification prioritaire" to false,
                    "Pack familial & mécénat Waqf" to false
                )
                MembershipPlan.EMERALD -> listOf(
                    "Carte d'identité numérique IDMuslim haute sécurité" to true,
                    "Hologramme dynamique Rub el Hizb & effets spéculaires or" to true,
                    "Émulation NFC Host Card Emulation (HCE) sans-contact" to true,
                    "Export Google Wallet (Pay Pass officiel sécurisé)" to true,
                    "Export Certificat PDF cryptographique officiel sans filigrane" to true,
                    "Badge de certification prioritaire Émeraude" to true,
                    "Sauvegarde Cloud chiffrée AES-256 de bout en bout" to true,
                    "Réductions billetterie & commerces partenaires halal" to true,
                    "Pack familial 5 profils & reçu fiscal Waqf" to false
                )
                MembershipPlan.WAQF_PATRON -> listOf(
                    "Tous les avantages du plan Émeraude Privilège inclus" to true,
                    "Badge Mécène Waqf exclusif (Kiswa dorée) sur la carte & profil" to true,
                    "Financement direct de la numérisation des mosquées et de l'entraide" to true,
                    "Pack Familial : jusqu'à 5 cartes d'identité numériques certifiées" to true,
                    "Reçu de don Waqf téléchargeable & rapport d'impact annuel" to true,
                    "Invitations VIP aux conférences, galas et assemblées" to true,
                    "Support prioritaire dédié 7j/7" to true
                )
            }

            features.forEach { (text, included) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (included) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (included) Color(0xFF10B981) else Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (included) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 11.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun IslamicEthicsBanner() {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFECFDF5),
        border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = Color(0xFF059669),
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = "Charte Éthique & Waqf Solidaire",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF065F46)
                )
                Text(
                    text = "Vos cotisations financent l'hébergement sécurisé européen des données et soutiennent les projets éducatifs et mosquées de la communauté.",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = Color(0xFF047857)
                )
            }
        }
    }
}

@Composable
fun WaqfDonationDialog(
    onDismiss: () -> Unit,
    onConfirmDonate: (Float) -> Unit
) {
    var selectedAmount by remember { mutableStateOf(10f) }
    var isCustom by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf("15") }

    val presetAmounts = listOf(5f, 10f, 25f, 50f, 100f)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(32.dp))
        },
        title = {
            Text(
                text = "Contribution Waqf Solidaire 🤲",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Le Waqf est une aumône continue (Sadaqah Jariyah) qui perpétue le développement des services communautaires et la numérisation des lieux de culte.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Montant du don :",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetAmounts.take(4).forEach { amount ->
                        val isChosen = !isCustom && selectedAmount == amount
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isChosen) Color(0xFFD97706) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isChosen) Color(0xFFFFD700) else Color.Transparent),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    isCustom = false
                                    selectedAmount = amount
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "${amount.toInt()} €",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = customText,
                    onValueChange = {
                        customText = it.filter { char -> char.isDigit() }
                        isCustom = true
                    },
                    label = { Text("Autre montant personnalisé (€)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalAmt = if (isCustom) customText.toFloatOrNull() ?: 10f else selectedAmount
                    onConfirmDonate(finalAmt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Valider la contribution")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
