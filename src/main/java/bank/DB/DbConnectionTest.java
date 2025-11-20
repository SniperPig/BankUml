package bank.DB;

import java.sql.Connection;

public class DbConnectionTest {
    public static void main(String[] args) {
        System.out.println("Trying to connect...");

        try (Connection conn = DbManager.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connected to DB!");
                System.out.println("URL: " + conn.getMetaData().getURL());
                System.out.println("User: " + conn.getMetaData().getUserName());
            } else {
                System.out.println("❌ Connection is null or closed");
            }
        } catch (Exception e) {
            System.out.println("❌ Failed to connect:");
            e.printStackTrace();
        }
    }
}