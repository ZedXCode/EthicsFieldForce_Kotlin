package ethicstechno.com.fieldforce.models.moreoption.visit

import com.google.gson.annotations.SerializedName

class DivisionMasterResponse (
    @SerializedName("DivisionMasterId")
    val divisionMasterId: Int? = 0,

    @SerializedName("DivisionName")
    val divisionName: String? = "",

    @SerializedName("IsActive")
    val isActive: Boolean? = false,

    @SerializedName("CommandId")
    val commandId: Int? = 0,

    @SerializedName("CreateBy")
    val createBy: Int? = 0
)