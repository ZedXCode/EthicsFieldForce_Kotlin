package ethicstechno.com.fieldforce.models

import com.google.gson.annotations.SerializedName

data class CheckUserMobileResponse(
    @SerializedName("UserId")
    val userId: String,

    @SerializedName("UserName")
    val userName: String?,

    @SerializedName("IMEINumber")
    val imeiNumber: String,

    @SerializedName("Photo")
    val photo: String?,

    @SerializedName("FirstName")
    val firstName: String?,

    @SerializedName("MiddleName")
    val middleName: String?,

    @SerializedName("LastName")
    val lastName: String?,

    @SerializedName("CountryId")
    val countryId: String?,

    @SerializedName("CountryName")
    val countryName: String?,

    @SerializedName("StateId")
    val stateId: String?,

    @SerializedName("StateName")
    val stateName: String?,

    @SerializedName("ZoneId")
    val zoneId: String?,

    @SerializedName("ZoneName")
    val zoneName: String?,

    @SerializedName("CityId")
    val cityId: String?,

    @SerializedName("CityName")
    val cityName: String?,

    @SerializedName("PinCode")
    val pinCode: String?,

    @SerializedName("Address")
    val address: String?,

    @SerializedName("PersonalEmailId")
    val personalEmailId: String?,

    @SerializedName("PersonalMobileNo")
    val personalMobileNo: String?,

    @SerializedName("Password")
    val password: String?,

    @SerializedName("IsActive")
    val isActive: Boolean,

    @SerializedName("IsDeleted")
    val isDeleted: String?,

    @SerializedName("CommandId")
    val commandId: String?,

    @SerializedName("CreateBy")
    val createBy: String?,

    @SerializedName("CreateDateTime")
    val createDateTime: String?,

    @SerializedName("UpdateBy")
    val updateBy: String?,

    @SerializedName("UpdateDateTime")
    val updateDateTime: String?,

    @SerializedName("LastLoginDateTime")
    val lastLoginDateTime: String?,

    @SerializedName("Success")
    val success: Boolean,

    @SerializedName("ReturnMessage")
    val returnMessage: String,

    @SerializedName("PhoneModelNo")
    val phoneModelNo: String,

    @SerializedName("PhoneBrandName")
    val phoneBrandName: String,

    @SerializedName("PhoneOSVersion")
    val phoneOSVersion: String,

    @SerializedName("BatteryPercentage")
    val batteryPercentage: String
)