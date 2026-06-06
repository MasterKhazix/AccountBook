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
import android.widget.Toast;

import com.example.accountbook.db.DatabaseHelper;
import com.example.accountbook.util.SessionManager;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RecordListActivity extends AppCompatActivity {

    private final Set<Integer> selectedRecordIds = new HashSet<>();
    private LinearLayout recordContainer;
    private TextView emptyTextView;
    private TextView manageButton;
    private TextView deleteSelectedButton;
    private Spinner filterTypeSpinner;
    private Spinner filterCategorySpinner;
    private EditText filterKeywordEditText;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private boolean manageMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_record_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.record_list_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        recordContainer = findViewById(R.id.record_container);
        emptyTextView = findViewById(R.id.tv_empty);
        manageButton = findViewById(R.id.btn_manage);
        deleteSelectedButton = findViewById(R.id.btn_delete_selected);
        filterTypeSpinner = findViewById(R.id.sp_filter_type);
        filterCategorySpinner = findViewById(R.id.sp_filter_category);
        filterKeywordEditText = findViewById(R.id.et_filter_keyword);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_record).setOnClickListener(v ->
                startActivity(new Intent(this, RecordEditActivity.class)));
        manageButton.setOnClickListener(v -> toggleManageMode());
        deleteSelectedButton.setOnClickListener(v -> confirmBatchDelete());
        findViewById(R.id.btn_apply_filter).setOnClickListener(v -> loadRecords());
        findViewById(R.id.btn_reset_filter).setOnClickListener(v -> resetFilters());

        initFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecords();
    }

    private void loadRecords() {
        recordContainer.removeAllViews();
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            emptyTextView.setVisibility(View.VISIBLE);
            return;
        }

        try (Cursor cursor = databaseHelper.searchRecords(
                userId,
                getSelectedType(),
                getSelectedCategory(),
                filterKeywordEditText.getText().toString().trim())) {
            boolean hasRecords = cursor.getCount() > 0;
            emptyTextView.setVisibility(hasRecords ? View.GONE : View.VISIBLE);
            manageButton.setVisibility(hasRecords ? View.VISIBLE : View.GONE);
            if (!hasRecords && manageMode) {
                exitManageMode();
            }

            while (cursor.moveToNext()) {
                int recordId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("record_date"));
                String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));
                recordContainer.addView(createRecordRow(recordId, type, category, amount, date, note));
            }
        }
    }

    private void initFilters() {
        setSpinnerItems(filterTypeSpinner, new String[]{"全部类型", "支出", "收入"});
        setSpinnerItems(filterCategorySpinner, new String[]{
                "全部分类", "餐饮", "交通", "购物", "生活缴费", "其他支出", "工资", "奖金", "兼职", "其他收入"
        });
    }

    private void setSpinnerItems(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private String getSelectedType() {
        String value = String.valueOf(filterTypeSpinner.getSelectedItem());
        if ("支出".equals(value)) {
            return RecordEditActivity.TYPE_EXPENSE;
        }
        if ("收入".equals(value)) {
            return RecordEditActivity.TYPE_INCOME;
        }
        return "";
    }

    private String getSelectedCategory() {
        String value = String.valueOf(filterCategorySpinner.getSelectedItem());
        return "全部分类".equals(value) ? "" : value;
    }

    private void resetFilters() {
        filterTypeSpinner.setSelection(0);
        filterCategorySpinner.setSelection(0);
        filterKeywordEditText.setText("");
        if (manageMode) {
            exitManageMode();
        } else {
            loadRecords();
        }
    }

    private View createRecordRow(int recordId, String type, String category, double amount, String date, String note) {
        boolean isIncome = RecordEditActivity.TYPE_INCOME.equals(type);
        int amountColor = ContextCompat.getColor(this, isIncome ? R.color.income : R.color.expense);

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

        infoColumn.addView(title);
        infoColumn.addView(subtitle);

        TextView amountText = new TextView(this);
        amountText.setText(String.format(Locale.CHINA, "%s¥ %.2f", isIncome ? "+" : "-", amount));
        amountText.setTextColor(amountColor);
        amountText.setTextSize(16);
        amountText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView selectCircle = new TextView(this);
        LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(dp(22), dp(22));
        circleParams.setMargins(dp(14), 0, 0, 0);
        selectCircle.setLayoutParams(circleParams);
        selectCircle.setVisibility(manageMode ? View.VISIBLE : View.GONE);
        applySelectionState(selectCircle, selectedRecordIds.contains(recordId));

        row.addView(infoColumn);
        row.addView(amountText);
        row.addView(selectCircle);
        row.setOnClickListener(v -> {
            if (manageMode) {
                toggleRecordSelection(recordId);
            } else {
                openEdit(recordId);
            }
        });
        return row;
    }

    private void openEdit(int recordId) {
        Intent intent = new Intent(this, RecordEditActivity.class);
        intent.putExtra(RecordEditActivity.EXTRA_RECORD_ID, recordId);
        startActivity(intent);
    }

    private void toggleManageMode() {
        if (manageMode) {
            exitManageMode();
        } else {
            manageMode = true;
            manageButton.setText("完成");
            deleteSelectedButton.setVisibility(View.VISIBLE);
            refreshDeleteButtonText();
            loadRecords();
        }
    }

    private void exitManageMode() {
        manageMode = false;
        selectedRecordIds.clear();
        manageButton.setText("管理");
        deleteSelectedButton.setVisibility(View.GONE);
        refreshDeleteButtonText();
        loadRecords();
    }

    private void toggleRecordSelection(int recordId) {
        if (selectedRecordIds.contains(recordId)) {
            selectedRecordIds.remove(recordId);
        } else {
            selectedRecordIds.add(recordId);
        }
        refreshDeleteButtonText();
        loadRecords();
    }

    private void confirmBatchDelete() {
        if (selectedRecordIds.isEmpty()) {
            Toast.makeText(this, "请先选择账单", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("批量删除账单")
                .setMessage(String.format(Locale.CHINA, "确定要删除选中的 %d 条账单吗？", selectedRecordIds.size()))
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> deleteSelectedRecords())
                .show();
    }

    private void deleteSelectedRecords() {
        int userId = sessionManager.getUserId();
        for (Integer recordId : selectedRecordIds) {
            databaseHelper.deleteRecord(recordId, userId);
        }
        Toast.makeText(this, "已删除所选账单", Toast.LENGTH_SHORT).show();
        exitManageMode();
    }

    private void refreshDeleteButtonText() {
        deleteSelectedButton.setText(selectedRecordIds.isEmpty()
                ? "删除所选"
                : String.format(Locale.CHINA, "删除所选（%d）", selectedRecordIds.size()));
    }

    private void applySelectionState(TextView circle, boolean selected) {
        circle.setBackgroundResource(selected ? R.drawable.bg_select_circle_on : R.drawable.bg_select_circle_off);
        circle.setGravity(Gravity.CENTER);
        circle.setText(selected ? "✓" : "");
        circle.setTextColor(ContextCompat.getColor(this, R.color.white));
        circle.setTextSize(14);
        circle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
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
