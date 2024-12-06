package ethicstechno.com.fieldforce.ui.fragments.loginsignup

import AnimationType
import addFragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import ethicstechno.com.fieldforce.R
import ethicstechno.com.fieldforce.databinding.FragmentForgotPwdBinding
import ethicstechno.com.fieldforce.ui.base.MainBaseFragment

class ForgotPwdFragment : MainBaseFragment() {

    //private var prefREM: SharedPreferences? = null
    lateinit var binding: FragmentForgotPwdBinding
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.e("TAG", "onAttach: " )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("TAG", "onCreate: " )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  DataBindingUtil.inflate(inflater,R.layout.fragment_forgot_pwd, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("TAG", "onViewCreated: " )


    }



}