package weiner.noah.groceryguide;

import java.util.ArrayList;
import java.util.List;

//A user's shopping list, which contains product objects
public class ShoppingList {
    private final List<Product> products;

    public ShoppingList() {
        products = new ArrayList<Product>();
    }

    public void addProduct(Product prod) {
        products.add(prod);
    }
}
