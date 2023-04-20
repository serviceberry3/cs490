package weiner.noah.groceryguide;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import weiner.noah.groceryguide.databinding.ActivityMainBinding;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private Graph mGraph = new Graph();
    private SSWhalley ssWhalley = new SSWhalley();

    NavHostFragment navHostFragment;
    NavController navController;

    //matrix to use for rotating points, etc.
    Matrix canvasMatrixTemp = new Matrix();

    //list of shopping lists
    List<ShoppingList> shoppingLists = new ArrayList<ShoppingList>();

    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getSupportActionBar().setTitle();
        //getSupportActionBar().setSubtitle();

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        //set view to root view
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        //get the NavController
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        //make one default shopping list
        shoppingLists.add(new ShoppingList());

        //create the graph
        createGraph();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void navToMap() {
        int currFrag = Objects.requireNonNull(navController.getCurrentDestination()).getId();

        switch (currFrag) {
            case R.id.BrowseProductsFragment:
                //safely navigate (ie if we're already in the MapFragment, this will fail gracefully)
                NavWrapper.navigateSafe(navController, R.id.action_BrowseProductsFragment_to_MapFragment, null);
                break;
            case R.id.ShoppingListFragment:
                NavWrapper.navigateSafe(navController, R.id.action_ShoppingListFragment_to_MapFragment, null);
                break;
        }
    }

    public void navToProd() {
        //get currently active fragment (the current destination)
        int currFrag = Objects.requireNonNull(navController.getCurrentDestination()).getId();

        switch (currFrag) {
            case R.id.MapFragment:
                //safely navigate (ie if we're already in the MapFragment, this will fail gracefully)
                NavWrapper.navigateSafe(navController, R.id.action_MapFragment_to_BrowseProductsFragment, null);
                break;
            case R.id.ShoppingListFragment:
                NavWrapper.navigateSafe(navController, R.id.action_ShoppingListFragment_to_BrowseProductsFragment, null);
                break;
        }
    }

    public SSWhalley getStoreModel() {
        return this.ssWhalley;
    }

    public Graph getGraph() {
        return this.mGraph;
    }

    public void navToList() {
        int currFrag = Objects.requireNonNull(navController.getCurrentDestination()).getId();

        switch (currFrag) {
            case R.id.MapFragment:
                //safely navigate (ie if we're already in the MapFragment, this will fail gracefully)
                NavWrapper.navigateSafe(navController, R.id.action_MapFragment_to_ShoppingListFragment, null);
                break;
            case R.id.BrowseProductsFragment:
                NavWrapper.navigateSafe(navController, R.id.action_BrowseProductsFragment_to_ShoppingListFragment, null);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle action bar menu item clicks here. The action bar will automatically handle clicks on the Home/Up button, as long as you specify parent activity in AndroidManifest.xml
        int id = item.getItemId();

        switch (id) {
            case R.id.action_view_map:
                navToMap();
                return true;
            case R.id.action_db_browse:
                navToProd();
                return true;
            case R.id.action_view_list:
                navToList();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        //get the fragment element in content_main
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }


    public void addProdToShoppingList(int listIdx, Product prod) {
        shoppingLists.get(listIdx).addProduct(prod);
    }

    public void createGraph() {
        /*PSEUDOCODE:createGraph

         */
        RectF thisRect, elementRect;
        //for store elements that are drawn as rotated rectangles, we'll create a Path to hold the lines delimiting their edges
        //then we'll check to see if certain cells overlap or fall within the bounds created by that Path
        Path rotatedRectPath = new Path(), cellPath = new Path(), res = new Path();
        float rot;
        ArrayList<SSWhalley.StoreElement> elements = ssWhalley.getRectList();

        int currId = 0;

        //is this map cell completely clear of obstacles?
        boolean clear;

        //iterate for all rows
        for (int top = 0; top < Constants.mapFrameRectHeight; top += Constants.cellHeight) {
            //iterate for all cols
            for (int left = 0; left < Constants.mapFrameRectWidth; left += Constants.cellWidth) {
                clear = true;

                //args: left, top, rt, bottom
                //thisRect is the rect representing the map cell that we're currently examining
                thisRect = new RectF(left, top, left + Constants.cellWidth, top + Constants.cellHeight);

                //iterate thru the store elements
                for (SSWhalley.StoreElement element : elements) {
                    //ignore the outer frame of the map
                    if (!Objects.equals(element.getId(), "frame")) {
                        rot = element.getRot();
                        elementRect = element.getRect();

                        //if some rotation is applied, need to find new points
                        if (rot != 0.00f) {
                            //Log.i(TAG, "HAS ROT");
                            //get the original four corner coords of this store element
                            float[] corners = {
                                    elementRect.left, elementRect.top, //left, top
                                    elementRect.right, elementRect.top, //right, top
                                    elementRect.right, elementRect.bottom, //right, bottom
                                    elementRect.left, elementRect.bottom //left, bottom
                            };

                            //rotate the corner points appropriately
                            canvasMatrixTemp.setRotate(rot, elementRect.centerX(), elementRect.centerY());
                            canvasMatrixTemp.mapPoints(corners);

                            //add the cell bounds to the path
                            rotatedRectPath.reset();
                            rotatedRectPath.moveTo(corners[0], corners[1]);
                            rotatedRectPath.lineTo(corners[2], corners[3]);
                            rotatedRectPath.lineTo(corners[4], corners[5]);
                            rotatedRectPath.lineTo(corners[6], corners[7]);
                            cellPath.reset();
                            cellPath.addRect(thisRect, Path.Direction.CCW);

                            //check if the cell path and the store element path intersect
                            res.reset();
                            if (res.op(rotatedRectPath, cellPath, Path.Op.INTERSECT)) {
                                //the two paths did intersect, set clear to false
                                if (!res.isEmpty()) {
                                    clear = false;
                                }
                            }
                        }

                        //otherwise, just use the Rect intersects method to check if cell intersects w/store element (obstacle)
                        else {
                            //Log.i(TAG, "CHECKING INTERSECT NONROT");

                            //otherwise not looking at a rotated rectangle. simply check if thisRect intersects with elementRect
                            if (RectF.intersects(thisRect, elementRect)) {
                                //Log.i(TAG, "cell rect with bounds top " + thisRect.top + " and left " + thisRect.left + " intersects element rect with bounds top " + elementRect.top + " and left " + elementRect.left);

                                //Log.i(TAG, "non-rotated store element rect intersects this cell, SETTING CLEAR FALSE");
                                clear = false;
                            }
                        }
                    }
                }

                //if it doesn't touch any obstacles, add a node to the graph with its Rect
                if (clear) {
                    //Log.i(TAG, "ADDING NODE");
                    //now we can add the cell as a node in the graph
                    mGraph.addNode(new Node(currId, thisRect));
                }

                //if the node is invalid, add it but set its Rect to null so we'll know it's invalid
                else {
                    mGraph.addNode(new Node(currId, null));
                }

                //increment node ID no matter what.
                currId++;
            }
        }


        currId = 0;
        ArrayList<Integer> possibleNeighbors = new ArrayList<Integer>();

        /*PSEUDOCODE:
        //again, iterate through all rows and all cols of the grid.
        //at each cell, check if it is a node of the graph. if it is, compute the node ID's of all of its possible neighbors.
        //go through that list of ID's and check if each is a node of the graph. if it is, add the appropriate edge to this node's adjacency list
         */
        int lastColLeft = (int)(Constants.mapFrameRectWidth - Constants.cellWidth);
        int lastRowTop = (int)(Constants.mapFrameRectHeight - Constants.cellHeight);

        for (int top = 0; top < Constants.mapFrameRectHeight; top += Constants.cellHeight) {
            for (int left = 0; left < Constants.mapFrameRectWidth; left += Constants.cellWidth) {
                possibleNeighbors.clear();

                if (mGraph.containsNode(currId)) {
                    //CASE 1: upper left corner, two possible neighbors
                    if (left == 0 && top == 0) {
                        possibleNeighbors.add(currId + (int)Constants.mapFrameRectNumCellsWide);
                        possibleNeighbors.add(currId + 1);
                    }

                    //CASE 2: upper rt corner, two possible neighbors
                    else if (left == Constants.mapFrameRectWidth - Constants.cellWidth && top == 0) {
                        possibleNeighbors.add(currId + (int)Constants.mapFrameRectNumCellsWide);
                        possibleNeighbors.add(currId - 1);
                    }

                    //CASE 3: lower rt corner, two possible neighbors
                    else if (left == lastColLeft && top == lastRowTop) {
                        possibleNeighbors.add(currId - (int)Constants.mapFrameRectNumCellsWide);
                        possibleNeighbors.add(currId - 1);
                    }

                    //CASE 5: lower left corner, two possible neighbors
                    else if (left == 0 && top == lastRowTop) {
                        possibleNeighbors.add(currId - (int)Constants.mapFrameRectNumCellsWide);
                        possibleNeighbors.add(currId + 1);
                    }

                    //CASE 6: upper edge, three possible neighbors
                    else if (top == 0) {
                        possibleNeighbors.add(currId + (int) Constants.mapFrameRectNumCellsWide);
                        possibleNeighbors.add(currId + 1);
                        possibleNeighbors.add(currId - 1);
                    }

                    //CASE 7: rt edge, three possible neighbors
                    else if (left == lastColLeft) {
                        possibleNeighbors.add(currId + (int) Constants.mapFrameRectNumCellsWide);
                        possibleNeighbors.add(currId - (int) Constants.mapFrameRectNumCellsWide);
                        possibleNeighbors.add(currId - 1);
                    }

                    //CASE 8: lower edge, three possible neighbors
                    else if (top == lastRowTop) {
                        possibleNeighbors.add(currId - (int) Constants.mapFrameRectNumCellsWide);
                        possibleNeighbors.add(currId - 1);
                        possibleNeighbors.add(currId + 1);
                    }

                    //CASE 9: left edge, three possible neighbors
                    else if (left == 0) {
                        possibleNeighbors.add(currId + (int) Constants.mapFrameRectNumCellsWide);
                        possibleNeighbors.add(currId - (int) Constants.mapFrameRectNumCellsWide);
                        possibleNeighbors.add(currId + 1);
                    }

                    //CASE 10: else, four possible neighbors
                    else {
                        possibleNeighbors.add(currId + (int) Constants.mapFrameRectNumCellsWide);
                        possibleNeighbors.add(currId - (int) Constants.mapFrameRectNumCellsWide);
                        possibleNeighbors.add(currId + 1);
                        possibleNeighbors.add(currId - 1);
                    }

                    for (Integer id : possibleNeighbors) {
                        if (mGraph.containsNode(id)) {
                            mGraph.addEdge(currId, id);
                        }
                    }
                }

                currId++;
            }
        }
    }

}