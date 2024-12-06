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
import androidx.databinding.DataBindingUtil
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
import ethicstechno.com.fieldforce.utils.*
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.getBatteryPercentage
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.showToastMessage
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
        appDao.getAppRegistration().let {
            if(it != null){
                logoFile = it.apiHostingServer + it.logoFilePath
                if (logoFile.isNotEmpty()) {
                    ImageUtils().loadImageUrl(mActivity, logoFile, binding.imgLogo)
                }
            } else {
                binding.imgLogo.setImageResource(R.drawable.ethics_app_logo)
            }
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
        val intent = Intent(mActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        mActivity.finish()
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

        val appRegistrationCall = WebApiClient.getInstance(mActivity)
            .webApi_without()?.getAppRegistration(appRegistrationReq)

        appRegistrationCall?.enqueue(object : Callback<List<AppRegistrationResponse>> {
            override fun onResponse(
                call: Call<List<AppRegistrationResponse>>,
                response: Response<List<AppRegistrationResponse>>
            ) {
                CommonMethods.hideLoading()

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
                                    0.0
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
                if(mActivity != null) {
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

        Log.e("TAG", "callLoginApi: " + loginReq.toString())
        val appRegistrationCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)?.loginApi(loginReq)

        appRegistrationCall?.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                CommonMethods.hideLoading()
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
                            AppPreference.saveBooleanPreference(mActivity, IS_TRIP_START, isTripStart)
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
                if(mActivity != null) {
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

        val checkUserMobileDeviceReq = JsonObject()
        checkUserMobileDeviceReq.addProperty("UserId", loginData.userId)
        checkUserMobileDeviceReq.addProperty("IMEINumber", CommonMethods.getDeviceId(mActivity))
        checkUserMobileDeviceReq.addProperty("PhoneModelNo", CommonMethods.getdeviceModel())
        checkUserMobileDeviceReq.addProperty("PhoneBrandName", CommonMethods.getdevicename())
        checkUserMobileDeviceReq.addProperty("PhoneOSVersion", CommonMethods.getDeviceVersion())
        checkUserMobileDeviceReq.addProperty("BatteryPercentage", getBatteryPercentage(mActivity))

        Log.e("TAG", "callLoginApi: " + checkUserMobileDeviceReq.toString())
        val appRegistrationCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.checkUserMobileDevice(checkUserMobileDeviceReq)

        appRegistrationCall?.enqueue(object : Callback<CheckUserMobileResponse> {
            override fun onResponse(
                call: Call<CheckUserMobileResponse>,
                response: Response<CheckUserMobileResponse>
            ) {
                CommonMethods.hideLoading()
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
                CommonMethods.hideLoading()
                if(mActivity != null) {
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
        val companyDialog = Dialog(mActivity)
        companyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        companyDialog.setCancelable(true)
        companyDialog.setContentView(R.layout.company_selection_dialog)

        val spCompany: Spinner = companyDialog.findViewById(R.id.spCompany)
        val btnSubmit: TextView = companyDialog.findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            if (spCompany.selectedItemPosition == 0) {
                showToastMessage(
                    mActivity,
                    getString(R.string.please_select_company)
                )
                return@setOnClickListener
            }
            companyDialog.dismiss()
            appDao.deleteAppRegistration()
            Log.e("TAG", "showCompanyDialog: " + spCompany.selectedItemPosition)
            appDao.insertAppRegistration(companyList[spCompany.selectedItemPosition])
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