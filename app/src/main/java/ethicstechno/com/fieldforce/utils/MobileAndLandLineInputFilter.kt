package ethicstechno.com.fieldforce.utils

import android.text.InputFilter
import android.text.Spanned

class MobileAndLandLineInputFilter : InputFilter {

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val input = dest.toString() + source.toString()

        // Allow only numbers and a single dash
        return if (input.matches(Regex("^\\d*-?\\d*$")) && input.count { it == '-' } <= 1) {
            null // Accept input
        } else {
            "" // Reject input
        }
    }

}