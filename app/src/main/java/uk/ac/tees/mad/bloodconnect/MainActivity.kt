package uk.ac.tees.mad.bloodconnect

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.bloodconnect.ui.theme.BloodConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BloodConnectTheme {
                AuthScreen()
            }
        }
    }
}

@Composable
fun AuthScreen() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) } // Toggle between login & sign-up

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLoginMode) "Login" else "Sign Up",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Auth Button
        Button(
            onClick = {
                if (isLoginMode) {
                    loginUser(auth, email, password) { result ->
                        message = result
                        Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    signUpUser(auth, email, password) { result ->
                        message = result
                        Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isLoginMode) "Login" else "Sign Up")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Toggle Login/SignUp Mode
        TextButton(onClick = { isLoginMode = !isLoginMode }) {
            Text(text = if (isLoginMode) "Don't have an account? Sign Up" else "Already have an account? Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display Message (Success/Error)
        if (message.isNotEmpty()) {
            Text(text = message, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// Function to Handle User Sign-Up
fun signUpUser(auth: FirebaseAuth, email: String, password: String, callback: (String) -> Unit) {
    if (email.isBlank() || password.isBlank()) {
        callback("Email and Password cannot be empty")
        return
    }
    auth.createUserWithEmailAndPassword(email, password)
        .addOnSuccessListener {
            callback("Sign Up Successful! Please log in.")
        }
        .addOnFailureListener { e ->
            callback("Sign Up Failed: ${e.localizedMessage}")
        }
}

// Function to Handle User Login
fun loginUser(auth: FirebaseAuth, email: String, password: String, callback: (String) -> Unit) {
    if (email.isBlank() || password.isBlank()) {
        callback("Email and Password cannot be empty")
        return
    }
    auth.signInWithEmailAndPassword(email, password)
        .addOnSuccessListener {
            callback("Login Successful!")
        }
        .addOnFailureListener { e ->
            callback("Login Failed: ${e.localizedMessage}")
        }
}