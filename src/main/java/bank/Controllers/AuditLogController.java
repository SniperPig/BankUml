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

    @FXML
    private void handleApplyFilters(ActionEvent event) {
        errorLabel.setText("");
        refreshTable();
    }

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

    @FXML
    private void handleBackToAdmin(ActionEvent event) {
        switchScene(event, "/bank/Views/AdminDashboard.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.clear();
        switchScene(event, "/bank/Views/LoginForm.fxml");
    }

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

    private String nullToEmpty(String v) {
        return v == null ? "" : v;
    }

    private String string(Object o) {
        return o == null ? "" : o.toString();
    }

    public static class AuditLogRecord {
        private final String actionTime;
        private final String branchId;
        private final String actorType;
        private final String actorId;
        private final String action;
        private final String targetType;
        private final String targetId;
        private final String details;

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
