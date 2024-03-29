package weiner.noah.groceryguide;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;

import weiner.noah.groceryguide.databinding.FragmentMapBinding;

public class MapFragment extends Fragment {
    private enum RecyclerViewState {
        OPEN,
        CLOSED
    }

    private RecyclerViewState elementsRecyclerViewState = RecyclerViewState.CLOSED;
    private RecyclerViewState elementContentsRecyclerViewState = RecyclerViewState.CLOSED;

    private final String TAG = "MapFragment";

    private FragmentManager fragmentManager;

    private FragmentMapBinding binding;
    private ElementRecyclerViewAdapter elementsListAdapter, elementContentsListAdapter;

    RecyclerView elementsListRecyclerView, elementContentsListRecyclerView;

    private Button nextPathButton;

    private View v;

    private MainActivity mainActivity;

    private ShoppingList shoppingList;

    private TextView xAccel, yAccel, zAccel, xVel, yVel, zVel, xPos, yPos;

    private int listItemIndex = 0;

    private Integer thisNode;

    private SSWhalley ssWhalley;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get main activity (which this Fragment is used in)
        mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        mainActivity.setMapFragment(this);

        //listen for results sent to this fragment when show item on map button clicked
        getParentFragmentManager().setFragmentResultListener("showItemOnMap", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                int[] idArr = bundle.getIntArray("idArr");

                //add dot to list of dots for each subcat label for the subcat of interest
                for (int i = 0; i < idArr.length; i++) {
                    binding.storeMap.addDot(idArr[i]);
                }

                binding.storeMap.invalidate();
                binding.storeMap.zoomOnSubcatLabels();
            }
        });


        //listen for results sent to this fragment when GO button is clicked on shopping list frag
        getParentFragmentManager().setFragmentResultListener("startNav", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                shoppingList = mainActivity.shoppingLists.get(0);
                listItemIndex = 1;

                int key = bundle.getInt("key");
                if (key == 1) {

                    //start the navigation visualization on the map, ONLY proceeding if full route CAN be computed using the fxns in StoreMap2D
                    if (binding.storeMap.startNav(shoppingList.getProdList())) {
                        //make the next button visible for navigation
                        binding.nextPathButton.setVisibility(View.VISIBLE);

                        //add room at bottom of window for the navigation next button
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.storeMap.getLayoutParams();
                        params.height = Constants.mapCanvModifiedHt;
                        binding.storeMap.setLayoutParams(params);

                        //get ID of next node in the finalNodeOrdering list
                        thisNode = binding.storeMap.finalNodeOrdering.get(listItemIndex);

                        //set textview text
                        binding.pathDescription.setText(getResources().getString(R.string.proceed_to) + binding.storeMap.nodesToHitProductKey.get(thisNode).getName());
                        binding.pathDescription.setVisibility(View.VISIBLE);
                        listItemIndex++;

                        //set path index to 0 and invalidate so that first path is drawn
                        binding.storeMap.setPathIdx(0);
                        binding.storeMap.invalidate();
                    }
                    else {
                        if (shoppingList.getProblemItems().size() > 0) {
                            Snackbar.make(binding.getRoot(), "The product " + shoppingList.getProblemItems().get(0).getName() + " is not in the map database.", Snackbar.LENGTH_SHORT).show();
                        }
                        else {
                            Snackbar.make(binding.getRoot(), "Your route could not be computed.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView() called!!");

        //get the binding for fragment_first and its root view to display
        binding = FragmentMapBinding.inflate(inflater, container, false);
        if (binding == null) {
            Log.i(TAG, "ERROR: binding is NULL in onCreateView()!!");
        }

        v = inflater.inflate(R.layout.fragment_map, container, false);

        ssWhalley = binding.storeMap.getSsWhalley();
        xAccel = binding.accelX;
        yAccel = binding.accelY;
        zAccel = binding.accelZ;
        xVel = binding.velX;
        yVel = binding.velY;
        zVel = binding.velZ;
        xPos = binding.posX;
        yPos = binding.posY;

        //display initial user position
        xPos.setText("x pos (m): " + String.format("%.2f", CurrentUserPosition.getCurrXPos()));
        yPos.setText("y pos (m): " + String.format("%.2f", CurrentUserPosition.getCurrYPos()));

        //when next button clicked, increment the path index and cause the map to be redrawn so that next path is drawn
        //also change the textview to reflect next shopping list item
        binding.nextPathButton.setOnClickListener(view -> {
            //every time the user departs from a waypoint, clear LocationService's position reading to 0, and set the waypoint cell as new initial pixel location of user
            mainActivity.mLocationService.resetPos();
            binding.storeMap.calibrateUserPixelLocToWaypointCell();


            //if the path index is equal to size of route node list - 2, we know we just showed the last path, so the user must have clicked finish
            //clear the path index and set the button view to invisible
            if (binding.storeMap.getPathIdx() == binding.storeMap.getFinalNodeOrdering().size() - 2) {
                binding.storeMap.setPathIdx(-1); //clear any path drawings
                binding.nextPathButton.setVisibility(View.INVISIBLE);

                //reset the storemap view so it takes up entire window
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.storeMap.getLayoutParams();
                params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                binding.storeMap.setLayoutParams(params);

                //set textview to invisible
                binding.pathDescription.setVisibility(View.INVISIBLE);
            }

            //if the path index is equal to size of the route node list - 3, we know we are about to show the last path, so set button text to finish
            else if (binding.storeMap.getPathIdx() == binding.storeMap.getFinalNodeOrdering().size() - 3) { //size-3 must be >=0, since if doing navigation will have at least one item in shopping list, plus entrance and checkout nodes
                binding.nextPathButton.setText(R.string.finish_path_button);

                //still increment path idx, trigger redraw of store map
                binding.storeMap.setPathIdx(binding.storeMap.getPathIdx() + 1);
                binding.storeMap.invalidate();

                binding.pathDescription.setText(getResources().getString(R.string.proceed_to) + "exit");
                listItemIndex++;
            }

            //increment the path index so that the next path is drawn, and trigger redraw of the store map
            else {
                binding.storeMap.setPathIdx(binding.storeMap.getPathIdx() + 1);
                binding.storeMap.invalidate(); //trigger store map redraw

                thisNode = binding.storeMap.finalNodeOrdering.get(listItemIndex);

                binding.pathDescription.setText(getResources().getString(R.string.proceed_to) + binding.storeMap.nodesToHitProductKey.get(thisNode).getName());
                listItemIndex++;
            }
        });

        binding.clearPosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.mLocationService.resetPos();
            }
        });

        //set the adapter for the RecyclerView that holds the products list
        Context context = v.getContext();

        elementsListRecyclerView = binding.elements;
        elementContentsListRecyclerView = binding.elementContents;

        //RecyclerView's LayoutManager measures and positions item views w/in a RecyclerView and
        //determines policy for when to recycle item views that are no longer visible
        //here we set the layout manager to be a LinearLayoutManager since we want a list with a single col
        elementsListRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        elementContentsListRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        elementsListAdapter = new ElementRecyclerViewAdapter(ssWhalley.getElementList(), this);

        //set adapter for the RecyclerView, using the shopping list at index 0 as the list data
        elementsListRecyclerView.setAdapter(elementsListAdapter);

        //should pretty much always return the root (outermost) view here
        return binding.getRoot();
    }

    public void openElementList(SSWhalley.StoreElement element) {
        if (elementsRecyclerViewState == RecyclerViewState.CLOSED) {
            if (binding == null) {
                Log.i(TAG, "ERROR: openElementList(): binding is NULL!!");
            }

            //add room at bottom of window for the navigation next button
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.storeMap.getLayoutParams();
            params.topMargin = Constants.mapCanvTopMarginForScrollView;
            binding.storeMap.setLayoutParams(params);

            binding.elements.setVisibility(View.VISIBLE);

            elementsRecyclerViewState = RecyclerViewState.OPEN;
        }
    }

    public void scrollToElement(SSWhalley.StoreElement e) {
        ArrayList<SSWhalley.StoreElement> elementList = ssWhalley.getElementList();
        for (int i = 0; i < elementList.size(); i++) {
            if (Objects.equals(elementList.get(i).getName(), e.getName())) {
                binding.elements.scrollToPosition(i);
                elementsListAdapter.setHighlightPosition(i);

                openElementContents(e);
            }
        }
    }

    //when user taps a store element in top recyclerview, open list of element contents to browse at bottom of screen
    public void openElementContents(SSWhalley.StoreElement element) {
        elementContentsListAdapter = new ElementRecyclerViewAdapter(element.getContents(), this);

        elementContentsListRecyclerView.setAdapter(elementContentsListAdapter);

        synchronized(elementContentsListRecyclerView) {
            elementContentsListRecyclerView.notify();
        }

        //add room at bottom of window for the navigation next button
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.storeMap.getLayoutParams();
        params.bottomMargin = 300;
        binding.storeMap.setLayoutParams(params);

        binding.elementContents.setVisibility(View.VISIBLE);

        elementContentsRecyclerViewState = RecyclerViewState.OPEN;

        binding.storeMap.setSelectedElement(element);
        binding.storeMap.invalidate();
    }

    public void closeElementInfoLists() {
        elementsRecyclerViewState = RecyclerViewState.CLOSED;
        elementContentsRecyclerViewState = RecyclerViewState.CLOSED;

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.storeMap.getLayoutParams();
        params.height = Constants.mapCanvHeight;
        params.topMargin = 0;
        params.bottomMargin = 0;
        binding.storeMap.setLayoutParams(params);

        binding.elements.setVisibility(View.INVISIBLE);
        binding.elementContents.setVisibility(View.INVISIBLE);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public FragmentMapBinding getBinding() {
        return binding;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}