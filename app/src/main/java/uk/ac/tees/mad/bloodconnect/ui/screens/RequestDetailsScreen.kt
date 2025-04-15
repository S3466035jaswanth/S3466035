package uk.ac.tees.mad.bloodconnect.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailsScreen(requestId: String, navController: NavController) {

    val db = FirebaseFirestore.getInstance()
    var requestDetails by remember { mutableStateOf<BloodRequest?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(requestId) {
        db.collection("blood_requests").document(requestId).get()
            .addOnSuccessListener { doc ->
                val data = doc.data
                requestDetails = BloodRequest(
                    id = doc.id,
                    bloodGroup = data?.get("bloodGroup") as? String ?: "",
                    requesterName = data?.get("requesterName") as? String ?: "",
                    contact = data?.get("contact") as? String ?: "",
                    unitsRequired = data?.get("unitsRequired") as? String ?: "",
                    latitude = (data?.get("latitude") as? Number)?.toDouble() ?: 0.0,
                    longitude = (data?.get("longitude") as? Number)?.toDouble() ?: 0.0,
                    hospitalName = data?.get("hospitalName") as? String ?: "",
                    documentImage = data?.get("documentImage") as? String,
                    timestamp = (data?.get("timestamp") as? Number)?.toLong() ?: 0L
                )
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Blood Request Details",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            requestDetails?.let { request ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {


                    DetailRow(label = "Blood Group", value = request.bloodGroup)
                    DetailRow(label = "Requester", value = request.requesterName)
                    DetailRow(label = "Contact", value = request.contact)
                    DetailRow(label = "Units Required", value = request.unitsRequired)
                    DetailRow(label = "Hospital", value = request.hospitalName)

                    request.documentImage?.let { base64Image ->
                        val decodedBitmap = decodeBase64ToBitmap(base64Image)
                        decodedBitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Uploaded Document",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                            )
                        }
                    }
                }
            } ?: Text("Error: Request details not found!", color = Color.Red)
        }
    }

}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label:", fontWeight = FontWeight.Bold)
        Text(text = value)
    }
}

