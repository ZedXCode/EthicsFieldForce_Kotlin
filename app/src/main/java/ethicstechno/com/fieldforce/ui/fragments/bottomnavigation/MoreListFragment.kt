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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentMoreListBinding
import ethicstechno.com.fieldforce.databinding.ItemMoreBinding
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardDrillResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardListResponse
import ethicstechno.com.fieldforce.models.moreoption.DynamicMenuListResponse
import ethicstechno.com.fieldforce.models.moreoption.MoreModel
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.ui.fragments.dashboard.DashboardDrillFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.ExpenseListFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.approval.ApprovalFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.inquiry.InquiryEntryListFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.leave.LeaveApplicationListFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.order_entry.OrderEntryListFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.partydealer.PartyDealerListFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.quotation.QuotationEntryListFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.tourplan.TourPlanFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.visit.VisitListFragment
import ethicstechno.com.fieldforce.utils.APPROVAL_MODULE
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.EXPENSE_ENTRY_MODULE
import ethicstechno.com.fieldforce.utils.INQUIRY_MODULE
import ethicstechno.com.fieldforce.utils.LEAVE_APPLICATION_MODULE
import ethicstechno.com.fieldforce.utils.MORE_APPROVAL
import ethicstechno.com.fieldforce.utils.MORE_EXPENSE_ENTRY
import ethicstechno.com.fieldforce.utils.MORE_INQUIRY_ENTRY
import ethicstechno.com.fieldforce.utils.MORE_LEAVE_APPLICATION
import ethicstechno.com.fieldforce.utils.MORE_ORDER_ENTRY
import ethicstechno.com.fieldforce.utils.MORE_PARTY_DEALER
import ethicstechno.com.fieldforce.utils.MORE_QUOTATION_ENTRY
import ethicstechno.com.fieldforce.utils.MORE_TOUR_PLAN
import ethicstechno.com.fieldforce.utils.MORE_VISIT
import ethicstechno.com.fieldforce.utils.ORDER_ENTRY_MODULE
import ethicstechno.com.fieldforce.utils.PARTY_DEALER_MODULE
import ethicstechno.com.fieldforce.utils.QUOTATION_MODULE
import ethicstechno.com.fieldforce.utils.TOUR_PLAN_MODULE
import ethicstechno.com.fieldforce.utils.VISIT_MODULE
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MoreListFragment : HomeBaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentMoreListBinding
    var moreList: ArrayList<MoreModel> = arrayListOf()
    var dynamicMenuList: ArrayList<DynamicMenuListResponse> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_more_list, container, false)
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
        binding.toolbar.imgMenu.visibility = View.VISIBLE
        binding.toolbar.imgBack.visibility = View.GONE
        binding.toolbar.tvHeader.text = getString(R.string.more_options)
        binding.toolbar.imgMenu.setOnClickListener(this)
        createModelList()
        callDynamicMenuList()
    }

    private fun createModelList() {
        moreList.clear()

        val menuItems = listOf(
            Triple(
                MORE_PARTY_DEALER,
                getString(R.string.more_party_dealer),
                R.drawable.ic_menu_party_dealer
            ) to PARTY_DEALER_MODULE,
            Triple(
                MORE_VISIT,
                getString(R.string.more_visit),
                R.drawable.ic_menu_visit
            ) to VISIT_MODULE,
            Triple(
                MORE_TOUR_PLAN,
                getString(R.string.more_tour_plan),
                R.drawable.ic_menu_tour_plan
            ) to TOUR_PLAN_MODULE,
            Triple(
                MORE_INQUIRY_ENTRY,
                getString(R.string.more_inquiry_entry),
                R.drawable.ic_menu_inquiry
            ) to INQUIRY_MODULE,
            Triple(
                MORE_QUOTATION_ENTRY,
                getString(R.string.more_quotation_entry),
                R.drawable.ic_menu_quotation
            ) to QUOTATION_MODULE,
            Triple(
                MORE_ORDER_ENTRY,
                getString(R.string.more_order_entry),
                R.drawable.ic_menu_order
            ) to ORDER_ENTRY_MODULE,
            Triple(
                MORE_EXPENSE_ENTRY,
                getString(R.string.more_expense_entry),
                R.drawable.ic_menu_expense
            ) to EXPENSE_ENTRY_MODULE,
            //Triple(MORE_EXPENSE_APPROVAL, getString(R.string.more_expense_approval), R.drawable.ic_menu_approval) to EXPENSE_APPROVAL_MODULE,
            Triple(
                MORE_LEAVE_APPLICATION,
                getString(R.string.more_leave_application),
                R.drawable.ic_menu_leave
            ) to LEAVE_APPLICATION_MODULE,
            Triple(
                MORE_APPROVAL,
                getString(R.string.approval),
                R.drawable.ic_menu_approval
            ) to APPROVAL_MODULE
            //Triple(MORE_LEAVE_APPROVAL, getString(R.string.more_leave_approval), R.drawable.ic_menu_approval) to LEAVE_APPROVAL_MODULE
        )


        menuItems.forEach { (moreModel, key) ->
            if (AppPreference.getBooleanPreference(mActivity, key)) {
                moreList.add(MoreModel(moreModel.first, moreModel.second, moreModel.third))
            } else {
                moreList.remove(MoreModel(moreModel.first, moreModel.second, moreModel.third))
            }
        }
    }


    private fun setupMoreAdapter() {
        val attendanceAdapter = MoreAdapter(moreList)
        binding.rvMore.adapter = attendanceAdapter
        binding.rvMore.layoutManager =
            GridLayoutManager(mActivity, 3, RecyclerView.VERTICAL, false)
    }

    private fun callDynamicMenuList() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val dynamicMenuReq = JsonObject()
        dynamicMenuReq.addProperty("UserId", loginData.userId)

        CommonMethods.getBatteryPercentage(mActivity)

        val reportListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getDynamicMenuList(dynamicMenuReq)

        reportListCall?.enqueue(object : Callback<List<DynamicMenuListResponse>> {
            override fun onResponse(
                call: Call<List<DynamicMenuListResponse>>,
                response: Response<List<DynamicMenuListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            dynamicMenuList.clear()
                            dynamicMenuList.addAll(it)
                            for (i in dynamicMenuList) {
                                moreList.add(
                                    MoreModel(
                                        i.dynamicScreenSetupId,
                                        i.dynamicScreenName,
                                        R.drawable.ic_expense_entry
                                    )
                                )
                            }
                            setupMoreAdapter()
                        } else {
                            setupMoreAdapter()
                        }
                    }
                } else {
                    setupMoreAdapter()
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<List<DynamicMenuListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                setupMoreAdapter()
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


    inner class MoreAdapter(
        private val menuList: ArrayList<MoreModel>
    ) : RecyclerView.Adapter<MoreAdapter.ViewHolder>() {

        private var maxHeight = 0

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemMoreBinding.inflate(inflater, parent, false)

            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return menuList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val menuData = menuList[position]
            holder.bind(menuData)
        }

        inner class ViewHolder(private val binding: ItemMoreBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(moreData: MoreModel) {
                binding.tvMenuName.text = moreData.modelName
                binding.imgMenu.setImageResource(moreData.modelImage)

                binding.tvMenuName.post {
                    val height: Int = binding.tvMenuName.height
                    if (height > maxHeight) {
                        maxHeight = height // Store the tallest item height
                    }
                    // Apply the same height to all items
                    binding.tvMenuName.setMinHeight(maxHeight)
                }

                binding.cardAttendance.setOnClickListener {
                    when (moreData.modelName) {
                        getString(R.string.more_expense_entry) -> {
                            mActivity.addFragment(
                                ExpenseListFragment.newInstance(false),
                                addToBackStack = true,
                                ignoreIfCurrent = true,
                                animationType = AnimationType.fadeInfadeOut
                            )
                        }

                        getString(R.string.more_expense_approval) -> {
                            mActivity.addFragment(
                                ExpenseListFragment.newInstance(true),
                                addToBackStack = true,
                                ignoreIfCurrent = true,
                                animationType = AnimationType.fadeInfadeOut
                            )
                        }

                        getString(R.string.more_party_dealer) -> {
                            mActivity.addFragment(
                                PartyDealerListFragment(),
                                true,
                                true,
                                AnimationType.fadeInfadeOut
                            )
                        }

                        getString(R.string.more_visit) -> {
                            mActivity.addFragment(
                                VisitListFragment(),
                                addToBackStack = true,
                                ignoreIfCurrent = true,
                                animationType = AnimationType.fadeInfadeOut
                            )
                        }

                        getString(R.string.more_leave_application) -> {
                            mActivity.addFragment(
                                LeaveApplicationListFragment.newInstance(false),
                                addToBackStack = true,
                                ignoreIfCurrent = true,
                                animationType = AnimationType.fadeInfadeOut
                            )
                        }

                        getString(R.string.more_leave_approval) -> {
                            mActivity.addFragment(
                                LeaveApplicationListFragment.newInstance(true),
                                addToBackStack = true,
                                ignoreIfCurrent = true,
                                animationType = AnimationType.fadeInfadeOut
                            )
                        }

                        getString(R.string.more_tour_plan) -> {
                            mActivity.addFragment(
                                TourPlanFragment(),
                                addToBackStack = true,
                                ignoreIfCurrent = true,
                                animationType = AnimationType.fadeInfadeOut
                            )
                        }

                        getString(R.string.more_order_entry) -> {
                            mActivity.addFragment(
//                                OrderEntryListFragment(),
                                OrderEntryListFragment.newInstance(false),
                                addToBackStack = true,
                                ignoreIfCurrent = true,
                                animationType = AnimationType.fadeInfadeOut
                            )
                        }

                        getString(R.string.order_approval) -> {
                            mActivity.addFragment(
                                OrderEntryListFragment.newInstance(true),
                                addToBackStack = true,
                                ignoreIfCurrent = true,
                                animationType = AnimationType.fadeInfadeOut
                            )
                        }

                        getString(R.string.more_inquiry_entry) -> {
                            mActivity.addFragment(
                                InquiryEntryListFragment.newInstance(true),
                                addToBackStack = true,
                                ignoreIfCurrent = true,
                                animationType = AnimationType.fadeInfadeOut
                            )
                        }

                        getString(R.string.more_quotation_entry) -> {
                            mActivity.addFragment(
                                QuotationEntryListFragment.newInstance(false),
                                addToBackStack = true,
                                ignoreIfCurrent = true,
                                animationType = AnimationType.fadeInfadeOut
                            )
                        }

                        getString(R.string.approval) -> {
                            mActivity.addFragment(
                                ApprovalFragment.newInstance(true),
                                addToBackStack = true,
                                ignoreIfCurrent = true,
                                animationType = AnimationType.fadeInfadeOut
                            )
                        }


                        getString(R.string.more_payment_follow_up) -> {
                            gotoReportDrill(getString(R.string.more_payment_follow_up))
                            /* for(i in dynamicMenuList){
                                 if(i.dynamicScreenName == getString(R.string.more_payment_follow_up)){
                                     mActivity.addFragment(
                                         DashboardDrillFragment.newInstance(
                                             false,
                                             DashboardListResponse(),
                                             DashboardDrillResponse(),
                                             i.storeProcedureName,
                                             i.reportSetupId,
                                             arrayListOf(),
                                             i.reportName,
                                             i.filter,
                                             i.reportGroupBy,
                                             true
                                         ),true, true, AnimationType.fadeInfadeOut
                                     )
                                 }
                             }*/

                        }

                        getString(R.string.more_sales_inquiry) -> {
                            gotoReportDrill(getString(R.string.more_sales_inquiry))
                            /* for(i in dynamicMenuList){
                                 if(i.dynamicScreenName == getString(R.string.more_payment_follow_up)){
                                     mActivity.addFragment(
                                         DashboardDrillFragment.newInstance(
                                             false,
                                             DashboardListResponse(),
                                             DashboardDrillResponse(),
                                             i.storeProcedureName,
                                             i.reportSetupId,
                                             arrayListOf(),
                                             i.reportName,
                                             i.filter,
                                             i.reportGroupBy,
                                             true
                                         ),true, true, AnimationType.fadeInfadeOut
                                     )
                                 }
                             }*/
                        }
                    }
                }
                //binding.executePendingBindings()
            }
        }
    }

    fun gotoReportDrill(screenName: String) {
        for (i in dynamicMenuList) {
            if (i.dynamicScreenName == screenName) {
                Log.e("TAG", "gotoReportDrill: ")
                mActivity.addFragment(
                    DashboardDrillFragment.newInstance(
                        false,
                        DashboardListResponse(),
                        DashboardDrillResponse(),
                        i.storeProcedureName,
                        i.reportSetupId,
                        arrayListOf(),
                        i.reportName,
                        i.filter,
                        i.reportGroupBy,
                        true,
                        productFilter = false
                    ), true, true, AnimationType.fadeInfadeOut
                )
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgMenu ->
                mActivity.openDrawer()
        }
    }

}