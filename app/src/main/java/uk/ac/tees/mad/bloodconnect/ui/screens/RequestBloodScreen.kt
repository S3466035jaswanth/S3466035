package uk.ac.tees.mad.bloodconnect.ui.screens

import android.graphics.Bitmap
import android.location.Location
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestBloodScreen(navController: NavController) {
    val context = LocalContext.current
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
                onClick = { /* Submit Blood Request */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Request")
            }
        }
    }
}
