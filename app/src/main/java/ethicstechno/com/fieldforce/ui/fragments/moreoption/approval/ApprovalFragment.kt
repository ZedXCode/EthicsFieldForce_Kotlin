package ethicstechno.com.fieldforce.ui.fragments.moreoption.approval

import AnimationType
import addFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentApprovalBinding
import ethicstechno.com.fieldforce.databinding.ItemMoreApprovalBinding
import ethicstechno.com.fieldforce.models.ApprovalCountResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.ExpenseListFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.leave.LeaveApplicationListFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.order_entry.OrderEntryListFragment
import ethicstechno.com.fieldforce.ui.fragments.moreoption.quotation.QuotationEntryListFragment
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.FORM_ID_EXPENSE_ENTRY_NUMBER
import ethicstechno.com.fieldforce.utils.FORM_ID_LEAVE_APPLICATION_ENTRY_NUMBER
import ethicstechno.com.fieldforce.utils.FORM_ID_ORDER_ENTRY_NUMBER
import ethicstechno.com.fieldforce.utils.FORM_ID_QUOTATION_ENTRY_NUMBER
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApprovalFragment : HomeBaseFragment(),View.OnClickListener {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentApprovalBinding

    var moreList: ArrayList<ApprovalCountResponse> = arrayListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       // return inflater.inflate(R.layout.fragment_approval, container, false)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_approval, container, false)
        return binding.root
    }

    companion object {

        fun newInstance(
            isFromInquiry: Boolean
        ): ApprovalFragment {
            val args = Bundle()
            val fragment = ApprovalFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.toolbar.imgBack.visibility = View.VISIBLE
        binding.toolbar.tvHeader.text = getString(R.string.approval_list)
        binding.toolbar.imgBack.setOnClickListener(this)

        callApprovalCountList()
        setupMoreAdapter()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isAdded && !hidden) {
            mActivity.bottomVisible()
            callApprovalCountList()
            setupMoreAdapter()
        }
    }

    private fun callApprovalCountList() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)
        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val dashboardListReq = JsonObject()
        dashboardListReq.addProperty("UserId", loginData.userId)

        val dashboardListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getApprovalCount(dashboardListReq)

        dashboardListCall?.enqueue(object : Callback<List<ApprovalCountResponse>> {
            override fun onResponse(
                call: Call<List<ApprovalCountResponse>>,
                response: Response<List<ApprovalCountResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        moreList.clear()
                        moreList.addAll(it)
                        binding.rvMore.adapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onFailure(call: Call<List<ApprovalCountResponse>>, t: Throwable) {
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


    private fun setupMoreAdapter() {
        val attendanceAdapter = MoreAdapter(moreList)
        binding.rvMore.adapter = attendanceAdapter
        binding.rvMore.layoutManager =
            GridLayoutManager(mActivity, 3, RecyclerView.VERTICAL, false)
    }


    override fun onResume() {
        super.onResume()
        mActivity.bottomHide()
    }

    inner class MoreAdapter(
        private val menuList: ArrayList<ApprovalCountResponse>
    ) : RecyclerView.Adapter<MoreAdapter.ViewHolder>() {

        private var maxHeight = 0

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemMoreApprovalBinding.inflate(inflater, parent, false)

            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return menuList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val menuData = menuList[position]
            holder.bind(menuData)
        }

        inner class ViewHolder(private val binding: ItemMoreApprovalBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(moreData: ApprovalCountResponse) {
                binding.tvMenuName.text = moreData.documentName
                binding.txtApprovalCount.text = moreData.documentCount.toString()

                binding.tvMenuName.post {
                    val height: Int = binding.tvMenuName.height
                    if (height > maxHeight) {
                        maxHeight = height // Store the tallest item height
                    }
                    // Apply the same height to all items
                    binding.tvMenuName.setMinHeight(maxHeight)
                }

                binding.cardAttendance.setOnClickListener {
                    when (moreData.formId.toString()){
                        FORM_ID_ORDER_ENTRY_NUMBER ->{
                            mActivity.addFragment(
                                OrderEntryListFragment.newInstance(true),
                                addToBackStack = true,
                                ignoreIfCurrent = true,
                                animationType = AnimationType.fadeInfadeOut
                            )
                        }

                        FORM_ID_QUOTATION_ENTRY_NUMBER ->{
                            mActivity.addFragment(
                                QuotationEntryListFragment.newInstance(true),
                                addToBackStack = true,
                                ignoreIfCurrent = true,
                                animationType = AnimationType.fadeInfadeOut
                            )
                        }

                        FORM_ID_EXPENSE_ENTRY_NUMBER ->{
                            mActivity.addFragment(
                                ExpenseListFragment.newInstance(true),
                                addToBackStack = true,
                                ignoreIfCurrent = true,
                                animationType = AnimationType.fadeInfadeOut
                            )
                        }
                        FORM_ID_LEAVE_APPLICATION_ENTRY_NUMBER ->{
                            mActivity.addFragment(
                                LeaveApplicationListFragment.newInstance(true),
                                addToBackStack = true,
                                ignoreIfCurrent = true,
                                animationType = AnimationType.fadeInfadeOut
                            )
                        }
                    }
                }
            }
        }
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack ->{
                if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                    mActivity.onBackPressedDispatcher.onBackPressed()
                } else {
                    mActivity.onBackPressed()
                }
            }
        }
    }

}