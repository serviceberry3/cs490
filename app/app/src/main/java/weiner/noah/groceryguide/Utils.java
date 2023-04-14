package weiner.noah.groceryguide;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Utils {
    //method to convert your text to image
    public static Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);

        //ascent() returns distance above the baseline (ascent) based on the current typeface and text size.
        //(baseline is invisible line on which text rests)
        float y = paint.ascent() * -1;

        //measureText() returns width of the text
        int width = (int)paint.measureText(text);
        int height = (int)(y + paint.descent());

        //create bitmap with appropriate w and h to contain the text
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        //create Canvas from the Bitmap, then draw in the text
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, y, paint);

        //return the Bitmap
        return image;
    }
}
