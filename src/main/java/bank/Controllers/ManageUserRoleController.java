package bank.Controllers;

import bank.Controllers.AdminDashboardController.UserRecord;
import bank.DB.BankDb;
import bank.Models.Branch;
import bank.Models.Employee;
import bank.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controller for the Manage User Role screen.
 * Uses BankDb.adminUpdateUserRole to apply changes.
 */
public class ManageUserRoleController {

    @FXML private Label userTypeLabel;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label currentRoleLabel;
    @FXML private Label currentBranchLabel;

    @FXML private ComboBox<String> roleCombo;
    @FXML private Label branchLabel;
    @FXML private ComboBox<Branch> branchCombo;

    @FXML private Label newSecPwdLabel;
    @FXML private PasswordField newSecondaryPasswordField;

    @FXML private PasswordField adminSecondaryPasswordField;

    @FXML private Label errorLabel;

    private final BankDb bankDb = new BankDb();

    private UserRecord selectedUser;
    private final ObservableList<Branch> availableBranches =
            FXCollections.observableArrayList();

    /**
     * Called by AdminDashboardController after loading this FXML.
     *
     * @param selectedUser the user record selected in the admin dashboard
     *                     whose role/branch may be updated in this screen
     */
    public void initializeData(UserRecord selectedUser) {
        this.selectedUser = selectedUser;

        if (selectedUser == null) {
            errorLabel.setText("No user data provided.");
            return;
        }

        // Populate read-only labels
        userTypeLabel.setText(selectedUser.getUserType());
        nameLabel.setText(selectedUser.getName());
        emailLabel.setText(selectedUser.getEmail());
        currentRoleLabel.setText(
                selectedUser.getUserType().equals("CUSTOMER")
                        ? "Customer"
                        : (selectedUser.getRole() == null || selectedUser.getRole().isBlank()
                           ? "Employee"
                           : selectedUser.getRole())
        );
        currentBranchLabel.setText(
                selectedUser.getBranchName() == null
                        ? ""
                        : selectedUser.getBranchName()
        );

        // Populate role options
        roleCombo.setItems(FXCollections.observableArrayList(
                "Customer", "Teller", "Manager", "Admin"
        ));

        // Default to current role if available
        if ("CUSTOMER".equals(selectedUser.getUserType())) {
            roleCombo.getSelectionModel().select("Customer");
        } else if (selectedUser.getRole() != null && !selectedUser.getRole().isBlank()) {
            String currentRole = selectedUser.getRole();
            if (currentRole.equalsIgnoreCase("teller")) {
                roleCombo.getSelectionModel().select("Teller");
            } else if (currentRole.equalsIgnoreCase("manager")) {
                roleCombo.getSelectionModel().select("Manager");
            } else if (currentRole.equalsIgnoreCase("admin")) {
                roleCombo.getSelectionModel().select("Admin");
            }
        }

        // Load branches from DB
        loadBranches();

        // Initial visibility depending on selected role
        updateVisibilityForRole(roleCombo.getValue());

        // React to role changes
        roleCombo.valueProperty().addListener((obs, oldVal, newVal) ->
                updateVisibilityForRole(newVal)
        );
    }

    /**
     * Show/hide branch & new secondary password fields based on selected role.
     *
     * @param role the role selected in the role combo box
     */
    private void updateVisibilityForRole(String role) {
        if (role == null) {
            branchLabel.setVisible(false);
            branchCombo.setVisible(false);
            newSecPwdLabel.setVisible(false);
            newSecondaryPasswordField.setVisible(false);
            return;
        }

        boolean needsBranch = role.equals("Teller")
                           || role.equals("Manager")
                           || role.equals("Admin");
        branchLabel.setVisible(needsBranch);
        branchCombo.setVisible(needsBranch);

        boolean needsNewSecPassword = role.equals("Admin");
        newSecPwdLabel.setVisible(needsNewSecPassword);
        newSecondaryPasswordField.setVisible(needsNewSecPassword);
    }

    /**
     * Load branches from the database using BankDb.getAllBranches().
     */
    private void loadBranches() {
        try {
            availableBranches.setAll(bankDb.getAllBranches());
            branchCombo.setItems(availableBranches);
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Failed to load branches: " + e.getMessage());
        }
    }

