package storeassist.kwibs.com.storeassist.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;

import storeassist.kwibs.com.storeassist.R;
import storeassist.kwibs.com.storeassist.database.bean.Item;
import storeassist.kwibs.com.storeassist.database.core.StoreAssistDBHelper;
import storeassist.kwibs.com.storeassist.database.dao.ItemDao;
import storeassist.kwibs.com.storeassist.database.daoimpl.ItemDaoImpl;

public class ItemsProductsActivity extends AppCompatActivity {

    public static final int DEFAULT_IMAGE = R.drawable.default_item_image;

    private StoreAssistDBHelper helper;
    private ItemDao itemDao;

    private List<Item> items;

    class ItemViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).getID();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            view = getLayoutInflater().inflate(R.layout.items_info, null);

            ImageView imageView = (ImageView) view.findViewById(R.id.image_view);
            TextView textViewName = (TextView) view.findViewById(R.id.text_view_name);
            TextView textViewPrice = (TextView) view.findViewById(R.id.text_view_price);

            try {
                items.get(position).setImage(readImage(AddEditItemActivity.IMAGE_FILE_PREFIX+items.get(position).getID()));
            } catch (IOException e) {
                items.get(position).setImage(null);
            }
            byte[] data = items.get(position).getImage();
            if(data != null){
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                imageView.setImageBitmap(bitmap);
            }else {
                imageView.setImageResource(DEFAULT_IMAGE);
            }
            textViewName.setText(items.get(position).getItemName());
            textViewPrice.setText(new DecimalFormat("#,##0.00").format(items.get(position).getPrice()));

            return view;
        }
    }

    @Override
    public void onBackPressed() {
        getFragmentManager().popBackStack(MainPageActivity.BACK_STACK_TAG_INITIAL, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Intent i = new Intent(this, MainPageActivity.class);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.helper = new StoreAssistDBHelper(this);
        setContentView(R.layout.items_products_page);
        itemDao = new ItemDaoImpl(this.helper);
        populateListView("");
        registerOnItemClick();
        registerTextChangeListener();
    }

    private void registerTextChangeListener(){
        EditText editTextSearch = (EditText) findViewById(R.id.search);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                populateListView(s.toString());
            }
        });
    }

    private void populateListView(String searchSt){
        try {
            ListView listView = (ListView) findViewById(R.id.list_view_items);
            if(searchSt.isEmpty()) {
                this.items = this.itemDao.findAll();
            }else{
                this.items = this.itemDao.findByItemName(searchSt);
            }
            ItemViewAdapter adapter = new ItemViewAdapter();
            listView.setAdapter(adapter);
        }catch (Exception ex){
            Toast.makeText(ItemsProductsActivity.this, "Error: "+ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void registerOnItemClick(){
        ListView listView = (ListView) findViewById(R.id.list_view_items);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ItemsProductsActivity.this, AddEditItemActivity.class);
                Item selItem = items.get(position);
                intent.putExtra("mode", AddEditItemActivity.MODE_EDIT);
                intent.putExtra("id", selItem.getID());
                intent.putExtra("name", selItem.getItemName());
                intent.putExtra("price", selItem.getPrice());
                intent.putExtra("barcode", selItem.getBarcode());
                startActivity(intent);
            }
        });
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

    @Override
    protected void onDestroy(){
        this.helper.close();
        super.onDestroy();
    }

}
