package ethicstechno.com.fieldforce.ui.fragments

import AnimationType
import addFragment
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.BuildConfig
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentSplashBinding
import ethicstechno.com.fieldforce.db.dao.AppDao
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.CheckUserMobileResponse
import ethicstechno.com.fieldforce.models.LoginResponse
import ethicstechno.com.fieldforce.ui.activities.HomeActivity
import ethicstechno.com.fieldforce.ui.base.MainBaseFragment
import ethicstechno.com.fieldforce.ui.fragments.loginsignup.LoginFragment
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.FCM_TOKEN
import ethicstechno.com.fieldforce.utils.IS_LOGIN
import ethicstechno.com.fieldforce.utils.IS_TRIP_START
import ethicstechno.com.fieldforce.utils.ImageUtils
import ethicstechno.com.fieldforce.utils.USER_NAME
import ethicstechno.com.fieldforce.utils.USER_PWD
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashFragment : MainBaseFragment() {
    lateinit var appDao: AppDao
    lateinit var binding: FragmentSplashBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_splash, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*if(isMockSettingsON(mActivity)){
            showMockAlertDialog()
            return
        }

        if(areThereMockPermissionApps(mActivity)){
            showMockAlertDialog()
            return
        }*/

        try {
            val isLogin = AppPreference.getBooleanPreference(mActivity, IS_LOGIN)
            appDao = appDatabase.appDao()

            appDao.getAppRegistration()?.let {
                val logoFile = it.apiHostingServer + it.logoFilePath
                if (logoFile.isNotEmpty()) {
                    ImageUtils().loadImageUrl(mActivity, logoFile, binding.imgLogo)
                } else {
                    binding.imgLogo.setImageResource(R.drawable.ethics_app_logo)
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (isLogin) {
                    callLoginApi()
                } else {
                    gotoLogin()
                }

            }, 1000)
        }catch (e: Exception){
            CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$onViewCreated$ :: error = " + e.message.toString())
        }
    }

    fun showMockAlertDialog(){
        CommonMethods.showAlertDialog(
            mActivity,
            getString(R.string.location_error_title),
            getString(R.string.mock_location_msg),
            okListener = object : PositiveButtonListener {
                override fun okClickListener() {
                    mActivity.finish()
                }
            },
            isCancelVisibility = false
        )
    }

    private fun gotoLogin() {
        try {
            mActivity.addFragment(
                LoginFragment(),
                false,
                true,
                animationType = AnimationType.rightInLeftOut
            )
        }catch (e: Exception){
            CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$gotoLogin()$ :: error = " + e.message.toString())
        }
    }

    private fun gotoHome() {
        try {
            val intent = Intent(mActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            mActivity.startActivity(intent)
            mActivity.finish()
        }catch (e: Exception){
            CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$gotoHome()$ :: error = " + e.message.toString())
        }
    }

    private fun callLoginApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showAlertDialog(
                mActivity,
                getString(R.string.network_error),
                getString(R.string.network_error_msg),
                null
            )
            return
        }

        val appRegistrationData = appDatabase.appDao().getAppRegistration()
        val username = AppPreference.getStringPreference(mActivity, USER_NAME)
        val password = AppPreference.getStringPreference(mActivity, USER_PWD)
        val loginReq = JsonObject()
        loginReq.addProperty("MobileNo", username)
        loginReq.addProperty("Password", password)
        loginReq.addProperty("Version", BuildConfig.VERSION_NAME)

        CommonMethods.writeLog("[" + this.javaClass.simpleName + "] IN \$callLoginApi()$ :: API REQUEST = " + loginReq.toString())

        Log.e("TAG", "callLoginApi: " + loginReq.toString())
        val appRegistrationCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)?.loginApi(loginReq)

        appRegistrationCall?.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                CommonMethods.writeLog("[" + this.javaClass.simpleName + "] IN \$callLoginApi()$ :: API RESPONSE = " + Gson().toJson(response.body()))
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (!it.success) {
                            return
                        }

                        if (it.version == BuildConfig.VERSION_NAME) {
                            appDatabase.appDao().insertLogin(it)
                            AppPreference.saveBooleanPreference(mActivity, IS_LOGIN, true)
                            val fcmToken = getFCMToken();
                            if (appDatabase.appDao().getLoginData().tripId > 0) {
                                AppPreference.saveBooleanPreference(mActivity, IS_TRIP_START, true)
                            } else {
                                AppPreference.saveBooleanPreference(mActivity, IS_TRIP_START, false)
                            }
                            callCheckUserMobileDevice(it.userId, fcmToken)
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
                CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$callLoginApi()$ :: API onFailure = " + t.message.toString())
                //CommonMethods.showAlertDialog(mActivity, getString(R.string.error), t.message, null)
            }

        })
    }

    private fun getFCMToken(): String {
        val fcmToken = AppPreference.getStringPreference(mActivity, FCM_TOKEN, "")
        if(fcmToken.isNotEmpty()){
            return fcmToken
        }
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                try {
                    //DialogUtils.removeCustomProcessDialog()
                    val token = task.result

                    if (!task.isSuccessful) {
                        //PubFun.writeLog("[LoginActivity] *ERROR* IN \$validateLogin:OnComplete\$ :: error = ${task.exception?.stackTrace}")
                        Toast.makeText(mActivity, "Sorry!!! Unable to get token from Google, please reinstall your app and try again", Toast.LENGTH_LONG).show()
                        // Optionally: showDialogForLoginEvenAfterTokenNotReceived(customerCode, strDeviceID)
                        return@addOnCompleteListener
                    }

                    if (token != null) {
                        AppPreference.saveStringPreference(mActivity, FCM_TOKEN, token)
                        Log.e("TAG", "validateLogin: FETCH NEW TOKEN $token")
                        //performLogin(customerCode, strDeviceID)
                    } else {
                        //PubFun.writeLog("[LoginActivity] *ERROR* IN \$validateLogin:OnCompleted\$ :: error = ${task.exception?.stackTrace}")
                        Toast.makeText(mActivity, "Oops!!! Unable to get token from Google, please reinstall your app and try again", Toast.LENGTH_LONG).show()
                        //showDialogForLoginEvenAfterTokenNotReceived(customerCode, strDeviceID)
                    }
                } catch (e: Exception) {
                    //PubFun.writeLog("[LoginActivity] *MSG* IN \$validateLogin\$ :: Found Device Token : ${e.message}")
                    CommonMethods.showToastMessage(
                        mActivity,
                        "Token Exception : " + e.message.toString()
                    )
                    CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$getFCMToken()$ :: = " + e.message.toString())
                }
            }
        return AppPreference.getStringPreference(mActivity, FCM_TOKEN, "")
    }

    private fun callCheckUserMobileDevice(userId: Int, fcmToken: String) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showAlertDialog(
                mActivity,
                getString(R.string.network_error),
                getString(R.string.network_error_msg),
                null
            )
            return
        }

        val appRegistrationData = appDatabase.appDao().getAppRegistration()
        val checkUserMobileDeviceReq = JsonObject()
        checkUserMobileDeviceReq.addProperty("UserId", userId)
        checkUserMobileDeviceReq.addProperty("IMEINumber", CommonMethods.getDeviceId(mActivity))
        checkUserMobileDeviceReq.addProperty("PhoneModelNo", CommonMethods.getdeviceModel())
        checkUserMobileDeviceReq.addProperty("PhoneBrandName", CommonMethods.getdevicename())
        checkUserMobileDeviceReq.addProperty("PhoneOSVersion", CommonMethods.getDeviceVersion())
        checkUserMobileDeviceReq.addProperty(
            "BatteryPercentage",
            CommonMethods.getBatteryPercentage(mActivity)
        )
        checkUserMobileDeviceReq.addProperty("DeviceToken", fcmToken)

        CommonMethods.writeLog("[" + this.javaClass.simpleName + "] IN \$callLoginApi()$ :: API REQUEST = " + checkUserMobileDeviceReq)


        Log.e("TAG", "callLoginApi: $checkUserMobileDeviceReq")
        val appRegistrationCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.checkUserMobileDevice(checkUserMobileDeviceReq)

        appRegistrationCall?.enqueue(object : Callback<CheckUserMobileResponse> {
            override fun onResponse(
                call: Call<CheckUserMobileResponse>,
                response: Response<CheckUserMobileResponse>
            ) {
                CommonMethods.writeLog("[" + this.javaClass.simpleName + "] IN \$callCheckUserMobileDevice()$ :: API RESPONSE = " + Gson().toJson(response.body()))
                if (isSuccess(response)) {
                    response.body()?.let {
                        gotoHome()
                        /*if(!it.success){
                            CommonMethods.showToastMessage(
                                mActivity,
                                getString(R.string.str_device_not_valid)
                            )
                            return
                        }*/

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
                CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$callLoginApi()$ :: API onFailure = " + t.message)
                if (mActivity != null) {
                    CommonMethods.showAlertDialog(mActivity, getString(R.string.error), t.message, null)
                }
            }

        })
    }

}