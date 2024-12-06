package ethicstechno.com.fieldforce.ui.fragments.navigationdrawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentDynamicPageBinding
import ethicstechno.com.fieldforce.models.dynamiccontent.DynamicPageContentList
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DynamicPageContentFragment : HomeBaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentDynamicPageBinding
    var pageType = 0

    companion object {

        fun newInstance(
            pageType: Int,
        ): DynamicPageContentFragment {
            val args = Bundle()
            args.putInt(ARG_PARAM1, pageType)
            val fragment = DynamicPageContentFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_dynamic_page, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            pageType = it.getInt(ARG_PARAM1, 0)
        }
        initView()
        callDynamicPageContentApi()
    }

    private fun callDynamicPageContentApi() {

        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val dynamicPageReq = JsonObject()
        dynamicPageReq.addProperty("DynamicPageId", pageType)


        val dynamicPageCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getDynamicPageContent(dynamicPageReq)

        dynamicPageCall?.enqueue(object : Callback<List<DynamicPageContentList>> {
            override fun onResponse(
                call: Call<List<DynamicPageContentList>>,
                response: Response<List<DynamicPageContentList>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            loadWebView(it[0])
                        }
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

            override fun onFailure(call: Call<List<DynamicPageContentList>>, t: Throwable) {
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

    override fun onResume() {
        super.onResume()
        mActivity.bottomHide()
    }

    private fun initView() {
        binding.toolbar.imgBack.visibility = View.VISIBLE
        mActivity.bottomHide()
        binding.toolbar.imgBack.setOnClickListener(this)
        when (pageType) {
            1 -> binding.toolbar.tvHeader.text = getString(R.string.about_us)
            2 -> binding.toolbar.tvHeader.text = getString(R.string.terms_condition)
            3 -> binding.toolbar.tvHeader.text = getString(R.string.privacy_policy)
        }
    }

    private fun loadWebView(dynamicPageContentList: DynamicPageContentList) {
        binding.webView.settings.javaScriptEnabled = true

        // Other WebView settings
        val webSettings: WebSettings = binding.webView.settings
        webSettings.domStorageEnabled = true // Enable DOM Storage
        webSettings.loadsImagesAutomatically = true // Automatically load images
        webSettings.mixedContentMode =
            WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE // Handle mixed content

        // Improve WebView performance
        //webSettings.cach(true)
        webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

        // Set a WebViewClient to handle events such as page loading
        binding.webView.webViewClient = WebViewClient()
        webSettings.javaScriptEnabled = true

        binding.webView.loadDataWithBaseURL(
            null,
            dynamicPageContentList.dynamicPageContent,
            "text/html",
            "UTF-8",
            null
        )
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
        }
    }
}