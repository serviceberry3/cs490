package weiner.noah.groceryguide;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.PriorityQueue;

public class StoreMap2D extends View {
    private MapFragment mapFragment;
    private MainActivity mainActivity;

    public boolean mShowText;
    public int textPos;

    //PAINTS
    public Paint textPaint, dotPaint, debugRectPaint, zoneOfInterestRectPaint, userLocPaint;
    public TextPaint subCatTextPaint, tinyTextPaint;

    public float textWidth = 10;
    public float textHeight = 10;
    public int textColor = Color.BLACK;

    private float mScaleFactor = 1.f;

    //point in canvas that's currently at the ctr of the screen
    private float xCtr, yCtr;
    private float xCameraCtr, yCameraCtr;

    private float userPixelLocX, userPixelLocY, userPixelLocXInit, userPixelLocYInit, userPixelLocXRatio, userPixelLocYRatio;

    private ArrayList<PointF> ptsToDraw = new ArrayList<>();

    private final int INVALID_POINTER_ID = -1;
    private final int AXIS_X_MIN = 0;
    private final int AXIS_X_MAX = 1000;
    private final int AXIS_Y_MIN = 0;
    private final int AXIS_Y_MAX = 2000;

    private float mLastGestureX;
    private float mLastGestureY;

    //nodes representing entrance and exit of grocery store
    private int startNode, endNode;

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

    private Graph mGraph;
    private Graph nodesToHitGraph;

    //labels to be drawn, along with their drawing coordinates and bounding Paths
    private final ArrayList<SubcatLabel> subCatLabels = new ArrayList<SubcatLabel>();
    private ArrayList<SubcatLabel> drawnLabelsSaved;

    //rects for zooming to product zone
    private RectF src = null, dst = null, zone = null;

    //current transformation matrix for drawing Canvas that holds the map.
    //this is used on every single call of onDraw(), so on each redraw of the map Canvas
    Matrix matrix = new Matrix();
    Matrix ptsTransform = new Matrix();

    //save the previous matrix when doing zoom and pan
    Matrix savedMatrix = new Matrix();

    //save some stuff for zooming
    PointF touchStartingPt = new PointF();
    PointF midPtBetweenFingers = new PointF();

    float initialDistBetweenFingers = 1f;

    private FragmentManager fragmentManager;

    //cell dimensions for the graph (for running shortest path)
    private float cellWidth, cellHeight;
    private int numCellRows = (int) (Constants.mapFrameRectHeight / Constants.cellHeight);
    private int numCellCols = (int) (Constants.mapFrameRectWidth / Constants.cellWidth);
    private Paint gridPaint, cellPaint;

    ArrayList<Integer> nodesToHit = new ArrayList<Integer>();

    //shortest paths between each pair of nodes in nodesToHit
    ArrayList<ShortestPath> shortestPaths = new ArrayList<ShortestPath>();

    //the backtracked shortest paths. We basically want to cache the backtracked paths so that we can draw them quickly on each frame
    ArrayList<ArrayList<Integer>> backTrackedShortestPaths = new ArrayList<ArrayList<Integer>>();

    //the final route for the user's shopping trip
    ArrayList<Integer> finalNodeOrdering = new ArrayList<Integer>();
    ArrayList<ArrayList<Integer>> finalRoute = new ArrayList<ArrayList<Integer>>();

    //which path should we be showing now?
    int pathIdx = -1; //-1 means no path

    //zoom and pan: possible states
    private enum zoomPanState {
        STILL,
        PAN,
        ZOOM
    }

    private enum nodeColor {
        RED,
        GREEN,
        CHECKED
    }

    //the current state we're in
    zoomPanState currState = zoomPanState.STILL;

    //list of all dots to be drawn
    private final List<Dot> dots = new ArrayList<Dot>();

    //constructor
    public StoreMap2D(Context context, AttributeSet attrs) {
        super(context, attrs);
        mainActivity = (MainActivity) getContext();

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

        ssWhalley = mainActivity.getStoreModel();
        mGraph = mainActivity.getGraph();

        RectF entrance = ssWhalley.getElementByName("entrance").getRect();

        //set start pixel loc of user location
        userPixelLocX = entrance.centerX();
        userPixelLocY = entrance.top;
        userPixelLocYInit = userPixelLocY;
        userPixelLocXInit = userPixelLocX;
        userPixelLocXRatio = Constants.mapFrameRectWidth / Constants.SS_WHALLEY_ACTUAL_WIDTH;
        userPixelLocYRatio = Constants.mapFrameRectHeight / Constants.SS_WHALLEY_ACTUAL_HT;

        d = ResourcesCompat.getDrawable(getResources(), R.drawable.svg_floor, null);
        assert d != null;
        d.setBounds(left, top, right, bottom);

        //init stuff, including the Paint
        init();
    }

    //a subcategory label (represents ONE SINGLE text label)
    private class SubcatLabel {
        PointF pt;
        String txt; //name of subcat

        int id, subCatId;

        SSWhalley.StoreElement element; //which store element does this lbl belong to?

        String side; //side: north, south, east, west

        String dir; //which direction was user walking when recording vid on which this subcat lbl was annotated?

        float rot; //rotation for drawing

        //whether to actually draw this lbl on the map
        boolean show;

        //ht that lbl occupies in pixels
        float ht;

        //StaticLayout used to draw text for this subcat label
        StaticLayout textLayout;

        //the bounds of the label (bounds of the StaticLayout) - left, top, right, bottom
        Path bounds;

        //dist from start
        float distFromStart;

        public SubcatLabel(PointF pt, String txt, int id, int subCatId, float ht, Path bounds, StaticLayout layout, SSWhalley.StoreElement element, String side, String dir, float rot) {
            this.pt = pt;
            this.txt = txt;
            this.id = id; //the regular id of the subcat label (autoincrement in the db)
            this.subCatId = subCatId; //the id of the actual subcategory that this label represents
            this.ht = ht; //ht that the lbl takes up
            this.textLayout = layout;
            this.show = true;
            this.element = element;
            this.side = side;
            this.bounds = bounds;
            this.dir = dir;

            this.rot = rot;
        }

        public SubcatLabel(int id, int subCatId, String subCatName, SSWhalley.StoreElement element, String side, float distFromStart, String dir) {
            this.id = id;
            this.subCatId = subCatId;
            this.txt = subCatName;
            this.element = element;
            this.side = side;
            this.distFromStart = distFromStart;
            this.dir = dir;
        }

        public int getId() {
            return id;
        }

        public int getSubCatId() {
            return subCatId;
        }

        public String getTxt() {
            return txt;
        }

        public PointF getPt() {
            return pt;
        }

        public float getHt() {
            return ht;
        }

        public StaticLayout getTextLayout() {
            return textLayout;
        }

        /*
        //change the y position of this subcat label
        public void setY(float newY) {
            this.pt.y = newY;
            this.bounds.top = newY;
            this.bounds.bottom = this.bounds.top + this.ht;
        }*/

