package com.example.ble_jetpackcompose

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
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
    val focusManager = LocalFocusManager.current

    val authState by viewModel.authState.collectAsState()

    // Define custom colors (matching login screen)
    val primaryColor = Color(0xFF007AFF)
    val backgroundColor = Color(0xFFF2F2F7)
    val textFieldBgColor = Color(0xFFFFFFFF)
    val secondaryTextColor = Color(0xFF8E8E93)
    val successColor = Color(0xFF34C759)
    val errorColor = Color(0xFFFF3B30)

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
            title = { Text("Creating Account") },
            text = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator()
                }
            },
            confirmButton = { }
        )
    }
    // Handle auth state changes
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Welcome Text
        Text(
            text = "Create Account",
            style = TextStyle(
                fontSize = 34.sp,
                fontFamily = helveticaFont,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Sign up to get started",
            style = TextStyle(
                fontSize = 17.sp,
                color = secondaryTextColor,
                fontFamily = helveticaFont,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(60.dp))

        // Username TextField
        TextField(
            value = username,
            onValueChange = { username = it
                isUsernameValid = validateUsername(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Username") },
            isError = username.isNotEmpty() && !isUsernameValid,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = textFieldBgColor,
                unfocusedIndicatorColor = Color.LightGray,
                focusedIndicatorColor = primaryColor
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(
                fontSize = 17.sp,
                fontFamily = helveticaFont
            )
        )

        // Email TextField
        TextField(
            value = email,
            onValueChange = { email = it
                isEmailValid = validateEmail(it)},
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = email.isNotEmpty() && !isEmailValid,
            colors = TextFieldDefaults.textFieldColors(
                containerColor = textFieldBgColor,
                unfocusedIndicatorColor = Color.LightGray,
                focusedIndicatorColor = primaryColor
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(
                fontSize = 17.sp,
                fontFamily = helveticaFont
            )
        )

        // Password TextField
        TextField(
            value = password,
            onValueChange = { password = it
                isPasswordValid = validatePassword(it)},
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Password") },
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
                            id = if (passwordVisible) {
                                R.drawable.monkey
                            } else {
                                R.drawable.eyes
                            }
                        ),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color.Unspecified
                    )
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = textFieldBgColor,
                unfocusedIndicatorColor = Color.LightGray,
                focusedIndicatorColor = primaryColor
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(
                fontSize = 17.sp,
                fontFamily = helveticaFont
            )
        )

        // Confirm Password TextField
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it
                isConfirmPasswordValid = password == it},
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Confirm Password") },
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
                            id = if (confirmPasswordVisible) {
                                R.drawable.monkey
                            } else {
                                R.drawable.eyes
                            }
                        ),
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                        tint = Color.Unspecified
                    )
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = textFieldBgColor,
                unfocusedIndicatorColor = Color.LightGray,
                focusedIndicatorColor = primaryColor
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(
                fontSize = 17.sp,
                fontFamily = helveticaFont
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Register Button
        Button(
            onClick = {
                if (isUsernameValid && isEmailValid && isPasswordValid && password == confirmPassword) {
                    viewModel.registerUser(email, password)
                } },
            enabled = isUsernameValid &&
                    isEmailValid &&
                    isPasswordValid &&
                    isConfirmPasswordValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Create Account",
                style = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = helveticaFont
                )
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Social Sign Up Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(
                modifier = Modifier.weight(1f),
                color = Color.LightGray
            )
            Text(
                text = "Or continue with",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = TextStyle(
                    fontSize = 15.sp,
                    color = secondaryTextColor,
                    fontFamily = helveticaFont,
                    fontWeight = FontWeight.Bold
                )
            )
            Divider(
                modifier = Modifier.weight(1f),
                color = Color.LightGray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Social Login Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SocialLoginButton(
                icon = R.drawable.google_g,
                onClick = { /* Handle Google sign up */ }
            )
            SocialLoginButton(
                icon = R.drawable.facebook_f_,
                onClick = { /* Handle Facebook sign up */ }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Login Now Section
        Row(
            modifier = Modifier.padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                style = TextStyle(
                    fontSize = 15.sp,
                    color = Color.Black,
                    fontFamily = helveticaFont,
                    fontWeight = FontWeight.SemiBold
                )
            )
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = "Login Now",
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = primaryColor,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = helveticaFont
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(onNavigateToLogin = { }, onNavigateToHome = {}, viewModel = AuthViewModel())
}