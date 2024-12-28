package ethicstechno.com.fieldforce.models


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class CommonProductFilterResponse(
    @SerializedName("List1")
    val list1: CommonList? = null,

    @SerializedName("List2")
    val list2: CommonList? = null,

    @SerializedName("List3")
    val list3: CommonList? = null,

    @SerializedName("List4")
    val list4: CommonList? = null,

    @SerializedName("List5")
    val list5: CommonList? = null
) : Parcelable

@Parcelize
class CommonList(
    @SerializedName("headerName")
    val headerName: String? = null,

    @SerializedName("items")
    val items: List<DropDownItem>? = listOf()
) : Parcelable

@Parcelize
class DropDownItem(
    @SerializedName("DropdownKeyId")
    val dropdownKeyId: String? = null,

    @SerializedName("DropdownValue")
    val dropdownValue: String? = null
) : Parcelable
