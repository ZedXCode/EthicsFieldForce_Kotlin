package ethicstechno.com.fieldforce.ui.fragments.dashboard

import AnimationType
import addFragment
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentDashboardBinding
import ethicstechno.com.fieldforce.databinding.ItemDashboardBinding
import ethicstechno.com.fieldforce.models.MoreOptionMenuListResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardDrillResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardListResponse
import ethicstechno.com.fieldforce.models.notification.NotificationCountResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.ExpenseListFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.leave.LeaveApplicationListFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.order_entry.OrderEntryListFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.quotation.QuotationEntryListFragment
import ethicstechno.com.fieldforce.utils.APPROVAL_MODULE
import ethicstechno.com.fieldforce.utils.APPROVAL_MODULE_PRINT
import ethicstechno.com.fieldforce.utils.APPROVAL_STRING
import ethicstechno.com.fieldforce.utils.ATTENDANCE_REPORT
import ethicstechno.com.fieldforce.utils.ATTENDANCE_REPORT_MODULE
import ethicstechno.com.fieldforce.utils.ATTENDANCE_REPORT_PRINT
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.showToastMessage
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.EXPENSE_APPROVAL_MODULE
import ethicstechno.com.fieldforce.utils.EXPENSE_APPROVAL_PRINT
import ethicstechno.com.fieldforce.utils.EXPENSE_ENTRY_MODULE
import ethicstechno.com.fieldforce.utils.EXPENSE_ENTRY_PRINT
import ethicstechno.com.fieldforce.utils.FORM_ID_EXPENSE_ENTRY_NUMBER
import ethicstechno.com.fieldforce.utils.FORM_ID_LEAVE_APPLICATION_ENTRY_NUMBER
import ethicstechno.com.fieldforce.utils.FORM_ID_ORDER_ENTRY_NUMBER
import ethicstechno.com.fieldforce.utils.FORM_ID_QUOTATION_ENTRY_NUMBER
import ethicstechno.com.fieldforce.utils.INQUIRY_MODULE
import ethicstechno.com.fieldforce.utils.INQUIRY_PRINT
import ethicstechno.com.fieldforce.utils.IS_DATA_UPDATE
import ethicstechno.com.fieldforce.utils.LEAVE_APPLICATION_MODULE
import ethicstechno.com.fieldforce.utils.LEAVE_APPLICATION_PRINT
import ethicstechno.com.fieldforce.utils.LEAVE_APPROVAL_MODULE
import ethicstechno.com.fieldforce.utils.LEAVE_APPROVAL_PRINT
import ethicstechno.com.fieldforce.utils.MENU_APPROVAL
import ethicstechno.com.fieldforce.utils.MENU_EXPENSE_APPROVAL
import ethicstechno.com.fieldforce.utils.MENU_EXPENSE_ENTRY
import ethicstechno.com.fieldforce.utils.MENU_INQUIRY
import ethicstechno.com.fieldforce.utils.MENU_LEAVE_APPLICATION
import ethicstechno.com.fieldforce.utils.MENU_LEAVE_APPROVAL
import ethicstechno.com.fieldforce.utils.MENU_ORDER_ENTRY
import ethicstechno.com.fieldforce.utils.MENU_PARTY_DEALER
import ethicstechno.com.fieldforce.utils.MENU_QUOTATION
import ethicstechno.com.fieldforce.utils.MENU_TOUR_PLAN
import ethicstechno.com.fieldforce.utils.MENU_VISIT
import ethicstechno.com.fieldforce.utils.ORDER_ENTRY_MODULE
import ethicstechno.com.fieldforce.utils.ORDER_ENTRY_PRINT
import ethicstechno.com.fieldforce.utils.PARTY_DEALER_MODULE
import ethicstechno.com.fieldforce.utils.PARTY_DEALER_PRINT
import ethicstechno.com.fieldforce.utils.QUOTATION_MODULE
import ethicstechno.com.fieldforce.utils.QUOTATION_PRINT
import ethicstechno.com.fieldforce.utils.TOUR_PLAN_MODULE
import ethicstechno.com.fieldforce.utils.TOUR_PLAN_PRINT
import ethicstechno.com.fieldforce.utils.TRIP_REPORT
import ethicstechno.com.fieldforce.utils.TRIP_REPORT_MODULE
import ethicstechno.com.fieldforce.utils.TRIP_REPORT_PRINT
import ethicstechno.com.fieldforce.utils.TRIP_SUMMERY_REPORT
import ethicstechno.com.fieldforce.utils.TRIP_SUMMERY_REPORT_MODULE
import ethicstechno.com.fieldforce.utils.TRIP_SUMMERY_REPORT_PRINT
import ethicstechno.com.fieldforce.utils.VISIT_MODULE
import ethicstechno.com.fieldforce.utils.VISIT_PRINT
import ethicstechno.com.fieldforce.utils.VISIT_REPORT
import ethicstechno.com.fieldforce.utils.VISIT_REPORT_MODULE
import ethicstechno.com.fieldforce.utils.VISIT_REPORT_MODULE_PRINT
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Locale

class DashboardFragment : HomeBaseFragment(), View.OnClickListener {

    lateinit var dashboardBinding: FragmentDashboardBinding
    var dashboardList: ArrayList<DashboardListResponse> = arrayListOf()

