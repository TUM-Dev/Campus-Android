package de.tum.`in`.tumcampusapp.component.ui.transportation

import android.graphics.Color

/**
 * Encapsulates information about the symbol shown next to departure information. Contains the
 * background color and text color, as computed based on the line type and line number.
 *
 * @param line Line symbol name e.g. U6, S1, T14
 */
class MVVSymbol(line: String) {

    val backgroundColor: Int
    val textColor: Int

    init {
        var textColor = -0x1
        var backgroundColor = 0

        val symbol = line.getOrNull(0) ?: 'X'
        val lineNumber = line.substring(1).toIntOrNull() ?: 0

        when (symbol) {
            'S' -> {
                when (lineNumber) {
                    in 1..8 -> backgroundColor = S_LINE_COLOR[lineNumber - 1]
                    20 -> backgroundColor = -0x35ac96
                    27 -> backgroundColor = -0x266f68
                }
                textColor = if (lineNumber == 8) -0xe3500 else -0x1
            }
            'U' -> {
                backgroundColor = if (lineNumber > 0 && lineNumber <= U_LINE_COLOR.size) {
                    U_LINE_COLOR[lineNumber - 1]
                } else {
                    U_LINE_DEFAULT_COLOR
                }
                textColor = -0x30104
            }
            'N' -> {
                backgroundColor = -0x1000000
                textColor = -0x142dd2
            }
            'X' -> backgroundColor = -0xb88090
            else -> when {
                lineNumber < 50 -> {
                    backgroundColor = -0x23d9e4
                    textColor = -0x30104
                }
                lineNumber < 90 -> backgroundColor = -0x399fc
                else -> backgroundColor = -0xffb5a3
            }
        }

        this.textColor = textColor
        this.backgroundColor = backgroundColor
    }

    companion object {
        private val S_LINE_COLOR = intArrayOf(-0x924621, -0x724ccb, -0x82e785, -0x3effc4, 0, -0xff6ea2, -0x80cdd7, -0xe0e1df)

        private val U_LINE_COLOR = intArrayOf(-0xbb8dc4, -0x33d9c4, -0x1b8de4, -0xfb5d84, -0x5b8de4, -0xfba15c, -0x3efecc, -0x1495d9)

        private const val U_LINE_DEFAULT_COLOR = Color.GRAY
    }
}
