package ethicstechno.com.fieldforce.models.moreoption.tourplan

import com.google.gson.annotations.SerializedName

data class TourPlanListResponse(
    @SerializedName("TourPlanId") val tourPlanId: Int,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("PlanDate") val planDate: String,
    @SerializedName("DayName") val dayName: String,
    @SerializedName("CityId") val cityId: Int,
    @SerializedName("CityStateCountry") val cityStateCountry: String,
    @SerializedName("AccountMasterId") val accountMasterId: Int,
    @SerializedName("AccountName") val accountName: String,
    @SerializedName("Purpose") val purpose: String,
    @SerializedName("Remarks") val remarks: String,
    @SerializedName("Success") val success: Boolean,
    @SerializedName("ReturnMessage") val returnMessage: String?,
    @SerializedName("FromDate") val fromDate: String,
    @SerializedName("ToDate") val toDate: String
)
