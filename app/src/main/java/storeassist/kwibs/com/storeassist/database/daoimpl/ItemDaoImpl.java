package storeassist.kwibs.com.storeassist.database.daoimpl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import storeassist.kwibs.com.storeassist.database.bean.Item;
import storeassist.kwibs.com.storeassist.database.core.StoreAssistDBContract;
import storeassist.kwibs.com.storeassist.database.core.StoreAssistDBHelper;
import storeassist.kwibs.com.storeassist.database.dao.ItemDao;

/**
 * Created by cits-kwibs on 23/01/2018.
 */

public class ItemDaoImpl implements ItemDao {

    private final String[] projection = {
            StoreAssistDBContract.Item._ID,
            StoreAssistDBContract.Item.COLUMN_NAME_ITEM_NAME,
            StoreAssistDBContract.Item.COLUMN_NAME_PRICE,
            StoreAssistDBContract.Item.COLUMN_NAME_BARCODE
    };

    private SQLiteDatabase writeDb;
    private SQLiteDatabase readDb;

    public ItemDaoImpl(StoreAssistDBHelper helper){
        this.writeDb = helper.getWritableDatabase();
        this.readDb = helper.getReadableDatabase();
    }

    private ContentValues toContentValues(Item item){
        ContentValues values = new ContentValues();
        values.put(StoreAssistDBContract.Item.COLUMN_NAME_ITEM_NAME, item.getItemName());
        values.put(StoreAssistDBContract.Item.COLUMN_NAME_PRICE, item.getPrice());
        values.put(StoreAssistDBContract.Item.COLUMN_NAME_BARCODE, item.getBarcode());
        return values;
    }

    @Override
    public void add(Item item) throws Exception {
        try{
            ContentValues values = toContentValues(item);
            long newId = writeDb.insertOrThrow(StoreAssistDBContract.Item.TABLE_NAME, null, values);
            item.setID(newId);
        }catch (Exception ex){
            throw ex;
        }
    }

    @Override
    public void update(Item item) throws Exception {
        try{
            ContentValues values = toContentValues(item);
            String selection = StoreAssistDBContract.Item._ID + " = ?";
            String selectionArgs[] = {item.getID()+""};
            writeDb.update(StoreAssistDBContract.Item.TABLE_NAME, values, selection, selectionArgs);
        }catch (Exception ex){
            throw ex;
        }
    }

    @Override
    public void delete(Item item) throws Exception {
        try{
            String selection = StoreAssistDBContract.Item._ID + " = ?";
            String selectionArgs[] = {item.getID()+""};
            writeDb.delete(StoreAssistDBContract.Item.TABLE_NAME, selection, selectionArgs);
        }catch (Exception ex){
            throw ex;
        }
    }

    @Override
    public List<Item> findByItemName(String itemName) throws Exception {
        List<Item> items = new ArrayList<>();
        Cursor cursor = null;
        try{
            String selection = StoreAssistDBContract.Item.COLUMN_NAME_ITEM_NAME + " LIKE ?";
            String selectionArgs[] = {"%"+itemName+"%"};
            String sortOrder = StoreAssistDBContract.Item.COLUMN_NAME_ITEM_NAME + " ASC";
            cursor = readDb.query(StoreAssistDBContract.Item.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            while(cursor.moveToNext()){
                Item item = new Item();
                item.setID(cursor.getLong(cursor.getColumnIndexOrThrow(StoreAssistDBContract.Item._ID)));
                item.setItemName(cursor.getString(cursor.getColumnIndexOrThrow(StoreAssistDBContract.Item.COLUMN_NAME_ITEM_NAME)));
                item.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(StoreAssistDBContract.Item.COLUMN_NAME_PRICE)));
                item.setBarcode(cursor.getString(cursor.getColumnIndexOrThrow(StoreAssistDBContract.Item.COLUMN_NAME_BARCODE)));
                items.add(item);
            }
        }catch (Exception ex){
            throw ex;
        }finally {
            if(cursor != null)
                cursor.close();
        }
        return items;
    }

    @Override
    public Item findByBarcode(String barcode) throws Exception {
        Cursor cursor = null;
        Item item = null;
        try{
            String selection = StoreAssistDBContract.Item.COLUMN_NAME_BARCODE + " = ?";
            String selectionArgs[] = {barcode};
            cursor = readDb.query(StoreAssistDBContract.Item.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
            while(cursor.moveToNext()){
                item = new Item();
                item.setID(cursor.getLong(cursor.getColumnIndexOrThrow(StoreAssistDBContract.Item._ID)));
                item.setItemName(cursor.getString(cursor.getColumnIndexOrThrow(StoreAssistDBContract.Item.COLUMN_NAME_ITEM_NAME)));
                item.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(StoreAssistDBContract.Item.COLUMN_NAME_PRICE)));
                item.setBarcode(cursor.getString(cursor.getColumnIndexOrThrow(StoreAssistDBContract.Item.COLUMN_NAME_BARCODE)));
            }
        }catch (Exception ex){
            throw ex;
        }finally {
            if(cursor != null)
                cursor.close();
        }
        return item;
    }

    @Override
    public List<Item> findAll() throws Exception {
        List<Item> items = new ArrayList<>();
        Cursor cursor = null;
        try{
            String sortOrder = StoreAssistDBContract.Item.COLUMN_NAME_ITEM_NAME + " ASC";
            cursor = readDb.query(StoreAssistDBContract.Item.TABLE_NAME, projection, null, null, null, null, sortOrder);
            while(cursor.moveToNext()){
                Item item = new Item();
                item.setID(cursor.getLong(cursor.getColumnIndexOrThrow(StoreAssistDBContract.Item._ID)));
                item.setItemName(cursor.getString(cursor.getColumnIndexOrThrow(StoreAssistDBContract.Item.COLUMN_NAME_ITEM_NAME)));
                item.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(StoreAssistDBContract.Item.COLUMN_NAME_PRICE)));
                item.setBarcode(cursor.getString(cursor.getColumnIndexOrThrow(StoreAssistDBContract.Item.COLUMN_NAME_BARCODE)));
                items.add(item);
            }
        }catch (Exception ex){
            throw ex;
        }finally {
            if(cursor != null)
                cursor.close();
        }
        return items;
    }
}
