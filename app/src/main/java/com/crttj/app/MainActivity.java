
package com.crttj.app;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import android.content.*;
import org.json.*;

public class MainActivity extends Activity {

    LinearLayout root, content;
    SharedPreferences prefs;
    JSONArray trades;
    int blue = Color.rgb(25, 100, 220);

    public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("CRT_TJ", MODE_PRIVATE);
        loadTrades();
        showDashboard();
    }

    void loadTrades() {
        try {
            trades = new JSONArray(prefs.getString("trades", "[]"));
        } catch (Exception e) {
            trades = new JSONArray();
        }
    }

    void saveTrades() {
        prefs.edit().putString("trades", trades.toString()).apply();
    }

    TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(Color.BLACK);
        t.setPadding(8, 12, 8, 12);
        return t;
    }

    Button button(String value) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(blue));
        return b;
    }

    void setup(String title) {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(1);
        root.setPadding(16, 16, 16, 16);
        root.setBackgroundColor(Color.rgb(245, 248, 255));
        scroll.addView(root);

        TextView heading = text("CRT TJ  |  " + title, 23);
        heading.setTextColor(blue);
        heading.setTypeface(null, 1);
        root.addView(heading);

        setContentView(scroll);
    }

    void showDashboard() {
        loadTrades();
        setup("Trading Journal");

        int wins = 0, losses = 0;
        for (int i = 0; i < trades.length(); i++) {
            String result = trades.optJSONObject(i).optString("result");
            if (result.equals("Win")) wins++;
            if (result.equals("Loss")) losses++;
        }

        int total = wins + losses;
        double rate = total == 0 ? 0 : wins * 100.0 / total;

        root.addView(text("DASHBOARD", 20));
        root.addView(text("Total Trades: " + trades.length(), 18));
        root.addView(text("Wins: " + wins, 18));
        root.addView(text("Losses: " + losses, 18));
        root.addView(text("Win Rate: " + String.format("%.1f", rate) + "%", 18));

        Button add = button("+ ADD NEW TRADE");
        root.addView(add);
        add.setOnClickListener(v -> showAddTrade());

        Button history = button("TRADE HISTORY");
        root.addView(history);
        history.setOnClickListener(v -> showHistory());
    }

    void showAddTrade() {
        setup("New Trade");

        EditText pair = new EditText(this);
        pair.setHint("Pair (e.g. EURUSD)");
        root.addView(pair);

        EditText direction = new EditText(this);
        direction.setHint("Direction (Buy / Sell)");
        root.addView(direction);

        EditText entry = new EditText(this);
        entry.setHint("Entry Price");
        entry.setInputType(8194);
        root.addView(entry);

        EditText stop = new EditText(this);
        stop.setHint("Stop Loss");
        stop.setInputType(8194);
        root.addView(stop);

        EditText target = new EditText(this);
        target.setHint("Take Profit");
        target.setInputType(8194);
        root.addView(target);

        Spinner result = new Spinner(this);
        String[] choices = {"Win", "Loss", "Break Even"};
        result.setAdapter(new ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_dropdown_item, choices
        ));
        root.addView(result);

        EditText notes = new EditText(this);
        notes.setHint("Notes / CRT Analysis");
        notes.setMinLines(3);
        root.addView(notes);

        Button save = button("SAVE TRADE");
        root.addView(save);

        save.setOnClickListener(v -> {
            if (pair.getText().toString().trim().isEmpty()) {
                pair.setError("Enter a trading pair");
                return;
            }

            try {
                JSONObject trade = new JSONObject();
                trade.put("pair", pair.getText().toString());
                trade.put("direction", direction.getText().toString());
                trade.put("entry", entry.getText().toString());
                trade.put("stop", stop.getText().toString());
                trade.put("target", target.getText().toString());
                trade.put("result", result.getSelectedItem().toString());
                trade.put("notes", notes.getText().toString());
                trade.put("date", java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));

                trades.put(trade);
                saveTrades();

                Toast.makeText(this, "Trade Saved!", Toast.LENGTH_SHORT).show();
                showDashboard();
            } catch (Exception e) {
                Toast.makeText(this, "Could not save trade", Toast.LENGTH_SHORT).show();
            }
        });

        Button back = button("BACK TO DASHBOARD");
        root.addView(back);
        back.setOnClickListener(v -> showDashboard());
    }

    void showHistory() {
        loadTrades();
        setup("Trade History");

        if (trades.length() == 0) {
            root.addView(text("No trades recorded yet.", 17));
        }

        for (int i = trades.length() - 1; i >= 0; i--) {
            JSONObject trade = trades.optJSONObject(i);
            if (trade == null) continue;

            String details =
                trade.optString("pair") + " | " +
                trade.optString("direction") + "\n" +
                "Result: " + trade.optString("result") + "\n" +
                "Entry: " + trade.optString("entry") + "\n" +
                "SL: " + trade.optString("stop") +
                " | TP: " + trade.optString("target") + "\n" +
                trade.optString("date") + "\n" +
                "Notes: " + trade.optString("notes");

            TextView item = text(details, 15);
            item.setBackgroundColor(Color.WHITE);
            root.addView(item);
            View line = new View(this);
            line.setBackgroundColor(Color.LTGRAY);
            line.setLayoutParams(new LinearLayout.LayoutParams(-1, 2));
            root.addView(line);
        }

        Button back = button("BACK TO DASHBOARD");
        root.addView(back);
        back.setOnClickListener(v -> showDashboard());
    }
}
