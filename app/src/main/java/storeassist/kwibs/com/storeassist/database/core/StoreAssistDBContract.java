package storeassist.kwibs.com.storeassist.database.core;

import android.provider.BaseColumns;

/**
 * Created by cits-kwibs on 23/01/2018.
 */

public final class StoreAssistDBContract {

    private StoreAssistDBContract() {}

    public static class Item implements BaseColumns {
        public static final String TABLE_NAME = "items";
        public static final String COLUMN_NAME_ITEM_NAME = "itemName";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_BARCODE = "barcode";
    }

}
