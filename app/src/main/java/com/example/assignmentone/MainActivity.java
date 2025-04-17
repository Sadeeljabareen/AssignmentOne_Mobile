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
    private CheckBox filterLocalOnly;
    private CheckBox filterInStock;
    private Switch advancedSwitch;

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
        filterLocalOnly = findViewById(R.id.filter_local);
        filterInStock = findViewById(R.id.filter_available);
        advancedSwitch = findViewById(R.id.advanced_switch);
    }

    private void setupSharedPreferences() {
        sharedPreferences = getSharedPreferences("LocalMarketPrefs", MODE_PRIVATE);
    }

    private void initializeProductList() {
        productList = new ArrayList<>();

        // Load from SharedPreferences first
        Set<String> productSet = sharedPreferences.getStringSet("products", new HashSet<>());

        if (productSet.isEmpty()) {
            // If no saved products, load the default ones
            productList.add(new Product(1, "Red Apple", 2.5, "Fresh red apple from local farms", 10, "Fruits", true));
            productList.add(new Product(2, "Handmade Pottery", 15.0, "Artistic masterpiece by local artisan", 5, "Handicrafts", true));
            productList.add(new Product(3, "Organic Tomato", 3.0, "Organic tomatoes free from pesticides", 20, "Vegetables", true));
            productList.add(new Product(4, "Natural Honey", 10.0, "100% pure natural bee honey", 8, "Bee Products", true));
            productList.add(new Product(5, "Wicker Basket", 7.5, "Hand-woven wicker basket", 3, "Handicrafts", true));

            // Save the default products
            saveProductsToPrefs();
        } else {
            // Load products from SharedPreferences
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
        // Clear existing products
        productList.clear();

        // Reload from SharedPreferences
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

        // Reload quantities and update adapter
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

    // In your setupButtonListeners() method:
    private void setupButtonListeners() {
        goToCartButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            startActivity(intent);
        });

        // Add this for the transactions button
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

        // Setup category spinner
        ArrayList<String> categories = new ArrayList<>();
        categories.add("All Categories");
        categories.add("Fruits");
        categories.add("Vegetables");
        categories.add("Handicrafts");
        categories.add("Bee Products");
        categories.add("Palestinian Sweets");
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

            // Initialize price range defaults
            double minPrice = 0;
            double maxPrice = Double.MAX_VALUE;

            // Only process price range if RadioGroup is initialized
            if (priceRangeGroup != null) {
                int selectedPriceRangeId = priceRangeGroup.getCheckedRadioButtonId();

                if (selectedPriceRangeId == R.id.price_low) {
                    maxPrice = 10;
                } else if (selectedPriceRangeId == R.id.price_medium) {
                    minPrice = 10;
                    maxPrice = 50;
                } else if (selectedPriceRangeId == R.id.price_high) {
                    minPrice = 50;
                }
            }

            // Get filter states
            boolean localOnly = filterLocalOnly != null && filterLocalOnly.isChecked();
            boolean inStockOnly = filterInStock != null && filterInStock.isChecked();

            ArrayList<Product> filteredList = new ArrayList<>();

            if (productList == null) {
                Toast.makeText(this, "Product list is not initialized", Toast.LENGTH_SHORT).show();
                return;
            }

            for (Product product : productList) {
                boolean matchesName = product.getName().toLowerCase().contains(searchQuery);
                boolean matchesCategory = selectedCategory.equals("All Categories") ||
                        product.getCategory().equals(selectedCategory);
                boolean matchesPrice = product.getPrice() >= minPrice &&
                        product.getPrice() <= maxPrice;
                boolean matchesLocal = !localOnly || product.isLocal();
                boolean matchesStock = !inStockOnly || product.getAvailableQuantity() > 0;

                if (matchesName && matchesCategory && matchesPrice && matchesLocal && matchesStock) {
                    filteredList.add(product);
                }
            }

            // Update adapter with filtered results
            if (productAdapter == null) {
                productAdapter = new ProductAdapter(this, filteredList);
                productListView.setAdapter(productAdapter);
            } else {
                productAdapter.updateProducts(filteredList);
            }

            if (filteredList.isEmpty()) {
                Toast.makeText(this, "No products found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error during search: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        refreshProductList(); // This replaces the previous onResume implementation
    }
}