package weiner.noah.groceryguide;

import java.util.ArrayList;
import java.util.List;

//An SQL database query
public class Query {
    private QueryArgs queryArgs = null;

    public final ArrayList<String> colsToFilter = new ArrayList<String>();

    private String orderBy = null;
    private List<String> preparedArgs = null;

    private StringBuilder where = null;

    public Query() {
        //no filter, so select all rows from table
    }

    public Query(QueryArgs args) {
        this.queryArgs = args;

        //this can be changed based on which columns we want to filter for the query
        colsToFilter.add(DatabaseHelper.NAME);
        colsToFilter.add(DatabaseHelper.SUBCAT_NAME);
    }

    public String getOrderBy() {
        return this.orderBy;
    }

    public List<String> getPreparedArgs() {
        return this.preparedArgs;
    }

    public List<String> getColsToFilter() {
        return this.colsToFilter;
    }

    public String getWhereSelectionStatement() {
        return where == null ? null : where.toString();
    }

    //generate the query strings required to plug into SQLiteDatabase.query()
    public void generateSelection() {
        this.preparedArgs = new ArrayList<String>();

        //general SQL query statement is contained in Constants

        //generate WHERE SQL prepared statement portion, which is specific to the certain filters we want to use for each table column
        where = new StringBuilder();

        int i = 0;

        for (String colName : colsToFilter) {
            where.append(colName).append(" LIKE ? ");

            //insert OR unless we're on the last filter name
            if (i != colsToFilter.size() - 1) {
                where.append("OR ");
            }

            i++;
        }

        //generate the prepared statement arguments (to be used as selectionArgs parameter in SQLiteDatabase.query())
        preparedArgs.add("%" + queryArgs.getName() + "%");
        preparedArgs.add("%" + queryArgs.getSubCatName() + "%");

        //select how to order results
        orderBy = "subCatId ASC, aisle ASC, rootCatId ASC, prodId ASC";
    }
}
