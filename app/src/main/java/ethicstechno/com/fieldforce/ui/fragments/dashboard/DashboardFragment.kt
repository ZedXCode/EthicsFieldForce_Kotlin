package ethicstechno.com.fieldforce.ui.fragments.dashboard

import AnimationType
import addFragment
import android.graphics.Color
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
import ethicstechno.com.fieldforce.models.MoreOptionMenuListResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardDrillResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardListResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.ExpenseListFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.leave.LeaveApplicationListFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.order_entry.OrderEntryListFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.quotation.QuotationEntryListFragment
import ethicstechno.com.fieldforce.utils.APPROVAL_MODULE
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.showToastMessage
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.EXPENSE_APPROVAL_MODULE
import ethicstechno.com.fieldforce.utils.EXPENSE_APPROVAL_PRINT
import ethicstechno.com.fieldforce.utils.EXPENSE_ENTRY_MODULE
import ethicstechno.com.fieldforce.utils.EXPENSE_ENTRY_PRINT
import ethicstechno.com.fieldforce.utils.FORM_ID_EXPENSE_ENTRY_NUMBER
import ethicstechno.com.fieldforce.utils.FORM_ID_LEAVE_APPLICATION_ENTRY_NUMBER
import ethicstechno.com.fieldforce.utils.FORM_ID_ORDER_ENTRY_NUMBER
import ethicstechno.com.fieldforce.utils.FORM_ID_QUOTATION_ENTRY_NUMBER
import ethicstechno.com.fieldforce.utils.INQUIRY_MODULE
import ethicstechno.com.fieldforce.utils.INQUIRY_PRINT
import ethicstechno.com.fieldforce.utils.LEAVE_APPLICATION_MODULE
import ethicstechno.com.fieldforce.utils.LEAVE_APPLICATION_PRINT
import ethicstechno.com.fieldforce.utils.LEAVE_APPROVAL_MODULE
import ethicstechno.com.fieldforce.utils.LEAVE_APPROVAL_PRINT
import ethicstechno.com.fieldforce.utils.MENU_APPROVAL
import ethicstechno.com.fieldforce.utils.MENU_EXPENSE_APPROVAL
import ethicstechno.com.fieldforce.utils.MENU_EXPENSE_ENTRY
import ethicstechno.com.fieldforce.utils.MENU_INQUIRY
import ethicstechno.com.fieldforce.utils.MENU_LEAVE_APPLICATION
import ethicstechno.com.fieldforce.utils.MENU_LEAVE_APPROVAL
import ethicstechno.com.fieldforce.utils.MENU_ORDER_ENTRY
import ethicstechno.com.fieldforce.utils.MENU_PARTY_DEALER
import ethicstechno.com.fieldforce.utils.MENU_QUOTATION
import ethicstechno.com.fieldforce.utils.MENU_TOUR_PLAN
import ethicstechno.com.fieldforce.utils.MENU_VISIT
import ethicstechno.com.fieldforce.utils.ORDER_ENTRY_MODULE
import ethicstechno.com.fieldforce.utils.ORDER_ENTRY_PRINT
import ethicstechno.com.fieldforce.utils.PARTY_DEALER_MODULE
import ethicstechno.com.fieldforce.utils.PARTY_DEALER_PRINT
import ethicstechno.com.fieldforce.utils.QUOTATION_MODULE
import ethicstechno.com.fieldforce.utils.QUOTATION_PRINT
import ethicstechno.com.fieldforce.utils.TOUR_PLAN_MODULE
import ethicstechno.com.fieldforce.utils.TOUR_PLAN_PRINT
import ethicstechno.com.fieldforce.utils.VISIT_MODULE
import ethicstechno.com.fieldforce.utils.VISIT_PRINT
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : HomeBaseFragment(), View.OnClickListener {

    lateinit var dashboardBinding: FragmentDashboardBinding
    var dashboardList: ArrayList<DashboardListResponse> = arrayListOf()

    var statusColor = ""

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
        callMoreOptionList()
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

    private fun callMoreOptionList() {
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
            ?.getMoreOptionMenuList(dashboardListReq)

        dashboardListCall?.enqueue(object : Callback<List<MoreOptionMenuListResponse>> {
            override fun onResponse(
                call: Call<List<MoreOptionMenuListResponse>>,
                response: Response<List<MoreOptionMenuListResponse>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { moreOptionMenuList ->

                        moreOptionMenuList.forEach { option ->
                            when (option.formName) {
                                MENU_PARTY_DEALER -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        PARTY_DEALER_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        PARTY_DEALER_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_VISIT -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        VISIT_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        VISIT_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_TOUR_PLAN -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        TOUR_PLAN_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        TOUR_PLAN_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_EXPENSE_ENTRY -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        EXPENSE_ENTRY_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        EXPENSE_ENTRY_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_EXPENSE_APPROVAL -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        EXPENSE_APPROVAL_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        EXPENSE_APPROVAL_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_LEAVE_APPLICATION -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        LEAVE_APPLICATION_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        LEAVE_APPLICATION_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_LEAVE_APPROVAL -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        LEAVE_APPROVAL_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        LEAVE_APPROVAL_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_INQUIRY -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        INQUIRY_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        INQUIRY_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_QUOTATION -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        QUOTATION_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        QUOTATION_PRINT,
                                        option.allowPrint
                                    )
                                }

                                MENU_ORDER_ENTRY -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        ORDER_ENTRY_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        ORDER_ENTRY_PRINT,
                                        option.allowPrint
                                    )
                                }
                                MENU_APPROVAL -> {
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        APPROVAL_MODULE,
                                        option.allowRights
                                    )
                                    AppPreference.saveBooleanPreference(
                                        mActivity,
                                        APPROVAL_MODULE,
                                        option.allowPrint
                                    )
                                }
                            }
                        }
                    }

                }
                CommonMethods.hideLoading()
            }

            override fun onFailure(call: Call<List<MoreOptionMenuListResponse>>, t: Throwable) {
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


    override fun onResume() {
        super.onResume()
        mActivity.bottomVisible()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded && !hidden) {
            mActivity.bottomVisible()
            dashboardBinding.rvDashboard.isEnabled = true
            callDashboardListApi()
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
                Log.d("VIEWTYPE===>", "" + dashboardData.viewType)
                // Log.d("VIEWCOLOR===>",""+dashboardData.color4)

                dashboardList.forEach {
                    (dashboardData)
                    Log.d("LISTDATA===>", "" + dashboardList.size)
                    if (dashboardData.viewType == 1) {//For New Design
                        Log.d("VIEW===>", "view1")
                        binding.liView1.visibility = View.VISIBLE
                        binding.liView2.visibility = View.GONE
                        binding.liView3.visibility = View.GONE
                        binding.liView4.visibility = View.GONE
                        //binding.llMain.visibility = View.GONE

                        binding.tvValue2.text = dashboardData.value2
                        binding.tvTitleLine1.text = dashboardData.titleLine1
                        binding.tvValue1.text = dashboardData.value1

                        // statusColor = dashboardData.color4

                        val colors = dashboardData.color4.split("|")
                        if (colors[1].isEmpty()) {
                            binding.liView1.setBackgroundColor(Color.parseColor("#FFBCC8"))
                        } else {
                            binding.liView1.setBackgroundColor(Color.parseColor(colors[1]))
                            Log.d("VIEWCOLOR2===>", "" + dashboardData.color4)
                        }

                        val colors1 = dashboardData.color1.split("|")
                        if (colors1[0].isEmpty()) {
                            binding.tvValue2.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvValue2.setTextColor(Color.parseColor(colors1[0]))
                        }

                        val colors2 = dashboardData.color2.split("|")
                        if (colors2[0].isEmpty()) {
                            binding.tvValue1.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvValue1.setTextColor(Color.parseColor(colors2[0]))
                        }

                    } else if (dashboardData.viewType == 2) {//OLD View
                        Log.d("VIEW===>", "view2")
                        binding.liView1.visibility = View.GONE
                        binding.liView2.visibility = View.VISIBLE
                        binding.liView3.visibility = View.GONE
                        binding.liView4.visibility = View.GONE
                        //binding.llMain.visibility = View.GONE

                        binding.tvTitleLine2.text = dashboardData.reportName
                        binding.tvView2Value1.text = dashboardData.value1
                        binding.tvView2TitleLine1.text = dashboardData.titleLine1
                        binding.tvView2Value2.text = dashboardData.value2
                        binding.tvView2TitleLine2.text = dashboardData.titleLine2
                        binding.tvView2Value3.text = dashboardData.value3
                        binding.tvView2Value4.text = dashboardData.value4
                        binding.tvView2TitleLine3.text = dashboardData.titleLine3
                        binding.tvView2TitleLine4.text = dashboardData.titleLine4

                        val colors4 = dashboardData.color4.split("|")
                        if (colors4[1].isEmpty()) {
                            binding.liView2.setBackgroundColor(Color.parseColor("#FFBCC8"))
                        } else {
                            binding.liView2.setBackgroundColor(Color.parseColor(colors4[1]))
                            Log.d("VIEWCOLOR2===>", "" + dashboardData.color4)
                        }
                        if (colors4[0].isEmpty()) {
                            binding.tvView2Value4.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView2Value4.setTextColor(Color.parseColor(colors4[0]))
                        }

                        val colors1 = dashboardData.color1.split("|")
                        if (colors1[0].isEmpty()) {
                            binding.tvView2Value1.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView2Value1.setTextColor(Color.parseColor(colors1[0]))
                        }

                        val colors2 = dashboardData.color2.split("|")
                        if (colors2[0].isEmpty()) {
                            binding.tvView2Value2.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView2Value2.setTextColor(Color.parseColor(colors2[0]))
                        }

                        val colors3 = dashboardData.color3.split("|")
                        if (colors3[0].isEmpty()) {
                            binding.tvView2Value3.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView2Value3.setTextColor(Color.parseColor(colors3[0]))
                        }
                    } else if (dashboardData.viewType == 3) {
                        Log.d("VIEW===>", "view3")
                        binding.liView1.visibility = View.GONE
                        binding.liView2.visibility = View.GONE
                        binding.liView3.visibility = View.VISIBLE
                        binding.liView4.visibility = View.GONE
                        //binding.llMain.visibility = View.GONE

                        binding.tvTitleLine3.text = dashboardData.reportName
                        binding.tvView3TitleLine1.text = dashboardData.titleLine1
                        binding.tvView3Value1.text = dashboardData.value1
                        binding.tvView3TitleLine2.text = dashboardData.titleLine2
                        binding.tvView3Value2.text = dashboardData.value2
                        binding.tvView3TitleLine3.text = dashboardData.titleLine3
                        binding.tvView3Value3.text = dashboardData.value3
                        binding.tvView3TitleLine4.text = dashboardData.titleLine4
                        binding.tvView3Value4.text = dashboardData.value4


                        val color1 = dashboardData.color1.split("|")
                        if (color1[0].isEmpty()) {
                            binding.tvView3TitleLine1.setTextColor(Color.parseColor("#FF000000"))
                            binding.tvView3Value1.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView3Value1.setTextColor(Color.parseColor(color1[0]))
                            binding.tvView3TitleLine1.setTextColor(Color.parseColor(color1[0]))
                        }

                        if (color1[1].isEmpty()) {
                            binding.lvView3layout1.setBackgroundColor(Color.parseColor("#FFBCC8"))
                        } else {
                            binding.lvView3layout1.setBackgroundColor(Color.parseColor(color1[1]))
                        }

                        val color2 = dashboardData.color2.split("|")
                        if (color2[0].isEmpty()) {
                            binding.tvView3TitleLine2.setTextColor(Color.parseColor("#FF000000"))
                            binding.tvView3Value2.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView3Value2.setTextColor(Color.parseColor(color2[0]))
                            binding.tvView3TitleLine2.setTextColor(Color.parseColor(color2[0]))
                        }

                        if (color2[1].isEmpty()) {
                            binding.lvView3layout2.setBackgroundColor(Color.parseColor("#FFFFD8"))
                        } else {
                            binding.lvView3layout2.setBackgroundColor(Color.parseColor(color2[1]))
                        }

                        val color3 = dashboardData.color3.split("|")
                        if (color3[0].isEmpty()) {
                            binding.tvView3TitleLine3.setTextColor(Color.parseColor("#FF000000"))
                            binding.tvView3Value3.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView3Value3.setTextColor(Color.parseColor(color3[0]))
                            binding.tvView3TitleLine3.setTextColor(Color.parseColor(color3[0]))
                        }

                        if (color3[1].isEmpty()) {
                            binding.lvView3layout3.setBackgroundColor(Color.parseColor("#EAEBFF"))
                        } else {
                            binding.lvView3layout3.setBackgroundColor(Color.parseColor(color3[1]))
                        }

                        val colors4 = dashboardData.color4.split("|")
                        if (colors4[0].isEmpty()) {
                            binding.tvView3TitleLine4.setTextColor(Color.parseColor("#FF000000"))
                            binding.tvView3Value4.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView3Value4.setTextColor(Color.parseColor(colors4[0]))
                            binding.tvView3TitleLine4.setTextColor(Color.parseColor(colors4[0]))
                        }

                        if (colors4[1].isEmpty()) {
                            binding.lvView3layout4.setBackgroundColor(Color.parseColor("#E0FEFE"))
                        } else {
                            binding.lvView3layout4.setBackgroundColor(Color.parseColor(colors4[1]))
                        }
//                        if (colors4[1].isEmpty()){
//                            binding.liView3.setBackgroundColor(Color.parseColor("#FFBCC8"))
//                        }else{
//                            binding.liView3.setBackgroundColor(Color.parseColor(colors4[1]))
//                            Log.d("VIEWCOLOR2===>",""+dashboardData.color4)
//                        }

                        if (dashboardData.titleLine1.isEmpty()) {
                            binding.tvView4TitleLine1.visibility = View.GONE
                        } else if (dashboardData.value1.isEmpty()) {
                            binding.tvView4Value1.visibility = View.GONE
                        } else if (dashboardData.titleLine2.isEmpty()) {
                            binding.tvView4TitleLine2.visibility = View.GONE
                        } else if (dashboardData.value2.isEmpty()) {
                            binding.tvView4Value1.visibility = View.GONE
                        } else if (dashboardData.titleLine3.isEmpty()) {
                            binding.tvView4TitleLine3.visibility = View.GONE
                        } else if (dashboardData.value3.isEmpty()) {
                            binding.tvView4Value3.visibility = View.GONE
                        } else if (dashboardData.titleLine4.isEmpty()) {
                            binding.tvView4TitleLine4.visibility = View.GONE
                        } else if (dashboardData.value4.isEmpty()) {
                            binding.tvView4Value4.visibility = View.GONE
                        }

                        if (dashboardData.titleLine1.isEmpty() && dashboardData.value1.isEmpty()) {
                            binding.lvView3layout1.visibility = View.GONE
                        } else if (dashboardData.titleLine2.isEmpty() && dashboardData.value2.isEmpty()) {
                            binding.lvView3layout2.visibility = View.GONE
                        } else if (dashboardData.titleLine4.isEmpty() && dashboardData.value4.isEmpty()) {
                            binding.lvView3layout4.visibility = View.GONE
                        }


                        if (dashboardData.titleLine3.isEmpty() && dashboardData.value3.isEmpty()) {
                            binding.lvView3layout3.visibility = View.GONE
                        }
                    } else if (dashboardData.viewType == 4) {
                        Log.d("VIEW===>", "view4")
                        binding.liView1.visibility = View.GONE
                        binding.liView2.visibility = View.GONE
                        binding.liView3.visibility = View.GONE
                        binding.liView4.visibility = View.VISIBLE
                        //binding.llMain.visibility = View.GONE

                        binding.tvTitleLine4.text = dashboardData.reportName
                        binding.tvView4TitleLine1.text = dashboardData.titleLine1
                        binding.tvView4Value1.text = dashboardData.value1
                        binding.tvView4TitleLine2.text = dashboardData.titleLine2
                        binding.tvView4Value2.text = dashboardData.value2
                        binding.tvView4TitleLine3.text = dashboardData.titleLine3
                        binding.tvView4Value3.text = dashboardData.value3
                        binding.tvView4TitleLine4.text = dashboardData.titleLine4
                        binding.tvView4Value4.text = dashboardData.value4

                        val colors1 = dashboardData.color1.split("|")
                        if (colors1[0].isEmpty()) {
                            binding.tvView4Value1.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView4Value1.setTextColor(Color.parseColor(colors1[0]))
                        }

                        if (colors1[1].isEmpty()) {
                            binding.tvView4TitleLine1.setBackgroundColor(Color.parseColor("#FFBCC8"))
                            binding.tvView4Value1.setBackgroundColor(Color.parseColor("#FFBCC8"))

                        } else {
                            binding.tvView4TitleLine1.setBackgroundColor(Color.parseColor(colors1[1]))
                            binding.tvView4Value1.setBackgroundColor(Color.parseColor(colors1[1]))
                        }


                        val colors2 = dashboardData.color2.split("|")
                        if (colors2[0].isEmpty()) {
                            binding.tvView4Value2.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView4Value2.setTextColor(Color.parseColor(colors2[0]))
                        }

                        if (colors2[1].isEmpty()) {
                            binding.tvView4TitleLine2.setBackgroundColor(Color.parseColor("#FFFFD8"))
                            binding.tvView4Value2.setBackgroundColor(Color.parseColor("#FFFFD8"))

                        } else {
                            binding.tvView4TitleLine2.setBackgroundColor(Color.parseColor(colors2[1]))
                            binding.tvView4Value2.setBackgroundColor(Color.parseColor(colors2[1]))
                        }

                        val colors3 = dashboardData.color3.split("|")
                        if (colors3[0].isEmpty()) {
                            binding.tvView4Value3.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView4Value3.setTextColor(Color.parseColor(colors3[0]))
                        }

                        if (colors3[1].isEmpty()) {
                            binding.tvView4TitleLine3.setBackgroundColor(Color.parseColor("#EAEBFF"))
                            binding.tvView4Value3.setBackgroundColor(Color.parseColor("#EAEBFF"))

                        } else {
                            binding.tvView4TitleLine3.setBackgroundColor(Color.parseColor(colors3[1]))
                            binding.tvView4Value3.setBackgroundColor(Color.parseColor(colors3[1]))
                        }

                        val colors4 = dashboardData.color4.split("|")
                        if (colors4[0].isEmpty()) {
                            binding.tvView4Value4.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView4Value4.setTextColor(Color.parseColor(colors4[0]))
                        }

                        if (colors4[1].isEmpty()) {
                            binding.tvView4TitleLine4.setBackgroundColor(Color.parseColor("#E0FEFE"))
                            binding.tvView4Value4.setBackgroundColor(Color.parseColor("#E0FEFE"))

                        } else {
                            binding.tvView4TitleLine4.setBackgroundColor(Color.parseColor(colors4[1]))
                            binding.tvView4Value4.setBackgroundColor(Color.parseColor(colors4[1]))
                        }

//                        if (colors4[1].isEmpty()){
//                            binding.liView4.setBackgroundColor(Color.parseColor("#FFBCC8"))
//                        }else{
//                            binding.liView4.setBackgroundColor(Color.parseColor(colors4[1]))
//                            Log.d("VIEWCOLOR2===>",""+dashboardData.color4)
//                        }

                        if (dashboardData.titleLine1.isEmpty()) {
                            binding.tvView4TitleLine1.visibility = View.GONE
                        } else if (dashboardData.value1.isEmpty()) {
                            binding.tvView4Value1.visibility = View.GONE
                        } else if (dashboardData.titleLine2.isEmpty()) {
                            binding.tvView4TitleLine2.visibility = View.GONE
                        } else if (dashboardData.value2.isEmpty()) {
                            binding.tvView4Value1.visibility = View.GONE
                        } else if (dashboardData.titleLine3.isEmpty()) {
                            binding.tvView4TitleLine3.visibility = View.GONE
                        } else if (dashboardData.value3.isEmpty()) {
                            binding.tvView4Value3.visibility = View.GONE
                        } else if (dashboardData.titleLine4.isEmpty()) {
                            binding.tvView4TitleLine4.visibility = View.GONE
                        } else if (dashboardData.value4.isEmpty()) {
                            binding.tvView4Value4.visibility = View.GONE
                        }
                    } else {
                        Log.d("VIEW===>", "view4")
                        binding.liView1.visibility = View.GONE
                        binding.liView2.visibility = View.GONE
                        binding.liView3.visibility = View.GONE
                        binding.liView4.visibility = View.VISIBLE

                        binding.tvView4Value1.visibility = View.GONE
                        binding.tvView4Value2.visibility = View.GONE
                        binding.tvView4Value3.visibility = View.GONE
                        binding.tvView4Value4.visibility = View.GONE
                        //binding.llMain.visibility = View.GONE

                        binding.tvTitleLine4.text = dashboardData.reportName
                        binding.tvView4TitleLine1.text = dashboardData.titleLine1
                        binding.tvView4Value1.text = dashboardData.value1
                        binding.tvView4TitleLine2.text = dashboardData.titleLine2
                        binding.tvView4Value2.text = dashboardData.value2
                        binding.tvView4TitleLine3.text = dashboardData.titleLine3
                        binding.tvView4Value3.text = dashboardData.value3
                        binding.tvView4TitleLine4.text = dashboardData.titleLine4
                        binding.tvView4Value4.text = dashboardData.value4

                        val colors1 = dashboardData.color1.split("|")
                        if (colors1[0].isEmpty()) {
                            binding.tvView4Value1.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView4Value1.setTextColor(Color.parseColor(colors1[0]))
                        }

                        if (colors1[1].isEmpty()) {
                            binding.tvView4TitleLine1.setBackgroundColor(Color.parseColor("#FFBCC8"))
                            binding.tvView4Value1.setBackgroundColor(Color.parseColor("#FFBCC8"))

                        } else {
                            binding.tvView4TitleLine1.setBackgroundColor(Color.parseColor(colors1[1]))
                            binding.tvView4Value1.setBackgroundColor(Color.parseColor(colors1[1]))
                        }


                        val colors2 = dashboardData.color2.split("|")
                        if (colors2[0].isEmpty()) {
                            binding.tvView4Value2.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView4Value2.setTextColor(Color.parseColor(colors2[0]))
                        }

                        if (colors2[1].isEmpty()) {
                            binding.tvView4TitleLine2.setBackgroundColor(Color.parseColor("#FFFFD8"))
                            binding.tvView4Value2.setBackgroundColor(Color.parseColor("#FFFFD8"))

                        } else {
                            binding.tvView4TitleLine2.setBackgroundColor(Color.parseColor(colors2[1]))
                            binding.tvView4Value2.setBackgroundColor(Color.parseColor(colors2[1]))
                        }

                        val colors3 = dashboardData.color3.split("|")
                        if (colors3[0].isEmpty()) {
                            binding.tvView4Value3.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView4Value3.setTextColor(Color.parseColor(colors3[0]))
                        }

                        if (colors3[1].isEmpty()) {
                            binding.tvView4TitleLine3.setBackgroundColor(Color.parseColor("#EAEBFF"))
                            binding.tvView4Value3.setBackgroundColor(Color.parseColor("#EAEBFF"))

                        } else {
                            binding.tvView4TitleLine3.setBackgroundColor(Color.parseColor(colors3[1]))
                            binding.tvView4Value3.setBackgroundColor(Color.parseColor(colors3[1]))
                        }

                        val colors4 = dashboardData.color4.split("|")
                        if (colors4[0].isEmpty()) {
                            binding.tvView4Value4.setTextColor(Color.parseColor("#FF000000"))
                        } else {
                            binding.tvView4Value4.setTextColor(Color.parseColor(colors4[0]))
                        }

                        if (colors4[1].isEmpty()) {
                            binding.tvView4TitleLine4.setBackgroundColor(Color.parseColor("#E0FEFE"))
                            binding.tvView4Value4.setBackgroundColor(Color.parseColor("#E0FEFE"))

                        } else {
                            binding.tvView4TitleLine4.setBackgroundColor(Color.parseColor(colors4[1]))
                            binding.tvView4Value4.setBackgroundColor(Color.parseColor(colors4[1]))
                        }

//                        if (colors4[1].isEmpty()){
//                            binding.liView4.setBackgroundColor(Color.parseColor("#FFBCC8"))
//                        }else{
//                            binding.liView4.setBackgroundColor(Color.parseColor(colors4[1]))
//                            Log.d("VIEWCOLOR2===>",""+dashboardData.color4)
//                        }

                        if (dashboardData.titleLine1.isEmpty()) {
                            binding.tvView4TitleLine1.visibility = View.GONE
                        } else if (dashboardData.value1.isEmpty()) {
                            binding.tvView4Value1.visibility = View.GONE
                        } else if (dashboardData.titleLine2.isEmpty()) {
                            binding.tvView4TitleLine2.visibility = View.GONE
                        } else if (dashboardData.value2.isEmpty()) {
                            binding.tvView4Value1.visibility = View.GONE
                        } else if (dashboardData.titleLine3.isEmpty()) {
                            binding.tvView4TitleLine3.visibility = View.GONE
                        } else if (dashboardData.value3.isEmpty()) {
                            binding.tvView4Value3.visibility = View.GONE
                        } else if (dashboardData.titleLine4.isEmpty()) {
                            binding.tvView4TitleLine4.visibility = View.GONE
                        } else if (dashboardData.value4.isEmpty()) {
                            binding.tvView4Value4.visibility = View.GONE
                        }
                    }

                }

//                if (dashboardData.viewType == 1)
//                {
//                    Log.d("VIEW===>","view1")
//                    binding.liView1.visibility = View.VISIBLE
//                    binding.liView2.visibility = View.GONE
//                    binding.liView3.visibility = View.GONE
//                    binding.liView4.visibility = View.GONE
//                    //binding.llMain.visibility = View.GONE
//
//                    binding.tvValue2.text = dashboardData.value2
//                    binding.tvTitleLine1.text = dashboardData.titleLine1
//                    binding.tvValue1.text = dashboardData.value1
//
//                    if (dashboardData.color4.isNotEmpty()){
//                        binding.tvValue2.setTextColor(Color.parseColor(dashboardData.color4))
//                    }else{
//                        binding.tvValue2.setTextColor(Color.parseColor("#FF000000"))
//                    }
//
//                    if (dashboardData.color1.isEmpty()){
//                        binding.tvValue2.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvValue2.setTextColor(Color.parseColor(dashboardData.color1))
//                    }
//
//                    if (dashboardData.color2.isEmpty()){
//                        binding.tvValue1.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvValue1.setTextColor(Color.parseColor(dashboardData.color2))
//                    }
//
//                }
//                else if (dashboardData.viewType == 2){
//                    Log.d("VIEW===>","view2")
//                    binding.liView1.visibility = View.GONE
//                    binding.liView2.visibility = View.VISIBLE
//                    binding.liView3.visibility = View.GONE
//                    binding.liView4.visibility = View.GONE
//                    //binding.llMain.visibility = View.GONE
//
//                    binding.tvTitleLine2.text = dashboardData.reportName
//                    binding.tvView2Value1.text = dashboardData.value1
//                    binding.tvView2TitleLine1.text = dashboardData.titleLine1
//                    binding.tvView2Value2.text = dashboardData.value2
//                    binding.tvView2TitleLine2.text = dashboardData.titleLine2
//                    binding.tvView2Value3.text = dashboardData.value3
//                    binding.tvView2Value4.text = dashboardData.value4
//                    binding.tvView2TitleLine3.text = dashboardData.titleLine3
//                    binding.tvView2TitleLine4.text = dashboardData.titleLine4
//
//                    if (dashboardData.color1.isEmpty()){
//                        binding.tvView2Value1.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView2Value1.setTextColor(Color.parseColor(dashboardData.color1))
//                    }
//
//                    if (dashboardData.color2.isEmpty()){
//                        binding.tvView2Value2.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView2Value2.setTextColor(Color.parseColor(dashboardData.color2))
//                    }
//
//                    if (dashboardData.color3.isEmpty()){
//                        binding.tvView2Value3.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView2Value3.setTextColor(Color.parseColor(dashboardData.color3))
//                    }
//
//                    if (dashboardData.color4.isEmpty()){
//                        binding.tvView2TitleLine4.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView2TitleLine4.setTextColor(Color.parseColor(dashboardData.color4))
//                    }
//                }
//                else if (dashboardData.viewType == 3){
//                    Log.d("VIEW===>","view3")
//                    binding.liView1.visibility = View.GONE
//                    binding.liView2.visibility = View.GONE
//                    binding.liView3.visibility = View.VISIBLE
//                    binding.liView4.visibility = View.GONE
//                    //binding.llMain.visibility = View.GONE
//
//                    binding.tvTitleLine3.text = dashboardData.reportName
//                    binding.tvView3TitleLine1.text = dashboardData.titleLine1
//                    binding.tvView3Value1.text = dashboardData.value1
//                    binding.tvView3TitleLine2.text = dashboardData.titleLine2
//                    binding.tvView3Value2.text = dashboardData.value2
//                    binding.tvView3TitleLine3.text = dashboardData.titleLine3
//                    binding.tvView3Value3.text = dashboardData.value3
//                    binding.tvView3TitleLine4.text = dashboardData.titleLine4
//                    binding.tvView3Value4.text = dashboardData.value4
//
//                    if (dashboardData.color1.isEmpty()){
//                        binding.tvView3Value1.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView3Value1.setTextColor(Color.parseColor(dashboardData.color1))
//                    }
//
//                    if (dashboardData.color2.isEmpty()){
//                        binding.tvView3Value2.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView3Value2.setTextColor(Color.parseColor(dashboardData.color2))
//                    }
//
//                    if (dashboardData.color3.isEmpty()){
//                        binding.tvView3Value3.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView3Value3.setTextColor(Color.parseColor(dashboardData.color3))
//                    }
//
//                    if (dashboardData.color4.isEmpty()){
//                        binding.tvView3Value4.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView3Value4.setTextColor(Color.parseColor(dashboardData.color4))
//                    }
//                }
//                else {
//                    Log.d("VIEW===>","view4")
//                    binding.liView1.visibility = View.GONE
//                    binding.liView2.visibility = View.GONE
//                    binding.liView3.visibility = View.GONE
//                    binding.liView4.visibility = View.VISIBLE
//                    //binding.llMain.visibility = View.GONE
//
//                    binding.tvTitleLine4.text = dashboardData.reportName
//                    binding.tvView4TitleLine1.text = dashboardData.titleLine1
//                    binding.tvView4Value1.text = dashboardData.value1
//                    binding.tvView4TitleLine2.text = dashboardData.titleLine2
//                    binding.tvView4Value2.text = dashboardData.value2
//                    binding.tvView4TitleLine3.text = dashboardData.titleLine3
//                    binding.tvView4Value3.text = dashboardData.value3
//                    binding.tvView4TitleLine4.text = dashboardData.titleLine4
//                    binding.tvView4Value4.text = dashboardData.value4
//
//                    if (dashboardData.color1.isEmpty()){
//                        binding.tvView4Value1.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView4Value1.setTextColor(Color.parseColor(dashboardData.color1))
//                    }
//
//                    if (dashboardData.color2.isEmpty()){
//                        binding.tvView4Value2.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView4Value2.setTextColor(Color.parseColor(dashboardData.color2))
//                    }
//
//                    if (dashboardData.color3.isEmpty()){
//                        binding.tvView4Value3.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView4Value3.setTextColor(Color.parseColor(dashboardData.color3))
//                    }
//
//                    if (dashboardData.color4.isEmpty()){
//                        binding.tvView4Value4.setTextColor(Color.parseColor("#FF000000"))
//                    }
//                    else{
//                        binding.tvView4Value4.setTextColor(Color.parseColor(dashboardData.color4))
//                    }
//
//                    if (dashboardData.titleLine1.isEmpty()){
//                        binding.tvView4TitleLine1.visibility = View.GONE
//                    }
//                    else if (dashboardData.value1.isEmpty()){
//                        binding.tvView4Value1.visibility = View.GONE
//                    }
//                    else if (dashboardData.titleLine2.isEmpty()){
//                        binding.tvView4TitleLine2.visibility = View.GONE
//                    }
//                    else if (dashboardData.value2.isEmpty()){
//                        binding.tvView4Value1.visibility = View.GONE
//                    }
//                    else if (dashboardData.titleLine3.isEmpty()){
//                        binding.tvView4TitleLine3.visibility = View.GONE
//                    }
//                    else if (dashboardData.value3.isEmpty()){
//                        binding.tvView4Value3.visibility = View.GONE
//                    }
//                    else if (dashboardData.titleLine4.isEmpty()){
//                        binding.tvView4TitleLine4.visibility = View.GONE
//                    }
//                    else if (dashboardData.value4.isEmpty()){
//                        binding.tvView4Value4.visibility = View.GONE
//                    }
//                }

//                else{
//                    binding.liView1.visibility = View.GONE
//                    binding.liView2.visibility = View.GONE
//                    binding.liView3.visibility = View.GONE
//                    binding.liView4.visibility = View.GONE
//                    binding.llMain.visibility = View.VISIBLE
//
//                    binding.tvHeader.text = dashboardData.reportName
//                    binding.tvLine1.text = dashboardData.titleLine1
//                    binding.tvLine2.text = dashboardData.titleLine2
//                    binding.tvLine3.text = dashboardData.titleLine3
//                    binding.tvLine4.text = dashboardData.titleLine4
//                }
//                binding.tvHeader.text = dashboardData.reportName
//                binding.tvLine1.text = dashboardData.titleLine1
//                binding.tvLine2.text = dashboardData.titleLine2
//                binding.tvLine3.text = dashboardData.titleLine3
//                binding.tvLine4.text = dashboardData.titleLine4

//                binding.llMain.setOnClickListener {
//                    Log.e("TAG", "bind: DASHBOAR")
//                    if (mActivity.isDashboardVisible()) {
//                        mActivity.addFragment(
//                            DashboardDrillFragment.newInstance(
//                                false,
//                                dashboardData,
//                                DashboardDrillResponse(),
//                                dashboardData.storeProcedureName,
//                                dashboardData.reportSetupId, arrayListOf(),
//                                dashboardData.reportName,
//                                dashboardData.filter,
//                                dashboardData.reportGroupBy,
//                                startDate = "",
//                                endDate = ""
//                            ),
//                            addToBackStack = true,
//                            ignoreIfCurrent = true,
//                            animationType = AnimationType.fadeInfadeOut
//                        )
//                        dashboardBinding.rvDashboard.isEnabled = false
//                    }
//                }
                //binding.executePendingBindings()

                binding.liView1.setOnClickListener {
                    Log.e("TAG", "bind: DASHBOAR")
                    if (mActivity.isDashboardVisible()) {

                        if (dashboardData.reportType == "Authorization") {
                            when (dashboardData.value4) {
                                FORM_ID_ORDER_ENTRY_NUMBER -> {
                                    mActivity.addFragment(
                                        OrderEntryListFragment.newInstance(true),
                                        addToBackStack = true,
                                        ignoreIfCurrent = true,
                                        animationType = AnimationType.fadeInfadeOut
                                    )
                                }

                                FORM_ID_QUOTATION_ENTRY_NUMBER -> {
                                    mActivity.addFragment(
                                        QuotationEntryListFragment.newInstance(true),
                                        addToBackStack = true,
                                        ignoreIfCurrent = true,
                                        animationType = AnimationType.fadeInfadeOut
                                    )
                                }

                                FORM_ID_EXPENSE_ENTRY_NUMBER -> {
                                    mActivity.addFragment(
                                        ExpenseListFragment.newInstance(true),
                                        addToBackStack = true,
                                        ignoreIfCurrent = true,
                                        animationType = AnimationType.fadeInfadeOut
                                    )
                                }

                                FORM_ID_LEAVE_APPLICATION_ENTRY_NUMBER -> {
                                    mActivity.addFragment(
                                        LeaveApplicationListFragment.newInstance(true),
                                        addToBackStack = true,
                                        ignoreIfCurrent = true,
                                        animationType = AnimationType.fadeInfadeOut
                                    )
                                }
                            }
                        } else {

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
                        }
                        dashboardBinding.rvDashboard.isEnabled = false
                    }
                }
                // binding.executePendingBindings()

                binding.liView2.setOnClickListener {
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
                //.executePendingBindings()

                binding.liView3.setOnClickListener {
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
                //binding.executePendingBindings()

                binding.liView4.setOnClickListener {
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