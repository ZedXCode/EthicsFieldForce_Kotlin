package ethicstechno.com.fieldforce.models.reports

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentFollowUpResponse(
    @SerializedName("PaymentFollowUpId") val paymentFollowUpId: Int,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("FollowUpDate") val followUpDate: String,
    @SerializedName("TableName") val tableName: String,
    @SerializedName("ReferenceId") val referenceId: Int,
    @SerializedName("ExpectedDate1") val expectedDate1: String,
    @SerializedName("ExpectedAmount1") val expectedAmount1: Double,
    @SerializedName("ExpectedDate2") val expectedDate2: String,
    @SerializedName("ExpectedAmount2") val expectedAmount2: Double,
    @SerializedName("ExpectedDate3") val expectedDate3: String,
    @SerializedName("ExpectedAmount3") val expectedAmount3: Double,
    @SerializedName("ExpectedDate4") val expectedDate4: String,
    @SerializedName("ExpectedAmount4") val expectedAmount4: Double,
    @SerializedName("Remarks") val remarks: String,
    @SerializedName("IsActive") val isActive: Boolean,
    @SerializedName("CommandId") val commandId: Int,
    @SerializedName("CreateBy") val createBy: Int,
    @SerializedName("CreateDateTime") val createDateTime: String,
    @SerializedName("UpdateBy") val updateBy: Int,
    @SerializedName("UpdateDateTime") val updateDateTime: String,
    @SerializedName("Deleteby") val deleteBy: Int,
    @SerializedName("DeleteDateTime") val deleteDateTime: String,
    @SerializedName("Success") val success: Boolean,
    @SerializedName("ReturnMessage") val returnMessage: String?
) : Parcelable