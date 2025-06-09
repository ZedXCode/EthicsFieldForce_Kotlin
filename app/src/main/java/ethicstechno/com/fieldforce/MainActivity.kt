package ethicstechno.com.fieldforce

import AnimationType
import addFragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ethicstechno.com.fieldforce.databinding.ActivityMainBinding
import ethicstechno.com.fieldforce.db.AppDatabase
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.receiver.NetworkChangeReceiver
import ethicstechno.com.fieldforce.ui.fragments.SplashFragment
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.NETWORK_STATUS
import ethicstechno.com.fieldforce.utils.NETWORK_STATUS_CHANGE
import ethicstechno.com.fieldforce.utils.NETWORK_STATUS_CONNECTED
import ethicstechno.com.fieldforce.utils.NETWORK_STATUS_DISCONNECTED
import ethicstechno.com.fieldforce.utils.NETWORK_STATUS_SLOW

class MainActivity : AppCompatActivity() {

    lateinit var appDatabase: AppDatabase
    lateinit var binding: ActivityMainBinding
    private lateinit var networkReceiver: NetworkChangeReceiver
    //private lateinit var appUpdateManager: AppUpdateManager


    private val networkStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val status = intent?.getStringExtra(NETWORK_STATUS)

            when (status) {
                NETWORK_STATUS_CONNECTED -> {
                    hideLayout()
                    binding.appBarHome.contentHome.txtInternetCheck.text = "Internet is Connected"
                    binding.appBarHome.contentHome.imgInternet.setImageDrawable(
                        ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_internet_working)
                    )
                    binding.appBarHome.contentHome.lylInternet.setBackgroundColor(
                        ContextCompat.getColor(this@MainActivity, R.color.colorGreen)
                    )
                }

                NETWORK_STATUS_SLOW -> {
                    showLayout()
                    binding.appBarHome.contentHome.txtInternetCheck.text = "Internet is too slow or not working"
                    binding.appBarHome.contentHome.imgInternet.setImageDrawable(
                        ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_no_internet)
                    )
                    binding.appBarHome.contentHome.lylInternet.setBackgroundColor(
                        ContextCompat.getColor(this@MainActivity, R.color.colorRed)
                    )
                }

                NETWORK_STATUS_DISCONNECTED -> {
                    showLayout()
                    binding.appBarHome.contentHome.txtInternetCheck.text = "Internet is not working, Please check"
                    binding.appBarHome.contentHome.imgInternet.setImageDrawable(
                        ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_no_internet)
                    )
                    binding.appBarHome.contentHome.lylInternet.setBackgroundColor(
                        ContextCompat.getColor(this@MainActivity, R.color.colorRed)
                    )
                }
            }
        }
    }

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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            //appUpdateManager = AppUpdateManagerFactory.create(this)
            //checkUpdates()
            Log.e("TAG", "onCreate: ")
            appDatabase = AppDatabase.getDatabase(this)
            addFragment(SplashFragment(), false, true, animationType = AnimationType.fadeInfadeOut)
        } catch (e: Exception) {
            CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$onCreate()$ :: error = " + e.message.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //appUpdateManager.unregisterListener(listener)
    }

    override fun onStart() {
        super.onStart()
        networkReceiver = NetworkChangeReceiver()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(networkReceiver)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(networkStatusReceiver)
    }

    override fun onResume() {
        super.onResume()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            networkStatusReceiver,
            IntentFilter(NETWORK_STATUS_CHANGE)
        )

        /*appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                CommonMethods.showToastMessage(
                    this,
                    getString(R.string.update_has_just_been_downloaded)
                )
            }
        }*/
    }

    fun showUpdateDialog() {
        CommonMethods.showAlertDialog(this,
            getString(R.string.update_application),
            getString(R.string.you_must_need_to_update),
            positiveButtonText = getString(R.string.update),
            okListener = object : PositiveButtonListener {
                override fun okClickListener() {
                    try {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=${packageName}")
                        )
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
                        CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$showUpdateDialog()$ :: error = " + e.message.toString())
                        FirebaseCrashlytics.getInstance().recordException(e)
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=${packageName}")
                        )
                        startActivity(intent)
                    }
                }
            }, isCancelVisibility = false
        )
    }

    private fun showLayout() {
        binding.appBarHome.contentHome.lylInternet.visibility = View.VISIBLE
        binding.appBarHome.contentHome.lylInternet.translationY = -binding.appBarHome.contentHome.lylInternet.height.toFloat()
        binding.appBarHome.contentHome.lylInternet.animate()
            .translationY(0f)
            .setDuration(1000)
            .start()
    }

    private fun hideLayout() {
        binding.appBarHome.contentHome.lylInternet.animate()
            .translationY(-binding.appBarHome.contentHome.lylInternet.height.toFloat())
            .setDuration(1000)
            .withEndAction { binding.appBarHome.contentHome.lylInternet.visibility = View.GONE }
            .start()
    }
}