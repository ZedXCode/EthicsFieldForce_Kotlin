package ethicstechno.com.fieldforce.ui.fragments.reports

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.databinding.FragmentWebViewReportBinding
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.ARG_PARAM1
import ethicstechno.com.fieldforce.utils.ARG_PARAM2

class WebViewReportFragment : HomeBaseFragment() {

    lateinit var binding: FragmentWebViewReportBinding
    private var pageName = ""
    private var reportName = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_web_view_report, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            reportName = it.getString(ARG_PARAM1, "")
            pageName = it.getString(ARG_PARAM2, "")
        }
        binding.toolbar.imgBack.visibility = View.VISIBLE
        binding.toolbar.tvHeader.text = reportName
        binding.toolbar.imgBack.setOnClickListener {
            if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                mActivity.onBackPressedDispatcher.onBackPressed()
            } else {
                mActivity.onBackPressed()
            }
        }
        loadWebView()
    }

    override fun onResume() {
        super.onResume()
        binding.webView.clearCache(true)
        // Clear cookies (important to handle session properly)
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        Log.e("TAG", "onResume: HOME ACTIVITY RESUMED CALLED")
    }

    private fun loadWebView() {

        // Enable JavaScript
        binding.webView.settings.javaScriptEnabled = true

        // Enable Zoom Controls
        binding.webView.settings.setSupportZoom(true)
        binding.webView.settings.builtInZoomControls = true
        binding.webView.settings.displayZoomControls = false
        binding.webView.settings.domStorageEnabled = true
        binding.webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Enable Caching
        binding.webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK


        // Handle Page Load
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                handler?.proceed()
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.e("TAG", "shouldOverrideUrlLoading:PAGE STARTED URLS ::  $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.e("TAG", "shouldOverrideUrlLoading:PAGE FINISHED URLS ::  $url")
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(request?.url.toString())  // Load the redirected URL
                Log.e(
                    "TAG",
                    "shouldOverrideUrlLoading:REDIRECTED URLS ::  " + request?.url.toString()
                )
                return false  // Load inside the WebView
            }
        }
        val baseUrl = appDao.getAppRegistration().webHostingServer
        val authURL =
            "signin?registrationNo=${appDao.getLoginData().mobileNo}&password=${appDao.getLoginData().password}&isredirectform=1&redirectForm="

        binding.webView.loadUrl(baseUrl+authURL+pageName)
    }


    companion object {
        fun newInstance(
            reportName: String,
            reportUrl: String
        ): WebViewReportFragment {
            val args = Bundle()
            args.putString(ARG_PARAM1, reportName)
            args.putString(ARG_PARAM2, reportUrl)
            val fragment = WebViewReportFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.webView.canGoBack()) {
            binding.webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}