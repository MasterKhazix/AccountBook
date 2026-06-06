package com.example.accountbook;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.accountbook.db.DatabaseHelper;
import com.example.accountbook.util.SessionManager;

import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RecordListActivity extends AppCompatActivity {

    private LinearLayout recordContainer;
    private TextView emptyTextView;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

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

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_record).setOnClickListener(v ->
                startActivity(new Intent(this, RecordEditActivity.class)));
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

        try (Cursor cursor = databaseHelper.getRecordsByUser(userId)) {
            emptyTextView.setVisibility(cursor.getCount() == 0 ? View.VISIBLE : View.GONE);
            while (cursor.moveToNext()) {
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("record_date"));
                String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));
                recordContainer.addView(createRecordRow(type, category, amount, date, note));
            }
        }
    }

    private View createRecordRow(String type, String category, double amount, String date, String note) {
        boolean isIncome = RecordEditActivity.TYPE_INCOME.equals(type);
        int amountColor = ContextCompat.getColor(this, isIncome ? R.color.income : R.color.expense);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
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
        subtitle.setText(TextUtilsCompat.joinDateAndNote(date, note));
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

        row.addView(infoColumn);
        row.addView(amountText);
        return row;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class TextUtilsCompat {
        static String joinDateAndNote(String date, String note) {
            if (note == null || note.trim().isEmpty()) {
                return date;
            }
            return date + " · " + note;
        }
    }
}
