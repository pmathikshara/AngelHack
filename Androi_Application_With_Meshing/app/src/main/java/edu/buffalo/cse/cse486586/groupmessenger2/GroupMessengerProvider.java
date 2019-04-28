package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {

    private DBHelper dbHelper;
    private static final String DBNAME = "messengerDB2";
    private SQLiteDatabase database;


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */


        //Log.e("My code", " Attempting to insert the following content :" + values);

        //Log.e("My code", "Creating a Writable SQLite DB instance");
        database = dbHelper.getWritableDatabase();

        long rowId=0;
        try {
            //Log.e("My code", " Inside the try block for insert");
            rowId = database.replace(DBNAME, "", values);
        } catch (Exception e) {
            Log.e("My code", "SQL write failed due to "+e);
        }

        if(rowId>0){
            //Log.e("My code", "Insertion successful. Row "+rowId+" added to the table");
            getContext().getContentResolver().notifyChange(uri, null);
        }


        Log.v("insert", values.toString());
        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        //Log.e("My code", " Creating a new SQL Database Helper in the OnCreate method");
        Context context = getContext();
        dbHelper = new DBHelper(context,DBNAME,null,1);
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */

        Log.e("My code", "Creating a Readable SQLite DB instance");
        database = dbHelper.getReadableDatabase();

        Cursor c1=null;

        try{
            //Log.e("My code", "Inside the try block for query");
            String[] args = new String[1];
            args[0] = selection;
            c1 = database.query(
                    DBNAME,  // The table to query
                    projection,                               // The columns to return
                    "key =?",                                    // The columns for the WHERE clause
                    args,                                       // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder                                 // The sort order
            );

            //String queryString = "SELECT * FROM messengerDB WHERE KEY = ?";


            //Log.e("My code", "The column indices for key : "+c1.getColumnIndex("key") + " value : " + c1.getColumnIndex("value"));
            if(c1.moveToFirst())
                Log.e("My code", "The column contents for key : "+c1.getString(0) + " value : " + c1.getString(1));


            //c1 = database.rawQuery(queryString, args);

        }
        catch (Exception e) {
            Log.e("My code", "SQL read failed due to " + e);
        }

        Log.v("query", selection);
        return c1;
    }

    /**
     * Created by sanjay on 2/14/16.
     */
    private class DBHelper extends SQLiteOpenHelper {

        private static final String SQL_TABLE = "CREATE TABLE " +
                "messengerDB2" +                       // Table's name
                "(" +                           // The columns in the table
                " key TEXT PRIMARY KEY, " +
                " value TEXT )";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS messengerDB2";

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }

}
