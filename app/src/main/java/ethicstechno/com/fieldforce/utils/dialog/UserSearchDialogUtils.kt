package ethicstechno.com.fieldforce.utils.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.models.CommonDropDownResponse
import ethicstechno.com.fieldforce.models.moreoption.expense.ExpenseCityListResponse
import ethicstechno.com.fieldforce.models.moreoption.expense.ExpenseTypeListResponse
import ethicstechno.com.fieldforce.models.moreoption.orderentry.ShippingAddressResponse
import ethicstechno.com.fieldforce.models.moreoption.partydealer.AccountMasterList
import ethicstechno.com.fieldforce.models.moreoption.visit.BranchMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.CompanyMasterResponse
import ethicstechno.com.fieldforce.models.moreoption.visit.DivisionMasterResponse
import ethicstechno.com.fieldforce.models.profile.CountryListResponse
import ethicstechno.com.fieldforce.models.profile.StateListResponse
import ethicstechno.com.fieldforce.models.profile.ZoneListResponse
import ethicstechno.com.fieldforce.models.reports.UserListResponse
import ethicstechno.com.fieldforce.ui.adapter.BranchAdapterForSearchViewDialog
import ethicstechno.com.fieldforce.ui.adapter.CommonDropDownAdapterNewForSearchViewDialog
import ethicstechno.com.fieldforce.ui.adapter.CompanyAdapterForSearchViewDialog
import ethicstechno.com.fieldforce.ui.adapter.CountryAdapterForSearchViewDialog
import ethicstechno.com.fieldforce.ui.adapter.DivisionAdapterForSearchViewDialog
import ethicstechno.com.fieldforce.ui.adapter.ExpenseTypeAdapterForSearchViewDialog
import ethicstechno.com.fieldforce.ui.adapter.PartyDealerAdapterForSearchViewDialog
import ethicstechno.com.fieldforce.ui.adapter.PlaceAdapterForSearchViewDialog
import ethicstechno.com.fieldforce.ui.adapter.ShippingAddressAdapterForSearchViewDialog
import ethicstechno.com.fieldforce.ui.adapter.StateAdapterForSearchViewDialog
import ethicstechno.com.fieldforce.ui.adapter.UserAdapterForSearchViewDialog
import ethicstechno.com.fieldforce.ui.adapter.ZoneAdapterForSearchViewDialog
import ethicstechno.com.fieldforce.utils.DROP_DOWN_CURRENCY
import ethicstechno.com.fieldforce.utils.DROP_DOWN_FREIGHT_TERMS
import ethicstechno.com.fieldforce.utils.DROP_DOWN_INDUSTRY
import ethicstechno.com.fieldforce.utils.DROP_DOWN_REFERENCE_SOURCE
import ethicstechno.com.fieldforce.utils.DROP_DOWN_REGION
import ethicstechno.com.fieldforce.utils.DROP_DOWN_STAGE
import ethicstechno.com.fieldforce.utils.DROP_DOWN_TRANSACTION_TYPE
import ethicstechno.com.fieldforce.utils.DROP_DOWN_VISIT_TYPE
import ethicstechno.com.fieldforce.utils.FOR_BRANCH
import ethicstechno.com.fieldforce.utils.FOR_COMPANY
import ethicstechno.com.fieldforce.utils.FOR_COUNTRY
import ethicstechno.com.fieldforce.utils.FOR_CURRENCY_TYPE
import ethicstechno.com.fieldforce.utils.FOR_DIVISION
import ethicstechno.com.fieldforce.utils.FOR_EXPENSE_TYPE
import ethicstechno.com.fieldforce.utils.FOR_FREIGHT_TERMS
import ethicstechno.com.fieldforce.utils.FOR_INDUSTRY_TYPE
import ethicstechno.com.fieldforce.utils.FOR_PARTY_DEALER
import ethicstechno.com.fieldforce.utils.FOR_PLACE
import ethicstechno.com.fieldforce.utils.FOR_REFERENCE_SOURCE
import ethicstechno.com.fieldforce.utils.FOR_REGION_TYPE
import ethicstechno.com.fieldforce.utils.FOR_SHIPPING_ADDRESS
import ethicstechno.com.fieldforce.utils.FOR_STAGE
import ethicstechno.com.fieldforce.utils.FOR_STATE
import ethicstechno.com.fieldforce.utils.FOR_TRANSACTION_TYPE
import ethicstechno.com.fieldforce.utils.FOR_USER
import ethicstechno.com.fieldforce.utils.FOR_VISIT_TYPE
import ethicstechno.com.fieldforce.utils.FOR_ZONE

