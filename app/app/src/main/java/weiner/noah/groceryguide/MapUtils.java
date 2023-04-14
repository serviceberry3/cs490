package weiner.noah.groceryguide;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;

import com.google.android.material.snackbar.Snackbar;

public class MapUtils {
    public static final String TAG = "MapUtils";

    public static void startNav(NavController navController, Cursor cursor, DBManager dbManager, View rootView, FragmentManager parentFragmentManager, int navAction) {
        NavWrapper.navigateSafe(navController, navAction, null);
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
        args.setOrderByStr("distFromFront ASC");
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
                parentFragmentManager.setFragmentResult("drawDot", result);
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
}
