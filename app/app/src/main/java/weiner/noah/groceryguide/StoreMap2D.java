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
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StoreMap2D extends View {

    public boolean mShowText;
    public int textPos;

    public Paint textPaint, dotPaint, cameraRectPaint, zoneOfInterestRectPaint;
    public TextPaint subCatTextPaint;

    public float textWidth = 10;
    public float textHeight = 10;
    public int textColor = Color.BLACK;

    private float mScaleFactor = 1.f;

    //point in canvas that's currently at the ctr of the screen
    private float xCtr, yCtr;
    private float xCameraCtr, yCameraCtr;

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
    private final ArrayList<SubcatLabel> subCatLabels = new ArrayList<SubcatLabel>();
    private ArrayList<SubcatLabel> drawnLabelsSaved;

    //rects for zooming to product zone
    private RectF src = null, dst = null, zone = null;

    //current transformation matrix for drawing Canvas that holds the map.
    //this is used on every single call of onDraw(), so on each redraw of the map Canvas
    Matrix matrix = new Matrix();
    Matrix cameraMatrix = new Matrix();

    //save the previous matrix when doing zoom and pan
    Matrix savedMatrix = new Matrix();

    //save some stuff for zooming
    PointF touchStartingPt = new PointF();
    PointF midPtBetweenFingers = new PointF();

    float initialDistBetweenFingers = 1f;

    //zoom and pan: possible states
    private enum zoomPanState {
        STILL,
        PAN,
        ZOOM
    }

    //the current state we're in
    zoomPanState currState = zoomPanState.STILL;

    //list of all dots to be drawn
    private final List<Dot> dots = new ArrayList<Dot>();

    //constructor
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

        d = ResourcesCompat.getDrawable(getResources(), R.drawable.svg_floor, null);
        assert d != null;
        d.setBounds(left, top, right, bottom);

        //init stuff, including the Paint
        init();
    }

    //a subcategory label (represents ONE SINGLE text label)
    private class SubcatLabel {
        PointF pt;
        String txt;
        int id;

        public SubcatLabel(PointF pt, String txt, int id) {
            this.pt = pt;
            this.txt = txt;
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public String getTxt() {
            return txt;
        }

        public PointF getPt() {
            return pt;
        }
    }

    //this class represents a dot at a certain pt in an aisle to indicate presence of certain subcat
    private class Dot {
        int aisle;
        int side;
        float distFromFront;

        int id = -1;

        public Dot(int aisle, int side, float distFromFront) {
            this.aisle = aisle;
            this.side = side;
            this.distFromFront = distFromFront;
        }

        public Dot(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public float getDistFromFront() {
            return distFromFront;
        }

        public int getAisle() {
            return aisle;
        }

        public int getSide() {
            return side;
        }
    }

    //another way to indicate presence of subcat on the map. The zone is drawn as a rectangle outline around an area of subcat labels of the same subcat
    private class Zone {
        int aisle;
        int side;
        float distFromFrontMin;
        float distFromFrontMax;

        public Zone(int aisle, int side, float distFromFrontMin, float distFromFrontMax) {
            this.aisle = aisle;
            this.side = side;
            this.distFromFrontMin = distFromFrontMin;
            this.distFromFrontMax = distFromFrontMax;
        }
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

    //translate the "camera," ie if dx = 5, dy = 5, the map itself should move left 5, up 5
    public void cameraTranslate(float dx, float dy) {
        matrix.postTranslate(-1 * dx, -1 * dy);
        xCameraCtr += dx * 0.8;
        yCameraCtr += dy * 0.8;
    }

    public void cameraScale(float sx, float sy) {
        matrix.postScale(sx, sy, xCameraCtr, yCameraCtr);
    }

    //translate the map itself, ie if dx = 5, dy = 5, the map itself should move right 5, down 5
    public void mapTranslate(float dx, float dy) {
        matrix.postTranslate(dx, dy);
        xCameraCtr -= dx / 100;
        //yCameraCtr -= dy;
    }

    public void mapScale(float sx, float sy) {
        matrix.postScale(sx, sy, xCameraCtr, yCameraCtr);
    }

    private void init() {
        xCtr = Constants.mapFrameRectCtrX;
        yCtr = Constants.mapFrameRectCtrY;

        xCameraCtr = Constants.mapCanvCtrX;
        yCameraCtr = Constants.mapCanvCtrY;

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);

        if (textHeight == 0) {
            textHeight = textPaint.getTextSize();
        } else {
            textPaint.setTextSize(textHeight);
        }

        /*
        shadowPaint = new Paint(0);
        shadowPaint.setColor(0xff101010);
        shadowPaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));
         */

        //init the dot paint
        dotPaint = new Paint();
        dotPaint.setColor(Color.RED);

        subCatTextPaint = new TextPaint();

        cameraRectPaint = new Paint();
        cameraRectPaint.setColor(Color.BLACK);
        cameraRectPaint.setStyle(Paint.Style.STROKE);

        zoneOfInterestRectPaint = new Paint();
        zoneOfInterestRectPaint.setColor(Color.GREEN);
        zoneOfInterestRectPaint.setStyle(Paint.Style.STROKE);
        zoneOfInterestRectPaint.setStrokeWidth(Constants.zoneRectStrokeWidth);

        //initial translation / scale of the matrix so that map is centered in the window
        //cameraTranslate(0, -(Constants.mapCanvHeight - Constants.mapFrameRectHeight) / 2);

        //cameraScale(0.8f, 0.8f);

        invalidate();

        //load (from db) and lay out the subcategory name labels, i.e. find all of their drawing positions on the canvas
        loadSubcatNames();
    }

    //draw a dot at a certain position on map
    public void drawDots(Canvas canvas) {
        //draw all the Dot objects in the list
        for (Dot d : dots) {
            int id = d.getId();
            if (id > 0) {
                //now that we have the id number of the label that the dot should be drawn next to, we can simply look in the drawnLabels array, find label with that id,
                //and draw dot next to that label

                for (SubcatLabel l : subCatLabels) {
                    if (l.getId() == id) {
                        canvas.drawCircle(l.getPt().x + Math.min(l.getTxt().length(), Constants.catNameTextWidth) + Constants.dotsPadding, l.getPt().y + (Constants.catNameTextSize / 2f) + 0.5f, Constants.dotsRad, dotPaint);
                    }
                }

                /*PREVIOUS IMPLEM
                String aisleName = "aisle_" + (d.side == 0 ? d.aisle + 1 : d.aisle) + "_" + (d.side == 0 ? d.aisle : d.aisle - 1);

                for (SSWhalley.StoreElement element : elements) {
                    //FIXME: is there a faster way to find the correct element?
                    if (element.getId().equals(aisleName)) {
                        //find x center coord of aisle
                        RectF thisRect = element.getRect();
                        float x = thisRect.centerX() + Constants.catNameTextWidth;
                        float bottom = thisRect.bottom;

                        float len = thisRect.height();

                        float y = bottom - (len * d.distFromFront);

                        canvas.drawCircle(x, y, 0.5f, dotPaint);
                    }
                }*/
            }
        }
    }

    public void zoomOnSubcatLabels() {
        Log.i(TAG, "zoomOnSubCatLabels() called!");

        //which dim we want to fix for our rectangular zone of interest
        //if we fix x dimensions (0), the Canvas will zoom such that the x min and max of our zone are aligned with the screen left and rt edges, and the y bounds are scaled appropriately to maintain the aspect ratio of the whole Canvas
        //if we fix y dimensions (1), the Canvas will zoom such that the y min and max of our zone are aligned with the screen top and bottom edges, and the x bounds are scaled appropriately to maintain the aspect ratio of the whole Canvas
        int fixXOrYDim = 1; //by default, fix y dims

        //get the aisles
        ArrayList<SSWhalley.StoreElement> elements = ssWhalley.getRectList();

        //how far do the labels of interest span on x and y axis?
        float ySpan, xSpan, scaleFactor, leftPad, topPad, left, right, bottom, top;

        float xMin  = 5000;
        float xMax = -1;
        float yMin = 5000;
        float yMax = -1;

        int maxTextLen = -1;

        Log.i(TAG, "There are " + dots.size() + " dots currently, and " + subCatLabels.size() + " items in drawnLabels");

        if (dots.size() > 0) {
            //draw all the Dot objects in the list
            for (StoreMap2D.Dot d : dots) {
                int id = d.getId();

                if (id > 0) {
                    //now that we have the id number of the label that the dot should be drawn next to, we can simply look in the drawnLabels array, find label with that id,
                    //and draw dot next to that label

                    for (StoreMap2D.SubcatLabel l : subCatLabels) {
                        if (l.getId() == id) {
                            Log.i(TAG, "Dot ID matches drawn label ID, label x is " + l.getPt().x + ", label y is " + l.getPt().y);
                            xMin = Math.min(xMin, l.getPt().x);
                            yMin = Math.min(yMin, l.getPt().y);
                            xMax = Math.max(xMax, l.getPt().x);
                            yMax = Math.max(yMax, l.getPt().y);

                            maxTextLen = Math.max(maxTextLen, l.getTxt().length());
                        }
                    }
                }
            }

            ySpan = yMax - yMin;
            xSpan = Math.min(Constants.catNameTextWidth, maxTextLen);


            if (ySpan == 0) {
                //zoom so x bounds fit the screen edges instead, and y bounds will adjust accordingly
                fixXOrYDim = 0;
                ySpan = Constants.catNameTextSize + 2;
            }

            Log.i(TAG, "xMin is " + xMin + ", xMax is " + xMax + ", yMin is " + yMin + ", yMax is " + yMax);

            float xCentroid = (xMin + xMax) / 2;
            float yCentroid = ((yMin + yMax) / 2) + (Constants.mapCanvHeight - Constants.mapFrameRectHeight) / 2;

            Log.i(TAG, "zoomOnSubCatLabels(): centroid is (" + xCentroid + ", " + yCentroid + ")");

            float dx = Constants.mapCanvCtrX - xCentroid;
            float dy = Constants.mapCanvCtrY - yCentroid;

            xCtr -= dx;
            yCtr -= dy;

            if (fixXOrYDim == 1) {
                //compute scale factor needed to fill screen with rectangular zone of interest
                //to do so, find ratio between full canvas height and the height of our zone
                scaleFactor = Constants.mapCanvHeight / ySpan;

                //to find left padding dist, take some of the screen width (use canvas width but then scale down by scale factor we computed)
                leftPad = (Constants.mapCanvWidth / 2 - Constants.mapCanvWidth / 10) / scaleFactor;

                //compute left and right bounds
                left = xMin - leftPad;
                right = left + Constants.mapCanvAspectRatioWH * ySpan; //maintain entire canvas' aspect ratio so that mapping works: whatever y span needs to be, compute proportional x span

                //bottom is just bottom yBound plus space for last subcat label text
                bottom = yMax + Constants.catNameTextSize + 1;

                top = yMin;

                //make sure aspect ratio of src rect = aspect ratio of dst rect, else mapping will fail
                //TODO: instead of making new rect each time, just instantiate one at beginning and chg values

                //rect args: left, top, right, bottom
                src = new RectF(left, top, right, bottom); //src is rect containing zone of interest
            }
            else {
                scaleFactor = Constants.mapCanvWidth / xSpan;

                topPad = (Constants.mapCanvHeight / 2 - Constants.mapCanvHeight / 10) / scaleFactor;

                top = yMin - topPad;
                bottom = top + ((1/Constants.mapCanvAspectRatioWH) * xSpan);

                right = xMin + Math.min(Constants.catNameTextWidth, maxTextLen) + Constants.dotsPadding + Constants.dotsRad + 2;

                left = xMin - 2;

                src = new RectF(left, top, right, bottom);
            }

            dst = new RectF(0, 0, Constants.mapCanvWidth, Constants.mapCanvHeight); //dst is rect containing the whole canvas

            zone = new RectF(xMin - 1, yMin, xMin + Math.min(Constants.catNameTextWidth, maxTextLen) + Constants.dotsPadding + Constants.dotsRad + 1, bottom = yMax + Constants.catNameTextSize + 1);

            boolean ret = matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER); //center the rect on screen
            Log.i(TAG, "setRectToRect result is " + ret);
        }
    }

    public void addDot(int aisle, int side, float distFromFront) {
        //Log.i(TAG, "adding dot at aisle " + aisle + ", side " + side + ", distFromFront " + distFromFront);
        dots.add(new Dot(aisle, side, distFromFront));
    }

    public void addDot(int id) {
        //Log.i(TAG, "adding dot for subcat lbl with id " + id);
        dots.add(new Dot(id));
    }

    private void layOutSubcatNames(int id, String name, int aisle, int side, float dist) {
        String aisleName = "aisle_" + (side == 0 ? aisle + 1 : aisle) + "_" + (side == 0 ? aisle : aisle - 1);
        //Log.i(TAG, "drawNameInAisle: looking for name " + aisleName);

        subCatTextPaint.setTextSize(Constants.catNameTextSize);
        subCatTextPaint.setColor(textColor);

        ArrayList<SSWhalley.StoreElement> elements = ssWhalley.getRectList();

        for (SSWhalley.StoreElement element : elements) {
            //FIXME: is there a faster way to find the correct element?
            if (element.getId().equals(aisleName)) {
                //find x center coord of aisle
                RectF thisRect = element.getRect();
                float x = thisRect.centerX();
                float bottom = thisRect.bottom;

                float len = thisRect.height();

                //y val should be aisle bottom - distance up the aisle where the prod is located
                float y = bottom - (len * dist);

                //go through and see if any category name label have already used the same y position
                for (SubcatLabel l : subCatLabels) {
                    if (l.getPt().y == y) {
                        //Log.i(TAG, "TRIGGERED for category " + name + ": position " + y + " already used same y pos for category " + l.getTxt() + "!");

                        //check how long the name that's in the way is. if it's long enough that it will wrap, we need to lower this label by 2*text size
                        //adjust by placing directly above or below. otherwise we can just lower the label by 1*text size
                        y += (l.getTxt().length() > Constants.catNameTextWidth) ? Constants.catNameTextSize * 2 : Constants.catNameTextSize;
                    }
                }

                //Log.i(TAG, "subcatlabel x val is " + x + ", y val is " + y);
                PointF pt = new PointF(x, y);
                //Pair<PointF, String> newPair = new Pair<PointF, String>(pt, name);

                //Log.i(TAG, "Creating new subcatlabel with name " + name + " and yval " + pt.y);
                SubcatLabel newLabel = new SubcatLabel(pt, name, id);

                //add this subcat label to the drawn labels arraylist
                //note that the PointF of this label is the actual final loc where lbl was drawn
                subCatLabels.add(newLabel);
            }
        }
    }

    private void loadSubcatNames() {
        //need to examine the db, fetch coords for each
        //instantiate new DBManager object and open the db
        dbManager = new DBManager(getContext());
        dbManager.open();

        //get cursor to read the db, advancing to first entry
        Cursor cursor = dbManager.fetch(DatabaseHelper.SUBCAT_LOC_TABLE_NAME, new Query(), null);

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
                    int idColIdx = cursor.getColumnIndex("_id");


                    if (nameColIdx >= 0 && aisleColIdx >= 0 && sideColIdx >=0 && distFromFrontColIdx >= 0 && idColIdx >= 0) {
                        String subCatName = cursor.getString(nameColIdx);
                        int aisle = cursor.getInt(aisleColIdx);
                        int side = cursor.getInt(sideColIdx);
                        float distFromFront = cursor.getFloat(distFromFrontColIdx);
                        int id = cursor.getInt(idColIdx);

                        layOutSubcatNames(id, subCatName, aisle, side, distFromFront);
                    }
                    else {
                        Log.e(TAG, "ERROR: column not found in table!!");
                        break;
                    }
                } while (cursor.moveToNext());
            }
        }
    }

    public void drawSubcatLabels(Canvas canvas) {
        for (SubcatLabel l : subCatLabels) {
            StaticLayout mTextLayout = new StaticLayout(l.getTxt(), subCatTextPaint, Constants.catNameTextWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            //Log.i(TAG, "Drawing cat " + name + " at y coord " + y);
            //canvas.drawText(name, x, y, textPaint);

            canvas.save();

            canvas.translate(l.getPt().x, l.getPt().y);
            mTextLayout.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //save canvas state before proceeding
        canvas.save();

        //completely replace current canvas' transformation matrix with specified matrix. If the matrix param is null, then current matrix is reset to identity.
        canvas.setMatrix(matrix);

        //dims of the Canvas itself are 1999hx1080w
        //int ht = getHeight();
        //int width = getWidth();
        //Log.i(TAG, "Dims of the drawing Canvas are height " + ht + " and width " + width);

        /*
        canvas.translate(mPosX, mPosY);
        Log.i(TAG, "mScaleFactor is " + mScaleFactor);
        canvas.scale(mScaleFactor, mScaleFactor);*/

        ArrayList<SSWhalley.StoreElement> elements = ssWhalley.getRectList();

        //draw each element of the store map
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

        //Log.i(TAG, "onDraw() called..clearing list of drawn subcat labels!!");

        //TODO: change this?
        //draw in all of the subcategory names at appropriate location
        drawSubcatLabels(canvas);

        //draw aisle dots
        drawDots(canvas);

        Log.i(TAG, "Drawing centroid (local) at point (" + xCtr + ", " + yCtr + ")");
        Log.i(TAG, "Drawing centroid (camera) at point (" + xCameraCtr + ", " + yCameraCtr + ")");

        /*
        //FOR TESTING / DBUG
        //canvas.drawCircle(xCtr, yCtr, 6, dotPaint);
        //canvas.drawCircle(xCtrAbs, yCtrAbs, 6, subCatTextPaint);
        //draw rect around camera viewport when zoom in on product area
        if (src != null) {
            canvas.drawRect(src, cameraRectPaint);
        }*/

        //draw rect around area where prod should be when zoom in on prod
        if (zone != null) {
            canvas.drawRect(zone, zoneOfInterestRectPaint);
        }

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
        zoomAndPan(ev);

        invalidate();//necessary to repaint the canvas
        return true;
    }

    //handle all touch events on the store map
    void zoomAndPan(MotionEvent event) {
        //we maintain a state machine for the pan and zoom.
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            //when first finger goes down, get first point (that finger is touching)
            case MotionEvent.ACTION_DOWN:
                //save the Canvas' current transformation matrix
                savedMatrix.set(matrix);

                //save starting (x,y) point of the finger
                touchStartingPt.set(event.getX(), event.getY());

                //set current state to panning, since only one finger has been set down
                currState = zoomPanState.PAN;
                break;

            //when 2nd finger goes down, get pt it's touching
            case MotionEvent.ACTION_POINTER_DOWN:
                //save initial distance between the two fingers
                initialDistBetweenFingers = distFingers(event);

                //the user must now be trying to zoom

                //save Canvas' current transformation matrix, then find midpoint between the two fingers and save it into the variable
                savedMatrix.set(matrix);
                midPtFingers(midPtBetweenFingers, event);

                //set current state to zooming, since now two fingers are down
                currState = zoomPanState.ZOOM;

                break;

            //when both fingers are released, do nothing
            case MotionEvent.ACTION_UP:
                //skip, proceed to next case (so that this functions like an OR statement)
            case MotionEvent.ACTION_POINTER_UP:
                //state returns to still
                currState = zoomPanState.STILL;
                break;

            //when fingers are dragged, transform the matrix to create panning or zooming effect
            case MotionEvent.ACTION_MOVE:
                //if we're in pan state
                if (currState == zoomPanState.PAN) {
                    //set Canvas' matrix to what it was when the first finger was placed
                    matrix.set(savedMatrix);

                    //translate matrix appropriately to current finger position, relative to starting point
                    mapTranslate(event.getX() - touchStartingPt.x, event.getY() - touchStartingPt.y);
                }

                //if we're in zoom state
                else if (currState == zoomPanState.ZOOM) {
                    //calculate the live updated Euclidean distance between fingers
                    float newDist = distFingers(event);

                    //restore matrix to what it was when second finger was placed
                    matrix.set(savedMatrix);

                    //compute scale ratio as new dist between fingers : old dist between fingers
                    float scale = newDist / initialDistBetweenFingers;

                    //scale the matrix appropriately around the given midpoint
                    matrix.postScale(scale, scale, midPtBetweenFingers.x, midPtBetweenFingers.y);
                }
                break;
        }
    }


    //determine Euclidean dist between points where two fingers are touching the screen
    private float distFingers(MotionEvent event) {
        //get x and y displacement between the two fingers
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        //compute Euclidean distance formula
        return (float)Math.sqrt(x * x + y * y);
    }

    //determine midpt between points where two fingers are touching the screen
    private void midPtFingers(PointF pt, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);

        pt.set(x / 2, y / 2);
    }
}
