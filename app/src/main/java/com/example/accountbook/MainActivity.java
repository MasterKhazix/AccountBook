package com.example.accountbook;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindHomeActions();
    }

    private void bindHomeActions() {
        findViewById(R.id.btn_profile).setOnClickListener(v -> showComingSoon("个人中心"));
        findViewById(R.id.btn_add_expense).setOnClickListener(v -> showComingSoon("记一笔支出"));
        findViewById(R.id.btn_add_income).setOnClickListener(v -> showComingSoon("记一笔收入"));
        findViewById(R.id.btn_records).setOnClickListener(v -> showComingSoon("账单列表"));
        findViewById(R.id.btn_statistics).setOnClickListener(v -> showComingSoon("统计分析"));
        findViewById(R.id.btn_categories).setOnClickListener(v -> showComingSoon("分类管理"));
        findViewById(R.id.btn_search).setOnClickListener(v -> showComingSoon("筛选查询"));
        findViewById(R.id.btn_view_all).setOnClickListener(v -> showComingSoon("查看全部账单"));
    }

    private void showComingSoon(String featureName) {
        Toast.makeText(this, featureName + "功能待接入", Toast.LENGTH_SHORT).show();
    }
}
