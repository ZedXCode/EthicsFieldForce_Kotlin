package ethicstechno.com.fieldforce.ui.base

import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import ethicstechno.com.fieldforce.db.AppDatabase
import ethicstechno.com.fieldforce.db.dao.AppDao
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.LoginResponse
import ethicstechno.com.fieldforce.ui.activities.HomeActivity
import ethicstechno.com.fieldforce.utils.CommonMethods
import retrofit2.Response

open class HomeBaseDialogFragment :DialogFragment() {

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    lateinit var mActivity: HomeActivity
    lateinit var appDatabase: AppDatabase
    lateinit var mResources: Resources
    lateinit var appDao: AppDao
    lateinit var loginData: LoginResponse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity as HomeActivity
        mResources = mActivity.resources
        appDatabase = AppDatabase.getDatabase(mActivity)
        appDao = appDatabase.appDao()
        loginData = appDao.getLoginData()
    }

    open fun isSuccess(response: Response<*>): Boolean {
        return when {
            response.code() == 200 -> {

                true
            }
            response.code() == 500 -> {
                CommonMethods.showAlertDialog(
                    mActivity,
                    "SERVER ERROR",
                    response.message(),
                    object : PositiveButtonListener {
                        override fun okClickListener() {}
                    })
                false
            }
            response.code() == 401 -> {
                //callLogoutApi()
                false
            }
            else -> {
                false
            }
        }
    }


}