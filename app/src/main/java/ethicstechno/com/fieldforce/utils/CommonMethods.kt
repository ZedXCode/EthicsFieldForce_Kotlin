package ethicstechno.com.fieldforce.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.provider.Settings.Secure
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Polyline
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import ethicstechno.com.fieldforce.BuildConfig
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.listener.DatePickerListener
import ethicstechno.com.fieldforce.listener.FilterDialogListener
import ethicstechno.com.fieldforce.listener.ItemClickListener
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.dashboarddrill.FilterListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.moreoption.visit.CategoryMasterResponse
import ethicstechno.com.fieldforce.ui.activities.HomeActivity
import ethicstechno.com.fieldforce.ui.adapter.spinneradapter.DateOptionAdapter
import ethicstechno.com.fieldforce.ui.adapter.spinneradapter.FilterAdapter
import ethicstechno.com.fieldforce.ui.adapter.spinneradapter.VisitTypeAdapter
import ethicstechno.com.fieldforce.utils.dialog.SearchDialogUtil
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random


class CommonMethods {

    companion object {
        private var toast: Toast? = null
        var mAlertDialog: AlertDialog? = null
        val dateFormat = SimpleDateFormat(DATE_FORMAT)
        lateinit var searchDialog : SearchDialogUtil<AccountMasterList>
        lateinit var tvSelectPartyDealer: TextView
        var selectedPartyDealer: AccountMasterList = AccountMasterList()

        val dateTypeList = listOf(
            TODAY,
            YESTERDAY,
            LAST_7_DAYS,
            LAST_30_DAYS,
            THIS_MONTH,
            CUSTOM_RANGE
        )

        val statusList = listOf(
            STATUS_ALL,
            STATUS_RAISED,
            STATUS_APPROVED,
            STATUS_REJECTED
        )

        fun showToastMessage(context: Context?, message: String) {
            try {
                toast?.cancel()
                if (context != null) {
                    if (message.trim().isNotEmpty()) {
                        toast = Toast(context)
                        val inflater: LayoutInflater? =
                            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
                        val view: View? = inflater?.inflate(R.layout.toast, null)
                        val marginData = convertDpToPixels(10f, context)

                        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(marginData.roundToInt(), 0, marginData.roundToInt(), 0)

                        val tv: TextView? = view?.findViewById(R.id.txtToast)
                        tv?.text = message
                        tv?.layoutParams = params
                        toast?.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 150)
                        toast?.duration = Toast.LENGTH_SHORT
                        toast?.view = view
                        toast?.show()
//                val handler = Handler(Looper.getMainLooper())
//                handler.postDelayed({ toast?.cancel() }, 1200)
                    }
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }
        }

        private fun convertDpToPixels(dp: Float, context: Context): Float {
            val resources: Resources = context.resources
            val metrics: DisplayMetrics = resources.displayMetrics
            return dp * metrics.density + 0.5F
        }

        fun AppCompatActivity.hideKeyboard() {

            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            val view = currentFocus
            if (view != null) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        var mProgressHUD: ProgressHUD? = null

        fun showLoading(mContext: Context) {
            try {
                if (mProgressHUD == null) {
                    mProgressHUD = ProgressHUD.show(
                        mContext,
                        "Please wait", false, false
                    )
                } else {
                    if (!mProgressHUD!!.isShowing) {
                        mProgressHUD = ProgressHUD.show(
                            mContext,
                            "Please wait", false, false
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
            }

        }

        fun hideLoading() {
            try {
                if (mProgressHUD != null) {

                    if (mProgressHUD!!.isShowing) {
                        mProgressHUD!!.dismiss()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }

        fun getdeviceModel(): String {
            val device_model = Build.MODEL
            return device_model
        }

        fun getdevicename(): String {
            val device_name = Build.MANUFACTURER
            return device_name
        }

        fun getDeviceId(context: Context): String? {
            return Secure.getString(
                context.contentResolver,
                Secure.ANDROID_ID
            )
        }

        fun getDeviceVersion(): String? {
            return Build.VERSION.RELEASE
        }

        fun getBatteryPercentage(context: Context): Int {
            val batteryStatus: Intent? =
                context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

            if (level != -1 && scale != -1) {
                val batteryPct = (level.toFloat() / scale.toFloat() * 100).toInt()
                return batteryPct
            } else {
                return -1  // Error occurred, unable to get battery percentage
            }
        }

        fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): String? {
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (addresses!!.isNotEmpty()) {
                    val address = addresses[0]

                    // You can access various parts of the address, such as:
                    val addressLine = address!!.getAddressLine(0) // Full address
                    val city = address.locality // City
                    val state = address.adminArea // State
                    val country = address.countryName // Country
                    val postalCode = address.postalCode // Postal code

                    // Construct the complete address
                    val fullAddress = "$addressLine, $city, $state, $country $postalCode"
                    return fullAddress
                }
            } catch (e: IOException) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
            }
            return null
        }

        fun showAlertDialog(
            mContext: Context?,
            title: String?,
            message: String?,
            okListener: PositiveButtonListener?,
            isCancelVisibility: Boolean = true,
            positiveButtonText: String = "Ok",
            negativeButtonText: String = "Cancel"
        ) {

            val builder = AlertDialog.Builder(
                mContext!!
            )
            builder.setCancelable(false)
            builder.setTitle("")
            val view = View.inflate(mContext, R.layout.alert_dialog_layout, null)
            val tvTitle = view.findViewById<View>(R.id.tvTitle) as TextView
            val tvMessage = view.findViewById<View>(R.id.tvMessage) as TextView
            val tvCancel = view.findViewById<View>(R.id.tvCancel) as TextView
            val tvOk = view.findViewById<View>(R.id.tvOk) as TextView
            tvTitle.text = title
            tvMessage.text = message
            tvOk.text = positiveButtonText
            tvCancel.text = negativeButtonText

            if(okListener == null){
                tvCancel.visibility = View.GONE
            }

            if (isCancelVisibility) {
                tvCancel.visibility = View.VISIBLE
            } else {
                tvCancel.visibility = View.GONE
            }

            tvOk.setOnClickListener {
                okListener?.okClickListener()
                mAlertDialog!!.dismiss()
            }
            tvCancel.setOnClickListener {
                mAlertDialog!!.dismiss()
            }

            if (mAlertDialog == null) {
                mAlertDialog = builder.create()
                mAlertDialog!!.setView(view, 15, 0, 15, 0)
                mAlertDialog!!.window?.setBackgroundDrawableResource(R.drawable.dialog_shape)
                mAlertDialog!!.show()
            } else {
                if (!mAlertDialog!!.isShowing) {
                    mAlertDialog = builder.create()
                    mAlertDialog!!.setView(view, 15, 0, 15, 0)
                    mAlertDialog!!.window?.setBackgroundDrawableResource(R.drawable.dialog_shape)
                    mAlertDialog!!.show()
                }
            }

        }

        fun getCurrentDate(): String {
            val currentDate = Date()
            return dateFormat.format(currentDate)
        }

        fun getCurrentDateTime(): String {
            val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US)
            val currentDateTime = Date()
            return dateFormat.format(currentDateTime)
        }

        fun getStartDateOfCurrentMonth(): String {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val startDate = calendar.time
            return dateFormat.format(startDate)
        }

        fun getYesterdayDate(): String {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DATE, -1)
            val yesterday = calendar.time
            return dateFormat.format(yesterday)
        }

        fun getStartDateForLast7Days(): String {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DATE, -6)
            val startDate = calendar.time
            return dateFormat.format(startDate)
        }

        fun getStartDateForLast30Days(): String {
            val dateFormat = SimpleDateFormat(DATE_FORMAT)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DATE, -29)
            val startDate = calendar.time
            return dateFormat.format(startDate)
        }

        fun formatDateToMMMyyyy(date: Date): String {
            val dateFormat = SimpleDateFormat("MMM-yyyy", Locale.getDefault())
            return dateFormat.format(date)
        }

        private val partyDealerItemClickListener =
            object : ItemClickListener<AccountMasterList> {
                override fun onItemSelected(item: AccountMasterList) {
                    // Handle user item selection
                    searchDialog.closeDialog()
                    tvSelectPartyDealer.text = item.accountName
                    selectedPartyDealer = item
                }
            }

        fun showFilterDialog(
            listener: FilterDialogListener,
            mActivity: HomeActivity,
            startDate: String = "",
            endDate: String = "",
            dateOptionPosition: Int,
            isStatusVisible: Boolean = false,
            isFilterVisible: Boolean = false,
            isReportGroupByVisible: Boolean = false,
            isVisitTypeVisible: Boolean = false,
            isPartyDealerVisible:Boolean = false,
            filterList: ArrayList<FilterListResponse> = arrayListOf(),
            reportGroupByList: ArrayList<FilterListResponse> = arrayListOf(),
            visitTypeList: ArrayList<CategoryMasterResponse> = arrayListOf(),
            partyDealerList:ArrayList<AccountMasterList> = arrayListOf(),
            selectedVisitPosition: Int = 0,
            selectedStatusPosition: Int = 0,
            selectedFilterPosition : Int = 0,
            selectedReportGroupByPosition : Int = 0,
            selectedPartyDealerObj: AccountMasterList = AccountMasterList()
        ) {
            val filterDialog = Dialog(mActivity)

            filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            filterDialog.setCancelable(true)
            filterDialog.setContentView(R.layout.filter_dialog)

            val spDateOption: Spinner = filterDialog.findViewById(R.id.spDateOption)
            val spStatus: Spinner = filterDialog.findViewById(R.id.spStatus)
            val spFilter: Spinner = filterDialog.findViewById(R.id.spFilter)
            val spReportGroupBy: Spinner = filterDialog.findViewById(R.id.spReportGroupBy)
            val btnSubmit: TextView = filterDialog.findViewById(R.id.btnSubmit)
            val tvStartDate: TextView = filterDialog.findViewById(R.id.tvStartDate)
            val tvEndDate: TextView = filterDialog.findViewById(R.id.tvEndDate)
            val llStatus: LinearLayout = filterDialog.findViewById(R.id.llStatus)
            val llFilter: LinearLayout = filterDialog.findViewById(R.id.llFilter)
            val llReportGroupBy: LinearLayout = filterDialog.findViewById(R.id.llReportGroupBy)
            val llVisitType: LinearLayout = filterDialog.findViewById(R.id.llVisitType)
            val spVisitType: Spinner = filterDialog.findViewById(R.id.spVisitType)
            val llPartyDealer : LinearLayout = filterDialog.findViewById(R.id.llPartyDealer)
            tvSelectPartyDealer  = filterDialog.findViewById(R.id.tvPartyDealer)

            if (isFilterVisible) {
                llFilter.visibility = View.VISIBLE
            }
            if (isStatusVisible) {
                llStatus.visibility = View.VISIBLE
            }
            if (isReportGroupByVisible) {
                llReportGroupBy.visibility = View.VISIBLE
            }
            if (isVisitTypeVisible) {
                llVisitType.visibility = View.VISIBLE
            }
            if(isPartyDealerVisible){
                llPartyDealer.visibility = View.VISIBLE
                if(selectedPartyDealerObj.accountMasterId > 0) {
                    tvSelectPartyDealer.text = selectedPartyDealerObj.accountName
                    selectedPartyDealer = selectedPartyDealerObj
                }
            }
            tvStartDate.setOnClickListener {
                if (spDateOption.selectedItem == CUSTOM_RANGE) {
                    openStartDatePickerDialog(true, mActivity, tvStartDate, tvEndDate)
                }
            }
            tvEndDate.setOnClickListener {
                if (spDateOption.selectedItem == CUSTOM_RANGE) {
                    openStartDatePickerDialog(false, mActivity, tvStartDate, tvEndDate)
                }
            }
            tvSelectPartyDealer.setOnClickListener{
                if (partyDealerList.size > 0) {
                    try {
                        if (partyDealerList.size > 0) {
                            searchDialog = SearchDialogUtil(
                                activity = mActivity,
                                items = partyDealerList,
                                layoutId = R.layout.item_user, // The layout resource for each item
                                bind = { view, item ->
                                    val textView: TextView = view.findViewById(R.id.tvUserName)
                                    textView.text = item.accountName // Bind data to the view
                                },
                                itemClickListener = partyDealerItemClickListener,
                                title = mActivity.getString(R.string.party_dealer_list),
                                DIALOG_ACCOUNT_MASTER
                            )
                            searchDialog.showSearchDialog()
                        }
                    } catch (e: Exception) {
                        Log.e("TAG", "onClick: " + e.message)
                    }
                }
            }

            tvStartDate.text = startDate
            tvEndDate.text = endDate

            btnSubmit.setOnClickListener {
                val startDate = tvStartDate.text.toString()
                val endDate = tvEndDate.text.toString()
                val selectedDateOption = spDateOption.selectedItem.toString()

                listener.onFilterSubmitClick(
                    startDate,
                    endDate,
                    selectedDateOption,
                    spDateOption.selectedItemPosition,
                    if (isStatusVisible) spStatus.selectedItemPosition else 0,
                    if (isFilterVisible) filterList[spFilter.selectedItemPosition] else FilterListResponse(),
                    if (isReportGroupByVisible) reportGroupByList[spReportGroupBy.selectedItemPosition] else FilterListResponse(),
                    if (isVisitTypeVisible) visitTypeList[spVisitType.selectedItemPosition] else CategoryMasterResponse(),
                    if(isPartyDealerVisible) selectedPartyDealer else AccountMasterList(),
                    if (isVisitTypeVisible) spVisitType.selectedItemPosition else 0
                )

                filterDialog.dismiss()
            }

            val adapter = DateOptionAdapter(
                mActivity,
                R.layout.simple_spinner_item,
                dateTypeList,
            )
            spDateOption.adapter = adapter
            spDateOption.setSelection(dateOptionPosition)
            if (isStatusVisible) {
                val adapter = DateOptionAdapter(
                    mActivity,
                    R.layout.simple_spinner_item,
                    statusList,
                )
                spStatus.adapter = adapter
                spStatus.setSelection(selectedStatusPosition)
            }

            if (isVisitTypeVisible) {
                val adapter = VisitTypeAdapter(
                    mActivity,
                    R.layout.simple_spinner_item,
                    visitTypeList,
                )
                spVisitType.adapter = adapter
                spVisitType.setSelection(selectedVisitPosition)
            }

            if (isFilterVisible) {
                val adapter = FilterAdapter(
                    mActivity,
                    R.layout.simple_spinner_item,
                    filterList,
                )
                spFilter.adapter = adapter
                spFilter.setSelection(selectedFilterPosition)
            }
            if (isReportGroupByVisible) {
                val adapter = FilterAdapter(
                    mActivity,
                    R.layout.simple_spinner_item,
                    reportGroupByList,
                )
                spReportGroupBy.adapter = adapter
                spReportGroupBy.setSelection(selectedReportGroupByPosition)
            }

            spDateOption.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    when (spDateOption.selectedItem) {
                        TODAY -> {
                            setStartEndDate(
                                getCurrentDate(),
                                getCurrentDate(),
                                tvStartDate, tvEndDate
                            )
                        }
                        YESTERDAY -> {
                            setStartEndDate(
                                getYesterdayDate(),
                                getYesterdayDate(),
                                tvStartDate, tvEndDate
                            )
                        }
                        LAST_7_DAYS -> {
                            setStartEndDate(
                                getStartDateForLast7Days(),
                                getCurrentDate(),
                                tvStartDate, tvEndDate
                            )
                        }
                        LAST_30_DAYS -> {
                            setStartEndDate(
                                getStartDateForLast30Days(),
                                getCurrentDate(),
                                tvStartDate, tvEndDate
                            )
                        }
                        THIS_MONTH -> {
                            setStartEndDate(
                                getStartDateOfCurrentMonth(),
                                getCurrentDate(),
                                tvStartDate, tvEndDate
                            )
                        }
                        CUSTOM_RANGE -> {
                        }

                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}

            }

            val window = filterDialog.window
            window!!.setLayout(
                AbsListView.LayoutParams.MATCH_PARENT,
                AbsListView.LayoutParams.WRAP_CONTENT
            )
            filterDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            filterDialog.show()
        }

