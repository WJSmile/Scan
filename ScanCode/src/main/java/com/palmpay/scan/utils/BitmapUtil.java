package com.palmpay.scan.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;


public class BitmapUtil {

    /**
     * 添加logo到二维码图片上
     */
    public static Bitmap addLogoInQRCode(Bitmap original, Bitmap logo, int logoWidth,int logoHeight ) {
        if (original == null || logo == null) {
            return original;
        }

        int srcWidth = original.getWidth();
        int srcHeight = original.getHeight();

        logo = getResizedBitmap(logo, logoWidth, logoHeight);

        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(original, 0, 0, null);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) >> 1, (srcHeight - logoHeight) >> 1, null);
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            e.printStackTrace();
            bitmap = original;
        }
        return bitmap;
    }

    private static Bitmap getResizedBitmap(Bitmap original, int newWidth, int newHeight) {
        int width = original.getWidth();
        int height = original.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(original, 0, 0, width, height, matrix, false);
        original.recycle();
        return resizedBitmap;
    }



    public static Bitmap getBitmap(Context context, @DrawableRes int vectorDrawableId) {
        Bitmap bitmap;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
            if (vectorDrawable == null) {
                return null;
            }
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }
        return bitmap;
    }



    /**
     * 将本地图片文件转换成可解码二维码的 Bitmap
     *
     * @param picturePath 本地图片文件路径
     */
    public static Bitmap getDecodeAbleBitmap(String picturePath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picturePath, options);
            int sampleSize = Math.min(options.outHeight, options.outWidth) / 500;
            if (sampleSize <= 0) {
                sampleSize = 1;
            }
            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeFile(picturePath, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
