package weiner.noah.groceryguide;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import java.util.ArrayList;
import java.util.Objects;

public class StoreMap2D extends View {

    public boolean mShowText;
    public int textPos;

    public Paint textPaint, shadowPaint;
    public TextPaint subCatTextPaint;

    public float textWidth = 10;
    public float textHeight = 10;
    public int textColor = Color.BLACK;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    Matrix drawMatrix = new Matrix();
    Path transformedPath = new Path();

    private final int INVALID_POINTER_ID = -1;
    private final int AXIS_X_MIN = 0;
    private final int AXIS_X_MAX = 1000;
    private final int AXIS_Y_MIN = 0;
    private final int AXIS_Y_MAX = 2000;

    private float mLastGestureX;
    private float mLastGestureY;

    private int mActivePointerId = INVALID_POINTER_ID;

    private float mLastTouchX, mLastTouchY;
    private float mPosX, mPosY;

    private int left = 0;
    private int top = 200;
    private int right = 1080;
    private int bottom = 1100;

    private final String TAG = "StoreMap2D";

    private Drawable d;

    private final SSWhalley ssWhalley;

    private DBManager dbManager;

    //labels that have been drawn, along with their x,y coordinates
    private final ArrayList<Pair<PointF, String>> drawnLabels = new ArrayList<>();

    // These matrices will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int PAN = 1;
    static final int ZOOM = 2;
    int mode = NONE;


    public StoreMap2D(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.StoreMap2D,
                0, 0);

        try {
            mShowText = a.getBoolean(R.styleable.StoreMap2D_showText, false);
            textPos = a.getInteger(R.styleable.StoreMap2D_labelPosition, 0);
            mPosX = a.getInteger(R.styleable.StoreMap2D_posX, 0);
            mPosY = a.getInteger(R.styleable.StoreMap2D_posY, 0);
        } finally {
            a.recycle();
        }

        ssWhalley = new SSWhalley();

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        d = ResourcesCompat.getDrawable(getResources(), R.drawable.svg_floor, null);
        assert d != null;
        d.setBounds(left, top, right, bottom);

