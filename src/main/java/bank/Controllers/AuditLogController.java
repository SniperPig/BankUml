package bank.Controllers;

import bank.DB.BankDb;
import bank.Models.Branch;
import bank.Models.Employee;
import bank.SessionManager;
import javafx.beans.property.SimpleStringProperty;
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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller responsible for displaying and filtering the audit log view.
 * <p>
 * It initializes and manages the filter controls (date pickers, branch and actor filters)
 * and populates the audit table based on the selected criteria by querying {@link BankDb}.
 */
public class AuditLogController {

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<Branch> branchCombo;
    @FXML private ComboBox<String> actorTypeCombo;
    @FXML private TextField actorIdField;
    @FXML private Label errorLabel;

    @FXML private TableView<AuditLogRecord> auditTable;
    @FXML private TableColumn<AuditLogRecord, String> timeColumn;
    @FXML private TableColumn<AuditLogRecord, String> branchColumn;
    @FXML private TableColumn<AuditLogRecord, String> actorTypeColumn;
    @FXML private TableColumn<AuditLogRecord, String> actorIdColumn;
    @FXML private TableColumn<AuditLogRecord, String> actionColumn;
    @FXML private TableColumn<AuditLogRecord, String> targetTypeColumn;
    @FXML private TableColumn<AuditLogRecord, String> targetIdColumn;
    @FXML private TableColumn<AuditLogRecord, String> detailsColumn;

    private final BankDb bankDb = new BankDb();
    private final ObservableList<AuditLogRecord> rows =
            FXCollections.observableArrayList();

