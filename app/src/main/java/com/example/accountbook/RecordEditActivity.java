package com.example.accountbook;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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

public class RecordEditActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "type";
    public static final String TYPE_EXPENSE = "expense";
    public static final String TYPE_INCOME = "income";

    private RadioButton expenseRadioButton;
    private RadioButton incomeRadioButton;
    private EditText amountEditText;
    private EditText dateEditText;
    private EditText noteEditText;
    private Spinner categorySpinner;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_record_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.record_edit_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        bindViews();
        initForm();
    }

    private void bindViews() {
        expenseRadioButton = findViewById(R.id.rb_expense);
        incomeRadioButton = findViewById(R.id.rb_income);
        amountEditText = findViewById(R.id.et_amount);
        dateEditText = findViewById(R.id.et_date);
        noteEditText = findViewById(R.id.et_note);
        categorySpinner = findViewById(R.id.sp_category);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save_record).setOnClickListener(v -> saveRecord());
        ((RadioGroup) findViewById(R.id.rg_type)).setOnCheckedChangeListener((group, checkedId) -> refreshCategories());
    }

    private void initForm() {
        String type = getIntent().getStringExtra(EXTRA_TYPE);
        if (TYPE_INCOME.equals(type)) {
            incomeRadioButton.setChecked(true);
        } else {
            expenseRadioButton.setChecked(true);
        }

        dateEditText.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date()));
        refreshCategories();
    }

    private void refreshCategories() {
        String[] categories = incomeRadioButton.isChecked()
                ? new String[]{"工资", "奖金", "兼职", "其他收入"}
                : new String[]{"餐饮", "交通", "购物", "生活缴费", "其他支出"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        TextView titleTextView = findViewById(R.id.tv_record_title);
        titleTextView.setText(incomeRadioButton.isChecked() ? "记一笔收入" : "记一笔支出");
    }

    private void saveRecord() {
        int userId = sessionManager.getUserId();
        String amountText = amountEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        String note = noteEditText.getText().toString().trim();
        String type = incomeRadioButton.isChecked() ? TYPE_INCOME : TYPE_EXPENSE;
        String category = String.valueOf(categorySpinner.getSelectedItem());

        if (userId == -1) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(amountText)) {
            Toast.makeText(this, "请输入金额", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "金额格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "金额必须大于 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(this, "日期格式应为 yyyy-MM-dd", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = databaseHelper.addRecord(userId, type, category, amount, date, note);
        if (result == -1) {
            Toast.makeText(this, "保存失败，请重试", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "账单已保存", Toast.LENGTH_SHORT).show();
        finish();
    }
}
