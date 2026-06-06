package com.example.accountbook;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.accountbook.db.DatabaseHelper;
import com.example.accountbook.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;
    private TextView balanceTextView;
    private TextView incomeTextView;
    private TextView expenseTextView;

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
    }

    private void bindHomeActions() {
        findViewById(R.id.btn_profile).setOnClickListener(v -> logout());
        findViewById(R.id.btn_add_expense).setOnClickListener(v -> openRecordEdit(RecordEditActivity.TYPE_EXPENSE));
        findViewById(R.id.btn_add_income).setOnClickListener(v -> openRecordEdit(RecordEditActivity.TYPE_INCOME));
        findViewById(R.id.btn_records).setOnClickListener(v -> openRecordList());
        findViewById(R.id.btn_statistics).setOnClickListener(v -> showComingSoon("统计分析"));
        findViewById(R.id.btn_categories).setOnClickListener(v -> showComingSoon("分类管理"));
        findViewById(R.id.btn_search).setOnClickListener(v -> openRecordList());
        findViewById(R.id.btn_view_all).setOnClickListener(v -> openRecordList());
    }

    private void bindSummaryViews() {
        balanceTextView = findViewById(R.id.tv_balance);
        incomeTextView = findViewById(R.id.tv_income);
        expenseTextView = findViewById(R.id.tv_expense);
    }

    private void refreshMonthlySummary() {
        if (balanceTextView == null) {
            return;
        }

        int userId = sessionManager.getUserId();
        String monthPrefix = new SimpleDateFormat("yyyy-MM", Locale.CHINA).format(new Date());
        double income = databaseHelper.getMonthlyTotal(userId, RecordEditActivity.TYPE_INCOME, monthPrefix);
        double expense = databaseHelper.getMonthlyTotal(userId, RecordEditActivity.TYPE_EXPENSE, monthPrefix);
        double balance = income - expense;

        balanceTextView.setText(String.format(Locale.CHINA, "¥ %.2f", balance));
        incomeTextView.setText(String.format(Locale.CHINA, "¥ %.2f", income));
        expenseTextView.setText(String.format(Locale.CHINA, "¥ %.2f", expense));
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
}