    /**
     * Initializes the audit log view after the FXML has been loaded.
     * <p>
     * This method wires up the table columns, loads branches into the branch selector,
     * configures the actor type combo box, sets default date filters (last 7 days),
     * and triggers the initial table load.
     */
    @FXML
    private void initialize() {
        // table columns
        timeColumn.setCellValueFactory(c ->
                new SimpleStringProperty(nullToEmpty(c.getValue().actionTime)));
        branchColumn.setCellValueFactory(c ->
                new SimpleStringProperty(nullToEmpty(c.getValue().branchId)));
        actorTypeColumn.setCellValueFactory(c ->
                new SimpleStringProperty(nullToEmpty(c.getValue().actorType)));
        actorIdColumn.setCellValueFactory(c ->
                new SimpleStringProperty(nullToEmpty(c.getValue().actorId)));
        actionColumn.setCellValueFactory(c ->
                new SimpleStringProperty(nullToEmpty(c.getValue().action)));
        targetTypeColumn.setCellValueFactory(c ->
                new SimpleStringProperty(nullToEmpty(c.getValue().targetType)));
        targetIdColumn.setCellValueFactory(c ->
                new SimpleStringProperty(nullToEmpty(c.getValue().targetId)));
        detailsColumn.setCellValueFactory(c ->
                new SimpleStringProperty(nullToEmpty(c.getValue().details)));

        auditTable.setItems(rows);

        // actor type combo
        actorTypeCombo.setItems(FXCollections.observableArrayList("Any", "CUSTOMER", "EMPLOYEE"));
        actorTypeCombo.getSelectionModel().select("Any");

        // load branches into combo (if you already have BankDb.getAllBranches())
        try {
            List<Branch> branches = bankDb.getAllBranches();
            ObservableList<Branch> items = FXCollections.observableArrayList();
            items.add(null); // "Any"
            items.addAll(branches);
            branchCombo.setItems(items);
            branchCombo.setCellFactory(cb -> new ListCell<>() {
                @Override
                protected void updateItem(Branch item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item == null ? "Any" : item.getBranchName());
                    }
                }
            });
            branchCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Branch item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item == null ? "Any" : item.getBranchName());
                    }
                }
            });
            branchCombo.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            errorLabel.setText("Failed to load branches: " + e.getMessage());
        }

        // initial load: last 7 days, all branches/actors
        toDatePicker.setValue(LocalDate.now());
        fromDatePicker.setValue(LocalDate.now().minusDays(7));
        refreshTable();
    }

    /**
     * Handles the "Apply Filters" button action.
     * <p>
     * Clears any displayed error message and reloads the audit table
     * using the currently selected filter values.
     *
     * @param event the action event fired by the button
     */
    @FXML
    private void handleApplyFilters(ActionEvent event) {
        errorLabel.setText("");
        refreshTable();
    }

    /**
     * Handles the "Clear Filters" button action.
     * <p>
     * Resets all filter controls (dates, branch, actor type, actor ID),
     * clears any error message, and reloads the audit table without filters.
     *
     * @param event the action event fired by the button
     */
    @FXML
    private void handleClearFilters(ActionEvent event) {
        errorLabel.setText("");
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        branchCombo.getSelectionModel().selectFirst();
        actorTypeCombo.getSelectionModel().select("Any");
        actorIdField.clear();
        refreshTable();
    }

    /**
     * Reloads the audit table based on the currently selected filters.
     * <p>
     * This method reads the values from the branch selector, actor type combo,
     * actor ID field, and date pickers, validates them (e.g. numeric actor ID),
     * queries {@link BankDb#getAuditLog(Integer, String, Integer, Timestamp, Timestamp)},
     * and populates the table with the results.
     * <p>
     * If an error occurs (e.g. invalid actor ID or SQL error), a message is displayed
     * in {@code errorLabel} and the table content may not be updated.
     */
    private void refreshTable() {
        rows.clear();
        try {
            Integer branchId = null;
            Branch selectedBranch = branchCombo.getValue();
            if (selectedBranch != null) {
                branchId = selectedBranch.getBranchId();
            }

            String actorType = actorTypeCombo.getValue();
            if ("Any".equals(actorType)) {
                actorType = null;
            }

            Integer actorId = null;
            if (!actorIdField.getText().isBlank()) {
                try {
                    actorId = Integer.parseInt(actorIdField.getText().trim());
                } catch (NumberFormatException e) {
                    errorLabel.setText("Actor ID must be a number.");
                    return;
                }
            }

            Timestamp fromTs = null;
            Timestamp toTs = null;

            LocalDate from = fromDatePicker.getValue();
            LocalDate to = toDatePicker.getValue();

            if (from != null) {
                fromTs = Timestamp.valueOf(LocalDateTime.of(from, java.time.LocalTime.MIN));
            }
            if (to != null) {
                toTs = Timestamp.valueOf(LocalDateTime.of(to, java.time.LocalTime.MAX));
            }

            List<Map<String, Object>> data =
                    bankDb.getAuditLog(branchId, actorType, actorId, fromTs, toTs);

            for (Map<String, Object> row : data) {
                AuditLogRecord rec = new AuditLogRecord(
                        string(row.get("action_time")),
                        string(row.get("branch_id")),
                        string(row.get("actor_type")),
                        string(row.get("actor_id")),
                        string(row.get("action")),
                        string(row.get("target_type")),
                        string(row.get("target_id")),
                        string(row.get("details"))
                );
                rows.add(rec);
            }

        } catch (SQLException e) {
            errorLabel.setText("Error loading audit log: " + e.getMessage());
        }
    }

    /**
     * Handles navigation back to the admin dashboard screen.
     *
     * @param event the action event fired by the navigation control
     */
    @FXML
    private void handleBackToAdmin(ActionEvent event) {
        switchScene(event, "/bank/Views/AdminDashboard.fxml");
    }

    /**
     * Handles the logout action.
     * <p>
     * Clears the current session via {@link SessionManager#clear()} and
     * navigates back to the login form.
     *
     * @param event the action event fired by the logout control
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.clear();
        switchScene(event, "/bank/Views/LoginForm.fxml");
    }

    /**
     * Helper method to switch the current JavaFX scene to another FXML view.
     *
     * @param event    the originating action event, used to obtain the current stage
     * @param resource the classpath resource to the FXML file to load
     */
    private void switchScene(ActionEvent event, String resource) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(resource));
            Scene scene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.sizeToScene();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns an empty string if the provided value is {@code null}, otherwise returns it unchanged.
     *
     * @param v the input string which may be {@code null}
     * @return an empty string if {@code v} is {@code null}; otherwise {@code v}
     */
    private String nullToEmpty(String v) {
        return v == null ? "" : v;
    }

    /**
     * Converts an object to its string representation, returning an empty string if the object is {@code null}.
     *
     * @param o the object to convert, may be {@code null}
     * @return the object's {@code toString()} value, or an empty string if {@code o} is {@code null}
     */
    private String string(Object o) {
        return o == null ? "" : o.toString();
    }

    /**
     * Simple data holder representing a single row in the audit log table.
     * <p>
     * All fields are stored as strings for easier binding to the JavaFX table.
     */
    public static class AuditLogRecord {
        private final String actionTime;
        private final String branchId;
        private final String actorType;
        private final String actorId;
        private final String action;
        private final String targetType;
        private final String targetId;
        private final String details;

        /**
         * Constructs a new {@code AuditLogRecord} instance with all displayable fields.
         *
         * @param actionTime the time at which the action occurred
         * @param branchId   the identifier of the branch where the action took place
         * @param actorType  the type of actor (e.g. CUSTOMER, EMPLOYEE)
         * @param actorId    the identifier of the actor that performed the action
         * @param action     the action performed (e.g. CREATE_ACCOUNT, UPDATE_CUSTOMER)
         * @param targetType the type of the target entity affected by the action
         * @param targetId   the identifier of the target entity
         * @param details    additional descriptive details about the action
         */
        public AuditLogRecord(String actionTime,
                              String branchId,
                              String actorType,
                              String actorId,
                              String action,
                              String targetType,
                              String targetId,
                              String details) {
            this.actionTime = actionTime;
            this.branchId = branchId;
            this.actorType = actorType;
            this.actorId = actorId;
            this.action = action;
            this.targetType = targetType;
            this.targetId = targetId;
            this.details = details;
        }
    }
}
