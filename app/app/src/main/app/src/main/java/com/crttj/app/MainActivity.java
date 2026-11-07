
package com.crttj.app;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import android.content.*;
import org.json.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    int blue = Color.rgb(25, 100, 220);
    int navy = Color.rgb(16, 35, 70);
    int bg = Color.rgb(242, 246, 252);

    LinearLayout root, history;
    TextView stats;
    EditText pair, entry, exit, notes;
    Spinner setup, result;
    SharedPreferences prefs;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("CRT_TJ", MODE_PRIVATE);
        createUI();
        refresh();
    }

    TextView label(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        return t;
    }

    EditText field(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextSize(15);
        return e;
    }

    void createUI() {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(1);
        root.setPadding(20,20,20,20);
        root.setBackgroundColor(bg);
        scroll.addView(root);
        setContentView(scroll);

        TextView title = label("CRT TJ",30,Color.WHITE);
        title.setTypeface(null,1);

        TextView subtitle = label(
            "TRADING JOURNAL  |  BLUE EDITION",
            13,Color.WHITE);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(1);
        header.setPadding(20,25,20,25);
        header.setBackgroundColor(navy);
        header.addView(title);
        header.addView(subtitle);
        root.addView(header);

        stats = label("Dashboard",18,navy);
        stats.setPadding(10,25,10,25);
        root.addView(stats);

        TextView heading = label("ADD NEW TRADE",20,blue);
        heading.setTypeface(null,1);
        root.addView(heading);

        pair = field("Market (EURUSD, NAS100...)");
        entry = field("Entry Price");
        exit = field("Exit Price");
        notes = field("Trade Notes");

        root.addView(pair);
        root.addView(entry);
        root.addView(exit);
        root.addView(notes);

        setup = new Spinner(this);
        setup.setAdapter(new ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_dropdown_item,
            new String[]{"CRT","Liquidity Sweep","CHoCH",
            "FVG","CRT + Liquidity + CHoCH + FVG"}));

        result = new Spinner(this);
        result.setAdapter(new ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Win","Loss","Break Even"}));

        root.addView(label("Strategy",15,navy));
        root.addView(setup);
        root.addView(label("Trade Result",15,navy));
        root.addView(result);

        Button save = new Button(this);
        save.setText("SAVE TRADE");
        save.setTextColor(Color.WHITE);
        save.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(blue));
        save.setOnClickListener(v -> saveTrade());
        root.addView(save);

        root.addView(label("TRADE HISTORY",20,blue));
        history = new LinearLayout(this);
        history.setOrientation(1);
        root.addView(history);
    }

    void saveTrade() {
        String market = pair.getText().toString().trim();

        if (market.isEmpty()) {
            pair.setError("Enter market name");
            return;
        }

        try {
            JSONObject trade = new JSONObject();
            trade.put("market",market);
            trade.put("entry",entry.getText().toString());
            trade.put("exit",exit.getText().toString());
            trade.put("notes",notes.getText().toString());
            trade.put("setup",setup.getSelectedItem().toString());
            trade.put("result",result.getSelectedItem().toString());
            trade.put("date",new SimpleDateFormat(
                "yyyy-MM-dd HH:mm",Locale.getDefault()
            ).format(new Date()));

            JSONArray trades = new JSONArray(
                prefs.getString("trades","[]"));

            trades.put(0,trade);

            prefs.edit().putString(
                "trades",trades.toString()).apply();

            pair.setText("");
            entry.setText("");
            exit.setText("");
            notes.setText("");

            refresh();

            Toast.makeText(this,
                "Trade Saved Successfully!",
                Toast.LENGTH_SHORT).show();

        } catch(Exception e) {
            Toast.makeText(this,
                "Error saving trade",
                Toast.LENGTH_SHORT).show();
        }
    }

    void refresh() {
        try {
            JSONArray trades = new JSONArray(
                prefs.getString("trades","[]"));

            int wins=0, losses=0;

            for(int i=0;i<trades.length();i++) {
                String r=trades.getJSONObject(i)
                    .optString("result");

                if(r.equals("Win")) wins++;
                if(r.equals("Loss")) losses++;
            }

            int total=trades.length();

            double rate=total==0?0:
                (wins*100.0/total);

            stats.setText(
                "DASHBOARD\n\n"+
                "Total Trades: "+total+"\n"+
                "Wins: "+wins+"\n"+
                "Losses: "+losses+"\n"+
                "Win Rate: "+String.format(
                    Locale.US,"%.1f",rate)+"%"
            );

            history.removeAllViews();

            for(int i=0;i<total;i++) {
                JSONObject t=trades.getJSONObject(i);

                TextView card=label(
                    t.optString("market")+
                    "  |  "+t.optString("result")+
                    "\n"+t.optString("date")+
                    "\nStrategy: "+t.optString("setup")+
                    "\nEntry: "+t.optString("entry")+
                    "  Exit: "+t.optString("exit")+
                    "\n"+t.optString("notes"),
                    14,navy);

                card.setPadding(15,15,15,15);
                card.setBackgroundColor(Color.WHITE);

                LinearLayout.LayoutParams p =
                    new LinearLayout.LayoutParams(-1,-2);
                p.setMargins(0,8,0,8);

                history.addView(card,p);
            }

        } catch(Exception ignored) {}
    }
}
