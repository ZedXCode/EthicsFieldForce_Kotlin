package ethicstechno.com.fieldforce.models.moreoption.orderentry

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
class ProductGroupResponse(
    @SerializedName("OrderDetailsId") var orderDetailsId: Int? = 0,
    @SerializedName("OrderId") var orderId: Int? = 0,
    @SerializedName("ProductId") var productId: Int? = 0,
    @SerializedName("ProductName") var productName: String? = null,
    @SerializedName("ProductGroupId") var productGroupId: Int? = 0,
    @SerializedName("ProductGroupName") var productGroupName: String? = null,
    @SerializedName("Unit") var unit: String? = null,
    @SerializedName("AltUnit") var altUnit: String? = null,
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
    @SerializedName("Scheme") var scheme: String? = null,
    @SerializedName("Success") var success: Boolean? = false,
    @SerializedName("ReturnMessage") var returnMessage: String? = null,
    @SerializedName("Quantity") var qty: BigDecimal = BigDecimal.ZERO,
    @SerializedName("Amount") var amount: BigDecimal = BigDecimal.ZERO,
    @SerializedName("Rate") var price: BigDecimal = BigDecimal.ZERO,
    @SerializedName("PriceListDetailsId") var priceDetailsId: Int = 0,
    @SerializedName("SchemeDetailsId") var schemeDetailsId: Int = 0,
    @SerializedName("Remarks") var remarks: String = "",
    @SerializedName("DiscountAmount") var discountAmount: BigDecimal = BigDecimal.ZERO,
    @SerializedName("AltUnitQuantity") var altUnitQuantity: BigDecimal = BigDecimal.ZERO,
    @SerializedName("QuantityRoundOffType") var quantityRoundOffType: Int = 0,
    @SerializedName("PerUnitWeight") var perUnitWeight: BigDecimal = BigDecimal.ZERO,
    @SerializedName("VisitStatus") var visitStatus: Int = 0,
    @SerializedName("ReferenceTableName") var referenceName: String = "",
    @SerializedName("ReferenceDetailsId") var ReferenceDetailsId: Int = 0,
    @SerializedName("Discount") var discount: BigDecimal = BigDecimal.ZERO,
    var finalQty: BigDecimal = BigDecimal.ZERO
) : Parcelable {
    override fun toString(): String {
        return productGroupName ?: ""
    }
}

