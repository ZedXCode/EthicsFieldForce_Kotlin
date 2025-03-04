package ethicstechno.com.fieldforce.models.moreoption.visit

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddVisitSuccessResponse(
    @SerializedName("VisitId") val visitId: Int,
    @SerializedName("CategoryMasterId") val categoryMasterId: Int,
    @SerializedName("AccountMasterId") val accountMasterId: Int,
    @SerializedName("CityId") val cityId: Int,
    @SerializedName("ContactPersonName") val contactPersonName: String,
    @SerializedName("VisitDetails") val visitDetails: String,
    @SerializedName("NextVisitDateTime") val nextVisitDateTime: String,
    @SerializedName("NextVisitSubject") val nextVisitSubject: String,
    @SerializedName("Remakrs") val remarks: String,
    @SerializedName("Latitude") val latitude: Double,
    @SerializedName("Longitude") val longitude: Double,
    @SerializedName("Location") val location: String,
    @SerializedName("TripId") val tripId: Int,
    @SerializedName("PreviousVisitId") val previousVisitId: Int,
    @SerializedName("MapKM") val mapKM: Double,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("CompanyMasterId") val companyMasterId: Double,
    @SerializedName("BranchMasterId") val branchMasterId: Double,
    @SerializedName("DivisionMasterId") val divisionMasterId: Double,
    @SerializedName("DDMVisitTypeId") val ddmVisitTypeId: Double,
    @SerializedName("ModeOfCommunication") val modeOfCommunication: Int,
    @SerializedName("StartTime") val startTime: String,
    @SerializedName("EndTime") val endTime: String,
    @SerializedName("VisitStatus") val visitStatus: Int,
    @SerializedName("DDMStageId") val ddmStageId: Double,
    @SerializedName("SelfieFilePath") val selfieFilePath: String,
    @SerializedName("FilePath") val filePath: String,
    @SerializedName("FilePath2") val filePath2: String,
    @SerializedName("FilePath3") val filePath3: String,
    @SerializedName("FilePath4") val filePath4: String,
    @SerializedName("ParameterString") val parameterString: String?,
    @SerializedName("Success") val success: Boolean,
    @SerializedName("ReturnMessage") val returnMessage: String
) : Parcelable {
    constructor() : this(
        0, 0, 0, 0, "", "", "", "", "", 0.0, 0.0,
        "", 0, 0, 0.0, 0, 0.0, 0.0, 0.0, 0.0, 0,
        "", "", 0, 0.0, "", "", "", "", "", null,
        false, ""
    )
}
