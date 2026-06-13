package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun IslamicDateHeader(
    language: String,
    modifier: Modifier = Modifier
) {
    var showAdjustDialog by remember { mutableStateOf(false) }
    var hijriOffset by remember { mutableStateOf(0) } // +/- days calendar adjustment

    // Get current date and calculate Hijri representation
    val today = remember { LocalDate.now() }
    val displayedDate = remember(today, hijriOffset) {
        today.plusDays(hijriOffset.toLong())
    }

    val hijriDateStr = remember(displayedDate, language) {
        try {
            val hijrahDate = HijrahDate.from(displayedDate)
            val monthValue = hijrahDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
            val dayOfMonth = hijrahDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
            val yearValue = hijrahDate.get(java.time.temporal.ChronoField.YEAR)

            val monthNames = getHijriMonthNames(monthValue, language)
            "$dayOfMonth ${monthNames.first} $yearValue H"
        } catch (e: Exception) {
            // Safe fallback value
            "27 Dhou al-hijja 1447 H"
        }
    }

    val hijriDateArabicStr = remember(displayedDate) {
        try {
            val hijrahDate = HijrahDate.from(displayedDate)
            val monthValue = hijrahDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
            val dayOfMonth = hijrahDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
            val yearValue = hijrahDate.get(java.time.temporal.ChronoField.YEAR)

            val monthNames = getHijriMonthNames(monthValue, "ar")
            "$dayOfMonth ${monthNames.second} $yearValue هـ"
        } catch (e: Exception) {
            "٢٧ ذو الحجة ١٤٤٧ هـ"
        }
    }

    val gregorianDateStr = remember(displayedDate, language) {
        val pattern = if (language == "fr") {
            "EEEE d MMMM yyyy"
        } else {
            "EEEE, MMMM d, yyyy"
        }
        val locale = if (language == "fr") Locale.FRENCH else Locale.ENGLISH
        try {
            val formatter = DateTimeFormatter.ofPattern(pattern, locale)
            displayedDate.format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } catch (e: Exception) {
            "Samedi 13 Juin 2026"
        }
    }

    // Modern emerald card with soft radial-style gradient borders
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("islamic_date_card")
            .clickable { showAdjustDialog = true },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Circular Glowing Icon Frame
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                )
                            ),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Calendrier",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    // Hijri Date Label
                    Text(
                        text = hijriDateStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    // Gregorian Date Label
                    Text(
                        text = gregorianDateStr,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // Arabic Calligraphy visual placeholder / correction chip
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = hijriDateArabicStr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.End
                )
                if (hijriOffset != 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (hijriOffset > 0) "+$hijriOffset j" else "$hijriOffset j",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }

    // Interactive details dialog inside dashboard
    if (showAdjustDialog) {
        Dialog(onDismissRequest = { showAdjustDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .testTag("islamic_date_dialog"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (language == "fr") "CALENDRIER HIJRI" else "HIJRI CALENDAR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = if (language == "fr") "Ajustement du Calendrier" else "Calendar Adjustment",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dialog Preview Panel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = hijriDateArabicStr,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = hijriDateStr,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = gregorianDateStr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = if (language == "fr") "Correction lunaire (+/- jours)" else "Lunar correction (+/- days)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Adjuster Row Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { hijriOffset-- },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                                .size(40.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Diminuer", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }

                        Text(
                            text = if (hijriOffset >= 0) "+$hijriOffset Jours" else "$hijriOffset Jours",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .width(120.dp)
                                .padding(horizontal = 12.dp),
                            textAlign = TextAlign.Center
                        )

                        IconButton(
                            onClick = { hijriOffset++ },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                                .size(40.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Augmenter", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Brief educational tip matching Islamic community values
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == "fr") {
                                "En raison des observations de la lune, le calendrier Hijri réel peut varier de 1 à 2 jours d'un pays à l'autre."
                            } else {
                                "Due to crescent moon sightings, the Hijri date may vary by 1 or 2 days internationally."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TextButton(
                        onClick = { showAdjustDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (language == "fr") "Confirmer les réglages" else "Confirm settings",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Full translated mapping helper for Muslim Hijri month labels
private fun getHijriMonthNames(monthValue: Int, language: String): Pair<String, String> {
    val englishName = when (monthValue) {
        1 -> "Muharram"
        2 -> "Safar"
        3 -> "Rabi' al-Awwal"
        4 -> "Rabi' ath-Thani"
        5 -> "Jumada al-Ula"
        6 -> "Jumada al-Akhirah"
        7 -> "Rajab"
        8 -> "Sha'ban"
        9 -> "Ramadan"
        10 -> "Shawwal"
        11 -> "Dhu al-Qi'dah"
        12 -> "Dhu al-Hijjah"
        else -> "Muharram"
    }

    val frenchName = when (monthValue) {
        1 -> "Mouharram"
        2 -> "Safar"
        3 -> "Rabi' al-awwal"
        4 -> "Rabi' ath-thani"
        5 -> "Joumada al-oula"
        6 -> "Joumada ath-thania"
        7 -> "Rajab"
        8 -> "Chaâbane"
        9 -> "Ramadan"
        10 -> "Shawwal"
        11 -> "Dhou al-qi'da"
        12 -> "Dhou al-hijja"
        else -> "Mouharram"
    }

    val arabicName = when (monthValue) {
        1 -> "المحرّم"
        2 -> "صفر"
        3 -> "ربيع الأول"
        4 -> "ربيع الثاني"
        5 -> "جمادى الأولى"
        6 -> "جمادى الآخرة"
        7 -> "رجب"
        8 -> "شعبان"
        9 -> "رمضان"
        10 -> "شوال"
        11 -> "ذو القعدة"
        12 -> "ذو الحجة"
        else -> "المحرّم"
    }

    val displayPhonetic = if (language == "fr") frenchName else englishName
    return displayPhonetic to arabicName
}
