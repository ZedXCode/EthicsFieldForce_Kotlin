package ethicstechno.com.fieldforce

import addFragment
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ethicstechno.com.fieldforce.db.AppDatabase
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.ui.fragments.SplashFragment
import ethicstechno.com.fieldforce.utils.CommonMethods

class MainActivity : AppCompatActivity() {

    lateinit var appDatabase: AppDatabase
    //private lateinit var appUpdateManager: AppUpdateManager

    fun checkUpdates() {
        /*var appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
                        .build()
                )
            }
        }
        appUpdateManager.registerListener(listener)*/
    }

    private val listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            CommonMethods.showToastMessage(
                this,
                getString(R.string.update_has_just_been_downloaded)
            )
        }
    }
    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result: androidx.activity.result.ActivityResult ->
       /* if (result.resultCode != RESULT_OK) {
            CommonMethods.showAlertDialog(this,
                getString(R.string.update_application),
                getString(R.string.you_must_need_to_update),
                positiveButtonText = getString(R.string.update),
                okListener = object : PositiveButtonListener {
                    override fun okClickListener() {
                        checkUpdates()
                    }
                }, isCancelVisibility = false)
        }*/
    }

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main)
        //appUpdateManager = AppUpdateManagerFactory.create(this)
        //checkUpdates()
        Log.e("TAG", "onCreate: ")
        appDatabase = AppDatabase.getDatabase(this)
        addFragment(SplashFragment(), false, true, animationType = AnimationType.fadeInfadeOut)
    }

    override fun onDestroy() {
        super.onDestroy()
        //appUpdateManager.unregisterListener(listener)
    }

    override fun onResume() {
        super.onResume()

        /*appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                CommonMethods.showToastMessage(
                    this,
                    getString(R.string.update_has_just_been_downloaded)
                )
            }
        }*/
    }

    fun showUpdateDialog(){
        CommonMethods.showAlertDialog(this,
            getString(R.string.update_application),
            getString(R.string.you_must_need_to_update),
            positiveButtonText = getString(R.string.update),
            okListener = object : PositiveButtonListener {
                override fun okClickListener() {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${packageName}"))
                        intent.setPackage("com.android.vending")
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        } else {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=${packageName}")
                            )
                            startActivity(intent)
                        }
                    } catch (e: android.content.ActivityNotFoundException) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=${packageName}")
                        )
                        startActivity(intent)
                    }
                }
            }, isCancelVisibility = false)
    }
}