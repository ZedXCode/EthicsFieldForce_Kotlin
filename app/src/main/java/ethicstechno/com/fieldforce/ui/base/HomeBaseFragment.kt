package ethicstechno.com.fieldforce.ui.base


import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import ethicstechno.com.fieldforce.db.AppDatabase
import ethicstechno.com.fieldforce.db.dao.AppDao
import ethicstechno.com.fieldforce.listener.PositiveButtonListener
import ethicstechno.com.fieldforce.models.LoginResponse
import ethicstechno.com.fieldforce.ui.activities.HomeActivity
import ethicstechno.com.fieldforce.utils.CommonMethods
import retrofit2.Response

open class HomeBaseFragment : Fragment() {

    lateinit var mActivity: HomeActivity
    lateinit var appDatabase: AppDatabase
    lateinit var appDao: AppDao
    lateinit var loginData: LoginResponse

    //var loginData: LoginResult = LoginResult()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*mActivity = activity as HomeActivity
        appDatabase = AppDatabase.getDatabase(mActivity)
        appDao = appDatabase.appDao()
        loginData = appDao.getLoginData()*/
        //loginData = appDatabase.loginDao().getLogin()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is HomeActivity) {
            mActivity = context
            appDatabase = AppDatabase.getDatabase(mActivity)
            appDao = appDatabase.appDao()
            loginData = appDao.getLoginData()
        }
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

    open fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        TODO("Not yet implemented")
    }
}
