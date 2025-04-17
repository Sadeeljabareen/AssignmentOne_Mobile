package com.example.assignmentone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CartAdapter extends ArrayAdapter<CartItem> {
    public interface CartUpdateListener {
        void onCartUpdated();
        void onItemRemoved(int productId, int removedQuantity);
        void onQuantityChanged(int productId, int oldQuantity, int newQuantity);
    }

    private final Context context;
    private final ArrayList<CartItem> cartItems;
    private final SharedPreferences sharedPreferences;
    private final CartUpdateListener cartUpdateListener;

    public CartAdapter(Context context, ArrayList<CartItem> cartItems, CartUpdateListener listener) {
        super(context, R.layout.cart_item, cartItems);
        this.context = context;
        this.cartItems = cartItems;
        this.sharedPreferences = context.getSharedPreferences("LocalMarketPrefs", Context.MODE_PRIVATE);
        this.cartUpdateListener = listener;
    }

    @SuppressLint({"InflateParams", "DefaultLocale"})
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.cart_item, null);
        }

        CartItem item = cartItems.get(position);

        TextView name = view.findViewById(R.id.cart_item_name);
        TextView price = view.findViewById(R.id.cart_item_price);
        TextView quantity = view.findViewById(R.id.cart_item_quantity);
        Button removeButton = view.findViewById(R.id.remove_button);
        Button increaseButton = view.findViewById(R.id.increase_button);
        Button decreaseButton = view.findViewById(R.id.decrease_button);

        name.setText(item.getName());
        price.setText(String.format("%.2f ₪", item.getPrice()));
        quantity.setText(String.valueOf(item.getQuantity()));

        removeButton.setOnClickListener(v -> {
            removeFromCart(item.getProductId());
            cartItems.remove(position);
            notifyDataSetChanged();
            if (cartUpdateListener != null) {
                cartUpdateListener.onItemRemoved(item.getProductId(), item.getQuantity());
                cartUpdateListener.onCartUpdated();
            }
        });

        increaseButton.setOnClickListener(v -> {
            int oldQuantity = item.getQuantity();
            item.setQuantity(oldQuantity + 1);
            updateCartItemQuantity(item.getProductId(), oldQuantity, item.getQuantity());
            quantity.setText(String.valueOf(item.getQuantity()));
            notifyDataSetChanged();
            if (cartUpdateListener != null) {
                cartUpdateListener.onQuantityChanged(item.getProductId(), oldQuantity, item.getQuantity());
                cartUpdateListener.onCartUpdated();
            }
        });

        decreaseButton.setOnClickListener(v -> {
            int oldQuantity = item.getQuantity();
            if (oldQuantity > 1) {
                item.setQuantity(oldQuantity - 1);
                updateCartItemQuantity(item.getProductId(), oldQuantity, item.getQuantity());
                quantity.setText(String.valueOf(item.getQuantity()));
            } else {
                removeFromCart(item.getProductId());
                cartItems.remove(position);
                if (cartUpdateListener != null) {
                    cartUpdateListener.onItemRemoved(item.getProductId(), oldQuantity);
                }
            }
            notifyDataSetChanged();
            if (cartUpdateListener != null) {
                cartUpdateListener.onCartUpdated();
            }
        });

        return view;
    }

    private void removeFromCart(int productId) {
        updateCartSet(productId, -1);
    }

    private void updateCartItemQuantity(int productId, int oldQuantity, int newQuantity) {
        updateCartSet(productId, newQuantity);
    }

    private void updateCartSet(int productId, int newQuantity) {
        Set<String> cartSet = sharedPreferences.getStringSet("cart", new HashSet<>());
        Set<String> newCartSet = new HashSet<>();

        try {
            for (String item : cartSet) {
                JSONObject jsonObject = new JSONObject(item);
                if (jsonObject.getInt("productId") == productId) {
                    if (newQuantity > 0) {
                        jsonObject.put("quantity", newQuantity);
                        newCartSet.add(jsonObject.toString());
                    }
                } else {
                    newCartSet.add(item);
                }
            }

            sharedPreferences.edit().putStringSet("cart", newCartSet).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}