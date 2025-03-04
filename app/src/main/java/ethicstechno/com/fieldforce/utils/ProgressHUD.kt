package ethicstechno.com.fieldforce.utils

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.view.Gravity
import android.widget.ImageView
import android.widget.ProgressBar
import ethicstechno.com.fieldforce.R

open class ProgressHUD : Dialog {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, theme: Int) : super(context, theme) {}

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        val imageView = findViewById<ImageView>(R.id.spinnerImageView)
        val spinner = imageView
            .background as AnimationDrawable
        spinner.start()
    }

    fun setMessage(message: CharSequence?) {
        if (message != null && message.length > 0) {
            //  findViewById(R.id.message).setVisibility(View.VISIBLE);
            // TextView txt = (TextView) findViewById(R.id.message);
            //txt.setText(message);
            //txt.invalidate();
        }
    }

    companion object {

        private var dialog: ProgressHUD? = null

        fun show(
            context: Context, message: CharSequence?,
            indeterminate: Boolean, cancelable: Boolean
        ): ProgressHUD {

            dialog = ProgressHUD(context, R.style.NewDialog)
            dialog!!.setTitle("")
            dialog!!.setContentView(R.layout.progress_hud)

            val currentapiVersion = Build.VERSION.SDK_INT
            if (currentapiVersion < Build.VERSION_CODES.LOLLIPOP) {
                dialog!!.findViewById<ProgressBar>(R.id.pb_load).background =
                    context.resources.getDrawable(R.drawable.custom_progress_bar)
            }

            if (message == null || message.isEmpty()) {
                //dialog.findViewById(R.id.message).setVisibility(View.GONE);
            } else {
                //TextView txt = (TextView) dialog.findViewById(R.id.message);
                //txt.setText(message);
            }
            dialog!!.setCancelable(cancelable)
//            dialog!!.setOnCancelListener(cancelListener)
            dialog!!.window!!.attributes.gravity = Gravity.CENTER

            val lp = dialog!!.window!!.attributes
            lp.dimAmount = 0.2f
            dialog!!.window!!.attributes = lp
            // dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            dialog!!.show()
            return dialog as ProgressHUD
        }

        fun dialogDismiss() {
            dialog!!.dismiss()
        }
    }

}
