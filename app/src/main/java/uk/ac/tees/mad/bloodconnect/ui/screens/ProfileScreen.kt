package uk.ac.tees.mad.bloodconnect.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.decodeBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var bloodType by remember { mutableStateOf(TextFieldValue("")) }
    var contactInfo by remember { mutableStateOf(TextFieldValue("")) }
    var profileImageBase64 by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        context.contentResolver,
                        selectedUri
                    )
                )
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, selectedUri)
            }
            profileImageBase64 = encodeImageToBase64(bitmap)
        }
    }

    // Fetch user data from Firestore
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        name = TextFieldValue(document.getString("name") ?: "")
                        bloodType = TextFieldValue(document.getString("bloodType") ?: "")
                        contactInfo = TextFieldValue(document.getString("contactInfo") ?: "")
                        profileImageBase64 = document.getString("profileImage")
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
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Profile", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Image Section
            Box(contentAlignment = Alignment.Center) {
                profileImageBase64?.let { base64 ->
                    val bitmap = decodeBase64ToBitmap(base64)
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(120.dp)
                                .padding(8.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                } ?: run {
                    Box(
                        modifier = Modifier

                            .size(120.dp)
                            .padding(8.dp)
                            .clip(CircleShape)

                            .background(Color.LightGray)
                            .clickable {
                                imagePickerLauncher.launch("image/*")
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
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
                    saveUserProfile(
                        db,
                        userId,
                        name.text,
                        bloodType.text,
                        contactInfo.text,
                        profileImageBase64
                    ) { message ->
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

// Function to Store User Profile
fun saveUserProfile(
    db: FirebaseFirestore,
    userId: String,
    name: String,
    bloodType: String,
    contactInfo: String,
    profileImageBase64: String?,
    callback: (String) -> Unit
) {
    if (userId.isEmpty()) {
        callback("User not logged in")
        return
    }
    val userProfile = mapOf(
        "name" to name,
        "bloodType" to bloodType,
        "contactInfo" to contactInfo,
        "profileImage" to profileImageBase64 // Save image as Base64 string
    )

    db.collection("users").document(userId).set(userProfile)
        .addOnSuccessListener { callback("Profile saved successfully") }
        .addOnFailureListener { e -> callback("Failed: ${e.localizedMessage}") }
}

// Function to Convert Bitmap to Base64
fun encodeImageToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

// Function to Decode Base64 to Bitmap
fun decodeBase64ToBitmap(base64: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}
