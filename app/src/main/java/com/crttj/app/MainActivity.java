
package com.crttj.app;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import org.json.*;

public class MainActivity extends Activity {

    LinearLayout root;
    SharedPreferences prefs;
    JSONArray trades;
    Uri selectedImage;

    int bg = Color.rgb(10, 17, 35);
    int card = Color.rgb(23, 35, 61);
    int blue = Color.rgb(45, 115, 255);
    int white = Color.WHITE;
    int muted = Color.rgb(160, 177, 205);
    int green = Color.rgb(35, 205, 140);
    int red = Color.rgb(255, 90, 105);

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
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

    GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    TextView txt(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setPadding(8, 8, 8, 8);
        return t;
    }

    TextView bold(String value, int size, int color) {
        TextView t = txt(value, size, color);
        t.setTypeface(null, Typeface.BOLD);
        return t;
    }

    Button btn(String value) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextColor(white);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(blue)
        );
        return b;
    }

    LinearLayout vertical() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    void setup(String page) {
        ScrollView scroll = new ScrollView(this);
        root = vertical();
        root.setPadding(18, 16, 18, 24);
        root.setBackgroundColor(bg);
        scroll.addView(root);
        setContentView(scroll);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView logo = bold("CRT", 25, blue);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(shape(card, 14));
        header.addView(logo);

        LinearLayout names = vertical();
        names.addView(bold("CRT TJ", 21, white));
        names.addView(txt("TRADING JOURNAL", 10, muted));

        LinearLayout.LayoutParams np =
            new LinearLayout.LayoutParams(0, -2, 1);
        np.setMargins(12, 0, 0, 0);
        header.addView(names, np);
        header.addView(txt(page, 12, muted));

        root.addView(header);
        root.addView(txt(" ", 5, muted));
    }

    void addStat(LinearLayout row, String title,
                 String value, int color) {
        LinearLayout box = vertical();
        box.setPadding(12, 14, 12, 14);
        box.setBackground(shape(card, 18));

        box.addView(txt(title, 12, muted));
        box.addView(bold(value, 24, color));

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(0, -2, 1);
        p.setMargins(4, 5, 4, 5);
        row.addView(box, p);
    }

    EditText field(String hint, int inputType, boolean multiline) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(muted);
        e.setTextColor(white);
        e.setTextSize(15);
        e.setInputType(inputType);
        e.setPadding(14, 12, 14, 12);
        e.setBackground(shape(card, 14));

        if (multiline) {
            e.setMinLines(3);
            e.setGravity(Gravity.TOP);
        } else {
            e.setSingleLine(true);
        }

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 5, 0, 10);
        e.setLayoutParams(p);
        return e;
    }

    void showDashboard() {
        loadTrades();
        setup("OVERVIEW");

        int wins = 0, losses = 0, be = 0;

        for (int i = 0; i < trades.length(); i++) {
            JSONObject t = trades.optJSONObject(i);
            if (t == null) continue;

            String r = t.optString("result");
            if (r.equals("Win")) wins++;
            else if (r.equals("Loss")) losses++;
            else if (r.equals("Break Even")) be++;
        }

        int total = trades.length();
        int decided = wins + losses;
        double rate = decided == 0 ? 0 :
            wins * 100.0 / decided;

        root.addView(bold("Trading Performance", 24, white));
        root.addView(txt(
            "Build discipline. Track every setup.",
            13, muted
        ));

        LinearLayout row1 = new LinearLayout(this);
        root.addView(row1);
        addStat(row1, "TOTAL TRADES", "" + total, white);
        addStat(row1, "WIN RATE",
            String.format(java.util.Locale.US, "%.1f%%", rate),
            green);

        LinearLayout row2 = new LinearLayout(this);
        root.addView(row2);
        addStat(row2, "WINS", "" + wins, green);
        addStat(row2, "LOSSES", "" + losses, red);

        LinearLayout row3 = new LinearLayout(this);
        root.addView(row3);
        addStat(row3, "BREAK EVEN", "" + be, blue);

        root.addView(txt("QUICK ACTIONS", 14, muted));

        Button add = btn("+ ADD NEW TRADE");
        root.addView(add);
        add.setOnClickListener(v -> showAddTrade());

        Button history = btn("TRADE HISTORY");
        root.addView(history);
        history.setOnClickListener(v -> showHistory());

        root.addView(txt(
            "CRT TJ  |  Trade with a plan.",
            12, muted
        ));
    }

    void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode,
            int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            selectedImage = data.getData();

            try {
                getContentResolver().takePersistableUriPermission(
                    selectedImage,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            } catch (Exception ignored) {}

            Toast.makeText(this,
                "Screenshot selected",
                Toast.LENGTH_SHORT).show();

            showAddTrade();
        }
    }

    void showAddTrade() {
        setup("NEW TRADE");
        selectedImage = null;

        root.addView(bold("Record Your Trade", 22, white));
        root.addView(txt("Enter your setup details below.", 13, muted));

        EditText pair = field("Pair (e.g. EURUSD)", 1, false);
        EditText direction = field("Direction (Buy / Sell)", 1, false);
        EditText entry = field("Entry Price", 8194, false);
        EditText stop = field("Stop Loss", 8194, false);
        EditText target = field("Take Profit", 8194, false);

        root.addView(pair);
        root.addView(direction);
        root.addView(entry);
        root.addView(stop);
        root.addView(target);

        root.addView(txt("TRADE RESULT", 13, muted));

        Spinner result = new Spinner(this);
        String[] choices = {"Win", "Loss", "Break Even"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            choices
        );
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        );
        result.setAdapter(adapter);
        root.addView(result);

        EditText notes = field("CRT Analysis / Notes",
            1, true);
        root.addView(notes);

        root.addView(txt("TRADE SCREENSHOT", 14, muted));

        Button photo = btn("+ ADD SCREENSHOT");
        root.addView(photo);
        photo.setOnClickListener(v -> chooseImage());

        if (selectedImage != null) {
            TextView selected = txt(
                "Screenshot selected ✓", 13, green);
            root.addView(selected);
        }

        Button save = btn("SAVE TRADE");
        root.addView(save);

        save.setOnClickListener(v -> {
            if (pair.getText().toString().trim().isEmpty()) {
                pair.setError("Enter trading pair");
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
                trade.put("date",
                    java.text.DateFormat.getDateTimeInstance()
                    .format(new java.util.Date()));

                trade.put("image",
                    selectedImage == null ? "" :
                    selectedImage.toString());

                trades.put(trade);
                saveTrades();

                Toast.makeText(this,
                    "Trade Saved Successfully!",
                    Toast.LENGTH_SHORT).show();

                showDashboard();

            } catch (Exception e) {
                Toast.makeText(this,
                    "Could not save trade",
                    Toast.LENGTH_SHORT).show();
            }
        });

        Button back = btn("BACK TO DASHBOARD");
        root.addView(back);
        back.setOnClickListener(v -> showDashboard());
    }

    void showHistory() {
        loadTrades();
        setup("HISTORY");

        root.addView(bold("Trade History", 23, white));
        root.addView(txt(
            "Your recorded trading setups.",
            13, muted
        ));

        if (trades.length() == 0) {
            root.addView(txt(
                "No trades recorded yet.",
                16, muted
            ));
        }

        for (int i = trades.length() - 1; i >= 0; i--) {
            JSONObject t = trades.optJSONObject(i);
            if (t == null) continue;

            LinearLayout box = vertical();
            box.setPadding(14, 14, 14, 14);
            box.setBackground(shape(card, 18));

            String result = t.optString("result");
            int resultColor = result.equals("Win") ? green :
                result.equals("Loss") ? red : blue;

            box.addView(bold(
                t.optString("pair") + "  |  " +
                t.optString("direction"),
                18, white
            ));

            box.addView(bold(
                "RESULT: " + result, 14, resultColor
            ));

            box.addView(txt(
                "Entry: " + t.optString("entry") +
                "   SL: " + t.optString("stop") +
                "   TP: " + t.optString("target"),
                13, muted
            ));

            box.addView(txt(
                t.optString("date"), 12, muted
            ));

            box.addView(txt(
                "Notes: " + t.optString("notes"),
                13, white
            ));

            String image = t.optString("image", "");

            if (!image.isEmpty()) {
                try {
                    ImageView preview = new ImageView(this);
                    preview.setImageURI(Uri.parse(image));
                    preview.setScaleType(
                        ImageView.ScaleType.CENTER_CROP
                    );
                    LinearLayout.LayoutParams ip =
                        new LinearLayout.LayoutParams(-1, 190);
                    ip.setMargins(0, 10, 0, 8);
                    preview.setLayoutParams(ip);
                    box.addView(preview);

                    preview.setOnClickListener(v -> {
                        try {
                            Intent open = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(image)
                            );
                            open.setDataAndType(
                                Uri.parse(image), "image/*"
                            );
                            open.addFlags(
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                            startActivity(open);
                        } catch (Exception ignored) {}
                    });
                } catch (Exception ignored) {}
            }

            LinearLayout.LayoutParams bp =
                new LinearLayout.LayoutParams(-1, -2);
            bp.setMargins(0, 8, 0, 12);
            root.addView(box, bp);
        }

        Button back = btn("BACK TO DASHBOARD");
        root.addView(back);
        back.setOnClickListener(v -> showDashboard());
    }
}
