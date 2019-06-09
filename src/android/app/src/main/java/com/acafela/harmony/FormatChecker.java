package com.acafela.harmony;

public class FormatChecker {
    private static final String LOG_TAG = FormatChecker.class.getName();

    public static boolean isValidEmail(String email) {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false;
        }
        return true;
    }

    public static boolean isValidPassword(String password) {
        if (password.isEmpty() || password.length() != 4) {
            return false;
        }
        return true;
    }

    public static boolean isValidRepeatPassword(String password, String repeatPassword) {
        if (!repeatPassword.equals(password)) {
            return false;
        }
        return true;
    }
}
