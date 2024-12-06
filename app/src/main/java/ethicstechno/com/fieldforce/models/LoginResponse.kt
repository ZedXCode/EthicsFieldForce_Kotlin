package ethicstechno.com.fieldforce.models
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "loginTable")
data class LoginResponse(
    @SerializedName("EmailId")
    val emailId: String?,

    @SerializedName("UserName")
    val userName: String?,

    @SerializedName("UserPhoto")
    val userPhoto: String?,

    @SerializedName("MobileNo")
    val mobileNo: String,

    @SerializedName("Password")
    val password: String,

    @PrimaryKey(autoGenerate = true)
    @SerializedName("UserId")
    val userId: Int,

    @SerializedName("ReportingToUserId")
    val reportingToUserId: Int,

    @SerializedName("ReportingToUserName")
    val reportingToUserName: String?,

    @SerializedName("ReturnMessage")
    val returnMessage: String?,

    @SerializedName("LocationUpdateInterval")
    val locationUpdateInterval: Int,

    @SerializedName("TripLocationUpdateInterval")
    val tripLocationUpdateInterval: Int,

    @SerializedName("SaveIntervalWiseLocation")
    val saveIntervalWiseLocation: Boolean,

    @SerializedName("SupportPhoneNo")
    val supportPhoneNo: String?,

    @SerializedName("AttendanceId")
    val attendanceId: Int,

    @SerializedName("TodayClockInDone")
    val todayClockInDone: Boolean,

    @SerializedName("TodayClockOutDone")
    val todayClockOutDone: Boolean,

    @SerializedName("LastClockOutDone")
    val lastClockOutDone: Boolean,

    @SerializedName("TripId")
    val tripId: Int,

    @SerializedName("LastTripEnded")
    val lastTripEnded: Boolean,

    @SerializedName("LastLocationSyncDateTime")
    val lastLocationSyncDateTime: String?,

    @SerializedName("LastLoginDateTime")
    val lastLoginDateTime: String?,

    @SerializedName("Success")
    val success: Boolean,

    @SerializedName("ExpenseBackDateDifference")
    val expenseBackDateDifference: Int,

    @SerializedName("Version")
    val version: String,

    @SerializedName("GoogleApiKey")
    val googleApiKey: String?,

    @SerializedName("HQLatitude")
    val hqLatitude: Double,

    @SerializedName("HQLongitude")
    val hqLongitude: Double
)
