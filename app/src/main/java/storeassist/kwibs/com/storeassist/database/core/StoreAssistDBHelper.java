package storeassist.kwibs.com.storeassist.database.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by cits-kwibs on 23/01/2018.
 */

public class StoreAssistDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "StoreAssist.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + StoreAssistDBContract.Item.TABLE_NAME + " (" +
                    StoreAssistDBContract.Item._ID + " INTEGER PRIMARY KEY," +
                    StoreAssistDBContract.Item.COLUMN_NAME_ITEM_NAME + " TEXT," +
                    StoreAssistDBContract.Item.COLUMN_NAME_BARCODE + " TEXT," +
                    StoreAssistDBContract.Item.COLUMN_NAME_PRICE + " REAL)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + StoreAssistDBContract.Item.TABLE_NAME;

    public StoreAssistDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, newVersion, oldVersion);
    }

}
