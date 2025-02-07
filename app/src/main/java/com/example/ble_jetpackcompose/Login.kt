@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ble_jetpackcompose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.compose.ui.unit.times
import com.example.ble_jetpackcompose.AppColors.PrimaryColor
import com.example.ble_jetpackcompose.AppColors.SecondaryTextColor
import com.example.ble_jetpackcompose.AppColors.TextFieldBackgroundColor
import kotlinx.coroutines.delay

val helveticaFont = FontFamily(
    Font(R.font.helvetica),
    Font(R.font.helvetica_bold, weight = FontWeight.Bold)
)

object AppColors {
    val PrimaryColor = Color(0xFF007AFF) // iOS blue
    val BackgroundColor = Color(0xFFF2F2F7) // iOS light gray
    val TextFieldBackgroundColor = Color(0xFFFFFFFF) // White
    val SecondaryTextColor = Color(0xFF8E8E93) // Gray
    val ErrorColor = Color(0xFFFF3B30) // iOS red
    val DividerColor = Color(0xFFE5E5EA) // Light gray for dividers
    val DisabledColor = Color(0xFFE5E5EA) // Light gray for disabled states
    val TextPrimaryColor = Color(0xFF000000) // Black
    val TextSecondaryColor = Color(0xFF8E8E93) // Gray
    val SurfaceColor = Color(0xFFFFFFFF) // White
}


@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    // Track form validity
    val isFormValid by remember(email, password) {
        derivedStateOf {
            isValidEmail(email) && isValidPassword(password)
        }
    }

    val authState by viewModel.authState.collectAsState()

    // Handle loading state with a blocking dialog
    if (authState is AuthState.Loading) {
        LoadingDialog()
    }

    errorMessage?.let { error ->
        LaunchedEffect(error) {
            delay(5000)
            errorMessage = null
        }
    }

    // Handle auth state changes and errors
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onNavigateToHome()
            }

            is AuthState.Error -> {
                val error = (authState as AuthState.Error).message
                errorMessage = when {
                    error.contains("network", ignoreCase = true) ->
                        "Network error. Please check your connection."

                    error.contains("credentials", ignoreCase = true) ->
                        "Invalid email or password."

                    else -> "An unexpected error occurred. Please try again."
                }
            }

            is AuthState.PasswordResetEmailSent -> {
                showForgotPasswordDialog = false
                errorMessage = null
                // Show success message
                // You can handle this how you prefer - for now we'll use the error message system
                errorMessage = "Password reset email sent! Please check your inbox."
            }
            else -> {}
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Welcome Text
        Text(
            text = "Welcome Back",
            style = TextStyle(
                fontSize = 34.sp,
                fontFamily = helveticaFont,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Sign in to continue",
            style = TextStyle(
                fontSize = 17.sp,
                color = SecondaryTextColor,
                fontFamily = helveticaFont,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(90.dp))

        // Email TextField
        EmailTextField(
            email = email,
            onEmailChange = {
                email = it
                errorMessage = null
            },
            isError = !isValidEmail(email) && email.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Password TextField
        PasswordTextField(
            password = password,
            onPasswordChange = {
                password = it
                errorMessage = null
            },
            passwordVisible = passwordVisible,
            onPasswordVisibilityChange = { passwordVisible = it },
            isError = !isValidPassword(password) && password.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = helveticaFont
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }


        // Forgot Password
        TextButton(
            onClick = { showForgotPasswordDialog = true  },
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp)
        ) {
            Text(
                text = "Forgot Password?",
                style = TextStyle(
                    fontSize = 15.sp,
                    color = PrimaryColor,
                    fontFamily = helveticaFont,
                    fontWeight = FontWeight.SemiBold,
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign In Button
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
                containerColor = PrimaryColor,
                disabledContainerColor = PrimaryColor.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = "Sign In",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = helveticaFont
                    )
                )
            }
        }

//        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(48.dp))

        // Social Sign In Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color.LightGray
            )
            Text(
                text = "Or continue with",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = TextStyle(
                    fontSize = 15.sp,
                    color = SecondaryTextColor,
                    fontFamily = helveticaFont,
                    fontWeight = FontWeight.Bold
                )
            )
            HorizontalDivider(
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
                onClick = { /* Handle Google login */ }
            )
//            SocialLoginButton(
//                icon = R.drawable.ic_apple,
//                onClick = { /* Handle Apple login */ }
//            )
            SocialLoginButton(
                icon = R.drawable.facebook_f_,
                onClick = { /* Handle Facebook login */ }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Register Now Section
        Row(
            modifier = Modifier
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                style = TextStyle(
                    fontSize = 15.sp,
                    color = Color.Black,
                    fontFamily = helveticaFont,
                    fontWeight = FontWeight.SemiBold
                )
            )

            if (showForgotPasswordDialog) {
                AlertDialog(
                    onDismissRequest = { showForgotPasswordDialog = false },
                    title = {
                        Text(
                            text = "Reset Password",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontFamily = helveticaFont,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Enter your email address and we'll send you a link to reset your password.",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontFamily = helveticaFont
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            TextField(
                                value = forgotPasswordEmail,
                                onValueChange = { forgotPasswordEmail = it },
                                placeholder = { Text("Email") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Done
                                ),
                                colors = TextFieldDefaults.textFieldColors(
                                    containerColor = TextFieldBackgroundColor,
                                    unfocusedIndicatorColor = Color.LightGray,
                                    focusedIndicatorColor = PrimaryColor
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
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
                                containerColor = PrimaryColor,
                                disabledContainerColor = PrimaryColor.copy(alpha = 0.7f)
                            )
                        ) {
                            Text("Send Reset Link")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showForgotPasswordDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = "Register Now",
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = PrimaryColor,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = helveticaFont
                    )
                )
            }
        }
    }
}

