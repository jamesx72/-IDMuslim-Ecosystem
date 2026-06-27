package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrivacySettingsDashboard(
    language: String,
    profileVisibility: String,
    showEmail: Boolean,
    shareLocation: Boolean,
    shareData: Boolean,
    allowNotifications: Boolean,
    onUpdateProfileVisibility: (String) -> Unit,
    onUpdateShowEmail: (Boolean) -> Unit,
    onUpdateShareLocation: (Boolean) -> Unit,
    onUpdateShareData: (Boolean) -> Unit,
    onUpdateAllowNotifications: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Privacy Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == "fr") "PARAMÈTRES DE CONFIDENTIALITÉ" else if (language == "ar") "إعدادات الخصوصية" else "PRIVACY SETTINGS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Visibility
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == "fr") "Visibilité du profil" else if (language == "ar") "رؤية الملف الشخصي" else "Profile Visibility",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == "fr") "Qui peut voir votre profil IDMuslim" else if (language == "ar") "من يمكنه رؤية ملفك الشخصي في IDMuslim" else "Who can see your IDMuslim profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = if (profileVisibility == "Public") Icons.Default.Public else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (profileVisibility) {
                                "Public" -> if (language == "fr") "Public" else if (language == "ar") "عام" else "Public"
                                "Private" -> if (language == "fr") "Privé" else if (language == "ar") "خاص" else "Private"
                                else -> "Contacts"
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (language == "fr") "Public" else if (language == "ar") "عام" else "Public") },
                            onClick = { onUpdateProfileVisibility("Public"); expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text(if (language == "fr") "Contacts uniquement" else if (language == "ar") "جهات الاتصال فقط" else "Contacts Only") },
                            onClick = { onUpdateProfileVisibility("Contacts"); expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text(if (language == "fr") "Privé" else if (language == "ar") "خاص" else "Private") },
                            onClick = { onUpdateProfileVisibility("Private"); expanded = false }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Show Email Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == "fr") "Afficher l'email" else if (language == "ar") "إظهار البريد الإلكتروني" else "Show Email",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == "fr") "Afficher votre adresse email publiquement" else if (language == "ar") "عرض عنوان بريدك الإلكتروني للعامة" else "Display your email address publicly",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = showEmail,
                    onCheckedChange = { onUpdateShowEmail(it) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Share Location Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == "fr") "Partager la position" else if (language == "ar") "مشاركة الموقع" else "Share Location",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == "fr") "Utilisé pour les heures de prière locales" else if (language == "ar") "تستخدم لأوقات الصلاة المحلية" else "Used for local prayer times",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = shareLocation,
                    onCheckedChange = { onUpdateShareLocation(it) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Share Data Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == "fr") "Partage des données" else if (language == "ar") "مشاركة البيانات" else "Data Sharing",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == "fr") "Aider à améliorer l'application avec des données anonymes" else if (language == "ar") "المساعدة في تحسين التطبيق باستخدام بيانات مجهولة" else "Help improve the app with anonymous data",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = shareData,
                    onCheckedChange = { onUpdateShareData(it) }
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Notifications Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == "fr") "Autoriser les notifications" else if (language == "ar") "السماح بالإشعارات" else "Allow Notifications",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == "fr") "Recevoir des alertes pour les prières et événements" else if (language == "ar") "تلقي تنبيهات للصلوات والأحداث" else "Receive alerts for prayers and events",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = allowNotifications,
                    onCheckedChange = { onUpdateAllowNotifications(it) }
                )
            }
        }
    }
}
