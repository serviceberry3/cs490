package weiner.noah.groceryguide;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import weiner.noah.groceryguide.databinding.FragmentProductsBinding;


public class BrowseProductsFragment extends Fragment implements MenuItem.OnMenuItemClickListener {
    private final String TAG = "BrowseProductsFragment";

    private FragmentProductsBinding binding;

    private DBManager dbManager;

    private ListView listView;

    //product search bar
    private EditText searchBar;

    //curr selected view for which popup menu was opened
    private View selectedView;

    //the SimpleCursorAdapter is a convenience class to map columns from a database cursor to TextViews or ImageViews defined in an XML file
    private SimpleCursorAdapter adapter;

    //SQL cursor
    private Cursor cursor;

    //the table columns that we want to map to the view, with the corresponding layout elements that their text should fill
    final String[] from = new String[] { DatabaseHelper.PROD_ID, DatabaseHelper.NAME, DatabaseHelper.SUBCAT_ID, DatabaseHelper.SUBCAT_NAME };
    final int[] to = new int[] { R.id.prod_id, R.id.prod_name, R.id.subcat_id, R.id.subcat_name };

    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductsBinding.inflate(inflater, container, false);
        listView = binding.listView;
        searchBar = binding.searchBar;
        return binding.getRoot();
    }

    //Called immediately after onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle) has returned,
    // but before any saved state has been restored in to the view
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*
        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this).navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });*/

        //MainActivity mainActivity = (MainActivity) getActivity();

        //instantiate new DBManager object and open the db
        dbManager = new DBManager(getActivity());
        dbManager.open();

        //get cursor to read the db, advancing to first entry
        cursor = dbManager.fetch(DatabaseHelper.PRODS_TABLE_NAME, new Query(), null);

        //by default, listview just displays some text indicating no entries were found
        listView.setEmptyView(binding.empty);

        //SimpleCursorAdapter is easy adapter to map columns from a cursor to TextViews or ImageViews defined in an XML file.
        adapter = new SimpleCursorAdapter(getActivity(), R.layout.product_list_item, cursor, from, to, 0);
        adapter.notifyDataSetChanged();

        listView.setAdapter(adapter);

        //do this when items in the list are tapped
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long viewId) {
                showMenu(view);
            }
        });

        //listen for when search bar text changes.
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //when the text changes, want to search db and update the ListView with contents
                //Snackbar.make(binding.getRoot(), s, Snackbar.LENGTH_LONG).show();
                queryDbWithSearch(s.toString());
            }
        });
    }

    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);

        selectedView = v;

        //This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        popup.inflate(R.menu.product_click_menu);
        popup.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_prod_on_map:
                showProdOnMap();
                return true;
            case R.id.action_add_prod_to_shop_list:
                addProdToList();
                return true;
            default:
                return false;
        }
    }

    public void showProdOnMap() {
        //get the ID of product we want to query
        TextView idText = (TextView) ((ViewGroup) selectedView).getChildAt(0);
        int prodId = Integer.parseInt(idText.getText().toString());

        MapUtils.showProdOnMap(navController, cursor, dbManager, idText, binding.getRoot(), getParentFragmentManager(), prodId, R.id.action_BrowseProductsFragment_to_MapFragment);
    }

    public void addProdToList() {
        MainActivity mainActivity = (MainActivity) getActivity();

        TextView idText = (TextView) ((ViewGroup) selectedView).getChildAt(0);
        TextView nameText = (TextView) ((ViewGroup) selectedView).getChildAt(1);
        TextView subCatIdText = (TextView) ((ViewGroup) selectedView).getChildAt(2);
        TextView subCatNameText = (TextView) ((ViewGroup) selectedView).getChildAt(3);

        //instantiate a new product with ID and name
        Product prod = new Product(Integer.parseInt(idText.getText().toString()), nameText.getText().toString(), Integer.parseInt(subCatIdText.getText().toString()), subCatNameText.getText().toString());

        //for now add the product to the default shopping list, which always sits at index 0
        assert mainActivity != null;
        mainActivity.addProdToShoppingList(0, prod);
    }

    //search the product database by product name and update ListView to only show relevant
    public void queryDbWithSearch(String s) {
        //Log.i(TAG, "querying DB with string " + s);

        //make some QueryArgs
        QueryArgs args = new QueryArgs(s, s);

        //now instantiate a Query obj using the args, and constructc the SQL strings
        Query query = new Query(args);
        query.generateSelection();

        cursor = dbManager.fetch(DatabaseHelper.PRODS_TABLE_NAME, query, null);

        adapter.changeCursor(cursor);
        adapter.notifyDataSetChanged();
        //listView.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
}