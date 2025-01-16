package com.example.ble_jetpackcompose
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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


val helveticaFont = FontFamily(
    Font(R.font.helvetica),
    Font(R.font.helvetica_bold, weight = FontWeight.Bold)
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Define custom colors
    val primaryColor = Color(0xFF007AFF) // iOS blue
    val backgroundColor = Color(0xFFF2F2F7) // iOS light gray
    val textFieldBgColor = Color(0xFFFFFFFF)
    val secondaryTextColor = Color(0xFF8E8E93)

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
                color = secondaryTextColor,
                fontFamily = helveticaFont,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(90.dp))

        // Email TextField
        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
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

        // Password TextField
        TextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible },
                modifier = Modifier.size(24.dp))
                {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) {
                                R.drawable.monkey // Replace with your visible icon resource
                            } else {
                                R.drawable.eyes // Replace with your hidden icon resource
                            }
                        ),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color.Unspecified // Ensure original colors of the icon are preserved


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




        // Forgot Password
        TextButton(
            onClick = { /* Handle forgot password */ },
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp)
        ) {
            Text(
                text = "Forgot Password?",
                style = TextStyle(
                    fontSize = 15.sp,
                    color = primaryColor,
                    fontFamily = helveticaFont,
                    fontWeight = FontWeight.SemiBold,
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign In Button
        Button(
            onClick = { /* Handle login */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Sign In",
                style = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = helveticaFont,

                )
            )
        }

//        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(48.dp))

        // Social Sign In Section
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

        Spacer(modifier = Modifier.height(60.dp))

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



            TextButton(onClick = { /* Navigate to register */ }) {
                Text(
                    text = "Register Now",
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

@Composable
private fun SocialLoginButton(
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

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}