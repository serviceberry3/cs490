package weiner.noah.groceryguide;

//Representation of a grocery store product
public class Product {
    private int prodId;
    private String name;
    private int subCatId;
    private String subCatName;

    public Product(int id, String name) {
        this.prodId = id;
        this.name = name;
    }

    public Product(int id, String name, int subCatId, String subCatName) {
        this.name = name;
        this.prodId = id;
        this.subCatId = subCatId;
        this.subCatName = subCatName;
    }
}
