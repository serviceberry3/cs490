package weiner.noah.groceryguide;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseManager {
    private DatabaseHelper dbHelper;
    private Context context;

    private SQLiteDatabase database;

    private final String TAG = "DBManager";

    public DatabaseManager(Context c) {
        context = c;
    }

    public DatabaseManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();

        return this;
    }

    //query the db
    public Cursor fetch(String tableName, Query query, String[] cols) {
        String[] columns = null;
        Cursor cursor = null;

        //construct query

        //which columns do we want to fetch?
        switch (tableName) {
            case DatabaseHelper.PRODS_TABLE_NAME:
                columns = new String[] {DatabaseHelper._ID, DatabaseHelper.PROD_ID, DatabaseHelper.NAME, DatabaseHelper.SUBCAT_ID, DatabaseHelper.SUBCAT_NAME};
                break;
            case DatabaseHelper.SUBCAT_LOC_TABLE_NAME:
                columns = new String[] {DatabaseHelper._ID, DatabaseHelper.SUBCAT_NAME, DatabaseHelper.ELEMENT_NAME, DatabaseHelper.SIDE, DatabaseHelper.DIST_FROM_START, DatabaseHelper.SUBCAT_ID, DatabaseHelper.DIR};
                break;
        }

        //if user passed their own list of columns, override now
        if (cols != null) {
            columns = cols;
        }

        //Log.i(TAG, "calling fetch() with SQL where statement " + query.getWhereSelectionStatement() + " and args " + query.getPreparedArgs());

        if (columns != null) {
            //get cursor using the db query, passing in our filtering statement and ordering specification
            cursor = database.query(tableName, columns, query.getWhereSelectionStatement(), query.getPreparedArgs() != null ? query.getPreparedArgs().toArray(new String[0]) : null, null, null, query.getOrderBy());
        }
        else {
            return null;
        }

        //move cursor to first entry
        if (cursor != null) {
            cursor.moveToFirst();
        }
        else {
            return null;
        }
        return cursor;
    }
}
