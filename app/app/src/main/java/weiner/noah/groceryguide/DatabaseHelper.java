package weiner.noah.groceryguide;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class DatabaseHelper extends SQLiteOpenHelper {
    //table names
    public static final String PRODS_TABLE_NAME = "products";
    public static final String SUBCAT_LOC_TABLE_NAME = "subcat_loc";

    // Table columns

    //for products table
    public static final String _ID = "_id";
    public static final String PROD_ID = "prodId";
    public static final String NAME = "name";

    //for subcat loc table
    public static final String SUBCAT_NAME = "subCatName";
    public static final String AISLE = "aisle";
    public static final String SIDE = "side";
    public static final String DIST_FROM_FRONT = "distFromFront";

    // Database Information
    static final String DB_NAME = "prod.sqlite";

    // database version
    static final int DB_VERSION = 1;

    // Creating table query
    //private static final String CREATE_TABLE = "create table " + PRODS_TABLE_NAME + "(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + SUBJECT + " TEXT NOT NULL, " + DESC + " TEXT);";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //db.execSQL(CREATE_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PRODS_TABLE_NAME);
        onCreate(db);
    }
}
