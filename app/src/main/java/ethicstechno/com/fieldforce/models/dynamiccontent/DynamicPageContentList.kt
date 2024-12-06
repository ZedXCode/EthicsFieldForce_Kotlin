package ethicstechno.com.fieldforce.models.dynamiccontent

import com.google.gson.annotations.SerializedName

data class DynamicPageContentList(
    @SerializedName("DynamicPageId") val dynamicPageId: Int,
    @SerializedName("DynamicPageName") val dynamicPageName: String,
    @SerializedName("DynamicPageContent") val dynamicPageContent: String
)
