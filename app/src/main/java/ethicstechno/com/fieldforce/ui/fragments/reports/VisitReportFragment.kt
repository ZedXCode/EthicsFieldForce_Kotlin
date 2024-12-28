package ethicstechno.com.fieldforce.ui.fragments.reports

import addFragment
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentVisitReportBinding
import ethicstechno.com.fieldforce.databinding.ItemVisitReportBinding
import ethicstechno.com.fieldforce.listener.FilterDialogListener
import ethicstechno.com.fieldforce.models.dashboarddrill.FilterListResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveTypeListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.moreoption.visit.CategoryMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.VisitListResponse
import ethicstechno.com.fieldforce.models.reports.UserListResponse
import ethicstechno.com.fieldforce.models.reports.VisitReportListResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.visit.AddVisitFragment
import ethicstechno.com.fieldforce.utils.*
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.dateTypeList
import ethicstechno.com.fieldforce.utils.dialog.UserSearchDialogUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class VisitReportFragment : HomeBaseFragment(), View.OnClickListener, FilterDialogListener,
    UserSearchDialogUtil.UserSearchDialogDetect {

    lateinit var binding: FragmentVisitReportBinding
    var visitReportList: ArrayList<VisitReportListResponse> = arrayListOf()
    var userList: ArrayList<UserListResponse> = arrayListOf()
    var visitTypeList: ArrayList<CategoryMasterResponse> = arrayListOf()
    private var selectedUser = UserListResponse()
    private var selectedVisitType: CategoryMasterResponse = CategoryMasterResponse()
    var startDate = ""
    var endDate = ""
    private var selectedDateOptionPosition = 4 // This MONTH
    private var selectedVisitPosition = 0


    companion object {

        /*fun newInstance(
            optionType: Boolean
        ): TripReportFragment {
            val args = Bundle()
            args.putBoolean(ARG_PARAM1, optionType)
            val fragment = TripReportFragment()
            fragment.arguments = args
            return fragment
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_visit_report, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

    }

    override fun onResume() {
        super.onResume()
        mActivity.bottomHide()
    }

    private fun initView() {
        startDate = CommonMethods.getStartDateOfCurrentMonth()
        endDate = CommonMethods.getCurrentDate()

        binding.toolbar.imgBack.visibility = View.VISIBLE
        binding.toolbar.tvHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
        binding.toolbar.tvHeader.text = mActivity.getString(R.string.visit_report)
        binding.toolbar.imgFilter.visibility = View.VISIBLE
        mActivity.bottomHide()
        binding.toolbar.imgFilter.setOnClickListener(this)
        binding.toolbar.imgShare.setOnClickListener(this)

        binding.toolbar.imgBack.setOnClickListener(this)
        binding.llVisitReportHeader.tvUsername.setOnClickListener(this)
        selectedUser = UserListResponse(loginData.userId, loginData.userName ?: "")
        binding.llVisitReportHeader.tvUsername.text = selectedUser.userName
        callUserListApi()
        callVisitTypeListApi()
        setupAttendanceReportData()

    }

    private fun setupAttendanceReportData() {
        binding.llVisitReportHeader.tvDateOption.text = dateTypeList[4]
        val dateRange =
            "${CommonMethods.getStartDateOfCurrentMonth()} To ${CommonMethods.getCurrentDate()}"
        binding.llVisitReportHeader.tvDateRange.text = dateRange
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgMenu ->
                mActivity.openDrawer()
            R.id.imgBack -> {
                if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                    mActivity.onBackPressedDispatcher.onBackPressed()
                } else {
                    mActivity.onBackPressed()
                }
            }
            R.id.imgFilter -> {
                CommonMethods.showFilterDialog(
                    this,
                    mActivity,
                    startDate,
                    endDate,
                    selectedDateOptionPosition,
                    visitTypeList = visitTypeList,
                    isVisitTypeVisible = true,
                    selectedVisitPosition = selectedVisitPosition
                )
            }
            R.id.tvUsername -> {
                if (userList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        userList = userList,
                        userDialogInterfaceDetect = this as UserSearchDialogUtil.UserSearchDialogDetect
                    )
                    userDialog.showUserSearchDialog()
                }
            }
        }
    }
    private fun callVisitTypeListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val jsonReq = JsonObject()
        jsonReq.addProperty("UserId", loginData.userId)
        jsonReq.addProperty("parameterString", FORM_ID_VISIT)


        val visitTypeCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getCategoryMasterList(jsonReq)

        visitTypeCall?.enqueue(object : Callback<List<CategoryMasterResponse>> {
            override fun onResponse(
                call: Call<List<CategoryMasterResponse>>,
                response: Response<List<CategoryMasterResponse>>
            ) {
                CommonMethods.hideLoading()
                when {
                    response.code() == 200 -> {
                        response.body()?.let {
                            if (it.isNotEmpty()) {
                                visitTypeList.clear()
                                visitTypeList.add(CategoryMasterResponse(categoryMasterId = 0, categoryName = "All"))
                                visitTypeList.addAll(it)
                                selectedVisitType = visitTypeList[0]
                                callVisitReportListApi(startDate, endDate)
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<CategoryMasterResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.error),
                        t.message,
                        null
                    )
                }
            }
        })

    }


    private fun callVisitReportListApi(startDate: String, endDate: String) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showAlertDialog(
                mActivity,
                mActivity.getString(R.string.network_error),
                mActivity.getString(R.string.network_error_msg),
                null
            )
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val tripListReq = JsonObject()
        tripListReq.addProperty("UserId", selectedUser.userId)
        tripListReq.addProperty("CategoryMasterId", selectedVisitType.categoryMasterId)
        tripListReq.addProperty("StartDate", startDate)
        tripListReq.addProperty("EndDate", endDate)

        CommonMethods.getBatteryPercentage(mActivity)

        val tripListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.visitReportList(tripListReq)

        tripListCall?.enqueue(object : Callback<List<VisitReportListResponse>> {
            override fun onResponse(
                call: Call<List<VisitReportListResponse>>,
                response: Response<List<VisitReportListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let { it ->
                        if (it.isNotEmpty()) {
                            binding.rvVisitReport.visibility = View.VISIBLE
                            binding.tvNoData.visibility = View.GONE
                            visitReportList.clear()
                            visitReportList.addAll(it)
                            setupVisitReportAdapter()
                        }else{
                            binding.rvVisitReport.visibility = View.GONE
                            binding.tvNoData.visibility = View.VISIBLE
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        mActivity.getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<List<VisitReportListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.error),
                        t.message,
                        null
                    )
                }
            }
        })

    }

    private fun setupVisitReportAdapter() {
        val attendanceAdapter = VisitReportAdapter(visitReportList)
        binding.rvVisitReport.adapter = attendanceAdapter
        binding.rvVisitReport.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
    }

    private fun callUserListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showAlertDialog(
                mActivity,
                mActivity.getString(R.string.network_error),
                mActivity.getString(R.string.network_error_msg),
                null
            )
            return
        }

        CommonMethods.showLoading(mActivity)
        val appRegistrationData = appDao.getAppRegistration()
        val userListReq = JsonObject()
        userListReq.addProperty("UserId", loginData.userId)

        val appRegistrationCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)?.getUserList(userListReq)

        appRegistrationCall?.enqueue(object : Callback<List<UserListResponse>> {
            override fun onResponse(
                call: Call<List<UserListResponse>>,
                response: Response<List<UserListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            userList = arrayListOf()
                            userList.addAll(it)
                            //setupUserSpinner()
                        } else {
                            CommonMethods.showToastMessage(
                                mActivity,
                                mActivity.getString(R.string.something_went_wrong)
                            )
                        }
                    }
                }else{
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.error),
                        mActivity.getString(R.string.error_message),
                        null
                    )
                }
            }

            override fun onFailure(call: Call<List<UserListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.error),
                        t.message,
                        null
                    )
                }
            }

        })

    }

    inner class VisitReportAdapter(
        private val tripList: ArrayList<VisitReportListResponse>
    ) : RecyclerView.Adapter<VisitReportAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemVisitReportBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return tripList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val tripData = tripList[position]
            holder.bind(tripData)
        }

        inner class ViewHolder(private val binding: ItemVisitReportBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(report: VisitReportListResponse) {
                binding.tvDate.text = ": "+report.visitDate
                binding.tvTime.text = ": "+report.visitTime
                binding.tvType.text = ": "+report.categoryName
                binding.tvMapKm.text = ": "+report.mapKM
                binding.tvParty.text = " : "+report.accountName
                binding.tvPlace.text = " : "+report.cityName
                binding.llMain.setOnClickListener{
                    mActivity.addFragment(AddVisitFragment.newInstance(AccountMasterList(), report, true), true, true, AnimationType.fadeInfadeOut)
                }
            }
        }
    }

    override fun onFilterSubmitClick(
        startDateSubmit: String,
        endDateSubmit: String,
        dateOption: String,
        dateOptionPosition: Int,
        statusPosition: Int,
        selectedItemPosition: FilterListResponse,
        toString: FilterListResponse,
        visitType: CategoryMasterResponse,
        partyDealer: AccountMasterList,
        visitPosition: Int
    ) {
        startDate = startDateSubmit
        endDate = endDateSubmit
        selectedDateOptionPosition = dateOptionPosition
        binding.llVisitReportHeader.tvDateOption.text = dateOption
        binding.llVisitReportHeader.tvDateRange.text = "$startDate To $endDate"
        selectedVisitType = visitType
        selectedVisitPosition = visitPosition
        callVisitReportListApi(startDate, endDate)
    }

    override fun userSelect(userData: UserListResponse) {
        selectedUser = userData
        binding.llVisitReportHeader.tvUsername.text = userData.userName
        callVisitReportListApi(startDate, endDate)
    }

}