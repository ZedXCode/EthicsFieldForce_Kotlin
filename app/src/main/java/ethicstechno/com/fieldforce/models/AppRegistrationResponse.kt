package ethicstechno.com.fieldforce.models
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity(tableName = "appRegistrationTable")
data class AppRegistrationResponse(

    @PrimaryKey(autoGenerate = true)
    @SerializedName("CustomerRegistrationId")
    val customerRegistrationId: Int,

    @SerializedName("CustomerName")
    val customerName: String,

    @SerializedName("CustomerId")
    val customerId: String,

    @SerializedName("NoofUsers")
    val noOfUsers: Int,

    @SerializedName("APIHostingServer")
    val apiHostingServer: String,

    @SerializedName("AppColourCode1")
    val appColourCode1: String,

    @SerializedName("AppColourCode2")
    val appColourCode2: String,

    @SerializedName("LogoFilePath")
    val logoFilePath: String,

    @SerializedName("LastSyncDateTime")
    val lastSyncDateTime: String,

    @SerializedName("IsActive")
    val isActive: Boolean,

    @SerializedName("RegistredMobileNo")
    val registredMobileNo: String,

    @SerializedName("UserId")
    val userId: Int,

    @SerializedName("Success")
    val success: Boolean,

    @SerializedName("ReturnMessage")
    val returnMessage: String?,

    @SerializedName("HQLatitude")
    val hqLatitude: Double,

    @SerializedName("HQLongitude")
    val hqLongitude: Double
)
