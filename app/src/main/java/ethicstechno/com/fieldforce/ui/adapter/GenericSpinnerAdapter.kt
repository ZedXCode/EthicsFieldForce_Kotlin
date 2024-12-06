package ethicstechno.com.fieldforce.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import ethicstechno.com.fieldforce.R


class GenericSpinnerAdapter<T>(
    context: Context,
    private val items: List<T>,
    private val displayText: (T) -> String
) : ArrayAdapter<T>(
    context,
    android.R.layout.simple_spinner_item,
    items
) {

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        //or to support all versions use
        //or to support all versions use
        val typeface = ResourcesCompat.getFont(context, R.font.nunito_sans_regular)
        textView.typeface = typeface
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,15F);
        textView.text = displayText(items[position])
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = displayText(items[position])
        val typeface = ResourcesCompat.getFont(context, R.font.nunito_sans_regular)
        textView.typeface = typeface
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,15F);
        return view
    }
}
