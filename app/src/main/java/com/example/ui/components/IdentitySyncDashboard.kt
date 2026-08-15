package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IdentitySyncDashboard(
    isSyncEnabled: Boolean,
    isRealtimeActive: Boolean,
    lastSyncTimestamp: Long,
    syncStatusMessage: String?,
    onToggleSyncEnabled: (Boolean) -> Unit,
    onTriggerSync: () -> Unit,
    onCheckConflicts: () -> Unit,
    onClearSyncMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Cloud Background Sync",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Synchronisation Arrière-Plan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Firebase Firestore Multi-Appareils",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Main Toggle
                Switch(
                    checked = isSyncEnabled,
                    onCheckedChange = onToggleSyncEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Realtime Sync Status Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = if (isSyncEnabled && isRealtimeActive) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isSyncEnabled && isRealtimeActive) Color(0xFF81C784) else Color(0xFFFFB74D)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isSyncEnabled && isRealtimeActive) Color(0xFF2E7D32) else Color(0xFFE65100))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSyncEnabled) "Flux Cloud Temps Réel Actif" else "Synchro Suspendue",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSyncEnabled && isRealtimeActive) Color(0xFF1B5E20) else Color(0xFFBF360C)
                        )
                    }

                    val formattedTime = if (lastSyncTimestamp > 0) {
                        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        "Mis à jour: ${sdf.format(Date(lastSyncTimestamp))}"
                    } else {
                        "Synchro en attente"
                    }

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSyncEnabled && isRealtimeActive) Color(0xFF2E7D32) else Color(0xFFE65100),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Sync message toast box if available
            syncStatusMessage?.let { msg ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onClearSyncMessage,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Digital Identity Domain Checklist Grid
            Text(
                text = "Domaines d'identité synchronisés en continu :",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SyncDomainItem(
                    icon = Icons.Default.Badge,
                    title = "Profil Public & Badge Certified",
                    subtitle = "Nom, Mosquée, Statut de vérification (Niveau 1, 2, 3)",
                    isSynced = isSyncEnabled
                )
                SyncDomainItem(
                    icon = Icons.Default.Lock,
                    title = "Identité Secrète Chiffrée (Room <-> Firestore)",
                    subtitle = "Passeport, CNI, Date de naissance, Résidence",
                    isSynced = isSyncEnabled
                )
                SyncDomainItem(
                    icon = Icons.Default.Visibility,
                    title = "Règles de Confidentialité des Liens Shared",
                    subtitle = "Paramètres d'affichage sélectif des données QR",
                    isSynced = isSyncEnabled
                )
                SyncDomainItem(
                    icon = Icons.Default.FolderSpecial,
                    title = "Coffre-Fort Documents d'Identité",
                    subtitle = "Documents justificatifs téléversés & chiffrés",
                    isSynced = isSyncEnabled
                )
                SyncDomainItem(
                    icon = Icons.Default.FamilyRestroom,
                    title = "Ayants Droit & Membres Famille",
                    subtitle = "Cartes et badges de la famille rattachée",
                    isSynced = isSyncEnabled
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onTriggerSync,
                    enabled = isSyncEnabled,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Forcer Synchro",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Synchroniser", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onCheckConflicts,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Troubleshoot,
                        contentDescription = "Vérifier Cohérence",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cohérence Room/Cloud", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun SyncDomainItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isSynced: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (isSynced) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Synchronisé",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PauseCircle,
                    contentDescription = "En pause",
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
