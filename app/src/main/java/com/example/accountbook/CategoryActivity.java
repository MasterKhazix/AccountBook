package com.example.accountbook;

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.accountbook.db.DatabaseHelper;
import com.example.accountbook.util.SessionManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CategoryActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private RadioButton expenseRadioButton;
    private EditText categoryNameEditText;
    private LinearLayout categoryContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.category_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        bindViews();
        loadCategories();
    }

    private void bindViews() {
        expenseRadioButton = findViewById(R.id.rb_expense);
        categoryNameEditText = findViewById(R.id.et_category_name);
        categoryContainer = findViewById(R.id.category_container);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_category).setOnClickListener(v -> addCategory());
        ((RadioGroup) findViewById(R.id.rg_category_type)).setOnCheckedChangeListener((group, checkedId) -> loadCategories());
    }

    private void addCategory() {
        String name = categoryNameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "请输入分类名称", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = databaseHelper.addCategory(sessionManager.getUserId(), name, getSelectedType(), false);
        if (result == -1) {
            Toast.makeText(this, "分类已存在", Toast.LENGTH_SHORT).show();
            return;
        }

        categoryNameEditText.setText("");
        Toast.makeText(this, "分类已添加", Toast.LENGTH_SHORT).show();
        loadCategories();
    }

    private void loadCategories() {
        categoryContainer.removeAllViews();
        try (Cursor cursor = databaseHelper.getCategoriesByType(sessionManager.getUserId(), getSelectedType())) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                boolean isDefault = cursor.getInt(cursor.getColumnIndexOrThrow("is_default")) == 1;
                categoryContainer.addView(createCategoryRow(id, name, isDefault));
            }
        }
    }

    private View createCategoryRow(int id, String name, boolean isDefault) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_card);
        row.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(12));
        row.setLayoutParams(params);

        TextView nameTextView = new TextView(this);
        nameTextView.setText(name);
        nameTextView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        nameTextView.setTextSize(16);
        nameTextView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        nameTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView actionTextView = new TextView(this);
        actionTextView.setText(isDefault ? "默认" : "删除");
        actionTextView.setTextColor(ContextCompat.getColor(this, isDefault ? R.color.text_secondary : R.color.expense));
        actionTextView.setTextSize(14);
        actionTextView.setGravity(Gravity.CENTER);
        actionTextView.setPadding(dp(12), 0, 0, 0);
        if (!isDefault) {
            actionTextView.setOnClickListener(v -> confirmDelete(id));
        }

        row.addView(nameTextView);
        row.addView(actionTextView);
        return row;
    }

    private void confirmDelete(int categoryId) {
        new AlertDialog.Builder(this)
                .setTitle("删除分类")
                .setMessage("确定要删除这个自定义分类吗？已存在账单不会被删除。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    databaseHelper.deleteCustomCategory(sessionManager.getUserId(), categoryId);
                    loadCategories();
                })
                .show();
    }

    private String getSelectedType() {
        return expenseRadioButton.isChecked() ? RecordEditActivity.TYPE_EXPENSE : RecordEditActivity.TYPE_INCOME;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
