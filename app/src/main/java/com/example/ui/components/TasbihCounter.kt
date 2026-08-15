package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf

data class DhikrPreset(
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val target: Int = 33
)

@Composable
fun TasbihCounter(language: String = "fr") {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    val presets = listOf(
        DhikrPreset("سُبْحَانَ اللَّهِ", "SubhanAllah", "Gloire à Allah", 33),
        DhikrPreset("الْحَمْدُ لِلَّهِ", "Alhamdulillah", "Louange à Allah", 33),
        DhikrPreset("اللَّهُ أَكْبَرُ", "Allahu Akbar", "Allah est le plus Grand", 34),
        DhikrPreset("أَسْتَغْفِرُ اللَّهَ", "Astaghfirullah", "Je demande pardon à Allah", 100),
        DhikrPreset("لَا إِلٰهَ إِلَّا اللَّهُ", "La ilaha illallah", "Nulle divinité sauf Allah", 100),
        DhikrPreset("اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ", "Salawat", "Prières sur le Prophète ﷺ", 100)
    )

    var selectedPresetIndex by remember { mutableIntStateOf(0) }
    val currentPreset = presets[selectedPresetIndex]

    var count by remember { mutableIntStateOf(0) }
    var lapCount by remember { mutableIntStateOf(0) }
    var targetGoal by remember { mutableIntStateOf(33) }
    var isVibrationEnabled by remember { mutableStateOf(true) }

    fun vibrateOnce() {
        if (!isVibrationEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(35)
        }
    }

    fun vibrateSuccess() {
        if (!isVibrationEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 50, 120), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(150)
        }
    }

    val progress = if (targetGoal > 0) (count.toFloat() / targetGoal).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "tasbih_progress")

    val weeklyData = remember(count, lapCount) {
        listOf(120f, 250f, 180f, 330f, 99f, 210f, (count + lapCount * targetGoal).toFloat())
    }
    val chartEntryModel = entryModelOf(*weeklyData.toTypedArray())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = com.example.ui.locales.Translations.get(language, "tasbih_counter"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (lapCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                "Tours: $lapCount",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = { isVibrationEnabled = !isVibrationEnabled }) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Vibration",
                            tint = if (isVibrationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }

                    IconButton(onClick = {
                        count = 0
                        lapCount = 0
                        Toast.makeText(context, "Compteur réinitialisé", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Dhikr Presets Carousel
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets.indices.toList()) { index ->
                    val p = presets[index]
                    FilterChip(
                        selected = selectedPresetIndex == index,
                        onClick = {
                            selectedPresetIndex = index
                            targetGoal = p.target
                            count = 0
                        },
                        label = { Text(p.transliteration, fontSize = 12.sp) }
                    )
                }
            }

            // Current Dhikr Display Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentPreset.arabic,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentPreset.transliteration,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = currentPreset.translation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Target selector row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(33, 99, 100, 0).forEach { goal ->
                    SuggestionChip(
                        onClick = { targetGoal = goal },
                        label = { Text(if (goal == 0) "Infini" else "$goal", fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (targetGoal == goal) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Interactive Tasbih Dial
            Box(
                modifier = Modifier.size(170.dp),
                contentAlignment = Alignment.Center
            ) {
                if (targetGoal > 0) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(165.dp),
                        strokeWidth = 6.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }

                Button(
                    onClick = {
                        vibrateOnce()
                        count++
                        if (targetGoal > 0 && count >= targetGoal) {
                            vibrateSuccess()
                            lapCount++
                            count = 0
                            Toast.makeText(context, "Masha'Allah ! Objectif $targetGoal atteint ($lapCount)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(135.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = count.toString(),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        if (targetGoal > 0) {
                            Text(
                                text = "/ $targetGoal",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = com.example.ui.locales.Translations.get(language, "weekly_progress") ?: "Progrès de la Semaine",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Chart(
                chart = columnChart(),
                model = chartEntryModel,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    }
}
