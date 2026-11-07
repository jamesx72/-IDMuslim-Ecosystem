package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PayoutConfig(
    @Json(name = "accountHolderName") val accountHolderName: String = "",
    @Json(name = "iban") val iban: String = "",
    @Json(name = "bicSwift") val bicSwift: String = "",
    @Json(name = "bankName") val bankName: String = "",
    @Json(name = "stripeAccountId") val stripeAccountId: String = "",
    @Json(name = "stripePaymentLink") val stripePaymentLink: String = "",
    @Json(name = "paypalEmail") val paypalEmail: String = "",
    @Json(name = "paypalMeUsername") val paypalMeUsername: String = "",
    @Json(name = "cryptoUsdtAddress") val cryptoUsdtAddress: String = "",
    @Json(name = "cryptoUsdtNetwork") val cryptoUsdtNetwork: String = "TRC-20",
    @Json(name = "cryptoBtcAddress") val cryptoBtcAddress: String = "",
    @Json(name = "cryptoEthAddress") val cryptoEthAddress: String = "",
    @Json(name = "defaultCurrency") val defaultCurrency: String = "EUR",
    @Json(name = "organizationType") val organizationType: String = "ASSOCIATION_MOSQUEE", // ASSOCIATION_MOSQUEE, WAQF_CHARITY, COMMERCE_HALAL, PARTICULIER
    @Json(name = "instantQrEnabled") val instantQrEnabled: Boolean = true,
    @Json(name = "autoIssueReceipt") val autoIssueReceipt: Boolean = true,
    @Json(name = "isConfigured") val isConfigured: Boolean = false,
    @Json(name = "updatedAt") val updatedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class ReceivedPaymentRecord(
    @Json(name = "id") val id: String = "",
    @Json(name = "amount") val amount: Double = 0.0,
    @Json(name = "currency") val currency: String = "EUR",
    @Json(name = "payerName") val payerName: String = "",
    @Json(name = "motive") val motive: String = "", // e.g. "Cotisation Adhésion", "Don Waqf", "Zakat Al-Maal", "Sadaqah", "Prestation"
    @Json(name = "paymentMethod") val paymentMethod: String = "SEPA_VIREMENT", // SEPA_VIREMENT, STRIPE_CARD, PAYPAL, CRYPTO_USDT, ESPECES_QR
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    @Json(name = "status") val status: String = "CONFIRMED", // CONFIRMED, PENDING, PROCESSING
    @Json(name = "referenceCode") val referenceCode: String = "",
    @Json(name = "taxReceiptEligible") val taxReceiptEligible: Boolean = true
)