        //init the Paint
        init();
    }


    public boolean isShowText() {
        return mShowText;
    }

    public void setShowText(boolean showText) {
        mShowText = showText;

        //need to invalidate the view after any change to its properties that might change its appearance, so that the system knows it needs to be redrawn.
        invalidate();

        //you need to request a new layout if a property changes in a way that might affect the size or shape of the view.
        requestLayout();
    }

    private void init() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);

        if (textHeight == 0) {
            textHeight = textPaint.getTextSize();
        } else {
            textPaint.setTextSize(textHeight);
        }

        shadowPaint = new Paint(0);
        shadowPaint.setColor(0xff101010);
        shadowPaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));

        subCatTextPaint = new TextPaint();
    }

    private void drawNameInAisle(Canvas canvas, String name, int aisle, int side, float dist) {
        String aisleName = "aisle_" + (side == 0 ? aisle + 1 : aisle) + "_" + (side == 0 ? aisle : aisle - 1);
        //Log.i(TAG, "drawNameInAisle: looking for name " + aisleName);

        subCatTextPaint.setTextSize(Constants.catNameTextSize);
        subCatTextPaint.setColor(textColor);

        StaticLayout mTextLayout;

        ArrayList<SSWhalley.StoreElement> elements = ssWhalley.getRectList();

        for (SSWhalley.StoreElement element : elements) {
            //FIXME: is there a faster way to find the correct element?
            if (element.getId().equals(aisleName)) {
                //find x center coord of aisle
                RectF thisRect = element.getRect();
                float x = thisRect.centerX();
                float bottom = thisRect.bottom;

                float len = thisRect.height();


                float y = bottom - (len * dist);


                //go through and see if any category name label have already used the same y position
                for (Pair<PointF, String> p : drawnLabels) {
                    if (p.first.y == y) {
                        Log.i(TAG, "TRIGGERED for category " + name + ": position " + y + " already used same y pos for category " + p.second + "!");

                        //check how long the name that's in the way is. if it's long enough that it will wrap, we need to lower this label by 2*text size
                        //adjust by placing directly above or below
                        y += (p.second.length() > Constants.catNameTextWidth) ? Constants.catNameTextSize * 2 : Constants.catNameTextSize;
                    }

                }

                mTextLayout = new StaticLayout(name, subCatTextPaint, Constants.catNameTextWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                Log.i(TAG, "Drawing cat " + name + " at y coord " + y);
                //canvas.drawText(name, x, y, textPaint);

                canvas.save();

                canvas.translate(x, y);
                mTextLayout.draw(canvas);
                canvas.restore();

                PointF pt = new PointF(x, y);
                Pair<PointF, String> newPair = new Pair<PointF, String>(pt, name);
                //add this dist from front to the arraylist so we'll know if it's already taken
                drawnLabels.add(newPair);
            }
        }
    }

    private void drawSubcatNames(Canvas canvas) {
        //need to examine the db, fetch coords for each
        //instantiate new DBManager object and open the db
        dbManager = new DBManager(getContext());
        dbManager.open();

        //get cursor to read the db, advancing to first entry
        Cursor cursor = dbManager.fetch(DatabaseHelper.SUBCAT_LOC_TABLE_NAME);

        if (cursor != null) {
            //move cursor to first row of table
            if (cursor.moveToFirst()) {
                //iterate over all rows in the subcats location table
                do {
                    //get the indices of the table cols
                    int nameColIdx = cursor.getColumnIndex("subCatName");
                    int aisleColIdx = cursor.getColumnIndex("aisle");
                    int sideColIdx = cursor.getColumnIndex("side");
                    int distFromFrontColIdx = cursor.getColumnIndex("distFromFront");


                    if (nameColIdx >= 0 && aisleColIdx >= 0 && sideColIdx >=0 && distFromFrontColIdx >= 0) {
                        String subCatName = cursor.getString(nameColIdx);
                        int aisle = cursor.getInt(aisleColIdx);
                        int side = cursor.getInt(sideColIdx);
                        float distFromFront = cursor.getFloat(distFromFrontColIdx);

                        drawNameInAisle(canvas, subCatName, aisle, side, distFromFront);
                    }
                    else {
                        Log.e(TAG, "ERROR: column not found in table!!");
                        break;
                    }
                } while (cursor.moveToNext());
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.setMatrix(matrix);

        //dims are 2200x1375
        //dims are actually 1999x1080
        int ht = getHeight();
        int width = getWidth();
        //Log.i(TAG, "Canvas dims are height " + ht + " and width " + width);

        canvas.save();

        /*
        canvas.translate(mPosX, mPosY);
        Log.i(TAG, "mScaleFactor is " + mScaleFactor);
        canvas.scale(mScaleFactor, mScaleFactor);*/

        /*
        textPaint.setTextSize(100);
        // Draw the label text
        canvas.drawText("TEST", 100, 100, textPaint);

        textPaint.setStrokeWidth(10);

        //draw the pointer
        canvas.drawLine(200, 200, 400, 400, textPaint);
        canvas.drawCircle(600, 600, 100, textPaint);
        */

        //d.draw(canvas);

        ArrayList<SSWhalley.StoreElement> elements = ssWhalley.getRectList();

        for (int i = 0; i < elements.size(); i++) {
            if (Objects.equals(elements.get(i).getId(), "frame")) {
                textPaint.setColor(Color.parseColor("#f6def7"));
            } else {
                textPaint.setColor(Color.parseColor("#cccccc"));
            }

            //get actual rectangle bounds to be drawn on canvas
            RectF thisRect = elements.get(i).getRect();

            //save canvas state before possible rotation
            canvas.save();
            float rot = elements.get(i).getRot();
            if (rot != 0f) {
                canvas.rotate(rot, thisRect.centerX(), thisRect.centerY());
            }

            canvas.drawRect(thisRect, textPaint);

            //restore canvas to state it was in before rotation
            canvas.restore();
        }

        //TODO: change this?
        //draw in all of the subcategory names at appropriate location
        drawSubcatNames(canvas);

        drawnLabels.clear();

        //return canvas to state it was in upon entering onDraw()
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Account for padding
        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());

        // Account for the label
        if (mShowText) xpad += textWidth;

        float ww = (float) w - xpad;
        float hh = (float) h - ypad;

        // Figure out how big we can make the pie.
        float diameter = Math.min(ww, hh);

        super.onSizeChanged((int) ww, (int) hh, oldw, oldh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        /*
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);


        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                // Remember where we started (for dragging)
                mLastTouchX = x;
                mLastTouchY = y;

                // Save the ID of this pointer (for dragging)
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);

                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // Find the index of the active pointer and fetch its position
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);

                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float y = MotionEventCompat.getY(ev, pointerIndex);

                // Calculate the distance moved
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;

                mPosX += dx;
                mPosY += dy;

                invalidate();

                // Remember this touch position for the next move event
                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);

                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = MotionEventCompat.getX(ev, newPointerIndex);
                    mLastTouchY = MotionEventCompat.getY(ev, newPointerIndex);
                    mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
                }
                break;
            }
        }
        return true;
         */
        PanZoomWithTouch(ev);

        invalidate();//necessary to repaint the canvas
        return true;

    }

    void PanZoomWithTouch(MotionEvent event){
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN://when first finger down, get first point
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=PAN");
                mode = PAN;
                break;
            case MotionEvent.ACTION_POINTER_DOWN://when 2nd finger down, get second point
                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event); //then get the mide point as centre for zoom
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:       //when both fingers are released, do nothing
                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;
            case MotionEvent.ACTION_MOVE:     //when fingers are dragged, transform matrix for panning
                if (mode == PAN) {
                    // ...
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x,
                            event.getY() - start.y);
                    Log.d(TAG,"Mapping rect");
                    //start.set(event.getX(), event.getY());
                }
                else if (mode == ZOOM) { //if pinch_zoom, calculate distance ratio for zoom
                    float newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }
    }

    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
        // ...
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Sets the current viewport (defined by mCurrentViewport) to the given
     * X and Y positions. Note that the Y value represents the topmost pixel position,
     * and thus the bottom of the mCurrentViewport rectangle.
     */
    private void setViewportBottomLeft(float x, float y) {
        /*
         * Constrains within the scroll range. The scroll range is simply the viewport
         * extremes (AXIS_X_MAX, etc.) minus the viewport size. For example, if the
         * extremes were 0 and 10, and the viewport size was 2, the scroll range would
         * be 0 to 8.
         */

        float curWidth = mCurrentViewport.width();
        float curHeight = mCurrentViewport.height();
        x = Math.max(AXIS_X_MIN, Math.min(x, AXIS_X_MAX - curWidth));
        y = Math.max(AXIS_Y_MIN + curHeight, Math.min(y, AXIS_Y_MAX));

        mCurrentViewport.set(x, y - curHeight, x + curWidth, y);

        //Invalidates the View to update the display.
        ViewCompat.postInvalidateOnAnimation(this);
    }

    //RectF is a float rectangle
    // The current viewport. This rectangle represents the currently visible chart domain and range.
    private RectF mCurrentViewport = new RectF(AXIS_X_MIN, AXIS_Y_MIN, AXIS_X_MAX, AXIS_Y_MAX);

    //The current destination rectangle (in pixel coordinates) into which the chart data should be drawn.
    private Rect mContentRect;

    private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Scrolling uses math based on the viewport (as opposed to math using pixels).

            // Pixel offset is the offset in screen pixels, while viewport offset is the offset within the current viewport.
            float viewportOffsetX = distanceX * mCurrentViewport.width() / mContentRect.width();
            float viewportOffsetY = -distanceY * mCurrentViewport.height() / mContentRect.height();

            // Updates the viewport, refreshes the display.
            setViewportBottomLeft(mCurrentViewport.left + viewportOffsetX, mCurrentViewport.bottom + viewportOffsetY);

            return true;
        }
    };

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private final float MIN_SCALE = 0.1f;
        private final float MAX_SCALE = 10.0f;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            /*
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(MIN_SCALE, Math.min(mScaleFactor, MAX_SCALE));

            invalidate();
            */


            return true;
        }
    }
}
