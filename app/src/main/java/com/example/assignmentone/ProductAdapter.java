package com.example.assignmentone;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ProductAdapter extends ArrayAdapter<Product> {
    private Context context;
    private ArrayList<Product> products;
    private SharedPreferences sharedPreferences;

    public ProductAdapter(Context context, ArrayList<Product> products) {
        super(context, R.layout.product_item, products);
        this.context = context;
        this.products = products;
        this.sharedPreferences = context.getSharedPreferences("LocalMarketPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.product_item, null);
        }

        Product product = products.get(position);

        TextView name = view.findViewById(R.id.product_name);
        TextView price = view.findViewById(R.id.product_price);
        TextView quantity = view.findViewById(R.id.product_quantity);
        TextView description = view.findViewById(R.id.product_description);
        Button addToCart = view.findViewById(R.id.add_to_cart);

        name.setText(product.getName());
        price.setText(String.format("%.2f ₪", product.getPrice()));
        quantity.setText("Quantity: " + product.getAvailableQuantity());
        description.setText(product.getDescription());

        addToCart.setOnClickListener(v -> {
            if (product.getAvailableQuantity() > 0) {
                addToCart(product);
                product.setAvailableQuantity(product.getAvailableQuantity() - 1);
                saveProductQuantity(product);
                quantity.setText("Quantity: " + product.getAvailableQuantity());
                notifyDataSetChanged();
                Toast.makeText(context, "Added " + product.getName() + " to cart", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Out of stock", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void addToCart(Product product) {
        Set<String> cartSet = sharedPreferences.getStringSet("cart", new HashSet<>());
        Set<String> newCartSet = new HashSet<>(cartSet);

        try {
            boolean found = false;
            for (String item : cartSet) {
                JSONObject jsonObject = new JSONObject(item);
                if (jsonObject.getInt("productId") == product.getId()) {
                    jsonObject.put("quantity", jsonObject.getInt("quantity") + 1);
                    newCartSet.remove(item);
                    newCartSet.add(jsonObject.toString());
                    found = true;
                    break;
                }
            }

            if (!found) {
                JSONObject cartItem = new JSONObject();
                cartItem.put("productId", product.getId());
                cartItem.put("name", product.getName());
                cartItem.put("price", product.getPrice());
                cartItem.put("quantity", 1);
                newCartSet.add(cartItem.toString());
            }

            sharedPreferences.edit().putStringSet("cart", newCartSet).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateProducts(ArrayList<Product> filteredProducts) {
        clear();
        addAll(filteredProducts);
        notifyDataSetChanged();
    }
    private void saveProductQuantity(Product product) {
        sharedPreferences.edit()
                .putInt("product_qty_" + product.getId(), product.getAvailableQuantity())
                .apply();
    }

    public void updateList(ArrayList<Product> newList) {
        this.products = newList;
        notifyDataSetChanged();
    }

}