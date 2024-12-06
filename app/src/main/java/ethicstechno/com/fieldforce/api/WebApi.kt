package ethicstechno.com.fieldforce.api

import ethicstechno.com.fieldforce.models.LoginResponse
import com.google.gson.JsonObject
import ethicstechno.com.fieldforce.models.AppRegistrationResponse
import ethicstechno.com.fieldforce.models.CheckUserMobileResponse
import ethicstechno.com.fieldforce.models.CommonDropDownListModel
import ethicstechno.com.fieldforce.models.CompanySelectionResponse
import ethicstechno.com.fieldforce.models.moreoption.CommonSuccessResponse
import ethicstechno.com.fieldforce.models.attendance.CurrentMonthAttendanceResponse
import ethicstechno.com.fieldforce.models.attendance.PunchInResponse
import ethicstechno.com.fieldforce.models.attendance.UserLastSyncResponse
import ethicstechno.com.fieldforce.models.attendance.UserLocationListResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardDrillResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.DashboardListResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.FilterListResponse
import ethicstechno.com.fieldforce.models.dynamiccontent.DynamicPageContentList
import ethicstechno.com.fieldforce.models.moreoption.DynamicMenuListResponse
import ethicstechno.com.fieldforce.models.moreoption.expense.*
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveApplicationListResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveApplicationResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveTypeListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.moreoption.tourplan.TourPlanListResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.VisitListResponse
import ethicstechno.com.fieldforce.models.orderentry.OrderDetailsResponse
import ethicstechno.com.fieldforce.models.orderentry.OrderEntryResponse
import ethicstechno.com.fieldforce.models.orderentry.OrderListResponse
import ethicstechno.com.fieldforce.models.orderentry.ProductGroupResponse
import ethicstechno.com.fieldforce.models.profile.CountryListResponse
import ethicstechno.com.fieldforce.models.profile.StateListResponse
import ethicstechno.com.fieldforce.models.profile.UserProfileResponse
import ethicstechno.com.fieldforce.models.profile.ZoneListResponse
import ethicstechno.com.fieldforce.models.reports.*
import ethicstechno.com.fieldforce.models.trip.GetVisitFromPlaceListResponse
import ethicstechno.com.fieldforce.models.trip.TripSubmitResponse
import ethicstechno.com.fieldforce.models.trip.VehicleTypeListResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface WebApi {

    companion object {
        const val BASE_URL = "http://ffms.ethicstechno.com:41422/"
    }

    @POST("api/values/GetAppRegistration")
    fun getAppRegistration(@Body jsonObject: JsonObject): Call<List<AppRegistrationResponse>>

    @POST("api/values/AuthenticateUser")
    fun loginApi(@Body jsonObject: JsonObject): Call<LoginResponse>

    @POST("api/UserProfile/CheckUserMobileDevice")
    fun checkUserMobileDevice(@Body jsonObject: JsonObject): Call<CheckUserMobileResponse>

    @POST("api/Report/GetDashboardList")
    fun getDashboardList(@Body jsonObject: JsonObject): Call<List<DashboardListResponse>>

    @POST("api/Attendance/GetCurrentMonthAttendance")
    fun getCurrentMonthAttendance(@Body jsonObject: JsonObject): Call<List<CurrentMonthAttendanceResponse>>

    @POST("api/Attendance/ClockIn")
    fun punchInApi(@Body jsonObject: JsonObject): Call<PunchInResponse>

    @POST("api/Attendance/ClockOut")
    fun punchOutApi(@Body jsonObject: JsonObject): Call<PunchInResponse>

    @POST("api/Attendance/GetUsersLastSyncLocation")
    fun getUserLastSyncLocation(@Body jsonObject: JsonObject): Call<List<UserLastSyncResponse>>

    @POST("api/Report/GetReportList")
    fun getReportList(@Body jsonObject: JsonObject): Call<List<ReportListResponse>>

    @POST("api/UserProfile/GetSubOrdinateUserList")
    fun getUserList(@Body jsonObject: JsonObject): Call<List<UserListResponse>>

    @POST("api/Trip/GetTripList")
    fun getTripList(@Body jsonObject: JsonObject): Call<List<TripReportResponse>>

    @POST("api/Trip/GetTripAttendanceLocationList")
    fun getTripSummeryList(@Body jsonObject: JsonObject): Call<List<TripSummeryReportResponse>>

    @POST("api/Trip/GetTripListByAttendanceId")
    fun getTripListByAttendanceId(@Body jsonObject: JsonObject): Call<List<TripListByAttendanceIdResponse>>

    @POST("api/Expense/GetExpenseList")
    fun getExpenseList(@Body jsonObject: JsonObject): Call<List<ExpenseListResponse>>

    @POST("api/Expense/GetExpenseToApprove")
    fun getExpenseApprovalList(@Body jsonObject: JsonObject): Call<List<ExpenseListResponse>>

    @POST("api/Expense/GetExpenseCityList")
    fun getExpenseCityList(@Body jsonObject: JsonObject): Call<List<ExpenseCityListResponse>>

    @POST("api/Expense/GetExpenseTypeList")
    fun getExpenseTypeList(@Body jsonObject: JsonObject): Call<List<ExpenseTypeListResponse>>

    @POST("api/Expense/GetExpenseLimit")
    fun getExpenseLimit(@Body jsonObject: JsonObject): Call<List<ExpenseLimitResponse>>

    @POST("api/Expense/ExpenseAdd")
    fun addExpense(@Body jsonObject: JsonObject): Call<AddExpenseResponse>

    @POST("api/Expense/ExpenseUpdate")
    fun updateExpense(@Body jsonObject: JsonObject): Call<AddExpenseResponse>

    @POST("api/Attendance/LeaveTypeGet")
    fun getLeaveTypeList(@Body jsonObject: JsonObject): Call<List<LeaveTypeListResponse>>

    @POST("api/Trip/GetVehicleTypeList")
    fun getVehicleTypeList(): Call<List<VehicleTypeListResponse>>

    @POST("api/Attendance/LeaveApplicationAddUpdate")
    fun leaveApplicationAddUpdate(@Body jsonObject: JsonObject): Call<LeaveApplicationResponse>

    @POST("api/Attendance/LeaveApplicationDelete")
    fun deleteLeaveApplication(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @POST
    fun dashboardDrill(
        @Url url: String,
        @Body jsonObject: JsonObject
    ): Call<List<DashboardDrillResponse>>

    @POST("api/Attendance/LeaveApplicationGet")
    fun getLeaveApplicationList(@Body jsonObject: JsonObject): Call<List<LeaveApplicationListResponse>>

    @POST("api/Attendance/LeaveApprovalPendingGet")
    fun getLeaveApplicationApprovalList(@Body jsonObject: JsonObject): Call<List<LeaveApplicationListResponse>>

    @POST("api/Attendance/LeaveApplicationApproval")
    fun leaveApplicationApproval(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @POST("api/Report/GetDropDownValue")
    fun getDropDownValueList(@Body jsonObject: JsonObject): Call<List<FilterListResponse>>

    @POST("api/Visit/GetVisitFromPlace")
    fun getVisitFromPlace(@Body jsonObject: JsonObject): Call<List<GetVisitFromPlaceListResponse>>

    @POST("api/Trip/TripStart")
    fun startTripApi(@Body jsonObject: JsonObject): Call<TripSubmitResponse>

    @POST("api/Trip/TripEnd")
    fun endTripApi(@Body jsonObject: JsonObject): Call<TripSubmitResponse>

    @POST("api/AccountMaster/GetCityStateCountryList")
    fun getCityStateCountryList(@Body jsonObject: JsonObject): Call<List<ExpenseCityListResponse>>

    @POST("api/AccountMaster/GetAccountMaster")
    fun getAccountMasterList(@Body jsonObject: JsonObject): Call<List<AccountMasterList>>

    @POST("api/AccountMaster/GetCategoryMasterList")
    fun getPartyDealerCategoriesList(): Call<List<LeaveTypeListResponse>>

    @POST("api/AccountMaster/AccountMasterAdd")
    fun addPartyDealer(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @POST("api/AccountMaster/AccountMasterUpdate")
    fun updatePartyDealer(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @POST("api/AccountMaster/AccountMasterDelete")
    fun deletePartyDealer(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @POST("api/Visit/GetVisitAccountMaster")
    fun getVisitList(@Body jsonObject: JsonObject): Call<List<VisitListResponse>>

    @POST("api/Visit/GetCategoryMasterList")
    fun getVisitTypeList(): Call<List<LeaveTypeListResponse>>

    @POST("api/Visit/visitAdd")
    fun addVisit(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @POST("api/Trip/TourPlanGet")
    fun getTourPlanList(@Body jsonObject: JsonObject): Call<List<TourPlanListResponse>>

    @POST("api/UserProfile/GetCityList")
    fun getCityListApi(@Body jsonObject: JsonObject): Call<List<ExpenseCityListResponse>>

    @POST("api/Trip/TourPlanInsertUpdate")
    fun tourPlanAddUpdate(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @POST("api/DynamicPage/GetDynamicScreenMenuList")
    fun getDynamicMenuList(@Body jsonObject: JsonObject): Call<List<DynamicMenuListResponse>>

    @POST("api/Expense/ApproveExpense")
    fun expenseApprove(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @POST("api/Expense/RejectExpense")
    fun expenseReject(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @POST("api/UserProfile/getUserProfile")
    fun getUserProfile(@Body jsonObject: JsonObject): Call<UserProfileResponse>

    @POST("api/UserProfile/GetCountryList")
    fun getCountryList(@Body jsonObject: JsonObject): Call<List<CountryListResponse>>

    @POST("api/UserProfile/GetStateList")
    fun getStateList(@Body jsonObject: JsonObject): Call<List<StateListResponse>>

    @POST("api/UserProfile/GetZoneList")
    fun getZoneList(@Body jsonObject: JsonObject): Call<List<ZoneListResponse>>

    @POST("api/UserProfile/UpdateUserProfile")
    fun updateUserProfile(@Body jsonObject: JsonObject): Call<UserProfileResponse>

    @POST("api/values/ChangePassword")
    fun changePassword(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @POST("api/DynamicPage/GetDynamicPageContent")
    fun getDynamicPageContent(@Body jsonObject: JsonObject): Call<List<DynamicPageContentList>>

    @POST("api/Feedback/AddFeedback")
    fun addFeedback(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @POST("api/Attendance/SendUserLocation")
    fun sendUserLocation(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @POST("api/Trip/SendUserTripLocation")
    fun sendTripLocation(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @POST("api/Visit/GetVisit")
    fun visitReportList(@Body jsonObject: JsonObject): Call<List<VisitReportListResponse>>

    @POST("api/Trip/GetUserTripLocationList")
    fun getTripRoadMapList(@Body jsonObject: JsonObject): Call<List<TripRoadMapResponse>>

    @POST("api/DynamicPage/PaymentFollowUpGet")
    fun getPaymentFollowUp(@Body jsonObject: JsonObject): Call<List<PaymentFollowUpResponse>>

    @POST("api/DynamicPage/PaymentFollowUpInsertUpdate")
    fun addPaymentFollowUp(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @POST("api/Expense/ExpenseDelete")
    fun expenseDelete(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @POST("api/Attendance/GetUserLocationListById")
    fun userLocationList(@Body jsonObject: JsonObject): Call<List<UserLocationListResponse>>

    @POST("api/AccountMaster/GetDropDownValueList")
    fun getCommonDropDownList(@Body jsonObject: JsonObject): Call<List<CommonDropDownListModel>>

    @POST("api/Order/GetProductGroupList")
    fun getProductGroupList(@Body jsonObject: JsonObject): Call<List<ProductGroupResponse>>

    @POST("api/Order/GetProductList")
    fun getProductList(@Body jsonObject: JsonObject): Call<List<ProductGroupResponse>>

    @POST("api/Order/OrderInsertUpdate")
    fun addOrderInsertUpdate(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @POST("api/Order/GetOrderList")
    fun getOrderList(@Body jsonObject: JsonObject): Call<List<OrderListResponse>>

    @POST("api/Order/GetOrderDetails")
    fun getOrderDetails(@Body jsonObject: JsonObject): Call<List<OrderDetailsResponse>>

    @POST("api/Order/OrderDetailsDelete")
    fun deleteOrderDetails(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @POST("api/AccountMaster/GetAccountMasterList")
    fun getOrderAccountMasterList(@Body jsonObject: JsonObject): Call<List<AccountMasterList>>

    @POST("api/Order/OrderDelete")
    fun deleteOrderEntry(@Body jsonObject: JsonObject): Call<CommonSuccessResponse>

    @GET("api/v1/companylist/get")
    fun getCompanyList(): Call<CompanySelectionResponse>

}
