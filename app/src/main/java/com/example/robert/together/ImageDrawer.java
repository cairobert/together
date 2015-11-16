package com.example.robert.together;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

/**
 * Created by robert on 10/23/15.
 */
public class ImageDrawer {

    public static Bitmap getRoundBitmap(Context ctx, Bitmap bitmap, int diameter) {
        Bitmap output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawARGB(0, 0, 0, 0);

        Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect rect = new Rect(0, 0, diameter, diameter);
        RectF rectF = new RectF(rect);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float radius = (float) (diameter / 2.0);
        canvas.drawRoundRect(rectF, radius, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, rect, paint);

        return output;
    }

    private static final float BLUR_RADIUS = 7.5f;
    private static final float BITMAP_SCALE = 0.25f;

    public enum BlurPart {
        ALL, QUATER;
    }

    public static Bitmap blur(Context ctx, Bitmap bitmap) {
        return blur(ctx, bitmap, BlurPart.ALL);
    }

    public static Bitmap blur(Context ctx, Bitmap bitmap, BlurPart part) {
        int w = Math.round(bitmap.getWidth() * BITMAP_SCALE);
        int h = Math.round(bitmap.getHeight() * BITMAP_SCALE);

        Bitmap input = Bitmap.createScaledBitmap(bitmap, w, h, false);
        Bitmap output = Bitmap.createBitmap(input);

        RenderScript rs = RenderScript.create(ctx);
        ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation in = Allocation.createFromBitmap(rs, input);
        intrinsicBlur.setRadius(BLUR_RADIUS);
        intrinsicBlur.setInput(in);
        Allocation out = Allocation.createFromBitmap(rs, output);
        intrinsicBlur.forEach(out);
        out.copyTo(output);

        if (part == BlurPart.QUATER) {
            return quater(output);
        }
        return output;
    }

    private static Bitmap quater(Bitmap bitmap) {
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        return bitmap1;
    }
}
