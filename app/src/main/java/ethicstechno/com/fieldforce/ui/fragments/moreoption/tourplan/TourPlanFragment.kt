package ethicstechno.com.fieldforce.ui.fragments.moreoption.tourplan

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
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
import ethicstechno.com.fieldforce.databinding.FragmentTourPlanBinding
import ethicstechno.com.fieldforce.databinding.ItemTourPlanBinding
import ethicstechno.com.fieldforce.listener.FilterDialogListener
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.CommonDropDownResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.FilterListResponse
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.models.moreoption.expense.ExpenseCityListResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveTypeListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.moreoption.tourplan.TourPlanListResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.CategoryMasterResponse
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.*
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.showToastMessage
import ethicstechno.com.fieldforce.utils.dialog.UserSearchDialogUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TourPlanFragment : HomeBaseFragment(), View.OnClickListener, FilterDialogListener,
    UserSearchDialogUtil.PartyDealerDialogDetect, UserSearchDialogUtil.PlaceSearchDialogDetect {

    lateinit var binding: FragmentTourPlanBinding
    var tourPlanList: ArrayList<TourPlanListResponse> = arrayListOf()
    lateinit var tourPlanAdapter: TourPlanListAdapter
    var startDate = ""
    var endDate = ""
    var selectedDateOptionPosition = 4 // This MONTH
    val placeList: ArrayList<ExpenseCityListResponse> = arrayListOf()
    val partyDealerList: ArrayList<AccountMasterList> = arrayListOf()
    var selectedPartyDealer: AccountMasterList = AccountMasterList()
    var selectedAccountMasterId = 0
    var selectedAccountName = ""
    var selectedPlace: ExpenseCityListResponse = ExpenseCityListResponse()
    lateinit var tvPlace: TextView
    lateinit var tvPartyDealer: TextView
    private lateinit var tourPlanDialog: AlertDialog
    lateinit var imgCancelPartyDealer: ImageView
    lateinit var imgCancelPlace: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tour_plan, container, false)
        return binding.root
    }

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
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
        binding.toolbar.imgBack.visibility = View.VISIBLE
        binding.toolbar.imgFilter.visibility = View.VISIBLE
        binding.toolbar.imgFilter.setOnClickListener(this)
        binding.toolbar.imgBack.setOnClickListener(this)
        binding.toolbar.tvHeader.text = getString(R.string.tour_plan)
        binding.tourPlanHeader.llSpinner.visibility = View.GONE
        startDate = CommonMethods.getStartDateOfCurrentMonth()
        endDate = CommonMethods.getCurrentDate()
        binding.tourPlanHeader.tvDateOption.text = CommonMethods.dateTypeList[4]
        binding.tourPlanHeader.tvDateRange.text = "$startDate To $endDate"
        binding.toolbar.imgShare.visibility = View.VISIBLE
        binding.toolbar.imgShare.setOnClickListener(this)

        callTourPlanListApi()
        callPlaceListApi()
        callPartyDealerList()
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
                    this as FilterDialogListener,
                    mActivity,
                    startDate,
                    endDate,
                    selectedDateOptionPosition
                )
            }
            R.id.imgShare -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    createAndOpenPDF()
                }else{
                    val arrayPermissions = arrayListOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    PermissionUtil(mActivity).requestPermissions(arrayPermissions){
                        createAndOpenPDF()
                    }
                }
            }
        }
    }

    private fun callPlaceListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val placeListReq = JsonObject()
        placeListReq.addProperty("StateId", 0)
        placeListReq.addProperty("CityId", 0)

        val placeListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getCityListApi(placeListReq)

        placeListCall?.enqueue(object : Callback<List<ExpenseCityListResponse>> {
            override fun onResponse(
                call: Call<List<ExpenseCityListResponse>>,
                response: Response<List<ExpenseCityListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            placeList.clear()
                            placeList.addAll(it)
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        getString(R.string.error_message),
                        null,
                        isCancelVisibility = false
                    )
                }
            }

            override fun onFailure(call: Call<List<ExpenseCityListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null,
                        isCancelVisibility = false
                    )
                }
            }
        })

    }

    private fun callPartyDealerList() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()

        val partyDealerListReq = JsonObject()
        partyDealerListReq.addProperty("UserId", loginData.userId)

        val partyDealerCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getPartyDealerList(partyDealerListReq)

        partyDealerCall?.enqueue(object : Callback<List<AccountMasterList>> {
            override fun onResponse(
                call: Call<List<AccountMasterList>>,
                response: Response<List<AccountMasterList>>
            ) {
                CommonMethods.hideLoading()
                when {
                    response.code() == 200 -> {
                        response.body()?.let {
                            if (it.isNotEmpty()) {
                                partyDealerList.clear()
                                partyDealerList.addAll(it)
                                //setupPartyDealer()
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<AccountMasterList>>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null,
                        isCancelVisibility = false
                    )
                }
            }
        })

    }

    private fun callTourPlanListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val tourPlanReq = JsonObject()
        tourPlanReq.addProperty("UserId", loginData.userId)
        tourPlanReq.addProperty("FromDate", startDate)
        tourPlanReq.addProperty("ToDate", endDate)

        val tourPlanCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getTourPlanList(tourPlanReq)

        tourPlanCall?.enqueue(object : Callback<List<TourPlanListResponse>> {
            override fun onResponse(
                call: Call<List<TourPlanListResponse>>,
                response: Response<List<TourPlanListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            tourPlanList.clear()
                            tourPlanList.addAll(it)
                            setupReportRecyclerView()
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        getString(R.string.error_message),
                        null,
                        isCancelVisibility = false
                    )
                }
            }

            override fun onFailure(call: Call<List<TourPlanListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null,
                        isCancelVisibility = false
                    )
                }
            }
        })

    }

    private fun setupReportRecyclerView() {
        tourPlanAdapter = TourPlanListAdapter(tourPlanList)
        binding.rvTourPlan.adapter = tourPlanAdapter
        binding.rvTourPlan.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false)
    }

    inner class TourPlanListAdapter(
        private val tourPlanList: ArrayList<TourPlanListResponse>
    ) : RecyclerView.Adapter<TourPlanListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemTourPlanBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return tourPlanList.size
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val dashboardData = tourPlanList[position]
            holder.bind(dashboardData)
        }

        inner class ViewHolder(private val binding: ItemTourPlanBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(tourPlan: TourPlanListResponse) {
                binding.tvDate.text = tourPlan.planDate
                binding.tvDay.text = tourPlan.dayName
                if (tourPlan.tourPlanId > 0) {
                    val accountName =
                        if (tourPlan.accountName.isEmpty()) "" else tourPlan.accountName + ", "
                    val cityState =
                        if (tourPlan.cityStateCountry.isEmpty()) "" else tourPlan.cityStateCountry + ", "
                    val purpose = if (tourPlan.purpose.isEmpty()) "" else tourPlan.purpose + ", "
                    binding.tvPlanDetails.text =
                        "$accountName $cityState $purpose"
                }
                binding.executePendingBindings()
                binding.cardTourPlan.setOnClickListener {
                    showTourPlanDialog(tourPlan)
                }
            }
        }
    }

    private fun showTourPlanDialog(tourPlan: TourPlanListResponse) {
        val builder = AlertDialog.Builder(mActivity, R.style.MyAlertDialogStyle)
        tourPlanDialog = builder.create()
        tourPlanDialog.setCancelable(false)
        tourPlanDialog.setCanceledOnTouchOutside(false)
        tourPlanDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        tourPlanDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val inflater =
            mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout: View = inflater.inflate(R.layout.dialog_tour_plan, null)

        val flPlace: FrameLayout = layout.findViewById(R.id.flPlace)
        val flPartyDealer: FrameLayout = layout.findViewById(R.id.flPartyDealer)
        tvPlace = layout.findViewById(R.id.tvPlace)
        tvPartyDealer = layout.findViewById(R.id.tvPartyDealer)
        val tvDate: TextView = layout.findViewById(R.id.tvDate)
        val tvSubmit: TextView = layout.findViewById(R.id.tvSubmit)
        val tvReset: TextView = layout.findViewById(R.id.tvReset)
        val etPurpose: EditText = layout.findViewById(R.id.etPurpose)
        val etRemarks: EditText = layout.findViewById(R.id.etRemarks)
        val imgClose: ImageView = layout.findViewById(R.id.imgClose)
        imgCancelPartyDealer = layout.findViewById(R.id.imgCancelPartyDealer)
        imgCancelPlace = layout.findViewById(R.id.imgCancelPlace)

        imgCancelPlace.setOnClickListener {
            imgCancelPlace.visibility = View.GONE
            selectedPlace = ExpenseCityListResponse()
            tvPlace.text = ""
        }

        imgCancelPartyDealer.setOnClickListener {
            imgCancelPartyDealer.visibility = View.GONE
            selectedAccountName = ""
            selectedAccountMasterId = 0
            selectedPartyDealer = AccountMasterList()
            tvPartyDealer.text = ""
        }

        tvDate.text = tourPlan.planDate
        etPurpose.setText(tourPlan.purpose)
        etRemarks.setText(tourPlan.remarks)
        if (tourPlan.tourPlanId > 0) {
            tvPlace.text = tourPlan.cityStateCountry
            tvPartyDealer.text = tourPlan.accountName
            selectedPlace = ExpenseCityListResponse(
                0,
                tourPlan.cityId,
                tourPlan.cityStateCountry,
                tourPlan.userId,
                ""
            )
            selectedAccountMasterId = tourPlan.accountMasterId
            selectedAccountName = tourPlan.accountName
            imgCancelPlace.visibility =
                if (tvPlace.text.toString().isNotEmpty()) View.VISIBLE else View.GONE
            imgCancelPartyDealer.visibility =
                if (tvPartyDealer.text.toString().isNotEmpty()) View.VISIBLE else View.GONE
        } else {
            selectedPlace = ExpenseCityListResponse()
            selectedAccountMasterId = 0
        }
        tvReset.setOnClickListener {
            tvDate.text = CommonMethods.getCurrentDate()
            tvPartyDealer.text = ""
            tvPlace.text = ""
            tvPartyDealer.hint = getString(R.string.select_party)
            tvPlace.hint = getString(R.string.select_place)
            etPurpose.setText("")
            etRemarks.setText("")
            selectedPartyDealer = AccountMasterList()
            selectedPlace = ExpenseCityListResponse()
        }

        tvSubmit.setOnClickListener {
            callTourPlanAddUpdateApi(
                tourPlan,
                etPurpose.text.toString().trim(),
                etRemarks.text.toString().trim()
            )
        }

        flPlace.setOnClickListener {
            if (placeList.size > 0) {
                val userDialog = UserSearchDialogUtil(
                    mActivity,
                    type = FOR_PLACE,
                    placeList = placeList,
                    placeDialogInterfaceDetect = this as UserSearchDialogUtil.PlaceSearchDialogDetect,
                    userDialogInterfaceDetect = null
                )
                userDialog.showUserSearchDialog()
            }
        }

        flPartyDealer.setOnClickListener {
            if (partyDealerList.size > 0) {
                val partyDialog = UserSearchDialogUtil(
                    mActivity,
                    type = FOR_PARTY_DEALER,
                    partyDealerList = partyDealerList,
                    partyDealerInterfaceDetect = this as UserSearchDialogUtil.PartyDealerDialogDetect,
                    userDialogInterfaceDetect = null
                )
                partyDialog.showUserSearchDialog()
            }
        }

        imgClose.setOnClickListener {
            tourPlanDialog.dismiss()
        }



        tourPlanDialog.setView(layout, 0, 0, 0, 0)
        tourPlanDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_shape)
        tourPlanDialog.show()
    }

    private fun callTourPlanAddUpdateApi(
        tourPlan: TourPlanListResponse,
        purpose: String,
        remarks: String
    ) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val formattedData = CommonMethods.convertToAppDateFormat(tourPlan.planDate)

        Log.e("TAG", "callTourPlanAddUpdateApi: " + formattedData)

        val addTourPlanReq = JsonObject()
        addTourPlanReq.addProperty("TourPlanId", tourPlan.tourPlanId)
        addTourPlanReq.addProperty("PlanDate", formattedData)
        addTourPlanReq.addProperty("UserId", loginData.userId)
        addTourPlanReq.addProperty("UserName", loginData.userName)
        addTourPlanReq.addProperty("CityId", selectedPlace.cityId)
        addTourPlanReq.addProperty("CityStateCountry", selectedPlace.cityName)
        addTourPlanReq.addProperty("AccountMasterId", selectedAccountMasterId)
        addTourPlanReq.addProperty("AccountName", selectedAccountName)
        addTourPlanReq.addProperty("Purpose", purpose)
        addTourPlanReq.addProperty("Remarks", remarks)

        print("MY REQ ::::::: " + addTourPlanReq)
        val tourPlanCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.tourPlanAddUpdate(addTourPlanReq)

        tourPlanCall?.enqueue(object : Callback<CommonSuccessResponse> {
            override fun onResponse(
                call: Call<CommonSuccessResponse>,
                response: Response<CommonSuccessResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let { it ->
                        CommonMethods.showAlertDialog(
                            mActivity,
                            getString(R.string.tour_plan),
                            it.returnMessage,
                            object : PositiveButtonListener {
                                override fun okClickListener() {
                                    if (it.success) {
                                        tourPlanDialog.dismiss()
                                        selectedAccountMasterId = 0
                                        selectedPartyDealer = AccountMasterList()
                                        selectedAccountName = ""
                                        selectedPlace = ExpenseCityListResponse()
                                        callTourPlanListApi()
                                    }
                                }
                            }, isCancelVisibility = false
                        )
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        getString(R.string.error_message),
                        null,
                        isCancelVisibility = false
                    )
                }
            }

            override fun onFailure(call: Call<CommonSuccessResponse>, t: Throwable) {
                CommonMethods.hideLoading()
                if(mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        getString(R.string.error),
                        t.message,
                        null,
                        isCancelVisibility = false
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
        visitType: CommonDropDownResponse,
        partyDealer: AccountMasterList,
        visitPosition: Int
    ) {
        startDate = startDateSubmit
        endDate = endDateSubmit
        selectedDateOptionPosition = dateOptionPosition
        binding.tourPlanHeader.tvDateOption.text = dateOption
        binding.tourPlanHeader.tvDateRange.text = "$startDate To $endDate"
        callTourPlanListApi()
    }

    override fun partyDealerSelect(partyDealerData: AccountMasterList) {
        selectedPartyDealer = partyDealerData
        tvPartyDealer.text = partyDealerData.accountName
        selectedAccountName = partyDealerData.accountName
        selectedAccountMasterId = partyDealerData.accountMasterId
        imgCancelPartyDealer.visibility = View.VISIBLE
    }

    override fun placeSelect(placeData: ExpenseCityListResponse) {
        selectedPlace = placeData
        tvPlace.text = placeData.cityName
        imgCancelPlace.visibility = View.VISIBLE
    }

    private fun createAndOpenPDF() {
        showToastMessage(mActivity, "Report Generating...")
        val document = Document(PageSize.A4)

        val directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "TourPlan" + "_" + timeStamp + ".pdf"
        val filePath = File(directory, fileName)

        try {
            val writer = PdfWriter.getInstance(document, FileOutputStream(filePath))

            document.open()

            val titleFont = Font(Font.FontFamily.HELVETICA, 26f, Font.BOLD, BaseColor.BLUE)
            val titlePhrase = Phrase("Tour Plan Report", titleFont)
            val headerCell = PdfPCell(titlePhrase)
            headerCell.colspan = 4
            headerCell.horizontalAlignment = Element.ALIGN_CENTER
            headerCell.verticalAlignment = Element.ALIGN_MIDDLE
            headerCell.fixedHeight = 40f
            headerCell.border = PdfPCell.NO_BORDER
            val headerTable = PdfPTable(1)
            headerTable.widthPercentage = 100f
            headerTable.addCell(headerCell)
            document.add(headerTable)
            document.add(Paragraph("\n"))
            val infoFont = Font(Font.FontFamily.HELVETICA, 16f, Font.NORMAL, BaseColor.WHITE)
            val infoPhrase = Phrase(
                "User Name : ${loginData.userName} \n" +
                        "Date Option : ${CommonMethods.dateTypeList[selectedDateOptionPosition]} \n" +
                        "Date Range : $startDate to $endDate",
                infoFont
            )
            val infoCell = PdfPCell(infoPhrase)
            infoCell.colspan = 4
            infoCell.horizontalAlignment = Element.ALIGN_LEFT
            infoCell.verticalAlignment = Element.ALIGN_MIDDLE
            infoCell.fixedHeight = 70f
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

            val table = PdfPTable(floatArrayOf(10f, 20F, 10f, 60f))
            table.widthPercentage = 100f

            val headerFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, BaseColor.WHITE)
            val headerFontBlack = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, BaseColor.BLACK)
            table.addCell(CommonMethods.getStyledCell("Sr.No.", headerFont, BaseColor.BLUE))
            table.addCell(CommonMethods.getStyledCell("Date", headerFont, BaseColor.BLUE))
            table.addCell(CommonMethods.getStyledCell("Day", headerFont, BaseColor.BLUE))
            table.addCell(CommonMethods.getStyledCell("Plan Details", headerFont, BaseColor.BLUE))

            val dataFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL, BaseColor.BLACK)

            for (i in tourPlanList.indices) {
                table.addCell(
                    CommonMethods.getStyledCell(
                        (i + 1).toString(),
                        dataFont,
                        BaseColor.WHITE
                    )
                )

                val cellText = Phrase()
                cellText.add(Chunk(tourPlanList[i].planDate, dataFont))
                val cell = PdfPCell(cellText)
                cell.horizontalAlignment = Element.ALIGN_CENTER // Set left alignment for the cell
                cell.verticalAlignment =
                    Element.ALIGN_MIDDLE // Set center vertical alignment for the cell
                table.addCell(cell)

                val cellText1 = Phrase()
                cellText1.add(Chunk(tourPlanList[i].dayName, dataFont))
                val cell1 = PdfPCell(cellText1)
                cell1.horizontalAlignment = Element.ALIGN_CENTER // Set left alignment for the cell
                cell1.verticalAlignment =
                    Element.ALIGN_MIDDLE // Set center vertical alignment for the cell
                table.addCell(cell1)

                val accountName =
                    if (tourPlanList[i].accountName.isEmpty()) "" else tourPlanList[i].accountName + ", "
                val cityState =
                    if (tourPlanList[i].cityStateCountry.isEmpty()) "" else tourPlanList[i].cityStateCountry + ", "
                val purpose =
                    if (tourPlanList[i].purpose.isEmpty()) "" else tourPlanList[i].purpose + ", "
                val remarks =
                    if (tourPlanList[i].remarks.isEmpty()) "" else tourPlanList[i].remarks + ", "

                val cellText2 = Phrase()
                if(accountName.isNotEmpty()) {
                    cellText2.add(Chunk("Party : ", headerFontBlack))
                    cellText2.add(Chunk(accountName))
                }
                if(cityState.isNotEmpty()){
                    cellText2.add(Chunk("Place : ", headerFontBlack))
                    cellText2.add(Chunk(cityState))
                    cellText.add(Chunk("\n"))
                }
                if(purpose.isNotEmpty()){
                    cellText2.add(Chunk("Purpose : ", headerFontBlack))
                    cellText2.add(purpose)
                }
                if(remarks.isNotEmpty()){
                    cellText2.add(Chunk("Remarks : ", headerFontBlack))
                    cellText2.add((Chunk(remarks)))
                }
                //cellText2.add(Chunk("$accountName $cityState $purpose $remarks"))
                val cell2 = PdfPCell(cellText2)
                cell2.horizontalAlignment = Element.ALIGN_LEFT // Set left alignment for the cell
                cell2.verticalAlignment =
                    Element.ALIGN_MIDDLE // Set center vertical alignment for the cell

                table.addCell(cell2)

            }

// Step 11: Add the table to the document
            document.add(table)

// Step 12: Close the document
            document.close()

// Step 13: Dispose of the PdfWriter
            writer.close()


            // Step 12: Open the PDF using an intent
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

}