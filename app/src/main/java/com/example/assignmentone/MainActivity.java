package com.example.assignmentone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private ListView productListView;
    private Button goToCartButton;
    private ArrayList<Product> productList;
    private ProductAdapter productAdapter;
    private SharedPreferences sharedPreferences;
    private static final int ADD_PRODUCT_REQUEST = 1;
    private EditText searchInput;
    private Spinner categorySpinner;
    private RadioGroup priceRangeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupSharedPreferences();
        initializeProductList();
        setupProductAdapter();
        setupSearchViews();
        setupButtonListeners();
    }

    private void initializeViews() {
        productListView = findViewById(R.id.product_list);
        goToCartButton = findViewById(R.id.go_to_cart);
        priceRangeGroup = findViewById(R.id.price_range_group);

    }

    private void setupSharedPreferences() {
        sharedPreferences = getSharedPreferences("LocalMarketPrefs", MODE_PRIVATE);
    }

    private void initializeProductList() {
        productList = new ArrayList<>();

        // Load from SharedPreferences
        Set<String> productSet = sharedPreferences.getStringSet("products", new HashSet<>());

        if (productSet.isEmpty()) {
            productList.add(new Product(1, "Red Apple", 2.5, "Fresh red apple from local farms", 10, "Fruits", true));
            productList.add(new Product(2, "Handmade Pottery", 15.0, "Artistic masterpiece by local artisan", 5, "Handicrafts", true));
            productList.add(new Product(3, "Organic Tomato", 3.0, "Organic tomatoes free from pesticides", 20, "Vegetables", true));
            productList.add(new Product(4, "Natural Honey", 10.0, "100% pure natural bee honey", 8, "Bee Products", true));
            productList.add(new Product(5, "Wicker Basket", 7.5, "Hand-woven wicker basket", 3, "Handicrafts", true));
            productList.add(new Product(6, "Za'atar Mix", 8.0, "Traditional Palestinian thyme mix with sesame and sumac", 12, "Palestinian food", true));
            productList.add(new Product(7, "Makdous", 7.0, "Pickled baby eggplants stuffed with walnuts and peppers", 10, "Palestinian food", true));
            productList.add(new Product(8, "Dibs Tamr (Date Molasses)", 6.5, "Natural sweet syrup made from dates", 9, "Palestinian food", true));
            productList.add(new Product(9, "Extra Virgin Olive Oil", 18.0, "Cold-pressed from ancient Palestinian olive groves", 8, "Olive Oil Products", true));
            productList.add(new Product(10, "Olive Wood Carving", 35.0, "Hand-carved olive wood decoration", 4, "Handicrafts", true));
            productList.add(new Product(11, "Olive Oil Soap (Nabulsi)", 10.0, "Traditional olive oil soap from Nablus", 20, "Olive Oil Products", true));
            productList.add(new Product(12, "Tatreez Embroidery", 45.0, "Hand-stitched Palestinian traditional dress", 3, "Traditional Costumes", true));
            productList.add(new Product(13, "Palestinian Keffiyeh", 15.0, "Black and white traditional scarf", 15, "Traditional Costumes", true));
            productList.add(new Product(14, "Hand-Painted Ceramic Plate", 22.0, "Hebron-style ceramic art", 7, "Handicrafts", true));
            productList.add(new Product(15, "Akkawi Cheese", 12.0, "Salty white cheese from Palestine", 10, "Dairy Products", true));
            productList.add(new Product(16, "Jameed (Dried Yogurt)", 9.0, "Traditional dried yogurt for Mansaf", 6, "Dairy Products", true));


            saveProductsToPrefs();
        } else {
            // Load the products from SharedPreferences
            for (String productJson : productSet) {
                try {
                    JSONObject jsonObject = new JSONObject(productJson);
                    productList.add(new Product(
                            jsonObject.getInt("id"),
                            jsonObject.getString("name"),
                            jsonObject.getDouble("price"),
                            jsonObject.getString("description"),
                            jsonObject.getInt("availableQuantity"),
                            jsonObject.getString("category"),
                            jsonObject.getBoolean("isLocal")
                    ));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        loadSavedQuantities();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_PRODUCT_REQUEST && resultCode == RESULT_OK) {
            refreshProductList();
        }
    }
    private void refreshProductList() {
        productList.clear();

        Set<String> productSet = sharedPreferences.getStringSet("products", new HashSet<>());

        for (String productJson : productSet) {
            try {
                JSONObject jsonObject = new JSONObject(productJson);
                productList.add(new Product(
                        jsonObject.getInt("id"),
                        jsonObject.getString("name"),
                        jsonObject.getDouble("price"),
                        jsonObject.getString("description"),
                        jsonObject.getInt("availableQuantity"),
                        jsonObject.getString("category"),
                        jsonObject.getBoolean("isLocal")
                ));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        loadSavedQuantities();
        productAdapter.notifyDataSetChanged();
    }

    private void saveProductsToPrefs() {
        Set<String> productSet = new HashSet<>();
        for (Product product : productList) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", product.getId());
                jsonObject.put("name", product.getName());
                jsonObject.put("price", product.getPrice());
                jsonObject.put("description", product.getDescription());
                jsonObject.put("availableQuantity", product.getAvailableQuantity());
                jsonObject.put("category", product.getCategory());
                jsonObject.put("isLocal", product.isLocal());
                productSet.add(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        sharedPreferences.edit().putStringSet("products", productSet).apply();
    }

    private void loadSavedQuantities() {
        for (Product product : productList) {
            int savedQty = sharedPreferences.getInt("product_qty_" + product.getId(), product.getOriginalQuantity());
            product.setAvailableQuantity(savedQty);
        }
    }

    private void setupProductAdapter() {
        productAdapter = new ProductAdapter(this, productList);
        productListView.setAdapter(productAdapter);
    }

    private void setupButtonListeners() {
        goToCartButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            startActivity(intent);
        });

        Button transactionsButton = findViewById(R.id.transactions_button);
        transactionsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TransactionsActivity.class);
            startActivity(intent);
        });

        // Add this for the add product button
        Button addProductButton = findViewById(R.id.add_product_button);
        addProductButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddProductActivity.class);
            startActivityForResult(intent, ADD_PRODUCT_REQUEST);
        });
    }

    private void setupSearchViews() {
        searchInput = findViewById(R.id.search_input);
        categorySpinner = findViewById(R.id.category_spinner);
        Button searchButton = findViewById(R.id.search_button);

        ArrayList<String> categories = new ArrayList<>();
        categories.add("All Categories");
        categories.add("Fruits");
        categories.add("Vegetables");
        categories.add("Handicrafts");
        categories.add("Bee Products");
        categories.add("Palestinian food");
        categories.add("Olive Oil Products");
        categories.add("Traditional Costumes");
        categories.add("Dairy Products");


        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Set search functionality
        searchButton.setOnClickListener(v -> performSearch());
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void performSearch() {
        try {
            String searchQuery = searchInput.getText().toString().toLowerCase().trim();
            String selectedCategory = categorySpinner.getSelectedItem().toString();

            double minPrice = 0;
            double maxPrice = Double.MAX_VALUE;

            int selectedPriceId = priceRangeGroup.getCheckedRadioButtonId();

            if (selectedPriceId == R.id.price_low) {
                maxPrice = 10;
            } else if (selectedPriceId == R.id.price_medium) {
                minPrice = 10;
                maxPrice = 50;
            } else if (selectedPriceId == R.id.price_high) {
                minPrice = 50;
            }

            ArrayList<Product> filteredList = new ArrayList<>();

            Set<String> productSet = sharedPreferences.getStringSet("products", new HashSet<>());
            productList.clear();

            for (String productJson : productSet) {
                JSONObject jsonObject = new JSONObject(productJson);
                productList.add(new Product(
                        jsonObject.getInt("id"),
                        jsonObject.getString("name"),
                        jsonObject.getDouble("price"),
                        jsonObject.getString("description"),
                        jsonObject.getInt("availableQuantity"),
                        jsonObject.getString("category"),
                        jsonObject.getBoolean("isLocal")
                ));
            }

            for (Product product : productList) {
                boolean matchesName = searchQuery.isEmpty() ||
                        product.getName().toLowerCase().contains(searchQuery);
                boolean matchesCategory = selectedCategory.equals("All Categories") ||
                        product.getCategory().equals(selectedCategory);
                boolean matchesPrice = product.getPrice() >= minPrice &&
                        product.getPrice() <= maxPrice;

                if (matchesName && matchesCategory && matchesPrice) {
                    filteredList.add(product);
                }
            }

            productAdapter.updateProducts(filteredList);

            if (filteredList.isEmpty()) {
                Toast.makeText(this, "No products found", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Search error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        refreshProductList();
    }
}