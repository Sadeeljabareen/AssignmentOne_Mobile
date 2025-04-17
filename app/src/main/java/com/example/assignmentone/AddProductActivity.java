package com.example.assignmentone;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AddProductActivity extends AppCompatActivity {

    private EditText nameInput, priceInput, descriptionInput, quantityInput;
    private SharedPreferences sharedPreferences;
    private int nextProductId = 6;
    private Spinner categorySpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        nameInput = findViewById(R.id.product_name_input);
        priceInput = findViewById(R.id.product_price_input);
        descriptionInput = findViewById(R.id.product_description_input);
        quantityInput = findViewById(R.id.product_quantity_input);
        Button submitButton = findViewById(R.id.submit_button);
        categorySpinner = findViewById(R.id.category_spinner);
        sharedPreferences = getSharedPreferences("LocalMarketPrefs", MODE_PRIVATE);
        nextProductId = sharedPreferences.getInt("nextProductId", 6);

        setupCategorySpinner();

        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                String name = nameInput.getText().toString();
                double price = Double.parseDouble(priceInput.getText().toString());
                String description = descriptionInput.getText().toString();
                int quantity = Integer.parseInt(quantityInput.getText().toString());
                String category = categorySpinner.getSelectedItem().toString();
                // Create new product
                Product newProduct = new Product(
                        nextProductId,
                        name,
                        price,
                        description,
                        quantity,
                        category,
                        true
                );

                saveNewProduct(newProduct);

                Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }





    private void saveAddProductTransaction(Product product) {
        Set<String> transactions = sharedPreferences.getStringSet("transactions", new HashSet<>());
        Set<String> newTransactions = new HashSet<>(transactions);

        long currentTime = System.currentTimeMillis();
        String transaction = "Added|" + currentTime + "|" +
                product.getName() + "|" +
                product.getAvailableQuantity() + "|" +
                product.getPrice();

        newTransactions.add(transaction);

        sharedPreferences.edit()
                .putStringSet("transactions", newTransactions)
                .apply();
    }
    private void setupCategorySpinner() {
        ArrayList<String> categories = new ArrayList<>();
        categories.add("Fruits");
        categories.add("Vegetables");
        categories.add("Handicrafts");
        categories.add("Bee Products");
        categories.add("Palestinian Sweets");
        categories.add("Olive Oil Products");
        categories.add("Traditional Costumes");
        categories.add("Dairy Products");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void saveNewProduct(Product product) {
        Set<String> productSet = new HashSet<>(sharedPreferences.getStringSet("products", new HashSet<>()));

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

            sharedPreferences.edit()
                    .putStringSet("products", productSet)
                    .putInt("nextProductId", nextProductId + 1)
                    .apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        saveAddProductTransaction(product);
    }

    private boolean validateInputs() {
        if (nameInput.getText().toString().isEmpty()) {
            nameInput.setError("Product name is required");
            return false;
        }

        try {
            Double.parseDouble(priceInput.getText().toString());
        } catch (NumberFormatException e) {
            priceInput.setError("Invalid price");
            return false;
        }

        if (quantityInput.getText().toString().isEmpty()) {
            quantityInput.setError("Quantity is required");
            return false;
        }

        return true;
    }
}