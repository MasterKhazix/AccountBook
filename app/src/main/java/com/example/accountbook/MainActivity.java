package com.example.accountbook;

import android.os.Bundle;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.accountbook.db.DatabaseHelper;
import com.example.accountbook.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;
    private TextView balanceTextView;
    private TextView incomeTextView;
    private TextView expenseTextView;
    private TextView monthTextView;
    private LinearLayout recentListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        databaseHelper = new DatabaseHelper(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindHomeActions();
        bindSummaryViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshMonthlySummary();
        refreshRecentRecords();
    }

    private void bindHomeActions() {
        findViewById(R.id.btn_profile).setOnClickListener(v -> openProfile());
        findViewById(R.id.btn_add_expense).setOnClickListener(v -> openRecordEdit(RecordEditActivity.TYPE_EXPENSE));
        findViewById(R.id.btn_add_income).setOnClickListener(v -> openRecordEdit(RecordEditActivity.TYPE_INCOME));
        findViewById(R.id.btn_records).setOnClickListener(v -> openRecordList());
        findViewById(R.id.btn_statistics).setOnClickListener(v -> openStatistics());
        findViewById(R.id.btn_categories).setOnClickListener(v -> openCategories());
        findViewById(R.id.btn_search).setOnClickListener(v -> openRecordSearch());
        findViewById(R.id.btn_view_all).setOnClickListener(v -> openRecordList());
    }

    private void bindSummaryViews() {
        monthTextView = findViewById(R.id.tv_month);
        balanceTextView = findViewById(R.id.tv_balance);
        incomeTextView = findViewById(R.id.tv_income);
        expenseTextView = findViewById(R.id.tv_expense);
        recentListLayout = findViewById(R.id.recent_list);
    }

    private void refreshMonthlySummary() {
        if (balanceTextView == null) {
            return;
        }

        int userId = sessionManager.getUserId();
        Date now = new Date();
        String monthPrefix = new SimpleDateFormat("yyyy-MM", Locale.CHINA).format(now);
        monthTextView.setText(new SimpleDateFormat("yyyy 年 M 月账单总览", Locale.CHINA).format(now));
        double income = databaseHelper.getMonthlyTotal(userId, RecordEditActivity.TYPE_INCOME, monthPrefix);
        double expense = databaseHelper.getMonthlyTotal(userId, RecordEditActivity.TYPE_EXPENSE, monthPrefix);
        double balance = income - expense;

        balanceTextView.setText(String.format(Locale.CHINA, "¥ %.2f", balance));
        incomeTextView.setText(String.format(Locale.CHINA, "¥ %.2f", income));
        expenseTextView.setText(String.format(Locale.CHINA, "¥ %.2f", expense));
    }

    private void refreshRecentRecords() {
        if (recentListLayout == null) {
            return;
        }

        recentListLayout.removeAllViews();
        try (Cursor cursor = databaseHelper.getRecentRecords(sessionManager.getUserId(), 3)) {
            if (cursor.getCount() == 0) {
                recentListLayout.addView(createEmptyRecentView());
                return;
            }

            while (cursor.moveToNext()) {
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("record_date"));
                String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));
                recentListLayout.addView(createRecentRecordRow(type, category, amount, date, note));
            }
        }
    }

    private View createEmptyRecentView() {
        TextView emptyTextView = new TextView(this);
        emptyTextView.setText("暂无账单，先记一笔");
        emptyTextView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        emptyTextView.setTextSize(15);
        emptyTextView.setGravity(Gravity.CENTER);
        emptyTextView.setMinHeight(dp(72));
        return emptyTextView;
    }

    private View createRecentRecordRow(String type, String category, double amount, String date, String note) {
        boolean isIncome = RecordEditActivity.TYPE_INCOME.equals(type);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(4), dp(10), dp(4), dp(10));

        TextView typeTextView = new TextView(this);
        typeTextView.setText(isIncome ? "收入" : "支出");
        typeTextView.setTextColor(ContextCompat.getColor(this, isIncome ? R.color.income : R.color.expense));
        typeTextView.setTextSize(13);
        typeTextView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        typeTextView.setGravity(Gravity.CENTER);
        typeTextView.setBackgroundResource(isIncome ? R.drawable.bg_income_badge : R.drawable.bg_expense_badge);
        row.addView(typeTextView, new LinearLayout.LayoutParams(dp(48), dp(32)));

        LinearLayout infoColumn = new LinearLayout(this);
        infoColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        infoParams.setMargins(dp(12), 0, dp(10), 0);

        TextView categoryTextView = new TextView(this);
        categoryTextView.setText(category);
        categoryTextView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        categoryTextView.setTextSize(15);
        categoryTextView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView detailTextView = new TextView(this);
        detailTextView.setText(joinDateAndNote(date, note));
        detailTextView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        detailTextView.setTextSize(13);
        detailTextView.setPadding(0, dp(2), 0, 0);

        infoColumn.addView(categoryTextView);
        infoColumn.addView(detailTextView);
        row.addView(infoColumn, infoParams);

        TextView amountTextView = new TextView(this);
        amountTextView.setText(String.format(Locale.CHINA, "%s¥ %.2f", isIncome ? "+" : "-", amount));
        amountTextView.setTextColor(ContextCompat.getColor(this, isIncome ? R.color.income : R.color.expense));
        amountTextView.setTextSize(16);
        amountTextView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        row.addView(amountTextView);
        return row;
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

    private void showComingSoon(String featureName) {
        Toast.makeText(this, featureName + "功能待接入", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        sessionManager.logout();
        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
        startActivity(new android.content.Intent(this, LoginActivity.class));
        finish();
    }

    private void openRecordEdit(String type) {
        android.content.Intent intent = new android.content.Intent(this, RecordEditActivity.class);
        intent.putExtra(RecordEditActivity.EXTRA_TYPE, type);
        startActivity(intent);
    }

    private void openRecordList() {
        startActivity(new android.content.Intent(this, RecordListActivity.class));
    }

    private void openStatistics() {
        startActivity(new android.content.Intent(this, StatisticsActivity.class));
    }

    private void openRecordSearch() {
        startActivity(new android.content.Intent(this, RecordSearchActivity.class));
    }

    private void openCategories() {
        startActivity(new android.content.Intent(this, CategoryActivity.class));
    }

    private void openProfile() {
        startActivity(new android.content.Intent(this, ProfileActivity.class));
    }
}
