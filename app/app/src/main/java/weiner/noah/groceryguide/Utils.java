package weiner.noah.groceryguide;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

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


    //this code is based on permutation algo found at https://stackoverflow.com/questions/2920315/permutation-of-array
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

    public static ArrayList<Integer> reverseArray(ArrayList<Integer> arr) {
        ArrayList<Integer> ret = new ArrayList<>();

        for (int i = arr.size() - 1; i >= 0; i--) {
            ret.add(arr.get(i));
        }

        return ret;
    }

    public static float fixNanOrInfinite(float value)
    {
        //change NaN or infinity to 0
        if (Float.isNaN(value) || Float.isInfinite(value)) return 0;
        return value;
    }

    public static float rangeValue(float value, float min, float max)
    {
        //apply boundaries to a given value
        if (value > max) return max;

        //otherwise if val less than min return min, otherwise return value
        return Math.max(value, min);
    }

    public static void lowPassFilter(float[] input, float[] output, float alpha)
    {
        //iterate through each element of the input float array
        for (int i = 0; i < input.length; i++) {
            //set that slot in the output array to its previous value plus alphaConstant * (change in the value since last reading)
            output[i] = output[i] + (alpha * (input[i] - output[i])); //we only allow the acceleration reading to change by alpha of its actual change

            //a second way to implement
            //output[i] = input[i] - (alpha * output[i] + (1-alpha) * input[i]);
        }
    }

    public static float euclideanDist(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
    }

    public static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("MMddyyHHmmss");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
