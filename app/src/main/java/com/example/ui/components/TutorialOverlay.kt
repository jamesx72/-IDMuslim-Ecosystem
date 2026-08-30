package com.example.ui.components

import androidx.compose.runtime.Composable

@Composable
fun TutorialOverlay(
    language: String = "fr",
    userName: String = "Membre IDMuslim",
    memberId: String = "IDM-786-2026",
    onNavigateToQibla: (() -> Unit)? = null,
    onComplete: () -> Unit
) {
    CoachMarkOverlay(
        language = language,
        userName = userName,
        memberId = memberId,
        onNavigateToQibla = onNavigateToQibla,
        onComplete = onComplete
    )
}

