package com.example.convertio;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.widget.GridLayout; // ✅ FIXED

public class MainActivity extends AppCompatActivity {

    EditText amount;
    Spinner fromCurrency, toCurrency;
    TextView result, rateText, settingsIcon;
    Button swapBtn;
    GridLayout keypad;

    String[] currencies = {"INR", "USD", "JPY", "EUR"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("darkMode", false);

        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_main);

        amount = findViewById(R.id.amount);
        fromCurrency = findViewById(R.id.fromCurrency);
        toCurrency = findViewById(R.id.toCurrency);
        result = findViewById(R.id.result);
        rateText = findViewById(R.id.rateText);
        swapBtn = findViewById(R.id.swapBtn);
        settingsIcon = findViewById(R.id.settingsIcon);
        keypad = findViewById(R.id.keypad);

        // Disable keyboard safely
        try {
            amount.setShowSoftInputOnFocus(false);
        } catch (Exception ignored) {}

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, currencies);

        fromCurrency.setAdapter(adapter);
        toCurrency.setAdapter(adapter);

        swapBtn.setOnClickListener(v -> swapCurrencies());
        settingsIcon.setOnClickListener(v -> openSettingsPopup());

        // Keypad handling
        for (int i = 0; i < keypad.getChildCount(); i++) {
            View v = keypad.getChildAt(i);
            if (v instanceof Button) {
                Button btn = (Button) v;
                btn.setOnClickListener(view -> handleInput(btn.getText().toString()));
            }
        }
    }

    public void convert(View view) {
        String input = amount.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amt = Double.parseDouble(input);

        String from = fromCurrency.getSelectedItem().toString();
        String to = toCurrency.getSelectedItem().toString();

        double converted = convertCurrency(from, to, amt);
        double rate = convertCurrency(from, to, 1);

        rateText.setText("1 " + from + " = " + String.format("%.2f", rate) + " " + to);
        result.setText("Result: " + String.format("%.2f", converted));
    }

    private double convertCurrency(String from, String to, double amt) {
        double inr;

        switch (from) {
            case "USD": inr = amt * 93; break;
            case "JPY": inr = amt * 0.58; break;
            case "EUR": inr = amt * 108.174; break;
            default: inr = amt;
        }

        switch (to) {
            case "USD": return inr / 93;
            case "JPY": return inr / 0.58;
            case "EUR": return inr / 108.174;
            default: return inr;
        }
    }

    private void swapCurrencies() {
        int fromPos = fromCurrency.getSelectedItemPosition();
        int toPos = toCurrency.getSelectedItemPosition();

        fromCurrency.setSelection(toPos);
        toCurrency.setSelection(fromPos);
    }

    private void openSettingsPopup() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Theme");

        String[] options = {"Light Mode", "Dark Mode"};

        builder.setItems(options, (dialog, which) -> {
            SharedPreferences.Editor editor = prefs.edit();

            if (which == 0) {
                editor.putBoolean("darkMode", false);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                editor.putBoolean("darkMode", true);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }

            editor.apply();
            recreate();
        });

        builder.show();
    }

    private void handleInput(String value) {
        String current = amount.getText().toString();

        switch (value) {
            case "AC":
                amount.setText("");
                break;

            case "⌫":
                if (!current.isEmpty()) {
                    amount.setText(current.substring(0, current.length() - 1));
                }
                break;

            case ".":
                if (!current.contains(".")) {
                    amount.append(".");
                }
                break;

            default:
                amount.append(value);
        }
    }
}