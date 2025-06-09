package ethicstechno.com.fieldforce.ui.fragments.moreoption

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.api.WebApiClient
import ethicstechno.com.fieldforce.databinding.FragmentAddExpenceBinding
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.AppRegistrationResponse
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.models.moreoption.expense.AddExpenseResponse
import ethicstechno.com.fieldforce.models.moreoption.expense.ExpenseCityListResponse
import ethicstechno.com.fieldforce.models.moreoption.expense.ExpenseDetailListResponse
import ethicstechno.com.fieldforce.models.moreoption.expense.ExpenseListResponse
import ethicstechno.com.fieldforce.models.moreoption.expense.ExpenseTypeListResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.BranchMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.CategoryMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.CompanyMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.DivisionMasterResponse
import ethicstechno.com.fieldforce.ui.adapter.ImageAdapter
import ethicstechno.com.fieldforce.ui.adapter.LeaveTypeAdapter
import ethicstechno.com.fieldforce.ui.base.HomeBaseFragment
import ethicstechno.com.fieldforce.utils.ARG_PARAM1
import ethicstechno.com.fieldforce.utils.ARG_PARAM2
import ethicstechno.com.fieldforce.utils.ARG_PARAM3
import ethicstechno.com.fieldforce.utils.ARG_PARAM4
import ethicstechno.com.fieldforce.utils.ARG_PARAM5
import ethicstechno.com.fieldforce.utils.AlbumUtility
import ethicstechno.com.fieldforce.utils.AppPreference
import ethicstechno.com.fieldforce.utils.CONTROL_TYPE_FIX_PER_KM
import ethicstechno.com.fieldforce.utils.CommonMethods
import ethicstechno.com.fieldforce.utils.CommonMethods.Companion.dateFormat
import ethicstechno.com.fieldforce.utils.ConnectionUtil
import ethicstechno.com.fieldforce.utils.FORM_ID_EXPENSE_ENTRY
import ethicstechno.com.fieldforce.utils.FOR_BRANCH
import ethicstechno.com.fieldforce.utils.FOR_COMPANY
import ethicstechno.com.fieldforce.utils.FOR_DIVISION
import ethicstechno.com.fieldforce.utils.FOR_PLACE
import ethicstechno.com.fieldforce.utils.ID_ZERO
import ethicstechno.com.fieldforce.utils.IS_DATA_UPDATE
import ethicstechno.com.fieldforce.utils.ImagePreviewCommonDialog
import ethicstechno.com.fieldforce.utils.PermissionUtil
import ethicstechno.com.fieldforce.utils.STATUS_APPROVED
import ethicstechno.com.fieldforce.utils.dialog.UserSearchDialogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Calendar
import java.util.concurrent.Executors

class AddExpenseFragment : HomeBaseFragment(), View.OnClickListener,
    UserSearchDialogUtil.CompanyDialogDetect, UserSearchDialogUtil.DivisionDialogDetect,
    UserSearchDialogUtil.BranchDialogDetect,
    UserSearchDialogUtil.PlaceSearchDialogDetect, LeaveTypeAdapter.TypeSelect {

    lateinit var binding: FragmentAddExpenceBinding
    var placeList: ArrayList<ExpenseCityListResponse> = arrayListOf()
    var expenseTypeList: ArrayList<ExpenseTypeListResponse> = arrayListOf()
    lateinit var selectedPlace: ExpenseCityListResponse
    var selectedExpenseType: ExpenseTypeListResponse = ExpenseTypeListResponse()
    var imageFile: File? = null
    var base64Image: String = ""
    var isForUpdate = false
    var isPreviewImage = false
    var isEditMode = false
    var isForDetails = false
    var expenseDataForUpdate: ExpenseListResponse = ExpenseListResponse()
    var expenseDetailsResponse: ExpenseDetailListResponse = ExpenseDetailListResponse()

    var isAttachmentRequired = false
    private var isExpanded = false

    val companyMasterList: ArrayList<CompanyMasterResponse> = arrayListOf()
    var selectedCompany: CompanyMasterResponse? = null
    var selectedBranch: BranchMasterResponse? = null
    var selectedDivision: DivisionMasterResponse? = null

    var selectedCategory: CategoryMasterResponse? = null

    val branchMasterList: ArrayList<BranchMasterResponse> = arrayListOf()
    val divisionMasterList: ArrayList<DivisionMasterResponse> = arrayListOf()
    val categoryList: ArrayList<CategoryMasterResponse> = arrayListOf()

    private var imageAnyList: ArrayList<Any> = arrayListOf()
    private var imageAdapter: ImageAdapter? = null
    var isReadOnly = true

    private var expenseId: Int = 0
    private var isCompanyChange = false
    private var selectIndex: Int = 0
    private var detail: String = ""
    private var isForApproval : Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_expence, container, false)
        return binding.root
    }

    companion object {
        fun newInstance(
            expenseId: Int,
            isForUpdate: Boolean,
            expenseDataForUpdate: ExpenseListResponse = ExpenseListResponse(),
            isForDetails: Boolean,
            isForApproval:Boolean
        ): AddExpenseFragment {
            val args = Bundle()
            args.putInt(ARG_PARAM1, expenseId)
            args.putBoolean(ARG_PARAM2, isForUpdate)
            args.putParcelable(ARG_PARAM3, expenseDataForUpdate)
            args.putBoolean(ARG_PARAM4, isForDetails)
            args.putBoolean(ARG_PARAM5, isForApproval)
            val fragment = AddExpenseFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            expenseId = it.getInt(ARG_PARAM1, -1)
            isForUpdate = it.getBoolean(ARG_PARAM2)
            expenseDataForUpdate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_PARAM3, ExpenseListResponse::class.java)
                    ?: ExpenseListResponse()
            } else {
                it.getParcelable(ARG_PARAM3) ?: ExpenseListResponse()
            }
            isForDetails = it.getBoolean(ARG_PARAM4)
            isForApproval = it.getBoolean(ARG_PARAM5)
        }
        initView()
    }

    override fun onResume() {
        super.onResume()
        mActivity.bottomHide()
    }

    private fun initView() {
        mActivity.bottomHide()
        binding.toolbar.imgBack.visibility = View.VISIBLE
        binding.toolbar.imgBack.setOnClickListener(this)
        binding.toolbar.imgEdit.setOnClickListener(this)
        binding.toolbar.imgDelete.setOnClickListener(this)
        binding.toolbar.tvHeader.text =
            if (isForUpdate) mActivity.getString(R.string.expense_details) else mActivity.getString(
                R.string.add_expense
            )

        binding.tvDateForExpense.text =
            if (isForUpdate) expenseDataForUpdate.expenseDate else CommonMethods.getCurrentDate()

        //binding.cardImage.setOnClickListener(this)
        binding.cardImageCapture.setOnClickListener(this)

        setupImageUploadRecyclerView()

        binding.llSelectCompany.setOnClickListener(this)
        binding.llSelectBranch.setOnClickListener(this)
        binding.llSelectDivision.setOnClickListener(this)
        binding.llHeader.setOnClickListener(this)
        binding.tvDateForExpense.setOnClickListener(this)

        binding.tvDateForExpense.text = CommonMethods.getCurrentDate()


        binding.llHeader.setOnClickListener {
            toggleSectionVisibility(binding.llOptionalFields, binding.ivToggle)
        }

        selectedCompany = CompanyMasterResponse(companyMasterId = 0, companyName = "")
        selectedBranch = BranchMasterResponse(branchMasterId = 0, branchName = "")
        selectedDivision = DivisionMasterResponse(divisionMasterId = 0, divisionName = "")
        selectedCategory = CategoryMasterResponse(categoryMasterId = 0, categoryName = "")


        if (expenseId > 0) {
            binding.toolbar.imgDelete.visibility = if (expenseDataForUpdate.allowDelete) View.VISIBLE else View.GONE
            binding.toolbar.imgEdit.visibility = if (expenseDataForUpdate.allowEdit) View.VISIBLE else View.GONE
            callExpenseDetailListApi()
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    executeAPIsAndSetupData()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    e.printStackTrace()
                }
            }
        }

        if(isForApproval){
            binding.llAcceptReject.visibility = View.VISIBLE
            binding.llApprovalRemarks.visibility = View.VISIBLE
            binding.tvAccept.setOnClickListener(this)
            binding.tvReject.setOnClickListener(this)
        }else{
            binding.llAcceptReject.visibility = View.GONE
            binding.llApprovalRemarks.visibility = View.GONE
        }

        if (isForDetails && isForUpdate) {
            binding.etApprovalAmount.setText(expenseDataForUpdate.expenseAmount.toString())
            binding.llApprovalAmount.visibility = View.VISIBLE
            binding.tvExpenseType.visibility = View.VISIBLE
            binding.flExpnseType.visibility = View.GONE
        } else {
            binding.llApprovalAmount.visibility = View.GONE
            binding.tvExpenseType.visibility = View.GONE
            binding.flExpnseType.visibility = View.VISIBLE
        }
        if (isForDetails) {
            binding.tvAddExpense.visibility = View.GONE
        } else {
            binding.llSelectPlace.setOnClickListener(this)
            binding.tvDateForExpense.setOnClickListener(this)
            binding.tvAddExpense.setOnClickListener(this)
        }
        if (isForUpdate) {
            isPreviewImage = true
            setWidgetsClickability(false)
            /*if (!isForDetails) {
                binding.toolbar.imgEdit.visibility = View.VISIBLE
                binding.toolbar.imgDelete.visibility = View.VISIBLE
                binding.toolbar.imgEdit.setOnClickListener(this)
                binding.toolbar.imgDelete.setOnClickListener(this)
            }*/
//            binding.tvExpenseType.text = expenseDataForUpdate.expenseTypeName
//            binding.tvSelectPlace.text = expenseDataForUpdate.cityName
//            Log.d("COMPANY--->",""+expenseDataForUpdate.cityName)
//            binding.tvSelectControlMode.text = expenseDataForUpdate.controlModeName
//            binding.tvSelectVehicalType.text = expenseDataForUpdate.vehicleTypeName
//
//            binding.etRemarks.setText(expenseDataForUpdate.remarks)
//            binding.tvEligibleAmount.text = expenseDataForUpdate.eligibleAmount.toString()
//            binding.etExpenseAmount.setText(expenseDataForUpdate.expenseAmount.toString())
//
//            binding.tvSelectCompany.text = expenseDetail.companyName
//            binding.llSelectCompany.isClickable = false
//
//            binding.tvSelectBranch.text = expenseDetail.branchName
//            binding.llSelectBranch.isClickable = false
//
//            binding.tvSelectDivision.text = expenseDetail.divisionName
//            binding.llSelectDivision.isClickable = false
//
//            binding.tvCategory.text = expenseDetail.cityCategoryName//check categoryname

//
//            binding.tvDateForExpense.isClickable = false
//            Log.d("COMPANY--->",""+expenseDetail.companyName)
//            Log.d("BRANCH--->",""+expenseDetail.branchName)
//
//            Log.d("DIVISION--->",""+expenseDetail.divisionName)
//            Log.d("CATEGORY--->",""+expenseDetail.cityCategoryName)
//            binding.imgExpense.scaleType = ImageView.ScaleType.CENTER_CROP
//            ImageUtils().loadImageUrl(
//                mActivity,
//                appDatabase.appDao()
//                    .getAppRegistration().apiHostingServer + expenseDataForUpdate.documentPath,
//                binding.imgExpense
//            )
            binding.tvExpenseType.visibility = View.VISIBLE
            binding.flExpnseType.visibility = View.GONE
        } else {
            callCityListApi()
        }

    }

    private fun setupImageUploadRecyclerView() {
        val apiUrl = appDao.getAppRegistration().apiHostingServer
        binding.rvImages.layoutManager =
            LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
        imageAdapter = ImageAdapter(mActivity, apiUrl)
        binding.rvImages.adapter = imageAdapter
        handleAssetRVView(0)

        imageAdapter?.setOnClick(object : ImageAdapter.OnAssetImageCancelClick {
            override fun onImageCancel(position: Int) {
                Log.d("REMOVEIMAGE===>", "image deleted...")
                //if (!isReadOnly && position != RecyclerView.NO_POSITION && imageAnyList.size > 0) {
                if (imageAnyList.size > 0) {
                    imageAnyList.removeAt(position)
                    imageAdapter?.addImage(imageAnyList, false)
                    handleAssetRVView(imageAnyList.size)
                    Log.d("REMOVEIMAGE===>", "" + imageAnyList.size)
                }
                //}
            }

            override fun onImagePreview(position: Int) {
                if (imageAnyList[position] is String) {
                    ImagePreviewCommonDialog.showImagePreviewDialog(
                        mActivity,
                        appDatabase.appDao()
                            .getAppRegistration().apiHostingServer + imageAnyList[position]
                    )
                } else {
                    ImagePreviewCommonDialog.showImagePreviewDialog(
                        mActivity, imageAnyList[position]
                    )
                }
            }
        })
    }


    fun handleAssetRVView(imageList: Int) {
        try {
            binding.txtNoFilePathImages.visibility = if (imageList == 0) View.VISIBLE else View.GONE
            binding.rvImages.visibility = if (imageList == 0) View.GONE else View.VISIBLE
        } catch (e: java.lang.Exception) {
            Log.e("TAG", "handleAssetRVView: *ERROR* IN \$submitDALPaper$ :: error = " + e.message)
        }
    }