        fun setStartEndDate(
            startDate: String,
            endDate: String,
            tvStartDate: TextView,
            tvEndDate: TextView
        ) {
            tvStartDate.text = startDate
            tvEndDate.text = endDate
        }


        fun openStartDatePickerDialog(
            isStartDate: Boolean,
            mActivity: HomeActivity,
            tvStartDate: TextView,
            tvEndDate: TextView
        ) {
            val newDate = Calendar.getInstance()
            var dateString = ""
            mActivity.hideKeyboard()
            val datePickerDialog = DatePickerDialog(
                mActivity, { view, year, monthOfYear, dayOfMonth ->
                    newDate.set(year, monthOfYear, dayOfMonth)
                    dateString = dateFormat.format(newDate.time) ?: ""
                    if (isStartDate) {
                        tvStartDate.text = dateString
                    } else {
                        tvEndDate.text = dateString
                    }

                }, newDate.get(Calendar.YEAR), newDate.get(Calendar.MONTH), newDate.get(
                    Calendar.DAY_OF_MONTH
                )
            )
            //datePickerDialog.datePicker.minDate = newCalendar.timeInMillis
            datePickerDialog.show()
        }

        fun openDatePickerDialog(
            listener: DatePickerListener,
            mActivity: HomeActivity,
            isPastDateHide: Boolean = false
        ): String {
            val newDate = Calendar.getInstance()
            var dateString = ""
            mActivity.hideKeyboard()
            val datePickerDialog = DatePickerDialog(
                mActivity, { view, year, monthOfYear, dayOfMonth ->
                    newDate.set(year, monthOfYear, dayOfMonth)
                    dateString = dateFormat.format(newDate.time) ?: ""
                    listener.onDateSelect(dateString)

                }, newDate.get(Calendar.YEAR), newDate.get(Calendar.MONTH), newDate.get(
                    Calendar.DAY_OF_MONTH
                )
            )
            // Set min date to current date if past dates are to be hidden
            if (isPastDateHide) {
                datePickerDialog.datePicker.minDate = newDate.timeInMillis
            }
            //datePickerDialog.datePicker.minDate = newCalendar.timeInMillis
            datePickerDialog.show()
            return dateString
        }

