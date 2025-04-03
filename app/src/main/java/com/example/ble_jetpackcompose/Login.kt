@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ble_jetpackcompose

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay

val helveticaFont = FontFamily(
    Font(R.font.helvetica),
    Font(R.font.helvetica_bold, weight = FontWeight.Bold)
)

object AppColors {
    val PrimaryColor = Color(0xFF007AFF) // iOS blue
    val TextFieldBackgroundColor = Color(0xFFFFFFFF) // White
    val SecondaryTextColor = Color(0xFF8E8E93) // Gray
}

// Data class for translatable text in LoginScreen
data class TranslatedLoginScreenText(
    val welcomeBack: String = "Welcome Back",
    val signInToContinue: String = "Sign in to continue",
    val emailPlaceholder: String = "Email",
    val passwordPlaceholder: String = "Password",
    val invalidEmail: String = "Please enter a valid email address",
    val invalidPassword: String = "Password must be at least 8 characters",
    val forgotPassword: String = "Forgot Password?",
    val signIn: String = "Sign In",
    val orContinueWith: String = "Or continue with",
    val dontHaveAccount: String = "Don't have an account?",
    val registerNow: String = "Register Now",
    val resetPassword: String = "Reset Password",
    val resetPasswordPrompt: String = "Enter your email address and we'll send you a link to reset your password.",
    val sendResetLink: String = "Send Reset Link",
    val cancel: String = "Cancel",
    val resetEmailSent: String = "Password reset email sent! Please check your inbox.",
    val networkError: String = "Network error. Please check your connection.",
    val invalidCredentials: String = "Invalid email or password.",
    val unexpectedError: String = "An unexpected error occurred. Please try again."
)

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    // Theme and Language state
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()

    // Translated text state
    var translatedText by remember {
        mutableStateOf(
            TranslatedLoginScreenText(
                welcomeBack = TranslationCache.get("Welcome Back-$currentLanguage") ?: "Welcome Back",
                signInToContinue = TranslationCache.get("Sign in to continue-$currentLanguage") ?: "Sign in to continue",
                emailPlaceholder = TranslationCache.get("Email-$currentLanguage") ?: "Email",
                passwordPlaceholder = TranslationCache.get("Password-$currentLanguage") ?: "Password",
                invalidEmail = TranslationCache.get("Please enter a valid email address-$currentLanguage") ?: "Please enter a valid email address",
                invalidPassword = TranslationCache.get("Password must be at least 8 characters-$currentLanguage") ?: "Password must be at least 8 characters",
                forgotPassword = TranslationCache.get("Forgot Password?-$currentLanguage") ?: "Forgot Password?",
                signIn = TranslationCache.get("Sign In-$currentLanguage") ?: "Sign In",
                orContinueWith = TranslationCache.get("Or continue with-$currentLanguage") ?: "Or continue with",
                dontHaveAccount = TranslationCache.get("Don't have an account?-$currentLanguage") ?: "Don't have an account?",
                registerNow = TranslationCache.get("Register Now-$currentLanguage") ?: "Register Now",
                resetPassword = TranslationCache.get("Reset Password-$currentLanguage") ?: "Reset Password",
                resetPasswordPrompt = TranslationCache.get("Enter your email address and we'll send you a link to reset your password.-$currentLanguage") ?: "Enter your email address and we'll send you a link to reset your password.",
                sendResetLink = TranslationCache.get("Send Reset Link-$currentLanguage") ?: "Send Reset Link",
                cancel = TranslationCache.get("Cancel-$currentLanguage") ?: "Cancel",
                resetEmailSent = TranslationCache.get("Password reset email sent! Please check your inbox.-$currentLanguage") ?: "Password reset email sent! Please check your inbox.",
                networkError = TranslationCache.get("Network error. Please check your connection.-$currentLanguage") ?: "Network error. Please check your connection.",
                invalidCredentials = TranslationCache.get("Invalid email or password.-$currentLanguage") ?: "Invalid email or password.",
                unexpectedError = TranslationCache.get("An unexpected error occurred. Please try again.-$currentLanguage") ?: "An unexpected error occurred. Please try again."
            )
        )
    }

    // Preload translations on language change
    LaunchedEffect(currentLanguage) {
        val translator = GoogleTranslationService()
        val textsToTranslate = listOf(
            "Welcome Back", "Sign in to continue", "Email", "Password",
            "Please enter a valid email address", "Password must be at least 8 characters",
            "Forgot Password?", "Sign In", "Or continue with", "Don't have an account?",
            "Register Now", "Reset Password", "Enter your email address and we'll send you a link to reset your password.",
            "Send Reset Link", "Cancel", "Password reset email sent! Please check your inbox.",
            "Network error. Please check your connection.", "Invalid email or password.",
            "An unexpected error occurred. Please try again."
        )
        val translatedList = translator.translateBatch(textsToTranslate, currentLanguage)
        translatedText = TranslatedLoginScreenText(
            welcomeBack = translatedList[0],
            signInToContinue = translatedList[1],
            emailPlaceholder = translatedList[2],
            passwordPlaceholder = translatedList[3],
            invalidEmail = translatedList[4],
            invalidPassword = translatedList[5],
            forgotPassword = translatedList[6],
            signIn = translatedList[7],
            orContinueWith = translatedList[8],
            dontHaveAccount = translatedList[9],
            registerNow = translatedList[10],
            resetPassword = translatedList[11],
            resetPasswordPrompt = translatedList[12],
            sendResetLink = translatedList[13],
            cancel = translatedList[14],
            resetEmailSent = translatedList[15],
            networkError = translatedList[16],
            invalidCredentials = translatedList[17],
            unexpectedError = translatedList[18]
        )
    }

    // Theme-based colors
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color(0xFFB0B0B0) else AppColors.SecondaryTextColor
    val textFieldBackgroundColor = if (isDarkMode) Color(0xFF1E1E1E) else AppColors.TextFieldBackgroundColor
    val buttonBackgroundColor = if (isDarkMode) Color(0xFFBB86FC) else AppColors.PrimaryColor
    val buttonTextColor = if (isDarkMode) Color.Black else Color.White
    val dividerColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.LightGray
    val borderColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.LightGray
    val errorColor = if (isDarkMode) Color(0xFFCF6679) else MaterialTheme.colorScheme.error
    val dialogBackgroundColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) } // For popup
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) } // New state for error popup
    val isFormValid by remember(email, password) {
        derivedStateOf { isValidEmail(email) && isValidPassword(password) }
    }

    val authState by viewModel.authState.collectAsState()
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
                errorMessage = "Google Sign-In failed: ${e.statusCode} - ${e.message}"
                showErrorDialog = true
            }
        } else {
            errorMessage = "Google Sign-In cancelled with result code: $result.resultCode"
            showErrorDialog = true
        }
    }
    val googleSignInClient = remember { GoogleSignInHelper.getGoogleSignInClient(context) }
    LaunchedEffect(Unit) {
        viewModel.setGoogleSignInClient(googleSignInClient)
    }

    if (authState is AuthState.Loading) {
        LoadingDialog(onDismissRequest = {})
    }

    errorMessage?.let { error ->
        LaunchedEffect(error) {
            delay(5000)
            errorMessage = null
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> onNavigateToHome()
            is AuthState.Error -> {
                errorMessage = (authState as AuthState.Error).message
                showErrorDialog = true
            }
            is AuthState.PasswordResetEmailSent -> {
                showForgotPasswordDialog = false
                errorMessage = translatedText.resetEmailSent
                showErrorDialog = true // Show success as popup too
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 24.dp)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = translatedText.welcomeBack,
            style = TextStyle(
                fontSize = 34.sp,
                fontFamily = helveticaFont,
                fontWeight = FontWeight.Bold,
                color = textColor
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = translatedText.signInToContinue,
            style = TextStyle(
                fontSize = 17.sp,
                color = secondaryTextColor,
                fontFamily = helveticaFont,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(90.dp))

        EmailTextField(
            email = email,
            onEmailChange = {
                email = it
                errorMessage = null
            },
            isError = !isValidEmail(email) && email.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = translatedText.emailPlaceholder,
            invalidMessage = translatedText.invalidEmail,
            backgroundColor = textFieldBackgroundColor,
            textColor = textColor,
            borderColor = borderColor,
            errorColor = errorColor,
            buttonBackgroundColor = buttonBackgroundColor // Passed explicitly
        )

        PasswordTextField(
            password = password,
            onPasswordChange = {
                password = it
                errorMessage = null
            },
            passwordVisible = passwordVisible,
            onPasswordVisibilityChange = { passwordVisible = it },
            isError = !isValidPassword(password) && password.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            placeholder = translatedText.passwordPlaceholder,
            invalidMessage = translatedText.invalidPassword,
            backgroundColor = textFieldBackgroundColor,
            textColor = textColor,
            borderColor = borderColor,
            errorColor = errorColor,
            buttonBackgroundColor = buttonBackgroundColor // Passed explicitly
        )

//        AnimatedVisibility(
//            visible = errorMessage != null,
//            enter = fadeIn() + expandVertically(),
//            exit = fadeOut() + shrinkVertically()
//        ) {
//            Text(
//                text = errorMessage ?: "",
//                color = errorColor,
//                style = TextStyle(fontSize = 14.sp, fontFamily = helveticaFont),
//                modifier = Modifier.padding(top = 8.dp)
//            )
//        }

        TextButton(
            onClick = { showForgotPasswordDialog = true },
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp)
        ) {
            Text(
                text = translatedText.forgotPassword,
                style = TextStyle(
                    fontSize = 15.sp,
                    color = buttonBackgroundColor,
                    fontFamily = helveticaFont,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (isFormValid) {
                    viewModel.loginUser(email.trim(), password)
                }
            },
            enabled = isFormValid && authState !is AuthState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonBackgroundColor,
                disabledContainerColor = buttonBackgroundColor.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = buttonTextColor
                )
            } else {
                Text(
                    text = translatedText.signIn,
                    color = buttonTextColor,
                    style = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = helveticaFont
                    )
                )
            }
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
//                onClick = { /* Handle Facebook login */ },
//                backgroundColor = textFieldBackgroundColor,
//                borderColor = borderColor
//            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Replace this Row in LoginScreen (near the bottom of the Column):
        Row(
            modifier = Modifier.padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = translatedText.dontHaveAccount,
                style = TextStyle(
                    fontSize = 15.sp,
                    color = textColor,
                    fontFamily = helveticaFont,
                    fontWeight = FontWeight.SemiBold
                )
            )
            TextButton(
                onClick = {
                    try {
                        onNavigateToRegister() // Ensure navigation is called safely
                    } catch (e: Exception) {
                        // Log the error to identify the crash cause (optional, remove in production)
                        println("Navigation to register failed: ${e.message}")
                    }
                },
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = translatedText.registerNow ?: "Register Now", // Fallback to avoid null crash
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = buttonBackgroundColor,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = helveticaFont
                    )
                )
            }
        }

        if (showErrorDialog && errorMessage != null) {
            AlertDialog(
                onDismissRequest = {
                    showErrorDialog = false
                    errorMessage = null
                },
                title = {
                    Text(
                        text = "Error",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontFamily = helveticaFont,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                },
                text = {
                    Text(
                        text = errorMessage ?: "An unknown error occurred",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = helveticaFont,
                            color = textColor
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showErrorDialog = false
                            errorMessage = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonBackgroundColor
                        )
                    ) {
                        Text("OK", color = buttonTextColor)
                    }
                },
                containerColor = dialogBackgroundColor
            )
        }

        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                title = {
                    Text(
                        text = translatedText.resetPassword,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontFamily = helveticaFont,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                },
                text = {
                    Column {
                        Text(
                            text = translatedText.resetPasswordPrompt,
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = helveticaFont,
                                color = textColor
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = forgotPasswordEmail,
                            onValueChange = { forgotPasswordEmail = it },
                            placeholder = { Text(translatedText.emailPlaceholder, color = secondaryTextColor) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = textFieldBackgroundColor,
                                unfocusedIndicatorColor = borderColor,
                                focusedIndicatorColor = buttonBackgroundColor,
                                focusedTextColor = textColor, // Explicitly set for focus state
                                unfocusedTextColor = textColor // Explicitly set for unfocused state
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 17.sp, fontFamily = helveticaFont, color = textColor)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (isValidEmail(forgotPasswordEmail)) {
                                viewModel.sendPasswordResetEmail(forgotPasswordEmail.trim())
                            }
                        },
                        enabled = isValidEmail(forgotPasswordEmail),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonBackgroundColor,
                            disabledContainerColor = buttonBackgroundColor.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(translatedText.sendResetLink, color = buttonTextColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text(translatedText.cancel, color = buttonBackgroundColor)
                    }
                },
                containerColor = dialogBackgroundColor
            )
        }

    }
}

