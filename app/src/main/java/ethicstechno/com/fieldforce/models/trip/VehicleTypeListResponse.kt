package ethicstechno.com.fieldforce.models.trip

import com.google.gson.annotations.SerializedName

data class VehicleTypeListResponse(
    @SerializedName("VehicleTypeId")
    val vehicleTypeId: Int,

    @SerializedName("VehicleTypeName")
    val vehicleTypeName: String,

    @SerializedName("UserId")
    val userId: Int,

    @SerializedName("ExpenseTypeId")
    val expenseTypeId: Int,

    @SerializedName("CityId")
    val cityId: Int
)
