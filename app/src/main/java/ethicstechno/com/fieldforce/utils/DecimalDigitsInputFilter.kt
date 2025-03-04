package ethicstechno.com.fieldforce.utils

/*
class DecimalDigitsInputFilter(private val digitsBeforeZero: Int, private val digitsAfterZero: Int, private val tempEditText: EditText) : InputFilter {
    private var mPattern: Pattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?")
    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence {
        val matcher = mPattern.matcher(dest)
        if (!matcher.matches()) {
            if (dest.toString().contains(".")) {
                val cursorPosition: Int = tempEditText.selectionStart
                val dotPosition: Int = if (dest.toString().indexOf(".") == -1) {
                    dest.toString().indexOf(".")
                } else {
                    dest.toString().indexOf(".")
                }
                if (cursorPosition <= dotPosition) {
                    val beforeDot = dest.toString().substring(0, dotPosition)
                    return if (beforeDot.length < digitsBeforeZero) {
                        source
                    } else {
                        if (source.toString().equals(".", true)) {
                            source
                        } else {
                            ""
                        }
                    }
                } else {
                    if (dest.toString().substring(dest.toString().indexOf(".")).length > digitsAfterZero) {
                        return ""
                    }
                }
            } else if (!Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}").matcher(dest).matches()) {
                if (!dest.toString().contains(".")) {
                    if (source.toString().equals(".", true)) {
                        return source
                    }
                }
                return ""
            } else {
                return source
            }
        }
        return source
    }
}*/

import android.text.InputFilter
import android.text.Spanned
import android.widget.EditText
import java.util.regex.Pattern

class DecimalDigitsInputFilter(
    private val digitsBeforeZero: Int,
    private val digitsAfterZero: Int,
    private val tempEditText: EditText
) : InputFilter {

    private val mPattern: Pattern = if (digitsAfterZero > 0) {
        // Allows digits before and after the decimal point
        Pattern.compile("[0-9]{0,${digitsBeforeZero}}+((\\.[0-9]{0,${digitsAfterZero}})?)?")
    } else {
        // Only allows whole numbers (no decimal point)
        Pattern.compile("[0-9]{0,${digitsBeforeZero}}")
    }

    override fun filter(
        source: CharSequence, start: Int, end: Int,
        dest: Spanned, dstart: Int, dend: Int
    ): CharSequence {
        val newInput = dest.subSequence(0, dstart).toString() +
                source.subSequence(start, end) +
                dest.subSequence(dend, dest.length).toString()

        // If input doesn't match the pattern, reject it
        if (!mPattern.matcher(newInput).matches()) {
            return ""
        }

        // Prevent multiple dots if decimals are allowed
        if (digitsAfterZero > 0 && source == "." && dest.contains(".")) {
            return ""
        }

        return source
    }
}

class DecimalDigitsInputFilterWithoutEditText(
    private val digitsBeforeZero: Int,
    private val digitsAfterZero: Int
) : InputFilter {

    override fun filter(
        source: CharSequence, start: Int, end: Int,
        dest: Spanned, dstart: Int, dend: Int
    ): CharSequence? {
        val newText = dest.subSequence(0, dstart).toString() +
                source.subSequence(start, end) +
                dest.subSequence(dend, dest.length).toString()

        val pattern = if (digitsAfterZero > 0) {
            Pattern.compile("^\\d{0,$digitsBeforeZero}(\\.\\d{0,$digitsAfterZero})?$")
        } else {
            Pattern.compile("^\\d{0,$digitsBeforeZero}$") // Restrict to whole numbers
        }

        return if (pattern.matcher(newText).matches()) source else ""
    }
}
