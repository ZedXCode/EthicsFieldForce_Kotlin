package ethicstechno.com.fieldforce.models.moreoption.visit

import com.google.gson.annotations.SerializedName

class CompanyMasterResponse (
    @SerializedName("CompanyMasterId") val companyMasterId: Int? = 0,
    @SerializedName("CompanyCode") val companyCode: String? = "",
    @SerializedName("CompanyName") val companyName: String? = "",
    @SerializedName("CompanyShortName") val companyShortName: String? = "",
    @SerializedName("Address") val address: String? = "",
    @SerializedName("CityMasterId") val cityMasterId: Int? = 0,
    @SerializedName("CityName") val cityName: String? = "",
    @SerializedName("PinCode") val pinCode: String? = "",
    @SerializedName("PhoneNo1") val phoneNo1: String? = "",
    @SerializedName("PhoneNo2") val phoneNo2: String? = "",
    @SerializedName("Email") val email: String? = "",
    @SerializedName("PANNo") val panNo: String? = "",
    @SerializedName("GSTNo") val gstNo: String? = "",
    @SerializedName("BankName") val bankName: String? = "",
    @SerializedName("BankAccountNo") val bankAccountNo: String? = "",
    @SerializedName("BankIFSCCode") val bankIFSCCode: String? = "",
    @SerializedName("BankBranchName") val bankBranchName: String? = "",
    @SerializedName("LogoPath") val logoPath: String? = "",
    @SerializedName("CINNo") val cinNo: String? = "",
    @SerializedName("IECNo") val iecNo: String? = "",
    @SerializedName("IsActive") val isActive: Boolean? = false,
    @SerializedName("CommandId") val commandId: Int? = 0
)