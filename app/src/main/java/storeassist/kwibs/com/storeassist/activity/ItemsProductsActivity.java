package storeassist.kwibs.com.storeassist.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
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

            CircleImageView imageView = (CircleImageView) view.findViewById(R.id.image_view);
            TextView textViewName = (TextView) view.findViewById(R.id.text_view_name);
            TextView textViewPrice = (TextView) view.findViewById(R.id.text_view_price);

            try {
                items.get(position).setImage(readImage(AddEditItemActivity.IMAGE_FILE_PREFIX+items.get(position).getID()));
            } catch (IOException e) {
                items.get(position).setImage(null);
            }
            byte[] data = items.get(position).getImage();
            if(data != null){
                Glide.with(getBaseContext()).load(data).into(imageView);
            }else {
                Glide.with(getBaseContext()).load(DEFAULT_IMAGE).into(imageView);
            }

            textViewName.setText(items.get(position).getItemName());
            textViewPrice.setText(new DecimalFormat("#,##0.00").format(items.get(position).getPrice()));

            return view;
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, MainPageActivity.class);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_products_page);
        this.helper = new StoreAssistDBHelper(this);
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
            SwipeMenuListView listView = (SwipeMenuListView) findViewById(R.id.list_view_items);
            if(searchSt.isEmpty()) {
                this.items = this.itemDao.findAll();
            }else{
                this.items = this.itemDao.findByItemName(searchSt);
            }
            ItemViewAdapter adapter = new ItemViewAdapter();
            listView.setAdapter(adapter);
            listView.setMenuCreator(createSwipeMenuItems());
            registerOnSwipeDelete(listView, searchSt);
        }catch (Exception ex){
            Toast.makeText(ItemsProductsActivity.this, "Error: "+ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private SwipeMenuCreator createSwipeMenuItems(){
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                deleteItem.setWidth(170);
                deleteItem.setIcon(R.drawable.ic_delete);
                menu.addMenuItem(deleteItem);
            }
        };
        return creator;
    }

    private void registerOnSwipeDelete(final SwipeMenuListView listView, final String searchSt){
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        Item i = items.get(position);
                        try {
                            itemDao.delete(i);
                            deleteImageFile(AddEditItemActivity.IMAGE_FILE_PREFIX+i.getID());
                            populateListView(searchSt);
                            Toast.makeText(ItemsProductsActivity.this, "Item successfully deleted", Toast.LENGTH_LONG).show();
                        }catch (Exception ex){
                            Toast.makeText(ItemsProductsActivity.this, "Error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        break;
                }
                return false;
            }
        });
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

    private void deleteImageFile(String fileName){
        File file = new File(getFilesDir(), fileName);
        if(file.exists()){
            deleteFile(fileName);
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
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onDestroy(){
        this.helper.close();
        super.onDestroy();
    }

}
