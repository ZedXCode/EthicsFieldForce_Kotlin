package ethicstechno.com.fieldforce.ui.base


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
        mActivity = activity as MainActivity
        appDatabase = AppDatabase.getDatabase(mActivity)
        //loginData = appDatabase.loginDao().getLogin()
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

    /*open fun callLogoutApi() {
        if (!ConnectionUtil.isInternetAvailable(mActivity)) {
            Utils.showNoInternetMessage(mActivity)
            return
        }

        mActivity.isSpeedAvailable()
        val jsonObject = JsonObject()
        jsonObject.addProperty("user_id", loginData.user_id)
        Log.e("TAG", "callLogoutApi: $jsonObject" )
        val logoutCall = WebApiClient.getInstance(mActivity).webApi_without()
            ?.logOutApi(jsonObject)
        logoutCall?.enqueue(object : retrofit2.Callback<ApiResponseBean> {
            override fun onResponse(
                call: Call<ApiResponseBean>,
                response: Response<ApiResponseBean>
            ) {
                Common_Methods.hideLoading()
                when {
                    response.code() == 200 -> {

                        response.body().let { loginResult ->
                            if (loginResult?.code == 200){
                                Common_Methods.showToast(mActivity, loginResult.message)
                                appDatabase.loginDao().dropLogin()
                                val intent = Intent(mActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }
                        }
                    }
                    else -> {
                        Common_Methods.hideLoading()
                        Toast.makeText(mActivity, "Something went wrong", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponseBean>, t: Throwable) {
                Common_Methods.hideLoading()
                Log.e("TAG", "onFailure: ${t.message}")
            }

        })
    }*/

    fun gotoActivity(className: Class<*>?, bundle: Bundle?, isClearStack: Boolean) {
        val intent = Intent(context, className)
        if (bundle != null) intent.putExtras(bundle)
        if (isClearStack) {
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        mActivity.startActivity(intent)
    }
}
