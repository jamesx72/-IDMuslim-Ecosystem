package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacySettingsDashboard(
    language: String,
    profileVisibility: String,
    showEmail: Boolean,
    shareLocation: Boolean,
    shareData: Boolean,
    allowNotifications: Boolean,
    shareLinkFullName: Boolean = true,
    shareLinkDob: Boolean = true,
    shareLinkResidency: Boolean = true,
    shareLinkCommunity: Boolean = true,
    shareLinkStatus: Boolean = true,
    shareLinkPhoto: Boolean = false,
    onUpdateProfileVisibility: (String) -> Unit,
    onUpdateShowEmail: (Boolean) -> Unit,
    onUpdateShareLocation: (Boolean) -> Unit,
    onUpdateShareData: (Boolean) -> Unit,
    onUpdateAllowNotifications: (Boolean) -> Unit,
    onUpdateShareLinkFullName: (Boolean) -> Unit = {},
    onUpdateShareLinkDob: (Boolean) -> Unit = {},
    onUpdateShareLinkResidency: (Boolean) -> Unit = {},
    onUpdateShareLinkCommunity: (Boolean) -> Unit = {},
    onUpdateShareLinkStatus: (Boolean) -> Unit = {},
    onUpdateShareLinkPhoto: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(1) } // 0: General, 1: Granular Shared Links

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
            // Dashboard Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Privacy Dashboard",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == "fr") "TABLEAU DE BORD DE CONFIDENTIALITÉ" else if (language == "ar") "لوحة تحكم الخصوصية" else "PRIVACY DASHBOARD",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = if (language == "fr") "Gestion granulaire des données & liens de vérification" else if (language == "ar") "إدارة البيانات الدقيقة والروابط" else "Granular data & shared link visibility control",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Tab Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    label = { Text(if (language == "fr") "Profil Général" else "General Profile") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    label = { Text(if (language == "fr") "Liens Partagés" else "Shared Links") },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeTab == 0) {
                // --- TAB 0: GENERAL PROFILE PRIVACY ---
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
            } else {
                // --- TAB 1: GRANULAR SHARED LINK PRIVACY DASHBOARD ---
                Text(
                    text = if (language == "fr") "CONTRÔLE GRANULAIRE DES LIENS DE VÉRIFICATION" else "GRANULAR VERIFICATION LINK PRIVACY",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = if (language == "fr") "Activez ou désactivez la visibilité de chaque champ pour vos badges partagés et QR codes. Les données désactivées seront cryptographiquement masquées (\"***\")." else "Select which fields are disclosed when third parties scan or click your shared verification links.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 1. Full Name Toggle
                GranularFieldToggleRow(
                    icon = Icons.Default.Badge,
                    title = if (language == "fr") "Nom Complet" else "Full Name",
                    description = if (language == "fr") "Afficher votre nom officiel vs vos initiales anonymes" else "Display full name vs anonymous initials",
                    checked = shareLinkFullName,
                    onCheckedChange = onUpdateShareLinkFullName
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // 2. Date of Birth Toggle
                GranularFieldToggleRow(
                    icon = Icons.Default.Cake,
                    title = if (language == "fr") "Date de Naissance" else "Date of Birth",
                    description = if (language == "fr") "Inclure votre date de naissance (DD/MM/YYYY)" else "Include birthdate in shared verification card",
                    checked = shareLinkDob,
                    onCheckedChange = onUpdateShareLinkDob
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // 3. Residency Toggle
                GranularFieldToggleRow(
                    icon = Icons.Default.Home,
                    title = if (language == "fr") "Lieu de Résidence" else "Place of Residency",
                    description = if (language == "fr") "Afficher la ville et le pays de résidence" else "Display city and country of residence",
                    checked = shareLinkResidency,
                    onCheckedChange = onUpdateShareLinkResidency
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // 4. Community Affiliation Toggle
                GranularFieldToggleRow(
                    icon = Icons.Default.Mosque,
                    title = if (language == "fr") "Mosquée & Communauté" else "Mosque & Community",
                    description = if (language == "fr") "Divulguer votre mosquée ou centre de référence" else "Disclose your affiliated mosque or community center",
                    checked = shareLinkCommunity,
                    onCheckedChange = onUpdateShareLinkCommunity
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // 5. Verification Status Badge Toggle
                GranularFieldToggleRow(
                    icon = Icons.Default.VerifiedUser,
                    title = if (language == "fr") "Niveau & Statut de Vérification" else "Verification Level & Status",
                    description = if (language == "fr") "AFFICHER LE BADGE DE CONFIRMITÉ CERTIFIÉ (Level 3)" else "Display certified identity status badge",
                    checked = shareLinkStatus,
                    onCheckedChange = onUpdateShareLinkStatus
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // 6. Photo Avatar Toggle
                GranularFieldToggleRow(
                    icon = Icons.Default.AccountBox,
                    title = if (language == "fr") "Photo de Profil Certifiée" else "Certified Profile Photo",
                    description = if (language == "fr") "Inclure la vignette de photo d'identité dans le lien" else "Include profile picture thumbnail in link preview",
                    checked = shareLinkPhoto,
                    onCheckedChange = onUpdateShareLinkPhoto
                )

                Spacer(modifier = Modifier.height(20.dp))

                // --- LIVE PREVIEW CARD FOR SHARED LINK ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (language == "fr") "Aperçu en direct pour un tiers contrôleur :" else "Live Third-Party Verifier Preview:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                PreviewFieldItem("Identité :", if (shareLinkFullName) "M. AMADOU DIOP" else "M. A. D. (Masqué)", shareLinkFullName)
                                PreviewFieldItem("Date de naissance :", if (shareLinkDob) "15/08/1992" else "••/••/•••• (Masqué)", shareLinkDob)
                                PreviewFieldItem("Résidence :", if (shareLinkResidency) "Paris, France" else "••••••• (Masqué)", shareLinkResidency)
                                PreviewFieldItem("Mosquée :", if (shareLinkCommunity) "Grande Mosquée de Paris" else "•••••••••••• (Masqué)", shareLinkCommunity)
                                PreviewFieldItem("Statut IDMuslim :", if (shareLinkStatus) "VERIFIED LEVEL 3" else "INCONNU", shareLinkStatus)
                                PreviewFieldItem("Photo certifiée :", if (shareLinkPhoto) "INCLUSE DANS LE LIEN" else "NON INCLUSE", shareLinkPhoto)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val previewUrl = "https://idmuslim.org/verify/u981?dob=${if (shareLinkDob) 1 else 0}&res=${if (shareLinkResidency) 1 else 0}&status=${if (shareLinkStatus) 1 else 0}&photo=${if (shareLinkPhoto) 1 else 0}"
                        Text(
                            text = previewUrl,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GranularFieldToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun PreviewFieldItem(
    label: String,
    value: String,
    isVisible: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = if (isVisible) MaterialTheme.colorScheme.primary else Color.Gray,
                fontWeight = if (isVisible) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (isVisible) Icons.Default.CheckCircle else Icons.Default.Block,
                contentDescription = null,
                tint = if (isVisible) Color(0xFF25D366) else Color.Gray,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
