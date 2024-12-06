package ethicstechno.com.fieldforce.ui.fragments.reports

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentTripRoadMapBinding
import ethicstechno.com.fieldforce.databinding.ItemTourRoadMapBinding
import ethicstechno.com.fieldforce.models.reports.TripReportResponse
import ethicstechno.com.fieldforce.models.reports.TripRoadMapResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TripRoadMapFragment : HomeBaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentTripRoadMapBinding
    var tripRoadMapList: ArrayList<TripRoadMapResponse> = arrayListOf()
    var tripReportData = TripReportResponse()

    companion object {
        fun newInstance(
            tripData: TripReportResponse
        ): TripRoadMapFragment {
            val args = Bundle()
            args.putParcelable(ARG_PARAM1, tripData)
            val fragment = TripRoadMapFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_trip_road_map, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            tripReportData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_PARAM1, TripReportResponse::class.java) ?: TripReportResponse()
            } else {
                it.getParcelable(ARG_PARAM1) ?: TripReportResponse()
            }
        }
        initView()

    }

    override fun onResume() {
        super.onResume()
        mActivity.bottomHide()
    }

    private fun initView() {

        binding.toolbar.imgBack.visibility = View.VISIBLE
        binding.toolbar.tvHeader.text = mActivity.getString(R.string.trip_road_map)
        mActivity.bottomHide()
        binding.toolbar.imgBack.setOnClickListener(this)

        binding.tvStartTime.text = tripReportData.tripStartTime
        binding.tvEndTime.text = tripReportData.tripEndTime

        binding.tvStartReading.text = tripReportData.startMeterReading.toString()
        binding.tvEndReading.text = tripReportData.endMeterReading.toString()

        binding.tvTripDate.text = tripReportData.tripDate
        binding.tvVehicle.text = tripReportData.vehicleTypeName

        ImageUtils().loadImageUrl(
            mActivity,
            appDao.getAppRegistration()?.apiHostingServer + tripReportData.startMeterReadingPhoto,
            binding.imgStartImage
        )
        ImageUtils().loadImageUrl(
            mActivity,
            appDao.getAppRegistration()?.apiHostingServer + tripReportData.endMeterReadingPhoto,
            binding.imgEndImage
        )

        binding.imgStartImage.scaleType = ImageView.ScaleType.FIT_XY
        binding.imgEndImage.scaleType = ImageView.ScaleType.FIT_XY

        binding.imgStartImage.setOnClickListener(this)
        binding.imgEndImage.setOnClickListener(this)

        callTripRoadMapListApi()
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

            R.id.imgStartImage -> {
                ImagePreviewCommonDialog.showImagePreviewDialog(
                    mActivity,
                    appDao.getAppRegistration()?.apiHostingServer + tripReportData.startMeterReadingPhoto
                )
            }
            R.id.imgEndImage -> {
                ImagePreviewCommonDialog.showImagePreviewDialog(
                    mActivity,
                    appDao.getAppRegistration()?.apiHostingServer + tripReportData.endMeterReadingPhoto
                )
            }
        }
    }

    private fun callTripRoadMapListApi() {
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

        val tripRoadMapListReq = JsonObject()
        tripRoadMapListReq.addProperty("TripId", tripReportData.tripId)

        CommonMethods.getBatteryPercentage(mActivity)

        val tripRoadMapListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getTripRoadMapList(tripRoadMapListReq)

        tripRoadMapListCall?.enqueue(object : Callback<List<TripRoadMapResponse>> {
            override fun onResponse(
                call: Call<List<TripRoadMapResponse>>,
                response: Response<List<TripRoadMapResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let { it ->
                        if (it.isNotEmpty()) {
                            binding.rvTripRoadMap.visibility = View.VISIBLE
                            binding.tvNoData.visibility = View.GONE
                            tripRoadMapList.clear()
                            tripRoadMapList.addAll(it)
                            setupTripMapAdapter()
                        } else {
                            binding.rvTripRoadMap.visibility = View.GONE
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

            override fun onFailure(call: Call<List<TripRoadMapResponse>>, t: Throwable) {
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

    private fun setupTripMapAdapter() {
        val attendanceAdapter = TripMapAdapter(tripRoadMapList)
        binding.rvTripRoadMap.adapter = attendanceAdapter
        binding.rvTripRoadMap.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
    }

    inner class TripMapAdapter(
        private val tripList: ArrayList<TripRoadMapResponse>
    ) : RecyclerView.Adapter<TripMapAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemTourRoadMapBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return tripList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val tripData = tripList[position]
            holder.bind(tripData)
        }

        inner class ViewHolder(private val binding: ItemTourRoadMapBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(roadMap: TripRoadMapResponse) {
                binding.tvTime.text = roadMap.locationTime
                binding.tvLocation.text = roadMap.location
            }
        }
    }

}