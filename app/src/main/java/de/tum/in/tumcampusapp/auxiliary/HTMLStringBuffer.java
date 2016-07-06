package de.tum.in.tumcampusapp.auxiliary;

/**
 * StringBuffer wrapper class to format text in a customized way.
 */
public class HTMLStringBuffer {

    private static final String NEW_LINE = "<br />";

    private StringBuffer buffer;

    /**
     * Creates a new buffer.
     */
    public HTMLStringBuffer() {
        clear();
    }

    /**
     * Append text to the buffer.
     *
     * @param s The text.
     */
    public void append(String s) {
        buffer.append(s);
    }

    /**
     * Append a new label-content pair to the text. Format label as being bold.
     *
     * @param label   The label labeling the content.
     * @param content The content labeled by the label.
     */
    public void appendField(String label, String content) {
        if (content != null && !content.isEmpty()) {
            appendLine("<b>" + label + "</b>: " + content);
        }
    }

    /**
     * Append new line broken by "br".
     *
     * @param string Last string of the line.
     */
    void appendLine(String string) {
        String s = string.replace("null", "");

        append(s);
        append(NEW_LINE);
    }

    /**
     * Reset the buffer.
     */
    public final void clear() {
        buffer = new StringBuffer();
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

}
