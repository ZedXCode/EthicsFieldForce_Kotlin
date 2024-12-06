package ethicstechno.com.fieldforce.models.moreoption.expense
import com.google.gson.annotations.SerializedName

data class ExpenseCityListResponse(
    @SerializedName("StateId") val stateId: Int,
    @SerializedName("CityId") val cityId: Int,
    @SerializedName("CityName") val cityName: String,
    @SerializedName("UserId") val userId: Int,
    @SerializedName("ExpenseDate") val expenseDate: String
){
    constructor() : this(0, 0, "", 0, "")
}
