package weiner.noah.groceryguide;

import java.util.Objects;

/*Convenience class to specify filtering in order to selection only certain rows from SQL table*/
public class QueryArgs {
    private Integer prodId = null;
    private String name = null;
    private String subCatName = null;
    private Integer subCatId = null;

    private String orderByStr = null;

    //constructor for searching by subcat name or prod name
    public QueryArgs(String name, String subCat) {
        this.name = name;
        this.subCatName = subCat;
    }

    //constructor for searching by either subcat ID num or product ID number
    public QueryArgs(String name, int id) {
        if (Objects.equals(name, "prod")) {
            this.prodId = id;
        }
        else if (Objects.equals(name, "subcat")) {
            this.subCatId = id;
        }
    }

    public QueryArgs(int subCatId) {
        this.subCatId = subCatId;
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

    //set how want SQL query results to be ordered
    public void setOrderByStr(String text) {
        this.orderByStr = text;
    }

    public Integer getProdId() {
        return this.prodId;
    }

    public Integer getSubCatId() {
        return this.subCatId;
    }

    public String getName() {
        return this.name;
    }

    public String getSubCatName() {
        return this.subCatName;
    }

    public String getOrderByStr() {
        return this.orderByStr;
    }
}
