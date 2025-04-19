package uk.ac.tees.mad.bloodconnect.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import uk.ac.tees.mad.bloodconnect.BloodRequestViewModel
import uk.ac.tees.mad.bloodconnect.NotificationHelper
import uk.ac.tees.mad.bloodconnect.toDomain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailsScreen(
    requestId: String,
    navController: NavController,
    viewModel: BloodRequestViewModel = viewModel()
) {
    val context = LocalContext.current
    var requestDetails by remember { mutableStateOf<BloodRequest?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(requestId) {
        try {
            requestDetails = viewModel.getBloodRequest(requestId)?.toDomain()
            isLoading = false
        } catch (ex: Exception) {
            isLoading = false
        }
        // Show Notification when new request
        NotificationHelper.showNotification(
            context,
            "Urgent Blood Request",
            "${requestDetails?.bloodGroup} needed at ${requestDetails?.hospitalName}"
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Blood Request Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ElevatedButton(
                            onClick = { callNumber(context, request.contact) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Call")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Call")
                        }

                        ElevatedButton(
                            onClick = { sendMessage(context, request.contact) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = "Message")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Message")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ElevatedButton(
                        onClick = { openGoogleMaps(context, request.latitude, request.longitude) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Place, contentDescription = "Directions")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("See Directions")
                    }
                }
            } ?: Text("Error: Request details not found!", color = Color.Red)
        }
    }
}

// Function to Call
fun callNumber(context: Context, number: String) {
    val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
    context.startActivity(callIntent)
}

// Function to Send SMS
fun sendMessage(context: Context, number: String) {
    val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$number"))
    context.startActivity(smsIntent)
}

// Function to Open Google Maps
fun openGoogleMaps(context: Context, latitude: Double, longitude: Double) {
    val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Blood Request Location)")
    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(mapIntent)
}

// Detail Row Component
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
