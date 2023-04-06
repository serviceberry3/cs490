package weiner.noah.groceryguide;

public class Constants {
    public static final String storeName = "Stop & Shop Whalley Ave";
    public static final int catNameTextSize = 2;

    //width of aisle gray blocks
    public static final float aisleWidth = 17.0f;

    //dist between left side of one aisle and left side of the next aisle
    public static final float aisleSpan = 39.0f;

    //width that subcat text can occupy on side of aisle
    public static final int catNameTextWidth = 20;


    public static final String sqlSelectProduct = "SELECT cast(prodId as text), name, cast(aisle as text), cast(rootCatId as text), rootCatName, cast(subCatId as text), subCatName FROM products";
}
