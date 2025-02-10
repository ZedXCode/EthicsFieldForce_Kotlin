package ethicstechno.com.fieldforce.models

import com.google.gson.annotations.SerializedName


data class MoreOptionMenuListResponse(
    @SerializedName("FormId") val formId: Int,
    @SerializedName("FormName") val formName: String,
    @SerializedName("AllowRights") val allowRights: Boolean,
    @SerializedName("IconPath") val iconPath: String,
    @SerializedName("AllowPrint") val allowPrint: Boolean,
) {
}
