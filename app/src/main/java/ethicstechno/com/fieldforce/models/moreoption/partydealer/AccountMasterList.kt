package ethicstechno.com.fieldforce.models.moreoption.partydealer

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccountMasterList(
    @SerializedName("AccountMasterId") val accountMasterId: Int,
    @SerializedName("CategoryMasterId") val categoryMasterId: Int,
    @SerializedName("CategoryName") val categoryName: String,
    @SerializedName("AccountName") val accountName: String,
    @SerializedName("Address") val address: String,
    @SerializedName("PinCode") val pinCode: Int,
    @SerializedName("CityId") val cityId: Int,
    @SerializedName("CityName") val cityName: String,
    @SerializedName("PhoneNo") val phoneNo: String,
    @SerializedName("Email") val email: String,
    @SerializedName("ContactPersonName") val contactPersonName: String,
    @SerializedName("ContactPersonPhoneNo") val contactPersonPhoneNo: String,
    @SerializedName("ContactPersonEmail") val contactPersonEmail: String,
    @SerializedName("ParentAccountMasterId") val parentAccountMasterId: Int,
    @SerializedName("ParentAccountName") val parentAccountName: String,
    @SerializedName("HandledByUserId") val handledByUserId: Int,
    @SerializedName("HandledByUserName") val handledByUserName: String,
    @SerializedName("Remakrs") val remarks: String,
    @SerializedName("Latitude") val latitude: Double,
    @SerializedName("Longitude") val longitude: Double,
    @SerializedName("Location") val location: String,
    @SerializedName("IsActive") val isActive: Boolean,
    @SerializedName("IsDeleted") val isDeleted: Boolean,
    @SerializedName("CommandId") val commandId: Int,
    @SerializedName("CreateBy") val createBy: Int,
    @SerializedName("CreateDateTime") val createDateTime: String,
    @SerializedName("UpdateBy") val updateBy: Int,
    @SerializedName("UpdateDateTime") val updateDateTime: String,
    @SerializedName("Deleteby") val deleteBy: Int,
    @SerializedName("DeleteDateTime") val deleteDateTime: String,
    @SerializedName("Success") val success: Boolean,
    @SerializedName("ReturnMessage") val returnMessage: String?,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("ReportSetupId") val reportSetupId: Int,
    @SerializedName("ReportName") val reportName: String,
    @SerializedName("APIName") val apiName: String,
    @SerializedName("StoreProcedureName") val storeProcedureName: String,
    @SerializedName("ReportGroupBy") val reportGroupBy: String,
    @SerializedName("Filter") val filter: String,
    @SerializedName("ParameterString") val parameterString: String
) : Parcelable{
    constructor() : this(
        0, 0, "", "All Party/Dealer", "", 0, 0, "", "", "",
        "", "", "", 0, "", 0, "", "",
        0.0, 0.0, "", false, false, 0, 0, "", 0,
        "", 0, "", false, "", 0, 0, "", "",
        "", "", "", ""
    )
}