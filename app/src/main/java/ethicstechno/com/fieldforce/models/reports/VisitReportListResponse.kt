package ethicstechno.com.fieldforce.models.reports

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class VisitReportListResponse(
    @SerializedName("VisitId") val visitId: Int,
    @SerializedName("CategoryMasterId") val categoryMasterId: Int,
    @SerializedName("CategoryName") val categoryName: String,
    @SerializedName("AccountMasterId") val accountMasterId: Int,
    @SerializedName("AccountName") val accountName: String,
    @SerializedName("CityId") val cityId: Int,
    @SerializedName("CityName") val cityName: String,
    @SerializedName("ContactPersonName") val contactPersonName: String,
    @SerializedName("VisitDetails") val visitDetails: String,
    @SerializedName("NextVisitDateTime") val nextVisitDateTime: String,
    @SerializedName("NextVisitSubject") val nextVisitSubject: String,
    @SerializedName("Remakrs") val remarks: String,
    @SerializedName("FilePath") val filePath: String,
    @SerializedName("Latitude") val latitude: Double,
    @SerializedName("Longitude") val longitude: Double,
    @SerializedName("Location") val location: String,
    @SerializedName("TripId") val tripId: Int,
    @SerializedName("PreviousVisitId") val previousVisitId: Int,
    @SerializedName("MapKM") val mapKM: Double,
    @SerializedName("IsActive") val isActive: Boolean,
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
    @SerializedName("VisitDate") val visitDate: String,
    @SerializedName("VisitTime") val visitTime: String,
    @SerializedName("FromDate") val fromDate: String,
    @SerializedName("ToDate") val toDate: String,
    @SerializedName("NextVisitDate") val nextVisitDate: String,
    @SerializedName("NextVisitTime") val nextVisitTime: String
) : Parcelable {

    constructor() : this(
        0, 0, "", 0, "", 0, "", "", "", "", "", "", "",
        0.0, 0.0, "", 0, 0, 0.0, false, 0, 0, "", 0, "",
        0, "", false, "", 0, "", "", "", "", "", ""
    )
}
