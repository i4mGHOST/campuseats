package com.pawsitiveconnect;

public class UserSession {
    private static String token;
    private static String email;
    private static int roleId; // Added to track Admin vs Normal User

    public static void setSession(String jwtToken, String userEmail, int userRoleId) {
        token = jwtToken;
        email = userEmail;
        roleId = userRoleId;
    }

    public static String getToken() { return token; }
    public static String getEmail() { return email; }
    public static int getRoleId() { return roleId; } // New Getter

    public static void clearSession() {
        token = null;
        email = null;
        roleId = 0;
    }
}