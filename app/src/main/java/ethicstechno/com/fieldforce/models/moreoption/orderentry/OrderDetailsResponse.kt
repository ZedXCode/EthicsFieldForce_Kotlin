package ethicstechno.com.fieldforce.models.moreoption.orderentry

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
    @SerializedName("CompanyMasterId") var companyMasterId: Int? = null,
    @SerializedName("BranchMasterId") var branchMasterId: Int? = null,
    @SerializedName("DivisionMasterId") var divisionMasterId: Int? = null,
    @SerializedName("ModeOfCommunication") var modeOfCommunication: Int? = null,
    @SerializedName("DocumentNo") var documentNo: Int? = null,
    @SerializedName("ParameterString") var parameterString: String? = null,
    @SerializedName("FilePath2") var filePath2: String? = null,
    @SerializedName("FilePath3") var filePath3: String? = null,
    @SerializedName("FilePath4") var filePath4: String? = null,
    @SerializedName("CompanyName") var companyName: String? = null,
    @SerializedName("BranchName") var branchName: String? = null,
    @SerializedName("DivisionName") var divisionName: String? = null,
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
        @SerializedName("ProductGroupId") var productGroupId: Int? = null,
        @SerializedName("ProductGroupName") var productGroupName: String? = null,
        @SerializedName("Unit") var unit: String? = null,
        @SerializedName("AltUnit") var altUnit: String? = null,
        @SerializedName("ConversionFactor") var conversionFactor: Double? = null,
        @SerializedName("MRP") var mrp: Double? = null,
        @SerializedName("SalesPrice") var salesPrice: BigDecimal? = null,
        @SerializedName("Quantity") var quantity: BigDecimal? = null,
        @SerializedName("PendingQuantity") var pendingQuantity: Int? = null,
        @SerializedName("Rate") var rate: BigDecimal? = null,
        @SerializedName("Amount") var amount: BigDecimal? = null,
        @SerializedName("Discount") var discount: BigDecimal? = null,
        @SerializedName("TaxPercentage") var taxPercentage: Double? = null,
        @SerializedName("NetAmount") var netAmount: BigDecimal? = null,
        @SerializedName("UserId") var userId: Int? = null,
        @SerializedName("Success") var success: Boolean? = null,
        @SerializedName("ReturnMessage") var returnMessage: String? = null
    ) : Parcelable
}
