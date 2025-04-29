package ethicstechno.com.fieldforce.models.notification

import com.google.gson.annotations.SerializedName

data class NotificationCountResponse(
    @SerializedName("UnreadCount") val unReadCount: Int,
    @SerializedName("TotalCount") val totalCount: Int
)
