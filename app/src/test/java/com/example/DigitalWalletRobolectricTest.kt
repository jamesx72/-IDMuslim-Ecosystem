package com.example

import android.content.Context
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.wallet.SecureWalletDatabase
import com.example.data.wallet.SensitiveInfoCategory
import com.example.data.wallet.SensitiveInfoEntity
import com.example.data.wallet.WalletDocCategory
import com.example.data.wallet.WalletDocumentEntity
import com.example.data.wallet.WalletRepository
import com.example.security.WalletVaultKeyManager
import com.example.ui.components.EncryptedIdentityDocumentCard
import com.example.ui.components.SqlCipherHeroVaultBanner
import com.example.ui.theme.IDMuslimTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class DigitalWalletRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var database: SecureWalletDatabase
    private lateinit var repository: WalletRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = SecureWalletDatabase.createInMemoryForTesting(context)
        repository = WalletRepository(database.walletDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `wallet key manager generates 256-bit passphrase and verifies salted pin`() {
        val hexKey = WalletVaultKeyManager.getPassphraseHex(context)
        assertEquals(64, hexKey.length)

        val fingerprint = WalletVaultKeyManager.getKeyFingerprintSha256(context)
        assertTrue(fingerprint.contains(":"))

        WalletVaultKeyManager.setCustomVaultPin(context, "2486")
        assertTrue(WalletVaultKeyManager.hasCustomVaultPin(context))
        assertTrue(WalletVaultKeyManager.verifyCustomVaultPin(context, "2486"))
        assertFalse(WalletVaultKeyManager.verifyCustomVaultPin(context, "9999"))
    }

    @Test
    fun `repository encrypts identity documents and sensitive info at rest and decrypts on read`() = runBlocking {
        val doc = WalletDocumentEntity(
            id = "doc-1",
            title = "Passeport Français",
            category = WalletDocCategory.PASSPORT.code,
            documentNumber = "24FR99182",
            holderFullName = "Amine Benali",
            issuingCountry = "France",
            issuingAuthority = "Préfecture de Paris",
            issueDate = "10/01/2023",
            expiryDate = "10/01/2033",
            mrzCode = "P<FRABENALI<<AMINE<<<<<<<<<<",
            notes = "Passeport biométrique principal"
        )
        repository.saveDocument(doc)

        // Verify raw stored entity in DAO has encrypted ciphertext fields
        val rawStoredDoc = database.walletDao().getDocumentById("doc-1")!!
        assertNotEquals("24FR99182", rawStoredDoc.documentNumber)
        assertTrue(rawStoredDoc.documentNumber.startsWith("ENC::"))

        // Verify repository Flow transparently decrypts fields
        val decryptedDocs = repository.allDocuments.first()
        assertEquals(1, decryptedDocs.size)
        assertEquals("24FR99182", decryptedDocs.first().documentNumber)
        assertEquals("Amine Benali", decryptedDocs.first().holderFullName)

        // Verify sensitive user information encryption/decryption
        val secret = SensitiveInfoEntity(
            id = "sec-1",
            title = "Dossier Urgence ICE",
            category = SensitiveInfoCategory.MEDICAL_EMERGENCY.code,
            primaryLabel = "Groupe Sanguin & Allergies",
            secretValue = "O+ • Allergie Pénicilline",
            secondaryLabel = "Contact ICE",
            secondaryValue = "+33 6 12 34 56 78"
        )
        repository.saveSensitiveInfo(secret)

        val rawStoredSecret = database.walletDao().getSensitiveInfoById("sec-1")!!
        assertTrue(rawStoredSecret.secretValue.startsWith("ENC::"))

        val decryptedSecrets = repository.allSensitiveInfo.first()
        assertEquals(1, decryptedSecrets.size)
        assertEquals("O+ • Allergie Pénicilline", decryptedSecrets.first().secretValue)
    }

    @Test
    fun `encrypted identity card masks document number until revealed`() {
        val doc = WalletDocumentEntity(
            id = "card-test",
            title = "Carte Nationale",
            category = WalletDocCategory.NATIONAL_ID.code,
            documentNumber = "CNI88492011",
            holderFullName = "Sara Mansouri",
            issuingCountry = "France",
            issuingAuthority = "ANTS",
            issueDate = "01/03/2024",
            expiryDate = "01/03/2034"
        )

        composeTestRule.setContent {
            val revealedState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            IDMuslimTheme {
                androidx.compose.foundation.layout.Column(
                    modifier = androidx.compose.ui.Modifier.verticalScroll(
                        androidx.compose.foundation.rememberScrollState()
                    )
                ) {
                    EncryptedIdentityDocumentCard(
                        document = doc,
                        isRevealed = revealedState.value,
                        language = "fr",
                        onToggleReveal = { revealedState.value = !revealedState.value },
                        onCopyNumber = {},
                        onToggleFavorite = {},
                        onInspectPass = {},
                        onEdit = {},
                        onDelete = {}
                    )
                    SqlCipherHeroVaultBanner(
                        documentsCount = 1,
                        sensitiveCount = 2,
                        telemetry = null,
                        onAddDocument = {},
                        onAddSensitive = {},
                        onScanOcr = {},
                        onImportProfile = {}
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("wallet_doc_card_card-test").assertExists()
        composeTestRule.onNodeWithText("CN •••• •••• 11").assertExists()
        composeTestRule.onNodeWithTag("reveal_doc_card-test").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("CNI88492011").assertExists()
    }
}
