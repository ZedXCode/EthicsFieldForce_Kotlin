package ethicstechno.com.fieldforce.models.moreoption.visit

import com.google.gson.annotations.SerializedName

class CategoryMasterResponse (
    @SerializedName("CategoryMasterId")
    val categoryMasterId: Int? = 0,

    @SerializedName("CategoryName")
    val categoryName: String? = "",

    @SerializedName("IsActive")
    val isActive: Boolean? = false,

    @SerializedName("DataStatus")
    val dataStatus: String? = ""
)