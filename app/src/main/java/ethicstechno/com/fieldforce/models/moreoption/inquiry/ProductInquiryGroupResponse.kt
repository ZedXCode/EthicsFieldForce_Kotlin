package ethicstechno.com.fieldforce.models.moreoption.inquiry

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
class ProductInquiryGroupResponse(
    @SerializedName("InquiryDetailsId") var inquiryDetailsId: Int,
    @SerializedName("InquiryId") var inquiryId: Int,
    @SerializedName("ProductId") var productId: Int? = 0,
    @SerializedName("ProductName") var productName: String? = "",
    @SerializedName("ProductGroupId") var productGroupId: Int? = 0,
    @SerializedName("ProductGroupName") var productGroupName: String = "",
    @SerializedName("Unit") var unit: String? = "",
    @SerializedName("AltUnit") var altUnit: String? = "",
    @SerializedName("ConversionFactor") var conversionFactor: Double? = 0.0,
    @SerializedName("MRP") var mrp: Double? = 0.0,
    @SerializedName("SalesPrice") var salesPrice: BigDecimal? = BigDecimal.ZERO,
    @SerializedName("SalesPrice1") var salesPrice1: Double? = 0.0,
    @SerializedName("SalesPrice2") var salesPrice2: Double? = 0.0,
    @SerializedName("ParameterString") var parameterString: String? = null,
    @SerializedName("UserId") var userId: Int? = 0,
    @SerializedName("StandardDiscount") var standardDiscount: Double? = 0.0,
    @SerializedName("AdditionalDiscount") var additionalDiscount: Double? = 0.0,
    @SerializedName("IsPriceEditable") var isPriceEditable: Boolean? = false,
    @SerializedName("Scheme") var scheme: String? = "",
    @SerializedName("Success") var success: Boolean? = null,
    @SerializedName("ReturnMessage") var returnMessage: String? = "",
    @SerializedName("Quantity") var qty: BigDecimal = BigDecimal.ZERO,
    @SerializedName("Amount") var amount: BigDecimal = BigDecimal.ZERO,
    @SerializedName("Rate") var price: BigDecimal = BigDecimal.ZERO,
    @SerializedName("AltUnitQuantity") var altUnitQuantity: BigDecimal = BigDecimal.ZERO,
    @SerializedName("QuantityRoundOffType") var quantityRoundOffType: Int? = 0,
    @SerializedName("PerUnitWeight") var perUnitWeight: BigDecimal = BigDecimal.ZERO,
    @SerializedName("NewField") var newField: BigDecimal = BigDecimal.ZERO,
    @SerializedName("VisitStatus") var visitStatus: Int = 0,
    @SerializedName("ReferenceTableName") var referenceName: String = "",
    @SerializedName("ReferenceDetailsId") var ReferenceDetailsId: Int = 0,
    @SerializedName("Discount") var discount: BigDecimal = BigDecimal.ZERO,
    @SerializedName("DiscountAmount") var discountAmount: BigDecimal = BigDecimal.ZERO,
    @SerializedName("PriceListDetailsId") var priceDetailsId: Int = 0,
    @SerializedName("SchemeDetailsId") var schemeDetailsId: Int = 0,
    var finalQty: BigDecimal = BigDecimal.ZERO
) : Parcelable {
    override fun toString(): String {
        return productGroupName
    }
}
