package weiner.noah.groceryguide;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class Utils {
    public static final String TAG = "Utils";

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


    //idea from: https://stackoverflow.com/questions/2920315/permutation-of-array
    //this runs in O(arr.size()!) time. Anything above 12 for arr size will take long.
    public static void permuteArray(ArrayList<Integer> arr, int idx, ArrayList<ArrayList<Integer>> out) {
        ArrayList<Integer> arrCopy = new ArrayList<>(arr);

        for (int i = idx; i < arrCopy.size(); i++) {
            Collections.swap(arrCopy, i, idx);
            permuteArray(arrCopy, idx + 1, out);
            Collections.swap(arrCopy, idx, i);
        }

        if (idx == arrCopy.size() - 1) {
            Log.i(TAG, "adding " + arrCopy.toString() + " to the out list");
            //Log.i(TAG, arr.toString());
            out.add(arrCopy);
        }
    }
}
