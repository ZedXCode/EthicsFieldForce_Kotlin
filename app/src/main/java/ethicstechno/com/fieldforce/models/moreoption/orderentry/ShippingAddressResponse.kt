package ethicstechno.com.fieldforce.models.moreoption.orderentry

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShippingAddressResponse(
    @SerializedName("ShippingAddressId")
    val shippingAddressId: Int = 0,
    @SerializedName("ShippingAddress")
    val shippingAddress: String = "",
    @SerializedName("CityName")
    val cityName: String = "",
    @SerializedName("StateName")
    val stateName: String = "",
    @SerializedName("GstNo")
    val gstNo: String = ""
) : Parcelable
