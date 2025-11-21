package bank.Models;

/**
 * Shared contract for accounts that participate in the password reset flow.
 * This interface is implemented by both Customer and Employee models to support
 * password reset functionality.
 */
public interface PasswordResettable {
    String getEmail();
    String getSecurityQuestion();
    String getSecurityAnswer();
    void setPassword(String newPassword);
}
