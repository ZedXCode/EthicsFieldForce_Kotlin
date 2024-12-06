package ethicstechno.com.fieldforce.models.moreoption.expense
import com.google.gson.annotations.SerializedName

data class ExpenseTypeListResponse(
    @SerializedName("ExpenseTypeId") val expenseTypeId: Int,
    @SerializedName("ExpenseTypeName") val expenseTypeName: String,
    @SerializedName("ControlModeId") val controlModeId: Int,
    @SerializedName("ControlModeName") val controlModeName: String,
    @SerializedName("VehicleTypeId") val vehicleTypeId: Int,
    @SerializedName("VehicleTypeName") val vehicleTypeName: String,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("CityId") val cityId: Int,
    @SerializedName("IsAttachmentRequired") val isAttachmentRequired: Boolean,
    @SerializedName("TotalTrips") val totalTrips:String,
    @SerializedName("MapKm") val mapKM:Double,
    @SerializedName("ActualKm") val actualKM: Double,
    @SerializedName("EligibleAmount") val eligibleAmount: Double,
    @SerializedName("ExpenseAmount") val expenseAmount: Double,
    @SerializedName("ExpenseDate") val expenseDate: String
){
    constructor() : this(
        0,
        "Select Expense Type",
        0,
        "",
        0,
        "",
        0,
        0,
        false,
        "",
        0.0,
        0.0,
        0.0,
        0.0,
        ""
    )
}
