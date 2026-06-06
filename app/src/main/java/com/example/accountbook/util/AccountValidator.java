package com.example.accountbook.util;

public final class AccountValidator {

    private AccountValidator() {
    }

    public static boolean isUsernameValid(String username) {
        return username != null && username.length() >= 1 && username.length() <= 10;
    }

    public static boolean isPasswordValid(String password) {
        return password != null && password.length() >= 6 && password.length() <= 12;
    }

    public static String usernameRuleText() {
        return "用户名长度需为 1-10 位";
    }

    public static String passwordRuleText() {
        return "密码长度需为 6-12 位";
    }
}
