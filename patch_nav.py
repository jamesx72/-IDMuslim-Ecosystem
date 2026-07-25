import re
with open('app/src/main/java/com/example/ui/AppNavigation.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'object EditProfile : Screen("edit_profile", "Modifier le Profil", null)',
    'object EditProfile : Screen("edit_profile", "Modifier le Profil", null)\n    object DocumentScanner : Screen("document_scanner", "Scanner un document", null)'
)

content = content.replace(
    'import com.example.ui.screens.EditProfileScreen',
    'import com.example.ui.screens.EditProfileScreen\nimport com.example.ui.screens.DocumentScannerScreen'
)

nav_target = """            composable(Screen.Scanner.route) {
                ScannerScreen()
            }"""

nav_replacement = """            composable(Screen.Scanner.route) {
                ScannerScreen()
            }
            composable(Screen.DocumentScanner.route) {
                DocumentScannerScreen(
                    viewModel = eventViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }"""

content = content.replace(nav_target, nav_replacement)

# Also update ProfileScreen to navigate to document scanner
profile_nav_target = """                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    }"""

profile_nav_replacement = """                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onNavigateToDocumentScanner = {
                        navController.navigate(Screen.DocumentScanner.route)
                    }"""

content = content.replace(profile_nav_target, profile_nav_replacement)

with open('app/src/main/java/com/example/ui/AppNavigation.kt', 'w') as f:
    f.write(content)
