package uk.ac.tees.mad.bloodconnect.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import uk.ac.tees.mad.bloodconnect.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val bloodRequests = remember { mutableStateListOf<BloodRequest>() }

    // Fetch Blood Requests in Real-time
    LaunchedEffect(Unit) {
        db.collection("blood_requests")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Error fetching requests", e)
                    return@addSnapshotListener
                }
                bloodRequests.clear()
                snapshot?.documents?.forEach { doc ->
                    val data = doc.data
                    val bloodRequest = BloodRequest(
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
                    bloodRequests.add(bloodRequest)
                }
            }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.RequestBlood.route)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("BloodConnect") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(bloodRequests) { request ->
                BloodRequestItem(request, navController)
            }
        }
    }
}


@Composable
fun BloodRequestItem(request: BloodRequest, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier
            .padding(16.dp)
            .fillMaxWidth()) {
            Text(
                "Blood Group: ${request.bloodGroup}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text("Requested by: ${request.requesterName}")
            Text("Units Required: ${request.unitsRequired}")
            Text("Hospital: ${request.hospitalName}")
            Button(
                onClick = { navController.navigate("donorSearch/${request.latitude}/${request.longitude}/${request.bloodGroup}") },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("See details")
            }
        }
    }
}

data class BloodRequest(
    val bloodGroup: String = "",
    val requesterName: String = "",
    val contact: String = "",
    val unitsRequired: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val hospitalName: String = "",
    val documentImage: String? = null,
    val timestamp: Long = 0L
)