@Composable
fun SocialLoginButton(
    icon: Int,
    onClick: () -> Unit,
    backgroundColor: Color,
    borderColor: Color
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(64.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        contentPadding = PaddingValues(12.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = Color.Unspecified
        )
    }
}

@Composable
private fun EmailTextField(
    email: String,
    onEmailChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
    placeholder: String,
    invalidMessage: String,
    backgroundColor: Color,
    textColor: Color,
    borderColor: Color,
    errorColor: Color,
    buttonBackgroundColor: Color // Added parameter
) {
    TextField(
        value = email,
        onValueChange = onEmailChange,
        modifier = modifier,
        placeholder = { Text(placeholder, color = borderColor) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        isError = isError,
        supportingText = { if (isError) Text(invalidMessage, color = errorColor) },
        colors = TextFieldDefaults.textFieldColors(
            containerColor = backgroundColor,
            unfocusedIndicatorColor = borderColor,
            focusedIndicatorColor = buttonBackgroundColor,
            errorIndicatorColor = errorColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor
        ),
        shape = RoundedCornerShape(12.dp),
        textStyle = TextStyle(fontSize = 17.sp, fontFamily = helveticaFont, color = textColor)
    )
}

@Composable
private fun PasswordTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
    placeholder: String,
    invalidMessage: String,
    backgroundColor: Color,
    textColor: Color,
    borderColor: Color,
    errorColor: Color,
    buttonBackgroundColor: Color // Added parameter
) {
    TextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = modifier,
        placeholder = { Text(placeholder, color = borderColor) },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        isError = isError,
        supportingText = { if (isError) Text(invalidMessage, color = errorColor) },
        trailingIcon = {
            IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }, modifier = Modifier.size(24.dp)) {
                Icon(
                    painter = painterResource(id = if (passwordVisible) R.drawable.monkey else R.drawable.eyes),
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    tint = Color.Unspecified
                )
            }
        },
        colors = TextFieldDefaults.textFieldColors(
            containerColor = backgroundColor,
            unfocusedIndicatorColor = borderColor,
            focusedIndicatorColor = buttonBackgroundColor,
            errorIndicatorColor = errorColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor
        ),
        shape = RoundedCornerShape(12.dp),
        textStyle = TextStyle(fontSize = 17.sp, fontFamily = helveticaFont, color = textColor)
    )
}

// Validation functions
private fun isValidEmail(email: String): Boolean {
    return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun isValidPassword(password: String): Boolean {
    return password.length >= 8
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onNavigateToRegister = {}, onNavigateToHome = {}, viewModel = AuthViewModel())
}