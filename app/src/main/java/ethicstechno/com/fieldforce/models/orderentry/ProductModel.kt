package ethicstechno.com.fieldforce.models.orderentry

data class ProductModel (
    var groupData: ProductGroupResponse,
    var productData: ProductGroupResponse,
    var price:Double,
    var amount: Double,
    var unit:String,
    var qty:Int
)