package de.tum.in.tumcampusapp.component.ui.transportation;

import android.graphics.Color;

import com.google.common.base.Optional;
import com.google.common.primitives.Ints;

/**
 * Encapsulates information about the symbol shown next to departure information. Contains the
 * background color and text color, as computed based on the line type and line number.
 */
public class MVVSymbol {

    private static final int[] S_LINE_COLOR = {
            0xff6db9df, 0xff8db335, 0xff7d187b, 0xffc1003c, 0, 0xff00915e, 0xff7f3229, 0xff1f1e21
    };

    private static final int[] U_LINE_COLOR = {
            0xff44723c, 0xffcc263c, 0xffe4721c, 0xff04a27c, 0xffa4721c, 0xff045ea4, 0xffc10134, 0xffeb6a27
    };

    private static final int U_LINE_DEFAULT_COLOR = Color.GRAY;

    private final int mBackgroundColor;
    private final int mTextColor;

    /**
     * Standard constructor
     *
     * @param line Line symbol name e.g. U6, S1, T14
     */
    public MVVSymbol(String line) {
        int textColor = 0xffffffff;
        int backgroundColor = 0;

        char symbol = !line.isEmpty() ? line.charAt(0) : 'X';
        Integer parsedNumber = Ints.tryParse(line.substring(1));
        int lineNumber = Optional.fromNullable(parsedNumber).or(0);

        switch (symbol) {
            case 'S':
                if (lineNumber > 0 && lineNumber <= 8) {
                    backgroundColor = S_LINE_COLOR[lineNumber - 1];
                } else if (lineNumber == 20) {
                    backgroundColor = 0xffca536a;
                } else if (lineNumber == 27) {
                    backgroundColor = 0xffd99098;
                }
                textColor = lineNumber == 8 ? 0xfff1cb00 : 0xffffffff;
                break;
            case 'U':
                if (lineNumber > 0 && lineNumber <= U_LINE_COLOR.length) {
                    backgroundColor = U_LINE_COLOR[lineNumber - 1];
                } else {
                    backgroundColor = U_LINE_DEFAULT_COLOR;
                }
                textColor = 0xfffcfefc;
                break;
            case 'N':
                backgroundColor = 0xff000000;
                textColor = 0xffebd22e;
                break;
            case 'X':
                backgroundColor = 0xff477f70;
                break;
            default:
                if (lineNumber < 50) {
                    backgroundColor = 0xffdc261c;
                    textColor = 0xfffcfefc;
                } else if (lineNumber < 90) {
                    backgroundColor = 0xfffc6604;
                } else {
                    backgroundColor = 0xff004a5d;
                }
                break;
        }

        mTextColor = textColor;
        mBackgroundColor = backgroundColor;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

}
