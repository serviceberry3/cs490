package weiner.noah.groceryguide;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

//A user's shopping list, which contains product objects
public class ShoppingList {
    private final List<Product> products;

    //items whose subcategories are NOT in the subcat_loc database (not shown anywhere on map)
    private List<Product> problemItems;

    public ShoppingList() {
        products = new ArrayList<Product>();
        problemItems = new ArrayList<Product>();
    }

    public void addProduct(Product prod) {
        products.add(prod);
    }

    public void removeProduct(int id) {
        //a faster way would probably to use a HashMap instead of List
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getProdId() == id) {
                products.remove(i);
                break;
            }
        }
    }

    public List<Product> getProdList() {
        return products;
    }

    public int getSize() {
        return products.size();
    }
    
    public List<Product> getProblemItems() {
        return this.problemItems;
    }

    public void addProblemItem(Product p) {
        this.problemItems.add(p);
    }
}
