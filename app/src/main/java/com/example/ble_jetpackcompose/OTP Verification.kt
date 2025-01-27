package com.example.ble_jetpackcompose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

    @Composable
    fun OTPVerificationScreen() {
        val typography = Typography()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Back Button
            IconButton(
                onClick = { /* Navigate back */ },
                modifier = Modifier
                    .fillMaxWidth() // Fill the width
                    .wrapContentSize(Alignment.CenterStart) // Align to the start
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = colorResource(id = R.color.black)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // OTP Verification Header
            Text(
                text = "OTP Verification",
                style = typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Instruction Text
            Text(
                text = "Enter the verification code we just sent on your email address.",
                style = typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // OTP Input Fields
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                repeat(4) {
                    OTPInputField()
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Verify Button
            Button(
                onClick = { /* Handle OTP verification */ },
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.black)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = "Verify", color = Color.White)
            }

            Spacer(modifier = Modifier.height(300.dp))
            Spacer(modifier = Modifier.height(100.dp))

            // Resend Code
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Didn't received code?",
                    style = typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = { /* Resend OTP */ }) {
                    Text(text = "Resend", color = colorResource(id = R.color.black))
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun OTPInputField() {
        var otpValue by remember { mutableStateOf("") }

        TextField(
            value = otpValue,
            onValueChange = { value ->
                if (value.length <= 1) otpValue = value
            },
            modifier = Modifier
                .size(56.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            ),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }

@Preview(showBackground = true)
@Composable
fun OTPVerificationScreenPreview() {
    OTPVerificationScreen()
}