    var statusColor = ""
    var logFileDialog: AlertDialog? = null
    private var oAttachmentFilePath = ""
    private var strLogFilePath: String = ""
    private var strLogFileName: String = ""
    private var strLogDownloadUrl: String = ""
    private var strBackupDownloadUrl = ""
    private var gMailSubject = ""
    private var gMailBody = ""
    private var gMailSenderEmail = ""
    private var isNetworkLoss = false
    private var isBackupFileSend: Boolean = false
    private var isLogFileSend: Boolean = false
    private lateinit var tieMobile: TextInputEditText
    private lateinit var tieReason: TextInputEditText
    private var gMailRecipients: ArrayList<String> = arrayListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        return dashboardBinding.root
    }

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("TAG", "onCreate: ")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("TAG", "onViewCreated: ")
        initView()
    }

    private fun initView() {
        dashboardBinding.toolbar.imgMenu.visibility = View.VISIBLE
        dashboardBinding.toolbar.imgBack.visibility = View.GONE
        dashboardBinding.toolbar.tvHeader.text = "Welcome, ${appDao.getLoginData().userName}"
        dashboardBinding.toolbar.imgMenu.setOnClickListener(this)
        dashboardBinding.toolbar.rlNotification.setOnClickListener(this)
        dashboardBinding.toolbar.rlNotification.visibility = View.VISIBLE
        /*dashboardBinding.toolbar.imgLogFile.visibility = View.VISIBLE
        dashboardBinding.toolbar.imgLogFile.setOnClickListener {
            showLogFileDialog(mActivity.applicationContext)
        }*/

        callDashboardListApi()
        callMoreOptionList()
        callGetNotificationCountApi()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgMenu -> {
                mActivity.openDrawer()
            }

            R.id.rlNotification -> {
                mActivity.addFragment(
                    NotificationFragment(),
                    addToBackStack = true,
                    ignoreIfCurrent = true,
                    animationType = AnimationType.fadeInfadeOut
                )
            }
        }
    }

    private fun callDashboardListApi() {
        Log.e("TAG", "callDashboardListApi: ")
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val dashboardListReq = JsonObject()
        dashboardListReq.addProperty("UserId", loginData.userId)

        CommonMethods.writeLog("[" + this.javaClass.simpleName + "] IN \$callDashboardListApi()$ :: API REQUEST = " + dashboardListReq)

        val dashboardListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getDashboardList(dashboardListReq)


        dashboardListCall?.enqueue(object : Callback<List<DashboardListResponse>> {
            override fun onResponse(
                call: Call<List<DashboardListResponse>>,
                response: Response<List<DashboardListResponse>>
            ) {
                CommonMethods.hideLoading()
                CommonMethods.writeLog(
                    "[" + this.javaClass.simpleName + "] IN \$callDashboardListApi()$ :: API RESPONSE = " + Gson().toJson(
                        response.body()
                    )
                )
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            dashboardList.clear()
                            dashboardList.addAll(it)
                            setupDashboardAdapter()
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<List<DashboardListResponse>>, t: Throwable) {
                CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$callDashboardListApi()$ :: onFailure = " + t.message.toString())
                CommonMethods.hideLoading()
                if (mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null
                    )
                }
            }
        })

    }

    private fun callMoreOptionList() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)
        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val dashboardListReq = JsonObject()
        dashboardListReq.addProperty("UserId", loginData.userId)

        CommonMethods.writeLog("[" + this.javaClass.simpleName + "] IN \$callMoreOptionList()$ :: API REQUEST = " + dashboardListReq.toString())

        val dashboardListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getMoreOptionMenuList(dashboardListReq)

        dashboardListCall?.enqueue(object : Callback<List<MoreOptionMenuListResponse>> {
            override fun onResponse(
                call: Call<List<MoreOptionMenuListResponse>>,
                response: Response<List<MoreOptionMenuListResponse>>
            ) {
                CommonMethods.writeLog(
                    "[" + this.javaClass.simpleName + "] IN \$callMoreOptionList()$ :: API RESPONSE = " + Gson().toJson(
                        response.body()
                    )
                )
                if (response.isSuccessful) {
                    response.body()?.let { moreOptionMenuList ->

                        moreOptionMenuList.forEach { option ->
                            when (option.formName) {
                                MENU_PARTY_DEALER -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        PARTY_DEALER_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        PARTY_DEALER_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_VISIT -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        VISIT_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        VISIT_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_TOUR_PLAN -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        TOUR_PLAN_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        TOUR_PLAN_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_EXPENSE_ENTRY -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        EXPENSE_ENTRY_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        EXPENSE_ENTRY_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_EXPENSE_APPROVAL -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        EXPENSE_APPROVAL_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        EXPENSE_APPROVAL_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_LEAVE_APPLICATION -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        LEAVE_APPLICATION_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        LEAVE_APPLICATION_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_LEAVE_APPROVAL -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        LEAVE_APPROVAL_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        LEAVE_APPROVAL_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_INQUIRY -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        INQUIRY_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        INQUIRY_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_QUOTATION -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        QUOTATION_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        QUOTATION_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_ORDER_ENTRY -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        ORDER_ENTRY_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        ORDER_ENTRY_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_APPROVAL -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        APPROVAL_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        APPROVAL_MODULE_PRINT,
                                        option.allowPrint
                                    )
                                }

                                ATTENDANCE_REPORT -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        ATTENDANCE_REPORT_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        ATTENDANCE_REPORT_PRINT,
                                        option.allowPrint
                                    )
                                }

                                TRIP_REPORT -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        TRIP_REPORT_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        TRIP_REPORT_PRINT,
                                        option.allowPrint
                                    )
                                }

                                TRIP_SUMMERY_REPORT -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        TRIP_SUMMERY_REPORT_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        TRIP_SUMMERY_REPORT_PRINT,
                                        option.allowPrint
                                    )
                                }

                                VISIT_REPORT -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        VISIT_REPORT_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        VISIT_REPORT_MODULE_PRINT,
                                        option.allowPrint
                                    )
                                }
                            }
                        }
                    }

                }
                CommonMethods.hideLoading()
            }

            override fun onFailure(call: Call<List<MoreOptionMenuListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$callMoreOptionList()$ :: onFailure = " + t.message.toString())
                if (mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null
                    )
                }
            }
        })
    }

    private fun callGetNotificationCountApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)
        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val notificationCountReq = JsonObject()
        notificationCountReq.addProperty("UserId", loginData.userId)

        CommonMethods.writeLog("[" + this.javaClass.simpleName + "] IN \$callLoginApi()$ :: API REQUEST = " + notificationCountReq.toString())

        val notificationReqCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getNotificationCount(notificationCountReq)

        notificationReqCall?.enqueue(object : Callback<NotificationCountResponse> {
            override fun onResponse(
                call: Call<NotificationCountResponse>,
                response: Response<NotificationCountResponse>
            ) {
                CommonMethods.writeLog(
                    "[" + this.javaClass.simpleName + "] IN \$callGetNotificationCountApi()$ :: API RESPONSE = " + Gson().toJson(
                        response.body()
                    )
                )
                if (response.isSuccessful) {
                    response.body()?.let { notificationCount ->
                        if (notificationCount.unReadCount > 0) {
                            dashboardBinding.toolbar.cardNotificationCount.visibility = View.VISIBLE
                            dashboardBinding.toolbar.txtNotificationCount.text =
                                notificationCount.unReadCount.toString()
                            val topBottomPadding =
                                if (notificationCount.unReadCount.toString().length <= 1) 0 else 2
                            val leftRightPadding =
                                if (notificationCount.unReadCount.toString().length <= 1) 5 else 3
                            if (notificationCount.unReadCount.toString().length <= 1) {
                                dashboardBinding.toolbar.txtNotificationCount.setPadding(
                                    dpToPx(leftRightPadding),  // left
                                    dpToPx(topBottomPadding),  // top
                                    dpToPx(leftRightPadding),  // right
                                    dpToPx(topBottomPadding)   // bottom
                                )
                            }
                        } else {
                            dashboardBinding.toolbar.cardNotificationCount.visibility = View.GONE
                        }

                    }
                }
                CommonMethods.hideLoading()
            }

            override fun onFailure(call: Call<NotificationCountResponse>, t: Throwable) {
                CommonMethods.hideLoading()
                CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$callGetNotificationCountApi()$ :: onFailure = " + t.message.toString())
                if (mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null
                    )
                }
            }
        })
    }

    fun dpToPx(dp: Int): Int {
        return (dp * mActivity.resources.displayMetrics.density).toInt()
    }


    override fun onResume() {
        super.onResume()
        mActivity.bottomVisible()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        mActivity.bottomVisible()
        if (isAdded && !hidden && AppPreference.getBooleanPreference(
                mActivity,
                IS_DATA_UPDATE,
                false
            )
        ) {
            AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, false)
            dashboardBinding.rvDashboard.isEnabled = true
            callGetNotificationCountApi()
        }
    }

    private fun setupDashboardAdapter() {
        val dashBoardAdapter = DashboardAdapter(dashboardList)
        dashboardBinding.rvDashboard.adapter = dashBoardAdapter
        dashboardBinding.rvDashboard.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
    }

    private fun showLogFileDialog(context: Context) {
        try {
            if (logFileDialog != null && logFileDialog!!.isShowing) {
                return
            }
            val builder = AlertDialog.Builder(mActivity, R.style.MyAlertDialogStyle)
            logFileDialog = builder.create()
            logFileDialog!!.window!!.attributes.windowAnimations =
                R.style.TopRightRevealDialogAnimation
            logFileDialog!!.setCancelable(false)
            logFileDialog!!.setCanceledOnTouchOutside(false)
            logFileDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            logFileDialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val inflater = mActivity.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout: View = inflater.inflate(R.layout.dialog_log_file, null)
            val imgClose = layout.findViewById<View>(R.id.imgClose) as ImageView
            tieReason = layout.findViewById<TextInputEditText>(R.id.tieReason)
            tieMobile = layout.findViewById<TextInputEditText>(R.id.tieNumber)
            val btnSendLogFile = layout.findViewById<MaterialButton>(R.id.btnSendLogFile)
            btnSendLogFile.setOnClickListener(View.OnClickListener {
                if (tieReason.text.toString().isEmpty()) {
                    Toast.makeText(mActivity, "Please enter your reason", Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }
                if (tieMobile.text.toString().isEmpty()) {
                    Toast.makeText(mActivity, "Please enter your mobile number", Toast.LENGTH_SHORT)
                        .show()
                    return@OnClickListener
                }
                uploadBackUpFileToFireBaseStorage(mActivity.applicationContext)
                logFileDialog!!.dismiss()

            })
            imgClose.setOnClickListener { view: View? -> logFileDialog!!.dismiss() }
            logFileDialog!!.setView(layout, 0, 0, 0, 0)
            logFileDialog!!.window?.setBackgroundDrawableResource(R.drawable.dialog_shape)
            logFileDialog!!.show()
        } catch (e: Exception) {
            val exception =
                "[ScanMultipleInvoiceFragment] *ERROR* IN \$assetDetailDialog$ :: error = $e"
            CommonMethods.writeLog(exception)
        }
    }

    fun uploadBackUpFileToFireBaseStorage(context: Context) {
        try {
            val dir = context.cacheDir.toString()
            val s = CommonMethods.getCurrentDate()
            val fileNameFormat = s
            val strLogFilePath = "$dir/Log_$fileNameFormat.txt"
            val strLogFileName =
                "EMP_ID_${loginData.userName}_${strLogFilePath.substring(strLogFilePath.indexOf("Log_"))}"

            var isBackupFileSend = false
            var isLogFileSend = false
            var isNetworkLoss = false

            if (!ConnectionUtil.isInternetAvailable(mActivity)) {
                showToastMessage(mActivity, getString(R.string.no_internet))
                return
            }

            Log.e("TAG", "uploadBackUpFileToFireBaseStorage: INTERNET IS WORKING GO AHEAD")

            val storage = FirebaseStorage.getInstance().reference
            val logRef = storage.child("backupfile")

            val inFileName = "/data/data/${context.packageName}/databases/FieldForce"
            val dbFile = File(inFileName)
            val fis = FileInputStream(dbFile)
            val outFileName = "${context.cacheDir}/FieldForce"
            val desFile = File(outFileName)

            if (desFile.exists()) {
                desFile.delete()
            }

            val output: OutputStream = FileOutputStream(outFileName)
            val buffer = ByteArray(1024)
            var length: Int

            while (fis.read(buffer).also { length = it } > 0) {
                output.write(buffer, 0, length)
            }

            output.flush()
            output.close()
            fis.close()

            val inputStream = FileInputStream(File(outFileName))
            val fileRef = logRef.child(strLogFileName.replace("txt", "db"))

            fileRef.putStream(inputStream)
                .addOnSuccessListener { taskSnapshot ->
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        strBackupDownloadUrl = uri.toString()
                        isBackupFileSend = true
                        uploadLogFileToFireBaseStorage(context)
                    }
                }
                .addOnFailureListener { e ->
                    isBackupFileSend = false
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    CommonMethods.hideLoading()
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress =
                        (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                    // Optional: Log progress if needed
                }

            val handler = Handler(Looper.getMainLooper())
            var count = 0
            val runnable = object : Runnable {
                override fun run() {
                    count++
                    if (!ConnectionUtil.isInternetAvailable(mActivity)) {
                        showToastMessage(mActivity, getString(R.string.no_internet))
                        return
                    }

                    if (isBackupFileSend) {
                        isNetworkLoss = false
                        handler.removeCallbacks(this)
                        return
                    }
                    handler.postDelayed(this, 1000)
                }
            }

            handler.postDelayed(runnable, 1000)

        } catch (e: Exception) {
            Log.e("TAG", "uploadBackUpFileToFireBaseStorage: ${e.message}")
            CommonMethods.hideLoading()
            //PubFun.writeLog("[FragmentMyProfile] *ERROR* IN \$uploadLogFileToFireBaseStorage\$ :: error = ${e}")
        }
    }

    fun uploadLogFileToFireBaseStorage(context: Context) {
        try {
            var storageReference = FirebaseStorage.getInstance().reference
            val logRef = storageReference.child("logs")

            val inputStream = FileInputStream(File(strLogFilePath))
            val fileRef = logRef.child(strLogFileName)

            fileRef.putStream(inputStream)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        strLogDownloadUrl = uri.toString()
                        isLogFileSend = true
//                    sendGMailToSupportTeam() // Uncomment if needed
                        getEmailAddressFromServerSettings()
                    }
                }
                .addOnFailureListener { e ->
                    isLogFileSend = false
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    CommonMethods.hideLoading()
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress =
                        (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                    // Optional: Log or use progress
                }

            val handler = Handler(Looper.getMainLooper())
            var count = 0
            val runnable = object : Runnable {
                override fun run() {
                    if (!ConnectionUtil.isInternetAvailable(mActivity)) {
                        showToastMessage(mActivity, getString(R.string.no_internet))
                        return
                    }

                    if (isLogFileSend) {
                        isNetworkLoss = false
                        handler.removeCallbacks(this)
                        return
                    }

                    handler.postDelayed(this, 1000)
                }
            }

            handler.postDelayed(runnable, 1000)

        } catch (e: Exception) {
            CommonMethods.hideLoading()
            //PubFun.writeLog("[FragmentMyProfile] *ERROR* IN \$uploadLogFileToFireBaseStorage\$ :: error = ${e}")
        }
    }

    fun getEmailAddressFromServerSettings() {
        try {
            val strSetKey = "logemail" // Hardcoded as guided by Pratik Thummar
            gMailSenderEmail =
                "parth.khatsuriya@vc-erp.com, vishal.bhadani@vc-erp.com, mobileapps@vc-erp.com, mayur.patel@vc-erp.com"

            fireEmailIntent()
        } catch (e: Exception) {
            //PubFun.writeLog("[UserDetailFragment] *ERROR* IN \$getEmailAddressFromServerSettings\$ :: error = ${e}")
        }
    }

    private fun fireEmailIntent() {
        try {
            if (!isNetworkLoss) {
                oAttachmentFilePath = strLogFilePath
                val file = File(oAttachmentFilePath)
                if (file.exists()) {

                    //1. Subject
                    gMailSubject =
                        resources.getString(R.string.app_name) + " Log file from " + loginData.userName+ "( emmp id  =  " + loginData.userId + ")  for Investigation (" + CommonMethods.getCurrentDateTime() + ")"
                    val strReason: String = tieReason.text.toString().trim { it <= ' ' }

                    //2. Body
                    gMailBody = """Dear Support team,
Problem I face today, here is my explanation: $strReason

Also here is a log file content for your quick investigation with
My mobile no: ${tieMobile.text.toString().trim { it <= ' ' }}

Log file name: $strLogFileName - $strLogDownloadUrl
DB file name: ${strLogFileName.replace("txt", "db")} - $strBackupDownloadUrl"""

                    //3.Receiver
                    /*gMailRecipients = new String[]{"krunal.prajapati@vc-erp.com", "milan.sheth@vc-erp.com", "anand.patel@vc-erp.com," +
                            " mayur.patel@vc-erp.com, dmssupport@vadilalgroup.com", "abhijit@vadilalgroup.com"};*/
                    gMailRecipients = arrayListOf(gMailSenderEmail)
                    val emailIntent = Intent(Intent.ACTION_SEND)
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, gMailRecipients)
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, gMailSubject)
                    emailIntent.type = "text/plain"
                    emailIntent.putExtra(Intent.EXTRA_TEXT, gMailBody)
                    val pm: PackageManager = mActivity.packageManager
                    val matches = pm.queryIntentActivities(emailIntent, 0)
                    var best: ResolveInfo? = null
                    for (info in matches) {
                        if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.lowercase(
                                Locale.getDefault()
                            ).contains("gmail")
                        ) {
                            best = info
                        }
                    }
                    if (best != null) {
                        emailIntent.setClassName(
                            best.activityInfo.packageName,
                            best.activityInfo.name
                        )
                    }
                    startActivity(emailIntent)
                    CommonMethods.hideLoading()
                    //goNext(AppConstants.VIEW_DASHBOARD, null, "")
                } else {
                    //PubFun.writeLog("[FragmentMyProfile] *MSG* IN \$sendGmailToSupportTeam$ :: attachment file not found at path = $oAttachmentFilePath")
                    CommonMethods.hideLoading()
                    Toast.makeText(
                        mActivity,
                        "Mail Sending Failed!, Oops! some how log file was not created / found in your device. please try again. If this issue is still persisting then please contact administrator",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: java.lang.Exception) {
            CommonMethods.hideLoading()
            //PubFun.writeLog("[FragmentMyProfile] *ERROR* IN \$sendGMailToSupportTeam$ :: error = $e")
        }
    }


    inner class DashboardAdapter(
        private val dashboardList: ArrayList<DashboardListResponse>
    ) : RecyclerView.Adapter<DashboardAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemDashboardBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return dashboardList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val dashboardData = dashboardList[position]
            holder.bind(dashboardData)
        }

        inner class ViewHolder(private val binding: ItemDashboardBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(dashboardData: DashboardListResponse) {
                Log.d("VIEWTYPE===>", "" + dashboardData.viewType)
                // Log.d("VIEWCOLOR===>",""+dashboardData.color4)

                dashboardList.forEach {
                    (dashboardData)
                    Log.d("LISTDATA===>", "" + dashboardList.size)
                    if (dashboardData.viewType == 1) {//For New Design
                        Log.d("VIEW===>", "view1")
                        binding.liView1.visibility = View.VISIBLE
                        binding.liView2.visibility = View.GONE
                        binding.liView3.visibility = View.GONE
                        binding.liView4.visibility = View.GONE
                        //binding.llMain.visibility = View.GONE

                        binding.tvValue2.text = dashboardData.value2
                        binding.tvTitleLine1.text = dashboardData.titleLine1
                        binding.tvValue1.text = dashboardData.value1

                        // statusColor = dashboardData.color4

                        val colors = dashboardData.color4.split("|")
                        if (colors[1].isEmpty()) {
                            binding.liView1.setBackgroundColor(Color.parseColor("#FFBCC8"))
                        } else {
                            binding.liView1.setBackgroundColor(Color.parseColor(colors[1]))
                            Log.d("VIEWCOLOR2===>", "" + dashboardData.color4)
                        }

                        val colors1 = dashboardData.color1.split("|")
                        if (colors1[0].isEmpty()) {
                            binding.tvValue2.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvValue2.setTextColor(Color.parseColor(colors1[0]))
                        }

                        val colors2 = dashboardData.color2.split("|")
                        if (colors2[0].isEmpty()) {
                            binding.tvValue1.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvValue1.setTextColor(Color.parseColor(colors2[0]))
                        }

                    } else if (dashboardData.viewType == 2) {//OLD View
                        Log.d("VIEW===>", "view2")
                        binding.liView1.visibility = View.GONE
                        binding.liView2.visibility = View.VISIBLE
                        binding.liView3.visibility = View.GONE
                        binding.liView4.visibility = View.GONE
                        //binding.llMain.visibility = View.GONE

                        binding.tvTitleLine2.text = dashboardData.reportName
                        binding.tvView2Value1.text = dashboardData.value1
                        binding.tvView2TitleLine1.text = dashboardData.titleLine1
                        binding.tvView2Value2.text = dashboardData.value2
                        binding.tvView2TitleLine2.text = dashboardData.titleLine2
                        binding.tvView2Value3.text = dashboardData.value3
                        binding.tvView2Value4.text = dashboardData.value4
                        binding.tvView2TitleLine3.text = dashboardData.titleLine3
                        binding.tvView2TitleLine4.text = dashboardData.titleLine4

                        val colors4 = dashboardData.color4.split("|")
                        if (colors4[1].isEmpty()) {
                            binding.liView2.setBackgroundColor(Color.parseColor("#FFBCC8"))
                        } else {
                            binding.liView2.setBackgroundColor(Color.parseColor(colors4[1]))
                            Log.d("VIEWCOLOR2===>", "" + dashboardData.color4)
                        }
                        if (colors4[0].isEmpty()) {
                            binding.tvView2Value4.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView2Value4.setTextColor(Color.parseColor(colors4[0]))
                        }

                        val colors1 = dashboardData.color1.split("|")
                        if (colors1[0].isEmpty()) {
                            binding.tvView2Value1.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView2Value1.setTextColor(Color.parseColor(colors1[0]))
                        }

                        val colors2 = dashboardData.color2.split("|")
                        if (colors2[0].isEmpty()) {
                            binding.tvView2Value2.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView2Value2.setTextColor(Color.parseColor(colors2[0]))
                        }

                        val colors3 = dashboardData.color3.split("|")
                        if (colors3[0].isEmpty()) {
                            binding.tvView2Value3.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView2Value3.setTextColor(Color.parseColor(colors3[0]))
                        }
                    } else if (dashboardData.viewType == 3) {
                        Log.d("VIEW===>", "view3")
                        binding.liView1.visibility = View.GONE
                        binding.liView2.visibility = View.GONE
                        binding.liView3.visibility = View.VISIBLE
                        binding.liView4.visibility = View.GONE
                        //binding.llMain.visibility = View.GONE

                        binding.tvTitleLine3.text = dashboardData.reportName
                        binding.tvView3TitleLine1.text = dashboardData.titleLine1
                        binding.tvView3Value1.text = dashboardData.value1
                        binding.tvView3TitleLine2.text = dashboardData.titleLine2
                        binding.tvView3Value2.text = dashboardData.value2
                        binding.tvView3TitleLine3.text = dashboardData.titleLine3
                        binding.tvView3Value3.text = dashboardData.value3
                        binding.tvView3TitleLine4.text = dashboardData.titleLine4
                        binding.tvView3Value4.text = dashboardData.value4


                        val color1 = dashboardData.color1.split("|")
                        if (color1[0].isEmpty()) {
                            binding.tvView3TitleLine1.setTextColor(Color.parseColor("#FF000000"))
                            binding.tvView3Value1.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView3Value1.setTextColor(Color.parseColor(color1[0]))
                            binding.tvView3TitleLine1.setTextColor(Color.parseColor(color1[0]))
                        }

                        if (color1[1].isEmpty()) {
                            binding.lvView3layout1.setBackgroundColor(Color.parseColor("#FFBCC8"))
                        } else {
                            binding.lvView3layout1.setBackgroundColor(Color.parseColor(color1[1]))
                        }

                        val color2 = dashboardData.color2.split("|")
                        if (color2[0].isEmpty()) {
                            binding.tvView3TitleLine2.setTextColor(Color.parseColor("#FF000000"))
                            binding.tvView3Value2.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView3Value2.setTextColor(Color.parseColor(color2[0]))
                            binding.tvView3TitleLine2.setTextColor(Color.parseColor(color2[0]))
                        }

                        if (color2[1].isEmpty()) {
                            binding.lvView3layout2.setBackgroundColor(Color.parseColor("#FFFFD8"))
                        } else {
                            binding.lvView3layout2.setBackgroundColor(Color.parseColor(color2[1]))
                        }

                        val color3 = dashboardData.color3.split("|")
                        if (color3[0].isEmpty()) {
                            binding.tvView3TitleLine3.setTextColor(Color.parseColor("#FF000000"))
                            binding.tvView3Value3.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView3Value3.setTextColor(Color.parseColor(color3[0]))
                            binding.tvView3TitleLine3.setTextColor(Color.parseColor(color3[0]))
                        }

                        if (color3[1].isEmpty()) {
                            binding.lvView3layout3.setBackgroundColor(Color.parseColor("#EAEBFF"))
                        } else {
                            binding.lvView3layout3.setBackgroundColor(Color.parseColor(color3[1]))
                        }

                        val colors4 = dashboardData.color4.split("|")
                        if (colors4[0].isEmpty()) {
                            binding.tvView3TitleLine4.setTextColor(Color.parseColor("#FF000000"))
                            binding.tvView3Value4.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView3Value4.setTextColor(Color.parseColor(colors4[0]))
                            binding.tvView3TitleLine4.setTextColor(Color.parseColor(colors4[0]))
                        }

                        if (colors4[1].isEmpty()) {
                            binding.lvView3layout4.setBackgroundColor(Color.parseColor("#E0FEFE"))
                        } else {
                            binding.lvView3layout4.setBackgroundColor(Color.parseColor(colors4[1]))
                        }
//                        if (colors4[1].isEmpty()){
//                            binding.liView3.setBackgroundColor(Color.parseColor("#FFBCC8"))
//                        }else{
//                            binding.liView3.setBackgroundColor(Color.parseColor(colors4[1]))
//                            Log.d("VIEWCOLOR2===>",""+dashboardData.color4)
//                        }

                        if (dashboardData.titleLine1.isEmpty()) {
                            binding.tvView4TitleLine1.visibility = View.GONE
                        } else if (dashboardData.value1.isEmpty()) {
                            binding.tvView4Value1.visibility = View.GONE
                        } else if (dashboardData.titleLine2.isEmpty()) {
                            binding.tvView4TitleLine2.visibility = View.GONE
                        } else if (dashboardData.value2.isEmpty()) {
                            binding.tvView4Value1.visibility = View.GONE
                        } else if (dashboardData.titleLine3.isEmpty()) {
                            binding.tvView4TitleLine3.visibility = View.GONE
                        } else if (dashboardData.value3.isEmpty()) {
                            binding.tvView4Value3.visibility = View.GONE
                        } else if (dashboardData.titleLine4.isEmpty()) {
                            binding.tvView4TitleLine4.visibility = View.GONE
                        } else if (dashboardData.value4.isEmpty()) {
                            binding.tvView4Value4.visibility = View.GONE
                        }

                        if (dashboardData.titleLine1.isEmpty() && dashboardData.value1.isEmpty()) {
                            binding.lvView3layout1.visibility = View.GONE
                        } else if (dashboardData.titleLine2.isEmpty() && dashboardData.value2.isEmpty()) {
                            binding.lvView3layout2.visibility = View.GONE
                        } else if (dashboardData.titleLine4.isEmpty() && dashboardData.value4.isEmpty()) {
                            binding.lvView3layout4.visibility = View.GONE
                        }


                        if (dashboardData.titleLine3.isEmpty() && dashboardData.value3.isEmpty()) {
                            binding.lvView3layout3.visibility = View.GONE
                        }
                    } else if (dashboardData.viewType == 4) {
                        Log.d("VIEW===>", "view4")
                        binding.liView1.visibility = View.GONE
                        binding.liView2.visibility = View.GONE
                        binding.liView3.visibility = View.GONE
                        binding.liView4.visibility = View.VISIBLE
                        //binding.llMain.visibility = View.GONE

                        binding.tvTitleLine4.text = dashboardData.reportName
                        binding.tvView4TitleLine1.text = dashboardData.titleLine1
                        binding.tvView4Value1.text = dashboardData.value1
                        binding.tvView4TitleLine2.text = dashboardData.titleLine2
                        binding.tvView4Value2.text = dashboardData.value2
                        binding.tvView4TitleLine3.text = dashboardData.titleLine3
                        binding.tvView4Value3.text = dashboardData.value3
                        binding.tvView4TitleLine4.text = dashboardData.titleLine4
                        binding.tvView4Value4.text = dashboardData.value4

                        val colors1 = dashboardData.color1.split("|")
                        if (colors1[0].isEmpty()) {
                            binding.tvView4Value1.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView4Value1.setTextColor(Color.parseColor(colors1[0]))
                        }

                        if (colors1[1].isEmpty()) {
                            binding.tvView4TitleLine1.setBackgroundColor(Color.parseColor("#FFBCC8"))
                            binding.tvView4Value1.setBackgroundColor(Color.parseColor("#FFBCC8"))

                        } else {
                            binding.tvView4TitleLine1.setBackgroundColor(Color.parseColor(colors1[1]))
                            binding.tvView4Value1.setBackgroundColor(Color.parseColor(colors1[1]))
                        }


                        val colors2 = dashboardData.color2.split("|")
                        if (colors2[0].isEmpty()) {
                            binding.tvView4Value2.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView4Value2.setTextColor(Color.parseColor(colors2[0]))
                        }

                        if (colors2[1].isEmpty()) {
                            binding.tvView4TitleLine2.setBackgroundColor(Color.parseColor("#FFFFD8"))
                            binding.tvView4Value2.setBackgroundColor(Color.parseColor("#FFFFD8"))

                        } else {
                            binding.tvView4TitleLine2.setBackgroundColor(Color.parseColor(colors2[1]))
                            binding.tvView4Value2.setBackgroundColor(Color.parseColor(colors2[1]))
                        }

                        val colors3 = dashboardData.color3.split("|")
                        if (colors3[0].isEmpty()) {
                            binding.tvView4Value3.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView4Value3.setTextColor(Color.parseColor(colors3[0]))
                        }

                        if (colors3[1].isEmpty()) {
                            binding.tvView4TitleLine3.setBackgroundColor(Color.parseColor("#EAEBFF"))
                            binding.tvView4Value3.setBackgroundColor(Color.parseColor("#EAEBFF"))

                        } else {
                            binding.tvView4TitleLine3.setBackgroundColor(Color.parseColor(colors3[1]))
                            binding.tvView4Value3.setBackgroundColor(Color.parseColor(colors3[1]))
                        }

                        val colors4 = dashboardData.color4.split("|")
                        if (colors4[0].isEmpty()) {
                            binding.tvView4Value4.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView4Value4.setTextColor(Color.parseColor(colors4[0]))
                        }

                        if (colors4[1].isEmpty()) {
                            binding.tvView4TitleLine4.setBackgroundColor(Color.parseColor("#E0FEFE"))
                            binding.tvView4Value4.setBackgroundColor(Color.parseColor("#E0FEFE"))

                        } else {
                            binding.tvView4TitleLine4.setBackgroundColor(Color.parseColor(colors4[1]))
                            binding.tvView4Value4.setBackgroundColor(Color.parseColor(colors4[1]))
                        }

//                        if (colors4[1].isEmpty()){
//                            binding.liView4.setBackgroundColor(Color.parseColor("#FFBCC8"))
//                        }else{
//                            binding.liView4.setBackgroundColor(Color.parseColor(colors4[1]))
//                            Log.d("VIEWCOLOR2===>",""+dashboardData.color4)
//                        }

                        if (dashboardData.titleLine1.isEmpty()) {
                            binding.tvView4TitleLine1.visibility = View.GONE
                        } else if (dashboardData.value1.isEmpty()) {
                            binding.tvView4Value1.visibility = View.GONE
                        } else if (dashboardData.titleLine2.isEmpty()) {
                            binding.tvView4TitleLine2.visibility = View.GONE
                        } else if (dashboardData.value2.isEmpty()) {
                            binding.tvView4Value1.visibility = View.GONE
                        } else if (dashboardData.titleLine3.isEmpty()) {
                            binding.tvView4TitleLine3.visibility = View.GONE
                        } else if (dashboardData.value3.isEmpty()) {
                            binding.tvView4Value3.visibility = View.GONE
                        } else if (dashboardData.titleLine4.isEmpty()) {
                            binding.tvView4TitleLine4.visibility = View.GONE
                        } else if (dashboardData.value4.isEmpty()) {
                            binding.tvView4Value4.visibility = View.GONE
                        }
                    } else {
                        Log.d("VIEW===>", "view4")
                        binding.liView1.visibility = View.GONE
                        binding.liView2.visibility = View.GONE
                        binding.liView3.visibility = View.GONE
                        binding.liView4.visibility = View.VISIBLE

                        binding.tvView4Value1.visibility = View.GONE
                        binding.tvView4Value2.visibility = View.GONE
                        binding.tvView4Value3.visibility = View.GONE
                        binding.tvView4Value4.visibility = View.GONE
                        //binding.llMain.visibility = View.GONE

                        binding.tvTitleLine4.text = dashboardData.reportName
                        binding.tvView4TitleLine1.text = dashboardData.titleLine1
                        binding.tvView4Value1.text = dashboardData.value1
                        binding.tvView4TitleLine2.text = dashboardData.titleLine2
                        binding.tvView4Value2.text = dashboardData.value2
                        binding.tvView4TitleLine3.text = dashboardData.titleLine3
                        binding.tvView4Value3.text = dashboardData.value3
                        binding.tvView4TitleLine4.text = dashboardData.titleLine4
                        binding.tvView4Value4.text = dashboardData.value4

                        val colors1 = dashboardData.color1.split("|")
                        if (colors1[0].isEmpty()) {
                            binding.tvView4Value1.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView4Value1.setTextColor(Color.parseColor(colors1[0]))
                        }

                        if (colors1[1].isEmpty()) {
                            binding.tvView4TitleLine1.setBackgroundColor(Color.parseColor("#FFBCC8"))
                            binding.tvView4Value1.setBackgroundColor(Color.parseColor("#FFBCC8"))

                        } else {
                            binding.tvView4TitleLine1.setBackgroundColor(Color.parseColor(colors1[1]))
                            binding.tvView4Value1.setBackgroundColor(Color.parseColor(colors1[1]))
                        }


                        val colors2 = dashboardData.color2.split("|")
                        if (colors2[0].isEmpty()) {
                            binding.tvView4Value2.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView4Value2.setTextColor(Color.parseColor(colors2[0]))
                        }

                        if (colors2[1].isEmpty()) {
                            binding.tvView4TitleLine2.setBackgroundColor(Color.parseColor("#FFFFD8"))
                            binding.tvView4Value2.setBackgroundColor(Color.parseColor("#FFFFD8"))

                        } else {
                            binding.tvView4TitleLine2.setBackgroundColor(Color.parseColor(colors2[1]))
                            binding.tvView4Value2.setBackgroundColor(Color.parseColor(colors2[1]))
                        }

                        val colors3 = dashboardData.color3.split("|")
                        if (colors3[0].isEmpty()) {
                            binding.tvView4Value3.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView4Value3.setTextColor(Color.parseColor(colors3[0]))
                        }

                        if (colors3[1].isEmpty()) {
                            binding.tvView4TitleLine3.setBackgroundColor(Color.parseColor("#EAEBFF"))
                            binding.tvView4Value3.setBackgroundColor(Color.parseColor("#EAEBFF"))

                        } else {
                            binding.tvView4TitleLine3.setBackgroundColor(Color.parseColor(colors3[1]))
                            binding.tvView4Value3.setBackgroundColor(Color.parseColor(colors3[1]))
                        }

                        val colors4 = dashboardData.color4.split("|")
                        if (colors4[0].isEmpty()) {
                            binding.tvView4Value4.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView4Value4.setTextColor(Color.parseColor(colors4[0]))
                        }

                        if (colors4[1].isEmpty()) {
                            binding.tvView4TitleLine4.setBackgroundColor(Color.parseColor("#E0FEFE"))
                            binding.tvView4Value4.setBackgroundColor(Color.parseColor("#E0FEFE"))

                        } else {
                            binding.tvView4TitleLine4.setBackgroundColor(Color.parseColor(colors4[1]))
                            binding.tvView4Value4.setBackgroundColor(Color.parseColor(colors4[1]))
                        }

//                        if (colors4[1].isEmpty()){
//                            binding.liView4.setBackgroundColor(Color.parseColor("#FFBCC8"))
//                        }else{
//                            binding.liView4.setBackgroundColor(Color.parseColor(colors4[1]))
//                            Log.d("VIEWCOLOR2===>",""+dashboardData.color4)
//                        }

                        if (dashboardData.titleLine1.isEmpty()) {
                            binding.tvView4TitleLine1.visibility = View.GONE
                        } else if (dashboardData.value1.isEmpty()) {
                            binding.tvView4Value1.visibility = View.GONE
                        } else if (dashboardData.titleLine2.isEmpty()) {
                            binding.tvView4TitleLine2.visibility = View.GONE
                        } else if (dashboardData.value2.isEmpty()) {
                            binding.tvView4Value1.visibility = View.GONE
                        } else if (dashboardData.titleLine3.isEmpty()) {
                            binding.tvView4TitleLine3.visibility = View.GONE
                        } else if (dashboardData.value3.isEmpty()) {
                            binding.tvView4Value3.visibility = View.GONE
                        } else if (dashboardData.titleLine4.isEmpty()) {
                            binding.tvView4TitleLine4.visibility = View.GONE
                        } else if (dashboardData.value4.isEmpty()) {
                            binding.tvView4Value4.visibility = View.GONE
                        }
                    }

                }

//                if (dashboardData.viewType == 1)
//                {
//                    Log.d("VIEW===>","view1")
//                    binding.liView1.visibility = View.VISIBLE
//                    binding.liView2.visibility = View.GONE
//                    binding.liView3.visibility = View.GONE
//                    binding.liView4.visibility = View.GONE
//                    //binding.llMain.visibility = View.GONE
//
//                    binding.tvValue2.text = dashboardData.value2
//                    binding.tvTitleLine1.text = dashboardData.titleLine1
//                    binding.tvValue1.text = dashboardData.value1
//
//                    if (dashboardData.color4.isNotEmpty()){
//                        binding.tvValue2.setTextColor(Color.parseColor(dashboardData.color4))
//                    }else{
//                        binding.tvValue2.setTextColor(Color.parseColor("#FF000000"))
//                    }
//
//                    if (dashboardData.color1.isEmpty()){
//                        binding.tvValue2.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvValue2.setTextColor(Color.parseColor(dashboardData.color1))
//                    }
//
//                    if (dashboardData.color2.isEmpty()){
//                        binding.tvValue1.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvValue1.setTextColor(Color.parseColor(dashboardData.color2))
//                    }
//
//                }
//                else if (dashboardData.viewType == 2){
//                    Log.d("VIEW===>","view2")
//                    binding.liView1.visibility = View.GONE
//                    binding.liView2.visibility = View.VISIBLE
//                    binding.liView3.visibility = View.GONE
//                    binding.liView4.visibility = View.GONE
//                    //binding.llMain.visibility = View.GONE
//
//                    binding.tvTitleLine2.text = dashboardData.reportName
//                    binding.tvView2Value1.text = dashboardData.value1
//                    binding.tvView2TitleLine1.text = dashboardData.titleLine1
//                    binding.tvView2Value2.text = dashboardData.value2
//                    binding.tvView2TitleLine2.text = dashboardData.titleLine2
//                    binding.tvView2Value3.text = dashboardData.value3
//                    binding.tvView2Value4.text = dashboardData.value4
//                    binding.tvView2TitleLine3.text = dashboardData.titleLine3
//                    binding.tvView2TitleLine4.text = dashboardData.titleLine4
//
//                    if (dashboardData.color1.isEmpty()){
//                        binding.tvView2Value1.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView2Value1.setTextColor(Color.parseColor(dashboardData.color1))
//                    }
//
//                    if (dashboardData.color2.isEmpty()){
//                        binding.tvView2Value2.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView2Value2.setTextColor(Color.parseColor(dashboardData.color2))
//                    }
//
//                    if (dashboardData.color3.isEmpty()){
//                        binding.tvView2Value3.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView2Value3.setTextColor(Color.parseColor(dashboardData.color3))
//                    }
//
//                    if (dashboardData.color4.isEmpty()){
//                        binding.tvView2TitleLine4.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView2TitleLine4.setTextColor(Color.parseColor(dashboardData.color4))
//                    }
//                }
//                else if (dashboardData.viewType == 3){
//                    Log.d("VIEW===>","view3")
//                    binding.liView1.visibility = View.GONE
//                    binding.liView2.visibility = View.GONE
//                    binding.liView3.visibility = View.VISIBLE
//                    binding.liView4.visibility = View.GONE
//                    //binding.llMain.visibility = View.GONE
//
//                    binding.tvTitleLine3.text = dashboardData.reportName
//                    binding.tvView3TitleLine1.text = dashboardData.titleLine1
//                    binding.tvView3Value1.text = dashboardData.value1
//                    binding.tvView3TitleLine2.text = dashboardData.titleLine2
//                    binding.tvView3Value2.text = dashboardData.value2
//                    binding.tvView3TitleLine3.text = dashboardData.titleLine3
//                    binding.tvView3Value3.text = dashboardData.value3
//                    binding.tvView3TitleLine4.text = dashboardData.titleLine4
//                    binding.tvView3Value4.text = dashboardData.value4
//
//                    if (dashboardData.color1.isEmpty()){
//                        binding.tvView3Value1.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView3Value1.setTextColor(Color.parseColor(dashboardData.color1))
//                    }
//
//                    if (dashboardData.color2.isEmpty()){
//                        binding.tvView3Value2.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView3Value2.setTextColor(Color.parseColor(dashboardData.color2))
//                    }
//
//                    if (dashboardData.color3.isEmpty()){
//                        binding.tvView3Value3.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView3Value3.setTextColor(Color.parseColor(dashboardData.color3))
//                    }
//
//                    if (dashboardData.color4.isEmpty()){
//                        binding.tvView3Value4.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView3Value4.setTextColor(Color.parseColor(dashboardData.color4))
//                    }
//                }
//                else {
//                    Log.d("VIEW===>","view4")
//                    binding.liView1.visibility = View.GONE
//                    binding.liView2.visibility = View.GONE
//                    binding.liView3.visibility = View.GONE
//                    binding.liView4.visibility = View.VISIBLE
//                    //binding.llMain.visibility = View.GONE
//
//                    binding.tvTitleLine4.text = dashboardData.reportName
//                    binding.tvView4TitleLine1.text = dashboardData.titleLine1
//                    binding.tvView4Value1.text = dashboardData.value1
//                    binding.tvView4TitleLine2.text = dashboardData.titleLine2
//                    binding.tvView4Value2.text = dashboardData.value2
//                    binding.tvView4TitleLine3.text = dashboardData.titleLine3
//                    binding.tvView4Value3.text = dashboardData.value3
//                    binding.tvView4TitleLine4.text = dashboardData.titleLine4
//                    binding.tvView4Value4.text = dashboardData.value4
//
//                    if (dashboardData.color1.isEmpty()){
//                        binding.tvView4Value1.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView4Value1.setTextColor(Color.parseColor(dashboardData.color1))
//                    }
//
//                    if (dashboardData.color2.isEmpty()){
//                        binding.tvView4Value2.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView4Value2.setTextColor(Color.parseColor(dashboardData.color2))
//                    }
//
//                    if (dashboardData.color3.isEmpty()){
//                        binding.tvView4Value3.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView4Value3.setTextColor(Color.parseColor(dashboardData.color3))
//                    }
//
//                    if (dashboardData.color4.isEmpty()){
//                        binding.tvView4Value4.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView4Value4.setTextColor(Color.parseColor(dashboardData.color4))
//                    }
//
//                    if (dashboardData.titleLine1.isEmpty()){
//                        binding.tvView4TitleLine1.visibility = View.GONE
//                    }
//                    else if (dashboardData.value1.isEmpty()){
//                        binding.tvView4Value1.visibility = View.GONE
//                    }
//                    else if (dashboardData.titleLine2.isEmpty()){
//                        binding.tvView4TitleLine2.visibility = View.GONE
//                    }
//                    else if (dashboardData.value2.isEmpty()){
//                        binding.tvView4Value1.visibility = View.GONE
//                    }
//                    else if (dashboardData.titleLine3.isEmpty()){
//                        binding.tvView4TitleLine3.visibility = View.GONE
//                    }
//                    else if (dashboardData.value3.isEmpty()){
//                        binding.tvView4Value3.visibility = View.GONE
//                    }
//                    else if (dashboardData.titleLine4.isEmpty()){
//                        binding.tvView4TitleLine4.visibility = View.GONE
//                    }
//                    else if (dashboardData.value4.isEmpty()){
//                        binding.tvView4Value4.visibility = View.GONE
//                    }
//                }

//                else{
//                    binding.liView1.visibility = View.GONE
//                    binding.liView2.visibility = View.GONE
//                    binding.liView3.visibility = View.GONE
//                    binding.liView4.visibility = View.GONE
//                    binding.llMain.visibility = View.VISIBLE
//
//                    binding.tvHeader.text = dashboardData.reportName
//                    binding.tvLine1.text = dashboardData.titleLine1
//                    binding.tvLine2.text = dashboardData.titleLine2
//                    binding.tvLine3.text = dashboardData.titleLine3
//                    binding.tvLine4.text = dashboardData.titleLine4
//                }
//                binding.tvHeader.text = dashboardData.reportName
//                binding.tvLine1.text = dashboardData.titleLine1
//                binding.tvLine2.text = dashboardData.titleLine2
//                binding.tvLine3.text = dashboardData.titleLine3
//                binding.tvLine4.text = dashboardData.titleLine4

//                binding.llMain.setOnClickListener {
//                    Log.e("TAG", "bind: DASHBOAR")
//                    if (mActivity.isDashboardVisible()) {
//                        mActivity.addFragment(
//                            DashboardDrillFragment.newInstance(
//                                false,
//                                dashboardData,
//                                DashboardDrillResponse(),
//                                dashboardData.storeProcedureName,
//                                dashboardData.reportSetupId, arrayListOf(),
//                                dashboardData.reportName,
//                                dashboardData.filter,
//                                dashboardData.reportGroupBy,
//                                startDate = "",
//                                endDate = ""
//                            ),
//                            addToBackStack = true,
//                            ignoreIfCurrent = true,
//                            animationType = AnimationType.fadeInfadeOut
//                        )
//                        dashboardBinding.rvDashboard.isEnabled = false
//                    }
//                }
                //binding.executePendingBindings()

                binding.liView1.setOnClickListener {
                    Log.e("TAG", "bind: DASHBOAR")
                    if (mActivity.isDashboardVisible()) {

                        if (dashboardData.redirectFormId > 0) {
                            when (dashboardData.redirectFormId.toString()) {
                                FORM_ID_ORDER_ENTRY_NUMBER -> {
                                    val isForApproval = dashboardData.reportType == APPROVAL_STRING
                                    mActivity.addFragment(
                                        OrderEntryListFragment.newInstance(isForApproval),
                                        addToBackStack = true,
                                        ignoreIfCurrent = true,
                                        animationType = AnimationType.fadeInfadeOut
                                    )
                                }

                                FORM_ID_QUOTATION_ENTRY_NUMBER -> {
                                    val isForApproval = dashboardData.reportType == APPROVAL_STRING
                                    mActivity.addFragment(
                                        QuotationEntryListFragment.newInstance(isForApproval),
                                        addToBackStack = true,
                                        ignoreIfCurrent = true,
                                        animationType = AnimationType.fadeInfadeOut
                                    )
                                }

                                FORM_ID_EXPENSE_ENTRY_NUMBER -> {
                                    val isForApproval = dashboardData.reportType == APPROVAL_STRING
                                    mActivity.addFragment(
                                        ExpenseListFragment.newInstance(isForApproval),
                                        addToBackStack = true,
                                        ignoreIfCurrent = true,
                                        animationType = AnimationType.fadeInfadeOut
                                    )
                                }

                                FORM_ID_LEAVE_APPLICATION_ENTRY_NUMBER -> {
                                    val isForApproval = dashboardData.reportType == APPROVAL_STRING
                                    mActivity.addFragment(
                                        LeaveApplicationListFragment.newInstance(isForApproval),
                                        addToBackStack = true,
                                        ignoreIfCurrent = true,
                                        animationType = AnimationType.fadeInfadeOut
                                    )
                                }
                            }
                        } else {

                            mActivity.addFragment(
                                DashboardDrillFragment.newInstance(
                                    false,
                                    dashboardData,
                                    DashboardDrillResponse(),
                                    dashboardData.storeProcedureName,
                                    dashboardData.reportSetupId, arrayListOf(),
                                    dashboardData.reportName,
                                    dashboardData.filter,
                                    dashboardData.reportGroupBy,
                                    startDate = "",
                                    endDate = ""
                                ),
                                addToBackStack = true,
                                ignoreIfCurrent = true,
                                animationType = AnimationType.fadeInfadeOut
                            )
                        }
                        dashboardBinding.rvDashboard.isEnabled = false
                    }
                }
                // binding.executePendingBindings()

                binding.liView2.setOnClickListener {
                    Log.e("TAG", "bind: DASHBOAR")
                    if (mActivity.isDashboardVisible()) {
                        mActivity.addFragment(
                            DashboardDrillFragment.newInstance(
                                false,
                                dashboardData,
                                DashboardDrillResponse(),
                                dashboardData.storeProcedureName,
                                dashboardData.reportSetupId, arrayListOf(),
                                dashboardData.reportName,
                                dashboardData.filter,
                                dashboardData.reportGroupBy,
                                startDate = "",
                                endDate = ""
                            ),
                            addToBackStack = true,
                            ignoreIfCurrent = true,
                            animationType = AnimationType.fadeInfadeOut
                        )
                        dashboardBinding.rvDashboard.isEnabled = false
                    }
                }
                //.executePendingBindings()

                binding.liView3.setOnClickListener {
                    Log.e("TAG", "bind: DASHBOAR")
                    if (mActivity.isDashboardVisible()) {
                        mActivity.addFragment(
                            DashboardDrillFragment.newInstance(
                                false,
                                dashboardData,
                                DashboardDrillResponse(),
                                dashboardData.storeProcedureName,
                                dashboardData.reportSetupId, arrayListOf(),
                                dashboardData.reportName,
                                dashboardData.filter,
                                dashboardData.reportGroupBy,
                                startDate = "",
                                endDate = ""
                            ),
                            addToBackStack = true,
                            ignoreIfCurrent = true,
                            animationType = AnimationType.fadeInfadeOut
                        )
                        dashboardBinding.rvDashboard.isEnabled = false
                    }
                }
                //binding.executePendingBindings()

                binding.liView4.setOnClickListener {
                    Log.e("TAG", "bind: DASHBOAR")
                    if (mActivity.isDashboardVisible()) {
                        mActivity.addFragment(
                            DashboardDrillFragment.newInstance(
                                false,
                                dashboardData,
                                DashboardDrillResponse(),
                                dashboardData.storeProcedureName,
                                dashboardData.reportSetupId, arrayListOf(),
                                dashboardData.reportName,
                                dashboardData.filter,
                                dashboardData.reportGroupBy,
                                startDate = "",
                                endDate = ""
                            ),
                            addToBackStack = true,
                            ignoreIfCurrent = true,
                            animationType = AnimationType.fadeInfadeOut
                        )
                        dashboardBinding.rvDashboard.isEnabled = false
                    }
                }



                binding.executePendingBindings()

            }
        }
    }


}