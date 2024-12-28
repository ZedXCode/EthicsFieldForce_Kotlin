package ethicstechno.com.fieldforce.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
class CommonDropDownResponse(
    @SerializedName("DropdownMasterId")
    val dropdownMasterId: Int? = 0,

    @SerializedName("DropdownName")
    val dropdownName: String? = "",

    @SerializedName("DropdownMasterDetailsId")
    val dropdownMasterDetailsId: Int? = 0,

    @SerializedName("DropdownKeyId")
    val dropdownKeyId: String? = "",

    @SerializedName("DropdownValue")
    val dropdownValue: String? = "",

    @SerializedName("ParentDropdownMasterDetailsId")
    val parentDropdownMasterDetailsId: Int? = 0,

    @SerializedName("IsActive")
    val isActive: Boolean? = false
) : Parcelable

