package storeassist.kwibs.com.storeassist.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import storeassist.kwibs.com.storeassist.R;

public class MainPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage);
    }

    public void gotoItemsProduct(View view){
        Intent intent = new Intent(this, ItemsProductsActivity.class);
        startActivity(intent);
    }

    public void gotoAddItem(View view){
        Intent intent = new Intent(this, AddEditItemActivity.class);
        intent.putExtra("mode", AddEditItemActivity.MODE_ADD);
        startActivity(intent);
    }

    public void scanItem(View view){
        Intent intent = new Intent(this, BarcodeScannerActivity.class);
        startActivity(intent);
    }

    public void exit(View view){
    }

    @Override
    public void onBackPressed() {
    }
}
