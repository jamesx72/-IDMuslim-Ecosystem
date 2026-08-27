package com.example.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.Timings
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.*

/**
 * Represents the 4 distinct solar periods of the day.
 */
enum class SolarPhase(
    val titleFr: String,
    val titleEn: String,
    val subtitleFr: String,
    val subtitleEn: String
) {
    DAWN(
        titleFr = "Aube Naissante",
        titleEn = "Morning Dawn",
        subtitleFr = "Lueur douce d'or et de rosée matinale",
        subtitleEn = "Soft morning gold and serene rose luminescence"
    ),
    DAY(
        titleFr = "Plein Jour",
        titleEn = "Radiant Daylight",
        subtitleFr = "Teintes vives éclatantes & contraste haute visibilité",
        subtitleEn = "Crisp, bright daylight tones and high clarity"
    ),
    SUNSET_EVENING(
        titleFr = "Crépuscule & Soirée",
        titleEn = "Golden Hour & Dusk",
        subtitleFr = "Tons chauds ambrés, terre de Sienne et or saharien",
        subtitleEn = "Warm amber, terracotta and Saharan gold sunset tones"
    ),
    NIGHT(
        titleFr = "Nuit Étoilée",
        titleEn = "Starry Night",
        subtitleFr = "Obsidienne nocturne et or céleste Kiswa",
        subtitleEn = "Deep nocturnal obsidian and celestial gold accents"
    )
}

/**
 * Detailed real-time snapshot of the solar cycle at the user's location.
 */
data class SolarState(
    val phase: SolarPhase,
    val isDaytime: Boolean,
    val sunriseTimeStr: String,
    val sunsetTimeStr: String,
    val dawnTimeStr: String,
    val duskTimeStr: String,
    val currentLocalTimeStr: String,
    val sunriseMinutes: Int,
    val sunsetMinutes: Int,
    val currentMinutes: Int,
    val cycleProgressFraction: Float, // 0.0f to 1.0f progress of the active day/night arc
    val nextTransitionSummary: String,
    val locationLabel: String,
    val adaptedThemeId: Int,
    val adaptedGradientColors: List<Color>,
    val adaptedAccentColor: Color,
    val secondaryAccentColor: Color,
    val icon: ImageVector,
    val badgeText: String
) {
    val phaseDisplayName: String get() = phase.titleFr
    val phaseIcon: ImageVector get() = icon
    val sunriseTime: String get() = sunriseTimeStr
    val sunsetTime: String get() = sunsetTimeStr
}

object SolarThemeHelper {

    // Default coordinates: Paris (48.8566, 2.3522) used if GPS not yet available
    private const val DEFAULT_LAT = 48.8566
    private const val DEFAULT_LNG = 2.3522
    private const val DEFAULT_LOCATION_NAME = "Position Locale"

    /**
     * Pre-defined color palettes tuned for solar lighting and aesthetics:
     * - Day: Brighter, vivid Emerald / Turquoise / Daylight Cyan
     * - Sunset: Warm terracotta, deep amber, fiery copper and glowing gold
     * - Night: Deep obsidian, midnight slate, starry Kiswa gold
     * - Dawn: Rose gold, morning mist, amethyst amber
     */
    val DAY_GRADIENT = listOf(
        Color(0xFF04382A),
        Color(0xFF0D5F45),
        Color(0xFF107B59),
        Color(0xFF0E8A63)
    )
    val DAY_ACCENT = Color(0xFF34D399) // Radiant Emerald
    val DAY_SECONDARY = Color(0xFF6EE7B7)

    val SUNSET_GRADIENT = listOf(
        Color(0xFF3E1207),
        Color(0xFF6E2308),
        Color(0xFF94340A),
        Color(0xFFB45309)
    )
    val SUNSET_ACCENT = Color(0xFFF59E0B) // Warm Saharan Amber
    val SUNSET_SECONDARY = Color(0xFFFBBF24)

