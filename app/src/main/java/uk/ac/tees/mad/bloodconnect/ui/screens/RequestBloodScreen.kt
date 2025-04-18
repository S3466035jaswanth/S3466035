package uk.ac.tees.mad.bloodconnect.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestBloodScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var bloodGroup by remember { mutableStateOf("") }
    var unitsRequired by remember { mutableStateOf("") }
    var hospitalName by remember { mutableStateOf("") }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var documentImage by remember { mutableStateOf<Bitmap?>(null) }
    var encodedImage by remember { mutableStateOf<String?>(null) }

    // Camera launcher to capture image
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            documentImage = bitmap
            encodedImage = encodeImageToBase64(bitmap)
        }
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                userLocation = location
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Request Blood"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = bloodGroup,
                onValueChange = { bloodGroup = it },
                label = { Text("Blood Group") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = unitsRequired,
                onValueChange = { unitsRequired = it },
                label = { Text("Units Required") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = hospitalName,
                onValueChange = { hospitalName = it },
                label = { Text("Hospital Name") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            userLocation?.let {
                Text("Your Location: ${it.latitude}, ${it.longitude}")
            } ?: Text("Fetching location...")

            Spacer(modifier = Modifier.height(8.dp))
            // Button to open camera
            Button(onClick = { cameraLauncher.launch(null) }) {
                Text("Capture Document")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Show captured image if available
            documentImage?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Captured Image",
                    modifier = Modifier.size(200.dp),
                    contentScale = ContentScale.FillWidth
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (bloodGroup.isEmpty() || unitsRequired.isEmpty() || hospitalName.isEmpty() || documentImage == null || userLocation == null) {
                        return@Button
                    }
                    submitBloodRequest(
                        auth,
                        db,
                        bloodGroup,
                        unitsRequired,
                        hospitalName,
                        encodedImage,
                        userLocation!!,
                        context
                    ) {
                        navController.navigateUp()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Request")
            }
        }
    }
}


private fun submitBloodRequest(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    bloodGroup: String,
    units: String,
    hospital: String,
    imageBase64: String?,
    userLocation: Location,
    context: Context,
    onSuccess: () -> Unit
) {
    val userId = auth.currentUser?.uid
    if (userId == null) {
        Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
        return
    }

    db.collection("users").document(userId).get()
        .addOnSuccessListener { snapshot ->
            val name = snapshot.getString("name") ?: "Unknown"
            val contactInfo = snapshot.getString("contactInfo") ?: "Not Available"

            if (name == "Unknown" || contactInfo == "Not Available") {
                Toast.makeText(context, "Please complete your profile details!", Toast.LENGTH_SHORT)
                    .show()
                return@addOnSuccessListener
            }

            val request = hashMapOf(
                "bloodGroup" to bloodGroup,
                "requesterName" to name,
                "contact" to contactInfo,
                "unitsRequired" to units,
                "latitude" to userLocation.latitude,
                "longitude" to userLocation.longitude,
                "hospitalName" to hospital,
                "documentImage" to imageBase64,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("blood_requests")
                .add(request)
                .addOnSuccessListener {
                    Toast.makeText(context, "Blood request submitted!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to submit request", Toast.LENGTH_SHORT).show()
                }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error fetching user details!", Toast.LENGTH_SHORT).show()
        }
}
