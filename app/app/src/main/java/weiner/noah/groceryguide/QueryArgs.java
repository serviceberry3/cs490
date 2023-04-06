package weiner.noah.groceryguide;

/*Convenience class to specify filtering in order to selection only certain rows from SQL table*/
public class QueryArgs {
    private int prodId;
    private String name;
    private String subCatName;

    public QueryArgs(String name, String subCat) {
        this.name = name;
        this.subCatName = subCat;
    }

    public void setProdId(int prodId) {
        this.prodId = prodId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSubCatName(String text) {
        this.subCatName = text;
    }

    public int getProdId() {
        return this.prodId;
    }

    public String getName() {
        return this.name;
    }

    public String getSubCatName() {
        return this.subCatName;
    }
}
