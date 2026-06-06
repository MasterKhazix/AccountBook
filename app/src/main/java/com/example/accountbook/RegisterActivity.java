package com.example.accountbook;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.example.accountbook.db.DatabaseHelper;
import com.example.accountbook.util.AccountValidator;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameEditText = findViewById(R.id.et_register_username);
        passwordEditText = findViewById(R.id.et_register_password);
        confirmPasswordEditText = findViewById(R.id.et_confirm_password);
        databaseHelper = new DatabaseHelper(this);

        findViewById(R.id.btn_back_login).setOnClickListener(v -> finish());
        findViewById(R.id.btn_register).setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!AccountValidator.isUsernameValid(username)) {
            Toast.makeText(this, AccountValidator.usernameRuleText(), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!AccountValidator.isPasswordValid(password)) {
            Toast.makeText(this, AccountValidator.passwordRuleText(), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.isUsernameExists(username)) {
            Toast.makeText(this, "用户名已存在", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = databaseHelper.registerUser(username, password);
        if (result == -1) {
            Toast.makeText(this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
        finish();
    }
}
