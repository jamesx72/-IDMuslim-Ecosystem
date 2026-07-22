import re

with open('app/src/main/java/com/example/ui/components/DigitalIdCard.kt', 'r') as f:
    content = f.read()

target = """    onPhotoClick: (() -> Unit)? = null,
    lastSyncTime: Long? = null,"""

replacement = """    onPhotoClick: (() -> Unit)? = null,
    lastSyncTime: Long? = null,
    onDownloadPdfClick: (() -> Unit)? = null,"""

content = content.replace(target, replacement)

target2 = """                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Translations.get(language, "identity_card").uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }"""

replacement2 = """                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Translations.get(language, "identity_card").uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        if (onDownloadPdfClick != null) {
                            androidx.compose.material3.IconButton(
                                onClick = onDownloadPdfClick,
                                modifier = Modifier.size(32.dp).padding(start = 8.dp)
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.PictureAsPdf,
                                    contentDescription = "Download PDF",
                                    tint = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }"""

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/components/DigitalIdCard.kt', 'w') as f:
    f.write(content)
