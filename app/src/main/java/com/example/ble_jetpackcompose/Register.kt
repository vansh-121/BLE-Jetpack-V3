package com.example.ble_jetpackcompose

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    // Theme and Language state
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()

    // Translated text state
    var translatedText by remember {
        mutableStateOf(
            TranslatedRegisterScreenText(
                createAccount = TranslationCache.get("Create Account-$currentLanguage") ?: "Create Account",
                signUpToGetStarted = TranslationCache.get("Sign up to get started-$currentLanguage") ?: "Sign up to get started",
                usernamePlaceholder = TranslationCache.get("Username-$currentLanguage") ?: "Username",
                emailPlaceholder = TranslationCache.get("Email-$currentLanguage") ?: "Email",
                passwordPlaceholder = TranslationCache.get("Password-$currentLanguage") ?: "Password",
                confirmPasswordPlaceholder = TranslationCache.get("Confirm Password-$currentLanguage") ?: "Confirm Password",
                createAccountButton = TranslationCache.get("Create Account-$currentLanguage") ?: "Create Account",
                orContinueWith = TranslationCache.get("Or continue with-$currentLanguage") ?: "Or continue with",
                alreadyHaveAccount = TranslationCache.get("Already have an account?-$currentLanguage") ?: "Already have an account?",
                loginNow = TranslationCache.get("Login Now-$currentLanguage") ?: "Login Now",
                creatingAccount = TranslationCache.get("Creating Account-$currentLanguage") ?: "Creating Account"
            )
        )
    }

    // Preload translations on language change
    LaunchedEffect(currentLanguage) {
        val translator = GoogleTranslationService()
        val textsToTranslate = listOf(
            "Create Account", "Sign up to get started", "Username", "Email", "Password",
            "Confirm Password", "Create Account", "Or continue with", "Already have an account?",
            "Login Now", "Creating Account"
        )
        val translatedList = translator.translateBatch(textsToTranslate, currentLanguage)
        translatedText = TranslatedRegisterScreenText(
            createAccount = translatedList[0],
            signUpToGetStarted = translatedList[1],
            usernamePlaceholder = translatedList[2],
            emailPlaceholder = translatedList[3],
            passwordPlaceholder = translatedList[4],
            confirmPasswordPlaceholder = translatedList[5],
            createAccountButton = translatedList[6],
            orContinueWith = translatedList[7],
            alreadyHaveAccount = translatedList[8],
            loginNow = translatedList[9],
            creatingAccount = translatedList[10]
        )
    }

    // Theme-based colors
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF8E8E93)
    val textFieldBackgroundColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val buttonBackgroundColor = if (isDarkMode) Color(0xFFBB86FC) else Color(0xFF007AFF)
    val buttonTextColor = if (isDarkMode) Color.Black else Color.White
    val dividerColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.LightGray
    val borderColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.LightGray

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var isUsernameValid by remember { mutableStateOf(false) }
    var isEmailValid by remember { mutableStateOf(false) }
    var isPasswordValid by remember { mutableStateOf(false) }
    var isConfirmPasswordValid by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { viewModel.signInWithGoogle(it) }
            } catch (e: ApiException) {
//                viewModel.authState.value = AuthState.Error("Google Sign-In failed: ${e.message}")
            }
        }
    }

    val googleSignInClient = remember { GoogleSignInHelper.getGoogleSignInClient(context) }
    LaunchedEffect(Unit) {
        viewModel.setGoogleSignInClient(googleSignInClient)
    }


    LocalFocusManager.current

    val authState by viewModel.authState.collectAsState()

    fun validateUsername(value: String): Boolean {
        return value.length >= 3 && value.matches(Regex("^[a-zA-Z0-9_]+$"))
    }

    fun validateEmail(value: String): Boolean {
        return value.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$"))
    }

    fun validatePassword(value: String): Boolean {
        return value.length >= 8 &&
                value.matches(Regex(".*[A-Z].*")) &&
                value.matches(Regex(".*[a-z].*")) &&
                value.matches(Regex(".*\\d.*")) &&
                value.matches(Regex(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"))
    }

    if (authState is AuthState.Loading) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(translatedText.creatingAccount, color = textColor) },
            text = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(color = buttonBackgroundColor)
                }
            },
            confirmButton = { },
            containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
        )
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                println("User registered: ${(authState as AuthState.Success).user.email}")
                onNavigateToHome()
            }
            is AuthState.Error -> {
                println("Error: ${(authState as AuthState.Error).message}")
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = translatedText.createAccount,
            style = TextStyle(
                fontSize = 34.sp,
                fontFamily = helveticaFont,
                fontWeight = FontWeight.Bold,
                color = textColor
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = translatedText.signUpToGetStarted,
            style = TextStyle(
                fontSize = 17.sp,
                color = secondaryTextColor,
                fontFamily = helveticaFont,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(60.dp))

        TextField(
            value = username,
            onValueChange = {
                username = it
                isUsernameValid = validateUsername(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text(translatedText.usernamePlaceholder, color = secondaryTextColor) },
            isError = username.isNotEmpty() && !isUsernameValid,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = textFieldBackgroundColor,
                unfocusedIndicatorColor = borderColor,
                focusedIndicatorColor = buttonBackgroundColor,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(
                fontSize = 17.sp,
                fontFamily = helveticaFont,
                color = textColor
            )
        )

        TextField(
            value = email,
            onValueChange = {
                email = it
                isEmailValid = validateEmail(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text(translatedText.emailPlaceholder, color = secondaryTextColor) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = email.isNotEmpty() && !isEmailValid,
            colors = TextFieldDefaults.textFieldColors(
                containerColor = textFieldBackgroundColor,
                unfocusedIndicatorColor = borderColor,
                focusedIndicatorColor = buttonBackgroundColor,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(
                fontSize = 17.sp,
                fontFamily = helveticaFont,
                color = textColor
            )
        )

        TextField(
            value = password,
            onValueChange = {
                password = it
                isPasswordValid = validatePassword(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text(translatedText.passwordPlaceholder, color = secondaryTextColor) },
            isError = password.isNotEmpty() && !isPasswordValid,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            trailingIcon = {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.monkey else R.drawable.eyes
                        ),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color.Unspecified
                    )
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = textFieldBackgroundColor,
                unfocusedIndicatorColor = borderColor,
                focusedIndicatorColor = buttonBackgroundColor,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(
                fontSize = 17.sp,
                fontFamily = helveticaFont,
                color = textColor
            )
        )

        TextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                isConfirmPasswordValid = password == it
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(translatedText.confirmPasswordPlaceholder, color = secondaryTextColor) },
            isError = confirmPassword.isNotEmpty() && !isConfirmPasswordValid,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                IconButton(
                    onClick = { confirmPasswordVisible = !confirmPasswordVisible },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (confirmPasswordVisible) R.drawable.monkey else R.drawable.eyes
                        ),
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                        tint = Color.Unspecified
                    )
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = textFieldBackgroundColor,
                unfocusedIndicatorColor = borderColor,
                focusedIndicatorColor = buttonBackgroundColor,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(
                fontSize = 17.sp,
                fontFamily = helveticaFont,
                color = textColor
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (isUsernameValid && isEmailValid && isPasswordValid && password == confirmPassword) {
                    viewModel.registerUser(email, password)
                }
            },
            enabled = isUsernameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonBackgroundColor,
                disabledContainerColor = buttonBackgroundColor.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = translatedText.createAccountButton,
                style = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = helveticaFont,
                    color = buttonTextColor
                )
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = dividerColor
            )
            Text(
                text = translatedText.orContinueWith,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = TextStyle(
                    fontSize = 15.sp,
                    color = secondaryTextColor,
                    fontFamily = helveticaFont,
                    fontWeight = FontWeight.Bold
                )
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = dividerColor
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SocialLoginButton(
                icon = R.drawable.google_g,
                onClick = { launcher.launch(googleSignInClient.signInIntent) },
                backgroundColor = textFieldBackgroundColor,
                borderColor = borderColor
            )
//            SocialLoginButton(
//                icon = R.drawable.facebook_f_,
//                onClick = { /* Handle Facebook sign up */ },
//                backgroundColor = textFieldBackgroundColor,
//                borderColor = borderColor
//            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = translatedText.alreadyHaveAccount,
                style = TextStyle(
                    fontSize = 15.sp,
                    color = textColor,
                    fontFamily = helveticaFont,
                    fontWeight = FontWeight.SemiBold
                )
            )
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = translatedText.loginNow,
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = buttonBackgroundColor,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = helveticaFont
                    )
                )
            }
        }
    }
}

// Data class for translatable text
data class TranslatedRegisterScreenText(
    val createAccount: String,
    val signUpToGetStarted: String,
    val usernamePlaceholder: String,
    val emailPlaceholder: String,
    val passwordPlaceholder: String,
    val confirmPasswordPlaceholder: String,
    val createAccountButton: String,
    val orContinueWith: String,
    val alreadyHaveAccount: String,
    val loginNow: String,
    val creatingAccount: String
)

//@Preview(showBackground = true)
//@Composable
//fun RegisterScreenPreview() {
//    RegisterScreen(onNavigateToLogin = { }, onNavigateToHome = {}, viewModel = AuthViewModel())
//}