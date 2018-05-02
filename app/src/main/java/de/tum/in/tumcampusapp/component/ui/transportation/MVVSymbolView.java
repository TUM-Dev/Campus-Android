package de.tum.in.tumcampusapp.component.ui.transportation;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.google.common.base.Optional;
import com.google.common.primitives.Ints;

/**
 * Subclass of drawable that can draw a subway line icon
 */
public class MVVSymbolView extends Drawable {
    private static final int[] S_LINE_COLOR = {
            0xff6db9df, 0xff8db335, 0xff7d187b, 0xffc1003c, 0, 0xff00915e, 0xff7f3229, 0xff1f1e21
    };
    private static final int[] U_LINE_COLOR = {
            0xff44723c, 0xffcc263c, 0xffe4721c, 0xff04a27c, 0xffa4721c, 0xff045ea4
    };
    private final Paint mBgPaint;
    private final RectF mRect;
    private final int mTriangle;
    private final boolean mRounded;

    private final int mTextColor;

    /**
     * Standard constructor
     *
     * @param line Line symbol name e.g. U6, S1, T14
     */
    public MVVSymbolView(String line) {
        mRect = new RectF();
        boolean rounded = false;
        int textColor = 0xffffffff;
        int triangle = 0;
        int backgroundColor = 0;
        int num;

        switch (line.charAt(0)) {
            case 'S':
                num = Optional.fromNullable(
                        Ints.tryParse(line.substring(1)))
                              .or(0);
                rounded = true;
                if (num <= 8) {
                    backgroundColor = S_LINE_COLOR[num - 1];
                } else if (num == 20) {
                    backgroundColor = 0xffca536a;
                } else if (num == 27) {
                    backgroundColor = 0xffd99098;
                }
                textColor = num == 8 ? 0xfff1cb00 : 0xffffffff;
                break;
            case 'U':
                num = Optional.fromNullable(
                        Ints.tryParse(line.substring(1)))
                              .or(0);
                if (num == 7) {
                    triangle = 1;
                    backgroundColor = 0xffc10134;
                } else if (num == 8) {
                    triangle = 2;
                    backgroundColor = 0xffeb6a27;
                } else {
                    backgroundColor = U_LINE_COLOR[num - 1];
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
                num = Optional.fromNullable(
                        Ints.tryParse(line.substring(1)))
                              .or(0);
                if (num < 50) {
                    backgroundColor = 0xffdc261c;
                    textColor = 0xfffcfefc;
                } else if (num < 90) {
                    backgroundColor = 0xfffc6604;
                } else {
                    backgroundColor = 0xff004a5d;
                }
                break;
        }
        mBgPaint = new Paint();
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setColor(backgroundColor);

        mTriangle = triangle;
        mRounded = rounded;
        mTextColor = textColor;
    }

    /**
     * Gets the value color that should be used to draw value on top of this drawable
     *
     * @return Text color
     */
    public int getTextColor() {
        return mTextColor;
    }

    public int getBackgroundColor() {
        return mBgPaint.getColor();
    }

    @Override
    public void draw(Canvas canvas) {
        final int width = canvas.getWidth();
        final int height = canvas.getHeight();
        mRect.top = 0;
        mRect.bottom = height;
        mRect.left = 0;
        mRect.right = width;
        if (mRounded) {
            canvas.drawRoundRect(mRect, height / 2.0f, height / 2.0f, mBgPaint);
        } else if (mTriangle == 1) {
            drawTriangle(canvas, 0xff51812e);
        } else if (mTriangle == 2) {
            drawTriangle(canvas, 0xffc10134);
        } else {
            canvas.drawRect(mRect, mBgPaint);
        }
    }

    private void drawTriangle(Canvas canvas, int color) {
        canvas.drawRect(mRect, mBgPaint);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        Point point1 = new Point(0, 0);
        Point point2 = new Point((int) mRect.right, 0);
        Point point3 = new Point(0, (int) mRect.bottom);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(point1.x, point1.y);
        path.lineTo(point2.x, point2.y);
        path.lineTo(point3.x, point3.y);
        path.lineTo(point1.x, point1.y);
        path.close();

        canvas.drawPath(path, paint);
    }

    @Override
    public void setAlpha(int i) {
        // Noop
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // Noop
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}