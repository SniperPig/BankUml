package bank.Models;

import bank.DB.BankDb;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Employee implements PasswordResettable {
    private static final BankDb BANK_DB = new BankDb();

    int employeeID;
    String employeeName;
    String employeeEmail;
    String employeePhonenumber;
    LocalDate employeeDOB;
    String employeeAddress; 
    Branch branch;
    String role; 
    String passwordhash; 
    String adminSecondaryPasswordHash; 
    String securityQuestion;
    String securityAnswer; 

    /**
     * Constructor for creating a new employee
     * @param name of the employee
     * @param email of the employee
     * @param phoneNumber of the employee
     * @param dob of the employee
     * @param address of the employee
     * @param branch of the employee
     * @param role of the employee
     */
    public Employee(String name, String email, String phoneNumber,
                    LocalDate dob, String address, Branch branch, String role) {
        this.employeeName = name;
        this.employeeEmail = email;   
        this.employeePhonenumber = phoneNumber;
        this.employeeDOB = dob;
        this.employeeAddress = address;
        this.branch = branch;
        this.role = role; 
    }


    /**
     * Get the employee ID
     * @return employeeID       
     */
    public int getEmployeeID() {
        return employeeID;
    }

    /**
     * Set the employee ID
     * @param id to set       
    */
    public void setEmployeeID(int id) {
        this.employeeID = id;
    }

    /**
     * Get the employee name
     * @return employeeName       
     */    
    public String getName() {
        return employeeName;    
    }

    /**
     * Set the employee name
     * @param name to set       
     */
    public void setName(String name) {
        this.employeeName = name;   
    }

    /**
     * Get the employee email
     * @return employeeEmail       
     */
    public String getEmail() {
        return employeeEmail;
    }

    /**
     * Set the employee email
     * @param email to set       
     */    
    public void setEmail(String email) {
        this.employeeEmail = email; 
    }

    /**
     * Get the employee phone number
     * @return employeePhonenumber       
     */
    public String getPhoneNumber() {
        return employeePhonenumber;
    }

    /**
     * Set the employee phone number
     * @param phoneNumber to set       
     */
    public void setPhoneNumber(String phoneNumber) {
        this.employeePhonenumber = phoneNumber; 
    }

    /**
     * Get the employee date of birth
     * @return employeeDOB       
     */
    public LocalDate getDOB() {
        return employeeDOB;
    }

    /**
     * Set the employee date of birth
     * @param dob to set       
     */
    public void setDOB(LocalDate dob) {
        this.employeeDOB = dob;
    }

    /**
     * Get the employee address
     * @return employeeAddress       
     */
    public String getAddress() {
        return employeeAddress;
    }

    /**
     * Set the employee address
     * @param address to set       
     */
    public void setAddress(String address) {
        this.employeeAddress = address;
    }

    /**
     * Get the branch of the employee
     * @return branch       
     */    
    public Branch getBranch() {
        return branch;
    }

    /**
     * Set the branch of the employee
     * @param branch to set       
     */
    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    /**
     * Get the role of the employee
     * @return role       
     */
    public String getRole() {
        return role;
    }

    /**
     * Set the role of the employee
     * @param role to set       
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Set the security question and answer for password reset
     * @param question to set
     * @param answer to set
     */
    @Override
    public String getSecurityQuestion() {
        return securityQuestion;
    }

    /**
     * Set the security question and answer for password reset
     * @param question to set
     * @param answer to set
     */
    @Override
    public String getSecurityAnswer() {
        return securityAnswer;
    }

    /**
     * Get the password hash of the employee
     * @return passwordhash
     */
    public String getPasswordHash() {
        return passwordhash;
    }

    /**
     * Set the password hash of the employee
     * @param password to set
     */    
    public void setPasswordHash(String password) {
        this.passwordhash = hashPassword(password);
    }

    /**
     * Get the admin secondary password hash of the employee
     * @return adminSecondaryPasswordHash
     */
    public String getAdminSecondaryPasswordHash() {
        return adminSecondaryPasswordHash;
    }

    /**
     * Set the admin secondary password hash of the employee
     * @param adminSecondaryPassword to set
     */
    public void setAdminSecondaryPasswordHash(String adminSecondaryPassword) {
        this.adminSecondaryPasswordHash = hashPassword(adminSecondaryPassword);
    }

    /**
     * Validates the given password against the stored password hash.
     * @param password the plain text password to validate
     * @return true if the password is valid, false otherwise
     */
    public boolean isPasswordValid(String password) {
        if (password == null || passwordhash == null) {
            return false;
        }
        return passwordhash.equals(hashPassword(password));
    }

    /**
     * Verifies admin login by validating both the primary and admin passwords.
     * @param primaryPassword the main account password
     * @param adminPassword the admin secondary password
     * @return true if role is admin and both passwords match stored hashes
     */
    public boolean verifyAdminLogin(String primaryPassword, String adminPassword) {
        if (role == null || !role.equalsIgnoreCase("Admin")) {
            return false;
        }

        boolean primaryValid = isPasswordValid(primaryPassword);
        boolean adminValid = adminSecondaryPasswordHash != null
                && adminPassword != null
                && adminSecondaryPasswordHash.equals(hashPassword(adminPassword));

        return primaryValid && adminValid;
    }

    /**
     * Hashes the given password using SHA-256.
     * @param password the plain text password
     * @return the hashed password as a hexadecimal string
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", e);
        }
    }

    /**
     * Sets a new password for the employee and updates it in the database.
     * @param password the new plain text password
     * @throws IllegalArgumentException if the password is null or blank
     * @throws RuntimeException if the database update fails
     */
    @Override
    public void setPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        String oldPasswordHash = this.passwordhash;
        this.passwordhash = hashPassword(password);

        try {
            BANK_DB.employeeChangePassword(this.employeeID, oldPasswordHash, this.passwordhash);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update employee password.", e);
        }
    }

    /**
     * Loads a single employee by id or returns null if not found.
     * Controllers can call this instead of dealing directly with the stored procedures.
     * @param employeeID the ID of the employee to fetch
     * @return the Employee object if found, or null if not found
     */
    public static Employee fetchEmployeeByID(int employeeID) throws SQLException {
        List<Map<String, Object>> rows = BANK_DB.employeeGetById(employeeID);

        return rows.isEmpty() ? null : mapRowToEmployee(rows.get(0));
    }

    /**
     * Loads a single employee by email or returns null if not found.
     * Controllers can call this instead of dealing directly with the stored procedures.
     * @param email the email of the employee to fetch
     * @return the Employee object if found, or null if not found
     */
    public static Employee fetchEmployeeByEmail(String email) throws SQLException {
        List<Map<String, Object>> rows = BANK_DB.employeeGetByEmail(email);

        return rows.isEmpty() ? null : mapRowToEmployee(rows.get(0));
    }

    public static List<Employee> fetchAllEmployees() throws SQLException {
        List<Map<String, Object>> rows = BANK_DB.getAllEmployee();

        return rows.stream()
                   .map(Employee::mapRowToEmployee)
                   .toList();
    }

   /**
    * Maps a database row to an Employee object. 
    * @param row the database row as a map
    * @return the Employee object
    */
    private static Employee mapRowToEmployee(Map<String, Object> row) {
        Employee employee = new Employee(
            Objects.toString(row.get("employee_name"), null),
            Objects.toString(row.get("employee_email"), null),
            Objects.toString(row.get("employee_phone"), null),
            toLocalDate(row.get("dob")),
            Objects.toString(row.get("employee_address"), null),
            null,
            Objects.toString(row.get("role"), null)
        );

        Object idValue = row.get("employee_id");
        if (idValue instanceof Number number) {
            employee.setEmployeeID(number.intValue());
        }
        employee.passwordhash = Objects.toString(row.get("password_hash"), null);
        employee.adminSecondaryPasswordHash = Objects.toString(row.get("admin_secondary_password_hash"), null);
        employee.securityQuestion = Objects.toString(row.get("security_question"), null);
        employee.securityAnswer = Objects.toString(row.get("security_answer_hash"), null);
        return employee;
    }

    /**
     * Converts supported DB timestamp/date types to LocalDate.
     * @param value the object to convert
     * @return the LocalDate representation or null if input is null
     */
    private static LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (value instanceof Timestamp ts) {
            return ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }
}
