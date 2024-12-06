package ethicstechno.com.fieldforce.models.orderentry

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
class OrderDetailsResponse(
    @SerializedName("OrderId") var orderId: Int? = null,
    @SerializedName("OrderDate") var orderDate: String? = null,
    @SerializedName("AccountMasterId") var accountMasterId: Int? = null,
    @SerializedName("AccountName") var accountName: String? = null,
    @SerializedName("ContactPersonName") var contactPersonName: String? = null,
    @SerializedName("CityId") var cityId: Int? = null,
    @SerializedName("CityName") var cityName: String? = null,
    @SerializedName("CategoryMasterId") var categoryMasterId: Int? = null,
    @SerializedName("CategoryName") var categoryName: String? = null,
    @SerializedName("OrderMode") var orderMode: Int? = null,
    @SerializedName("OrderModeName") var orderModeName: String? = null,
    @SerializedName("OrderAmount") var orderAmount: BigDecimal? = null,
    @SerializedName("Remarks") var remarks: String? = null,
    @SerializedName("FilePath") var filePath: String? = null,
    @SerializedName("OrderStatusId") var orderStatusId: Int? = null,
    @SerializedName("OrderStatusName") var orderStatusName: String? = null,
    @SerializedName("UserId") var userId: Int? = null,
    @SerializedName("UserName") var userName: String? = null,
    @SerializedName("CreateDateTime") var createDateTime: String? = null,
    @SerializedName("DistributorAccountMasterId") var distributorAccountMasterId: Int? = null,
    @SerializedName("DistributorAccountName") var distributorAccountName: String? = null,
    @SerializedName("FromDate") var fromDate: String? = null,
    @SerializedName("ToDate") var toDate: String? = null,
    @SerializedName("Success") var success: Boolean? = null,
    @SerializedName("ReturnMessage") var returnMessage: String? = null,
    @SerializedName("OrderDetails") var orderDetails: ArrayList<ProductGroupResponse> = arrayListOf()
) : Parcelable {
    @Parcelize
    data class OrderDetails(
        @SerializedName("OrderDetailsId") var orderDetailsId: Int = 0,
        @SerializedName("OrderId") var orderId: Int? = null,
        @SerializedName("ProductId") var productId: Int? = null,
        @SerializedName("ProductName") var productName: String? = null,
        @SerializedName("Unit") var unit: String? = null,
        @SerializedName("Quantity") var quantity: BigDecimal? = null,
        @SerializedName("PendingQuantity") var pendingQuantity: Int? = null,
        @SerializedName("Rate") var rate: BigDecimal? = null,
        @SerializedName("Amount") var amount: BigDecimal? = null,
        @SerializedName("UserId") var userId: Int? = null,
        @SerializedName("Success") var success: Boolean? = null,
        @SerializedName("ReturnMessage") var returnMessage: String? = null
    ) : Parcelable
}