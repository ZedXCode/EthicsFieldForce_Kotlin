package ethicstechno.com.fieldforce.models.moreoption.expense

import com.google.gson.annotations.SerializedName

data class ExpenseLimitResponse(
    @SerializedName("UserId") val userId: Int,
    @SerializedName("CityId") val cityId: Int,
    @SerializedName("CityCategoryId") val cityCategoryId: Int,
    @SerializedName("ExpenseTypeId") val expenseTypeId: Int,
    @SerializedName("ControlModeId") val controlModeId: Int,
    @SerializedName("VehicleTypeId") val vehicleTypeId: Int,
    @SerializedName("TotalTrips") val totalTrips: String,
    @SerializedName("MapKm") val mapKm: Double,
    @SerializedName("TotalTripKM") val totalTripKM: Double,
    @SerializedName("ActualKm") val actualKm: Double,
    @SerializedName("Limit") val limit: Double,
    @SerializedName("TotalLimit") val totalLimit: Double,
    @SerializedName("EligibleAmount") val eligibleAmount: Double,
    @SerializedName("ExpenseAmount") val expenseAmount: Double,
    @SerializedName("ApprovedAmount") val approvedAmount: Double,
    @SerializedName("UserName") val userName: String?,
    @SerializedName("StartDate") val startDate: String,
    @SerializedName("EndDate") val endDate: String,
    @SerializedName("CityCategoryName") val cityCategoryName: String?,
    @SerializedName("ExpenseTypeName") val expenseTypeName: String?,
    @SerializedName("ControlModeName") val controlModeName: String?,
    @SerializedName("VehicleTypeName") val vehicleTypeName: String?,
    @SerializedName("ExpenseDate") val expenseDate: String?
)
