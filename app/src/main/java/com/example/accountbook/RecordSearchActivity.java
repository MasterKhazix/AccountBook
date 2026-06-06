package com.example.accountbook;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.accountbook.db.DatabaseHelper;
import com.example.accountbook.util.SessionManager;

import java.util.ArrayList;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RecordSearchActivity extends AppCompatActivity {

    private Spinner typeSpinner;
    private Spinner categorySpinner;
    private EditText keywordEditText;
    private LinearLayout resultContainer;
    private TextView emptyTextView;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_record_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.record_search_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        bindViews();
        initFilters();
        loadResults();
    }

    private void bindViews() {
        typeSpinner = findViewById(R.id.sp_filter_type);
        categorySpinner = findViewById(R.id.sp_filter_category);
        keywordEditText = findViewById(R.id.et_filter_keyword);
        resultContainer = findViewById(R.id.search_result_container);
        emptyTextView = findViewById(R.id.tv_empty_search);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_apply_filter).setOnClickListener(v -> loadResults());
        findViewById(R.id.btn_reset_filter).setOnClickListener(v -> resetFilters());
    }

    private void initFilters() {
        setSpinnerItems(typeSpinner, new String[]{"全部类型", "支出", "收入"});
        refreshCategoryFilter();
    }

    private void setSpinnerItems(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void refreshCategoryFilter() {
        ArrayList<String> categories = new ArrayList<>();
        categories.add("全部分类");
        try (Cursor cursor = databaseHelper.getAllCategories(sessionManager.getUserId())) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                if (!categories.contains(name)) {
                    categories.add(name);
                }
            }
        }
        setSpinnerItems(categorySpinner, categories.toArray(new String[0]));
    }

    private void resetFilters() {
        typeSpinner.setSelection(0);
        categorySpinner.setSelection(0);
        keywordEditText.setText("");
        loadResults();
    }

    private void loadResults() {
        resultContainer.removeAllViews();
        try (Cursor cursor = databaseHelper.searchRecords(
                sessionManager.getUserId(),
                getSelectedType(),
                getSelectedCategory(),
                keywordEditText.getText().toString().trim())) {
            emptyTextView.setVisibility(cursor.getCount() == 0 ? View.VISIBLE : View.GONE);
            while (cursor.moveToNext()) {
                int recordId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("record_date"));
                String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));
                resultContainer.addView(createResultRow(recordId, type, category, amount, date, note));
            }
        }
    }

    private View createResultRow(int recordId, String type, String category, double amount, String date, String note) {
        boolean isIncome = RecordEditActivity.TYPE_INCOME.equals(type);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_card);
        row.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, dp(12));
        row.setLayoutParams(rowParams);

        LinearLayout infoColumn = new LinearLayout(this);
        infoColumn.setOrientation(LinearLayout.VERTICAL);
        infoColumn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView title = new TextView(this);
        title.setText(category);
        title.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView subtitle = new TextView(this);
        subtitle.setText(joinDateAndNote(date, note));
        subtitle.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        subtitle.setTextSize(13);
        subtitle.setPadding(0, dp(4), 0, 0);

        TextView amountText = new TextView(this);
        amountText.setText(String.format(Locale.CHINA, "%s¥ %.2f", isIncome ? "+" : "-", amount));
        amountText.setTextColor(ContextCompat.getColor(this, isIncome ? R.color.income : R.color.expense));
        amountText.setTextSize(16);
        amountText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        infoColumn.addView(title);
        infoColumn.addView(subtitle);
        row.addView(infoColumn);
        row.addView(amountText);
        row.setOnClickListener(v -> openEdit(recordId));
        return row;
    }

    private void openEdit(int recordId) {
        Intent intent = new Intent(this, RecordEditActivity.class);
        intent.putExtra(RecordEditActivity.EXTRA_RECORD_ID, recordId);
        startActivity(intent);
    }

    private String getSelectedType() {
        String value = String.valueOf(typeSpinner.getSelectedItem());
        if ("支出".equals(value)) {
            return RecordEditActivity.TYPE_EXPENSE;
        }
        if ("收入".equals(value)) {
            return RecordEditActivity.TYPE_INCOME;
        }
        return "";
    }

    private String getSelectedCategory() {
        String value = String.valueOf(categorySpinner.getSelectedItem());
        return "全部分类".equals(value) ? "" : value;
    }

    private String joinDateAndNote(String date, String note) {
        if (note == null || note.trim().isEmpty()) {
            return date;
        }
        return date + " · " + note;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
