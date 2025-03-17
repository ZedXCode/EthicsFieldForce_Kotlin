package ethicstechno.com.fieldforce.models

import com.google.gson.annotations.SerializedName

data class ApprovalCountResponse (
    @SerializedName("DocumentCount") var documentCount: Int,
    @SerializedName("DocumentName") val documentName: String,
    @SerializedName("formId") val formId: Int)
{

}
