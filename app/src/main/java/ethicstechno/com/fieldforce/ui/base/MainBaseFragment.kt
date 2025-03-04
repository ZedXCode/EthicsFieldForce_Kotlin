package ethicstechno.com.fieldforce.ui.base


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import ethicstechno.com.fieldforce.MainActivity
import ethicstechno.com.fieldforce.db.AppDatabase
import retrofit2.Response

open class MainBaseFragment : Fragment() {

    lateinit var mActivity: MainActivity
    lateinit var appDatabase: AppDatabase

    //var loginData: LoginResult = LoginResult()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //mActivity = activity as MainActivity
        //appDatabase = AppDatabase.getDatabase(mActivity)
        //loginData = appDatabase.loginDao().getLogin()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mActivity = context
            appDatabase = AppDatabase.getDatabase(mActivity)
        }
    }

    open fun isSuccess(response: Response<*>): Boolean {
        return when {
            response.code() === 200 -> {
                true
            }

            response.code() === 401 -> {
                //callLogoutApi()
                false
            }

            else -> {
                false
            }
        }
    }

    fun gotoActivity(className: Class<*>?, bundle: Bundle?, isClearStack: Boolean) {
        val intent = Intent(context, className)
        if (bundle != null) intent.putExtras(bundle)
        if (isClearStack) {
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        mActivity.startActivity(intent)
    }
}
