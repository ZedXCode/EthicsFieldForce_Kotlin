package ethicstechno.com.fieldforce.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


@Parcelize
class CommonDropDownResponse (
    @SerializedName("Code")
    var code: Int,
    @SerializedName("Data")
    var `data`: ArrayList<CommonDropDownListModelNew>,
    @SerializedName("Error")
    var error: Error,
    @SerializedName("Message")
    var message: String,
    @SerializedName("Status_cd")
    var status_cd: String
) : Parcelable {
    @Parcelize
    data class Error(
        @SerializedName("Code")
        var code: @RawValue Any?,
        @SerializedName("Error_cd")
        var error_cd: @RawValue Any?,
        @SerializedName("Error_msg")
        var error_msg: @RawValue Any?,
        @SerializedName("Message")
        var message: @RawValue Any?,
        @SerializedName("Origin")
        var origin: @RawValue Any?
    ) : Parcelable

    @Parcelize
    data class TypeData(
        var generalMasterDetailsId: Int?,
        var generalValue:String = ""
    ): Parcelable

    @Parcelize
    data class CommonDropDownListModelNew(
        @SerializedName("DropdownMasterId")
        val dropdownMasterId: Int,

        @SerializedName("DropdownName")
        val dropdownName: String,

        @SerializedName("DropdownMasterDetailsId")
        val dropdownMasterDetailsId: Int,

        @SerializedName("DropdownKeyId")
        val dropdownKeyId: String,

        @SerializedName("DropdownValue")
        val dropdownValue: String,

        @SerializedName("ParentDropdownMasterDetailsId")
        val parentDropdownMasterDetailsId: Int,

        @SerializedName("IsActive")
        val isActive: Boolean
    ):Parcelable
}

