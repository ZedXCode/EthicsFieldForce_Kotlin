package ethicstechno.com.fieldforce.models.profile

import com.google.gson.annotations.SerializedName

data class ZoneListResponse(
    @SerializedName("ZoneId") val zoneId: Int,
    @SerializedName("ZoneName") val zoneName: String
)
