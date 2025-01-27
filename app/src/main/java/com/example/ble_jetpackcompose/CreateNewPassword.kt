package com.example.ble_jetpackcompose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
//import androidx.compose.material.icons.filled.Visibility
//import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreateNewPasswordScreen() {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    val primaryColor = Color(0xFF007AFF) // iOS blue

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(70.dp))

                AnimatedVisibility(
                    visible = true,
                    enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn()
                ) {
                    Text(
                        text = "Create new password",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = helveticaFont,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }


                Spacer(modifier = Modifier.height(10.dp))



                Text(
                    text = "Your new password must be unique from those previously used.",
                    fontSize = 16.sp,
                    color = Color.Gray.copy(alpha = 0.8f),
                    fontFamily = helveticaFont,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))


                // Password Fields Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password", fontFamily = helveticaFont) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colors.primary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        visualTransformation = if (isNewPasswordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            androidx.compose.material3.IconButton(
                                onClick = { isNewPasswordVisible = !isNewPasswordVisible },
                                modifier = Modifier.size(24.dp)
                            )
                            {
                                androidx.compose.material3.Icon(
                                    painter = painterResource(
                                        id = if (isNewPasswordVisible) {
                                            R.drawable.monkey // Replace with your visible icon resource
                                        } else {
                                            R.drawable.eyes // Replace with your hidden icon resource
                                        }
                                    ),
                                    contentDescription = if (isNewPasswordVisible) "Hide password" else "Show password",
                                    tint = Color.Unspecified // Ensure original colors of the icon are preserved


                                )
                            }
                        },
                    )


                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password", fontFamily = helveticaFont) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colors.primary,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            visualTransformation = if (isConfirmPasswordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                androidx.compose.material3.IconButton(
                                    onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                                    modifier = Modifier.size(24.dp)
                                )
                                {
                                    androidx.compose.material3.Icon(
                                        painter = painterResource(
                                            id = if (isConfirmPasswordVisible) {
                                                R.drawable.monkey // Replace with your visible icon resource
                                            } else {
                                                R.drawable.eyes // Replace with your hidden icon resource
                                            }
                                        ),
                                        contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password",
                                        tint = Color.Unspecified // Ensure original colors of the icon are preserved


                                    )
                                }
                            },
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))


                Button(
                    onClick = { /* Handle send code action */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = primaryColor ,
                        contentColor = Color.White
                    )
                )


                {
                    Text(
                        text = "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = helveticaFont
                    )
                }

//                Spacer(modifier = Modifier.height(0.dp))
            }

            // Bottom Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Remember Password? ",
                    color = Color.Gray.copy(alpha = 0.8f),
                    fontFamily = helveticaFont,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { /* Navigate to login */ }) {
                    Text(
                        text = "Login",
                        color = primaryColor,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = helveticaFont
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateNewPasswordScreenPreview() {
    CreateNewPasswordScreen()
}