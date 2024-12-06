package ethicstechno.com.fieldforce.ui.fragments.navigationdrawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentSupportBinding
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SupportFragment : HomeBaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentSupportBinding
    var pageType = 0

    companion object {

        /*fun newInstance(
            pageType: Int,
        ): SupportFragment {
            val args = Bundle()
            args.putInt(ARG_PARAM1, pageType)
            val fragment = SupportFragment()
            fragment.arguments = args
            return fragment
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_support, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            pageType = it.getInt(ARG_PARAM1, 0)
        }
        initView()
    }

    override fun onResume() {
        super.onResume()
        mActivity.bottomHide()
    }

    private fun initView() {
        binding.toolbar.imgBack.visibility = View.VISIBLE
        binding.toolbar.tvHeader.text = getString(R.string.support)
        mActivity.bottomHide()
        binding.toolbar.imgBack.setOnClickListener(this)
        binding.tvSubmit.setOnClickListener(this)
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
            R.id.tvSubmit -> {
                feedbackFormValidation()
            }
        }
    }

    private fun feedbackFormValidation() {
        if (binding.etFullName.text.toString().trim().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_fullname))
            return
        }
        if (binding.etMobileNumber.text.toString().trim().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_mobile))
            return
        }
        if (binding.etFeedback.text.toString().trim().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.enter_feedback))
            return
        }
        callSupportFeedbackSubmitApi()
    }

    private fun callSupportFeedbackSubmitApi() {

        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val feedbackReq = JsonObject()
        feedbackReq.addProperty("UserId", loginData.userId)
        feedbackReq.addProperty("FeedbackId", "0")
        feedbackReq.addProperty("CustomerName", binding.etFullName.text.toString().trim())
        feedbackReq.addProperty("MobileNo", binding.etMobileNumber.text.toString().trim())
        feedbackReq.addProperty("Feedback", binding.etFeedback.text.toString().trim())


        val feedbackCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.addFeedback(feedbackReq)

        feedbackCall?.enqueue(object : Callback<CommonSuccessResponse> {
            override fun onResponse(
                call: Call<CommonSuccessResponse>,
                response: Response<CommonSuccessResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        CommonMethods.showAlertDialog(mActivity,
                            getString(R.string.feedback),
                            it.returnMessage,//getString(R.string.feedback_submit_success_msg),
                            isCancelVisibility = false,
                            okListener = object : PositiveButtonListener {
                                override fun okClickListener() {
                                    if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                                        mActivity.onBackPressedDispatcher.onBackPressed()
                                    } else {
                                        mActivity.onBackPressed()
                                    }
                                }
                            })
                    }
                } else {
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

}