class UserSearchDialogUtil(
    private val activity: Activity,
    private val type: Int = FOR_USER,
    private var userList: ArrayList<UserListResponse> = arrayListOf(),
    private var placeList: ArrayList<ExpenseCityListResponse> = arrayListOf(),
    private var partyDealerList: ArrayList<AccountMasterList> = arrayListOf(),
    private var countryList: ArrayList<CountryListResponse> = arrayListOf(),
    private var stateList: ArrayList<StateListResponse> = arrayListOf(),
    private var zoneList: ArrayList<ZoneListResponse> = arrayListOf(),
    private var expenseTypeList: ArrayList<ExpenseTypeListResponse> = arrayListOf(),
    private var commonDropDownList: ArrayList<CommonDropDownResponse> = arrayListOf(),
    private var companyList: ArrayList<CompanyMasterResponse> = arrayListOf(),
    private var branchList: ArrayList<BranchMasterResponse> = arrayListOf(),
    private var divisionList: ArrayList<DivisionMasterResponse> = arrayListOf(),
    private var shippingList: ArrayList<ShippingAddressResponse> = arrayListOf(),

    userDialogInterfaceDetect: UserSearchDialogDetect? = null,
    placeDialogInterfaceDetect: PlaceSearchDialogDetect? = null,
    partyDealerInterfaceDetect: PartyDealerDialogDetect? = null,
    countryInterfaceDetect: CountrySearchDialogDetect? = null,
    stateInterfaceDetect: StateSearchDialogDetect? = null,
    zoneInterfaceDetect: ZoneSearchDialogDetect? = null,
    expenseTypeInterfaceDetect: ExpenseTypeDialogDetect? = null,
    commonDropDownInterfaceDetect: CommonDropDownDialogDetect? = null,
    companyInterfaceDetect: CompanyDialogDetect? = null,
    branchInterfaceDetect: BranchDialogDetect? = null,
    divisionInterfaceDetect: DivisionDialogDetect? = null,
    shippingAddressInterfaceDetect: ShippingAddressDialogDetect? = null,
) : UserAdapterForSearchViewDialog.UserItemClick,
    PlaceAdapterForSearchViewDialog.PlaceItemClick,
    PartyDealerAdapterForSearchViewDialog.PartyDealerItemClick,
    CountryAdapterForSearchViewDialog.CountryItemClick,
    StateAdapterForSearchViewDialog.StateItemClick,
    ZoneAdapterForSearchViewDialog.ZoneItemClick,
    ExpenseTypeAdapterForSearchViewDialog.ExpenseTypeItemClick,
    CommonDropDownAdapterNewForSearchViewDialog.CommonDropDownItemClick,
    CompanyAdapterForSearchViewDialog.CompanyItemClick,
    BranchAdapterForSearchViewDialog.BranchItemClick,
    DivisionAdapterForSearchViewDialog.DivisionItemClick,
    ShippingAddressAdapterForSearchViewDialog.ShippingAddressItemClick{
    private lateinit var userDialog: AlertDialog
    private var dialogInterfaceDetect: UserSearchDialogDetect? = userDialogInterfaceDetect
    private var placeDialogInterfaceDetect: PlaceSearchDialogDetect? = placeDialogInterfaceDetect
    private var partyDealerInterfaceDetect: PartyDealerDialogDetect? = partyDealerInterfaceDetect
    private var countryInterfaceDetect: CountrySearchDialogDetect? = countryInterfaceDetect
    private var stateInterfaceDetect: StateSearchDialogDetect? = stateInterfaceDetect
    private var zoneInterfaceDetect: ZoneSearchDialogDetect? = zoneInterfaceDetect
    private var expenseTypeInterfaceDetect: ExpenseTypeDialogDetect? = expenseTypeInterfaceDetect
    private var commonDropDownInterfaceDetect: CommonDropDownDialogDetect? = commonDropDownInterfaceDetect
    private var companyInterfaceDetect: CompanyDialogDetect? = companyInterfaceDetect
    private var branchInterfaceDetect: BranchDialogDetect? = branchInterfaceDetect
    private var divisionInterfaceDetect: DivisionDialogDetect? = divisionInterfaceDetect
    private var shippingAddressInterfaceDetect: ShippingAddressDialogDetect? = shippingAddressInterfaceDetect
    lateinit var userSearchViewAdapter: UserAdapterForSearchViewDialog
    lateinit var placeAdapterForSearchView: PlaceAdapterForSearchViewDialog
    lateinit var partyDealerAdapterForSearchViewDialog: PartyDealerAdapterForSearchViewDialog
    lateinit var countryAdapterForSearchViewDialog: CountryAdapterForSearchViewDialog
    lateinit var stateAdapterForSearchViewDialog: StateAdapterForSearchViewDialog
    lateinit var zoneAdapterForSearchViewDialog: ZoneAdapterForSearchViewDialog
    lateinit var expenseTypeAdapterForSearchViewDialog: ExpenseTypeAdapterForSearchViewDialog
    lateinit var commonDropDownAdapterNewForSearchViewDialog: CommonDropDownAdapterNewForSearchViewDialog
    lateinit var companyAdapterForSearchViewDialog: CompanyAdapterForSearchViewDialog
    lateinit var branchAdapterForSearchViewDialog: BranchAdapterForSearchViewDialog
    lateinit var divisionAdapterForSearchViewDialog: DivisionAdapterForSearchViewDialog
    lateinit var shippingAddressAdapterForSearchViewDialog: ShippingAddressAdapterForSearchViewDialog

    fun showUserSearchDialog() {
        try {
            val builder = AlertDialog.Builder(activity, R.style.MyAlertDialogStyle)
            userDialog = builder.create()
            userDialog.setCancelable(false)
            userDialog.setCanceledOnTouchOutside(false)
            userDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            userDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val inflater =
                activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout: View = inflater.inflate(R.layout.dialog_search_filter, null)
            val rvUser = layout.findViewById<RecyclerView>(R.id.rvUser)
            val imgClose = layout.findViewById<ImageView>(R.id.imgClose)
            val svView = layout.findViewById<SearchView>(R.id.svView)
            val tvTitle = layout.findViewById<TextView>(R.id.tvTitle)
            svView.setIconifiedByDefault(false)
            svView.isIconified = true
            when (type) {
                FOR_USER -> {
                    tvTitle.text = activity.getString(R.string.user_list)
                    userSearchViewAdapter = UserAdapterForSearchViewDialog(
                        userList,
                        this as UserAdapterForSearchViewDialog.UserItemClick
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = userSearchViewAdapter
                }
                FOR_PLACE -> {
                    tvTitle.text = activity.getString(R.string.place_list)
                    placeAdapterForSearchView = PlaceAdapterForSearchViewDialog(
                        placeList,
                        this as PlaceAdapterForSearchViewDialog.PlaceItemClick
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = placeAdapterForSearchView
                }
                FOR_PARTY_DEALER -> {
                    tvTitle.text = activity.getString(R.string.party_dealer_list)
                    partyDealerAdapterForSearchViewDialog = PartyDealerAdapterForSearchViewDialog(
                        partyDealerList,
                        this as PartyDealerAdapterForSearchViewDialog.PartyDealerItemClick
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = partyDealerAdapterForSearchViewDialog
                }
                FOR_COUNTRY -> {
                    tvTitle.text = activity.getString(R.string.country_list)
                    countryAdapterForSearchViewDialog = CountryAdapterForSearchViewDialog(
                        countryList,
                        this as CountryAdapterForSearchViewDialog.CountryItemClick
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = countryAdapterForSearchViewDialog
                }
                FOR_STATE -> {
                    tvTitle.text = activity.getString(R.string.state_list)
                    stateAdapterForSearchViewDialog = StateAdapterForSearchViewDialog(
                        stateList,
                        this as StateAdapterForSearchViewDialog.StateItemClick
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = stateAdapterForSearchViewDialog
                }
                FOR_ZONE -> {
                    tvTitle.text = activity.getString(R.string.zone_list)
                    zoneAdapterForSearchViewDialog = ZoneAdapterForSearchViewDialog(
                        zoneList,
                        this as ZoneAdapterForSearchViewDialog.ZoneItemClick
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = zoneAdapterForSearchViewDialog
                }
                FOR_EXPENSE_TYPE -> {
                    tvTitle.text = activity.getString(R.string.expense_type_list)
                    expenseTypeAdapterForSearchViewDialog = ExpenseTypeAdapterForSearchViewDialog(
                        expenseTypeList,
                        this as ExpenseTypeAdapterForSearchViewDialog.ExpenseTypeItemClick
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = expenseTypeAdapterForSearchViewDialog
                }
                FOR_REGION_TYPE -> {
                    tvTitle.text = activity.getString(R.string.region_list)
                    commonDropDownAdapterNewForSearchViewDialog = CommonDropDownAdapterNewForSearchViewDialog(
                        commonDropDownList,
                        this as CommonDropDownAdapterNewForSearchViewDialog.CommonDropDownItemClick,
                        DROP_DOWN_REGION
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = commonDropDownAdapterNewForSearchViewDialog
                }
                FOR_CURRENCY_TYPE -> {
                    tvTitle.text = activity.getString(R.string.currency_list)
                    commonDropDownAdapterNewForSearchViewDialog = CommonDropDownAdapterNewForSearchViewDialog(
                        commonDropDownList,
                        this as CommonDropDownAdapterNewForSearchViewDialog.CommonDropDownItemClick,
                        DROP_DOWN_CURRENCY
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = commonDropDownAdapterNewForSearchViewDialog
                }
                FOR_TRANSACTION_TYPE -> {
                    tvTitle.text = activity.getString(R.string.transaction_list)
                    commonDropDownAdapterNewForSearchViewDialog = CommonDropDownAdapterNewForSearchViewDialog(
                        commonDropDownList,
                        this as CommonDropDownAdapterNewForSearchViewDialog.CommonDropDownItemClick,
                        DROP_DOWN_TRANSACTION_TYPE
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = commonDropDownAdapterNewForSearchViewDialog
                }
                FOR_FREIGHT_TERMS -> {
                    tvTitle.text = activity.getString(R.string.freight_terms_list)
                    commonDropDownAdapterNewForSearchViewDialog = CommonDropDownAdapterNewForSearchViewDialog(
                        commonDropDownList,
                        this as CommonDropDownAdapterNewForSearchViewDialog.CommonDropDownItemClick,
                        DROP_DOWN_FREIGHT_TERMS
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = commonDropDownAdapterNewForSearchViewDialog
                }
                FOR_INDUSTRY_TYPE -> {
                    tvTitle.text = activity.getString(R.string.indstry_type_list)
                    commonDropDownAdapterNewForSearchViewDialog = CommonDropDownAdapterNewForSearchViewDialog(
                        commonDropDownList,
                        this as CommonDropDownAdapterNewForSearchViewDialog.CommonDropDownItemClick,
                        DROP_DOWN_INDUSTRY
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = commonDropDownAdapterNewForSearchViewDialog
                }
                FOR_REFERENCE_SOURCE -> {
                    tvTitle.text = activity.getString(R.string.reference_source_list)
                    commonDropDownAdapterNewForSearchViewDialog = CommonDropDownAdapterNewForSearchViewDialog(
                        commonDropDownList,
                        this as CommonDropDownAdapterNewForSearchViewDialog.CommonDropDownItemClick,
                        DROP_DOWN_REFERENCE_SOURCE
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = commonDropDownAdapterNewForSearchViewDialog
                }
                FOR_STAGE -> {
                    tvTitle.text = activity.getString(R.string.stage_list)
                    commonDropDownAdapterNewForSearchViewDialog = CommonDropDownAdapterNewForSearchViewDialog(
                        commonDropDownList,
                        this as CommonDropDownAdapterNewForSearchViewDialog.CommonDropDownItemClick,
                        DROP_DOWN_STAGE
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = commonDropDownAdapterNewForSearchViewDialog
                }
                FOR_VISIT_TYPE -> {
                    tvTitle.text = activity.getString(R.string.visit_type_list)
                    commonDropDownAdapterNewForSearchViewDialog = CommonDropDownAdapterNewForSearchViewDialog(
                        commonDropDownList,
                        this as CommonDropDownAdapterNewForSearchViewDialog.CommonDropDownItemClick,
                        DROP_DOWN_VISIT_TYPE
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = commonDropDownAdapterNewForSearchViewDialog
                }
                FOR_COMPANY -> {
                    tvTitle.text = activity.getString(R.string.company_list)
                    companyAdapterForSearchViewDialog = CompanyAdapterForSearchViewDialog(
                        companyList,
                        this as CompanyAdapterForSearchViewDialog.CompanyItemClick,
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = companyAdapterForSearchViewDialog
                }
                FOR_BRANCH -> {
                    tvTitle.text = activity.getString(R.string.branch_list)
                    branchAdapterForSearchViewDialog = BranchAdapterForSearchViewDialog(
                        branchList,
                        this as BranchAdapterForSearchViewDialog.BranchItemClick,
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = branchAdapterForSearchViewDialog
                }
                FOR_DIVISION -> {
                    tvTitle.text = activity.getString(R.string.division_list)
                    divisionAdapterForSearchViewDialog = DivisionAdapterForSearchViewDialog(
                        divisionList,
                        this as DivisionAdapterForSearchViewDialog.DivisionItemClick,
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = divisionAdapterForSearchViewDialog
                }
                FOR_SHIPPING_ADDRESS -> {
                    tvTitle.text = activity.getString(R.string.shipping_address_list)
                    shippingAddressAdapterForSearchViewDialog = ShippingAddressAdapterForSearchViewDialog(
                        shippingList,
                        this as ShippingAddressAdapterForSearchViewDialog.ShippingAddressItemClick,
                    )
                    rvUser.layoutManager = LinearLayoutManager(activity)
                    rvUser.adapter = shippingAddressAdapterForSearchViewDialog
                }
            }

            imgClose.setOnClickListener { userDialog.dismiss() }

            when (type) {
                FOR_USER -> {
                    svView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            Log.e("TAG", "onQueryTextChange: " + userList.size)
                            val filteredList = userList.filter { item ->
                                item.userName.contains(newText.orEmpty(), ignoreCase = true)
                            }
                            userSearchViewAdapter.refreshAdapter(
                                filteredList as ArrayList<UserListResponse>,
                            )
                            return true
                        }
                    })
                }
                FOR_PLACE -> {
                    svView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            Log.e("TAG", "onQueryTextChange: " + placeList.size)
                            val filteredList = placeList.filter { item ->
                                item.cityName.contains(newText.orEmpty(), ignoreCase = true)
                            }
                            placeAdapterForSearchView.refreshAdapter(
                                filteredList as ArrayList<ExpenseCityListResponse>,
                            )
                            return true
                        }
                    })
                }
                FOR_PARTY_DEALER -> {
                    svView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            Log.e("TAG", "onQueryTextChange: " + partyDealerList.size)
                            val filteredList = partyDealerList.filter { item ->
                                item.accountName.contains(newText.orEmpty(), ignoreCase = true)
                            }
                            partyDealerAdapterForSearchViewDialog.refreshAdapter(
                                filteredList as ArrayList<AccountMasterList>,
                            )
                            return true
                        }
                    })
                }
                FOR_COUNTRY -> {
                    svView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            Log.e("TAG", "onQueryTextChange: " + countryList.size)
                            val filteredList = countryList.filter { item ->
                                item.countryName.contains(newText.orEmpty(), ignoreCase = true)
                            }
                            countryAdapterForSearchViewDialog.refreshAdapter(
                                filteredList as ArrayList<CountryListResponse>,
                            )
                            return true
                        }
                    })
                }
                FOR_STATE -> {
                    svView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            Log.e("TAG", "onQueryTextChange: " + stateList.size)
                            val filteredList = stateList.filter { item ->
                                item.stateName.contains(newText.orEmpty(), ignoreCase = true)
                            }
                            stateAdapterForSearchViewDialog.refreshAdapter(
                                filteredList as ArrayList<StateListResponse>,
                            )
                            return true
                        }
                    })
                }
                FOR_ZONE -> {
                    svView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            Log.e("TAG", "onQueryTextChange: " + zoneList.size)
                            val filteredList = zoneList.filter { item ->
                                item.zoneName.contains(newText.orEmpty(), ignoreCase = true)
                            }
                            zoneAdapterForSearchViewDialog.refreshAdapter(
                                filteredList as ArrayList<ZoneListResponse>,
                            )
                            return true
                        }
                    })
                }
                FOR_EXPENSE_TYPE -> {
                    svView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            Log.e("TAG", "onQueryTextChange: " + expenseTypeList.size)
                            val filteredList = expenseTypeList.filter { item ->
                                item.expenseTypeName.contains(newText.orEmpty(), ignoreCase = true)
                            }
                            expenseTypeAdapterForSearchViewDialog.refreshAdapter(
                                filteredList as ArrayList<ExpenseTypeListResponse>,
                            )
                            return true
                        }
                    })
                }
                FOR_COMPANY -> {
                    svView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            val filteredList = companyList.filter { item ->
                                (item.companyName ?: "").contains(newText.orEmpty(), ignoreCase = true)
                            }
                            companyAdapterForSearchViewDialog.refreshAdapter(
                                filteredList as ArrayList<CompanyMasterResponse>,
                            )
                            return true
                        }
                    })
                }
                FOR_BRANCH -> {
                    svView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            val filteredList = branchList.filter { item ->
                                (item.branchName ?: "").contains(newText.orEmpty(), ignoreCase = true)
                            }
                            branchAdapterForSearchViewDialog.refreshAdapter(
                                filteredList as ArrayList<BranchMasterResponse>,
                            )
                            return true
                        }
                    })
                }
                FOR_DIVISION -> {
                    svView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            val filteredList = divisionList.filter { item ->
                                (item.divisionName ?: "").contains(newText.orEmpty(), ignoreCase = true)
                            }
                            divisionAdapterForSearchViewDialog.refreshAdapter(
                                filteredList as ArrayList<DivisionMasterResponse>,
                            )
                            return true
                        }
                    })
                }
                FOR_REGION_TYPE, FOR_INDUSTRY_TYPE, FOR_REFERENCE_SOURCE, FOR_STAGE, FOR_VISIT_TYPE, FOR_CURRENCY_TYPE, FOR_TRANSACTION_TYPE, FOR_FREIGHT_TERMS -> {
                    svView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            Log.e("TAG", "onQueryTextChange: " + commonDropDownList.size)
                            val filteredList = commonDropDownList.filter { item ->
                                (item.dropdownValue ?: "").contains(newText.orEmpty(), ignoreCase = true)
                            }
                            commonDropDownAdapterNewForSearchViewDialog.refreshAdapter(
                                filteredList as ArrayList<CommonDropDownResponse>,
                            )
                            return true
                        }
                    })
                }
            }

            userDialog.setView(layout, 0, 0, 0, 0)
            userDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_shape)
            userDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    interface UserSearchDialogDetect {
        fun userSelect(userData: UserListResponse)
    }

    interface PlaceSearchDialogDetect {
        fun placeSelect(userData: ExpenseCityListResponse)
    }

    interface PartyDealerDialogDetect {
        fun partyDealerSelect(partyDealerData: AccountMasterList)
    }

    interface CountrySearchDialogDetect {
        fun countrySelect(countryData: CountryListResponse)
    }

    interface StateSearchDialogDetect {
        fun stateSelect(countryData: StateListResponse)
    }

    interface ZoneSearchDialogDetect {
        fun zoneSelect(zoneData: ZoneListResponse)
    }

    interface ExpenseTypeDialogDetect {
        fun expenseSelect(expenseTypeData: ExpenseTypeListResponse)
    }

    interface CommonDropDownDialogDetect {
        fun dropDownSelect(dropDownData: CommonDropDownResponse, dropDownType:String)
    }

    interface CompanyDialogDetect {
        fun companySelect(dropDownData: CompanyMasterResponse)
    }
    interface BranchDialogDetect {
        fun branchSelect(dropDownData: BranchMasterResponse)
    }

    interface DivisionDialogDetect {
        fun divisionSelect(dropDownData: DivisionMasterResponse)
    }

    interface ShippingAddressDialogDetect {
        fun shippingAddressSelect(shippingAddress: ShippingAddressResponse)
    }

    override fun onUserOnClick(userData: UserListResponse) {
        userDialog.dismiss()
        dialogInterfaceDetect?.userSelect(userData)
    }

    override fun onPlaceClick(placeData: ExpenseCityListResponse) {
        userDialog.dismiss()
        placeDialogInterfaceDetect?.placeSelect(placeData)
    }

    override fun onPartyDealerClick(partyDealerData: AccountMasterList) {
        userDialog.dismiss()
        partyDealerInterfaceDetect?.partyDealerSelect(partyDealerData)
    }

    override fun onCountryClick(countryData: CountryListResponse) {
        userDialog.dismiss()
        countryInterfaceDetect?.countrySelect(countryData)
    }

    override fun onStateClick(stateData: StateListResponse) {
        userDialog.dismiss()
        stateInterfaceDetect?.stateSelect(stateData)
    }

    override fun onZoneClick(zoneData: ZoneListResponse) {
        userDialog.dismiss()
        zoneInterfaceDetect?.zoneSelect(zoneData)
    }

    override fun onZoneClick(expenseTypeData: ExpenseTypeListResponse) {
        userDialog.dismiss()
        expenseTypeInterfaceDetect?.expenseSelect(expenseTypeData)
    }

    override fun onDropDownItemClick(dropDownData: CommonDropDownResponse, dropDownType: String) {
        userDialog.dismiss()
        commonDropDownInterfaceDetect?.dropDownSelect(dropDownData, dropDownType)
    }

    override fun onBranchItemClick(branchResponse: BranchMasterResponse) {
        userDialog.dismiss()
        branchInterfaceDetect?.branchSelect(branchResponse)
    }

    override fun onCompanyItemClick(companyResponse: CompanyMasterResponse) {
        userDialog.dismiss()
        companyInterfaceDetect?.companySelect(companyResponse)
    }

    override fun onDivisionItemClick(divisionResponse: DivisionMasterResponse) {
        userDialog.dismiss()
        divisionInterfaceDetect?.divisionSelect(divisionResponse)
    }

    override fun onShippingAddressItemClick(shippingAddressResponse: ShippingAddressResponse) {
        userDialog.dismiss()
        shippingAddressInterfaceDetect?.shippingAddressSelect(shippingAddressResponse)
    }
}