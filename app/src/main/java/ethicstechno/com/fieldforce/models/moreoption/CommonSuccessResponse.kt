package ethicstechno.com.fieldforce.models.moreoption

import com.google.gson.annotations.SerializedName

data class CommonSuccessResponse(
    @SerializedName("Success") val success: Boolean,
    @SerializedName("ReturnMessage") val returnMessage: String?
)