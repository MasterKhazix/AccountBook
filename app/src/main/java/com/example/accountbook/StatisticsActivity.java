package com.example.accountbook;

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
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

public class StatisticsActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private LinearLayout categoryTotalContainer;
    private TextView emptyStatisticsTextView;
    private TextView balanceTextView;
    private TextView incomeTextView;
    private TextView expenseTextView;
    private TextView monthTextView;
    private EditText yearEditText;
    private EditText monthEditText;

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
        yearEditText = findViewById(R.id.et_stat_year);
        monthEditText = findViewById(R.id.et_stat_month);
        findViewById(R.id.btn_apply_statistics).setOnClickListener(v -> loadStatistics());

        Date now = new Date();
        yearEditText.setText(new SimpleDateFormat("yyyy", Locale.CHINA).format(now));
        monthEditText.setText(new SimpleDateFormat("M", Locale.CHINA).format(now));
    }

    private void loadStatistics() {
        int userId = sessionManager.getUserId();
        String datePrefix = buildDatePrefix();
        if (datePrefix == null) {
            return;
        }
        monthTextView.setText(buildPeriodTitle(datePrefix));

        double income = databaseHelper.getPeriodTotal(userId, RecordEditActivity.TYPE_INCOME, datePrefix);
        double expense = databaseHelper.getPeriodTotal(userId, RecordEditActivity.TYPE_EXPENSE, datePrefix);
        double balance = income - expense;

        balanceTextView.setText(formatMoney(balance));
        incomeTextView.setText(formatMoney(income));
        expenseTextView.setText(formatMoney(expense));
        loadExpenseCategories(userId, datePrefix, expense);
    }

    private String buildDatePrefix() {
        String year = yearEditText.getText().toString().trim();
        String month = monthEditText.getText().toString().trim();

        if (TextUtils.isEmpty(year)) {
            Toast.makeText(this, "年份不能为空", Toast.LENGTH_SHORT).show();
            return null;
        }

        if (!year.matches("\\d{4}")) {
            Toast.makeText(this, "年份格式应为 4 位数字", Toast.LENGTH_SHORT).show();
            return null;
        }

        if (TextUtils.isEmpty(month)) {
            return year;
        }

        int monthValue;
        try {
            monthValue = Integer.parseInt(month);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "月份格式不正确", Toast.LENGTH_SHORT).show();
            return null;
        }

        if (monthValue < 1 || monthValue > 12) {
            Toast.makeText(this, "月份必须在 1 到 12 之间", Toast.LENGTH_SHORT).show();
            return null;
        }

        return String.format(Locale.CHINA, "%s-%02d", year, monthValue);
    }

    private String buildPeriodTitle(String datePrefix) {
        if (datePrefix.length() == 4) {
            return datePrefix + " 年全年收支概览";
        }
        return datePrefix + " 收支概览";
    }

    private void loadExpenseCategories(int userId, String datePrefix, double totalExpense) {
        categoryTotalContainer.removeAllViews();
        try (Cursor cursor = databaseHelper.getPeriodCategoryTotals(
                userId,
                RecordEditActivity.TYPE_EXPENSE,
                datePrefix)) {
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
