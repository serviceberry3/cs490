package weiner.noah.groceryguide;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import weiner.noah.groceryguide.databinding.FragmentShoppingListBinding;

/**
 * A fragment representing a list of Items.
 */
public class ShoppingListFragment extends Fragment implements MenuItem.OnMenuItemClickListener {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private MainActivity mainActivity;
    private int mColumnCount = 1;
    private DatabaseManager dbManager;

    private NavController navController;

    //button for viewing route on map
    private FloatingActionButton fab;

    //SQL cursor
    private Cursor cursor;

    //curr selected view and product for which popup menu was opened
    private View selectedView;
    private Product selectedProd;

    private FragmentShoppingListBinding binding;
    private ShoppingListItemRecyclerViewAdapter adapter;

    private ShoppingList shoppingList;

    private final String TAG = "ShoppingListFragment";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ShoppingListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();

        //arguments can be passed to fragments
        //getArguments() fetches the arguments supplied to setArguments(Bundle), if any
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            Log.i(TAG, "column count passed into ShoppingListFragment is " + mColumnCount);
        }
        else {
            Log.i(TAG, "No args passed to ShoppingListFragment!");
        }
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);

        //instantiate new DBManager object and open the db
        dbManager = new DatabaseManager(getActivity());
        dbManager.open();

        //get first shopping list
        shoppingList = mainActivity.shoppingLists.get(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.i(TAG, "onCreateView() called!");
        View view = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        binding = FragmentShoppingListBinding.inflate(inflater, container, false);

        fab = binding.fab;
        fab.setImageBitmap(Utils.textAsBitmap("GO", 40, Color.BLACK));

        //onclick listener for the fab button
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shoppingList.getSize() > 0) {
                    MapUtils.startNav(navController, getParentFragmentManager(), R.id.action_ShoppingListFragment_to_MapFragment);
                    //Snackbar.make(view, "FAB pressed", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                else {
                    Snackbar.make(view, "Please add some items to your shopping list", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        //set the adapter for the RecyclerView that holds the products list
        Context context = view.getContext();
        RecyclerView recyclerView = binding.shopList;

        //RecyclerView's LayoutManager measures and positions item views w/in a RecyclerView and
        //determines policy for when to recycle item views that are no longer visible
        //here we set the layout manager to be a LinearLayoutManager since we want a list with a single col
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        adapter = new ShoppingListItemRecyclerViewAdapter(shoppingList.getProdList(), this);

        //set adapter for the RecyclerView, using the shopping list at index 0 as the list data
        recyclerView.setAdapter(adapter);

        //should pretty much always return the root (outermost) view here
        return binding.getRoot();
    }

    public void showMenu(View v, int position) {
        PopupMenu popup = new PopupMenu(getContext(), v);

        selectedView = v;
        selectedProd = shoppingList.getProdList().get(position);

        //This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        popup.inflate(R.menu.product_click_menu_v2);
        popup.show();
    }

    public void showProdOnMap() {
        //get the ID of product we want to query
        TextView idText = (TextView) ((ViewGroup) selectedView).getChildAt(0);
        int prodId = Integer.parseInt(idText.getText().toString());

        int subCatId = selectedProd.getSubCatId();

        MapUtils.showProdOnMap(navController, cursor, dbManager, idText, getParentFragmentManager(), subCatId, R.id.action_ShoppingListFragment_to_MapFragment);
    }

    public void removeProduct() {
        //get the ID of product we want to remove
        TextView idText = (TextView) ((ViewGroup) selectedView).getChildAt(0);
        int prodId = Integer.parseInt(idText.getText().toString());

        shoppingList.removeProduct(prodId);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_prod_on_map:
                showProdOnMap();
                return true;
            case R.id.action_remove_prod:
                removeProduct();
                return true;
            default:
                return false;
        }
    }
}