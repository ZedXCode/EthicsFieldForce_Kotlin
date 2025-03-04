package ethicstechno.com.fieldforce.listener

import ethicstechno.com.fieldforce.models.CommonDropDownResponse
import ethicstechno.com.fieldforce.models.dashboarddrill.FilterListResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList

interface FilterDialogListener {
    fun onFilterSubmitClick(
        startDate: String,
        endDate: String,
        dateOption: String,
        dateOptionPosition: Int,
        statusPosition: Int,
        selectedItemPosition: FilterListResponse,
        toString: FilterListResponse,
        selectedVisitType: CommonDropDownResponse,
        selectedPartyDealer: AccountMasterList,
        visitPosition: Int
    )
}