        public void setDistFromStart(float newDist) {
            this.distFromStart = newDist;
        }

        public void setShow(boolean show) {
            this.show = show;
        }

        public boolean getShow() {
            return this.show;
        }

        public String getSide() {
            return side;
        }

        public SSWhalley.StoreElement getElement() {
            return element;
        }

        public Path getBounds() {
            return bounds;
        }

        public float getRot() {
            return rot;
        }

        public String getDir() {
            return dir;
        }

        public float getDistFromStart() {
            return distFromStart;
        }

        public void setBounds(Path bounds) {
            this.bounds = bounds;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }

        public void setPt(PointF pt) {
            this.pt = pt;
        }

        public void setHt(float ht) {
            this.ht = ht;
        }

        public void setTextLayout(StaticLayout textLayout) {
            this.textLayout = textLayout;
        }

        public void setRot(float rot) {
            this.rot = rot;
        }
    }

    //this class represents a dot at a certain pt in an aisle to indicate presence of certain subcat
    private class Dot {
        int aisle;
        int side;
        float distFromStart;

        int id = -1;

        public Dot(int aisle, int side, float distFromFront) {
            this.aisle = aisle;
            this.side = side;
            this.distFromStart = distFromFront;
        }

        public Dot(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public float getDistFromStart() {
            return distFromStart;
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
        float distFromStartMin;
        float distFromStartMax;

        public Zone(int aisle, int side, float distFromFrontMin, float distFromFrontMax) {
            this.aisle = aisle;
            this.side = side;
            this.distFromStartMin = distFromFrontMin;
            this.distFromStartMax = distFromFrontMax;
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

    private void init() {
        Log.i(TAG, "init() running!!");

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
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(Color.RED);

        userLocPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        userLocPaint.setColor(Color.BLUE);

        subCatTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        //subCatTextPaint.setTextScaleX(Constants.subCatTextXScale); //this seems to stop letters from overlapping
        subCatTextPaint.setLetterSpacing(0.5f);
        subCatTextPaint.setTextSize(Constants.subCatNameTextSize);
        subCatTextPaint.setColor(textColor);

        tinyTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        tinyTextPaint.setTextSize(1);
        tinyTextPaint.setColor(Color.BLACK);

        debugRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        debugRectPaint.setColor(Color.BLACK);
        debugRectPaint.setStyle(Paint.Style.STROKE);

        zoneOfInterestRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        zoneOfInterestRectPaint.setColor(Color.GREEN);
        zoneOfInterestRectPaint.setStyle(Paint.Style.STROKE);
        zoneOfInterestRectPaint.setStrokeWidth(Constants.zoneRectStrokeWidth);

        //initial translation / scale of the matrix so that map is centered in the window
        matrix.postTranslate(0, (Constants.mapCanvHeight - Constants.mapFrameRectHeight) / 2);
        matrix.postScale(0.8f, 0.8f, Constants.mapCanvCtrX, Constants.mapCanvCtrY);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        gridPaint.setColor(Color.GRAY);
        gridPaint.setAlpha(Constants.gridTransparency);
        cellWidth = Constants.cellWidth;
        cellHeight = Constants.cellHeight;

        cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellPaint.setColor(Color.RED); //color is red by default
        cellPaint.setStyle(Paint.Style.FILL);



        //load (from db) and lay out the subcategory name labels, i.e. find all of their drawing positions on the canvas
        loadSubcatLabels();
        pruneDuplicateLabels();
        adjustSubCatLabels();
    }

    public ArrayList<Integer> getFinalNodeOrdering() {
        return finalNodeOrdering;
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
                        canvas.drawCircle(l.getPt().x + Math.min(l.getTxt().length(), Constants.subCatNameTextWidth) + Constants.dotsPadding, l.getPt().y + (Constants.subCatNameTextSize / 2f) + 0.5f, Constants.dotsRad, dotPaint);
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
        float ySpan, xSpan, scaleFactor, xSpanScaledForAspectRatio, xSpanTotWithBox, leftPad, topPad, left, right, bottom, top;

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
            xSpan = Math.min(Constants.subCatNameTextWidth, maxTextLen);


            if (ySpan == 0) {
                //zoom so x bounds fit the screen edges instead, and y bounds will adjust accordingly
                fixXOrYDim = 0;
                ySpan = Constants.subCatNameTextSize + 2;
            }

            Log.i(TAG, "xMin is " + xMin + ", xMax is " + xMax + ", yMin is " + yMin + ", yMax is " + yMax);

            float xCentroid = (xMin + xMax) / 2;
            float yCentroid = ((yMin + yMax) / 2) + (Constants.mapCanvHeight - Constants.mapFrameRectHeight) / 2;

            Log.i(TAG, "zoomOnSubCatLabels(): centroid is (" + xCentroid + ", " + yCentroid + ")");

            float dx = Constants.mapCanvCtrX - xCentroid;
            float dy = Constants.mapCanvCtrY - yCentroid;

            xCtr -= dx;
            yCtr -= dy;

            xSpanTotWithBox = xSpan + Constants.dotsPadding + Constants.dotsRad + Constants.zoneRectXPad;
            //compute scale factor needed to fill screen with rectangular zone of interest
            //to do so, find ratio between full canvas height and the height of our zone
            scaleFactor = Constants.mapCanvHeight / ySpan;

            //how wide will the zoom rect be
            xSpanScaledForAspectRatio = Constants.mapCanvAspectRatioWH * ySpan;

            if (fixXOrYDim == 1) {
                //to find left padding dist, take some of the screen width (use canvas width but then scale down by scale factor we computed)
                leftPad = (xSpanScaledForAspectRatio - xSpanTotWithBox) / 2;

                //compute left and right bounds
                left = xMin - leftPad;
                right = left + xSpanScaledForAspectRatio; //maintain entire canvas' aspect ratio so that mapping works: whatever y span needs to be, compute proportional x span

                //bottom is just bottom yBound plus space for last subcat label text
                bottom = yMax + Constants.subCatNameTextSize + Constants.zoomRectBottomPad + Constants.zoneRectStrokeWidth;

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

                right = xMin + Math.min(Constants.subCatNameTextWidth, maxTextLen) + Constants.dotsPadding + Constants.dotsRad + 2;

                left = xMin - 2;

                src = new RectF(left, top, right, bottom);
            }

            dst = new RectF(0, 0, Constants.mapCanvWidth, Constants.mapCanvHeight); //dst is rect containing the whole canvas

            zone = new RectF(xMin - Constants.zoneRectXPad, yMin, xMin + xSpanTotWithBox , bottom = yMax + Constants.subCatNameTextSize + Constants.zoneRectBottomPad);

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

    public Pair<Float, Float> getLabelLocalPos(SubcatLabel l) {
        float lblPosTop, lblPosBottom;
        RectF storeFixture = l.getElement().getRect();
        RectF ret;

        //label is on left/rt side of store fixture (fixture is vertical)
        if (Objects.equals(l.getSide(), "n") || Objects.equals(l.getSide(), "s")) {
            //moving upwards
            if (Objects.equals(l.getDir(), "e")) {
                lblPosTop = storeFixture.bottom - (l.getDistFromStart() * storeFixture.height());
            }

            //moving downwards
            else {
                lblPosTop = (l.getDistFromStart() * storeFixture.height()) + storeFixture.top;
            }
            lblPosBottom = lblPosTop + l.getTextLayout().getHeight();
        }

        //label is on top/bottom side of store fixture (fixture is horizontal)
        else {
            //moving left
            if (Objects.equals(l.getDir(), "n")) {
                lblPosTop = storeFixture.right - (l.getDistFromStart() * storeFixture.width());
            }

            //moving rt
            else {
                lblPosTop = (l.getDistFromStart() * storeFixture.width()) + storeFixture.left;
            }
            lblPosBottom = lblPosTop + l.getTextLayout().getHeight();
        }

        return new Pair<Float, Float>(lblPosTop, lblPosBottom);
    }

    public void pruneDuplicateLabels() {
        for (SubcatLabel l : subCatLabels) {
            for (SubcatLabel lbl : subCatLabels) {
                if (l != lbl && l.getElement() == lbl.getElement() && Objects.equals(l.getTxt(), lbl.getTxt()) && Objects.equals(l.getSide(), lbl.getSide()) && l.getDistFromStart() == lbl.getDistFromStart() && l.getShow() && lbl.getShow()) {
                    Log.i(TAG, "Duplicate label found: label " + l.getTxt() + " with ID " + l.getId() + " is duplicate of label with ID " + lbl.getId());
                    l.setShow(false);
                    break; //continue to next for loop iteration
                }
            }
        }

        ListIterator<SubcatLabel> iter = subCatLabels.listIterator();
        SubcatLabel l;

        while (iter.hasNext()) {
            l = iter.next();
            if (!l.getShow()) {
                iter.remove();
            }
        }
    }

    public boolean checkOverlapWithOtherLabels(SubcatLabel thisLabel) {
        String name;
        float newTop, newHt, newBottom, oldTop, oldHt, oldBottom;
        Pair<Float, Float> localPosOfPassedLbl = getLabelLocalPos(thisLabel);
        Pair<Float, Float> localPosOfCompetingLbl;

        SSWhalley.StoreElement element = thisLabel.getElement();
        RectF fixtureRect = thisLabel.getElement().getRect();

        //Log.i(TAG, "Now running adjusts for lbl with ID #" + thisLabel.getId() + ", which spans from " + thisLabel.getPt().y + " to " + (thisLabel.getPt().y + thisLabel.getHt()));
        name = thisLabel.getTxt();

        /*newTop = thisLabel.getPt().y;
        newHt = thisLabel.getHt();
        newBottom = newTop + newHt;*/

        newTop = localPosOfPassedLbl.first;
        newHt = thisLabel.getHt();
        newBottom = localPosOfPassedLbl.second;

        Path res = new Path();

        SubcatLabel l;

        //iterate through all of the OTHER subcat labels
        ListIterator<SubcatLabel> iter = subCatLabels.listIterator();

        while (iter.hasNext()) {
            l = iter.next();

            res.reset();

            //only look at other subcat lbls that are in the same store fixture, on the same side
            if (l.getId() != thisLabel.getId() && l.getElement() == thisLabel.getElement() && Objects.equals(l.getSide(), thisLabel.getSide())) {
                //check if this label will overlap with lbl we're trying to draw. can simply use android Rect.intersect() method

                if (res.op(thisLabel.getBounds(), l.getBounds(), Path.Op.INTERSECT)) {
                    //compute the offset in pixels of the lbl from "start" of the store fixture
                    /*oldTop = l.getPt().y;
                    oldHt = l.getHt();
                    oldBottom = oldTop + oldHt;*/

                    localPosOfCompetingLbl = getLabelLocalPos(l);

                    oldTop = localPosOfCompetingLbl.first;
                    oldHt = l.getHt();
                    oldBottom = localPosOfCompetingLbl.second;

                    //DBUG

                    //FOUR OVERLAP CASES (each is separate if clause to make it clear)
                    /*if (oldTop <= newTop && newTop < oldBottom) {
                        newTop += oldBottom - newTop;
                    }
                    else if ((newBottom > oldTop && newBottom <= oldBottom)) {
                        newTop += newBottom - oldTop;
                    }
                    else if (newTop >= oldTop && newBottom <= oldBottom) {
                        newTop += oldBottom - newTop;
                    }
                    else if (oldTop >= newTop  && oldBottom <= newBottom) {
                        newTop += newBottom - oldTop;
                    }*/
                    Log.i(TAG, "Comparing passed label " + name + " with ID #" + thisLabel.getId() + " to label " + l.getTxt() + " with ID #" + l.getId() + ": passed label has top bottom " + newTop + ", " + newBottom + ", while " +
                            "competing label in question has top bottom " + oldTop + ", " + oldBottom);

                    if (oldTop <= newTop && newTop < oldBottom) {
                        Log.i(TAG, "Overlap found for label " + name + " with ID #" + thisLabel.getId() + ": its position " + newTop + " overlaps with label for category " + l.getTxt() + " with ID #" + l.getId() +
                                " which spans from " + oldTop + " to " + oldBottom + " and has linecount " + l.getTextLayout().getLineCount() +
                                ". subtracting " + (oldBottom - newTop)/element.getSpan() + " to this lbl which will move it down to " + (thisLabel.getDistFromStart() - (oldBottom - newTop)/element.getSpan()));
                        //newTop += oldBottom - newTop;
                        thisLabel.setDistFromStart(thisLabel.getDistFromStart() - (oldBottom - newTop)/element.getSpan());
                    }
                    else if ((newBottom > oldTop && newBottom <= oldBottom)) {
                        Log.i(TAG, "Overlap found for label " + name + " with ID #" + thisLabel.getId() + ": its position " + newTop + " overlaps with label for category " + l.getTxt() + " with ID #" + l.getId() +
                                " which spans from " + oldTop + " to " + oldBottom + " and has linecount " + l.getTextLayout().getLineCount() +
                                ". Adding " + (newBottom - oldTop) + " to this lbl which will move it down to " + (newTop + (newBottom - oldTop)));
                        //newTop += newBottom - oldTop;
                        thisLabel.setDistFromStart(thisLabel.getDistFromStart() - (newBottom - oldTop)/element.getSpan());
                    }
                    else if (newTop >= oldTop && newBottom <= oldBottom) {
                        Log.i(TAG, "Overlap found for label " + name + " with ID #" + thisLabel.getId() + ": its position " + newTop + " overlaps with label for category " + l.getTxt() + " with ID #" + l.getId() +
                                " which spans from " + oldTop + " to " + oldBottom + " and has linecount " + l.getTextLayout().getLineCount() +
                                ". Adding " + (oldBottom - newTop) + " to this lbl which will move it down to " + (newTop + (oldBottom - newTop)));
                        //newTop += oldBottom - newTop;
                        thisLabel.setDistFromStart(thisLabel.getDistFromStart() - (oldBottom - newTop)/element.getSpan());
                    }
                    else if (oldTop >= newTop  && oldBottom <= newBottom) {
                        Log.i(TAG, "Overlap found for label " + name + " with ID #" + thisLabel.getId() + ": its position " + newTop + " overlaps with label for category " + l.getTxt() + " with ID #" + l.getId() +
                                " which spans from " + oldTop + " to " + oldBottom + " and has linecount " + l.getTextLayout().getLineCount() +
                                ". Adding " + (newBottom - oldTop) + " to this lbl which will move it down to " + (newTop + (newBottom - oldTop)));
                        //newTop += newBottom - oldTop;
                        thisLabel.setDistFromStart(thisLabel.getDistFromStart() - (newBottom - oldTop)/element.getSpan());
                    }
                    else {
                        Log.i(TAG, "NONE triggered, but calling layOutSubcatLabel()");
                        return false;
                    }

                    //adjust this lbl's y starting position by adding the overlap distance

                    //thisLabel.setY(newTop); //FIXME
                    //iter.remove();
                    layOutSubcatLabel(thisLabel, false);
                    return true; //return true as long as there was some adjustment
                }

                //OLD IMPLEMENTATION
                /*
                if ((l.getPt().y <= y && y < l.getPt().y + l.getHt()) || ((y + thisLabel.getHt() > l.getPt().y && y + thisLabel.getHt() <= l.getPt().y + l.getHt()))) { //strictly less than since it's okay for next lbl to start right where prev lbl ends
                    //if the labels are the same, just remove duplicate lbl from the list
                    if (Objects.equals(l.getTxt(), name)) {
                        Log.i(TAG, "Duplicated label found: label " + name + " with ID " + thisLabel.getId());
                        thisLabel.setShow(false);
                        return false;
                    }

                    Log.i(TAG, "Overlap found for label " + name + " with ID #" + thisLabel.getId() + ": its position " + y + " overlaps with label for category " + l.getTxt() + " with ID #" + l.getId() +
                            " which spans from " + l.getPt().y + " to " + (l.getPt().y + l.getHt()) + " and has linecount " + l.getTextLayout().getLineCount() +
                            ". Adding " + (l.getPt().y + l.getHt() - y) + " to this lbl which will move it down to " + (y + (l.getPt().y + l.getHt() - y)));

                    //adjust this lbl's y starting position by adding the overlap distance
                    y += l.getPt().y + l.getHt() - y;
                    thisLabel.setY(y);
                    return true; //return true as long as there was some adjustment
                }*/
            }
        }

        //subCatLabels.add(thisLabel);
        return false; //checked against all existing lbls in same aisle and side, and no issues
    }

    /**
     * This function adjusts the subcategory labels so that none are overlapping, making the map easier to read. These subcategory labels are originally annotated by a human annotator.
     * This function will modify the PointF objs of each label to change their coordinates appropriately.
     */
    public void adjustSubCatLabels() {
        //make copy of the subCatLabels list
        //ArrayList<SubcatLabel> lbls = new ArrayList<SubcatLabel>(subCatLabels);

        ListIterator<SubcatLabel> iter = subCatLabels.listIterator();
        SubcatLabel thisLabel;

        /*
        for (SubcatLabel thisLabel : lbls) {
            //adjust the position of the subcat label until it's not overlapping any other labels (hopefully pos isn't adjusted too much)
            while(checkOverlapWithOtherLabels(thisLabel));

            //if this label was marked for deletion due to being a duplicate, delete it from the subcat labels list
            if (!thisLabel.getShow()) {
                iter.remove();
            }
        }*/


        //for each subcat label, go through and see if any other label will overlap with it
        while (iter.hasNext()) {
            thisLabel = iter.next();

            //adjust the position of the subcat label until it's not overlapping any other labels (hopefully pos isn't adjusted too much)
            while(checkOverlapWithOtherLabels(thisLabel));

            //if this label was marked for deletion due to being a duplicate, delete it from the subcat labels list
            if (!thisLabel.getShow()) {
                iter.remove();
            }
        }
    }

    private void layOutSubcatLabel(SubcatLabel l, boolean insert) {
        //Log.i(TAG, "layOutSubcatNames() called for lbl subcat " + l.getTxt() + ", element " + l.getElement() + ", side " + l.getSide() + ", dir " + l.getDir());

        ArrayList<SSWhalley.StoreElement> elements = ssWhalley.getRectList();

        StaticLayout.Builder builder;
        StaticLayout mTextLayout;
        float lblHeight, lblWidth;

        float x = 0, y = 0, bottom, len, width;
        float rot = 0;

        String text = Constants.showId ? l.getTxt() + " (" + l.getId() + ")" : l.getTxt();
        //Log.i(TAG, "text is " + text);

        //create the text layout for this subcat label
        builder = StaticLayout.Builder.obtain(text, 0, text.length(), subCatTextPaint, (int)Math.min(subCatTextPaint.measureText(text), Constants.subCatNameTextWidth));
        mTextLayout = builder.build();

        //get ht in pixels of the StaticLayout we created for the subcat label text
        lblHeight = mTextLayout.getHeight();
        lblWidth = mTextLayout.getWidth();

        //args: left, top, rt, bottom
        //create rect that represents the bounds of the label
        RectF lblBounds;
        Path lblBoundsPath = new Path();

        SSWhalley.StoreElement e = l.getElement();

        //find x center coord of aisle
        RectF thisRect = e.getRect();

        //if element is vert, want labels to go left/right
        if (Objects.equals(e.getOrientation(), "v")) {
            x = thisRect.centerX();
            len = thisRect.height();

            if (Objects.equals(l.getSide(), "n")) {
                //lbl lies on north (left) side of store element, so justify it right to aisle centerX
                x -= lblWidth;
            }
            if (Objects.equals(l.getDir(), "e")) {
                //travelling upward on map
                //y val should be aisle bottom - distance up the aisle where the prod is located
                y = thisRect.bottom - (len * l.getDistFromStart());
            }
            else {
                //travelling downward on map
                y = thisRect.top + (len * l.getDistFromStart());
            }
        }

        //else labels should go up/down
        else {
            rot += -90f;
            y = thisRect.centerY();
            width = thisRect.width();

            if (Objects.equals(l.getSide(), "w")) {
                //lbl lies on west (lower) side of store element, so justify it right to aisle centery

                y += lblWidth;

                if (Objects.equals(l.getDir(), "n")) {
                    //travelling left on map
                    //x val should be aisle right - dist
                    x = thisRect.right - (width * l.getDistFromStart());
                }
                else {
                    //travelling right on map
                    //x val should be aisle left + dist
                    x = thisRect.left + (width * l.getDistFromStart());
                }
            }


            else {
               //lbl lies on east (upper) side of store element, so leave it left-justified to aisle centery
                if (Objects.equals(l.getDir(), "n")) {
                    //travelling left on map
                    //x val should be aisle right - dist
                    x = thisRect.right - (width * l.getDistFromStart());
                }
                else {
                    //travelling right on map
                    //x val should be aisle left + dist
                    x = thisRect.left + (width * l.getDistFromStart());
                }
            }

        }

        float[] pts = new float[2];

        pts[0] = x;
        pts[1] = y;

        lblBounds = new RectF(x, y, x + lblWidth, y + lblHeight);

        //rotate the label drawing point around the center of the store fixture rect
        ptsTransform.setRotate(e.getRot(), thisRect.centerX(), thisRect.centerY());
        ptsTransform.mapPoints(pts);

        PointF pt = new PointF(pts[0], pts[1]);

        ptsTransform.reset();

        //rotate each label's bounding rect by -90 if the fixture is horizontal
        ptsTransform.setRotate(rot, lblBounds.left, lblBounds.top);
        ptsTransform.mapRect(lblBounds);

        //you can't use mapRect() to actually rotate a Rect, since a Rect always has to have its edges aligned with the x/y axes
        //to rotate a rect off the axes, need to rotate each of the Rect's four corner pts separately
        float[] rectCorners = {
                lblBounds.left, lblBounds.top, //left, top
                lblBounds.right, lblBounds.top, //right, top
                lblBounds.right, lblBounds.bottom, //right, bottom
                lblBounds.left, lblBounds.bottom//left, bottom
        };

        ptsTransform.reset();
        ptsTransform.setRotate(e.getRot(), thisRect.centerX(), thisRect.centerY());
        ptsTransform.mapPoints(rectCorners);

        //reconstruct the rotated rect
        lblBoundsPath.moveTo(rectCorners[0], rectCorners[1]);
        lblBoundsPath.lineTo(rectCorners[2], rectCorners[3]);
        lblBoundsPath.lineTo(rectCorners[4], rectCorners[5]);
        lblBoundsPath.lineTo(rectCorners[6], rectCorners[7]);
        lblBoundsPath.close();

        //Log.i(TAG, "subcatlabel x val is " + x + ", y val is " + y);

        //add rotation of the store fixture
        rot += e.getRot();

        //Log.i(TAG, "Creating new subcatlabel with name " + name + " and yval " + pt.y);

        l.setPt(pt);
        l.setHt(lblHeight);
        l.setBounds(lblBoundsPath);
        l.setTextLayout(mTextLayout);
        l.setRot(rot);
        l.setShow(true);

        if (insert) {
            //add this subcat label to the labels arraylist so that it's drawn in onDraw()
            //note that the PointF of this label is the actual final loc where lbl was drawn
            subCatLabels.add(l);
        }
    }

    private void loadSubcatLabels() {
        //need to examine the db, fetch coords for each
        //instantiate new DBManager object and open the db
        dbManager = new DBManager(getContext());
        dbManager.open();

        //get cursor to read the db, advancing to first entry
        Cursor cursor = dbManager.fetch(DatabaseHelper.SUBCAT_LOC_TABLE_NAME, new Query(), null);
        SubcatLabel l;

        SSWhalley.StoreElement elemObj;

        if (cursor != null) {
            //move cursor to first row of table
            if (cursor.moveToFirst()) {
                //iterate over all rows in the subcats location table
                do {
                    //get the indices of the table cols
                    int subCatIdColIdx = cursor.getColumnIndex(DatabaseHelper.SUBCAT_ID);
                    int nameColIdx = cursor.getColumnIndex(DatabaseHelper.SUBCAT_NAME);
                    int aisleColIdx = cursor.getColumnIndex(DatabaseHelper.ELEMENT_NAME);
                    int sideColIdx = cursor.getColumnIndex(DatabaseHelper.SIDE);
                    int distFromStartColIdx = cursor.getColumnIndex(DatabaseHelper.DIST_FROM_START);
                    int idColIdx = cursor.getColumnIndex(DatabaseHelper._ID);
                    int dirColIdx = cursor.getColumnIndex(DatabaseHelper.DIR);


                    if (subCatIdColIdx >= 0 && nameColIdx >= 0 && aisleColIdx >= 0 && sideColIdx >=0 && distFromStartColIdx >= 0 && idColIdx >= 0 && dirColIdx >= 0) {
                        int subCatId = cursor.getInt(subCatIdColIdx);
                        String subCatName = cursor.getString(nameColIdx);
                        String element = cursor.getString(aisleColIdx);
                        String side = cursor.getString(sideColIdx);
                        float distFromStart = cursor.getFloat(distFromStartColIdx);
                        int id = cursor.getInt(idColIdx);
                        String dir = cursor.getString(dirColIdx);

                        elemObj = ssWhalley.getElementByName(element);

                        l = new SubcatLabel(id, subCatId, subCatName, elemObj, side, distFromStart, dir);
                        layOutSubcatLabel(l, true);
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
            canvas.save();

            canvas.translate(l.getPt().x, l.getPt().y);
            canvas.rotate(l.getRot());

            if (l.getShow()) {
                //Log.i(TAG, "Drawing subcat label " + l.getTextLayout().toString() + " at point " + l.getPt());
                l.getTextLayout().draw(canvas);
                //canvas.drawCircle(l.getPt().x, l.getPt().y, 2, dotPaint);
            }
            canvas.restore();
            canvas.drawPath(l.getBounds(), debugRectPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float rot;
        super.onDraw(canvas);

        //save canvas state before proceeding
        canvas.save();

        //completely replace current canvas' transformation matrix with specified matrix. If the matrix param is null, then current matrix is reset to identity
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
            rot = elements.get(i).getRot();

            if (rot != 0.00f) {
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

        //Log.i(TAG, "Drawing centroid (local) at point (" + xCtr + ", " + yCtr + ")");
        //Log.i(TAG, "Drawing centroid (camera) at point (" + xCameraCtr + ", " + yCameraCtr + ")");

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

        /*
        for (PointF p : ptsToDraw) {
            canvas.drawCircle(p.x, p.y, 3, subCatTextPaint);
        }


        Log.i(TAG, "node data len is " + mGraph.getData().size());
        for (Node n : mGraph.getData()) {
            canvas.drawRect(n.getCellBounds(), cellPaint);
        }*/


        /* DRAW ALL EDGES IN GRAPH FOR DEBUG
        ArrayList<ArrayList<Integer>> adjacencyLists = mGraph.getAdjacencyLists();
        int currId = 0;
        ArrayList<Node> data = mGraph.getData();

        for (ArrayList<Integer> list : adjacencyLists) {
            for (Integer id : list) {
                //draw the edge
                canvas.drawLine(data.get(currId).getCellBounds().centerX(), data.get(currId).getCellBounds().centerY(),
                        data.get(id).getCellBounds().centerX(), data.get(id).getCellBounds().centerY(), cellPaint);
            }

            currId++;
        }
         */


        /*
        for (Integer i : nodesToHit) {
            colorNode(mGraph.getData().get(i), canvas);
        }*/

        /*
        for (ArrayList<Integer> backTrackedPath : backTrackedShortestPaths) {
            colorShortestPath(backTrackedPath, canvas);
        }*/


        colorPath(canvas);

        /*
        ArrayList<Node> data = mGraph.getData();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getCellBounds() != null) {
                canvas.drawText(String.valueOf(i), data.get(i).getCellBounds().left, data.get(i).getCellBounds().centerY(), subCatTextPaint);
            }
        }*/

        //draw the grid
        drawGrid(canvas);

        drawUserLocDot(canvas);

        //return canvas to state it was in upon entering onDraw()
        canvas.restore();
    }

    //fill a node (map grid cell) with some color
    public void colorNode(Node node, nodeColor color, Canvas canvas) {
        RectF cellBounds = node.getCellBounds();
        int idx = 0;
        float squareWidth = Constants.cellWidth / 3;

        if (cellBounds != null) {
            switch(color) {
                case RED:
                    cellPaint.setColor(Color.RED);
                    canvas.drawRect(cellBounds, cellPaint);
                    break;
                case GREEN:
                    cellPaint.setColor(Color.GREEN);
                    canvas.drawRect(cellBounds, cellPaint);
                    break;
                case CHECKED:
                    for (float left = cellBounds.left; left + squareWidth <= cellBounds.right + 0.1; left += squareWidth) {
                        for (float top = cellBounds.top; top + squareWidth <= cellBounds.bottom + 0.1; top += squareWidth) {
                            if (idx % 2 == 0) {
                                cellPaint.setColor(Color.BLACK);
                            }
                            else {
                                cellPaint.setColor(Color.WHITE);
                            }

                            canvas.drawRect(new RectF(left, top, left + squareWidth, top + squareWidth), cellPaint); //left, top, rt, bottom

                            idx++;
                        }
                    }
            }


        }
        else {
            Log.i(TAG, "colorNode: this node's cellBounds RectF is null!! So it's an obstructed cell.");
        }
    }

    public void colorShortestPath(ArrayList<Integer> backTrackedPath, Canvas canvas) {
        for (Integer i : backTrackedPath) {
            //color in the nodes
            colorNode(mGraph.getData().get(i), nodeColor.RED, canvas);
        }
    }

    public void calibrateUserPixelLocToWaypointCell() {
        //get the full path from this node to the next
        ArrayList<Integer> path = fetchBacktrackedPathBetweenNodes(finalNodeOrdering.get(pathIdx), finalNodeOrdering.get(pathIdx + 1));

        //get the last node
        Integer lastNode = path.get(path.size() - 1);

        RectF rect = mGraph.getData().get(lastNode).getCellBounds();

        float x = rect.centerX();
        float y = rect.centerY();

        setUserPixelLocInit(x, y);
        mainActivity.mLocationService.resetPos();
    }

    public void drawUserLocDot(Canvas canvas) {
        float xPos = (float)CurrentUserPosition.getCurrXPos();
        float yPos = (float)CurrentUserPosition.getCurrYPos();

        //scale the actual meter vals down to pixel vals
        float scaledXPos = xPos * userPixelLocXRatio;
        float scaledYPos = yPos * userPixelLocYRatio;

        canvas.drawCircle(userPixelLocXInit + scaledXPos, userPixelLocYInit - scaledYPos, 7, userLocPaint);
    }

    public void setUserPixelLocInit(float x, float y) {
        userPixelLocXInit = x;
        userPixelLocYInit = y;
    }



    public void setPathIdx(int newIdx) {
        this.pathIdx = newIdx;
    }

    public int getPathIdx() {
        return this.pathIdx;
    }

    public void colorPath(Canvas canvas) {
        ArrayList<Integer> path;
        nodeColor color;

        if (pathIdx < finalNodeOrdering.size() - 1 && pathIdx >= 0) {
            //get the full path from this node to the next
            path = fetchBacktrackedPathBetweenNodes(finalNodeOrdering.get(pathIdx), finalNodeOrdering.get(pathIdx + 1));

            if (path != null) {
                //FIXME: why is this here?
                finalRoute.add(path);

                for (int i = 0; i < path.size(); i++) {
                    if (i == 0) {
                        color = nodeColor.GREEN;
                    }
                    else if (i == path.size() - 1) {
                        color = nodeColor.CHECKED;
                    }
                    else {
                        color = nodeColor.RED;
                    }

                    //color in the nodes
                    colorNode(mGraph.getData().get(path.get(i)), color, canvas);
                }
            }
        }
    }

    public void drawGrid(Canvas canvas) {
        //draw horizontal lines
        for (int i = 0; i < numCellRows; i++)
        {
            canvas.drawLine(0, i * cellHeight, Constants.mapFrameRectWidth, i * cellHeight, gridPaint);
        }

        //draw vertical lines
        for (int i = 0; i < numCellCols; i++)
        {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, Constants.mapFrameRectHeight, gridPaint);
        }
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

    public void processTap(float x, float y) {
        Log.i(TAG, "Tap registered at x " + x + ", and y " + y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        zoomAndPan(ev);

        invalidate();//necessary to repaint the canvas
        return true;
    }

    long timeDown = 0;
    float touchX = 0;
    float touchY = 0;

    //handle all touch events on the store map
    void zoomAndPan(MotionEvent event) {
        //we maintain a state machine for the pan and zoom.
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            //when first finger goes down, get first point (that finger is touching)
            case MotionEvent.ACTION_DOWN:
                timeDown = System.currentTimeMillis();
                touchX = event.getX();
                touchY = event.getY();

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
                long tapDur = event.getEventTime() - timeDown;

                if (tapDur < Constants.tapDurThresh && Utils.euclideanDist(event.getX(), event.getY(), touchX, touchY) < Constants.tapDistThresh) {
                    processTap(event.getX(), event.getY());
                }

                //no break, proceed to next case (so that this functions like an OR statement)
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
                    matrix.postTranslate(event.getX() - touchStartingPt.x, event.getY() - touchStartingPt.y);
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



    public boolean startNav(List<Product> shopList) {
        nodesToHit.clear();

        int subCatId;
        int nodeId;
        RectF cell; //left, top, rt, bottom

        RectF lblBoundingRect = new RectF();

        boolean found = false;

        //come up with list of cells for each product in the shopping list
        for (Product p : shopList) {
            //get subcat id for the prod
            subCatId = p.getSubCatId();
            found = false;

            //find any subcat label with matching subcat ID
            for (SubcatLabel l : subCatLabels) {
                if (l.getSubCatId() == p.getSubCatId()) {
                    l.getBounds().computeBounds(lblBoundingRect, true);
                    cell = MapUtils.convertArbitraryRectToCell(lblBoundingRect, l.getSide(), l.getElement().getRot());

                    Log.i(TAG, "Product " + p.getName() + " matched to existing subcat label " + l.getTxt() + ", label bounding rect bounds are left " + lblBoundingRect.left + ", top " + lblBoundingRect.top + ", rt " + lblBoundingRect.right + ", bottom " + lblBoundingRect.bottom);
                    Log.i(TAG, "Product " + p.getName() + " matched to existing subcat label " + l.getTxt() + ", cell bounds are left " + cell.left + ", top " + cell.top + ", rt " + cell.right + ", bottom " + cell.bottom);

                    //get ID of corresponding node in graph
                    nodeId = MapUtils.convertCellBoundsToNodeId(cell);
                    if (nodeId == -1) {
                        Log.i(TAG, "startNav() ERROR: could not get node ID for subcat lbl " + l.getTxt());
                        return false;
                    }

                    nodesToHit.add(nodeId);
                    found = true;
                    break; //continue to next product in the shopping list
                }
            }

            //if the product's subcat is not in the map db, return false immediately
            if (!found) {
                mainActivity.shoppingLists.get(0).addProblemItem(p);
                return false;
            }
        }

        //append start and end nodes to the end of the nodesToHit list
        addStartAndEndNodes();

        //start shortest path computations
        return computePath();
    }

    public void addStartAndEndNodes() {
        //add the start and end nodes
        RectF start, end; //left, top, rt, bottom

        SSWhalley.StoreElement entrance = ssWhalley.getElementByName("entrance");
        SSWhalley.StoreElement exit = ssWhalley.getElementByName("exit");

        start = new RectF(entrance.getRect().centerX(), entrance.getRect().top - Constants.cellHeight, entrance.getRect().centerX(), entrance.getRect().top);
        start = MapUtils.convertArbitraryRectToCell(start, null, 0);
        startNode = MapUtils.convertCellBoundsToNodeId(start);

        end = new RectF(exit.getRect().centerX(), exit.getRect().top - Constants.cellHeight, exit.getRect().centerX(), exit.getRect().top);
        end = MapUtils.convertArbitraryRectToCell(end, null, 0);
        endNode = MapUtils.convertCellBoundsToNodeId(end);

        //Log.i(TAG, "startnode ID is " + startNode + ", endNode ID is " + endNode);

        nodesToHit.add(startNode);
        nodesToHit.add(endNode);
    }

    //compute Manhattan distance (taxicab distance) between two nodes. Use this as heuristic fxn for A* algorithm
    public float heuristicManhattanDist(Node a, Node b) {
        if (a == null || b == null | a.getCellBounds() == null || b.getCellBounds() == null) {
            return -1f;
        }

        float ax = a.getCellBounds().centerX();
        float ay = a.getCellBounds().centerY();

        float bx = b.getCellBounds().centerX();
        float by = b.getCellBounds().centerY();

        //compute manhattan dist on square grid
        return Math.abs(ax - bx) + Math.abs(ay - by);
    }


    class NodeComparator implements Comparator<Integer> {
        Graph graph;
        public NodeComparator(Graph g) {
            this.graph = g;
        }
        @Override
        public int compare(Integer n1, Integer n2) {
            ArrayList<Node> data = graph.getData();

            //**lower values have higher priority
            return (int)(data.get(n1).getPriority() - data.get(n2).getPriority());
        }
    }


    public boolean computePath() {
        //nodesToHit now contains ID nums of all nodes we need to visit

        shortestPaths.clear();

        PriorityQueue<Integer> frontier = new PriorityQueue<Integer>(new NodeComparator(mGraph));
        Integer currNode;
        ArrayList<Pair<Integer, Integer>> neighbors;
        ShortestPath thisPath;
        float prio, updatedCost, heur;
        ArrayList<Node> data = mGraph.getData();

        //FIRST: find shortest path between each pair of nodes in nodesToHit

        //iterate over all unique pairs of nodes in nodesToHit list
        for (int i = 0; i < nodesToHit.size(); i++) {
            for (int j = i + 1; j < nodesToHit.size(); j++) {
                //"frontier" is the priority queue representing the expanding list of nodes being explored for the shortest path
                frontier.clear();

                Integer srcNode = nodesToHit.get(i);
                Integer dstNode = nodesToHit.get(j);

                //Log.i(TAG, "Computing shortest path between source node " + srcNode + " and dest node " + dstNode);
                thisPath = new ShortestPath(srcNode, dstNode);
                //create new shortest path
                shortestPaths.add(thisPath);

                //to get to source node, we didn't come from any node
                thisPath.addCameFromEntry(srcNode, null);
                thisPath.addDistSoFarEntry(srcNode, 0f);

                //start with just the source node in the PriorityQueue
                frontier.add(srcNode);

                //while there's still at least one node in the queue
                while (!frontier.isEmpty()) {
                    //get node at head of queue
                    currNode = frontier.remove();
                    //Log.i(TAG, "Removed node " + currNode + " from head of queue");

                    //if we popped the destination node off the queue, we're done. the cameFrom entry for the dest node will already exist
                    if (Objects.equals(currNode, dstNode)) {
                        //Log.i(TAG, "REACHED DEST NODE");
                        thisPath.setLen(Objects.requireNonNull(thisPath.getDistSoFar().get(currNode)).intValue());
                        //Log.i(TAG, "Set path len to " + thisPath.getLen() + " for path from src " + srcNode + " to dst " + dstNode);
                        break;
                    }

                    //get all of this node's neighbors
                    neighbors = mGraph.getAdjacencyLists().get(currNode);

                    //iterate through this node's neighbors
                    for (Pair<Integer, Integer> nextNodeCandidate : neighbors) {
                        updatedCost = thisPath.getDistSoFar().get(currNode) + Constants.cellHeight;

                        //if we haven't already visited this node, OR if we have visited this node but now have found some shorter path to it
                        if (!thisPath.getCameFrom().containsKey(nextNodeCandidate.first) || updatedCost < thisPath.getDistSoFar().get(nextNodeCandidate.first)) {
                            thisPath.updateDistSoFarEntry(nextNodeCandidate.first, updatedCost);

                            //in order to favor nodes that are closer to the destination node, compute Manhattan dist between this node and the dst node and use that
                            //as priority value in the queue
                            heur = heuristicManhattanDist(data.get(dstNode), data.get(nextNodeCandidate.first));

                            if (heur == -1f) {
                                return false;
                            }
                            prio = updatedCost + heur;
                            data.get(nextNodeCandidate.first).setPriority(prio);

                            /*Log.i(TAG, "Adding node " + nextNodeCandidate + " to queue with priority: cost to get here is " + updatedCost + " and manhatt dist to dest is " + heuristicManhattanDist(data.get(dstNode), data.get(nextNodeCandidate))
                                    + " which makes total " + prio);*/
                            //add the node to the queue with its priority
                            frontier.add(nextNodeCandidate.first);

                            thisPath.addCameFromEntry(nextNodeCandidate.first, currNode);
                        }
                    }
                }
            }
        }

        //reconstruct the shortest paths and save them
        storeBackTrackedShortestPaths();

        //compute the final route using one of two methods
        return computeRouteOrder();
    }

    public void storeBackTrackedShortestPaths() {
        for (ShortestPath shortestPath : shortestPaths) {
            backTrackedShortestPaths.add(shortestPath.reconstructPath());
        }
    }

    public float fetchShortestDistBetweenNodes(Integer n1, Integer n2) {
        //Log.i(TAG, "fetchShortestDistBetweenNodes(): looking for nodes " + n1 + " and " + n2);

        for (ArrayList<Integer> list : backTrackedShortestPaths) {
            if (list == null) {
                Log.i(TAG, "fetchShortestDistBetweenNodes() ERROR: this backTrackedShortestPaths list is NULL!");
                return -1f;
            }

            if ((Objects.equals(list.get(0), n1) && Objects.equals(list.get(list.size() - 1), n2)) ||
                    (Objects.equals(list.get(0), n2) && Objects.equals(list.get(list.size() - 1), n1))) {
                return (list.size() - 1) * Constants.cellHeight; //first node shouldn't count in distance computation
            }
        }

        //shortest path between these nodes was not computed
        return -1f;
    }

    public ArrayList<Integer> fetchBacktrackedPathBetweenNodes(Integer n1, Integer n2) {
        //Log.i(TAG, "fetchBacktrackedPathBetweenNodes called for nodes " + n1 + ", " + n2);

        for (ArrayList<Integer> list : backTrackedShortestPaths) {
            if (list == null) {
                Log.i(TAG, "fetchBacktrackedPathBetweenNodes() ERROR: this backTrackedShortestPaths list is null!");
            }

            if ((Objects.equals(list.get(0), n1) && Objects.equals(list.get(list.size() - 1), n2))) {
                return list;
            }

            //the path is here but it's the reverse of what we're looking for
            if ((Objects.equals(list.get(0), n2) && Objects.equals(list.get(list.size() - 1), n1))) {
                return Utils.reverseArray(list);
            }
        }

        Log.i(TAG, "fetchBacktrackedPathBetweenNodes returning null!");

        //shortest path between these nodes was not computed
        return null;
    }

    public void constructCompleteGraphOfShortestDists() {
        int dist;
        int currId = 0;

        //for fast indexing into the graph data array, still create a node for every single cell
        for (int top = 0; top < Constants.mapFrameRectHeight; top += Constants.cellHeight) {
            //iterate for all cols
            for (int left = 0; left < Constants.mapFrameRectWidth; left += Constants.cellWidth) {
                if (nodesToHit.contains(currId)) {
                    nodesToHitGraph.addNode(new Node(currId, mGraph.getData().get(currId).getCellBounds()));
                }
                else {
                    nodesToHitGraph.addNode(new Node(currId, null));
                }

                //increment node ID no matter what.
                currId++;
            }
        }


        //iterate over all unique pairs of nodes in nodesToHit list
        for (int i = 0; i < nodesToHit.size(); i++) {
            for (int j = i + 1; j < nodesToHit.size(); j++) {
                Integer n1 = nodesToHit.get(i);
                Integer n2 = nodesToHit.get(j);

                //get shortest dist between these grid cells
                dist = (int)fetchShortestDistBetweenNodes(n1, n2);

                //add edge to the graph
                nodesToHitGraph.addEdge(n1, n2, dist);
            }
        }
    }

    void primsAlgo(MinSpanningTree mst, Graph g) {
        PriorityQueue<Integer> q = new PriorityQueue<Integer>(new NodeComparator(nodesToHitGraph));

        //get source node for the MST
        Node src = g.getData().get(mst.getSrcNode());
        Integer currNode;
        ArrayList<Pair<Integer, Integer>> neighbors;

        Node n;

        if (src != null) {
            src.setPriority(0);

            //add all vertices to the prio queue
            q.addAll(nodesToHit);

            while (!q.isEmpty()) {
                currNode = q.remove();

                neighbors = g.getAdjacencyLists().get(currNode);
                for (Pair<Integer, Integer> nextNodeCandidate : neighbors) {
                    n = g.getData().get(nextNodeCandidate.first);

                    //if we haven't explored this vertex yet, AND distance from currNode to this vertex is less than this vertex's current priority
                    if (q.contains(nextNodeCandidate.first) && nextNodeCandidate.second < n.getPriority()) {
                        //update this vertex's priority with the distance from current node to it
                        n.setPriority(nextNodeCandidate.second);

                        mst.addCameFromEntry(nextNodeCandidate.first, currNode);
                    }
                }
            }
        }
    }

    public boolean computeRouteOrder() {
        float shortestRouteDist = Float.POSITIVE_INFINITY;

        ArrayList<Integer> shortestRoute;
        float thisRouteDist;
        float dist;

        //up to 12 nodes, can just compute permutations of the shortest paths
        if (mainActivity.shoppingLists.get(0).getProdList().size() <= 12) {
            ArrayList<ArrayList<Integer>> out = new ArrayList<ArrayList<Integer>>();

            //remove start and end nodes
            nodesToHit.remove(nodesToHit.size() - 1);
            nodesToHit.remove(nodesToHit.size() - 1);

            //compute possible permutations of the items that need to be found in store
            Utils.permuteArray(nodesToHit, 0, out);

            //out now contains all permutations
            //iterate thru permutations
            for (ArrayList<Integer> list : out) {
                thisRouteDist = 0;

                //add back the start and end nodes
                list.add(0, startNode);
                list.add(endNode);

                //Log.i(TAG, "computeRouteOrder(): this permutation to compute tot dist of is " + list);

                //compute total dist for this candidate route
                for (int i = 0; i < list.size() - 1; i++) {
                    dist = fetchShortestDistBetweenNodes(list.get(i), list.get(i + 1));
                    //Log.i(TAG, "computeRouteOrder(): got route dist of " + dist + " from src " + list.get(i) + " to dst " + list.get(i + 1));

                    if (dist >= 0f) {
                        thisRouteDist += dist;
                    }
                    else {
                        //shortest path between these nodes has not been computed for some reason
                        return false;
                    }
                }

                if (thisRouteDist < shortestRouteDist) {
                    shortestRouteDist = thisRouteDist;
                    finalNodeOrdering = list;
                }
            }

            //Log.i(TAG, out.toString());
            //Log.i(TAG, "finalNodeOrdering is " + finalNodeOrdering);
        }

        //if have more than 12 nodes, computing all permutations will take too long
        //we can use the Minimum Spanning Tree approximation for TSP
        //since our subgraph will always satisfy triangle inequality (all edges on the subgraph are shortest paths between nodes),
        //it can be shown that the total distance of the route output by the approximation algo is never more than twice the cost of best possible output for TSP
        else {
            //remove end node
            nodesToHit.remove(nodesToHit.size() - 1);

            nodesToHitGraph = new Graph();

            //construct the subgraph
            constructCompleteGraphOfShortestDists();

            //now run Prim's on subgraph, using start node as the root
            Integer startNode = nodesToHit.get(nodesToHit.size() - 1);

            MinSpanningTree mst = new MinSpanningTree(startNode);

            primsAlgo(mst, nodesToHitGraph);
        }

        return true;
    }
}
