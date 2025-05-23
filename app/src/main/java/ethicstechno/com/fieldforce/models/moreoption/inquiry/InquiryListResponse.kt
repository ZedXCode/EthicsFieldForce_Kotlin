package ethicstechno.com.fieldforce.models.moreoption.inquiry

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

class InquiryListResponse(
    @SerializedName("InquiryId") var inquiryId: Int? = null,
    @SerializedName("InquiryDate") var inquiryDate: String? = null,
    @SerializedName("AccountMasterId") var accountMasterId: Int? = null,
    @SerializedName("AccountName") var accountName: String? = null,
    @SerializedName("ContactPersonName") var contactPersonName: String? = null,
    @SerializedName("CityId") var cityId: Int? = null,
    @SerializedName("CityName") var cityName: String? = null,
    @SerializedName("CategoryMasterId") var categoryMasterId: Int? = null,
    @SerializedName("CategoryName") var categoryName: String? = null,
    @SerializedName("InquiryMode") var inquiryMode: Int? = null,
    @SerializedName("InquiryModeName") var inquiryModeName: String? = null,
    @SerializedName("InquiryAmount") var inquiryAmount: BigDecimal? = null,
    @SerializedName("Remarks") var remarks: String? = null,
    @SerializedName("FilePath") var filePath: String? = null,
    @SerializedName("InquiryStatusId") var inquiryStatusId: Int? = null,
    @SerializedName("InquiryStatusName") var inquiryStatusName: String? = null,
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
    @SerializedName("FilePath2") var filePath2: String? = null,
    @SerializedName("FilePath3") var filePath3: String? = null,
    @SerializedName("FilePath4") var filePath4: String? = null,
    @SerializedName("CompanyName") var companyName: String? = null,
    @SerializedName("BranchName") var branchName: String? = null,
    @SerializedName("DivisionName") var divisionName: String? = null,
    @SerializedName("DocumentNo") var documentNo: Int? = null,
    @SerializedName("ParameterString") var parameterString: String? = null,
    @SerializedName("InquiryDetails") var inquiryDetails: String? = null,
    @SerializedName("Success") var success: Boolean? = null,
    @SerializedName("ReturnMessage") var returnMessage: String? = null,
    @SerializedName("AllowEdit") var allowEdit: Boolean? = false,
    @SerializedName("AllowDelete") var allowDelete: Boolean? = false,
    @SerializedName("ProductCount") var productCount: Int? = 0,
    @SerializedName("TotalQuantity") var totalQuantity: Double? = 0.0,
    @SerializedName("StatusColor") var statusColor: String? = null
)
