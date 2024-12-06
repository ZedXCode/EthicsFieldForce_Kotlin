package ethicstechno.com.fieldforce.models.reports

import com.google.gson.annotations.SerializedName

data class UserListResponse(
    @SerializedName("UserId") val userId: Int,
    @SerializedName("UserName") val userName : String
){
    constructor() : this (0, "")
}