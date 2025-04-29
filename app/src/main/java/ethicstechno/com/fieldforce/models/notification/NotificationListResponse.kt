package ethicstechno.com.fieldforce.models.notification

import com.google.gson.annotations.SerializedName

data class NotificationListResponse(
    @SerializedName("NotificationId") val notificationId: Int,
    @SerializedName("FromUserMasterId") val fromUserMasterId: Int,
    @SerializedName("FromUserName") val fromUserName: String,
    @SerializedName("ToUserMasterId") val toUserMasterId: Int,
    @SerializedName("ToUserName") val toUserName: String,
    @SerializedName("SentDateTime") val sentDateTime: String,
    @SerializedName("MessageSentDevice") val messageSentDevice: String,
    @SerializedName("NotificationType") val notificationType: Int,
    @SerializedName("NotificationMessage") val notificationMessage: String,
    @SerializedName("NotificationTitle") val notificationTitle: String,
    @SerializedName("IsRead") val isRead: Boolean,
    @SerializedName("ReadDatetime") val readDatetime: String?, // Nullable
    @SerializedName("TableName") val tableName: String,
    @SerializedName("TransactionId") val transactionId: Int,
    @SerializedName("FormId") val formId: Int
)
