package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun TutorialOverlay(onComplete: () -> Unit) {
    var currentStep by remember { mutableStateOf(0) }
    val steps = listOf(
        Pair(
            "Carte d'identité numérique",
            "Consultez votre carte d'identité virtuelle sécurisée. Vous pouvez la présenter pour vous authentifier."
        ),
        Pair(
            "Activité",
            "Gardez un œil sur l'historique de vos actions, y compris les événements de vérification de votre identité et vos interactions dans l'application."
        ),
        Pair(
            "Scanner un document",
            "Utilisez la fonction 'Scanner' pour numériser de manière sécurisée vos documents physiques d'identité afin de les ajouter à votre profil."
        )
    )

    Dialog(
        onDismissRequest = { /* No dismiss */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bienvenue !",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = steps[currentStep].first,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = steps[currentStep].second,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (currentStep > 0) {
                            TextButton(onClick = { currentStep-- }) {
                                Text("Précédent")
                            }
                        } else {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        Button(
                            onClick = {
                                if (currentStep < steps.size - 1) {
                                    currentStep++
                                } else {
                                    onComplete()
                                }
                            }
                        ) {
                            Text(if (currentStep < steps.size - 1) "Suivant" else "Terminer")
                        }
                    }
                }
            }
        }
    }
}
