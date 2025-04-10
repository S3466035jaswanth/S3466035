package uk.ac.tees.mad.bloodconnect

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import uk.ac.tees.mad.bloodconnect.ui.theme.BloodConnectTheme
import uk.ac.tees.mad.bloodconnect.utils.RequestPermissions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BloodConnectTheme {
                val context = LocalContext.current

                RequestPermissions(
                    context = context,
                    onPermissionsGranted = {
                        Toast.makeText(context, "All permissions granted!", Toast.LENGTH_SHORT)
                            .show()
                    },
                    onPermissionsDenied = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
                AppNavigation()
            }
        }
    }
}
