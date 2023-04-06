package weiner.noah.groceryguide;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import weiner.noah.groceryguide.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    NavHostFragment navHostFragment;
    NavController navController;

    //list of shopping lists
    List<ShoppingList> shoppingLists = new ArrayList<ShoppingList>();

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

        /*
        //onclick listener for the fab button
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "FAB pressed", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle action bar menu item clicks here. The action bar will automatically handle clicks on the Home/Up button, as long as you specify parent activity in AndroidManifest.xml
        int id = item.getItemId();

        if (id == R.id.action_view_map) {
            NavWrapper.navigateSafe(navController, R.id.action_BrowseProductsFragment_to_MapFragment, null);

            return true;
        }

        else if (id == R.id.action_db_browse) {
            //safely navigate (ie if we're already in the MapFragment, this will fail gracefully)
            NavWrapper.navigateSafe(navController, R.id.action_MapFragment_to_BrowseProductsFragment, null);

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