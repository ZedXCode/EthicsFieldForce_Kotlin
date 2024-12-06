package ethicstechno.com.fieldforce.models.profile

import com.google.gson.annotations.SerializedName

data class CityListResponse(
    @SerializedName("StateId") val stateId: Int,
    @SerializedName("CityId") val cityId: Int,
    @SerializedName("CityName") val cityName: String
)
