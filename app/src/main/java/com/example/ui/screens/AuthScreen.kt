package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.ApiClient
import com.example.network.EmailService
import com.example.network.SessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.random.Random

import androidx.compose.ui.platform.testTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var verificationMode by remember { mutableStateOf(false) }
    var verificationCode by remember { mutableStateOf("") }
    var generatedCode by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Fields error states
    var isEmailError by remember { mutableStateOf(false) }
    var isNameError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }
    var isConfirmPasswordError by remember { mutableStateOf(false) }

    // Validation
    fun validateFields(): Boolean {
        var isValid = true
        isEmailError = false
        isNameError = false
        isPasswordError = false
        isConfirmPasswordError = false

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isEmailError = true
            isValid = false
        }
        if (isSignUp && name.isBlank()) {
            isNameError = true
            isValid = false
        }
        if (password.length < 6) {
            isPasswordError = true
            isValid = false
        }
        if (isSignUp && password != confirmPassword) {
            isConfirmPasswordError = true
            isValid = false
        }
        return isValid
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 0f,
                        endY = 1000f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header Logo
                Icon(
                    imageVector = Icons.Default.Mosque,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 16.dp)
                )

                Text(
                    text = "IDMuslim",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = if (isSignUp) "Créer votre identité numérique" else "Se connecter à votre identité",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Registration / Login Card Container
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Error message banner
                        AnimatedVisibility(visible = errorMessage != null) {
                            errorMessage?.let { msg ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(12.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Success message banner
                        AnimatedVisibility(visible = successMessage != null) {
                            successMessage?.let { msg ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD1F2E5)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF0F5132),
                                        modifier = Modifier.padding(12.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        if (verificationMode) {
                            Text(
                                text = "Code de vérification",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )
                            
                            Text(
                                text = "Un code a été envoyé à $email.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp),
                                textAlign = TextAlign.Center
                            )

                            OutlinedTextField(
                                value = verificationCode,
                                onValueChange = { verificationCode = it },
                                label = { Text("Code à 6 chiffres") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    if (verificationCode.trim() == generatedCode) {
                                        successMessage = "Vérification réussie !"
                                        onAuthSuccess()
                                    } else {
                                        errorMessage = "Le code est incorrect."
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Valider et Continuer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        } else {
                            Text(
                                text = if (isSignUp) "Inscription" else "Connexion",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                        // Name Field (Sign Up Only)
                        AnimatedVisibility(visible = isSignUp) {
                            Column {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Nom complet") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .testTag("name_field"),
                                    singleLine = true,
                                    isError = isNameError,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                if (isNameError) {
                                    Text(
                                        text = "Le nom ne peut pas être vide.",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                                    )
                                }
                            }
                        }

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_field"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            isError = isEmailError,
                            shape = RoundedCornerShape(12.dp)
                        )
                        if (isEmailError) {
                            Text(
                                text = "Veuillez entrer une adresse email valide.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 8.dp, top = 4.dp, bottom = 12.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Mot de passe") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Afficher mot de passe"
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_field"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            isError = isPasswordError,
                            shape = RoundedCornerShape(12.dp)
                        )
                        if (isPasswordError) {
                            Text(
                                text = "Le mot de passe doit contenir au moins 6 caractères.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 8.dp, top = 4.dp, bottom = 12.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Confirm Password Field (Sign Up Only)
                        AnimatedVisibility(visible = isSignUp) {
                            Column {
                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    label = { Text("Confirmer le mot de passe") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    trailingIcon = {
                                        IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Afficher mot de passe"
                                            )
                                        }
                                    },
                                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .testTag("confirm_password_field"),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    isError = isConfirmPasswordError,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                if (isConfirmPasswordError) {
                                    Text(
                                        text = "Les mots de passe ne correspondent pas.",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                                    )
                                }
                            }
                        }

                        // Main Submit Button
                        Button(
                            onClick = {
                                if (validateFields()) {
                                    isLoading = true
                                    errorMessage = null
                                    successMessage = null
                                    if (isSignUp) {
                                        auth.createUserWithEmailAndPassword(email, password)
                                            .addOnCompleteListener { task ->
                                                isLoading = false
                                                if (task.isSuccessful) {
                                                    // Registering locally and in FB Profile
                                                    val user = auth.currentUser
                                                    val userProfileChange = com.google.firebase.auth.userProfileChangeRequest {
                                                        displayName = name
                                                    }
                                                    user?.updateProfile(userProfileChange)
                                                    
                                                    // Store auth tokens locally to integrate with existing interceptor
                                                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                                                        if (tokenTask.isSuccessful) {
                                                            val token = tokenTask.result.token ?: ""
                                                            ApiClient.getSessionManager().saveAuthToken(token)
                                                        }
                                                    }

                                                    val code = Random.nextInt(100000, 999999).toString()
                                                    generatedCode = code
                                                    coroutineScope.launch {
                                                        EmailService.sendVerificationEmail(email, code)
                                                    }
                                                    successMessage = "Inscription réussie. Veuillez vérifier votre email."
                                                    verificationMode = true
                                                } else {
                                                    errorMessage = task.exception?.localizedMessage ?: "L'inscription a échoué."
                                                }
                                            }
                                    } else {
                                        auth.signInWithEmailAndPassword(email, password)
                                            .addOnCompleteListener { task ->
                                                isLoading = false
                                                if (task.isSuccessful) {
                                                    val user = auth.currentUser
                                                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                                                        if (tokenTask.isSuccessful) {
                                                            val token = tokenTask.result.token ?: ""
                                                            ApiClient.getSessionManager().saveAuthToken(token)
                                                        }
                                                    }
                                                    successMessage = "Connexion réussie!"
                                                    onAuthSuccess()
                                                } else {
                                                    errorMessage = task.exception?.localizedMessage ?: "Connexion échouée. Veuillez vérifier vos identifiants."
                                                }
                                            }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .padding(vertical = 4.dp)
                                .testTag("submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    text = if (isSignUp) "S'inscrire" else "Se connecter",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Switch signup <-> login triggers
                        TextButton(
                            onClick = {
                                isSignUp = !isSignUp
                                errorMessage = null
                                successMessage = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("switch_auth_mode_button")
                        ) {
                            Text(
                                text = if (isSignUp) "Déjà un compte ? Se connecter" else "Nouveau membre ? Créer un compte",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        }
                    }
                }
            }
        }
    }
}
