package com.example.btl1mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    // Define the authentication credentials
    val validEmail = "user@gmail.com"
    val validPassword = "1234"

    // State for input fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Image(
            painter = painterResource(id = R.drawable.bk_name_vi),
            contentDescription = "Logo trường top",
            modifier = Modifier.padding(20.dp)
        )

        Spacer(modifier = Modifier.height(100.dp))

        Image(
            painter = painterResource(id = R.drawable.bk),
            contentDescription = "Logo trường",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(80.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Mã số cá nhân") }
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = errorMessage, color = androidx.compose.ui.graphics.Color.Red)
        }

        Spacer(modifier = Modifier.height(60.dp))

        Button(
            onClick = {
                if (email == validEmail && password == validPassword) {
                    onLoginSuccess()
                } else {
                    errorMessage = "Email hoặc mã số cá nhân không đúng"
                }
            },
            modifier = Modifier.size(width = 200.dp, height = 50.dp)
        ) {
            Text(text = "Đăng nhập", fontSize = 20.sp)
        }
    }
}