        fun convertImageFileToBase64(file: File): String? {
            try {

                val inputStream = FileInputStream(file)
                val bytes = ByteArray(file.length().toInt())
                inputStream.read(bytes)
                inputStream.close()

                // Encode the bytes as Base64
                val base64String =
                    android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                return base64String
            } catch (e: IOException) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
            }
            return null
        }

        @Throws(IOException::class)
        fun createImageFile(mContext: Context): File? {
            var imageFileName = ""
            val timeStamp =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(Date())
            imageFileName = "Image" + timeStamp + "_"
            val storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            //fileData = image.getAbsolutePath();
            return File.createTempFile(imageFileName, ".jpg", storageDir)
        }


        fun addDateAndTimeToFile(inputFile: File, outputFilePath: File): File {
            val timeStamp =
                SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

            // Load the original image from the file
            val originalBitmap = BitmapFactory.decodeFile(inputFile.absolutePath)

            // Create a new bitmap with the date and time added
            val modifiedBitmap = addDateAndTimeToImage(originalBitmap, timeStamp)

            val finalBitmap = modifyOrientation(modifiedBitmap, inputFile.absolutePath)

            // Save the modified image to a new file
            val outputBitmapFile = outputFilePath
            val fileOutputStream = FileOutputStream(outputBitmapFile)



            finalBitmap?.compress(Bitmap.CompressFormat.JPEG, 40, fileOutputStream)
            fileOutputStream.close()
            return outputBitmapFile
        }

        private fun addDateAndTimeToImage(bitmap: Bitmap, timeStamp: String): Bitmap {
            // Create a canvas for drawing text on the bitmap
            val resultBitmap = bitmap.copy(bitmap.config, true)
            val canvas = Canvas(resultBitmap)

            // Define the paint attributes for the text (customize as needed)
            val paint = Paint()
            paint.color = Color.YELLOW // Text color
            paint.textSize = 60f // Text size

            // Calculate the position for the text (adjust as needed)
            val x = 50f
            val y = 50f

            // Draw the date and time on the image
            canvas.drawText(timeStamp, x, y, paint)

            return resultBitmap
        }

        @Throws(IOException::class)
        fun modifyOrientation(bitmap: Bitmap?, image_absolute_path: String?): Bitmap? {
            val ei = ExifInterface(image_absolute_path!!)
            return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate(
                    bitmap!!,
                    90f
                )
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate(
                    bitmap!!,
                    180f
                )
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate(
                    bitmap!!,
                    270f
                )
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(
                    bitmap!!,
                    true,
                    false
                )
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(
                    bitmap!!,
                    false,
                    true
                )
                else -> bitmap
            }
        }

        fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(degrees)
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
            val matrix = Matrix()
            matrix.preScale(
                (if (horizontal) -1 else 1.toFloat()) as Float,
                (if (vertical) -1 else 1.toFloat()) as Float
            )
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        fun calculateNoOfRows(context: Context, v: Double): Int {
            val displayMetrics = context.resources.displayMetrics
            val dpHeight = displayMetrics.heightPixels.toFloat()
            return (dpHeight / v).toInt()
        }

        fun calculateNoOfColumns(context: Context, v: Double): Int {
            val displayMetrics = context.resources.displayMetrics
            val dpWidth = displayMetrics.widthPixels.toFloat()
            return (dpWidth / v).toInt()
        }


        fun isEmailValid(email: String): Boolean {
            val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})([.]{1})(.{1,})"
            return email.matches(emailRegex.toRegex())
        }

        fun isMobileNumberValid(phoneNumber: String): Boolean {
            val phoneRegex = "^\\d{10}\$"
            return phoneNumber.matches(phoneRegex.toRegex())
        }

        fun validateMobileLandlineNumber(input: String): String {
            val mobileRegex = Regex("^\\d{10}$") // Mobile numbers with exactly 10 digits
            val landlineRegex = Regex("^\\d{2,5}-\\d{6,8}$") // Landline numbers like 022-1234567

            return when {
                input.contains("-") -> {
                    if (landlineRegex.matches(input)) "Valid"
                    else "Invalid landline number format."
                }
                else -> {
                    if (mobileRegex.matches(input)) "Valid"
                    else "Invalid mobile number format. Use exactly 10 digits."
                }
            }
        }



        fun formatDateTime(inputDateTime: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            try {
                val inputDate = inputFormat.parse(inputDateTime)
                return outputFormat.format(inputDate!!)
            } catch (e: Exception) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
            }

            return inputDateTime // return original string if parsing fails
        }

        fun getRandomHexColor(): String {
            // Generate random color with values biased towards darkness
            val randomColor = Random.nextInt(0x000000, 0x7F7F7F + 1)
            return String.format("#%06X", randomColor)
        }


        fun getCurrentTimeIn12HrFormat(): String {
            val calendar = Calendar.getInstance()
            val amPm: String
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            amPm = if (hour >= 12) {
                "PM"
            } else {
                "AM"
            }

            val hour12 = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour

            val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
            val time = timeFormat.format(calendar.time)

            return time
        }

        fun getTodayEndTime(): String {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
            return timeFormat.format(calendar.time)
        }

        fun getCurrentTimeIn24HrFormat(): String {
            val calendar = Calendar.getInstance()
            val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
            return timeFormat.format(calendar.time)
        }

        fun convertToAppDateFormat(inputDate: String, inputFormat: String = "dd/MM/yyyy"): String {
            try {
                val inputFormatter = SimpleDateFormat(inputFormat, Locale.getDefault())
                val outputFormatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

                val inputDate = inputFormatter.parse(inputDate)
                return outputFormatter.format(inputDate!!)
            } catch (e: ParseException) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
            }

            return "" // Return an empty string in case of an error
        }

        fun animatePolyline(polyline: Polyline) {
            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.duration = 5000 // Animation duration in milliseconds
            val points = polyline.points

            animator.addUpdateListener { valueAnimator ->
                val animatedFraction = valueAnimator.animatedFraction
                val index = (points.size * animatedFraction).toInt()

                if (index < points.size) {
                    val subList = points.subList(0, index)
                    polyline.points = subList
                }
            }

            animator.start()
        }


        fun BitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
            val vectorDrawable: Drawable = ContextCompat.getDrawable(context, vectorResId)!!
            vectorDrawable.setBounds(
                0, 0, vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight
            )
            val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            vectorDrawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }

        fun getLocationData(mContext: Context): JSONArray? {
            var arrLocationList: JSONArray? = JSONArray()
            try {
                val strLastLocation: String =
                    AppPreference.getStringPreference(mContext, LOCATION_LIST, "")
                if (!strLastLocation.isEmpty()) {
                    arrLocationList = JSONArray(strLastLocation)
                }
            } catch (ex: java.lang.Exception) {
                FirebaseCrashlytics.getInstance().recordException(ex)
                Log.e("TAG", "getLocationData: " + ex.message.toString())
            }
            return arrLocationList
        }

        fun saveLocationData(latitude: Double, longitude: Double, mContext: Context) {
            try {
                val strLastLocation: String =
                    AppPreference.getStringPreference(mContext, LOCATION_LIST, "")
                var arrLocationList = JSONArray()
                if (strLastLocation.isNotEmpty()) {
                    arrLocationList = JSONArray(strLastLocation)
                }
                if (arrLocationList.length() >= 10) {
                    arrLocationList.remove(0)
                }
                val locationData = JSONObject()
                locationData.put("latitude", latitude)
                locationData.put("longitude", longitude)
                arrLocationList.put(locationData)
                AppPreference.saveStringPreference(
                    mContext,
                    LOCATION_LIST,
                    arrLocationList.toString()
                )
            } catch (ex: java.lang.Exception) {
                FirebaseCrashlytics.getInstance().recordException(ex)
                Log.e("TAG", "saveLocationData: " + ex.message.toString())
            }
        }

        fun isDateValid(fromDate: String, toDate: String): Boolean {
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            dateFormat.isLenient = false // This will make the date validation strict

            try {
                val from = dateFormat.parse(fromDate)
                val to = dateFormat.parse(toDate)

                // Check if toDate is greater than or equal to fromDate
                if (to != null && from != null) {
                    return !to.before(from)
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }

            return false
        }

        fun openPDFWithIntent(mActivity: Activity, file: File) {
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
                mActivity.startActivity(intent)

            } catch (e: ActivityNotFoundException) {
                FirebaseCrashlytics.getInstance().recordException(e)
                // Handle the exception if a PDF viewer is not installed
                Toast.makeText(mActivity, "No PDF viewer installed", Toast.LENGTH_SHORT).show()
            }
        }

        fun getStyledCell(content: String, font: Font, bgColor: BaseColor): PdfPCell {
            val cell = PdfPCell(Phrase(content, font))
            cell.backgroundColor = bgColor
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.verticalAlignment = Element.ALIGN_MIDDLE
            cell.setPadding(4f)// = 8f
            return cell
        }

        fun isMockSettingsON(context: Context): Boolean {
            Log.e("TAG", "isMockSettingsON: ", )
            // returns true if mock location enabled, false if not enabled.
            return !Secure.getString(
                context.contentResolver,
                Secure.ALLOW_MOCK_LOCATION
            ).equals("0")
        }

        fun areThereMockPermissionApps(context: Context): Boolean {
            Log.e("TAG", "areThereMockPermissionApps: CALL THE FUNCTIONS" )
            var count = 0
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (applicationInfo in packages) {
                Log.e("TAG", "areThereMockPermissionApps: "+applicationInfo.packageName )
                try {
                    val packageInfo = pm.getPackageInfo(
                        applicationInfo.packageName,
                        PackageManager.GET_PERMISSIONS
                    )

                    // Get Permissions
                    val requestedPermissions = packageInfo.requestedPermissions
                    if (requestedPermissions != null) {
                        for (i in requestedPermissions.indices) {
                            if ((requestedPermissions[i]
                                        == "android.permission.ACCESS_MOCK_LOCATION") && applicationInfo.packageName != context.packageName
                            ) {
                                count++
                            }
                        }
                    }
                } catch (e: NameNotFoundException) {
                    Log.e("Got exception ", e.message!!)
                }
            }
            return count > 0
        }

        fun convertDateStringForOrderEntry(inputDate: String): String? {
            return try {
                val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val date = inputFormat.parse(inputDate)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun formatLargeDouble(number: Double): String {
            val bigDecimal = BigDecimal(number)
            val decimalFormat = DecimalFormat("#.00") // Use desired format here
            return decimalFormat.format(bigDecimal)
        }

        fun formatBigDecimal(number: BigDecimal): String {
            val roundedNumber = number.setScale(2, RoundingMode.HALF_UP)
            val decimalFormat = DecimalFormat("0.00") // Ensures leading zero for values like 0.00
            return decimalFormat.format(roundedNumber)
        }



        fun dateStringToCalendar(dateString: String): Calendar {
            val date = dateFormat.parse(dateString)
            return Calendar.getInstance().apply {
                time = date
            }
        }

        fun expand(view: View) {
            view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val targetHeight = view.measuredHeight

            view.layoutParams.height = 0
            view.visibility = View.VISIBLE

            val animation = ValueAnimator.ofInt(0, targetHeight)
            animation.addUpdateListener { valueAnimator ->
                view.layoutParams.height = valueAnimator.animatedValue as Int
                view.requestLayout()
            }
            animation.duration = 300
            animation.interpolator = AccelerateDecelerateInterpolator()
            animation.start()
        }

        fun collapse(view: View) {
            val initialHeight = view.measuredHeight

            val animation = ValueAnimator.ofInt(initialHeight, 0)
            animation.addUpdateListener { valueAnimator ->
                view.layoutParams.height = valueAnimator.animatedValue as Int
                view.requestLayout()
            }
            animation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                }
            })
            animation.duration = 300
            animation.interpolator = AccelerateDecelerateInterpolator()
            animation.start()
        }

    }

    }

    /*fun setSpinnerByString(spinner: Spinner, myString: String): Int {
        for (i in 0 until spinner.count) {
            Log.e("TAG", "setSpinnerByString: "+spinner.getItemAtPosition(i) )
            if (spinner.getItemAtPosition(i).toString().equals(myString, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }*/


