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
    @SerializedName("EntrySystem") var entrySystem: Int? = null,
    @SerializedName("DDMCurrencySetupId") var ddmCurrencySetupId: Int? = null,
    @SerializedName("DDMTransactionTypeId") var ddmTransactionTypeId: Int? = null,
    @SerializedName("ConsigneeAccountMasterId") var consigneeAccountMasterId: Int? = null,
    @SerializedName("ShippingAddressId") var shippingAddressId: Int? = null,
    @SerializedName("ShippingAddress") var shippingAddress: String? = null,
    @SerializedName("DDMFreightTermsId") var ddmFreightTermsId: Int? = null,
    @SerializedName("ExpectedDispatchDate") var expectedDispatchDate: String? = null,
    @SerializedName("CreditDays") var creditDays: Int? = null,
    @SerializedName("TermsAndCondition") var termsAndCondition: String? = null,
    @SerializedName("AllowAdd") var allowAdd: Boolean? = null,
    @SerializedName("AllowDelete") var allowDelete: Boolean? = null,
    @SerializedName("AllowEdit") var allowEdit: Boolean? = null,
    @SerializedName("AllowRights") var allowRights: Boolean? = null,
    @SerializedName("AllowView") var allowView: Boolean? = null,
    @SerializedName("Status") var status: String? = null,
    @SerializedName("Success") var success: Boolean? = null,
    @SerializedName("ReturnMessage") var returnMessage: String? = null,
    @SerializedName("OrderDetails") var orderDetails: ArrayList<ProductGroupResponse> = arrayListOf(),
    @SerializedName("TransactionTypeName") var transactionTypeName: String = "",
    @SerializedName("ConsigneeName") var consigneeName: String = "",
    @SerializedName("FreightTermsName") var freightTermsName: String = ""

) : Parcelable {

    @Parcelize
    data class OrderDetail(
        @SerializedName("OrderDetailsId") var orderDetailsId: Int = 0,
        @SerializedName("OrderId") var orderId: Int? = null,
        @SerializedName("ProductId") var productId: Int? = null,
        @SerializedName("ProductName") var productName: String? = null,
        @SerializedName("Unit") var unit: String? = null,
        @SerializedName("Quantity") var quantity: BigDecimal? = null,
        @SerializedName("PendingQuantity") var pendingQuantity: BigDecimal? = null,
        @SerializedName("Rate") var rate: BigDecimal? = null,
        @SerializedName("Amount") var amount: BigDecimal? = null,
        @SerializedName("MRP") var mrp: BigDecimal? = null,
        @SerializedName("Discount") var discount: BigDecimal? = null,
        @SerializedName("AdditionalDiscount") var additionalDiscount: BigDecimal? = null,
        @SerializedName("PriceListDetailsId") var priceListDetailsId: Int? = null,
        @SerializedName("SchemeDetailsId") var schemeDetailsId: Int? = null,
        @SerializedName("Remarks") var remarks: String? = null,
        @SerializedName("DiscountAmount") var discountAmount: BigDecimal? = null,
        @SerializedName("UserId") var userId: Int? = null,
        @SerializedName("VisitStatus") var visitStatus: Int? = null,
        @SerializedName("ParameterString") var parameterString: String? = null,
        @SerializedName("Success") var success: Boolean? = null,
        @SerializedName("ReturnMessage") var returnMessage: String? = null
    ) : Parcelable
}
