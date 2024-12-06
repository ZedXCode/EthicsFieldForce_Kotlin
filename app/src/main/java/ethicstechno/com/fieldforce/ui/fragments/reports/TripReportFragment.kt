package ethicstechno.com.fieldforce.ui.fragments.reports

import AnimationType
import addFragment
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentTripReportBinding
import ethicstechno.com.fieldforce.databinding.ItemTripReportBinding
import ethicstechno.com.fieldforce.listener.FilterDialogListener
import ethicstechno.com.fieldforce.models.dashboarddrill.FilterListResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveTypeListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.reports.TripReportResponse
import ethicstechno.com.fieldforce.models.reports.UserListResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.dateTypeList
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.PermissionUtil
import ethicstechno.com.fieldforce.utils.dialog.UserSearchDialogUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class TripReportFragment : HomeBaseFragment(), View.OnClickListener, FilterDialogListener,
    UserSearchDialogUtil.UserSearchDialogDetect {

    lateinit var binding: FragmentTripReportBinding
    var tripList: ArrayList<TripReportResponse> = arrayListOf()
    var userList: ArrayList<UserListResponse> = arrayListOf()
    private lateinit var selectedUser: UserListResponse
    var startDate = ""
    var endDate = ""
    private var selectedDateOptionPosition = 4 // This MONTH


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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trip_report, container, false)
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
        binding.toolbar.tvHeader.text = mActivity.getString(R.string.trip_report)
        binding.toolbar.imgFilter.visibility = View.VISIBLE
        binding.toolbar.imgShare.visibility = View.VISIBLE
        mActivity.bottomHide()
        binding.toolbar.imgFilter.setOnClickListener(this)
        binding.toolbar.imgShare.setOnClickListener(this)

        binding.toolbar.imgBack.setOnClickListener(this)
        binding.llAttendanceReportHeader.tvUsername.setOnClickListener(this)

        /* val currentDate = Date() // Replace this with your date
         binding.tvCurrentMonth.text = formatDateToMMMyyyy(currentDate)*/


        selectedUser = UserListResponse(loginData.userId, loginData.userName ?: "")
        binding.llAttendanceReportHeader.tvUsername.text = selectedUser.userName
        selectedUser = UserListResponse(loginData.userId, loginData.userName ?: "")
        binding.llAttendanceReportHeader.tvUsername.text = selectedUser.userName
        callUserListApi()
        setupAttendanceReportData()
        callTripListApi(startDate, endDate)
    }

    private fun setupAttendanceReportData() {
        binding.llAttendanceReportHeader.tvDateOption.text = dateTypeList[4]
        val dateRange =
            "${CommonMethods.getStartDateOfCurrentMonth()} To ${CommonMethods.getCurrentDate()}"
        binding.llAttendanceReportHeader.tvDateRange.text = dateRange
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
            R.id.imgFilter -> {
                CommonMethods.showFilterDialog(
                    this,
                    mActivity,
                    startDate,
                    endDate,
                    selectedDateOptionPosition
                )
            }
            R.id.imgShare -> {
                if (tripList.isEmpty()) {
                    return
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    openSharableOption()
                }else{
                    val arrayPermissions = arrayListOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    PermissionUtil(mActivity).requestPermissions(arrayPermissions){
                        openSharableOption()
                    }
                }
            }
            R.id.tvUsername -> {
                Log.e("TAG", "onClick: ${userList.size}")
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

    private fun openSharableOption() {
        createAndOpenPDF()
    }

    private fun createAndOpenPDF() {
        CommonMethods.showToastMessage(mActivity, "Report Generating...")
        val document = Document(PageSize.A4)

        val directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "TRIP_REPORT_$timeStamp.pdf"
        val filePath = File(directory, fileName)

        try {
            val writer = PdfWriter.getInstance(document, FileOutputStream(filePath))

            document.open()

            val titleFont = Font(Font.FontFamily.HELVETICA, 26f, Font.BOLD, BaseColor.BLUE)
            val titlePhrase = Phrase("TRIP REPORT", titleFont)
            val headerCell = PdfPCell(titlePhrase)
            headerCell.colspan = 6
            headerCell.horizontalAlignment = Element.ALIGN_CENTER
            headerCell.verticalAlignment = Element.ALIGN_MIDDLE
            headerCell.fixedHeight = 40f
            headerCell.border = PdfPCell.NO_BORDER
            val headerTable = PdfPTable(1)
            headerTable.widthPercentage = 100f
            headerTable.addCell(headerCell)
            document.add(headerTable)
            document.add(Paragraph("\n"))
            val infoFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL, BaseColor.WHITE)
            val infoPhrase = Phrase(
                "User Name : ${loginData.userName ?: ""}\n" +
                        "Start date: $startDate  " +
                        "End date: $endDate",
                infoFont
            )
            val infoCell = PdfPCell(infoPhrase)
            infoCell.colspan = 6
            infoCell.horizontalAlignment = Element.ALIGN_LEFT
            infoCell.verticalAlignment = Element.ALIGN_MIDDLE
            infoCell.fixedHeight = 40f
            infoCell.paddingTop = 8f
            infoCell.paddingRight = 8f
            infoCell.paddingLeft = 8f
            infoCell.paddingBottom = 8f
            infoCell.backgroundColor = BaseColor.BLUE // Add background color
            val infoTable = PdfPTable(1)
            infoTable.widthPercentage = 100f
            infoTable.addCell(infoCell)
            document.add(infoTable)

            document.add(Paragraph("\n"))

            val table = PdfPTable(floatArrayOf(7f, 13f, 30f, 30f, 10f, 10f))
            table.widthPercentage = 100f

            val headerFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, BaseColor.WHITE)
            table.addCell(getStyledCell("Sr.", headerFont, BaseColor.BLUE))
            table.addCell(getStyledCell("From Date & To Date", headerFont, BaseColor.BLUE))
            table.addCell(getStyledCell("From Place", headerFont, BaseColor.BLUE))
            table.addCell(getStyledCell("To Place", headerFont, BaseColor.BLUE))
            table.addCell(getStyledCell("Map Km", headerFont, BaseColor.BLUE))
            table.addCell(getStyledCell("Actual Km", headerFont, BaseColor.BLUE))

            val dataFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL, BaseColor.BLACK)
            for (i in tripList.indices) {
                table.addCell(getStyledCell((i + 1).toString(), dataFont, BaseColor.WHITE))
                table.addCell(
                    getStyledCell(
                        tripList[i].createDateTime + " & " + tripList[i].tripCompletionDateTime,
                        dataFont,
                        BaseColor.WHITE
                    )
                )
                table.addCell(getStyledCell(tripList[i].fromPlace, dataFont, BaseColor.WHITE))
                table.addCell(getStyledCell(tripList[i].toPlace, dataFont, BaseColor.WHITE))
                table.addCell(
                    getStyledCell(
                        tripList[i].totalTripKM.toString(),
                        dataFont,
                        BaseColor.WHITE
                    )
                )
                table.addCell(
                    getStyledCell(
                        tripList[i].actualKM.toString(),
                        dataFont,
                        BaseColor.WHITE
                    )
                )
            }

            document.add(table)

            document.close()

            writer.close()

            CommonMethods.openPDFWithIntent(mActivity, filePath)

        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            CommonMethods.showAlertDialog(
                mActivity, "Error ", "Error is : " + e.message.toString(),
                okListener = null, isCancelVisibility = false
            )
            e.printStackTrace()
        }
    }

    private fun getStyledCell(content: String, font: Font, bgColor: BaseColor): PdfPCell {
        val cell = PdfPCell(Phrase(content, font))
        cell.backgroundColor = bgColor
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        cell.setPadding(8f)
        return cell
    }

    private fun callTripListApi(startDate: String, endDate: String) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showAlertDialog(
                mActivity,
                getString(R.string.network_error),
                getString(R.string.network_error_msg),
                null
            )
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val tripListReq = JsonObject()
        tripListReq.addProperty("TripId", "0")
        tripListReq.addProperty("UserId", selectedUser.userId)
        tripListReq.addProperty("StartDate", startDate)
        tripListReq.addProperty("EndDate", endDate)

        CommonMethods.getBatteryPercentage(mActivity)

        val tripListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getTripList(tripListReq)

        tripListCall?.enqueue(object : Callback<List<TripReportResponse>> {
            override fun onResponse(
                call: Call<List<TripReportResponse>>,
                response: Response<List<TripReportResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let { it ->
                        if (it.isNotEmpty()) {
                            binding.rvTripReport.visibility = View.VISIBLE
                            binding.tvNoData.visibility = View.GONE
                            tripList.clear()
                            tripList.addAll(it)
                            setupTripReportAdapter()
                        } else {
                            binding.rvTripReport.visibility = View.GONE
                            binding.tvNoData.visibility = View.VISIBLE
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

            override fun onFailure(call: Call<List<TripReportResponse>>, t: Throwable) {
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

    private fun setupTripReportAdapter() {
        val attendanceAdapter = TripReportAdapter(tripList)
        binding.rvTripReport.adapter = attendanceAdapter
        binding.rvTripReport.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
    }

    private fun callUserListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showAlertDialog(
                mActivity,
                getString(R.string.network_error),
                getString(R.string.network_error_msg),
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
                                getString(R.string.something_went_wrong)
                            )
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

            override fun onFailure(call: Call<List<UserListResponse>>, t: Throwable) {
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

    inner class TripReportAdapter(
        private val tripList: ArrayList<TripReportResponse>
    ) : RecyclerView.Adapter<TripReportAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemTripReportBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return tripList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val tripData = tripList[position]
            holder.bind(tripData)
        }

        inner class ViewHolder(private val binding: ItemTripReportBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(report: TripReportResponse) {
                binding.tvTripDate.text = ": " + report.tripDate
                binding.tvVehicle.text = ": " + report.vehicleTypeName
                binding.tvStartTime.text = ": " + report.tripStartTime
                binding.tvEndTime.text = ": " + report.tripEndTime
                binding.tvPlace.text = ": " + report.toPlace
                binding.tvVisitCount.text = ": " + report.visitCount.toString()
                binding.tvReadingKm.text = ": " + report.actualKM.toString()
                binding.tvMapKm.text = ": " + report.totalTripKM.toString()
                binding.llMain.setOnClickListener {
                    mActivity.addFragment(
                        TripRoadMapFragment.newInstance(report),
                        true,
                        true,
                        AnimationType.fadeInfadeOut
                    )
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
        visitType: LeaveTypeListResponse,
        partyDealer: AccountMasterList,
        visitPosition: Int
    ) {
        startDate = startDateSubmit
        endDate = endDateSubmit
        selectedDateOptionPosition = dateOptionPosition
        callTripListApi(startDate, endDate)
        binding.llAttendanceReportHeader.tvDateOption.text = dateOption
        binding.llAttendanceReportHeader.tvDateRange.text = "$startDate To $endDate"
    }

    override fun userSelect(userData: UserListResponse) {
        selectedUser = userData
        binding.llAttendanceReportHeader.tvUsername.text = userData.userName
        callTripListApi(startDate, endDate)
    }

}