package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class SendGridEmailTo(val email: String)

@JsonClass(generateAdapter = true)
data class SendGridPersonalization(val to: List<SendGridEmailTo>)

@JsonClass(generateAdapter = true)
data class SendGridFrom(val email: String, val name: String? = null)

@JsonClass(generateAdapter = true)
data class SendGridContent(val type: String, val value: String)

@JsonClass(generateAdapter = true)
data class SendGridEmailRequest(
    val personalizations: List<SendGridPersonalization>,
    val from: SendGridFrom,
    val subject: String,
    val content: List<SendGridContent>
)

interface SendGridApi {
    @POST("v3/mail/send")
    suspend fun sendEmail(
        @Header("Authorization") authHeader: String,
        @Body request: SendGridEmailRequest
    )
}

object EmailService {
    private var retrofit: Retrofit? = null
    private var api: SendGridApi? = null

    fun initialize(sessionManager: SessionManager) {
        if (retrofit == null) {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(sessionManager))
                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl("https://api.sendgrid.com/")
                .addConverterFactory(MoshiConverterFactory.create(com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()))
                .client(client)
                .build()
            
            api = retrofit?.create(SendGridApi::class.java)
        }
    }

    suspend fun sendConfirmationEmail(userEmail: String, eventTitle: String) {
        val apiKey = BuildConfig.SENDGRID_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_SENDGRID_API_KEY") {
            println("Email not sent: SendGrid API Key is missing. Please configure it in the Secrets panel.")
            // Avoid crashing if key is missing so we can still test the UI
            return
        }

        val request = SendGridEmailRequest(
            personalizations = listOf(
                SendGridPersonalization(to = listOf(SendGridEmailTo(userEmail)))
            ),
            from = SendGridFrom(email = "noreply@idmuslim.com", name = "IDMuslim Events"),
            subject = "Confirmation d'inscription : $eventTitle",
            content = listOf(
                SendGridContent(
                    type = "text/plain",
                    value = "Bonjour,\n\nVous avez réussi votre inscription à l'événement : $eventTitle.\nVotre billet électronique a été généré et est disponible dans votre Espace Profil.\n\nMerci,\nL'équipe IDMuslim"
                )
            )
        )

        try {
            api?.sendEmail("Bearer $apiKey", request)
            println("Email successfully sent to $userEmail via SendGrid.")
        } catch (e: Exception) {
            println("Failed to send email: ${e.message}")
        }
    }

    suspend fun sendIdentityVerifiedEmail(userEmail: String, fullName: String) {
        val apiKey = BuildConfig.SENDGRID_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_SENDGRID_API_KEY") {
            println("Identity verified email not sent: SendGrid API Key is missing. Please configure it in the Secrets panel.")
            return
        }

        val nameGreeting = if (fullName.isNotBlank()) " $fullName" else ""
        val request = SendGridEmailRequest(
            personalizations = listOf(
                SendGridPersonalization(to = listOf(SendGridEmailTo(userEmail)))
            ),
            from = SendGridFrom(email = "security@idmuslim.com", name = "IDMuslim Sécurité"),
            subject = "Votre identité IDMuslim a été validée d'un badge vérifié !",
            content = listOf(
                SendGridContent(
                    type = "text/plain",
                    value = "Bonjour$nameGreeting,\n\nFélicitations ! Votre demande de vérification d'identité a été évaluée avec succès par notre équipe d'accréditation numérique. Votre compte a désormais reçu le label de citoyen officiel et votre badge électronique 'VÉRIFIÉ' a été ajouté à votre profil.\n\nVous bénéficiez à présent d'un accès sécurisé à l'ensemble du réseau biométrique et événementiel d'IDMuslim.\n\nMerci,\nL'équipe Sécurité IDMuslim"
                )
            )
        )

        try {
            api?.sendEmail("Bearer $apiKey", request)
            println("Identity verification approval email successfully sent to $userEmail via SendGrid.")
        } catch (e: Exception) {
            println("Failed to send identity verification approval email: ${e.message}")
        }
    }

    suspend fun sendVerificationEmail(userEmail: String, code: String) {
        val apiKey = BuildConfig.SENDGRID_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_SENDGRID_API_KEY") {
            println("Verification email not sent: SendGrid API Key missing.")
            return
        }

        val request = SendGridEmailRequest(
            personalizations = listOf(
                SendGridPersonalization(to = listOf(SendGridEmailTo(userEmail)))
            ),
            from = SendGridFrom(email = "noreply@idmuslim.com", name = "IDMuslim Security"),
            subject = "Code de vérification IDMuslim",
            content = listOf(
                SendGridContent(
                    type = "text/plain",
                    value = "Bonjour,\n\nVotre code de vérification est : $code\n\nMerci,\nL'équipe IDMuslim"
                )
            )
        )

        try {
            api?.sendEmail("Bearer $apiKey", request)
            println("Verification email successfully sent to $userEmail via SendGrid.")
        } catch (e: Exception) {
            println("Failed to send verification email: ${e.message}")
        }
    }

    suspend fun sendWaitlistJoinedEmail(userEmail: String, eventTitle: String) {
        val apiKey = BuildConfig.SENDGRID_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_SENDGRID_API_KEY") {
            println("Waitlist joined email not sent: SendGrid API Key missing.")
            return
        }

        val request = SendGridEmailRequest(
            personalizations = listOf(
                SendGridPersonalization(to = listOf(SendGridEmailTo(userEmail)))
            ),
            from = SendGridFrom(email = "noreply@idmuslim.com", name = "IDMuslim Events"),
            subject = "Liste d'attente : $eventTitle",
            content = listOf(
                SendGridContent(
                    type = "text/plain",
                    value = "Bonjour,\n\nVous avez bien été ajouté à la liste d'attente pour l'événement : $eventTitle.\nNous vous informerons automatiquement dès qu'une place se libère.\n\nMerci,\nL'équipe IDMuslim"
                )
            )
        )

        try {
            api?.sendEmail("Bearer $apiKey", request)
            println("Waitlist joined email successfully sent to $userEmail via SendGrid.")
        } catch (e: Exception) {
            println("Failed to send waitlist email: ${e.message}")
        }
    }

    suspend fun sendWaitlistPromotedEmail(userEmail: String, eventTitle: String) {
        val apiKey = BuildConfig.SENDGRID_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_SENDGRID_API_KEY") {
            println("Waitlist promoted email not sent: SendGrid API Key missing.")
            return
        }

        val request = SendGridEmailRequest(
            personalizations = listOf(
                SendGridPersonalization(to = listOf(SendGridEmailTo(userEmail)))
            ),
            from = SendGridFrom(email = "noreply@idmuslim.com", name = "IDMuslim Events"),
            subject = "Bonne nouvelle : Une place s'est libérée pour $eventTitle !",
            content = listOf(
                SendGridContent(
                    type = "text/plain",
                    value = "Bonjour,\n\nUne place s'est libérée et vous avez été automatiquement inscrit à l'événement : $eventTitle.\nVotre billet électronique a été généré et est disponible dans votre Espace Profil.\n\nMerci,\nL'équipe IDMuslim"
                )
            )
        )

        try {
            api?.sendEmail("Bearer $apiKey", request)
            println("Waitlist promoted email successfully sent to $userEmail via SendGrid.")
        } catch (e: Exception) {
            println("Failed to send waitlist promoted email: ${e.message}")
        }
    }
}
