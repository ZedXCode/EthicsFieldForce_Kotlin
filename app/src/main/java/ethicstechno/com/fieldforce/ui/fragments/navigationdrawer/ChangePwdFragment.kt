package ethicstechno.com.fieldforce.ui.fragments.navigationdrawer

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.gson.JsonObject
import hideKeyboard
import ethicstechno.com.fieldforce.MainActivity
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentChangePwdBinding
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.IS_LOGIN
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePwdFragment : HomeBaseFragment(), View.OnClickListener {

    //private var prefREM: SharedPreferences? = null
    lateinit var binding: FragmentChangePwdBinding
    private var oldPasswordShowed: Boolean = false
    private var newPasswordShowed: Boolean = false
    private var confirmPasswordShowed: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_change_pwd, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity.bottomHide()
        initView()
    }

    private fun initView() {
        binding.toolbar.tvHeader.text = getString(R.string.change_password)
        binding.toolbar.imgBack.visibility = View.VISIBLE
        binding.toolbar.imgBack.setOnClickListener(this)
        binding.tvChangePwd.setOnClickListener(this)
        binding.imgOldPwdIndicator.setOnClickListener(this)
        binding.imgConfirmPwdIndicator.setOnClickListener(this)
        binding.imgNewPwdIndicator.setOnClickListener(this)

    }

    override fun onResume() {
        super.onResume()
        mActivity.bottomHide()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                    mActivity.onBackPressedDispatcher.onBackPressed()
                } else {
                    mActivity.onBackPressed()
                }
            }
            R.id.tvChangePwd -> {
                changePwdValidation()
            }
            R.id.imgOldPwdIndicator -> {
                mActivity.hideKeyboard()
                if (binding.etOldPassword.text.toString().isNotEmpty()) {
                    if (oldPasswordShowed) {
                        binding.etOldPassword.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                        binding.imgOldPwdIndicator.setImageResource(R.drawable.ic_password_hide)
                        oldPasswordShowed = false
                    } else {
                        binding.etOldPassword.transformationMethod =
                            HideReturnsTransformationMethod.getInstance()
                        binding.imgOldPwdIndicator.setImageResource(R.drawable.ic_password_view)
                        oldPasswordShowed = true
                    }
                    binding.etOldPassword.setSelection(binding.etOldPassword.text.toString().length)
                }
            }
            R.id.imgNewPwdIndicator -> {
                mActivity.hideKeyboard()
                if (binding.etNewPassword.text.toString().isNotEmpty()) {
                    if (newPasswordShowed) {
                        binding.etNewPassword.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                        binding.imgNewPwdIndicator.setImageResource(R.drawable.ic_password_hide)
                        newPasswordShowed = false
                    } else {
                        binding.etNewPassword.transformationMethod =
                            HideReturnsTransformationMethod.getInstance()
                        binding.imgNewPwdIndicator.setImageResource(R.drawable.ic_password_view)
                        newPasswordShowed = true
                    }
                    binding.etNewPassword.setSelection(binding.etNewPassword.text.toString().length)
                }
            }
            R.id.imgConfirmPwdIndicator -> {
                mActivity.hideKeyboard()
                if (binding.etConfirmPassword.text.toString().isNotEmpty()) {
                    if (confirmPasswordShowed) {
                        binding.etConfirmPassword.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                        binding.imgConfirmPwdIndicator.setImageResource(R.drawable.ic_password_hide)
                        confirmPasswordShowed = false
                    } else {
                        binding.etConfirmPassword.transformationMethod =
                            HideReturnsTransformationMethod.getInstance()
                        binding.imgConfirmPwdIndicator.setImageResource(R.drawable.ic_password_view)
                        confirmPasswordShowed = true
                    }
                    binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.toString().length)
                }
            }
        }
    }

    private fun changePwdValidation() {
        if (binding.etOldPassword.text.toString().trim().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, "Please enter old password")
            return
        }
        if (binding.etNewPassword.text.toString().trim().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, "Please enter new password")
            return
        }
        if (binding.etConfirmPassword.text.toString().trim().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, "Please enter confirm password")
            return
        }
        if (binding.etNewPassword.text.toString()
                .trim() != binding.etConfirmPassword.text.toString().trim()
        ) {
            CommonMethods.showToastMessage(
                mActivity,
                "New Password and Confirm Password Must be same"
            )
            return
        }
        callUpdatePasswordApi()
    }

    private fun callUpdatePasswordApi() {

        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val changePwdReq = JsonObject()
        changePwdReq.addProperty("UserId", loginData.userId)
        changePwdReq.addProperty("OldPassword", binding.etOldPassword.text.toString().trim())
        changePwdReq.addProperty("NewPassword", binding.etNewPassword.text.toString().trim())
        changePwdReq.addProperty("UpdateBy", loginData.userId)

        val changePwdCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.changePassword(changePwdReq)

        changePwdCall?.enqueue(object : Callback<CommonSuccessResponse> {
            override fun onResponse(
                call: Call<CommonSuccessResponse>,
                response: Response<CommonSuccessResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        CommonMethods.showAlertDialog(
                            mActivity,
                            getString(R.string.change_pwd),
                            it.returnMessage,//getString(R.string.profile_pwd_update_success_msg),
                            isCancelVisibility = false,
                            okListener = object : PositiveButtonListener {
                                override fun okClickListener() {
                                    appDao.deleteLogin()
                                    AppPreference.saveBooleanPreference(mActivity, IS_LOGIN, false)
                                    callLogoutApi()
                                }
                            })
                    }
                }else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<CommonSuccessResponse>, t: Throwable) {
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

    private fun callLogoutApi() {
        val intent = Intent(mActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        mActivity.startActivity(intent)
    }
}