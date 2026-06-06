package com.example.accountbook;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.accountbook.db.DatabaseHelper;
import com.example.accountbook.util.AccountValidator;
import com.example.accountbook.util.SessionManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;
    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);
        databaseHelper = new DatabaseHelper(this);
        bindViews();
    }

    private void bindViews() {
        TextView usernameTextView = findViewById(R.id.tv_username);
        oldPasswordEditText = findViewById(R.id.et_old_password);
        newPasswordEditText = findViewById(R.id.et_new_password);
        confirmPasswordEditText = findViewById(R.id.et_confirm_password);

        usernameTextView.setText(sessionManager.getUsername());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_change_password).setOnClickListener(v -> changePassword());
        findViewById(R.id.btn_logout).setOnClickListener(v -> logout());
    }

    private void changePassword() {
        int userId = sessionManager.getUserId();
        String oldPassword = oldPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "请输入旧密码和新密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "两次新密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!AccountValidator.isPasswordValid(newPassword)) {
            Toast.makeText(this, AccountValidator.passwordRuleText(), Toast.LENGTH_SHORT).show();
            return;
        }

        if (oldPassword.equals(newPassword)) {
            Toast.makeText(this, "新密码不能与旧密码相同", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!databaseHelper.checkPassword(userId, oldPassword)) {
            Toast.makeText(this, "旧密码错误", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.updatePassword(userId, newPassword) <= 0) {
            Toast.makeText(this, "修改失败，请重试", Toast.LENGTH_SHORT).show();
            return;
        }

        oldPasswordEditText.setText("");
        newPasswordEditText.setText("");
        confirmPasswordEditText.setText("");
        Toast.makeText(this, "密码已修改", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        sessionManager.logout();
        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
