package ethicstechno.com.fieldforce.ui.fragments.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentNotificationBinding
import ethicstechno.com.fieldforce.databinding.ItemNotificationLayoutBinding
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.models.notification.NotificationListResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.IS_DATA_UPDATE
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationFragment : HomeBaseFragment() {
    var notificationList: ArrayList<NotificationListResponse> = arrayListOf()
    lateinit var notificationBinding: FragmentNotificationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notificationBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_notification, container, false)
        return notificationBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notificationBinding.toolbar.tvHeader.text = mActivity.getString(R.string.notification_list)
        notificationBinding.toolbar.imgBack.setOnClickListener {
            if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                mActivity.onBackPressedDispatcher.onBackPressed()
            } else {
                mActivity.onBackPressed()
            }
        }
        callNotificationListApi()

    }

    private fun callNotificationListApi() {
        Log.e("TAG", "callDashboardListApi: ")
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val notificationListReq = JsonObject()
        notificationListReq.addProperty("UserId", loginData.userId)

        val dashboardListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getNotificationList(notificationListReq)

        dashboardListCall?.enqueue(object : Callback<List<NotificationListResponse>> {
            override fun onResponse(
                call: Call<List<NotificationListResponse>>,
                response: Response<List<NotificationListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            notificationList.clear()
                            notificationList.addAll(it)
                            setupNotificationAdapter()
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

            override fun onFailure(call: Call<List<NotificationListResponse>>, t: Throwable) {
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

    private fun callNotificationReadApi(notificationData: NotificationListResponse) {
        Log.e("TAG", "callDashboardListApi: ")
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val notificationListReq = JsonObject()
        notificationListReq.addProperty("UserId", loginData.userId)
        notificationListReq.addProperty("notificationDetailsId", notificationData.notificationId)
        notificationListReq.addProperty("isReadAll", 0)

        val notificationReadApiCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.notificationRead(notificationListReq)

        notificationReadApiCall?.enqueue(object : Callback<CommonSuccessResponse> {
            override fun onResponse(
                call: Call<CommonSuccessResponse>,
                response: Response<CommonSuccessResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.success) {
                            AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, true)
                            callNotificationListApi()
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

            override fun onFailure(call: Call<CommonSuccessResponse>, t: Throwable) {
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

    private fun setupNotificationAdapter() {
        val dashBoardAdapter = NotificationAdapter(notificationList)
        notificationBinding.rvNotification.adapter = dashBoardAdapter
        notificationBinding.rvNotification.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
    }

    inner class NotificationAdapter(
        private val mNotificationList: ArrayList<NotificationListResponse>
    ) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemNotificationLayoutBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return mNotificationList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val notificationData = mNotificationList[position]
            holder.bind(notificationData)
        }

        inner class ViewHolder(private val notificationBinding: ItemNotificationLayoutBinding) :
            RecyclerView.ViewHolder(notificationBinding.root) {

            fun bind(notificationData: NotificationListResponse) {

                notificationBinding.txtNotificationMessage.text =
                    notificationData.notificationMessage
                notificationBinding.txtNotificationDate.text = notificationData.sentDateTime
                if (notificationData.isRead) {
                    notificationBinding.cardRead.visibility = View.GONE
                } else {
                    notificationBinding.cardRead.visibility = View.VISIBLE
                }
                notificationBinding.cardRead.setOnClickListener {
                    callNotificationReadApi(notificationData)
                }
                notificationBinding.executePendingBindings()

            }
        }
    }


}