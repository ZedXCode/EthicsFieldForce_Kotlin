package ethicstechno.com.fieldforce.models

import android.os.Parcelable
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class CompanySelectionResponse(
    var code: Int,
    var `data`: ArrayList<CompanySelectionData>,
    var error: Error,
    var message: String,
    var status_cd: String
) : Parcelable {
    @Parcelize
    data class Error(
        var code: @RawValue Any?,
        var error_cd: @RawValue Any?,
        var error_msg: @RawValue Any?,
        var message: @RawValue Any?,
        var origin: @RawValue Any?
    ) : Parcelable

    @Parcelize
    data class CompanySelectionData (
        @PrimaryKey(autoGenerate = true)
        var companyMasterId: Int? = null,
        var companyName: String? = null,
        var serverName: String? = null,
        var authenticationType: Int? = null,
        var databaseName: String? = null,
        var userName: String? = null,
        var password: String? = null,
        var version: String? = null
    ) : Parcelable
}