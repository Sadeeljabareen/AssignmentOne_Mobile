package com.example.assignmentone;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartUpdateListener {
    private ListView cartListView;
    private ArrayList<CartItem> cartItems;
    private CartAdapter cartAdapter;
    private SharedPreferences sharedPreferences;
    private TextView totalPriceView;
    private Button checkoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initializeViews();
        setupSharedPreferences();
        loadCartItems();
        setupCartAdapter();
        setupCheckoutButton();
    }

    private void initializeViews() {
        cartListView = findViewById(R.id.cart_list);
        totalPriceView = findViewById(R.id.total_price);
        checkoutButton = findViewById(R.id.checkout_button);
    }

    private void setupSharedPreferences() {
        sharedPreferences = getSharedPreferences("LocalMarketPrefs", MODE_PRIVATE);
    }

    private void loadCartItems() {
        cartItems = new ArrayList<>();
        Set<String> cartSet = sharedPreferences.getStringSet("cart", new HashSet<>());

        for (String itemJson : cartSet) {
            try {
                JSONObject jsonObject = new JSONObject(itemJson);
                cartItems.add(new CartItem(
                        jsonObject.getInt("productId"),
                        jsonObject.getString("name"),
                        jsonObject.getDouble("price"),
                        jsonObject.getInt("quantity")
                ));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupCartAdapter() {
        cartAdapter = new CartAdapter(this, cartItems, this);
        cartListView.setAdapter(cartAdapter);
        updateTotalPrice();
    }

    private void setupCheckoutButton() {
        checkoutButton.setOnClickListener(v -> {
            checkout();
            finish();
        });
    }

    @SuppressLint("DefaultLocale")
    public void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        totalPriceView.setText(String.format("%.2f ₪", total));
    }

    private void checkout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your shopping cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        saveTransactionHistory();

        clearCart();

        showPurchaseConfirmation();
    }

    private void saveTransactionHistory() {
        Set<String> transactions = sharedPreferences.getStringSet("transactions", new HashSet<>());
        Set<String> newTransactions = new HashSet<>(transactions);

        long currentTime = System.currentTimeMillis();

        for (CartItem item : cartItems) {
            String transaction = "Purchase|" + currentTime + "|" +
                    item.getName() + "|" +
                    item.getQuantity() + "|" +
                    item.getPrice();
            newTransactions.add(transaction);
        }

        sharedPreferences.edit()
                .putStringSet("transactions", newTransactions)
                .apply();
    }

    private void clearCart() {
        sharedPreferences.edit()
                .putStringSet("cart", new HashSet<>())
                .apply();

        cartItems.clear();

        if (cartAdapter != null) {
            cartAdapter.notifyDataSetChanged();
        }

        updateTotalPrice();
    }

    private void showPurchaseConfirmation() {
        double total = calculateTotal();
        String confirmationMsg = buildConfirmationMessage(total);
        Toast.makeText(this, confirmationMsg, Toast.LENGTH_LONG).show();
    }

    private double calculateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    @SuppressLint("DefaultLocale")
    private String buildConfirmationMessage(double total) {
        StringBuilder builder = new StringBuilder("Purchase Successful!\n\n");
        for (CartItem item : cartItems) {
            builder.append(String.format("- %dx %s: ₪%.2f\n",
                    item.getQuantity(),
                    item.getName(),
                    item.getPrice() * item.getQuantity()));
        }
        builder.append(String.format("\nTotal: ₪%.2f", total));
        return builder.toString();
    }

    @Override
    public void onItemRemoved(int productId, int removedQuantity) {
        // Update inventory
        restoreProductQuantity(productId, removedQuantity);

        updateTotalPrice();

        if (cartItems.isEmpty()) {
            showEmptyCartMessage();
        }
    }

    private void restoreProductQuantity(int productId, int quantity) {
        int currentQty = sharedPreferences.getInt("product_qty_" + productId, 0);
        sharedPreferences.edit()
                .putInt("product_qty_" + productId, currentQty + quantity)
                .apply();
    }

    private void showEmptyCartMessage() {
        Toast.makeText(this, "Your cart is now empty", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onQuantityChanged(int productId, int oldQuantity, int newQuantity) {
        int quantityDifference = oldQuantity - newQuantity;
        adjustProductQuantity(productId, quantityDifference);
        updateTotalPrice();
    }

    private void adjustProductQuantity(int productId, int difference) {
        int currentQty = sharedPreferences.getInt("product_qty_" + productId, 0);
        sharedPreferences.edit()
                .putInt("product_qty_" + productId, currentQty + difference)
                .apply();
    }

    @Override
    public void onCartUpdated() {
        updateTotalPrice();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCart();
    }

    private void refreshCart() {
        loadCartItems();
        if (cartAdapter != null) {
            cartAdapter.clear();
            cartAdapter.addAll(cartItems);
            cartAdapter.notifyDataSetChanged();

        }
        updateTotalPrice();
    }
}