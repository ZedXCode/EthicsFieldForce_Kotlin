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
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.BuildConfig
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentSplashBinding
import ethicstechno.com.fieldforce.db.dao.AppDao
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.CheckUserMobileResponse
import ethicstechno.com.fieldforce.models.CompanySelectionResponse
import ethicstechno.com.fieldforce.models.LoginResponse
import ethicstechno.com.fieldforce.ui.activities.HomeActivity
import ethicstechno.com.fieldforce.ui.base.MainBaseFragment
import ethicstechno.com.fieldforce.ui.fragments.loginsignup.LoginFragment
import ethicstechno.com.fieldforce.utils.*
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.areThereMockPermissionApps
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.isMockSettingsON
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
        mActivity.addFragment(
            LoginFragment(),
            false,
            true,
            animationType = AnimationType.rightInLeftOut
        )
    }

    private fun gotoHome() {
        val intent = Intent(mActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        mActivity.startActivity(intent)
        mActivity.finish()
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

        Log.e("TAG", "callLoginApi: " + loginReq.toString())
        val appRegistrationCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)?.loginApi(loginReq)

        appRegistrationCall?.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {

                if (isSuccess(response)) {
                    response.body()?.let {
                        if (!it.success) {
                            return
                        }

                        if (it.version == BuildConfig.VERSION_NAME) {
                            appDatabase.appDao().insertLogin(it)
                            AppPreference.saveBooleanPreference(mActivity, IS_LOGIN, true)
                            if (appDatabase.appDao().getLoginData().tripId > 0) {
                                AppPreference.saveBooleanPreference(mActivity, IS_TRIP_START, true)
                            } else {
                                AppPreference.saveBooleanPreference(mActivity, IS_TRIP_START, false)
                            }
                            callCheckUserMobileDevice(it.userId)
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
                //CommonMethods.showAlertDialog(mActivity, getString(R.string.error), t.message, null)
            }

        })

    }

    private fun callCheckUserMobileDevice(userId: Int) {
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

        Log.e("TAG", "callLoginApi: " + checkUserMobileDeviceReq.toString())
        val appRegistrationCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.checkUserMobileDevice(checkUserMobileDeviceReq)

        appRegistrationCall?.enqueue(object : Callback<CheckUserMobileResponse> {
            override fun onResponse(
                call: Call<CheckUserMobileResponse>,
                response: Response<CheckUserMobileResponse>
            ) {
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
                if (mActivity != null) {
                    CommonMethods.showAlertDialog(mActivity, getString(R.string.error), t.message, null)
                }
            }

        })
    }

}