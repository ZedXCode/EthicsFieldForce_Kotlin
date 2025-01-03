package ethicstechno.com.fieldforce.models.moreoption.visit

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class InquiryResponse(
    @SerializedName("Select")
    val select: Int? = 0,

    @SerializedName("DocumentDetailsId")
    val documentDetailsId: Int? = 0,

    @SerializedName("DocumentId")
    val documentId: Int? = 0,

    @SerializedName("DocumentMode")
    val documentMode: Int? = 0,

    @SerializedName("CategoryMasterId")
    val categoryMasterId: Int? = 0,

    @SerializedName("DocumentDate")
    val documentDate: String? = "",

    @SerializedName("ProductId")
    val productId: Double? = 0.0,

    @SerializedName("Quantity")
    val quantity: Double? = 0.0,

    @SerializedName("Rate")
    val rate: Double? = 0.0,

    @SerializedName("Amount")
    val amount: Double? = 0.0,

    @SerializedName("CategoryName")
    val categoryName: String? = "",

    @SerializedName("ProductName")
    val productName: String? = "",

    @SerializedName("DocumentNo")
    val documentNo: Int? = 0,

    @SerializedName("TableName")
    val tableName: String? = "",
    // New property to track selection state
    var isSelected: Boolean = false
) : Parcelable
