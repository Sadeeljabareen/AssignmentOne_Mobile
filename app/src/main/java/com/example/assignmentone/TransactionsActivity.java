package com.example.assignmentone;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TransactionsActivity extends AppCompatActivity {

    private ListView transactionsListView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        transactionsListView = findViewById(R.id.transactions_list);
        sharedPreferences = getSharedPreferences("LocalMarketPrefs", MODE_PRIVATE);

        displayTransactionHistory();
    }

    private void displayTransactionHistory() {
        Set<String> transactionSet = sharedPreferences.getStringSet("transactions", new HashSet<>());
        List<TransactionRecord> transactions = new ArrayList<>();

        // Parse all transactions
        for (String transaction : transactionSet) {
            String[] parts = transaction.split("\\|");
            if (parts.length == 5) {
                try {
                    String type = parts[0];
                    long timestamp = Long.parseLong(parts[1]);
                    String productName = parts[2];
                    int quantity = Integer.parseInt(parts[3]);
                    double price = Double.parseDouble(parts[4]);

                    transactions.add(new TransactionRecord(type, timestamp, productName, quantity, price));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        // Sort by timestamp (newest first)
        Collections.sort(transactions, (t1, t2) -> Long.compare(t2.timestamp, t1.timestamp));

        // Format for display
        ArrayList<String> displayList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        for (TransactionRecord record : transactions) {
            String dateStr = dateFormat.format(new Date(record.timestamp));
            String action = record.type.equals("Purchase") ? "Bought" : "Added";
            String total = String.format(Locale.getDefault(), "%.2f", record.quantity * record.price);

            boolean add = displayList.add(String.format("%s: %s %d %s for ₪%s total",
                    dateStr,
                    action,
                    record.quantity,
                    record.productName,
                    total));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                displayList
        );
        transactionsListView.setAdapter(adapter);
    }

    private static class TransactionRecord {
        String type;
        long timestamp;
        String productName;
        int quantity;
        double price;

        TransactionRecord(String type, long timestamp, String productName, int quantity, double price) {
            this.type = type;
            this.timestamp = timestamp;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
        }
    }
}