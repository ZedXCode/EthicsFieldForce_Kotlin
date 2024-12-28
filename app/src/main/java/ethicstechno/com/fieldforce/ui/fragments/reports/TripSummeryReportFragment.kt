package ethicstechno.com.fieldforce.ui.fragments.reports

import addFragment
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import ethicstechno.com.fieldforce.BuildConfig
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentTripSummeryReportBinding
import ethicstechno.com.fieldforce.listener.FilterDialogListener
import ethicstechno.com.fieldforce.models.attendance.CurrentMonthAttendanceResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.FilterListResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveTypeListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.moreoption.visit.CategoryMasterResponse
import ethicstechno.com.fieldforce.models.reports.TripSummeryReportResponse
import ethicstechno.com.fieldforce.models.reports.UserListResponse
import ethicstechno.com.fieldforce.ui.adapter.AttendanceAdapter
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.*
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.dateTypeList
import ethicstechno.com.fieldforce.utils.dialog.UserSearchDialogUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class TripSummeryReportFragment : HomeBaseFragment(), View.OnClickListener, FilterDialogListener,
    UserSearchDialogUtil.UserSearchDialogDetect, AttendanceAdapter.LocationClick {

    lateinit var binding: FragmentTripSummeryReportBinding
    var tripSummeryReportList: ArrayList<TripSummeryReportResponse> = arrayListOf()
    var userList: ArrayList<UserListResponse> = arrayListOf()
    private lateinit var selectedUser: UserListResponse
    var startDate = ""
    var endDate = ""
    var selectedDateOptionPosition = 4 // This MONTH
    lateinit var tripSummeryData: TripSummeryReportResponse

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
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_trip_summery_report,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity.bottomHide()
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
        binding.toolbar.tvHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
        binding.toolbar.tvHeader.text = mActivity.getString(R.string.trip_summery_report)
        binding.toolbar.imgFilter.visibility = View.VISIBLE
        binding.toolbar.imgShare.visibility = View.VISIBLE
        binding.toolbar.imgFilter.setOnClickListener(this)
        binding.toolbar.imgShare.setOnClickListener(this)
        binding.toolbar.imgBack.setOnClickListener(this)
        binding.llTripReportHeader.tvUsername.setOnClickListener(this)

        selectedUser = UserListResponse(loginData.userId, loginData.userName ?: "")
        binding.llTripReportHeader.tvUsername.text = selectedUser.userName
        callUserListApi()
        setupAttendanceReportData()
        callTripSummeryListApi(startDate, endDate)
    }

    private fun setupAttendanceReportData() {
        binding.llTripReportHeader.tvDateOption.text = dateTypeList[4]
        val dateRange =
            "${CommonMethods.getStartDateOfCurrentMonth()} To ${CommonMethods.getCurrentDate()}"
        binding.llTripReportHeader.tvDateRange.text = dateRange
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    createAndOpenPDF()
                } else {
                    val arrayPermissions = arrayListOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    PermissionUtil(mActivity).requestPermissions(arrayPermissions) {
                        createAndOpenPDF()
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


    private fun callTripSummeryListApi(startDate: String, endDate: String) {
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

        val tripSummeryListReq = JsonObject()
        tripSummeryListReq.addProperty("TripId", 0)
        tripSummeryListReq.addProperty("UserId", selectedUser.userId)
        tripSummeryListReq.addProperty("StartDate", startDate)
        tripSummeryListReq.addProperty("EndDate", endDate)

        CommonMethods.getBatteryPercentage(mActivity)

        val tripSummeryListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getTripSummeryList(tripSummeryListReq)

        tripSummeryListCall?.enqueue(object : Callback<List<TripSummeryReportResponse>> {
            override fun onResponse(
                call: Call<List<TripSummeryReportResponse>>,
                response: Response<List<TripSummeryReportResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let { it ->
                        if (it.isNotEmpty()) {
                            binding.rvTripReport.visibility = View.VISIBLE
                            binding.tvNoData.visibility = View.GONE
                            tripSummeryReportList.clear()
                            tripSummeryReportList.addAll(it)
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

            override fun onFailure(call: Call<List<TripSummeryReportResponse>>, t: Throwable) {
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
        val attendanceAdapter = AttendanceAdapter(
            null, tripSummeryReportList,
            isFromAttendanceReport = true,//for display map label
            isFromTripSummery = true,
            this
        )
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
        callTripSummeryListApi(startDate, endDate)
        binding.llTripReportHeader.tvDateOption.text = dateOption
        binding.llTripReportHeader.tvDateRange.text = "$startDate To $endDate"
    }

    override fun userSelect(userData: UserListResponse) {
        selectedUser = userData
        binding.llTripReportHeader.tvUsername.text = userData.userName
        callTripSummeryListApi(startDate, endDate)
    }

    override fun onLocationClick(
        any: CurrentMonthAttendanceResponse,
        tripSummeryDataRes: TripSummeryReportResponse
    ) {
        Log.e("TAG", "onLocationClick: ")
        tripSummeryData = tripSummeryDataRes
        mActivity.addFragment(
            MapViewFragment.newInstance(
                Integer.parseInt(tripSummeryData.attendanceId),
                tripSummeryData.tripId,
                false,
                0.0,
                0.0,
                "",
                "",
                0.0,
                0.0,
                "",
                "",
                false
            ), true, true, AnimationType.fadeInfadeOut
        )
    }

    private fun createAndOpenPDF() {
        CommonMethods.showToastMessage(mActivity, "Report Generating...")
        // Step 1: Create a new document
        val document = Document(PageSize.A4)

        // Step 2: Specify the file path where the PDF will be saved
        val directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "TRIP_SUMMERY_REPORT_$timeStamp.pdf"
        val filePath = File(directory, fileName)

        try {
            val writer = PdfWriter.getInstance(document, FileOutputStream(filePath))

            document.open()

            val titleFont = Font(Font.FontFamily.HELVETICA, 26f, Font.BOLD, BaseColor.BLUE)
            val titlePhrase = Phrase("TRIP SUMMERY REPORT", titleFont)
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

            val table = PdfPTable(floatArrayOf(10f, 20f, 10f, 20f, 20f, 20f))
            table.widthPercentage = 100f

            val headerFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, BaseColor.WHITE)
            table.addCell(getStyledCell("Sr.", headerFont, BaseColor.BLUE))
            table.addCell(getStyledCell("Date", headerFont, BaseColor.BLUE))
            table.addCell(getStyledCell("Type", headerFont, BaseColor.BLUE))
            table.addCell(getStyledCell("Visit Count", headerFont, BaseColor.BLUE))
            table.addCell(getStyledCell("Map Km", headerFont, BaseColor.BLUE))
            table.addCell(getStyledCell("Actual Km", headerFont, BaseColor.BLUE))

            val dataFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL, BaseColor.BLACK)
            for (i in tripSummeryReportList.indices) {
                table.addCell(getStyledCell((i + 1).toString(), dataFont, BaseColor.WHITE))
                table.addCell(
                    getStyledCell(
                        tripSummeryReportList[i].tripDate,
                        dataFont,
                        BaseColor.WHITE
                    )
                )
                table.addCell(
                    getStyledCell(
                        tripSummeryReportList[i].attendanceStatus,
                        dataFont,
                        BaseColor.WHITE
                    )
                )
                table.addCell(
                    getStyledCell(
                        tripSummeryReportList[i].totalTrips,
                        dataFont,
                        BaseColor.WHITE
                    )
                )
                table.addCell(
                    getStyledCell(
                        tripSummeryReportList[i].totalTripKM.toString(),
                        dataFont,
                        BaseColor.WHITE
                    )
                )
                table.addCell(
                    getStyledCell(
                        tripSummeryReportList[i].actualKm.toString(),
                        dataFont,
                        BaseColor.WHITE
                    )
                )
            }

// Step 11: Add the table to the document
            document.add(table)

// Step 12: Close the document
            document.close()

// Step 13: Dispose of the PdfWriter
            writer.close()

            // Function to create a styled cell with specified font and background color


            // Step 12: Open the PDF using an intent
            openPDFWithIntent(filePath)

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

    private fun openPDFWithIntent(file: File) {
        try {
            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Use FileProvider for Android 7.0 and above
                FileProvider.getUriForFile(
                    mActivity,
                    BuildConfig.APPLICATION_ID + ".fileProvider",
                    file
                )
            } else {
                // For older devices, use Uri.fromFile
                Uri.fromFile(file)
            }

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)

        } catch (e: ActivityNotFoundException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            // Handle the exception if a PDF viewer is not installed
            Toast.makeText(mActivity, "No PDF viewer installed", Toast.LENGTH_SHORT).show()
        }
    }


}