//    private fun openAlbumForList() {
//        AlbumUtility(mActivity, true).openAlbumAndHandleImageMultipleSelection(
//            onImagesSelected = { selectedFiles ->
//                lifecycleScope.launch(Dispatchers.IO) {
//                    val modifiedImageFiles = selectedFiles.mapNotNull { file ->
//                        CommonMethods.addDateAndTimeToFile(
//                            file, CommonMethods.createImageFile(mActivity) ?: return@mapNotNull null
//                        )
//                    }
//
//                    withContext(Dispatchers.Main) {
//                        if (modifiedImageFiles.isNotEmpty()) {
//                            imageAnyList.addAll(modifiedImageFiles)
//                            imageAdapter?.addImage(imageAnyList, false)
//                            handleAssetRVView(imageAnyList.size)
//                        }
//                    }
//                }
//            },
//            onError = {
//                CommonMethods.showToastMessage(mActivity, it)
//            }
//        )
//    }

    private fun openAlbumForList() {
        val maxImageLimit = 4
        val remainingLimit = maxImageLimit - imageAnyList.size
        AlbumUtility(mActivity, true).openAlbumAndHandleImageMultipleSelection(
            onImagesSelected = { selectedFiles ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val modifiedImageFiles = selectedFiles.mapNotNull { file ->
                        CommonMethods.addDateAndTimeToFile(
                            file, CommonMethods.createImageFile(mActivity) ?: return@mapNotNull null
                        )
                    }

                    withContext(Dispatchers.Main) {
                        if (modifiedImageFiles.isNotEmpty()) {
                            if (imageAnyList.size + modifiedImageFiles.size > 4) {
                                CommonMethods.showToastMessage(
                                    mActivity, "You can only display 4 image items."
                                )
                                return@withContext
                            }

                            imageAnyList.addAll(modifiedImageFiles)
                            imageAdapter?.addImage(imageAnyList, false)
                            handleAssetRVView(imageAnyList.size)
                        }
                    }
                }
            },
            onError = {
                CommonMethods.showToastMessage(mActivity, it)
            }, remainingLimit
        )
    }


    private fun toggleSectionVisibility(view: View, toggleIcon: ImageView) {
        if (isExpanded) {
            CommonMethods.collapse(view)
            toggleIcon.setImageResource(R.drawable.ic_add_circle)
        } else {
            CommonMethods.expand(view)
            toggleIcon.setImageResource(R.drawable.ic_remove_circle)
        }
        isExpanded = !isExpanded
    }

    private fun setWidgetsClickability(flag: Boolean) {
        binding.llSelectPlace.isEnabled = false
        binding.tvDateForExpense.isEnabled = false
        binding.etExpenseAmount.isEnabled = flag
        binding.etRemarks.isEnabled = flag
        binding.tvSelectControlMode.isEnabled = false
        binding.flExpnseType.isEnabled = false
        binding.spExpenseType.isEnabled = false
        binding.tvAddExpense.visibility = if (flag) View.VISIBLE else View.GONE

        if (flag) {
            isEditMode = true
            //binding.toolbar.imgEdit.visibility = View.GONE
        }
    }

    private suspend fun executeAPIsAndSetupData() {
        withContext(Dispatchers.IO) {
            try {
                val companyListDeferred = async { callCompanyListApi() }
                companyListDeferred.await()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("TAG", "executeAPIsAndSetupData: " + e.message.toString())
            }
        }
    }

    private fun callCompanyListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val objReq = JsonObject()
        objReq.addProperty("companyMasterId", ID_ZERO)
        objReq.addProperty("userId", loginData.userId)
        objReq.addProperty("ParameterString", FORM_ID_EXPENSE_ENTRY)

        val companyCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getCompanyMasterList(objReq)

        companyCall?.enqueue(object : Callback<List<CompanyMasterResponse>> {
            override fun onResponse(
                call: Call<List<CompanyMasterResponse>>,
                response: Response<List<CompanyMasterResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            companyMasterList.clear()
                            companyMasterList.add(
                                CompanyMasterResponse(
                                    companyMasterId = 0,
                                    companyName = mActivity.getString(R.string.select_company),
                                )
                            )
                            companyMasterList.addAll(it)
                            if (it.size == 1) {
                                selectedCompany = CompanyMasterResponse(
                                    companyMasterId = companyMasterList[1].companyMasterId,
                                    companyName = companyMasterList[1].companyName
                                )
                                binding.tvSelectCompany.text = selectedCompany?.companyName ?: ""
                                isCompanyChange = true
                            }
                            callBranchListApi()
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

            override fun onFailure(call: Call<List<CompanyMasterResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if (mActivity != null) {
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

    private fun callCityListApi() {
        Log.e("TAG", "callCityListApi: ")
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)
        val apiDate = CommonMethods.convertToAppDateFormat(binding.tvDateForExpense.text.toString(), "MM/dd/yyyy")

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val placeListReq = JsonObject()
        placeListReq.addProperty("StateId", 0)
        placeListReq.addProperty("CityId", 0)
        placeListReq.addProperty("UserId", loginData.userId)
        placeListReq.addProperty("ExpenseDate", apiDate)

        val placeListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getExpenseCityList(placeListReq)

        placeListCall?.enqueue(object : Callback<List<ExpenseCityListResponse>> {
            override fun onResponse(
                call: Call<List<ExpenseCityListResponse>>,
                response: Response<List<ExpenseCityListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let { it ->
                        if (it.isNotEmpty()) {
                            placeList.clear()
                            placeList.addAll(it)
                            if (isForUpdate) {
                                selectedPlace = ExpenseCityListResponse(
                                    0,
                                    expenseDetailsResponse.cityId,
                                    expenseDetailsResponse.cityName ?: "",
                                    0,
                                    ""
                                )
                                binding.tvSelectPlace.text = selectedPlace.cityName
                                callGetExpenseTypeList(selectedPlace.cityId)
                            }
                        }
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.error),
                        mActivity.getString(R.string.error_message),
                        null,
                        isCancelVisibility = false
                    )
                }
            }

            override fun onFailure(call: Call<List<ExpenseCityListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if (mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.error),
                        t.message,
                        null,
                        isCancelVisibility = false
                    )
                }
            }

        })

    }

    private fun callBranchListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val objReq = JsonObject()
        objReq.addProperty("BranchMasterId", ID_ZERO)
        objReq.addProperty("UserId", loginData.userId)
        objReq.addProperty(
            "ParameterString",
            "CompanyMasterId=${selectedCompany?.companyMasterId} and $FORM_ID_EXPENSE_ENTRY"
        )

        val branchCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getBranchMasterList(objReq)

        branchCall?.enqueue(object : Callback<List<BranchMasterResponse>> {
            override fun onResponse(
                call: Call<List<BranchMasterResponse>>,
                response: Response<List<BranchMasterResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            branchMasterList.clear()
                            branchMasterList.add(
                                BranchMasterResponse(
                                    branchMasterId = 0,
                                    branchName = mActivity.getString(R.string.select_branch)
                                )
                            )
                            branchMasterList.addAll(it)
                            if (isCompanyChange && it.size == 1) {
                                selectedBranch = BranchMasterResponse(
                                    branchMasterId = branchMasterList[1].branchMasterId,
                                    branchName = branchMasterList[1].branchName
                                )
                                binding.tvSelectBranch.text = selectedBranch?.branchName ?: ""
                                callDivisionListApi()
                            }
                        } else {
                            CommonMethods.showToastMessage(
                                mActivity,
                                getString(R.string.branch_list_not_found)
                            )
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

            override fun onFailure(call: Call<List<BranchMasterResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if (mActivity != null) {
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

    private fun callDivisionListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val objReq = JsonObject()
        objReq.addProperty("divisionMasterId", ID_ZERO)
        objReq.addProperty("userId", loginData.userId)
        objReq.addProperty(
            "ParameterString",
            "CompanyMasterId=${selectedCompany?.companyMasterId} and BranchMasterId=${selectedBranch?.branchMasterId} and $FORM_ID_EXPENSE_ENTRY"
        )

        val divisionCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getDivisionMasterList(objReq)

        divisionCall?.enqueue(object : Callback<List<DivisionMasterResponse>> {
            override fun onResponse(
                call: Call<List<DivisionMasterResponse>>,
                response: Response<List<DivisionMasterResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            divisionMasterList.clear()
                            divisionMasterList.add(
                                DivisionMasterResponse(
                                    divisionMasterId = 0,
                                    divisionName = mActivity.getString(R.string.select_division)
                                )
                            )
                            divisionMasterList.addAll(it)
                            if (isCompanyChange && it.size == 1) {
                                selectedDivision = DivisionMasterResponse(
                                    divisionMasterId = divisionMasterList[1].divisionMasterId,
                                    divisionName = divisionMasterList[1].divisionName
                                )
                                binding.tvSelectDivision.text = selectedDivision?.divisionName ?: ""
                                callCategoryListApi()
                            }
                        } else {
                            CommonMethods.showToastMessage(
                                mActivity,
                                getString(R.string.division_list_not_found)
                            )
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

            override fun onFailure(call: Call<List<DivisionMasterResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if (mActivity != null) {
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

    private fun callCategoryListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        if (selectedDivision == null || selectedDivision?.divisionMasterId == 0) {
            CommonMethods.showToastMessage(
                mActivity,
                mActivity.getString(R.string.please_select_branch)
            )
            return;
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val jsonReq = JsonObject()
        jsonReq.addProperty("UserId", loginData.userId)
        jsonReq.addProperty(
            "ParameterString",
            "CompanyMasterId=${selectedCompany?.companyMasterId} and BranchMasterId=${selectedBranch?.branchMasterId} and DivisionMasterid=${selectedDivision?.divisionMasterId} and $FORM_ID_EXPENSE_ENTRY"
        )

        val visitTypeCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getCategoryMasterList(jsonReq)

        visitTypeCall?.enqueue(object : Callback<List<CategoryMasterResponse>> {
            override fun onResponse(
                call: Call<List<CategoryMasterResponse>>,
                response: Response<List<CategoryMasterResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            categoryList.clear()
                            categoryList.add(CategoryMasterResponse(categoryName = "Select Category"))
                            categoryList.addAll(it)
                            setupCategorySpinner()
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

            override fun onFailure(call: Call<List<CategoryMasterResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if (mActivity != null) {
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

    private fun callExpenseDetailListApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)
        val appRegistrationData = appDao.getAppRegistration()

        val loginData = appDao.getLoginData()
        val expenseListReq = JsonObject()
        expenseListReq.addProperty("userId", loginData.userId)
        expenseListReq.addProperty("expenseId", expenseId)

        val expenseListCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getExpenseListDetail(expenseListReq)

        expenseListCall?.enqueue(object : Callback<List<ExpenseDetailListResponse>> {
            override fun onResponse(
                call: Call<List<ExpenseDetailListResponse>>,
                response: Response<List<ExpenseDetailListResponse>>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.isNotEmpty() && it.size > 0) {
                            setupOrderDetailsData(it[0], appRegistrationData)
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

//                if (isSuccess(response)) {
//                    response.body()?.let {
//                        if (it.isNotEmpty()) {
//                            setupOrderDetailsData(it[0], appRegistrationData)
//                        }
//                    }
//                } else {
//                    CommonMethods.showAlertDialog(
//                        mActivity,
//                        getString(R.string.error),
//                        getString(R.string.error_message),
//                        null
//                    )
//                }
            }

            override fun onFailure(call: Call<List<ExpenseDetailListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                Log.e("TAG", "onFailure: " + t.message.toString())
                if (mActivity != null) {
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

    private fun setupOrderDetailsData(
        expenseDetails: ExpenseDetailListResponse,
        appRegistrationData: AppRegistrationResponse
    ) {
        expenseDetailsResponse = expenseDetails
        binding.tvExpenseType.text = expenseDetails.expenseTypeName
        binding.tvSelectPlace.text = expenseDetails.cityName
        Log.d("COMPANY--->", "" + expenseDetails.cityName)
        binding.tvSelectControlMode.text = expenseDetails.controlModeName
        binding.tvSelectVehicalType.text = expenseDetails.vehicleTypeName

        binding.etRemarks.setText(expenseDetails.remarks)
        binding.tvEligibleAmount.text = expenseDetails.eligibleAmount.toString()
        binding.etExpenseAmount.setText(expenseDetails.expenseAmount.toString())

        binding.tvSelectCompany.text = expenseDetails.companyName
        binding.llSelectCompany.isClickable = false

        binding.tvSelectBranch.text = expenseDetails.branchName
        binding.llSelectBranch.isClickable = false

        binding.tvSelectDivision.text = expenseDetails.divisionName
        binding.llSelectDivision.isClickable = false

        //var catSP = expenseDetails.CategoryName

        binding.tvDateForExpense.text = expenseDetails.expenseDate
        detail = expenseDetails.categoryName
        binding.tvDateForExpense.isClickable = false

        if (expenseId > 0) {
            binding.flCategory.visibility = View.GONE
            binding.tvCategory.visibility = View.VISIBLE
            binding.tvCategory.setText(detail)
            Log.d("TEXT===>", "" + detail)
        }

        selectedCategory = CategoryMasterResponse(
            categoryMasterId = expenseDetails.categoryMasterId,
            categoryName = expenseDetails.categoryName
        )

//        if (expenseDetails.categoryName.isNotEmpty()){
//            binding.spCategory.setSelection(1)
//            binding.tvCategory.text = expenseDetails.categoryName
//        }

        selectedCompany = CompanyMasterResponse(
            companyMasterId = expenseDetails.companyMasterId,
            companyName = expenseDetails.companyName
        )
        selectedBranch = BranchMasterResponse(
            branchMasterId = expenseDetails.branchMasterId,
            branchName = expenseDetails.branchName
        )
        selectedDivision = DivisionMasterResponse(
            divisionMasterId = expenseDetails.divisionMasterId,
            divisionName = expenseDetails.divisionName
        )
        selectedCategory = CategoryMasterResponse(
            categoryMasterId = expenseDetails.categoryMasterId,
            categoryName = expenseDetails.categoryName
        )

        Log.d("COMPANY--->", "" + expenseDetails.companyName)
        Log.d("BRANCH--->", "" + expenseDetails.branchName)

        Log.d("DIVISION--->", "" + expenseDetails.divisionName)

        if ((expenseDetails.filePath1 ?: "").isNotEmpty()) {
            imageAnyList.add((expenseDetails.filePath1) ?: "")
        }
        if ((expenseDetails.filePath2 ?: "").isNotEmpty()) {
            imageAnyList.add((expenseDetails.filePath2) ?: "")
        }
        if ((expenseDetails.filePath3 ?: "").isNotEmpty()) {
            imageAnyList.add((expenseDetails.filePath3) ?: "")
        }
        if ((expenseDetails.filePath4 ?: "").isNotEmpty()) {
            imageAnyList.add((expenseDetails.filePath4) ?: "")
        }

        Log.e("TAG", "setupDataForUpdate:BEFORE " + imageAnyList.toString())

        imageAdapter?.addImage(imageAnyList, true)
        handleAssetRVView(imageAnyList.size)

        // imageUrl = orderDetails.filePath ?: ""
        //calculateTotalOrderAmount()
    }

    private fun callGetExpenseTypeList(placeId: Int) {
        Log.e("TAG", "callGetExpenseTypeList: ")
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()
        val apiDate = CommonMethods.convertToAppDateFormat(binding.tvDateForExpense.text.toString(), "MM/dd/yyyy")


        val expenseTypeReq = JsonObject()
        expenseTypeReq.addProperty("UserId", loginData.userId)
        expenseTypeReq.addProperty("CityId", placeId)
        expenseTypeReq.addProperty("ExpenseDate", apiDate)

        val expenseTypeCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.getExpenseTypeList(expenseTypeReq)

        expenseTypeCall?.enqueue(object : Callback<List<ExpenseTypeListResponse>> {
            override fun onResponse(
                call: Call<List<ExpenseTypeListResponse>>,
                response: Response<List<ExpenseTypeListResponse>>
            ) {
                Log.d("EXPENSETYPERESPONSE===>", "" + response.toString())
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let { it ->
                        if (it.isNotEmpty()) {
                            Log.d("EXPENSETYPERESPONSE1===>", "" + response.body())
                            expenseTypeList.clear()
                            expenseTypeList.add(ExpenseTypeListResponse())
                            expenseTypeList.addAll(it)
                            setupExpenseTypeSpinner(expenseTypeList)
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

            override fun onFailure(call: Call<List<ExpenseTypeListResponse>>, t: Throwable) {
                CommonMethods.hideLoading()
                if (mActivity != null) {
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

    private fun setupExpenseTypeSpinner(expenseTypeList: ArrayList<ExpenseTypeListResponse>) {

        val adapter = ExpenseTypeAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            expenseTypeList
        )
        binding.spExpenseType.adapter = adapter

        if (isForUpdate) {
            selectedExpenseType = ExpenseTypeListResponse(
                expenseDataForUpdate.expenseTypeId,
                expenseDataForUpdate.expenseTypeName,
                expenseDataForUpdate.controlModeId,
                expenseDataForUpdate.controlModeName,
                expenseDataForUpdate.vehicleTypeId,
                expenseDataForUpdate.vehicleTypeName,
                0,
                0,
                true,
                "",
                expenseDataForUpdate.mapKm,
                expenseDataForUpdate.actualKm,
                expenseDataForUpdate.eligibleAmount,
                expenseDataForUpdate.expenseAmount,
                expenseDataForUpdate.expenseDate
            )
            isAttachmentRequired = selectedExpenseType.isAttachmentRequired
            val pos =
                expenseTypeList.indexOfFirst { it.expenseTypeId == selectedExpenseType.expenseTypeId }
            if (pos != -1) {
                binding.spExpenseType.setSelection(pos)
            }
            binding.tvSelectControlMode.text = selectedExpenseType.controlModeName
            binding.tvSelectVehicalType.text = selectedExpenseType.vehicleTypeName

//            binding.tvSelectCompany.text = expenseDataForUpdate.companyName
//            binding.tvSelectBranch.text = expenseDataForUpdate.branchName
//            binding.tvSelectDivision.text = expenseDataForUpdate.divisionName
//
//            binding.tvCategory.text = expenseDataForUpdate.cityCategoryName//check categoryname
        } else {
            binding.spExpenseType.setSelection(0)
            selectedExpenseType = expenseTypeList[0]
            isAttachmentRequired = selectedExpenseType.isAttachmentRequired
            binding.spExpenseType.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    val expenseData = expenseTypeList[position]

                    // Display the selected item in the TextView
                    if (binding.spExpenseType.selectedItemPosition != 0) {
                        if (!isForUpdate && expenseData.expenseTypeId > 0) {
                            binding.tvDateForExpense.isEnabled = false
                            Log.e("TAG", "onItemSelected: " + expenseData.expenseTypeId)
                            selectedExpenseType = expenseData
                            isAttachmentRequired = selectedExpenseType.isAttachmentRequired
                            binding.tvSelectControlMode.text = expenseData.controlModeName
                            binding.tvSelectVehicalType.text = expenseData.vehicleTypeName

//                            binding.tvSelectCompany.text = expenseDataForUpdate.companyName
//                            binding.tvSelectBranch.text = expenseDataForUpdate.branchName
//                            binding.tvSelectDivision.text = expenseDataForUpdate.divisionName
//
//                            binding.tvCategory.text = expenseDataForUpdate.cityCategoryName//check categoryname

                            binding.tvEligibleAmount.text =
                                expenseData.eligibleAmount.toString()//it[0].eligibleAmount.toString()
                            binding.etExpenseAmount.setText(expenseData.expenseAmount.toString())
                            if (selectedExpenseType.controlModeId == CONTROL_TYPE_FIX_PER_KM) {
                                binding.tvEligibleAmountLabel.text =
                                    "Eligible Amount (MAP KM:${expenseData.mapKM})"
                                binding.tvExpenseAmountLabel.text =
                                    "Expense Amount (Actual KM:${expenseData.actualKM})"
                            } else {
                                binding.tvEligibleAmountLabel.text =
                                    mActivity.getString(R.string.eligible_amount)
                                binding.tvExpenseAmountLabel.text =
                                    mActivity.getString(R.string.expense_amount)
                            }

                            Log.e("TAG", "getView: ")
                            //callExpenseLimitApi()
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }
        }


    }

//    private fun callAddExpenseApi() {
//        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
//            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
//            return
//        }
//        CommonMethods.showLoading(mActivity)
//
//        val appRegistrationData = appDao.getAppRegistration()
//        val loginData = appDao.getLoginData()
//        val expenseAmount = if(binding.etExpenseAmount.text.toString().isEmpty()) 0 else binding.etExpenseAmount.text.toString().trim().toDouble()
//
//        val addUpdateExpenseReq = JsonObject()
//        addUpdateExpenseReq.addProperty(
//            "ExpenseId",
//            if (isForUpdate) expenseDataForUpdate.expenseId else 0
//        )
//        addUpdateExpenseReq.addProperty("UserId", loginData.userId)
//        addUpdateExpenseReq.addProperty("ExpenseAmount", expenseAmount)
//        addUpdateExpenseReq.addProperty("Remarks", binding.etRemarks.text.toString())
//        addUpdateExpenseReq.addProperty("DocumentPath", base64Image.ifEmpty { "" })
//
//        if (!isForUpdate) {
//            addUpdateExpenseReq.addProperty(
//                "CityId",
//                if (isForUpdate) expenseDataForUpdate.cityId else selectedPlace.cityId
//            )
//            addUpdateExpenseReq.addProperty("ExpenseDate", binding.tvDate.text.toString())
//            addUpdateExpenseReq.addProperty(
//                "ExpenseTypeId",
//                if (isForUpdate) expenseDataForUpdate.expenseTypeId else selectedExpenseType.expenseTypeId
//            )
//            addUpdateExpenseReq.addProperty(
//                "ControlModeId",
//                if (isForUpdate) expenseDataForUpdate.controlModeId else selectedExpenseType.controlModeId
//            )
//            addUpdateExpenseReq.addProperty(
//                "VehicleTypeId",
//                if (isForUpdate) expenseDataForUpdate.vehicleTypeId else selectedExpenseType.vehicleTypeId
//            )
//            addUpdateExpenseReq.addProperty(
//                "EligibleAmount",
//                binding.tvEligibleAmount.text.toString()
//            )
//            addUpdateExpenseReq.addProperty("CreateBy", loginData.userId)
//            addUpdateExpenseReq.addProperty("TripId", 0)
//            addUpdateExpenseReq.addProperty("StatusId", 1)
//            if (selectedExpenseType.controlModeId == CONTROL_TYPE_FIX_PER_KM) {
//                addUpdateExpenseReq.addProperty(
//                    "MapKm",
//                    if (isForUpdate) expenseDataForUpdate.mapKm else selectedExpenseType.mapKM
//                )
//                addUpdateExpenseReq.addProperty(
//                    "ActualKm",
//                    if (isForUpdate) expenseDataForUpdate.actualKm else selectedExpenseType.actualKM
//                )
//            }
//        }
//
//
//        print("MY REQ ::::::: " + addUpdateExpenseReq)
//        Log.e("TAG", "callAddExpenseApi: ADD EXPENSE REQ :: " + addUpdateExpenseReq)
//
//        val addUpdateExpenseCall = if (isForUpdate) {
//            WebApiClient.getInstance(mActivity)
//                .webApi_without(appRegistrationData.apiHostingServer)
//                ?.updateExpense(addUpdateExpenseReq)
//        } else {
//            WebApiClient.getInstance(mActivity)
//                .webApi_without(appRegistrationData.apiHostingServer)
//                ?.addExpense(addUpdateExpenseReq)
//        }
//
//
//        addUpdateExpenseCall?.enqueue(object : Callback<AddExpenseResponse> {
//            override fun onResponse(
//                call: Call<AddExpenseResponse>,
//                response: Response<AddExpenseResponse>
//            ) {
//                CommonMethods.hideLoading()
//                if (isSuccess(response)) {
//                    response.body()?.let { it ->
//                        Log.e("TAG", "onResponse: ADD EXPENSE RESPONSE :: " + it.toString())
//                        CommonMethods.showAlertDialog(
//                            mActivity,
//                            if (isForUpdate) mActivity.getString(R.string.update_expense) else mActivity.getString(R.string.add_expense_dialog_title),
//                            it.returnMessage,
//                            object : PositiveButtonListener {
//                                override fun okClickListener() {
//                                    if (it.success) {
//                                        AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, true)
//                                        if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
//                                            mActivity.onBackPressedDispatcher.onBackPressed()
//                                        } else {
//                                            mActivity.onBackPressed()
//                                        }
//                                    }
//                                }
//                            }, isCancelVisibility = false)
//                    }
//                } else {
//                    CommonMethods.showAlertDialog(
//                        mActivity,
//                        "Error",
//                        mActivity.getString(R.string.error_message),
//                        null,
//                        isCancelVisibility = false
//                    )
//                }
//            }
//
//            override fun onFailure(call: Call<AddExpenseResponse>, t: Throwable) {
//                CommonMethods.hideLoading()
//                if(mActivity != null) {
//                    CommonMethods.showAlertDialog(
//                        mActivity,
//                        mActivity.getString(R.string.error),
//                        t.message,
//                        null,
//                        isCancelVisibility = false
//                    )
//                }
//            }
//
//        })
//
//    }

    private fun callAddExpenseApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, mActivity.getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)
        val apiDate = CommonMethods.convertToAppDateFormat(binding.tvDateForExpense.text.toString(), "MM/dd/yyyy")


        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()
        val expenseAmount = if (binding.etExpenseAmount.text.toString()
                .isEmpty()
        ) 0 else binding.etExpenseAmount.text.toString().trim().toDouble()

        val addUpdateExpenseReq = JsonObject()
        addUpdateExpenseReq.addProperty(
            "expenseId",
            if (isForUpdate) expenseDetailsResponse.expenseId else 0
        )
        addUpdateExpenseReq.addProperty("userId", loginData.userId)
        addUpdateExpenseReq.addProperty("attendanceId", expenseDetailsResponse.attendanceId)
        //if (!isForUpdate) {
        addUpdateExpenseReq.addProperty("expenseDate", apiDate)
        addUpdateExpenseReq.addProperty(
            "cityId",
            if (isForUpdate) expenseDetailsResponse.cityId else selectedPlace.cityId
        )
        addUpdateExpenseReq.addProperty(
            "expenseTypeId",
            if (isForUpdate) expenseDetailsResponse.expenseTypeId else selectedExpenseType.expenseTypeId
        )
        addUpdateExpenseReq.addProperty(
            "controlModeId",
            if (isForUpdate) expenseDetailsResponse.controlModeId else selectedExpenseType.controlModeId
        )
        addUpdateExpenseReq.addProperty(
            "vehicleTypeId",
            if (isForUpdate) expenseDetailsResponse.vehicleTypeId else selectedExpenseType.vehicleTypeId
        )
        //addUpdateExpenseReq.addProperty("eligibleAmount", binding.tvEligibleAmount.text.toString())
        addUpdateExpenseReq.addProperty("EligibleAmount", binding.tvEligibleAmount.text.toString())
        addUpdateExpenseReq.addProperty("expenseAmount", expenseAmount)
        if (selectedExpenseType.controlModeId == CONTROL_TYPE_FIX_PER_KM) {
            addUpdateExpenseReq.addProperty(
                "mapKm",
                if (isForUpdate) expenseDetailsResponse.mapKm else selectedExpenseType.mapKM
            )
            addUpdateExpenseReq.addProperty(
                "actualKm",
                if (isForUpdate) expenseDetailsResponse.actualKm else selectedExpenseType.actualKM
            )
        }
        addUpdateExpenseReq.addProperty("remarks", binding.etRemarks.text.toString())
        addUpdateExpenseReq.addProperty("documentPath", expenseDetailsResponse.documentPath)
        addUpdateExpenseReq.addProperty("statusId", 1)
        addUpdateExpenseReq.addProperty("tripId", 0)
        addUpdateExpenseReq.addProperty("createBy", loginData.userId)
        addUpdateExpenseReq.addProperty("createDateTime", "1")
        addUpdateExpenseReq.addProperty("companyMasterId", selectedCompany?.companyMasterId)
        addUpdateExpenseReq.addProperty("branchMasterId", selectedBranch?.branchMasterId)
        addUpdateExpenseReq.addProperty("divisionMasterId", selectedDivision?.divisionMasterId)
        addUpdateExpenseReq.addProperty("expenseFinalApprovedStatus", 0)
        addUpdateExpenseReq.addProperty("expenseFinalRejectedStatus", 0)
        addUpdateExpenseReq.addProperty("categoryMasterId", selectedCategory?.categoryMasterId)
        if (imageAnyList.size > 0) {
            if (imageAnyList[0] is String) {
                addUpdateExpenseReq.addProperty("FilePath1", imageAnyList[0].toString())
            } else {
                val imageBase64 =
                    CommonMethods.convertImageFileToBase64(imageAnyList[0] as File).toString()
                addUpdateExpenseReq.addProperty("FilePath1", imageBase64)
            }
            if (imageAnyList.size > 1) {
                if (imageAnyList[1] is String) {
                    addUpdateExpenseReq.addProperty("FilePath2", imageAnyList[1].toString())
                } else {
                    val imageBase64 =
                        CommonMethods.convertImageFileToBase64(imageAnyList[1] as File).toString()
                    addUpdateExpenseReq.addProperty("FilePath2", imageBase64)
                }
            } else {
                addUpdateExpenseReq.addProperty("FilePath2", "")
            }
            if (imageAnyList.size > 2) {
                if (imageAnyList[2] is String) {
                    addUpdateExpenseReq.addProperty("FilePath3", imageAnyList[2].toString())
                } else {
                    val imageBase64 =
                        CommonMethods.convertImageFileToBase64(imageAnyList[2] as File).toString()
                    addUpdateExpenseReq.addProperty("FilePath3", imageBase64)
                }
            } else {
                addUpdateExpenseReq.addProperty("FilePath3", "")
            }
            if (imageAnyList.size > 3) {
                if (imageAnyList[3] is String) {
                    addUpdateExpenseReq.addProperty("FilePath4", imageAnyList[3].toString())
                } else {
                    val imageBase64 =
                        CommonMethods.convertImageFileToBase64(imageAnyList[3] as File).toString()
                    addUpdateExpenseReq.addProperty("FilePath4", imageBase64)
                }
            } else {
                addUpdateExpenseReq.addProperty("FilePath4", "")
            }
        } else {
            addUpdateExpenseReq.addProperty("FilePath1", "")
            addUpdateExpenseReq.addProperty("FilePath2", "")
            addUpdateExpenseReq.addProperty("FilePath3", "")
            addUpdateExpenseReq.addProperty("FilePath4", "")
        }
        // }


        print("MY REQ ::::::: " + addUpdateExpenseReq)
        Log.e("TAG", "callAddExpenseApi: ADD EXPENSE REQ :: " + addUpdateExpenseReq)

        /*val addUpdateExpenseCall = if (isForUpdate) {
            WebApiClient.getInstance(mActivity)
                .webApi_without(appRegistrationData.apiHostingServer)
                ?.updateExpense(addUpdateExpenseReq)
        } else {
            WebApiClient.getInstance(mActivity)
                .webApi_without(appRegistrationData.apiHostingServer)
                ?.addExpense(addUpdateExpenseReq)
        }*/

        val addUpdateExpenseCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.insertUpdateExpense(addUpdateExpenseReq)

        addUpdateExpenseCall?.enqueue(object : Callback<AddExpenseResponse> {
            override fun onResponse(
                call: Call<AddExpenseResponse>,
                response: Response<AddExpenseResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let { it ->
                        Log.e("TAG", "onResponse: ADD EXPENSE RESPONSE :: " + it.toString())
                        CommonMethods.showAlertDialog(
                            mActivity,
                            if (isForUpdate) mActivity.getString(R.string.update_expense) else mActivity.getString(
                                R.string.add_expense_dialog_title
                            ),
                            it.returnMessage,
                            object : PositiveButtonListener {
                                override fun okClickListener() {
                                    if (it.success) {
                                        AppPreference.saveBooleanPreference(
                                            mActivity,
                                            IS_DATA_UPDATE,
                                            true
                                        )
                                        if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                                            mActivity.onBackPressedDispatcher.onBackPressed()
                                        } else {
                                            mActivity.onBackPressed()
                                        }
                                    }
                                }
                            }, isCancelVisibility = false
                        )
                    }
                } else {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        "Error",
                        mActivity.getString(R.string.error_message),
                        null,
                        isCancelVisibility = false
                    )
                }
            }

            override fun onFailure(call: Call<AddExpenseResponse>, t: Throwable) {
                CommonMethods.hideLoading()
                if (mActivity != null) {
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.error),
                        t.message,
                        null,
                        isCancelVisibility = false
                    )
                    Log.d("ADDEXPENSEMESSAGE===>", "" + t.message)
                }
            }

        })

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                AppPreference.saveBooleanPreference(mActivity, IS_DATA_UPDATE, false)
                if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                    mActivity.onBackPressedDispatcher.onBackPressed()
                } else {
                    mActivity.onBackPressed()
                }
            }

            R.id.tvDateForExpense -> {
                val selectedCalendar = if (binding.tvDateForExpense.text.toString()
                        .isEmpty()
                ) null else CommonMethods.dateStringToCalendar(binding.tvDateForExpense.text.toString())
                openDatePicker(
                    selectedCalendar
                ) { selectedDate ->
                    if (binding.tvDateForExpense.text.toString().isNotEmpty()) {
                        binding.tvDateForExpense.text = selectedDate
                    } else {
                        binding.tvDateForExpense.text = selectedDate
                    }
                    binding.tvSelectPlace.text = ""
                    selectedPlace = ExpenseCityListResponse();
                    callCityListApi()
                }
            }

            R.id.llSelectPlace -> {
                if (placeList.size > 0 && selectedExpenseType.expenseTypeId == 0) {
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

//            R.id.cardImage -> {
//                if (isPreviewImage) {
//                    Log.e("TAG", "onClick: preview iamge")
//                    ImagePreviewCommonDialog.showImagePreviewDialog(
//                        mActivity,
//                        appDatabase.appDao()
//                            .getAppRegistration().apiHostingServer + expenseDataForUpdate.documentPath
//                    )
//                } else {
//                    askCameraGalleryPermission()
//                }
//            }

            R.id.cardImageCapture -> {
                if (imageAnyList.size == 4) {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.photo_upload_limit_reached)
                    )
                    return
                }
                askCameraGalleryPermission()
            }

            R.id.llSelectCompany -> {
                if (companyMasterList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_COMPANY,
                        companyList = companyMasterList,
                        companyInterfaceDetect = this as UserSearchDialogUtil.CompanyDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    userDialog.showUserSearchDialog()
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.company_list_not_found)
                    )
                }
            }

            R.id.llSelectBranch -> {
                if (selectedCompany == null || selectedCompany?.companyMasterId == 0) {
                    CommonMethods.showToastMessage(
                        mActivity,
                        mActivity.getString(R.string.please_select_company)
                    )
                    return;
                }
                if (branchMasterList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_BRANCH,
                        branchList = branchMasterList,
                        branchInterfaceDetect = this as UserSearchDialogUtil.BranchDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    userDialog.showUserSearchDialog()
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.branch_list_not_found)
                    )
                }
            }

            R.id.llSelectDivision -> {
                if (selectedBranch == null || selectedBranch?.branchMasterId == 0) {
                    CommonMethods.showToastMessage(
                        mActivity,
                        mActivity.getString(R.string.please_select_branch)
                    )
                    return;
                }
                if (divisionMasterList.size > 0) {
                    val userDialog = UserSearchDialogUtil(
                        mActivity,
                        type = FOR_DIVISION,
                        divisionList = divisionMasterList,
                        divisionInterfaceDetect = this as UserSearchDialogUtil.DivisionDialogDetect,
                        userDialogInterfaceDetect = null
                    )
                    userDialog.showUserSearchDialog()
                } else {
                    CommonMethods.showToastMessage(
                        mActivity,
                        getString(R.string.branch_list_not_found)
                    )
                }
            }

            R.id.tvAddExpense -> {
                addExpenseValidation()
            }

            R.id.tvAccept -> {
                if (binding.etExpenseApprovalRemarks.text.toString().isEmpty()) {
                    binding.etExpenseApprovalRemarks.requestFocus()
                    CommonMethods.showToastMessage(
                        mActivity,
                        mActivity.getString(R.string.enter_approval_remarks)
                    )
                    return
                }
                callApproveRejectExpense(
                    true,
                    binding.etExpenseApprovalRemarks.text.toString().trim(),
                    expenseDataForUpdate.expenseId,
                    binding.etApprovalAmount.text.toString().trim().toDouble()
                )
                //showRemarksDialog(true)
            }

            R.id.tvReject -> {
                if (binding.etExpenseApprovalRemarks.text.toString().isEmpty()) {
                    binding.etExpenseApprovalRemarks.requestFocus()
                    CommonMethods.showToastMessage(
                        mActivity,
                        mActivity.getString(R.string.enter_approval_remarks)
                    )
                    return
                }
                callApproveRejectExpense(
                    false,
                    binding.etExpenseApprovalRemarks.text.toString().trim(),
                    expenseDataForUpdate.expenseId,
                    binding.etApprovalAmount.text.toString().trim().toDouble()
                )
                //showRemarksDialog(false)
            }

            R.id.imgEdit -> {
                isPreviewImage = false
                if (expenseDataForUpdate.expenseStatusName == STATUS_APPROVED) {
                    binding.toolbar.tvHeader.text = mActivity.getString(R.string.update_expense)
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.update_expense),
                        mActivity.getString(R.string.approved_expense_msg),
                        isCancelVisibility = false,
                        okListener = null
                    )
                } else {
                    binding.toolbar.tvHeader.text = mActivity.getString(R.string.update_expense)
                    CommonMethods.showAlertDialog(
                        mActivity,
                        mActivity.getString(R.string.update_expense),
                        mActivity.getString(R.string.edit_msg),
                        positiveButtonText = mActivity.getString(R.string.yes),
                        negativeButtonText = mActivity.getString(R.string.no),
                        okListener = object : PositiveButtonListener {
                            override fun okClickListener() {
                                setWidgetsClickability(true)
                                CommonMethods.showToastMessage(
                                    mActivity,
                                    mActivity.getString(R.string.edit_this_expense)
                                )
                            }
                        }
                    )
                }
            }

            R.id.imgDelete -> {
                showRemarksDialog()
            }
        }
    }

   /* private fun openDatePicker(
        previousSelectedDate: Calendar? = null,
        onDateSelected: (String) -> Unit,
    ) {
        val calendar = Calendar.getInstance()

        previousSelectedDate?.let {
            calendar.timeInMillis = it.timeInMillis
        }

        val datePickerDialog = DatePickerDialog(
            mActivity,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                val dateString = dateFormat.format(calendar.time)
                onDateSelected(dateString)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate =
            CommonMethods.dateStringToCalendar(CommonMethods.getCurrentDate()).timeInMillis
        datePickerDialog.show()
    }*/

    private fun openDatePicker(
        previousSelectedDate: Calendar? = null,
        onDateSelected: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance()

        previousSelectedDate?.let {
            calendar.timeInMillis = it.timeInMillis
        }

        val datePickerDialog = DatePickerDialog(
            mActivity,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                val dateString = dateFormat.format(calendar.time)
                onDateSelected(dateString)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate =
            CommonMethods.dateStringToCalendar(CommonMethods.getCurrentDate()).timeInMillis
        datePickerDialog.show()
    }

    private fun addExpenseValidation() {
        if (binding.tvSelectPlace.text.toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.place_validation))
            return
        }
        if (binding.spExpenseType.selectedItemPosition == 0) {
            CommonMethods.run { showToastMessage(mActivity, getString(R.string.expense_type_msg)) }
            return
        }
        if (binding.etExpenseAmount.text.toString().isEmpty()) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.expense_amount_validation))
            return
        }
//        if (!isForUpdate) {
//            if (isAttachmentRequired && base64Image.isEmpty()) {
//                CommonMethods.showToastMessage(
//                    mActivity,
//                    getString(R.string.upload_expense_validation)
//                )
//                return
//            }
//        }
        callAddExpenseApi()
    }

    private fun showRemarksDialog() {
        val companyDialog = Dialog(mActivity)
        companyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        companyDialog.setCancelable(true)
        companyDialog.setContentView(R.layout.company_selection_dialog)

        val flCompany: FrameLayout = companyDialog.findViewById(R.id.flCompany)
        val tvLabel: TextView = companyDialog.findViewById(R.id.tvLabel)
        val etRemarks: EditText = companyDialog.findViewById(R.id.etRemarks)
        val btnSubmit: TextView = companyDialog.findViewById(R.id.btnSubmit)
        flCompany.visibility = View.GONE
        etRemarks.visibility = View.VISIBLE
        tvLabel.text = getString(R.string.remark)

        btnSubmit.setOnClickListener {
            if (etRemarks.text.toString().trim().isEmpty()) {
                CommonMethods.showToastMessage(mActivity, getString(R.string.enter_remark))
                return@setOnClickListener
            }
            companyDialog.dismiss()
            callDeleteExpense(etRemarks.text.toString().trim())
        }

        val window = companyDialog.window
        window!!.setLayout(
            AbsListView.LayoutParams.MATCH_PARENT,
            AbsListView.LayoutParams.WRAP_CONTENT
        )
        companyDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        companyDialog.show()
    }

    private fun callDeleteExpense(remarks: String) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val expenseDeleteReq = JsonObject()
        expenseDeleteReq.addProperty("UserId", loginData.userId)
        expenseDeleteReq.addProperty("ExpenseId", expenseDataForUpdate.expenseId)
        expenseDeleteReq.addProperty("Remarks", remarks)

        Log.e("TAG", "callAddLeaveApplicationApi: " + expenseDeleteReq.toString())
        CommonMethods.getBatteryPercentage(mActivity)

        val deleteExpenseCall = WebApiClient.getInstance(mActivity)
            .webApi_without(appRegistrationData.apiHostingServer)
            ?.expenseDelete(expenseDeleteReq)

        deleteExpenseCall?.enqueue(object : Callback<CommonSuccessResponse> {
            override fun onResponse(
                call: Call<CommonSuccessResponse>,
                response: Response<CommonSuccessResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.success) {
                            CommonMethods.showAlertDialog(mActivity,
                                getString(R.string.delete_expense),
                                it.returnMessage
                                    ?: getString(R.string.expense_delete_success),
                                object : PositiveButtonListener {
                                    override fun okClickListener() {
                                        AppPreference.saveBooleanPreference(
                                            mActivity,
                                            IS_DATA_UPDATE,
                                            true
                                        )
                                        if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                                            mActivity.onBackPressedDispatcher.onBackPressed()
                                        } else {
                                            mActivity.onBackPressed()
                                        }
                                    }
                                }, isCancelVisibility = false
                            )
                        } else {
                            CommonMethods.showAlertDialog(mActivity,
                                getString(R.string.expense_details),
                                it.returnMessage ?: "",
                                object : PositiveButtonListener {
                                    override fun okClickListener() {

                                    }
                                }, isCancelVisibility = false
                            )
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

            override fun onFailure(call: Call<CommonSuccessResponse>, t: Throwable) {
                CommonMethods.hideLoading()
                if (mActivity != null) {
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

    private fun callApproveRejectExpense(
        isExpenseApprove: Boolean,
        remarks: String,
        expenseId: Int,
        approvalAmount: Double
    ) {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            CommonMethods.showToastMessage(mActivity, getString(R.string.no_internet))
            return
        }
        CommonMethods.showLoading(mActivity)

        val appRegistrationData = appDao.getAppRegistration()
        val loginData = appDao.getLoginData()

        val expenseListReq = JsonObject()
        expenseListReq.addProperty("UserId", loginData.userId)
        expenseListReq.addProperty("ExpenseId", expenseId)
        expenseListReq.addProperty("ApprovedAmount", approvalAmount)
        expenseListReq.addProperty("Remarks", remarks)
        val leaveApprovalCall = if (isExpenseApprove) {
            WebApiClient.getInstance(mActivity)
                .webApi_without(appRegistrationData.apiHostingServer)
                ?.expenseApprove(expenseListReq)
        } else {
            WebApiClient.getInstance(mActivity)
                .webApi_without(appRegistrationData.apiHostingServer)
                ?.expenseReject(expenseListReq)
        }

        leaveApprovalCall?.enqueue(object : Callback<CommonSuccessResponse> {
            override fun onResponse(
                call: Call<CommonSuccessResponse>,
                response: Response<CommonSuccessResponse>
            ) {
                CommonMethods.hideLoading()
                if (isSuccess(response)) {
                    response.body()?.let {
                        if (it.success) {
                            CommonMethods.showAlertDialog(mActivity,
                                getString(R.string.expense_approval),
                                it.returnMessage/*if (isExpenseApprove) getString(R.string.expense_approve_msg) else getString(
                                    R.string.expense_reject_msg
                                )*/,
                                isCancelVisibility = false,
                                okListener = object : PositiveButtonListener {
                                    override fun okClickListener() {
                                        AppPreference.saveBooleanPreference(
                                            mActivity,
                                            IS_DATA_UPDATE,
                                            true
                                        )
                                        if (mActivity.onBackPressedDispatcher.hasEnabledCallbacks()) {
                                            mActivity.onBackPressedDispatcher.onBackPressed()
                                        } else {
                                            mActivity.onBackPressed()
                                        }
                                    }
                                })
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

            override fun onFailure(call: Call<CommonSuccessResponse>, t: Throwable) {
                CommonMethods.hideLoading()
                if (mActivity != null) {
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


    private fun askCameraGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val arrListOfPermission = arrayListOf<String>(
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOfPermission) {
                //openAlbum()
                openAlbumForList()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val arrListOfPermission = arrayListOf<String>(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOfPermission) {
                //openAlbum()
                openAlbumForList()
            }
        } else {
            val arrListOffPermission = arrayListOf<String>(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            PermissionUtil(mActivity).requestPermissions(arrListOffPermission) {
                //openAlbum()
                openAlbumForList()
            }
        }

    }

    private fun openAlbum() {
        AlbumUtility(mActivity, true).openAlbumAndHandleImageSelection(
            onImageSelected = {
                imageFile = it

                val executor = Executors.newSingleThreadExecutor()
                val handler = Handler(Looper.getMainLooper())
                executor.execute {
                    // This code runs on a background thread
                    val modifiedImageFile: File = CommonMethods.addDateAndTimeToFile(
                        it,
                        CommonMethods.createImageFile(mActivity)!!
                    )

                    if (modifiedImageFile != null) {

                        // Simulate a time-consuming task
                        Thread.sleep(1000)

                        // Update the UI on the main thread using runOnUiThread
                        handler.post {
                            Glide.with(mActivity)
                                .load(modifiedImageFile)
                            //.into(binding.imgExpense)
                            base64Image =
                                CommonMethods.convertImageFileToBase64(modifiedImageFile)
                                    .toString()
                            //binding.imgExpense.scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    }
                }
            },
            onError = {
                CommonMethods.showToastMessage(mActivity, it)
            }
        )

    }

    override fun placeSelect(placeData: ExpenseCityListResponse) {
        Log.e("TAG", "userSelect: " + placeData.cityName)
        binding.tvSelectPlace.text = placeData.cityName
        selectedPlace = placeData
        binding.tvDateForExpense.isEnabled = false
        callGetExpenseTypeList(selectedPlace.cityId)
    }

    inner class ExpenseTypeAdapter(
        private val context: Context, private val spinnerLayout: Int,
        private var expenseTypeList: ArrayList<ExpenseTypeListResponse>,
    ) :
        ArrayAdapter<ExpenseTypeListResponse>(context, spinnerLayout, expenseTypeList) {
        private val mContext: Context = context

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            val expenseData = expenseTypeList[position]
            name.text = expenseData.expenseTypeName
            Log.e("TAG", "getView: " + name.text.toString())
            /*if(binding.spExpenseType.selectedItemPosition != 0) {
                if (!isForUpdate && expenseData.expenseTypeId > 0) {
                    selectedExpenseType = expenseData
                    binding.tvSelectControlMode.text = expenseData.controlModeName
                    binding.tvSelectVehicalType.text = expenseData.vehicleTypeName

                    binding.tvEligibleAmount.text = expenseData.eligibleAmount.toString()//it[0].eligibleAmount.toString()
                    binding.etExpenseAmount.setText(expenseData.expenseAmount.toString())
                    if (selectedExpenseType.controlModeId == CONTROL_TYPE_FIX_PER_KM) {
                        binding.tvEligibleAmountLabel.text =
                            "Eligible Amount (MAP KM:${expenseData.mapKM})"
                        binding.tvExpenseAmountLabel.text =
                            "Expense Amount (Actual KM:${expenseData.actualKM})"
                    } else {
                        binding.tvEligibleAmountLabel.text =
                            getString(R.string.eligible_amount)
                        binding.tvExpenseAmountLabel.text =
                            getString(R.string.expense_amount)
                    }

                    Log.e("TAG", "getView: ")
                    //callExpenseLimitApi()
                }
            }*/
            return listItem
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItem = convertView
            if (listItem == null) listItem =
                LayoutInflater.from(mContext).inflate(R.layout.simple_spinner_item, parent, false)
            val name: TextView = listItem!!.findViewById(android.R.id.text1)
            val currentSupervisor = expenseTypeList[position]
            name.text = currentSupervisor.expenseTypeName
            return listItem
        }


    }

    override fun companySelect(dropDownData: CompanyMasterResponse) {
        selectedCompany = dropDownData
        isCompanyChange = true
        binding.tvSelectCompany.text = selectedCompany?.companyName ?: ""
        resetSelection(
            resetBranch = true,
            resetDivision = true,
            resetCategory = true
        )
        if ((selectedCompany?.companyMasterId ?: 0) > 0) {
            callBranchListApi()
        }
    }

    override fun branchSelect(dropDownData: BranchMasterResponse) {
        selectedBranch = dropDownData
        binding.tvSelectBranch.text = selectedBranch?.branchName ?: ""
        resetSelection(
            resetBranch = false,
            resetDivision = true,
            resetCategory = true
        )
        if ((selectedBranch?.branchMasterId ?: 0) > 0) {
            callDivisionListApi()
        }
    }

    override fun divisionSelect(dropDownData: DivisionMasterResponse) {
        selectedDivision = dropDownData
        binding.tvSelectDivision.text = selectedDivision?.divisionName ?: ""
        resetSelection(
            resetBranch = false,
            resetDivision = false,
            resetCategory = true
        )
        if ((selectedDivision?.divisionMasterId ?: 0) > 0) {
            callCategoryListApi()
        }
    }

    override fun onTypeSelect(typeData: CategoryMasterResponse) {
        selectedCategory = typeData
    }

    private fun setupCategorySpinner() {
        val adapter = LeaveTypeAdapter(
            mActivity,
            R.layout.simple_spinner_item,
            categoryList,
            this,
        )
        binding.spCategory.adapter = adapter
        if (isCompanyChange && categoryList.size == 2) {
            selectedCategory = CategoryMasterResponse(
                categoryMasterId = categoryList[1].categoryMasterId,
                categoryName = categoryList[1].categoryName
            )
            binding.spCategory.setSelection(1)
            categoryList[1]
        } else {
            Log.d("LIST", "" + categoryList.size)
//            for (i in categoryList.indices) {
//                if (categoryList[i].categoryMasterId == 1) {
//                    selectIndex = i
//                    Log.d("INDEX===>",""+selectIndex)
//                    binding.spCategory.setSelection(i)
//                    break
//                }
//                else{
//                    //Log.d("INDEX===>","false")
//                }
//            }

            if (isCompanyChange) {
                binding.spCategory.setSelection(0)
                categoryList[0]
            } else {
                val selectedCategoryIndex =
                    categoryList.indexOfFirst { it.categoryMasterId == selectedCategory?.categoryMasterId }
                binding.spCategory.setSelection(selectedCategoryIndex)
            }
        }
    }

    private fun resetSelection(
        resetBranch: Boolean,
        resetDivision: Boolean,
        resetCategory: Boolean
    ) {
        if (resetBranch) {
            selectedBranch = null
            binding.tvSelectBranch.hint = mActivity.getString(R.string.select_branch)
            binding.tvSelectBranch.text = ""
            branchMasterList.clear()
        }
        if (resetDivision) {
            selectedDivision = null
            binding.tvSelectDivision.hint = mActivity.getString(R.string.select_division)
            binding.tvSelectDivision.text = ""
            divisionMasterList.clear()
        }
        if (resetCategory) {
            selectedCategory = null
            binding.tvCategory.hint = mActivity.getString(R.string.select_category)
            binding.tvCategory.text = ""
            categoryList.clear()
            categoryList.add(CategoryMasterResponse(categoryName = "Select Category"))
            setupCategorySpinner()
        }
    }

}
