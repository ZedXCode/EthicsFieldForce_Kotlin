package ethicstechno.com.fieldforce.models.moreoption.partydealer

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class AccountMasterList(
    @SerializedName("AccountMasterId") val accountMasterId: Int = 0,
    @SerializedName("CategoryMasterId") val categoryMasterId: Int = 0,
    @SerializedName("CategoryName") val categoryName: String = "",
    @SerializedName("AccountName") val accountName: String = "",
    @SerializedName("Address") val address: String? = "",
    @SerializedName("PinCode") val pinCode: BigDecimal = BigDecimal.ZERO,
    @SerializedName("CityId") val cityId: Int = 0,
    @SerializedName("CityName") val cityName: String? = "",
    @SerializedName("PhoneNo") val phoneNo: String? = "",
    @SerializedName("Email") val email: String? = "",
    @SerializedName("ContactPersonName") val contactPersonName: String? = "",
    @SerializedName("ContactPersonPhoneNo") val contactPersonPhoneNo: String? = "",
    @SerializedName("ContactPersonEmail") val contactPersonEmail: String? = "",
    @SerializedName("ParentAccountMasterId") val parentAccountMasterId: Int = 0,
    @SerializedName("ParentAccountName") val parentAccountName: String? = "",
    @SerializedName("HandledByUserId") val handledByUserId: Int = 0,
    @SerializedName("HandledByUserName") val handledByUserName: String? = "",
    @SerializedName("Remakrs") val remarks: String? = "",
    @SerializedName("Latitude") val latitude: Double = 0.0,
    @SerializedName("Longitude") val longitude: Double = 0.0,
    @SerializedName("Location") val location: String? = "",
    @SerializedName("IsActive") val isActive: Boolean = false,
    @SerializedName("UserId") val userId: Int = 0,
    @SerializedName("ReportSetupId") val reportSetupId: Int = 0,
    @SerializedName("ReportName") val reportName: String? = "",
    @SerializedName("APIName") val apiName: String? = "",
    @SerializedName("StoreProcedureName") val storeProcedureName: String? = "",
    @SerializedName("ReportGroupBy") val reportGroupBy: String? = "",
    @SerializedName("Filter") val filter: String? = "",
    @SerializedName("ParameterString") val parameterString: String? = "",
    @SerializedName("DDMRegionId") val ddmRegionId: Int = 0,
    @SerializedName("DDMIndustryTypeId") val ddmIndustryTypeId: Int = 0,
    @SerializedName("DDMReferenceSourceId") val ddmReferenceSourceId: Int = 0,
    @SerializedName("RegionName") val regionName: String? = "",
    @SerializedName("IndustryTypeName") val industryTypeName: String? = "",
    @SerializedName("ReferenceSourceName") val referenceSourceName: String? = "",
    @SerializedName("FilePath1") val filePath1: String? = "",
    @SerializedName("FilePath2") val filePath2: String? = "",
    @SerializedName("FilePath3") val filePath3: String? = "",
    @SerializedName("FilePath4") val filePath4: String? = "",
    @SerializedName("Success") val success: Boolean = false,
    @SerializedName("ReturnMessage") val returnMessage: String? = "",
    @SerializedName("AllowEdit") val allowEdit: Boolean = false,
    @SerializedName("AllowDelete") val allowDelete: Boolean = false,
    @SerializedName("AllowOrder") val allowOrder: Boolean = false,
    @SerializedName("AllowInquiry") val allowInquiry: Boolean = false,
    @SerializedName("Rating") val rating: Int = 0,
    @SerializedName("LastVisitDatetime") val lastVisitDateTime: String = "",
    @SerializedName("NextVisitDatetime") val nextVisitDateTime: String = ""
    ) : Parcelable
