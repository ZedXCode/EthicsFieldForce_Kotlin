package ethicstechno.com.fieldforce.ui.fragments.dashboard

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
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentDashboardBinding
import ethicstechno.com.fieldforce.databinding.ItemDashboardBinding
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardDrillResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardListResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.showToastMessage
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : HomeBaseFragment(), View.OnClickListener {

    lateinit var dashboardBinding: FragmentDashboardBinding
    var dashboardList: ArrayList<DashboardListResponse> = arrayListOf()

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

        callDashboardListApi()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgMenu -> mActivity.openDrawer()
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

        val dashboardListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getDashboardList(dashboardListReq)

        dashboardListCall?.enqueue(object : Callback<List<DashboardListResponse>> {
            override fun onResponse(
                call: Call<List<DashboardListResponse>>,
                response: Response<List<DashboardListResponse>>
            ) {
                CommonMethods.hideLoading()
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
        mActivity.bottomVisible()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded && !hidden) {
            mActivity.bottomVisible()
            dashboardBinding.rvDashboard.isEnabled = true
        }
    }

    private fun setupDashboardAdapter() {
        val dashBoardAdapter = DashboardAdapter(dashboardList)
        dashboardBinding.rvDashboard.adapter = dashBoardAdapter
        dashboardBinding.rvDashboard.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
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
                binding.tvHeader.text = dashboardData.reportName
                binding.tvLine1.text = dashboardData.titleLine1
                binding.tvLine2.text = dashboardData.titleLine2
                binding.tvLine3.text = dashboardData.titleLine3
                binding.tvLine4.text = dashboardData.titleLine4
                binding.llMain.setOnClickListener {
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