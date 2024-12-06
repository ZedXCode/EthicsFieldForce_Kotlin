package ethicstechno.com.fieldforce.utils

import android.util.Log
import ethicstechno.com.fieldforce.permission.KotlinPermissions
import ethicstechno.com.fieldforce.ui.activities.HomeActivity


class PermissionUtil(private val mActivity: HomeActivity) {

    fun requestPermissions(
        permissions: List<String>,
        onPermissionGranted: () -> Unit
    ) {
        KotlinPermissions.with(mActivity)
            .permissions(*permissions.toTypedArray())
            .onAccepted {
                Log.e("TAG", "requestPermissions: "+it.size+", I ACCEPTED :: "+permissions.size )
                if (it.size == permissions.size) {
                    onPermissionGranted()
                }
            }
            .onDenied {
                requestPermissions(permissions, onPermissionGranted)
            }
            .onForeverDenied {
                // Handle the case where the user has denied permission forever.
                // You can show a dialog or navigate to app settings from here.
            }
            .ask()
    }
}