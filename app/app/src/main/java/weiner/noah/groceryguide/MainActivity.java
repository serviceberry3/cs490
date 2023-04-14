package weiner.noah.groceryguide;

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

    NavHostFragment navHostFragment;
    NavController navController;

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
}