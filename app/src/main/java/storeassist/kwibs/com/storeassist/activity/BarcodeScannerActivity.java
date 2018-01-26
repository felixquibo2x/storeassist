package storeassist.kwibs.com.storeassist.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.icu.text.DecimalFormat;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import storeassist.kwibs.com.storeassist.database.bean.Item;
import storeassist.kwibs.com.storeassist.database.core.StoreAssistDBHelper;
import storeassist.kwibs.com.storeassist.database.dao.ItemDao;
import storeassist.kwibs.com.storeassist.database.daoimpl.ItemDaoImpl;

import static android.Manifest.permission.CAMERA;

public class BarcodeScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    private static int camId = Camera.CameraInfo.CAMERA_FACING_BACK;

    private StoreAssistDBHelper helper;
    private ItemDao itemDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.scannerView = new ZXingScannerView(this);
        setContentView(this.scannerView);

        this.helper = new StoreAssistDBHelper(this);
        itemDao = new ItemDaoImpl(this.helper);

        int currApiVersion = Build.VERSION.SDK_INT;
        if(currApiVersion >= Build.VERSION_CODES.M){
            if(!checkPermission()){
                requestPermission();
            }else
                this.scannerView.startCamera();
        }
    }

    private boolean checkPermission()
    {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onResume() {
        super.onResume();
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if(scannerView == null) {
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.helper.close();
        scannerView.stopCamera();
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
        new android.support.v7.app.AlertDialog.Builder(BarcodeScannerActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void handleResult(Result result) {
        String myResult = result.getText();
        try{
            Item item = this.itemDao.findByBarcode(myResult);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Item Information");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    scannerView.resumeCameraPreview(BarcodeScannerActivity.this);
                }
            });
            if(item != null) {
                builder.setMessage(
                        "Item: " + item.getItemName() + "\n" +
                                "Price: " + new DecimalFormat("#,##0.00").format(item.getPrice())
                );
            }else{
                builder.setMessage("Item not found");
            }
            AlertDialog alert1 = builder.create();
            alert1.show();

        }catch (Exception ex){
            Toast.makeText(this, "Error: "+ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}