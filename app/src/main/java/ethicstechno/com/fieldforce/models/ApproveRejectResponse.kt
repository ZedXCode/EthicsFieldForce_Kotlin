package ethicstechno.com.fieldforce.models

import com.google.gson.annotations.SerializedName


//data class ApproveRejectResponse(
//    @SerializedName("FileName") var fileName: String,
//    @SerializedName("status_cd") val statusCd: Int,
//    @SerializedName("status_msg") val statusMsg: String
//) {
//}

data class ApproveRejectResponse(
    @SerializedName("Success") val success: Boolean,
    @SerializedName("ReturnMessage") val returnMessage: String?
)
