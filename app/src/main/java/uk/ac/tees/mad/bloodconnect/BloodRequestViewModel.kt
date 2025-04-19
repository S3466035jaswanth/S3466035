package uk.ac.tees.mad.bloodconnect

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.bloodconnect.database.BloodRequestDao
import uk.ac.tees.mad.bloodconnect.database.BloodRequestDatabase
import uk.ac.tees.mad.bloodconnect.database.BloodRequestEntity
import uk.ac.tees.mad.bloodconnect.ui.screens.BloodRequest

class BloodRequestViewModel(application: Application) : AndroidViewModel(application) {

    val db = FirebaseFirestore.getInstance()

    val dao: BloodRequestDao by lazy {
        BloodRequestDatabase.getDatabase(application).bloodRequestDao()
    }

    private val _bloodRequests = MutableStateFlow<List<BloodRequestEntity>>(emptyList())
    val bloodRequests = _bloodRequests.asStateFlow()

    init {
        viewModelScope.launch {
            // Observe local data first
            dao.getAllRequests().collect { _bloodRequests.value = it }
        }
        fetchBloodRequestsFromFirestore()
    }

    private fun fetchBloodRequestsFromFirestore() {
        db.collection("blood_requests").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            snapshot?.let {
                val requests = it.documents.mapNotNull { doc -> doc.toBloodRequestEntity() }
                viewModelScope.launch {
                    dao.clearRequests()
                    dao.insertRequests(requests)
                }
            }
        }
    }

    fun getBloodRequest(requestId: String): BloodRequestEntity? {

        return _bloodRequests.value.find { it.id == requestId }

    }
}

fun DocumentSnapshot.toBloodRequestEntity(): BloodRequestEntity? {
    val data = data ?: return null
    return BloodRequestEntity(
        id = id,
        bloodGroup = data["bloodGroup"] as? String ?: "",
        requesterName = data["requesterName"] as? String ?: "",
        contact = data["contact"] as? String ?: "",
        unitsRequired = data["unitsRequired"] as? String ?: "",
        latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
        longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
        hospitalName = data["hospitalName"] as? String ?: "",
        documentImage = data["documentImage"] as? String,
        timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L
    )
}

fun BloodRequestEntity.toDomain(): BloodRequest {
    return BloodRequest(
        id,
        bloodGroup,
        requesterName,
        contact,
        unitsRequired,
        latitude,
        longitude,
        hospitalName,
        documentImage,
        timestamp
    )
}

