import re
with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'r') as f:
    content = f.read()

target = """    if (showIdReadyAlert) {
        AlertDialog(
            onDismissRequest = { 
                showIdReadyAlert = false 
                com.example.network.ApiClient.getSessionManager().saveIdReadyAlertDismissed(true)
            },"""

replacement = """    val hasSeenTutorial by viewModel.hasSeenTutorial.collectAsState()
    if (!hasSeenTutorial) {
        com.example.ui.components.TutorialOverlay(
            onComplete = { viewModel.completeTutorial() }
        )
    }

    if (showIdReadyAlert) {
        AlertDialog(
            onDismissRequest = { 
                showIdReadyAlert = false 
                com.example.network.ApiClient.getSessionManager().saveIdReadyAlertDismissed(true)
            },"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'w') as f:
    f.write(content)
