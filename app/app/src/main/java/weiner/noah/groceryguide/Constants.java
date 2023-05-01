package weiner.noah.groceryguide;

public class Constants {
    public static final String storeName = "Stop & Shop Whalley Ave";

    //width, ht of cells for the graph
    public static final float cellWidth = 5f;
    public static final float cellHeight = 5f;
    public static final float subCatNameTextSize = 1.5f;

    //how much to stretch subcat label text on x axis
    public static final float subCatTextXScale = 1.5f;

    //width of aisle gray blocks
    public static final float aisleWidth = cellWidth * 4;

    //dist between left side of one aisle and left side of the next aisle
    public static final float aisleSpan = aisleWidth * 2;

    //dist between left side of one aisle and rt side of the next aisle
    public static final float aisleSpacing = aisleSpan - aisleWidth;

    //width that subcat text can occupy on side of aisle
    public static final float subCatNameTextWidth = (aisleSpacing / 2) + (aisleWidth / 2); //padding of 3??

    public static final float dotsRad = 1f;

    //add some space between end of label and dot centroid
    public static final float dotsPadding = 2f;

    public static final String sqlSelectProduct = "SELECT cast(prodId as text), name, cast(aisle as text), cast(rootCatId as text), rootCatName, cast(subCatId as text), subCatName, subCatId FROM products";

    public static final float mapTopMargin = 400f;

    //the whole map canvas covers the entire screen (besides the action bar)
    public static final float mapCanvHeight = 1999f;
    public static final float mapCanvWidth = 1080f;

    public static final float mapFrameRectHeight = 900f;
    public static final float mapFrameRectWidth = 1080f;

    public static final float mapFrameRectNumCellsTall = mapFrameRectHeight / cellHeight;
    public static final float mapFrameRectNumCellsWide = mapFrameRectWidth / cellWidth;
    public static final float numCells = mapFrameRectNumCellsTall * mapFrameRectNumCellsWide;
    public static final float mapFrameRectCtrX = mapFrameRectWidth / 2;
    public static final float mapFrameRectCtrY = mapFrameRectHeight / 2;

    public static final float mapCanvCtrX = mapCanvWidth / 2;
    public static final float mapCanvCtrY = mapCanvHeight / 2;

    //width:height ratio of the whole map canvas
    public static final float mapCanvAspectRatioWH = mapCanvWidth / mapCanvHeight;

    //stroke width of rect that surrounds "show on map" zone
    public static final float zoneRectStrokeWidth = 1;

    //show the label ID next to the subcat lbls?
    public static final boolean showId = false;

    //how visible should the grid be?
    public static final int gridTransparency = 200;

    public static final float zoomRectBottomPad = 1;
    public static final float zoneRectYPad = 1;

    public static final float zoneRectXPad = 1;


    //FOR LOCATIONSERVICE
    public static final float NANOSEC_TO_SEC = 1.0f / 1000000000.0f;
    public static final float MAX_ACC = 20.0f;

    public static final float IN_MOTION_STD_THRESH = 0.8f;

    //parameter for low-pass filter
    public static final float LOW_PASS_ALPHA = 0.95f;

    //adjustable parameter that scales up velocity when integrating position
    public static final int VELOCITY_AMPL_DEFAULT = 1;

    public static final float VELOCITY_FRICTION_DEFAULT = 0.0f;
    public static final float POSITION_FRICTION_DEFAULT = 0.0f;

    //approximate heading when facing directly towards the main entrance of Stop & Shop
    public static final float SS_WHALLEY_HEADING = 116f;

    public static final float SS_WHALLEY_ACTUAL_WIDTH = 82f; //in meters
    public static final float SS_WHALLEY_ACTUAL_HT = 49f; //in meters

    public static final float tapDurThresh = 50;
    public static final float tapDistThresh = 20;

    public static final int maxShoppingListSzForPermutationMethod = 10;
}
