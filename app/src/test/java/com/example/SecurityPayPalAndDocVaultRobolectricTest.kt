package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.example.data.wallet.EncryptedVaultFileEntity
import com.example.data.wallet.SecureWalletDatabase
import com.example.data.wallet.WalletDocCategory
import com.example.data.wallet.WalletRepository
import com.example.network.ApiClient
import com.example.payment.PayPalOrderPreparationResult
import com.example.payment.PayPalPaymentGateway
import com.example.security.EccCryptoEngine
import com.example.security.KeystoreManager
import com.example.ui.components.EncryptedDocumentUploadHeaderCard
import com.example.ui.components.EncryptedVaultFileCard
import com.example.ui.theme.IDMuslimTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class SecurityPayPalAndDocVaultRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var database: SecureWalletDatabase
    private lateinit var repository: WalletRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        ApiClient.initialize(context)
        database = SecureWalletDatabase.createInMemoryForTesting(context)
        repository = WalletRepository(database.walletDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `paypal gateway validates configuration and processes subscription without hosted button errors`() = runBlocking {
        val gateway = PayPalPaymentGateway.getInstance(context)
        val audit = gateway.auditConfiguration()
        assertTrue(audit.isReadyWithoutError)

        // Generated SDK v5 HTML must use EUR and never contain legacy USD HostedButtons bug
        val safeHtml = gateway.buildSafeCheckoutSdkHtml(
            clientId = "sb-test-client-id-123456",
            amountEur = 29.90,
            description = "IDMuslim EMERALD ANNUAL",
            installment4X = true
        )
        assertTrue(safeHtml.contains("currency=EUR"))
        assertTrue(safeHtml.contains("enable-funding=paylater"))
        assertFalse(safeHtml.contains("HostedButtons"))
        assertFalse(safeHtml.contains("currency=USD"))

        // Invalid email rejected cleanly
        val invalid = gateway.prepareSubscriptionPayment(
            payerEmail = "not-an-email",
            tier = "EMERALD",
            billingCycle = "ANNUAL",
            amountEur = 29.90,
            isRenewal = false,
            installment4X = false
        )
        assertTrue(invalid is PayPalOrderPreparationResult.Failed)

        // Valid sandbox subscription completes cleanly with ECC signature and EUR ledger state
        val valid = gateway.prepareSubscriptionPayment(
            payerEmail = "member@idmuslim.app",
            tier = "EMERALD",
            billingCycle = "ANNUAL",
            amountEur = 29.90,
            isRenewal = false,
            installment4X = true
        )
        assertTrue(valid is PayPalOrderPreparationResult.DirectSandboxApproved)
        val approved = (valid as PayPalOrderPreparationResult.DirectSandboxApproved).transactionResult
        assertEquals("EMERALD", approved.tier)
        assertEquals("ANNUAL", approved.billingCycle)
        assertEquals(29.90, approved.amountEur, 0.001)
        assertTrue(approved.installment4X)
        assertNotNull(approved.eccReceiptSignature)

        val session = ApiClient.getSessionManager()
        assertEquals("PAYPAL", session.getLastPaymentMethod())
        assertEquals("EMERALD", session.getMembershipTier())
        assertTrue(session.getPaypalSubscriptionId().startsWith("PAYPAL-SBX-"))
    }

    @Test
    fun `encrypted pdf and image upload encrypts disk blob with ecc ecies and persists metadata in sqlcipher`() = runBlocking {
        val pdfPlaintext = "%PDF-1.4\n1 0 obj\n<< /Title (Passeport Confidential) >>\n%%EOF"
            .toByteArray(Charsets.UTF_8)

        val storedResult = repository.uploadEncryptedBytes(
            context = context,
            plainBytes = pdfPlaintext,
            fileName = "passeport_2026.pdf",
            mimeType = "application/pdf",
            title = "Passeport Biométrique PDF",
            docCategory = WalletDocCategory.PASSPORT.code,
            uploadToCloud = false
        )
        assertTrue(storedResult.isSuccess)
        val entity = storedResult.getOrThrow()

        // Verify disk .enc file does NOT contain plaintext PDF magic or text
        val encryptedDiskFile = File(entity.localEncryptedPath)
        assertTrue(encryptedDiskFile.exists())
        val diskBytes = encryptedDiskFile.readBytes()
        assertTrue(EccCryptoEngine.isValidEciesWireFormat(diskBytes))
        assertFalse(String(diskBytes, Charsets.ISO_8859_1).contains("%PDF-1.4"))
        assertFalse(String(diskBytes, Charsets.ISO_8859_1).contains("Passeport Confidential"))

        // Verify ECDSA signature over document manifest
        val manifest = "${entity.id}|${entity.sha256PlaintextHex}|${entity.userId}|${entity.createdAt}"
        assertTrue(KeystoreManager.verifyData(manifest, entity.ecdsaSignatureBase64))

        // Verify DAO stores encrypted title/fileName fields in DB while repository returns decrypted
        val rawDaoRecord = database.walletDao().getEncryptedFileById(entity.id)!!
        assertNotEquals("Passeport Biométrique PDF", rawDaoRecord.title)
        assertTrue(rawDaoRecord.title.startsWith("ENC::"))

        val observedList = repository.allEncryptedFiles.first()
        assertEquals(1, observedList.size)
        assertEquals("Passeport Biométrique PDF", observedList.first().title)

        // Verify decryption restores exact plaintext PDF bytes
        val decrypted = repository.decryptVaultFileBytes(context, observedList.first()).getOrThrow()
        assertArrayEquals(pdfPlaintext, decrypted)

        // Verify delete wipes disk .enc file and DB row
        repository.deleteEncryptedFile(observedList.first())
        assertFalse(encryptedDiskFile.exists())
        assertTrue(repository.allEncryptedFiles.first().isEmpty())
    }

    @Test
    fun `encrypted document upload ui renders header card and uploaded file metadata`() {
        val sampleFile = EncryptedVaultFileEntity(
            id = "file-ui-1",
            userId = "uid-test",
            title = "Titre de Séjour Recto",
            docCategory = WalletDocCategory.RESIDENCE_PERMIT.code,
            fileName = "titre_sejour.pdf",
            mimeType = "application/pdf",
            originalSizeBytes = 245_000L,
            encryptedSizeBytes = 245_128L,
            sha256PlaintextHex = "aabbccddeeff00112233445566778899",
            sha256CiphertextHex = "99887766554433221100ffeeddccbbaa",
            eccPublicKeyFingerprint = "12:34:56:78:9A:BC",
            ecdsaSignatureBase64 = "MEUCIQTest==",
            localEncryptedPath = "/tmp/test.enc",
            firebaseStoragePath = "encrypted_documents/uid-test/file-ui-1.enc",
            syncStatus = "SYNCED_CLOUD"
        )

        composeTestRule.setContent {
            IDMuslimTheme {
                androidx.compose.foundation.layout.Column {
                    EncryptedDocumentUploadHeaderCard(
                        eccFingerprint = "12:34:56:78:9A:BC",
                        fileCount = 1,
                        isUploading = false,
                        onPickPdfOrFile = {},
                        onPickImagePhoto = {}
                    )
                    EncryptedVaultFileCard(
                        file = sampleFile,
                        language = "fr",
                        onDecryptAndOpen = {},
                        onRetryCloudSync = {},
                        onDelete = {}
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("encrypted_doc_upload_card").assertExists()
        composeTestRule.onNodeWithTag("upload_encrypted_pdf_button").assertExists()
        composeTestRule.onNodeWithTag("upload_encrypted_image_button").assertExists()
        composeTestRule.onNodeWithTag("encrypted_file_card_file-ui-1").assertExists()
        composeTestRule.onNodeWithTag("decrypt_open_file_file-ui-1").assertExists()
        composeTestRule.onNodeWithText("Titre de Séjour Recto").assertExists()
        composeTestRule.onNodeWithText("Cloud + Local").assertExists()
    }
}
