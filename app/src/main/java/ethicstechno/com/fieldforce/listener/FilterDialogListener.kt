package ethicstechno.com.fieldforce.listener

import ethicstechno.com.fieldforce.models.dashboarddrill.FilterListResponse
import ethicstechno.com.fieldforce.models.moreoption.leave.LeaveTypeListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.moreoption.visit.CategoryMasterResponse

interface FilterDialogListener {
    fun onFilterSubmitClick(
        startDate: String,
        endDate: String,
        dateOption: String,
        dateOptionPosition: Int,
        statusPosition: Int,
        selectedItemPosition: FilterListResponse,
        toString: FilterListResponse,
        selectedVisitType: CategoryMasterResponse,
        selectedPartyDealer: AccountMasterList,
        visitPosition: Int
    )
}