    /**
     * Handles the "Save" action.
     * <p>
     * Validates the selected role, branch (when required), and the admin's
     * secondary password, then calls {@link BankDb#adminUpdateUserRole(int, int, String, Integer, String, String)}
     * to persist the role/branch updates and show a confirmation dialog.
     * On success, navigates back to the admin dashboard.
     *
     * @param event the action event triggered by clicking the Save button
     */
    @FXML
    private void handleSaveChanges(ActionEvent event) {
        errorLabel.setText("");

        if (selectedUser == null) {
            errorLabel.setText("No user selected.");
            return;
        }

        // Only employees can have their "role" changed via this screen
        if ("CUSTOMER".equals(selectedUser.getUserType())) {
            errorLabel.setText("Role changes for customers are not supported here.");
            return;
        }

        String chosenRoleUi = roleCombo.getValue();
        if (chosenRoleUi == null || chosenRoleUi.isBlank()) {
            errorLabel.setText("Please select a new role.");
            return;
        }

        String newRoleDb;
        switch (chosenRoleUi) {
            case "Teller" -> newRoleDb = "TELLER";
            case "Manager" -> newRoleDb = "MANAGER";
            case "Admin" -> newRoleDb = "ADMIN";
            case "Customer" -> {
                errorLabel.setText("Changing an employee to Customer is not supported.");
                return;
            }
            default -> {
                errorLabel.setText("Unknown role: " + chosenRoleUi);
                return;
            }
        }

        Branch chosenBranch = branchCombo.isVisible()
                ? branchCombo.getValue()
                : null;

        Integer newBranchId = null;
        if (branchCombo.isVisible()) {
            if (chosenBranch == null) {
                errorLabel.setText("Please select a branch for the new role.");
                return;
            }
            newBranchId = chosenBranch.getBranchId();
        }

        String newSecPwd = newSecondaryPasswordField.isVisible()
                ? newSecondaryPasswordField.getText()
                : null;

        if (newSecondaryPasswordField.isVisible()
                && (newSecPwd == null || newSecPwd.isBlank())) {
            errorLabel.setText("Please provide a new secondary password for Admin role.");
            return;
        }

        String adminSecPwd = adminSecondaryPasswordField.getText();
        if (adminSecPwd == null || adminSecPwd.isBlank()) {
            errorLabel.setText("Please enter your admin secondary password to confirm.");
            return;
        }

        // Get the currently logged-in admin
        Employee admin = SessionManager.getCurrentEmployee();
        if (admin == null) {
            errorLabel.setText("No admin session found.");
            return;
        }

        // Apply the change through BankDb (stored procedure handles validation + audit)
        try {
            bankDb.adminUpdateUserRole(
                    admin.getEmployeeID(),
                    selectedUser.getEmployeeId(),
                    newRoleDb,
                    newBranchId,
                    adminSecPwd,
                    newSecPwd
            );

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Success");
            ok.setHeaderText("Role updated successfully");
            ok.setContentText(
                    "Employee #" + selectedUser.getEmployeeId() +
                    " is now " + newRoleDb +
                    (chosenBranch != null ? " at branch " + chosenBranch.getBranchName() : "")
            );
            ok.showAndWait();

            goBackToAdminDashboard(event);

        } catch (SQLException e) {
            e.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Error");
            err.setHeaderText("Failed to update user role");
            err.setContentText(e.getMessage());
            err.showAndWait();
        }
    }

    /**
     * Handles the "Cancel" action by returning to the admin dashboard
     * without applying any changes.
     *
     * @param event the action event triggered by clicking the Cancel button
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        goBackToAdminDashboard(event);
    }

    /**
     * Handles the "Back" action in the UI and navigates to the admin dashboard.
     *
     * @param event the action event triggered by a Back button or link
     */
    @FXML
    private void handleBackToAdmin(ActionEvent event) {
        goBackToAdminDashboard(event);
    }

    /**
     * Utility method to switch the current scene back to the Admin Dashboard view.
     * <p>
     * Loads {@code /bank/Views/AdminDashboard.fxml}, replaces the scene root and
     * resizes the window. If loading fails, an error dialog is shown.
     *
     * @param event the originating action event, used to access the current stage
     */
    private void goBackToAdminDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/bank/Views/AdminDashboard.fxml"));
            Scene scene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.sizeToScene();
        } catch (IOException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Unable to return to Admin Dashboard");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }
}
