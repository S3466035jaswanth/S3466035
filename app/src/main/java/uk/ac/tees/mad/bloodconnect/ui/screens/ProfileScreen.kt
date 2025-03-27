package uk.ac.tees.mad.bloodconnect.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var bloodType by remember { mutableStateOf(TextFieldValue("")) }
    var contactInfo by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch user data from Firestore
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        name = TextFieldValue(document.getString("name") ?: "")
                        bloodType = TextFieldValue(document.getString("bloodType") ?: "")
                        contactInfo = TextFieldValue(document.getString("contactInfo") ?: "")
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    if (isLoading) {
        // Show Loading Indicator
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Profile", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Blood Type Field
            OutlinedTextField(
                value = bloodType,
                onValueChange = { bloodType = it },
                label = { Text("Blood Type") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Contact Info Field
            OutlinedTextField(
                value = contactInfo,
                onValueChange = { contactInfo = it },
                label = { Text("Contact Info") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    saveUserProfile(db, userId, name.text, bloodType.text, contactInfo.text) { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}

// Function to Save Profile Data to Firestore
fun saveUserProfile(
    db: FirebaseFirestore,
    userId: String,
    name: String,
    bloodType: String,
    contactInfo: String,
    callback: (String) -> Unit
) {
    if (userId.isEmpty()) {
        callback("User not logged in")
        return
    }
    val userProfile = mapOf(
        "name" to name,
        "bloodType" to bloodType,
        "contactInfo" to contactInfo
    )

    db.collection("users").document(userId).set(userProfile)
        .addOnSuccessListener { callback("Profile updated successfully") }
        .addOnFailureListener { e -> callback("Update failed: ${e.localizedMessage}") }
}