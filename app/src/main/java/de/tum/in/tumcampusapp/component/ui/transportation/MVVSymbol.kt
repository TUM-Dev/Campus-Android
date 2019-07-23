package de.tum.`in`.tumcampusapp.component.ui.transportation

import android.content.Context
import de.tum.`in`.tumcampusapp.R

/**
 * Encapsulates information about the symbol shown next to departure information. Contains the
 * background color and text color, as computed based on the line type and line number.
 *
 * @param line Line symbol name e.g. U6, S1, T14
 */
class MVVSymbol(line: String, val context: Context) {

    val backgroundColor: Int
    val textColor: Int

    init {
        var textColor = R.color.white
        val backgroundColor: Int

        val symbol = line.getOrNull(0) ?: 'X'
        val lineNumber = line.substring(1).toIntOrNull() ?: 0

        when (symbol) {
            'S' -> {
                backgroundColor = when (lineNumber) {
                    in 1..8 -> S_LINE_COLOR[lineNumber - 1]
                    20 -> R.color.s_20
                    else -> R.color.unknown_line
                }
                textColor = if (lineNumber == 8) R.color.s_8_font else R.color.white
            }
            'U' -> {
                backgroundColor = if (lineNumber > 0 && lineNumber <= U_LINE_COLOR.size) {
                    U_LINE_COLOR[lineNumber - 1]
                } else {
                    R.color.unknown_line
                }
                textColor = when (lineNumber) {
                    7 -> R.color.u_7_accent
                    8 -> R.color.u_8_accent
                    else -> R.color.white
                }
            }
            'N' -> {
                backgroundColor = R.color.black
                textColor = R.color.night_line_font
            }
            'X' -> backgroundColor = R.color.express_bus
            else -> backgroundColor = when {
                lineNumber < 50 -> R.color.tram
                lineNumber < 90 -> R.color.metro_bus
                else -> R.color.regular_bus
            }
        }

        this.textColor = context.resources.getColor(textColor)
        this.backgroundColor = context.resources.getColor(backgroundColor)
    }

    fun getHighlight(): Int {
        return context.resources.getColor(R.color.reduced_opacity) and backgroundColor
    }

    companion object {
        private val S_LINE_COLOR = intArrayOf(
                R.color.s_1,
                R.color.s_2,
                R.color.s_3,
                R.color.s_4,
                R.color.unknown_line,
                R.color.s_6,
                R.color.s_7,
                R.color.s_8)

        private val U_LINE_COLOR = intArrayOf(
                R.color.u_1,
                R.color.u_2,
                R.color.u_3,
                R.color.u_4,
                R.color.u_5,
                R.color.u_6,
                R.color.u_7,
                R.color.u_8)
    }
}
