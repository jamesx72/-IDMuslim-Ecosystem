package com.example.network.gemini

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun askQuestion(question: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "YOUR_GEMINI_API_KEY") {
            try {
                val request = GenerateContentRequest(
                    contents = listOf(Content(
                        parts = listOf(Part(text = question))
                    )),
                    systemInstruction = Content(
                        parts = listOf(Part(text = "Tu es un assistant savant et bienveillant spécialisé dans les sciences islamiques et la vie communautaire pour l'application IDMuslim. Réponds avec précision, bienveillance, clarté et courtoisie en citant les sources authentiques (Coran, Sunna) lorsque pertinent. Ne donne pas de fatwa personnalisée obligatoire et rappelle de consulter un imam local en cas de doute complexe."))
                    )
                )
                val response = service.generateContent(apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    return@withContext text
                }
            } catch (e: Exception) {
                // Fallback to offline knowledge base if API fails
            }
        }

        // Offline / Built-in Knowledge Base for standard questions
        val lower = question.lowercase()
        return@withContext when {
            "prière" in lower || "salat" in lower || "horaire" in lower || "fajr" in lower -> {
                "📖 **Les 5 Prières Quotidiennes (As-Salat)** :\n\n" +
                "1. **Fajr** : 2 unités (Rak'at) à l'aube.\n" +
                "2. **Dhuhr** : 4 unités au zénith du soleil.\n" +
                "3. **Asr** : 4 unités l'après-midi.\n" +
                "4. **Maghrib** : 3 unités juste après le coucher du soleil.\n" +
                "5. **Isha** : 4 unités à la nuit tombée.\n\n" +
                "💡 *Rappel :* La prière est le second pilier de l'Islam et le pilier central de la foi. Consultez l'onglet Horaires dans l'application pour vos horaires géolocalisés précis."
            }
            "ablution" in lower || "wudu" in lower || "woudou" in lower || "laver" in lower -> {
                "💧 **Les étapes des Grandes et Petites Ablutions (Al-Wudu)** :\n\n" +
                "1. Formuler l'intention sincère (An-Niyya) et dire *Bismillah*.\n" +
                "2. Laver les mains 3 fois jusqu'aux poignets.\n" +
                "3. Rincer la bouche (Madmada) 3 fois.\n" +
                "4. Rincer le nez (Istinshaq) 3 fois.\n" +
                "5. Laver l'ensemble du visage 3 fois.\n" +
                "6. Laver les bras jusqu'aux coudes 3 fois (en commençant par le droit).\n" +
                "7. Passer les mains mouillées sur la tête puis nettoyer les oreilles 1 fois.\n" +
                "8. Laver les pieds jusqu'aux chevilles 3 fois (droit puis gauche).\n\n" +
                "✨ *Invocation finale :* « Ash-hadu an la ilaha illallah wahdahu la sharika lah... »"
            }
            "zakat" in lower || "aumône" in lower || "nisab" in lower -> {
                "💰 **La Zakat Al-Maal (Aumône Légale Obligatoire)** :\n\n" +
                "• **Taux :** 2,5 % de l'épargne cumulée sur une année lunaire (Al-Hawl).\n" +
                "• **Condition (Nisab) :** Équivalent d'environ 85g d'or pur ou 595g d'argent.\n" +
                "• **Bénéficiaires :** Les pauvres, les nécessiteux, les endettés et les autres catégories mentionnées dans la sourate At-Tawba (9:60).\n\n" +
                "💡 La Zakat purifie les biens et renforce la solidarité fraternelle dans la communauté."
            }
            "ramadan" in lower || "jeûne" in lower || "sawm" in lower -> {
                "🌙 **Le Jeûne du Ramadan (As-Sawm)** :\n\n" +
                "• **Définition :** S'abstenir de manger, boire et relations intimes de l'aube (Fajr) jusqu'au coucher du soleil (Maghrib).\n" +
                "• **Piliers :** L'intention (Niyya) la veille et l'abstinence des interdits.\n" +
                "• **Mérites :** Mois de la révélation du Coran, de la Nuit du Destin (Laylat Al-Qadr) et de la miséricorde divine."
            }
            "carte" in lower || "id" in lower || "vérification" in lower || "statut" in lower -> {
                "🛡️ **Identité Numérique & Vérification IDMuslim** :\n\n" +
                "Votre carte d'identité numérique IDMuslim certifie votre appartenance communautaire et garantit la protection de vos données grâce au chiffrement AES-256 / Room chiffré.\n\n" +
                "• **Niveau 1 (Bronze) :** Compte créé avec email/authentification.\n" +
                "• **Niveau 2 (Argent) :** Document d'identité (CNI / Passeport) validé.\n" +
                "• **Niveau 3 (Émeraude / Or) :** Validation biométrique (Liveness Check) + Rattachement Mosquée partenaire."
            }
            else -> {
                "Assalamu alaykum. Merci pour votre question sur : « $question ».\n\n" +
                "📚 En Islam, la recherche du savoir (*Talab al-'Ilm*) est une obligation pour chaque croyant. Les fondements reposent sur les cinq piliers de l'Islam (Attestation de foi, Prière, Zakat, Jeûne du Ramadan, Pèlerinage à La Mecque) et les six piliers de la foi (Iman).\n\n" +
                "Pour les questions de jurisprudence (*Fiqh*) détaillées ou spécifiques à votre situation personnelle, nous vous invitons également à solliciter l'imam de votre mosquée locale partenaire répertoriée dans l'application."
            }
        }
    }
}
