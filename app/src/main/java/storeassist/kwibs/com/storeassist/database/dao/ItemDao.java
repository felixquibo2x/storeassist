package storeassist.kwibs.com.storeassist.database.dao;

import java.util.List;

import storeassist.kwibs.com.storeassist.database.bean.Item;

/**
 * Created by cits-kwibs on 23/01/2018.
 */

public interface ItemDao extends Dao<Item> {
    public List<Item> findByItemName(String itemName) throws Exception;
    public Item findByBarcode(String barcode) throws Exception;
}
