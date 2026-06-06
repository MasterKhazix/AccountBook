package com.example.accountbook;

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class StatisticsActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private LinearLayout categoryTotalContainer;
    private TextView emptyStatisticsTextView;
    private TextView balanceTextView;
    private TextView incomeTextView;
    private TextView expenseTextView;
    private TextView monthTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.statistics_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        bindViews();
        loadStatistics();
    }

    private void bindViews() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        categoryTotalContainer = findViewById(R.id.category_total_container);
        emptyStatisticsTextView = findViewById(R.id.tv_empty_statistics);
        balanceTextView = findViewById(R.id.tv_stat_balance);
        incomeTextView = findViewById(R.id.tv_stat_income);
        expenseTextView = findViewById(R.id.tv_stat_expense);
        monthTextView = findViewById(R.id.tv_statistics_month);
    }

    private void loadStatistics() {
        int userId = sessionManager.getUserId();
        String monthPrefix = new SimpleDateFormat("yyyy-MM", Locale.CHINA).format(new Date());
        monthTextView.setText(monthPrefix + " 收支概览");

        double income = databaseHelper.getMonthlyTotal(userId, RecordEditActivity.TYPE_INCOME, monthPrefix);
        double expense = databaseHelper.getMonthlyTotal(userId, RecordEditActivity.TYPE_EXPENSE, monthPrefix);
        double balance = income - expense;

        balanceTextView.setText(formatMoney(balance));
        incomeTextView.setText(formatMoney(income));
        expenseTextView.setText(formatMoney(expense));
        loadExpenseCategories(userId, monthPrefix, expense);
    }

    private void loadExpenseCategories(int userId, String monthPrefix, double totalExpense) {
        categoryTotalContainer.removeAllViews();
        try (Cursor cursor = databaseHelper.getMonthlyCategoryTotals(
                userId,
                RecordEditActivity.TYPE_EXPENSE,
                monthPrefix)) {
            boolean hasData = cursor.getCount() > 0;
            emptyStatisticsTextView.setVisibility(hasData ? View.GONE : View.VISIBLE);
            while (cursor.moveToNext()) {
                String category = cursor.getString(0);
                double amount = cursor.getDouble(1);
                categoryTotalContainer.addView(createCategoryRow(category, amount, totalExpense));
            }
        }
    }

    private View createCategoryRow(String category, double amount, double totalExpense) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setBackgroundResource(R.drawable.bg_card);
        row.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, dp(12));
        row.setLayoutParams(rowParams);

        LinearLayout line = new LinearLayout(this);
        line.setGravity(Gravity.CENTER_VERTICAL);
        line.setOrientation(LinearLayout.HORIZONTAL);

        TextView categoryTextView = new TextView(this);
        categoryTextView.setText(category);
        categoryTextView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        categoryTextView.setTextSize(16);
        categoryTextView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        categoryTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView amountTextView = new TextView(this);
        amountTextView.setText(formatMoney(amount));
        amountTextView.setTextColor(ContextCompat.getColor(this, R.color.expense));
        amountTextView.setTextSize(16);
        amountTextView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView percentTextView = new TextView(this);
        double percent = totalExpense <= 0 ? 0 : amount * 100 / totalExpense;
        percentTextView.setText(String.format(Locale.CHINA, "%.1f%%", percent));
        percentTextView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        percentTextView.setTextSize(13);
        percentTextView.setPadding(0, dp(8), 0, 0);

        line.addView(categoryTextView);
        line.addView(amountTextView);
        row.addView(line);
        row.addView(percentTextView);
        return row;
    }

    private String formatMoney(double value) {
        return String.format(Locale.CHINA, "¥ %.2f", value);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
