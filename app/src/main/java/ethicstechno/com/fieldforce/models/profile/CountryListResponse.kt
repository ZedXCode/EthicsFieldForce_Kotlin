package ethicstechno.com.fieldforce.models.profile

import com.google.gson.annotations.SerializedName

data class CountryListResponse(
    @SerializedName("CountryId") val countryId: Int,
    @SerializedName("CountryName") val countryName: String
)
