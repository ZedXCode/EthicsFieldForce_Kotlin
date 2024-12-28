package ethicstechno.com.fieldforce.models.moreoption.visit

import com.google.gson.annotations.SerializedName

class BranchMasterResponse (
    @SerializedName("BranchMasterId")
    val branchMasterId: Int? = 0,

    @SerializedName("BranchName")
    val branchName: String? = "",

    @SerializedName("IsActive")
    val isActive: Boolean? = false,

    @SerializedName("CommandId")
    val commandId: Int? = 0,

    @SerializedName("CreateBy")
    val createBy: Int? = 0

)