@Composable
fun SocialLoginButton(
    icon: Int,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(64.dp), // Increased button size
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.LightGray),
        contentPadding = PaddingValues(12.dp) // Added padding inside button
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(32.dp), // Increased icon size
            tint = Color.Unspecified
        )
    }
}

//@Composable
//fun LoadingDialog() {
//    val primaryColor = MaterialTheme.colorScheme.primary
//    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
//
//    Box(
//        contentAlignment = Alignment.Center, // Centers the inner Box within the parent
//        modifier = Modifier
//            .wrapContentSize() // Ensures the size wraps the content
//    ) {
//        Box(
//            modifier = Modifier
//                .size(160.dp)
//                .shadow(elevation = 30.dp, shape = MaterialTheme.shapes.medium)
//                .background(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium),
//            contentAlignment = Alignment.Center
//        ) {
//            BluetoothIcon(primaryColor)
//            AnimatedWiFiArcs(primaryColor)
//
//            Spacer(modifier = Modifier.height(16.dp)) // Space between the animation and text
//
//            Text(
//                text = "Wait... it's Sensing",
//                style = MaterialTheme.typography.bodyMedium,
//                color = onSurfaceColor,
//                modifier = Modifier.align(Alignment.BottomCenter)
//            )
//        }
//    }
//}
//
//
//@Composable
//fun BluetoothIcon(primaryColor: Color) {
//    Icon(
//        imageVector = Icons.Default.Bluetooth,
//        contentDescription = "Bluetooth Icon",
//        modifier = Modifier.size(60.dp),
//        tint = primaryColor
//    )
//}
//
//@Composable
//fun AnimatedWiFiArcs(primaryColor: Color) {
//    val infiniteTransition = rememberInfiniteTransition(label = "WiFi Arc Animation Transition")
//
//    // Animating the alpha and scale of the arcs with labels for better inspection
//    val alpha by infiniteTransition.animateFloat(
//        initialValue = 0.1f,
//        targetValue = 1f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(2000, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ),
//        label = "WiFi Arc Alpha"
//    )
//    val scale by infiniteTransition.animateFloat(
//        initialValue = 1f,
//        targetValue = 1.5f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(2000, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ),
//        label = "WiFi Arc Scale"
//    )
//
//    Canvas (modifier = Modifier.fillMaxSize()) {
//        val baseRadius = size.minDimension / 4
//
//        for (i in 1..3) {
//            val scaledRadius = baseRadius * scale + (i * 20)
//            drawArc(
//                color = primaryColor.copy(alpha = alpha - (0.15f * i)),
//                startAngle = -45f,
//                sweepAngle = 90f,
//                useCenter = false,
//                style = Stroke(width = 6.dp.toPx()),
//                size = Size((scaledRadius * 1.6).toFloat(), (scaledRadius * 2.0).toFloat()),
//                topLeft = Offset(
//                    center.x - scaledRadius,
//                    center.y - scaledRadius
//                )
//            )
//        }
//    }
//}


@Composable
private fun EmailTextField(
    email: String,
    onEmailChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    TextField(
        value = email,
        onValueChange = onEmailChange,
        modifier = modifier,
        placeholder = { Text("Email") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        isError = isError,
        supportingText = {
            if (isError) {
                Text("Please enter a valid email address")
            }
        },
        colors = TextFieldDefaults.textFieldColors(
            containerColor = TextFieldBackgroundColor,
            unfocusedIndicatorColor = Color.LightGray,
            focusedIndicatorColor = PrimaryColor,
            errorIndicatorColor = MaterialTheme.colorScheme.error
        ),
        shape = RoundedCornerShape(12.dp),
        textStyle = TextStyle(
            fontSize = 17.sp,
            fontFamily = helveticaFont
        )
    )
}

@Composable
private fun PasswordTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    TextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = modifier,
        placeholder = { Text("Password") },
        visualTransformation = if (passwordVisible)
            VisualTransformation.None
        else
            PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        isError = isError,
        supportingText = {
            if (isError) {
                Text("Password must be at least 8 characters")
            }
        },
        trailingIcon = {
            IconButton(
                onClick = { onPasswordVisibilityChange(!passwordVisible) },
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
                    contentDescription = if (passwordVisible)
                        "Hide password"
                    else
                        "Show password",
                    tint = Color.Unspecified
                )
            }
        },
        colors = TextFieldDefaults.textFieldColors(
            containerColor = TextFieldBackgroundColor,
            unfocusedIndicatorColor = Color.LightGray,
            focusedIndicatorColor = PrimaryColor,
            errorIndicatorColor = MaterialTheme.colorScheme.error
        ),
        shape = RoundedCornerShape(12.dp),
        textStyle = TextStyle(
            fontSize = 17.sp,
            fontFamily = helveticaFont
        )
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