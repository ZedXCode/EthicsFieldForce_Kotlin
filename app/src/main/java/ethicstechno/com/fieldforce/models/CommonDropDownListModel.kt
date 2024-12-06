package ethicstechno.com.fieldforce.models

import com.google.gson.annotations.SerializedName

data class CommonDropDownListModel(
    @SerializedName("DropDownValueId")
    val dropDownValueId: Int,
    @SerializedName("DropDownValueName")
    val dropDownValueName: String,
    @SerializedName("DropDownFieldName")
    val dropDownFieldName: String
)