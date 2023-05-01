package weiner.noah.groceryguide;

import android.database.Cursor;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

public class MapUtils {
    public static final String TAG = "MapUtils";

    public static void startNav(NavController navController, FragmentManager parentFragmentManager, int navAction) {
        NavWrapper.navigateSafe(navController, navAction, null);

        //make Bundle to be sent to the MapFragment
        Bundle result = new Bundle();

        result.putInt("key", 1);

        //set result which will be picked up by MapFragment
        parentFragmentManager.setFragmentResult("startNav", result);
    }

    public static void showProdOnMap(NavController navController, Cursor cursor, DBManager dbManager, View selectedView, View rootView, FragmentManager parentFragmentManager, int prodId, int navAction) {
        int subCatId = 0;
        int aisle = 0, side = 0;
        float distFromFrontMin = 0, distFromFrontMax = 0;

        QueryArgs args = new QueryArgs("prod", prodId);
        Query q = new Query(args);
        q.generateSelection();

        //get cursor to read the db, advancing to first entry
        cursor = dbManager.fetch(DatabaseHelper.PRODS_TABLE_NAME, q, null);

        //get subcat ID num for this prod
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int subCatIdColIdx = cursor.getColumnIndex("subCatId");

                if (subCatIdColIdx >= 0) {
                    subCatId = cursor.getInt(subCatIdColIdx);
                    //Log.i(TAG, "found subcat ID for product number " + prodId + ": " + subCatId);
                }
                else {
                    Log.i(TAG, "NO subCatId col idx found!");
                }
            }
        }
        else {
            Log.i(TAG, "Cursor is null!!");
        }

        args = new QueryArgs("subcat", subCatId);

        //results will fetch all entries in location table for the specific subCatId we're requesting. sort results by distance from front of aisle in asc order
        args.setOrderByStr("distFromStart ASC");
        q = new Query(args);
        q.generateSelection();

        //now take the subCatId, run query to the location table, get first location of that subcatid
        cursor = dbManager.fetch(DatabaseHelper.SUBCAT_LOC_TABLE_NAME, q, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int aisleColIdx = cursor.getColumnIndex("aisle");
                int sideColIdx = cursor.getColumnIndex("side");
                int distFromFrontColIdx = cursor.getColumnIndex("distFromFront");
                int idColIdx = cursor.getColumnIndex("_id");

                NavWrapper.navigateSafe(navController, navAction, null);

                //count num rows in the cursor
                int cnt = cursor.getCount();
                int[] idArr = new int[cnt];

                int i = 0;

                //iterate through all entries of the subcats location table whose subcat is the one of interest. get location data for each
                do {
                    idArr[i] = cursor.getInt(idColIdx);

                    i++;
                } while (cursor.moveToNext());

                //make Bundle to be sent to the MapFragment
                Bundle result = new Bundle();

                result.putIntArray("idArr", idArr);

                //set result which will be picked up by MapFragment
                parentFragmentManager.setFragmentResult("showItemOnMap", result);
            }
            else {
                Log.i(TAG, "Error: subcat for this prod was found, but NO location entry for that subcat");
                Snackbar.make(rootView, "Location of this product on the map cannot be determined :(", Snackbar.LENGTH_LONG).show();
            }
        }
        else {
            Log.i(TAG, "SQL cursor is null after querying subcat loc table with subcat ID " + subCatId + "!!");
        }
    }

    //convert from subcat label bounds to the bounds of an actual map grid cell
    public static RectF convertArbitraryRectToCell(RectF subCatRect, String side, float rot) {
        float left = 0, top = 0, rt, bottom;

        //snap top to the grid line above
        top = Constants.cellHeight * (Math.floorDiv((long)subCatRect.top, (long)Constants.cellHeight));


        //if right (south) side
        if (Objects.equals(side, "s")) {
            //snap right bound to grid
            left = (float)(Constants.cellWidth * Math.ceil(subCatRect.right / Constants.cellWidth));
        }

        //if left (north) side
        else if (Objects.equals(side, "n")) {
            //snap left bound to grid
            left = (float)(Constants.cellWidth * Math.floorDiv((long)subCatRect.left, (long)Constants.cellWidth));
        }

        //if top (east) side
        else if (Objects.equals(side, "e")) {
            //horizontal fixture, rotation to the left
            if (rot < 0) {
                //snap left bound to grid
                left = (float)(Constants.cellWidth * Math.floorDiv((long)subCatRect.left, (long)Constants.cellWidth));
            }

            //horizontal fixture, rotated to right
            else {
                //snap left bound to grid
                left = (float)(Constants.cellWidth * Math.ceil(subCatRect.right / Constants.cellWidth));
            }
        }

        //if bottom (west) side
        else if (Objects.equals(side, "w")) {
            //horizontal fixture, rotated to left
            if (rot < 0) {
                left = (float)(Constants.cellWidth * Math.ceil(subCatRect.right / Constants.cellWidth));
            }

            //horizontal fixture, rotated to right
            else {
                //snap left bound to grid
                left = (float)(Constants.cellWidth * Math.floorDiv((long)subCatRect.left, (long)Constants.cellWidth));
            }

            top = (float)(Constants.cellHeight * Math.ceil(subCatRect.bottom / Constants.cellHeight));
        }

        //otherwise side should be null, this is a special case where caller assumes responsibility
        else {
            left = Constants.cellWidth * Math.floorDiv((long)subCatRect.left, (long)Constants.cellWidth);
        }

        bottom = top + Constants.cellHeight;
        rt = left + Constants.cellWidth;

        return new RectF(left, top, rt, bottom);
    }

    //convert from RectF to the ID of the cell node
    public static int convertCellBoundsToNodeId(RectF bounds) {
        int row = (int) (bounds.top / Constants.cellHeight);
        int col = (int) (bounds.left / Constants.cellWidth);

        int cand = row * (int)Constants.mapFrameRectNumCellsWide + col;

        if (cand <= Constants.numCells - 1) {
            return cand;
        }

        //STH WENT WRONG
        else {
            Log.i(TAG, "candidate node number " + cand + " is too big");
            return -1;
        }
    }

    //rotate a RectF around its centroid and get the result as a Path
    //you can't use mapRect() to actually rotate a Rect, since a Rect always has to have its edges aligned with the x/y axes
    //to rotate a rect off the axes, need to rotate each of the Rect's four corner pts separately
    public static Path rotateRectToGetPath(RectF rect, float rot, float rotCtrX, float rotCtrY) {
        Path pth = new Path();

        float[] rectCorners = {
            rect.left, rect.top, //left, top
            rect.right, rect.top, //right, top
            rect.right, rect.bottom, //right, bottom
            rect.left, rect.bottom//left, bottom
        };

        Matrix ptsTransform = new Matrix();
        ptsTransform.setRotate(rot, rotCtrX, rotCtrY);
        ptsTransform.mapPoints(rectCorners);

        //reconstruct the rotated rect as a path
        pth.moveTo(rectCorners[0], rectCorners[1]);
        pth.lineTo(rectCorners[2], rectCorners[3]);
        pth.lineTo(rectCorners[4], rectCorners[5]);
        pth.lineTo(rectCorners[6], rectCorners[7]);
        pth.close();

        return pth;
    }
}
