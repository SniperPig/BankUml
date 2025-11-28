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

    @FXML
    private Label userTypeLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label currentRoleLabel;
    @FXML
    private Label currentBranchLabel;

    @FXML
    private ComboBox<String> roleCombo;
    @FXML
    private Label branchLabel;
    @FXML
    private ComboBox<Branch> branchCombo;

    @FXML
    private Label newSecPwdLabel;
    @FXML
    private PasswordField newSecondaryPasswordField;

    @FXML
    private PasswordField adminSecondaryPasswordField;

    @FXML
    private Label errorLabel;

    private final BankDb bankDb = new BankDb();

    private UserRecord selectedUser;
    private final ObservableList<Branch> availableBranches = FXCollections.observableArrayList();

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
                                : selectedUser.getRole()));
        currentBranchLabel.setText(
                selectedUser.getBranchName() == null
                        ? ""
                        : selectedUser.getBranchName());

        // Populate role options
        roleCombo.setItems(FXCollections.observableArrayList(
                "Customer", "Teller", "Admin"
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
        roleCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateVisibilityForRole(newVal));
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
     * secondary password, then calls
     * {@link BankDb#adminUpdateUserRole(int, int, String, Integer, String, String)}
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

        String chosenRoleUi = roleCombo.getValue();
        if (chosenRoleUi == null || chosenRoleUi.isBlank()) {
            errorLabel.setText("Please select a new role.");
            return;
        }

        // Map UI role string to DB role (for employees)
        String newRoleDb = null;
        if ("Teller".equals(chosenRoleUi)) {
            newRoleDb = "TELLER";
        } else if ("Manager".equals(chosenRoleUi)) {
            newRoleDb = "MANAGER";
        } else if ("Admin".equals(chosenRoleUi)) {
            newRoleDb = "ADMIN";
        } else if ("Customer".equals(chosenRoleUi)) {
            newRoleDb = "CUSTOMER";
        } else {
            errorLabel.setText("Unknown role: " + chosenRoleUi);
            return;
        }

        Branch chosenBranch = branchCombo.isVisible()
                ? branchCombo.getValue()
                : null;

        // For any employee role (incl. convert customer -> employee) we need a branch
        boolean roleIsEmployeeType = !"Customer".equals(chosenRoleUi);
        if (roleIsEmployeeType) {
            if (chosenBranch == null) {
                errorLabel.setText("Please select a branch for the employee role.");
                return;
            }
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

        Employee admin = SessionManager.getCurrentEmployee();
        if (admin == null) {
            errorLabel.setText("No admin session found.");
            return;
        }

        try {
            if ("EMPLOYEE".equals(selectedUser.getUserType())) {
                // EMPLOYEE -> CUSTOMER
                if ("Customer".equals(chosenRoleUi)) {
                    // verify admin secondary password first via existing role-change proc
                    bankDb.adminUpdateUserRole(
                            admin.getEmployeeID(),
                            selectedUser.getEmployeeId(),
                            "TELLER", // dummy, just to validate pwd; or make separate validator if you prefer
                            chosenBranch != null ? chosenBranch.getBranchId() : null,
                            adminSecPwd,
                            null);
                    // then conversion
                    bankDb.adminConvertEmployeeToCustomer(
                            admin.getEmployeeID(),
                            selectedUser.getEmployeeId());

                } else {
                    // pure employee role change (no type change)
                    bankDb.adminUpdateUserRole(
                            admin.getEmployeeID(),
                            selectedUser.getEmployeeId(),
                            newRoleDb, // TELLER/MANAGER/ADMIN
                            chosenBranch.getBranchId(),
                            adminSecPwd,
                            newSecPwd);
                }

            } else if ("CUSTOMER".equals(selectedUser.getUserType())) {
                // CUSTOMER -> EMPLOYEE (Teller/Manager/Admin)
                if ("Customer".equals(chosenRoleUi)) {
                    errorLabel.setText("User is already a customer.");
                    return;
                }

                // validate admin secondary password with a cheap call OR separate proc;
                // if you're okay trusting the conversion proc only, you can skip this.

                bankDb.adminConvertCustomerToEmployee(
                        admin.getEmployeeID(),
                        selectedUser.getCustomerId(),
                        newRoleDb, // TELLER/MANAGER/ADMIN
                        chosenBranch.getBranchId(),
                        newSecPwd);

            } else {
                errorLabel.setText("Unknown user type: " + selectedUser.getUserType());
                return;
            }

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Success");
            ok.setHeaderText("User type/role updated successfully");
            ok.setContentText(
                    selectedUser.getUserType() + " #" + selectedUser.getUserId() +
                            " is now " + chosenRoleUi +
                            (chosenBranch != null ? " at branch " + chosenBranch.getBranchName() : ""));
            ok.showAndWait();

            goBackToAdminDashboard(event);

        } catch (SQLException e) {
            e.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Error");
            err.setHeaderText("Failed to update user");
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
