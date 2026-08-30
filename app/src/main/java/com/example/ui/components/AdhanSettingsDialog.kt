package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.ApiClient
import com.example.notifications.PrayerNotificationScheduler
import com.example.utils.AdhanAudioPlayer
import com.example.utils.HapticHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhanSettingsDialog(
    onDismiss: () -> Unit,
    language: String = "fr"
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val sessionManager = remember { ApiClient.getSessionManager() }

    var prayerNotifications by remember { mutableStateOf(sessionManager.getPrayerNotifications()) }
    var adhanAudioEnabled by remember { mutableStateOf(sessionManager.isAdhanAudioEnabled()) }
    var preReminderMinutes by remember { mutableStateOf(sessionManager.getPrePrayerReminderMinutes()) }
    var isTestingAudio by remember { mutableStateOf(AdhanAudioPlayer.isPlaying()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Alertes Adhan & Prières", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Switch: Prayer Notifications
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notifications des Prières", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Alertes visuelles & sonores pour les 5 prières", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = prayerNotifications,
                        onCheckedChange = {
                            prayerNotifications = it
                            sessionManager.savePrayerNotifications(it)
                            HapticHelper.performClick(context, haptic)
                        }
                    )
                }

                HorizontalDivider()

                // Sub-Option: Adhan Call Audio Playback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Appel à la Prière (Adhan Audio)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Joue l'appel vocal Adhan à l'heure exacte", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = adhanAudioEnabled,
                        enabled = prayerNotifications,
                        onCheckedChange = {
                            adhanAudioEnabled = it
                            sessionManager.saveAdhanAudioEnabled(it)
                            HapticHelper.performClick(context, haptic)
                        }
                    )
                }

                // Audio Test Button
                OutlinedButton(
                    onClick = {
                        if (isTestingAudio) {
                            AdhanAudioPlayer.stopAdhan()
                            isTestingAudio = false
                        } else {
                            AdhanAudioPlayer.playAdhan(context) {
                                isTestingAudio = false
                            }
                            isTestingAudio = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isTestingAudio) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTestingAudio) "Arrêter l'Adhan de test" else "Tester la tonalité de l'Adhan")
                }

                HorizontalDivider()

                // Pre-Prayer Reminder Selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Rappel Pré-Prière (Ablutions)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("Alerte d'avance pour se préparer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    val minuteOptions = listOf(0 to "Désactivé", 5 to "5 min", 10 to "10 min", 15 to "15 min", 30 to "30 min")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        minuteOptions.forEach { (mins, label) ->
                            val isSelected = preReminderMinutes == mins
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable {
                                        preReminderMinutes = mins
                                        sessionManager.savePrePrayerReminderMinutes(mins)
                                        HapticHelper.performClick(context, haptic)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    AdhanAudioPlayer.stopAdhan()
                    onDismiss()
                }
            ) {
                Text("Enregistrer")
            }
        }
    )
}
