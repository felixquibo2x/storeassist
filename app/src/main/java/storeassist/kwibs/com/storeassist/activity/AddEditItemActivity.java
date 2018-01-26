package storeassist.kwibs.com.storeassist.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.Result;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import storeassist.kwibs.com.storeassist.R;
import storeassist.kwibs.com.storeassist.database.bean.Item;
import storeassist.kwibs.com.storeassist.database.core.StoreAssistDBHelper;
import storeassist.kwibs.com.storeassist.database.dao.ItemDao;
import storeassist.kwibs.com.storeassist.database.daoimpl.ItemDaoImpl;

import static android.Manifest.permission.CAMERA;

public class AddEditItemActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    public static final int MODE_ADD = 1;
    public static final int MODE_EDIT = 2;

    public static final String IMAGE_FILE_PREFIX = "ITEM_IMAGE_";

    private static final int PICK_IMAGE = 100;

    private StoreAssistDBHelper helper;
    private ItemDao itemDao;
    private int mode;
    private Item item;
    private EditText editTextName;
    private EditText editTextPrice;
    private EditText editTextBarcode;
    private ImageView imageView;

    private byte[] imageArray;

    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    private static int camId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private static final String BARCODE_SCAN_FRAGMENT_TAG = "barcodeScan";

    private String tmpName;
    private String tmpPrice;
    private String tmpBarcode;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_page);
        this.helper = new StoreAssistDBHelper(this);
        itemDao = new ItemDaoImpl(this.helper);

        editTextName = (EditText) findViewById(R.id.item_name);
        editTextPrice = (EditText) findViewById(R.id.price);
        editTextBarcode = (EditText) findViewById(R.id.barcode);
        imageView = (ImageView) findViewById(R.id.image_view_add);
        this.fragmentManager = getFragmentManager();
        this.scannerView = null;

        Intent intent = getIntent();
        mode = intent.getIntExtra("mode", 1);
        if(mode == MODE_EDIT){
            this.item = new Item();
            this.item.setID(intent.getLongExtra("id", 0));
            this.item.setItemName(intent.getStringExtra("name"));
            this.item.setPrice(intent.getDoubleExtra("price", 0));
            this.item.setBarcode(intent.getStringExtra("barcode"));
            try{
                this.item.setImage(readImage(AddEditItemActivity.IMAGE_FILE_PREFIX+this.item.getID()));
            }catch (IOException ex){
                Toast.makeText(this, "Error: "+ex.getMessage(), Toast.LENGTH_LONG).show();
            }
            this.imageArray = this.item.getImage();
            displayInfo();
        }
        registerOnImageClick();
    }

    public void onClickScan(View view){
        int currApiVersion = Build.VERSION.SDK_INT;
        if(currApiVersion >= Build.VERSION_CODES.M){
            if(!checkPermission()){
                requestPermission();
            }else{
                this.tmpName = editTextName.getText().toString();
                this.tmpPrice = editTextPrice.getText().toString();
                this.tmpBarcode = editTextBarcode.getText().toString();
                if(scannerView == null) {
                    scannerView = new ZXingScannerView(this);
                }
                scannerView.setResultHandler(this);
                setContentView(scannerView);

                FragmentTransaction ft = this.fragmentManager.beginTransaction();
                ft.addToBackStack("barcodeScanning");
                ft.commit();

                scannerView.startCamera();
            }
        }
    }

    private void displayInfo(){
        editTextName.setText(this.item.getItemName());
        editTextPrice.setText(new DecimalFormat("#,##0.00").format(this.item.getPrice()));
        editTextBarcode.setText(this.item.getBarcode());
        if(this.imageArray == null)
            imageView.setImageResource(ItemsProductsActivity.DEFAULT_IMAGE);
        else
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length));
    }

    private void registerOnImageClick(){
        imageView.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            Uri imageUri = data.getData();
            try {
                InputStream is = getContentResolver().openInputStream(imageUri);
                this.imageArray = getBytes(is);
                imageView.setImageURI(imageUri);
            }catch (Exception ex){
                Toast.makeText(this, "Error: "+ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(this.scannerView != null) {
            setContentView(R.layout.add_item_page);
            editTextBarcode = (EditText) findViewById(R.id.barcode);
            editTextName = (EditText) findViewById(R.id.item_name);
            editTextPrice = (EditText) findViewById(R.id.price);
            imageView = (ImageView) findViewById(R.id.image_view_add);

            editTextName.setText(this.tmpName);
            editTextPrice.setText(this.tmpPrice);
            editTextBarcode.setText(this.tmpBarcode);
            if (this.imageArray != null)
                this.imageView.setImageBitmap(BitmapFactory.decodeByteArray(this.imageArray, 0, this.imageArray.length));
            registerOnImageClick();
            this.scannerView.stopCamera();
            this.scannerView = null;
        }
    }

    private byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len = 0;
        while((len = is.read(buffer)) != -1){
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public void onSave(View view){
        try{
            String itemName = editTextName.getText().toString();
            String price = editTextPrice.getText().toString();
            String barcode = editTextBarcode.getText().toString();

            if(!itemName.isEmpty() && !price.isEmpty()) {
                if (this.mode == MODE_ADD) {
                    Item newItem = new Item();
                    newItem.setItemName(itemName);
                    newItem.setPrice(Double.parseDouble(price));
                    newItem.setBarcode(barcode);
                    newItem.setImage(this.imageArray);
                    itemDao.add(newItem);
                    if(this.imageArray != null)
                        writeImage(IMAGE_FILE_PREFIX+newItem.getID(), this.imageArray);
                    Intent intent = new Intent(this, ItemsProductsActivity.class);
                    startActivity(intent);
                } else if (this.mode == MODE_EDIT) {
                    item.setItemName(itemName);
                    item.setPrice(Double.parseDouble(price));
                    item.setBarcode(barcode);
                    item.setImage(this.imageArray);
                    itemDao.update(item);
                    if(this.imageArray != null)
                        writeImage(IMAGE_FILE_PREFIX+item.getID(), this.imageArray);
                    Intent intent = new Intent(this, ItemsProductsActivity.class);
                    startActivity(intent);
                }
            }
        }catch (Exception ex){
            Toast.makeText(this, "Error: "+ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private byte[] readImage(String fileName) throws IOException {
        byte[] imageArray = null;
        try {
            FileInputStream stream = openFileInput(fileName);
            imageArray = getBytes(stream);
        } catch (FileNotFoundException ex) {
        }
        return imageArray;
    }

    private void writeImage(String fileName, byte[] array) {
        FileOutputStream stream = null;
        try {
            stream = openFileOutput(fileName, MODE_PRIVATE);
            stream.write(array);
        } catch (FileNotFoundException ex) {
            Toast.makeText(this, "Error: "+ex.getMessage(), Toast.LENGTH_LONG).show();
        } catch (IOException ex) {
            Toast.makeText(this, "Error: "+ex.getMessage(), Toast.LENGTH_LONG).show();
        }finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    Toast.makeText(this, "Error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(this.helper != null)
            this.helper.close();
        if(this.scannerView != null)
            this.scannerView.stopCamera();
    }

    private boolean checkPermission(){
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onResume() {
        super.onResume();
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if(scannerView != null) {
                    FragmentTransaction ft = this.fragmentManager.beginTransaction();
                    ft.addToBackStack("barcodeScanning");
                    ft.commit();

                    setContentView(scannerView);
                    scannerView.setResultHandler(this);
                    scannerView.startCamera();
                }
            } else {
                requestPermission();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted){
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(AddEditItemActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void handleResult(Result result) {
        if(this.scannerView != null) {
            String myResult = result.getText();

            setContentView(R.layout.add_item_page);
            editTextBarcode = (EditText) findViewById(R.id.barcode);
            editTextName = (EditText) findViewById(R.id.item_name);
            editTextPrice = (EditText) findViewById(R.id.price);
            imageView = (ImageView) findViewById(R.id.image_view_add);

            editTextName.setText(this.tmpName);
            editTextPrice.setText(this.tmpPrice);
            editTextBarcode.setText(myResult);
            if (this.imageArray != null)
                this.imageView.setImageBitmap(BitmapFactory.decodeByteArray(this.imageArray, 0, this.imageArray.length));

            registerOnImageClick();
            this.scannerView.stopCamera();
            this.scannerView = null;
            super.onBackPressed();
        }
    }

}
