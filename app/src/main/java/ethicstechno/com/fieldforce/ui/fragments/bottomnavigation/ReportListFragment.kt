package ethicstechno.com.fieldforce.ui.fragments.bottomnavigation

import AnimationType
import addFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentReportListBinding
import ethicstechno.com.fieldforce.databinding.ItemReportBinding
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardDrillResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardListResponse
import ethicstechno.com.fieldforce.models.reports.ReportListResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.ui.fragments.dashboard.DashboardDrillFragment
import ethicstechno.com.fieldforce.ui.fragments.reports.TripReportFragment
import ethicstechno.com.fieldforce.ui.fragments.reports.TripSummeryReportFragment
import ethicstechno.com.fieldforce.ui.fragments.reports.VisitReportFragment
import ethicstechno.com.fieldforce.ui.fragments.reports.WebViewReportFragment
import ethicstechno.com.fieldforce.utils.ATTENDANCE_REPORT_MODULE
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.showToastMessage
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.TRIP_REPORT_MODULE
import ethicstechno.com.fieldforce.utils.TRIP_SUMMERY_REPORT_MODULE
import ethicstechno.com.fieldforce.utils.VISIT_REPORT_MODULE
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReportListFragment : HomeBaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentReportListBinding
    var reportList: ArrayList<ReportListResponse> = arrayListOf()
    var reportListFromApi: ArrayList<ReportListResponse> = arrayListOf()
    lateinit var reportAdapter: ReportListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_report_list, container, false)
        return binding.root
    }

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onResume() {
        super.onResume()
        Log.e("TAG", "onResume: ")
        mActivity.bottomVisible()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded && !hidden) {
            mActivity.bottomVisible()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.e("TAG", "onStart: ")
    }

    private fun initView() {
        try {
            binding.toolbar.imgMenu.visibility = View.VISIBLE
            binding.toolbar.imgBack.visibility = View.GONE
            binding.toolbar.tvHeader.text = getString(R.string.reports)
            binding.toolbar.imgMenu.setOnClickListener(this)
            reportList.clear()
            if (AppPreference.getBooleanPreference(mActivity, ATTENDANCE_REPORT_MODULE)) {
                reportList.add(
                    ReportListResponse(
                        reportName = getString(R.string.attendance_report),
                        isDynamicReport = false
                    )
                )
            }
            if (AppPreference.getBooleanPreference(mActivity, TRIP_REPORT_MODULE)) {
                reportList.add(
                    ReportListResponse(
                        reportName = getString(R.string.trip_report),
                        isDynamicReport = false
                    )
                )
            }
            if (AppPreference.getBooleanPreference(mActivity, TRIP_SUMMERY_REPORT_MODULE)) {
                reportList.add(
                    ReportListResponse(
                        reportName = getString(R.string.trip_summery_report),
                        isDynamicReport = false
                    )
                )
            }
            if (AppPreference.getBooleanPreference(mActivity, VISIT_REPORT_MODULE)) {
                reportList.add(
                    ReportListResponse(
                        reportName = getString(R.string.visit_report),
                        isDynamicReport = false
                    )
                )
            }
            callReportListApi()
        } catch (e: Exception) {
            CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$initView()$ :: onFailure = " + e.message.toString())
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgMenu -> mActivity.openDrawer()
        }
    }

    private fun callReportListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val reportListReq = JsonObject()
        reportListReq.addProperty("UserId", loginData.userId)

        CommonMethods.writeLog("[" + this.javaClass.simpleName + "] IN \$callReportListApi()$ :: API REQUEST = " + reportListReq.toString())

        val reportListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getReportList(reportListReq)

        reportListCall?.enqueue(object : Callback<List<ReportListResponse>> {
            override fun onResponse(
                call: Call<List<ReportListResponse>>,
                response: Response<List<ReportListResponse>>
            ) {
                CommonMethods.hideLoading()

                CommonMethods.writeLog(
                    "[" + this.javaClass.simpleName + "] IN \$callReportListApi()$ :: API RESPONSE = " + Gson().toJson(
                        response.body()
                    )
                )

                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            reportListFromApi.clear()
                            reportListFromApi.addAll(it)
                            reportList.addAll(reportListFromApi)
                            setupReportRecyclerView()
                        } else {
                            setupReportRecyclerView()
                        }
                    }
                } else {
                    /*CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        getString(R.string.error_message),
                        null
                    )*/
                    setupReportRecyclerView()
                }
            }

            override fun onFailure(call: Call<List<ReportListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                CommonMethods.writeLog("[" + this.javaClass.simpleName + "] *ERROR* IN \$callReportListApi()$ :: onFailure = " + t.message.toString())
                setupReportRecyclerView()
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

    private fun setupReportRecyclerView() {
        reportAdapter = ReportListAdapter(reportList)
        binding.rvReport.adapter = reportAdapter
        binding.rvReport.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
    }

    inner class ReportListAdapter(
        private val reportList: ArrayList<ReportListResponse>
    ) : RecyclerView.Adapter<ReportListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemReportBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return reportList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val dashboardData = reportList[position]
            holder.bind(dashboardData)
        }

        inner class ViewHolder(private val binding: ItemReportBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(report: ReportListResponse) {
                binding.tvReportName.text = report.reportName

                binding.llMain.setOnClickListener {

                    if (report.webUrl.isNotEmpty()) {
                        mActivity.addFragment(
                            WebViewReportFragment.newInstance(report.reportName, report.webUrl),
                            addToBackStack = true,
                            ignoreIfCurrent = true, AnimationType.fadeInfadeOut
                        )
                    } else {
                        if (report.isDynamicReport) {
                            mActivity.addFragment(
                                DashboardDrillFragment.newInstance(
                                    false,
                                    DashboardListResponse(),
                                    DashboardDrillResponse(),
                                    report.storeProcedureName,
                                    report.reportSetupId,
                                    arrayListOf(),
                                    report.reportName,
                                    report.filter,
                                    report.reportGroupBy,
                                    true,
                                    productFilter = report.productFilter
                                ), true, true, AnimationType.fadeInfadeOut
                            )
                        } else {
                            when (report.reportName) {
                                getString(R.string.attendance_report) -> {
                                    mActivity.addFragment(
                                        AttendanceFragment.newInstance(true),
                                        addToBackStack = true,
                                        ignoreIfCurrent = true,
                                        animationType = AnimationType.fadeInfadeOut
                                    )
                                }

                                getString(R.string.trip_report) -> {
                                    mActivity.addFragment(
                                        TripReportFragment(),
                                        addToBackStack = true,
                                        ignoreIfCurrent = true,
                                        animationType = AnimationType.fadeInfadeOut
                                    )
                                }

                                getString(R.string.trip_summery_report) -> {
                                    mActivity.addFragment(
                                        TripSummeryReportFragment(),
                                        addToBackStack = true,
                                        ignoreIfCurrent = true,
                                        animationType = AnimationType.fadeInfadeOut
                                    )
                                }

                                getString(R.string.visit_report) -> {
                                    mActivity.addFragment(
                                        VisitReportFragment(),
                                        addToBackStack = true,
                                        ignoreIfCurrent = true,
                                        animationType = AnimationType.fadeInfadeOut
                                    )
                                }
                            }
                        }
                    }
                }
                binding.executePendingBindings()
            }
        }
    }

}