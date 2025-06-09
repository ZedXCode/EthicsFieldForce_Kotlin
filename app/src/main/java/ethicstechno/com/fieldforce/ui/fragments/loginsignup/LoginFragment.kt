package ethicstechno.com.fieldforce.ui.fragments.loginsignup

import AnimationType
import addFragment
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.BuildConfig
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentLoginBinding
import ethicstechno.com.fieldforce.db.dao.AppDao
import ethicstechno.com.fieldforce.models.AppRegistrationResponse
import ethicstechno.com.fieldforce.models.CheckUserMobileResponse
import ethicstechno.com.fieldforce.models.LoginResponse
import ethicstechno.com.fieldforce.permission.KotlinPermissions
import ethicstechno.com.fieldforce.ui.activities.HomeActivity
import ethicstechno.com.fieldforce.ui.base.MainBaseFragment
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.getBatteryPercentage
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.showToastMessage
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.FCM_TOKEN
import ethicstechno.com.fieldforce.utils.IS_LOGIN
import ethicstechno.com.fieldforce.utils.IS_TRIP_START
import ethicstechno.com.fieldforce.utils.ImageUtils
import ethicstechno.com.fieldforce.utils.SELECT_COMPANY
import ethicstechno.com.fieldforce.utils.USER_NAME
import ethicstechno.com.fieldforce.utils.USER_PWD
import hideKeyboard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : MainBaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentLoginBinding
    var companyList: ArrayList<AppRegistrationResponse> = arrayListOf()
    lateinit var appDao: AppDao
    private var passwordShowed: Boolean = false
    var logoFile = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            appDao = appDatabase.appDao()
            binding.etUsername.setText(AppPreference.getStringPreference(mActivity, USER_NAME, ""))
            binding.btnLogin.setOnClickListener(this)
            binding.imgPwdIndicator.setOnClickListener(this)
            mActivity.addFragment(
                LoginFragment(),
                false,
                true,
                animationType = AnimationType.fadeInfadeOut
            )
            initView()
            notificationPermissionFor33()
            getFCMToken();
        } catch (e: Exception) {
            CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$onViewCreated()$ ::  " + e.message.toString())
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                try {
                    //DialogUtils.removeCustomProcessDialog()
                    val token = task.result

                    if (!task.isSuccessful) {
                        //PubFun.writeLog("[LoginActivity] *ERROR* IN \$validateLogin:OnComplete\$ :: error = ${task.exception?.stackTrace}")
                        Toast.makeText(
                            mActivity,
                            "Sorry!!! Unable to get token from Google, please reinstall your app and try again",
                            Toast.LENGTH_LONG
                        ).show()
                        // Optionally: showDialogForLoginEvenAfterTokenNotReceived(customerCode, strDeviceID)
                        return@addOnCompleteListener
                    }

                    if (token != null) {
                        AppPreference.saveStringPreference(mActivity, FCM_TOKEN, token)
                        Log.e("TAG", "validateLogin: FETCH NEW TOKEN $token")
                        //performLogin(customerCode, strDeviceID)
                    } else {
                        //PubFun.writeLog("[LoginActivity] *ERROR* IN \$validateLogin:OnCompleted\$ :: error = ${task.exception?.stackTrace}")
                        Toast.makeText(
                            mActivity,
                            "Oops!!! Unable to get token from Google, please reinstall your app and try again",
                            Toast.LENGTH_LONG
                        ).show()
                        //showDialogForLoginEvenAfterTokenNotReceived(customerCode, strDeviceID)
                    }
                } catch (e: Exception) {
                    CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$getFCMToken()$ ::Token error = " + e.message.toString())
                    //PubFun.writeLog("[LoginActivity] *MSG* IN \$validateLogin\$ :: Found Device Token : ${e.message}")
                    showToastMessage(mActivity, "Token Exception : " + e.message.toString())
                }
            }
    }

    private fun notificationPermissionFor33() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            KotlinPermissions.with(mActivity)
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

    private fun initView() {
        try {
            appDao.getAppRegistration().let {
                if (it != null) {
                    logoFile = it.apiHostingServer + it.logoFilePath
                    if (logoFile.isNotEmpty()) {
                        ImageUtils().loadImageUrl(mActivity, logoFile, binding.imgLogo)
                    }
                } else {
                    binding.imgLogo.setImageResource(R.drawable.ethics_app_logo)
                }
            }
        } catch (e: Exception) {
            CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$initView()$ :: error = " + e.message.toString())
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnLogin -> validation()
            R.id.tvForgotPwd -> {
                mActivity.addFragment(
                    ForgotPwdFragment(),
                    true,
                    true,
                    animationType = AnimationType.fadeInfadeOut
                )
            }

            R.id.imgPwdIndicator -> {
                mActivity.hideKeyboard()
                if (binding.etPassword.text.toString().isNotEmpty()) {
                    if (passwordShowed) {
                        binding.etPassword.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                        binding.imgPwdIndicator.setImageResource(R.drawable.ic_password_hide)
                        passwordShowed = false
                    } else {
                        binding.etPassword.transformationMethod =
                            HideReturnsTransformationMethod.getInstance()
                        binding.imgPwdIndicator.setImageResource(R.drawable.ic_password_view)
                        passwordShowed = true
                    }
                    binding.etPassword.setSelection(binding.etPassword.text.toString().length)
                }
            }
        }
    }

    private fun validation() {
        if (binding.etUsername.text.toString().isEmpty()) {
            showToastMessage(mActivity, getString(R.string.username_validation))
            return
        }
        if (binding.etPassword.text.toString().isEmpty()) {
            showToastMessage(mActivity, getString(R.string.password_validation))
            return
        }
        callAppRegistrationApi()
    }

    private fun gotoHome() {
        try {
            val intent = Intent(mActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            mActivity.finish()
        } catch (e: Exception) {
            CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$gotoHome()$ :: error = " + e.message.toString())
        }
    }

    private fun callAppRegistrationApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showAlertDialog(
                mActivity,
                getString(R.string.network_error),
                getString(R.string.network_error_msg),
                null
            )
            return
        }

        CommonMethods.showLoading(mActivity)

        val username = binding.etUsername.text.toString().trim()
        val appRegistrationReq = JsonObject()
        appRegistrationReq.addProperty("RegistredMobileNo", username)

        CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$callLoginApi()$ :: API REQUEST = " + appRegistrationReq.toString())

        val appRegistrationCall = WebApiClient.getInstance(mActivity)
            .webApi_without()?.getAppRegistration(appRegistrationReq)

        appRegistrationCall?.enqueue(object : Callback<List<AppRegistrationResponse>> {
            override fun onResponse(
                call: Call<List<AppRegistrationResponse>>,
                response: Response<List<AppRegistrationResponse>>
            ) {
                CommonMethods.hideLoading()
                CommonMethods.writeLog(
                    "[" + this.javaClass.simpleName + "] *ERROR* IN \$callAppRegistrationApi()$ :: API RESPONSE = " + Gson().toJson(
                        response.body()
                    )
                )

                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {

                            companyList.clear()
                            companyList.add(
                                AppRegistrationResponse(
                                    0,
                                    getString(R.string.select_company),
                                    "",
                                    0,
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    false,
                                    "",
                                    0,
                                    false,
                                    "",
                                    0.0,
                                    0.0,
                                    ""
                                )
                            )
                            companyList.addAll(it)
                            if (it.size > 1) {
                                showCompanyDialog(companyList)
                            } else {
                                appDatabase.appDao().insertAppRegistration(it[0])
                                callLoginApi()
                            }
                        } else {
                            showToastMessage(
                                mActivity,
                                getString(R.string.this_user_is_not_registered)
                            )
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

            override fun onFailure(call: Call<List<AppRegistrationResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$callLoginApi()$ :: API onFailure = " + t.message)
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

    private fun callLoginApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val loginReq = JsonObject()
        loginReq.addProperty("MobileNo", username)
        loginReq.addProperty("Password", password)
        loginReq.addProperty("Version", BuildConfig.VERSION_NAME)

        CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$callLoginApi()$ :: API REQUEST = " + loginReq.toString())

        val appRegistrationCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)?.loginApi(loginReq)

        appRegistrationCall?.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                CommonMethods.hideLoading()
                CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$callLoginApi()$ :: response = " + Gson().toJson(response.body()))
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (!it.success) {
                            showToastMessage(mActivity, getString(R.string.str_invalid_login))
                            return
                        }

                        if (it.version == BuildConfig.VERSION_NAME) {
                            appDao.insertLogin(it)
                            AppPreference.saveStringPreference(
                                mActivity,
                                USER_NAME,
                                binding.etUsername.text.toString()
                            )
                            AppPreference.saveStringPreference(
                                mActivity,
                                USER_PWD,
                                binding.etPassword.text.toString()
                            )
                            AppPreference.saveBooleanPreference(mActivity, IS_LOGIN, true)
                            val isTripStart = appDatabase.appDao().getLoginData().tripId > 0
                            AppPreference.saveBooleanPreference(
                                mActivity,
                                IS_TRIP_START,
                                isTripStart
                            )
                            callCheckUserMobileDevice()
                        } else {
                            mActivity.showUpdateDialog()
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

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                CommonMethods.hideLoading()
                CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$callLoginApi()$ :: response = " + t.message.toString())
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

    private fun callCheckUserMobileDevice() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()
        val notificationToken = AppPreference.getStringPreference(mActivity, FCM_TOKEN, "")
        val checkUserMobileDeviceReq = JsonObject()
        checkUserMobileDeviceReq.addProperty("UserId", loginData.userId)
        checkUserMobileDeviceReq.addProperty("IMEINumber", CommonMethods.getDeviceId(mActivity))
        checkUserMobileDeviceReq.addProperty("PhoneModelNo", CommonMethods.getdeviceModel())
        checkUserMobileDeviceReq.addProperty("PhoneBrandName", CommonMethods.getdevicename())
        checkUserMobileDeviceReq.addProperty("PhoneOSVersion", CommonMethods.getDeviceVersion())
        checkUserMobileDeviceReq.addProperty("BatteryPercentage", getBatteryPercentage(mActivity))
        checkUserMobileDeviceReq.addProperty("DeviceToken", notificationToken)


        CommonMethods.writeLog("[" + this.javaClass.simpleName + "] IN \$callCheckUserMobileDevice()$ :: API REQUEST = " + checkUserMobileDeviceReq.toString())

        val appRegistrationCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.checkUserMobileDevice(checkUserMobileDeviceReq)

        appRegistrationCall?.enqueue(object : Callback<CheckUserMobileResponse> {
            override fun onResponse(
                call: Call<CheckUserMobileResponse>,
                response: Response<CheckUserMobileResponse>
            ) {
                CommonMethods.hideLoading()
                CommonMethods.writeLog("[" + this.javaClass.simpleName + "] IN \$callCheckUserMobileDevice()$ :: API RESPONSE = " + Gson().toJson(response.body()))
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (!it.success) {
                            showToastMessage(mActivity, getString(R.string.str_device_not_valid))
                            return
                        }
                        gotoHome()
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

            override fun onFailure(call: Call<CheckUserMobileResponse>, t: Throwable) {
                CommonMethods.writeLog("[" + this.javaClass.simpleName + "] IN \$onFailure()$ :: API RESPONSE = " + t.message.toString())
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

    private fun showCompanyDialog(companyList: List<AppRegistrationResponse>) {
        try {
            val companyDialog = Dialog(mActivity)
            companyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            companyDialog.setCancelable(true)
            companyDialog.setContentView(R.layout.company_selection_dialog)

            val spCompany: Spinner = companyDialog.findViewById(R.id.spCompany)
            val btnSubmit: TextView = companyDialog.findViewById(R.id.btnSubmit)

            btnSubmit.setOnClickListener {
                if (spCompany.selectedItemPosition == 0) {
                    showToastMessage(mActivity, getString(R.string.please_select_company))
                    return@setOnClickListener
                }
                companyDialog.dismiss()
                appDao.deleteAppRegistration()
                Log.e("TAG", "showCompanyDialog: " + spCompany.selectedItemPosition)

                val selectedCompany = companyList[spCompany.selectedItemPosition]
                appDao.insertAppRegistration(selectedCompany)

                Log.d("SELECTCOMPANY===>", selectedCompany.customerName)

                AppPreference.saveStringPreference(
                    mActivity,
                    SELECT_COMPANY,
                    selectedCompany.customerName
                )
                callLoginApi()
            }

            val adapter = CompanyAdapter(mActivity, R.layout.simple_spinner_item, companyList)
            spCompany.adapter = adapter
            spCompany.setSelection(0)

            val window = companyDialog.window
            window!!.setLayout(
                AbsListView.LayoutParams.MATCH_PARENT,
                AbsListView.LayoutParams.WRAP_CONTENT
            )
            companyDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            companyDialog.show()
        }catch (e: Exception){
            CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$showCompanyDialog()$ :: error = " + e.message.toString())
        }
    }

    inner class CompanyAdapter(
        context: Context,
        spinnerLayout: Int,
        companyList: List<AppRegistrationResponse>
    ) :
        ArrayAdapter<AppRegistrationResponse>(context, spinnerLayout, companyList) {
        private val mContext: Context = context
        private var supervisorList: List<AppRegistrationResponse> = ArrayList()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val employeeData = companyList[position]
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            /*if (position == 0) {
                name.setTextColor(ContextCompat.getColor(mActivity, R.color.black))
            }*/
            name.text = employeeData.customerName
            return listItem
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val currentSupervisor = companyList[position]
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            name.text = currentSupervisor.customerName
            return listItem
        }

        init {
            supervisorList = companyList
        }
    }

}