    val NIGHT_GRADIENT = listOf(
        Color(0xFF0B1120),
        Color(0xFF111827),
        Color(0xFF1E293B),
        Color(0xFF1E1E2E)
    )
    val NIGHT_ACCENT = Color(0xFFFBBF24) // Starlight Gold / Kiswa Gold
    val NIGHT_SECONDARY = Color(0xFFE2E8F0)

    val DAWN_GRADIENT = listOf(
        Color(0xFF1C1427),
        Color(0xFF2E1C38),
        Color(0xFF4C2442),
        Color(0xFF6B2D4F)
    )
    val DAWN_ACCENT = Color(0xFFF472B6) // Rose Mist
    val DAWN_SECONDARY = Color(0xFFFDE047)

    /**
     * Computes the real-time SolarState based on the user's location and local device clock,
     * optionally accepting prayer timings from Aladhan API or falling back to astronomical calculation.
     */
    fun computeSolarState(
        latitude: Double = DEFAULT_LAT,
        longitude: Double = DEFAULT_LNG,
        locationName: String = DEFAULT_LOCATION_NAME,
        aladhanTimings: Timings? = null,
        overrideSimulation: String = "AUTO"
    ): SolarState {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMin = calendar.get(Calendar.MINUTE)
        val currentMinutes = currentHour * 60 + currentMin
        val timeFormat = String.format(Locale.getDefault(), "%02d:%02d", currentHour, currentMin)

        // 1. Determine Sunrise and Sunset minutes
        val (sunriseMin, sunsetMin, dawnMin, duskMin) = if (aladhanTimings != null) {
            parseTimingsToMinutes(aladhanTimings)
        } else {
            calculateAstronomicalSunTimes(calendar, latitude, longitude)
        }

        val sunriseStr = String.format(Locale.getDefault(), "%02d:%02d", sunriseMin / 60, sunriseMin % 60)
        val sunsetStr = String.format(Locale.getDefault(), "%02d:%02d", sunsetMin / 60, sunsetMin % 60)
        val dawnStr = String.format(Locale.getDefault(), "%02d:%02d", dawnMin / 60, dawnMin % 60)
        val duskStr = String.format(Locale.getDefault(), "%02d:%02d", duskMin / 60, duskMin % 60)

        // 2. Identify natural Solar Phase
        val goldenHourStartMin = sunsetMin - 45 // 45 min before sunset
        val goldenHourEndMin = sunsetMin + 40 // dusk/maghrib

        val naturalPhase = when {
            currentMinutes in dawnMin until sunriseMin -> SolarPhase.DAWN
            currentMinutes in sunriseMin until goldenHourStartMin -> SolarPhase.DAY
            currentMinutes in goldenHourStartMin..goldenHourEndMin -> SolarPhase.SUNSET_EVENING
            else -> SolarPhase.NIGHT
        }

        // Apply simulation override if testing/previewing
        val effectivePhase = when (overrideSimulation.uppercase()) {
            "DAWN" -> SolarPhase.DAWN
            "DAY" -> SolarPhase.DAY
            "SUNSET", "EVENING" -> SolarPhase.SUNSET_EVENING
            "NIGHT" -> SolarPhase.NIGHT
            else -> naturalPhase
        }

        val isDaytime = effectivePhase == SolarPhase.DAY || effectivePhase == SolarPhase.DAWN

        // 3. Compute next transition text
        val nextTransitionText = when (effectivePhase) {
            SolarPhase.DAWN -> {
                val diff = (sunriseMin - currentMinutes + 1440) % 1440
                "Lever du soleil à $sunriseStr (dans ${formatDuration(diff)})"
            }
            SolarPhase.DAY -> {
                val diff = (sunsetMin - currentMinutes + 1440) % 1440
                "Coucher du soleil à $sunsetStr (dans ${formatDuration(diff)})"
            }
            SolarPhase.SUNSET_EVENING -> {
                val diff = (duskMin - currentMinutes + 1440) % 1440
                "Crépuscule en cours • Nuit complète à $duskStr"
            }
            SolarPhase.NIGHT -> {
                val diff = (dawnMin - currentMinutes + 1440) % 1440
                "Aube / Fajr à $dawnStr (dans ${formatDuration(diff)})"
            }
        }

        // 4. Calculate progress percentage along active cycle
        val cycleProgress = when (effectivePhase) {
            SolarPhase.DAY -> {
                val totalDaySpan = max(1, sunsetMin - sunriseMin)
                ((currentMinutes - sunriseMin).toFloat() / totalDaySpan).coerceIn(0f, 1f)
            }
            SolarPhase.SUNSET_EVENING -> {
                val totalSunsetSpan = max(1, goldenHourEndMin - goldenHourStartMin)
                ((currentMinutes - goldenHourStartMin).toFloat() / totalSunsetSpan).coerceIn(0f, 1f)
            }
            SolarPhase.DAWN -> {
                val totalDawnSpan = max(1, sunriseMin - dawnMin)
                ((currentMinutes - dawnMin).toFloat() / totalDawnSpan).coerceIn(0f, 1f)
            }
            SolarPhase.NIGHT -> {
                val nightMinutesPassed = if (currentMinutes >= duskMin) {
                    currentMinutes - duskMin
                } else {
                    (1440 - duskMin) + currentMinutes
                }
                val totalNightSpan = (1440 - duskMin + dawnMin).coerceAtLeast(1)
                (nightMinutesPassed.toFloat() / totalNightSpan).coerceIn(0f, 1f)
            }
        }

        // 5. Select visual parameters & theme mapping
        val (themeId, gradient, accent, secondary, icon, badge) = when (effectivePhase) {
            SolarPhase.DAWN -> SolarPalette(
                themeId = 1, // Damascene Arabesque
                gradient = DAWN_GRADIENT,
                accent = DAWN_ACCENT,
                secondary = DAWN_SECONDARY,
                icon = Icons.Default.WbTwilight,
                badge = "AUBE"
            )
            SolarPhase.DAY -> SolarPalette(
                themeId = 0, // Rub el Hizb Emerald / Iznik Turquoise
                gradient = DAY_GRADIENT,
                accent = DAY_ACCENT,
                secondary = DAY_SECONDARY,
                icon = Icons.Default.WbSunny,
                badge = "JOUR"
            )
            SolarPhase.SUNSET_EVENING -> SolarPalette(
                themeId = 4, // Hilal & Dunes Sahara Amber
                gradient = SUNSET_GRADIENT,
                accent = SUNSET_ACCENT,
                secondary = SUNSET_SECONDARY,
                icon = Icons.Default.BrightnessMedium,
                badge = "CRÉPUSCULE"
            )
            SolarPhase.NIGHT -> SolarPalette(
                themeId = 3, // Kiswa Al-Kaaba Obsidian & Gold
                gradient = NIGHT_GRADIENT,
                accent = NIGHT_ACCENT,
                secondary = NIGHT_SECONDARY,
                icon = Icons.Default.NightsStay,
                badge = "NUIT"
            )
        }

        return SolarState(
            phase = effectivePhase,
            isDaytime = isDaytime,
            sunriseTimeStr = sunriseStr,
            sunsetTimeStr = sunsetStr,
            dawnTimeStr = dawnStr,
            duskTimeStr = duskStr,
            currentLocalTimeStr = timeFormat,
            sunriseMinutes = sunriseMin,
            sunsetMinutes = sunsetMin,
            currentMinutes = currentMinutes,
            cycleProgressFraction = cycleProgress,
            nextTransitionSummary = nextTransitionText,
            locationLabel = locationName,
            adaptedThemeId = themeId,
            adaptedGradientColors = gradient,
            adaptedAccentColor = accent,
            secondaryAccentColor = secondary,
            icon = icon,
            badgeText = badge
        )
    }

