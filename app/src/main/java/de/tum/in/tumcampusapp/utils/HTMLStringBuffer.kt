package de.tum.`in`.tumcampusapp.utils

/**
 * StringBuffer wrapper class to format text in a customized way.
 */
class HTMLStringBuffer {

    private val buffer: StringBuffer = StringBuffer()

    /**
     * Append text to the buffer.
     *
     * @param s The text.
     */
    fun append(s: String) = buffer.append(s)

    /**
     * Append a new label-content pair to the text. Format label as being bold.
     *
     * @param label   The label labeling the content.
     * @param content The content labeled by the label.
     */
    fun appendField(label: String, content: String?) {
        if (content != null && !content.isEmpty()) {
            appendLine("<b>$label</b>: $content")
        }
    }

    /**
     * Append new line broken by "br".
     *
     * @param string Last string of the line.
     */
    private fun appendLine(string: String) {
        val s = string.replace("null", "")

        append(s)
        append(NEW_LINE)
    }

    /**
     * Reset the buffer.
     */
    fun clear() = buffer.setLength(0)

    override fun toString(): String = buffer.toString()

    companion object {
        private const val NEW_LINE = "<br />"
    }
}
