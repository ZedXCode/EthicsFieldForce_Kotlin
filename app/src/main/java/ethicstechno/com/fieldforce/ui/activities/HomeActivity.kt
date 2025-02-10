package ethicstechno.com.fieldforce.ui.activities

import AnimationType
import addFragment
import android.app.Activity
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ethicstechno.com.fieldforce.BuildConfig
import ethicstechno.com.fieldforce.MainActivity
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.databinding.ActivityHomeBinding
import ethicstechno.com.fieldforce.db.AppDatabase
import ethicstechno.com.fieldforce.db.dao.AppDao
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.NavMenuModel
import ethicstechno.com.fieldforce.permission.KotlinPermissions
import ethicstechno.com.fieldforce.service.EthicsBackgroundService
import ethicstechno.com.fieldforce.ui.fragments.bottomnavigation.AttendanceFragment
import ethicstechno.com.fieldforce.ui.fragments.bottomnavigation.MoreListFragment
import ethicstechno.com.fieldforce.ui.fragments.bottomnavigation.ReportListFragment
import ethicstechno.com.fieldforce.ui.fragments.bottomnavigation.TripFragment
import ethicstechno.com.fieldforce.ui.fragments.dashboard.DashboardFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.order_entry.AddOrderEntryFragment
import ethicstechno.com.fieldforce.ui.fragments.navigationdrawer.ChangePwdFragment
import ethicstechno.com.fieldforce.ui.fragments.navigationdrawer.DynamicPageContentFragment
import ethicstechno.com.fieldforce.ui.fragments.navigationdrawer.ProfileFragment
import ethicstechno.com.fieldforce.ui.fragments.navigationdrawer.SupportFragment
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.hideKeyboard
import ethicstechno.com.fieldforce.utils.EXTRA_LATITUDE
import ethicstechno.com.fieldforce.utils.EXTRA_LONGITUDE
import ethicstechno.com.fieldforce.utils.IS_FOR_ATTENDANCE_REPORT
import ethicstechno.com.fieldforce.utils.IS_LOGIN
import ethicstechno.com.fieldforce.utils.IS_MOCK_LOCATION
import ethicstechno.com.fieldforce.utils.ImageUtils
import ethicstechno.com.fieldforce.utils.NAV_ABOUT_US
import ethicstechno.com.fieldforce.utils.NAV_ACCOUNT_DETAILS
import ethicstechno.com.fieldforce.utils.NAV_CHANGE_PASSWORD
import ethicstechno.com.fieldforce.utils.NAV_LOGOUT
import ethicstechno.com.fieldforce.utils.NAV_PRIVACY_POLICY
import ethicstechno.com.fieldforce.utils.NAV_SUPPORT
import ethicstechno.com.fieldforce.utils.NAV_TERMS_AND_CONDITIONS
import ethicstechno.com.fieldforce.utils.PAGE_ABOUT_US
import ethicstechno.com.fieldforce.utils.PAGE_PRIVACY_POLICY
import ethicstechno.com.fieldforce.utils.PAGE_TERMS_CONDITION
import ethicstechno.com.fieldforce.utils.PermissionUtil

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: ActivityHomeBinding
    lateinit var appDatabase: AppDatabase
    lateinit var appDao: AppDao
    var fragment: Fragment? = null
    var serviceLat = "0.01"
    var serviceLng = "0.01"
    private var fusedLocationClient: FusedLocationProviderClient? = null
    var currentLatitude = 0.0
    var currentLongitude = 0.0


    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    private val locationSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (fusedLocationClient == null) {
                    return@registerForActivityResult
                }
                fetchLocation(fusedLocationClient!!) // Call the method to fetch the location again or perform any other necessary tasks.
            } else {
                CommonMethods.showToastMessage(this, getString(R.string.enable_location))
                locationEnableDialog()
                // Location settings resolution failed or was canceled.
                // Handle the failure or cancellation accordingly.
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appDatabase = AppDatabase.getDatabase(this)
        appDao = appDatabase.appDao()

        binding.navView.setNavigationItemSelectedListener(this)
        //gotoDashBoard()
        setUpNavigationDrawer()
        setBottomNavigationItem()
        ImageUtils().loadImageUrlWithoutPlaceHolder(
            this,
            appDao.getAppRegistration().apiHostingServer + appDao.getAppRegistration().logoFilePath,
            binding.headerLayout.imgBottom
        )
        binding.bottomNavigationView.selectedItemId = R.id.nav_home
        notificationPermissionFor33()
        doBackgroundServiceOperations()
    }

    fun doBackgroundServiceOperations() {
        if (appDao.getLoginData().attendanceId > 0) {
            val arrListOfPermission = arrayListOf<String>(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            PermissionUtil(this).requestPermissions(arrListOfPermission) {
                if (appDao.getLoginData().todayClockInDone && !appDao.getLoginData().todayClockOutDone) {
                    val locationManager =
                        getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        fusedLocationClient =
                            LocationServices.getFusedLocationProviderClient(this)
                        fetchLocation(fusedLocationClient!!)
                    } else {
                        CommonMethods.showToastMessage(this, getString(R.string.enable_location))
                        locationEnableDialog()
                    }

                } else {
                    serviceLat = "0.01"
                    serviceLng = "0.01"
                }
            }

        } else {
            val mServiceIntent = Intent(this, EthicsBackgroundService::class.java)
            if (isMyServiceRunning(EthicsBackgroundService::class.java)) {
                stopService(mServiceIntent)
            }
        }
    }

    private fun locationEnableDialog() {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 30 * 1000
            locationRequest.fastestInterval = 5 * 1000
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)

            val result: Task<LocationSettingsResponse> = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build())
            result.addOnCompleteListener { task ->
                try {
                    val response: LocationSettingsResponse? =
                        task.getResult(ApiException::class.java)
                    // All location settings are satisfied. The client can initialize location requests here.
                    fetchLocation(fusedLocationClient!!)
                    //Toast.makeText(activity, AppConstants.mLatitude + ", " + AppConstants.mLongitude, Toast.LENGTH_SHORT).show()
                } catch (exception: ApiException) {
                    FirebaseCrashlytics.getInstance().recordException(exception)
                    when (exception.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            val resolvable: ResolvableApiException =
                                exception as ResolvableApiException
                            val intentSenderRequest =
                                IntentSenderRequest.Builder(resolvable.resolution).build()
                            locationSettingsLauncher.launch(intentSenderRequest)
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            // settings, so we won't show the dialog.
                        }
                    }
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("TAG", "locationEnableDialog: " + e.printStackTrace())
        }
    }

    private fun fetchLocation(fusedLocationClient: FusedLocationProviderClient) {
        try {
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 1000
            locationRequest.fastestInterval = 1000
            val locationCallback: LocationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    if (locationResult == null) {
                        // Handle the case where the location is null
                        return
                    }
                    val location = locationResult.locations[0]
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        AppPreference.saveBooleanPreference(
                            applicationContext,
                            IS_MOCK_LOCATION,
                            location.isMock
                        )
                    } else {
                        AppPreference.saveBooleanPreference(
                            applicationContext,
                            IS_MOCK_LOCATION,
                            location.isFromMockProvider
                        )
                    }

                    if (AppPreference.getBooleanPreference(this@HomeActivity, IS_MOCK_LOCATION)) {
                        CommonMethods.showAlertDialog(
                            applicationContext,
                            getString(R.string.location_error_title),
                            getString(R.string.mock_location_msg),
                            okListener = object : PositiveButtonListener {
                                override fun okClickListener() {
                                    finish()
                                }
                            },
                            isCancelVisibility = false
                        )
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        AppPreference.saveBooleanPreference(
                            applicationContext,
                            IS_MOCK_LOCATION,
                            location.isMock
                        )
                    } else {
                        AppPreference.saveBooleanPreference(
                            applicationContext,
                            IS_MOCK_LOCATION,
                            location.isFromMockProvider
                        )
                    }
                    if (!isMyServiceRunning(EthicsBackgroundService::class.java)) {
                        startLocationService()
                    }
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            e.printStackTrace()
        }
    }


    private fun notificationPermissionFor33() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            KotlinPermissions.with(this)
                .permissions(android.Manifest.permission.POST_NOTIFICATIONS)
                .onAccepted {

                }
                .onDenied {
                    notificationPermissionFor33()
                }
                .onForeverDenied {
                    // Handle the case where the user has denied permission forever.
                    // You can show a dialog or navigate to app settings from here.
                }
                .ask()
        }
    }

    private fun setUpNavigationDrawer() {
        val menuItems = listOf(
            NavMenuModel(NAV_ACCOUNT_DETAILS, "Account Details", R.drawable.ic_nav_account_details),
            NavMenuModel(NAV_ABOUT_US, "About Us", R.drawable.ic_nav_about_us),
            NavMenuModel(NAV_TERMS_AND_CONDITIONS, "Terms & Conditions", R.drawable.ic_nav_terms_condition),
            NavMenuModel(NAV_PRIVACY_POLICY, "Privacy Policy", R.drawable.ic_nav_privacy_policy),
            NavMenuModel(NAV_CHANGE_PASSWORD, "Change Password", R.drawable.ic_nav_change_password),
            NavMenuModel(NAV_SUPPORT, "Support", R.drawable.ic_nav_support),
            NavMenuModel(NAV_LOGOUT, "Logout", R.drawable.ic_nav_logout)
        )

        val adapter = NavMenuAdapter(menuItems) { menuItem ->
            closeDrawer()
            when(menuItem.navId){
                NAV_ACCOUNT_DETAILS -> {
                    addFragment(
                        ProfileFragment(),
                        true,
                        false,
                        animationType = AnimationType.rightInLeftOut
                    )
                }
                NAV_ABOUT_US ->{
                    addFragment(
                        DynamicPageContentFragment.newInstance(PAGE_ABOUT_US),
                        true,
                        true,
                        AnimationType.rightInLeftOut
                    )
                }
                NAV_TERMS_AND_CONDITIONS -> {
                    addFragment(
                        DynamicPageContentFragment.newInstance(PAGE_TERMS_CONDITION),
                        true,
                        true,
                        AnimationType.rightInLeftOut
                    )
                }
                NAV_PRIVACY_POLICY -> {
                    addFragment(
                        DynamicPageContentFragment.newInstance(PAGE_PRIVACY_POLICY),
                        true,
                        true,
                        AnimationType.rightInLeftOut
                    )
                }
                NAV_CHANGE_PASSWORD -> {
                    addFragment(
                        ChangePwdFragment(),
                        true,
                        false,
                        animationType = AnimationType.rightInLeftOut
                    )
                }
                NAV_SUPPORT -> {
                    addFragment(SupportFragment(), true, true, AnimationType.rightInLeftOut)
                }
                NAV_LOGOUT -> {
                    callLogoutApi()
                }
            }
        }

        binding.headerLayout.rvNavigationMenu.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.headerLayout.rvNavigationMenu.adapter = adapter

        //val headerView = binding.navView.getHeaderView(0)
        //val tvLastLoginData: TextView = headerView.findViewById(R.id.tvLastLogin)
        //imgProfile = binding.headerLayout.findViewById(R.id.imgProfile)
        //imgProfile = binding.headerLayout.imgProfile
        //val tvUserName: TextView = headerView.findViewById(R.id.tvUserName)
        binding.headerLayout.tvUserName.text = appDao.getLoginData().userName+"\n"+appDao.getLoginData().lastLoginDateTime
        binding.headerLayout.tvVersion.text = BuildConfig.VERSION_NAME
        //tvLastLoginData.text = appDao.getLoginData().lastLoginDateTime
        ImageUtils().loadCircleIMageUrl(
            this,
            appDao.getAppRegistration().apiHostingServer + appDao.getLoginData().userPhoto,
            binding.headerLayout.imgProfile
        )

    }

    fun refreshProfileImage() {
        Log.e("TAG", "refreshProfileImage: ")
        setUpNavigationDrawer()
    }

    fun bottomHide() {
        binding.bottomNavigationView.visibility = View.GONE
    }

    fun bottomVisible() {
        binding.bottomNavigationView.visibility = View.VISIBLE
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_account_details -> {
                addFragment(
                    ProfileFragment(),
                    true,
                    false,
                    animationType = AnimationType.rightInLeftOut
                )
            }
            R.id.nav_about_us -> {
                addFragment(
                    DynamicPageContentFragment.newInstance(PAGE_ABOUT_US),
                    true,
                    true,
                    AnimationType.rightInLeftOut
                )
            }
            R.id.nav_terms_condition -> {
                addFragment(
                    DynamicPageContentFragment.newInstance(PAGE_TERMS_CONDITION),
                    true,
                    true,
                    AnimationType.rightInLeftOut
                )
            }
            R.id.nav_privacy_policy -> {
                addFragment(
                    DynamicPageContentFragment.newInstance(PAGE_PRIVACY_POLICY),
                    true,
                    true,
                    AnimationType.rightInLeftOut
                )
            }
            R.id.nav_change_pwd -> {
                addFragment(
                    ChangePwdFragment(),
                    true,
                    false,
                    animationType = AnimationType.rightInLeftOut
                )
            }
            R.id.nav_support -> {
                addFragment(SupportFragment(), true, true, AnimationType.rightInLeftOut)
            }
            R.id.nav_logout -> {
                callLogoutApi()
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun callLogoutApi() {
        appDao.deleteLogin()
        AppPreference.saveBooleanPreference(this, IS_LOGIN, false)
        val mServiceIntent = Intent(this, EthicsBackgroundService::class.java)
        if (isMyServiceRunning(EthicsBackgroundService::class.java)) {
            stopService(mServiceIntent)
        }
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    fun closeDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    fun enableDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }


    fun disableDrawer() {
        closeDrawer()
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    fun checkNavigationItem(position: Int) {
        binding.navView.menu.getItem(position).isChecked = true
    }

    fun checkBottomNavigationItem(position: Int) {
        binding.bottomNavigationView.menu.getItem(position).isChecked = true
    }

    private fun setBottomNavigationItem() {
        binding.bottomNavigationView.itemIconTintList = null
        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_attendance -> {
                    fragment = AttendanceFragment.newInstance(false)
                }
                R.id.nav_trip -> {
                    fragment = TripFragment()
                }
                R.id.nav_home -> {
                    fragment = DashboardFragment()
                }
                R.id.nav_reports -> {
                    fragment = ReportListFragment()
                }
                R.id.nav_more -> {
                    fragment = MoreListFragment()
                }

            }

            if (fragment != null) {
                displayFragment(fragment!!)
            }
            true
        }
    }

    private fun displayFragment(fragment: Fragment) {
        addFragment(fragment, true, true, AnimationType.rightInLeftOut)
    }

    fun isDashboardVisible(): Boolean {
        val currentFragment: Fragment? =
            supportFragmentManager.findFragmentById(R.id.frame_container)!!
        return currentFragment is DashboardFragment
    }

    override fun onBackPressed() {
        hideKeyboard()
        val currentFragment: Fragment? =
            supportFragmentManager.findFragmentById(R.id.frame_container)!!
        if (currentFragment is DashboardFragment || currentFragment is TripFragment || currentFragment is ReportListFragment
            || currentFragment is MoreListFragment
        ) {
            bottomVisible()
        } else {
            bottomHide()
        }

        val isFromAttendanceReport =
            AppPreference.getBooleanPreference(this, IS_FOR_ATTENDANCE_REPORT, false)

        if (currentFragment is AttendanceFragment) {
            if (!isFromAttendanceReport) {
                checkBottomNavigationItem(2)//Home
                addFragment(DashboardFragment(), false, true, AnimationType.fadeInfadeOut)
                bottomVisible()
            } else {
                bottomHide()
            }
        }
        if (currentFragment is TripFragment || currentFragment is ReportListFragment || currentFragment is MoreListFragment) {
            checkBottomNavigationItem(2)//Home
            addFragment(DashboardFragment(), false, true, AnimationType.fadeInfadeOut)
        }
        when (currentFragment) {
            is AddOrderEntryFragment -> {
                currentFragment.isExitFromAddOrder
                CommonMethods.showAlertDialog(this, getString(R.string.alert),
                    getString(R.string.are_you_sure_you_want_to_exit),
                    object :PositiveButtonListener{
                        override fun okClickListener() {
                            if (supportFragmentManager.backStackEntryCount > 0) {
                                supportFragmentManager.popBackStack() // Navigate back in the fragment stack
                            } else {
                                finish() // Close the activity if no fragments are in the back stack
                            }
                        }

                    }, true, getString(R.string.yes), getString(R.string.no))
            }

            is DashboardFragment -> {
                finish()
            }

            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun startLocationService() {

        LocalBroadcastManager.getInstance(this).registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val serviceLat = intent.getStringExtra(EXTRA_LATITUDE) ?: "0.01"
                    val serviceLng = intent.getStringExtra(EXTRA_LONGITUDE) ?: "0.01"
                    if (serviceLat == "0.01" && serviceLng == "0.01") {
                        CommonMethods.showToastMessage(
                            applicationContext,
                            getString(R.string.unable_to_fetch_location)
                        )
                    }
                }
            }, IntentFilter(EthicsBackgroundService().ACTION_LOCATION_BROADCAST)
        )
        val mServiceIntent = Intent(this, EthicsBackgroundService::class.java)

        if (!isMyServiceRunning(EthicsBackgroundService::class.java)) {
            Log.e("HOMEACTIVITY", "startLocationService: ")
            startService(mServiceIntent)
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }
    class NavMenuAdapter(
        private val menuList: List<NavMenuModel>,
        private val onItemClick: (NavMenuModel) -> Unit
    ) : RecyclerView.Adapter<NavMenuAdapter.MenuViewHolder>() {

        class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
            val tvMenuName: TextView = itemView.findViewById(R.id.tvMenuName)
            //val imgIndicator: ImageView = itemView.findViewById(R.id.imgIndicator)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_navigation_drawer, parent, false)
            return MenuViewHolder(view)
        }

        override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
            val menuItem = menuList[position]

            holder.imgIcon.setImageResource(menuItem.iconResId)
            holder.tvMenuName.text = menuItem.name

            // Show indicator if the item is expandable
            holder.itemView.setOnClickListener { onItemClick(menuItem) }
        }

        override fun getItemCount(): Int = menuList.size
    }


}