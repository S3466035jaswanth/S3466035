package uk.ac.tees.mad.bloodconnect.ui.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import uk.ac.tees.mad.bloodconnect.BloodRequestViewModel
import uk.ac.tees.mad.bloodconnect.Screen
import uk.ac.tees.mad.bloodconnect.toDomain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: BloodRequestViewModel = viewModel()) {
    val bloodRequests by viewModel.bloodRequests.collectAsState(emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.RequestBlood.route) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        topBar = {
            TopAppBar(title = { Text("BloodConnect") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(bloodRequests) { request ->
                BloodRequestItem(request.toDomain(), navController)
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
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                "Blood Group: ${request.bloodGroup}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text("Requested by: ${request.requesterName}")
            Text("Units Required: ${request.unitsRequired}")
            Text("Hospital: ${request.hospitalName}")
            Button(
                onClick = { navController.navigate("request_details/${request.id}") },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("See details")
            }
        }
    }
}

data class BloodRequest(
    val id: String,
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