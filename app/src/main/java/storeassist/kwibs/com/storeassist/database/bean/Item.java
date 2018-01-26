package storeassist.kwibs.com.storeassist.database.bean;

/**
 * Created by cits-kwibs on 23/01/2018.
 */

public class Item {

    private long ID;
    private String itemName;
    private double price;
    private String barcode;
    private byte[] image;

    public Item(){}

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public String toString(){
        return this.itemName;
    }

}