    private data class SolarPalette(
        val themeId: Int,
        val gradient: List<Color>,
        val accent: Color,
        val secondary: Color,
        val icon: ImageVector,
        val badge: String
    )

    private fun formatDuration(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    private fun parseTimingsToMinutes(timings: Timings): SunTimeQuad {
        val sunrise = parseTime(timings.Sunrise, 6 * 60)
        val sunset = parseTime(timings.Maghrib, 20 * 60)
        val fajr = parseTime(timings.Fajr, 5 * 60)
        val isha = parseTime(timings.Isha, 21 * 60 + 30)
        return SunTimeQuad(sunrise, sunset, fajr, isha)
    }

    private fun parseTime(timeStr: String?, defaultMinutes: Int): Int {
        if (timeStr.isNullOrBlank()) return defaultMinutes
        val clean = timeStr.trim().split(" ")[0] // remove timezone suffixes like (CET)
        val parts = clean.split(":")
        if (parts.size >= 2) {
            val h = parts[0].toIntOrNull() ?: return defaultMinutes
            val m = parts[1].toIntOrNull() ?: return defaultMinutes
            return h * 60 + m
        }
        return defaultMinutes
    }

    /**
     * Standard Astronomical Solar calculation (Equation of Time and Solar Declination)
     * Provides accurate local sunrise, sunset, dawn and dusk without internet.
     */
    private fun calculateAstronomicalSunTimes(calendar: Calendar, lat: Double, lng: Double): SunTimeQuad {
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val tzOffsetHours = calendar.timeZone.getOffset(calendar.timeInMillis) / 3600000.0

        // Fractional year in radians
        val gamma = 2 * PI / 365.0 * (dayOfYear - 1 + (calendar.get(Calendar.HOUR_OF_DAY) - 12) / 24.0)

        // Equation of time in minutes
        val eqtime = 229.18 * (0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) -
                0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma))

