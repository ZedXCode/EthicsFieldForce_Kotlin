package ethicstechno.com.fieldforce.models.profile

import com.google.gson.annotations.SerializedName

data class StateListResponse(
    @SerializedName("StateId") val stateId: Int,
    @SerializedName("CountryId") val countryId: Int,
    @SerializedName("StateName") val stateName: String
)