        // Solar declination angle in radians
        val decl = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) -
                0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma) -
                0.002697 * cos(3 * gamma) + 0.00148 * sin(3 * gamma)

        val latRad = Math.toRadians(lat)

        // Zenith for standard sunrise/sunset: 90.833°
        val zenithStandard = Math.toRadians(90.833)
        val cosHourAngle = (cos(zenithStandard) - sin(latRad) * sin(decl)) / (cos(latRad) * cos(decl))

        val haDeg = if (cosHourAngle in -1.0..1.0) {
            Math.toDegrees(acos(cosHourAngle))
        } else if (cosHourAngle < -1.0) {
            180.0 // Midnight sun
        } else {
            0.0 // Polar night
        }

        // Solar noon in local minutes
        val solarNoonMin = 720.0 - 4.0 * lng - eqtime + tzOffsetHours * 60.0

        val sunriseMin = (solarNoonMin - haDeg * 4.0).roundToInt().coerceIn(0, 1439)
        val sunsetMin = (solarNoonMin + haDeg * 4.0).roundToInt().coerceIn(0, 1439)

        // Dawn (Civil Twilight Zenith 96°)
        val zenithCivil = Math.toRadians(96.0)
        val cosCivilHA = (cos(zenithCivil) - sin(latRad) * sin(decl)) / (cos(latRad) * cos(decl))
        val civilHADeg = if (cosCivilHA in -1.0..1.0) Math.toDegrees(acos(cosCivilHA)) else haDeg + 6.0
        val dawnMin = (solarNoonMin - civilHADeg * 4.0).roundToInt().coerceIn(0, 1439)
        val duskMin = (solarNoonMin + civilHADeg * 4.0).roundToInt().coerceIn(0, 1439)

        return SunTimeQuad(sunriseMin, sunsetMin, dawnMin, duskMin)
    }

    private data class SunTimeQuad(val sunrise: Int, val sunset: Int, val dawn: Int, val dusk: Int)
}
