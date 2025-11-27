package bank.Models;

import bank.DB.BankDb;
import bank.Models.PasswordResettable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Customer implements PasswordResettable {
    private static final BankDb BANK_DB = new BankDb();

    int customerID;
    String customerName;
    String customerEmail;
    String customerPhonenumber;
    LocalDate customerDOB;
    String customerGovtID;
    String customerAddress;
    String passwordHash;
    String securityQuestion;
    String securityAnswer;

    /**
     * Constructor for creating a new customer
     * @param name of the customer
     * @param email of the customer
     * @param phoneNumber of the customer
     * @param dob of the customer
     * @param govtID of the customer
     * @param address of the customer
     */
    public Customer(String name, String email, String phoneNumber,
                    LocalDate dob, String govtID, String address) {
        this.customerName = name;
        this.customerEmail = email;
        this.customerPhonenumber = phoneNumber;
        this.customerDOB = dob;
        this.customerGovtID = govtID;
        this.customerAddress = address;
    }

    /**
     * Get the customer ID
     * @return customerID
     */
    public int getCustomerID() {
        return customerID;
    }   

    /**
     * Set the customer ID
     * @param id to set
     */
    public void setCustomerID(int id) {
        this.customerID = id;
    }

    /**
     * Get the customer name
     * @return customerName
     */
    public String getName() {
        return customerName;
    }

    /**
     * Set the customer name
     * @param name to set
     */
    public void setName(String name) {
        this.customerName = name;
    }

    /**
     * Get the customer email
     * @return customerEmail
     */
    public String getEmail() {
        return customerEmail;
    }       

    /**
     * Set the customer email
     * @param email to set
     */
    public void setEmail(String email) {
        this.customerEmail = email;
    }

    /**
     * Get the customer phone number
     * @return customerPhonenumber
     */
    public String getPhoneNumber() {
        return customerPhonenumber;
    }

    /**
     * Set the customer phone number
     * @param phoneNumber to set
     */
    public void setPhoneNumber(String phoneNumber) {
        this.customerPhonenumber = phoneNumber;
    }

    /**
     * Get the customer date of birth
     * @return customerDOB
     */
    public LocalDate getDOB() {
        return customerDOB;
    }

    /**
     * Set the customer date of birth
     * @param dob to set
     */
    public void setDOB(LocalDate dob) {
        this.customerDOB = dob;
    }

    /**
     * Get the customer government ID
     * @return customerGovtID
     */
    public String getGovtID() {
        return customerGovtID;
    }

    /**
     * Set the customer government ID
     * @param govtID to set
     */
    public void setGovtID(String govtID) {
        this.customerGovtID = govtID;
    }

    /**
     * Get the customer address
     * @return customerAddress
     */
    public String getAddress() {
        return customerAddress;
    }

    /**
     * Set the customer address
     * @param address to set
     */
    public void setAddress(String address) {
        this.customerAddress = address;
    }

    /**
     * Get the security question
     * @return securityQuestion
     */
    @Override
    public String getSecurityQuestion() {
        return securityQuestion;
    }

    /**
     * Set the security question for password recovery.
     * @param securityQuestion the question to store
     */
    public void setSecurityQuestion(String securityQuestion) {
        if (securityQuestion == null || securityQuestion.isBlank()) {
            throw new IllegalArgumentException("Security question cannot be empty.");
        }
        this.securityQuestion = securityQuestion.trim();
    }

    /**
     * Get the security answer
     * @return securityAnswer
     */
    @Override
    public String getSecurityAnswer() {
        return securityAnswer;
    }

    /**
     * Set the security answer for password recovery.
     * @param securityAnswer the answer to store
     */
    public void setSecurityAnswer(String securityAnswer) {
        if (securityAnswer == null || securityAnswer.isBlank()) {
            throw new IllegalArgumentException("Security answer cannot be empty.");
        }
        this.securityAnswer = securityAnswer.trim();
    }

    /**
     * Update contact information
     * @param phoneNumber the new phone number
     * @param address the new address
     */
    public void updateContactInfo(String phoneNumber, String address) {
        this.customerPhonenumber = phoneNumber;
        this.customerAddress = address;
    }

    /**
     * Set the customer password
     * @param password to set
     */
    @Override
    public void setPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        String oldPasswordHash = this.passwordHash;
        this.passwordHash = hashPassword(password);

        try {   
            BANK_DB.customerChangePassword(this.customerID, oldPasswordHash, this.passwordHash);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update password in the database.", e);
        }
    }

    /**
     * Get the hashed password
     * @return passwordHash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Checks if the email already exists in the database.
     * @param email to check
     * @return true if email exists, false otherwise
     */
    public boolean isEmailExisting(String email) throws SQLException {
        List<Map<String, Object>> rows = BANK_DB.customerGetByEmail(email);
        return !rows.isEmpty();
    }

    /**
     * Validates the given password against the stored password hash.      
     * @param password the plain text password to validate
     * @return true if the password is valid, false otherwise
     */
    public boolean isPasswordValid(String password) {
        if (password == null || passwordHash == null) {
            return false;
        }
        return passwordHash.equals(hashPassword(password));
    }

    /**
     * Hashes the given password using SHA-256.
     * @param password the plain text password
     * @return the hashed password as a hexadecimal string
     */
    public static String hashPassword(String password) {
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
     * Prints customer information to the console.
     */
    public void printCustomerInfo() {
        System.out.println("Customer's info: " );
        System.out.println("name: "+ customerName);
    }

    /**
     * Fetches customers for the specified branch filtered by the optional search query.
     * Controllers can call this instead of dealing directly with the stored procedures.
     * @param branchId the branch ID to filter customers (not used in current implementation)
     * @param searchQuery optional search query to filter customers by name or email
     * @return list of customers matching the criteria
     */
    public static List<Customer> fetchCustomersFromDB(int branchId, String searchQuery) throws SQLException {
        List<Map<String, Object>> rows = BANK_DB.customerSearchAll(
                searchQuery == null ? "" : searchQuery.trim());
        List<Customer> customers = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            customers.add(fromRow(row));
        }
        return customers;
    }

    /**
     * Fetch all customers regardless of branch.
     * @return list of all customers
     */
    public static List<Customer> fetchAllCustomers() throws SQLException {
        List<Map<String, Object>> rows = BANK_DB.fetchAllCustomers();
        List<Customer> customers = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            customers.add(fromRow(row));
        }
        return customers;
    }

    /**
     * Loads a single customer by id or returns null if not found.
     * @param customerId the ID of the customer to fetch
     * @return the Customer object if found, or null if not found
     */
    public static Customer fetchCustomerById(int customerId) throws SQLException {
        List<Map<String, Object>> rows = BANK_DB.customerGetById(customerId);
        if (rows.isEmpty()) {
            return null;
        }
        return fromRow(rows.get(0));
    }

    /**
     * Loads a single customer by email or returns null if not found.
     * @param email the email of the customer to fetch
     * @return the Customer object if found, or null if not found
     */
    public static Customer fetchCustomerByEmail(String email) throws SQLException {
        List<Map<String,Object>> rows = BANK_DB.customerGetByEmail(email);
        return rows.isEmpty() ? null : fromRow(rows.get(0));
    }

    
    /**
     * Creates a Customer object from a database row.
     * @param row the database row as a map
     * @return the Customer object
     */
    private static Customer fromRow(Map<String, Object> row) {
        Customer customer = new Customer(
                Objects.toString(row.get("customer_name"), null),
                Objects.toString(row.get("customer_email"), null),
                Objects.toString(row.get("customer_phone"), null),
                toLocalDate(row.get("dob")),
                Objects.toString(row.get("government_id"), null),
                Objects.toString(row.get("customer_address"), null)
        );
        Object idValue = row.get("customer_id");
        if (idValue instanceof Number number) {
            customer.setCustomerID(number.intValue());
        }
        customer.passwordHash = Objects.toString(row.get("password_hash"), null);
        customer.securityQuestion = Objects.toString(row.get("security_question"), null);
        customer.securityAnswer = Objects.toString(row.get("security_answer_hash"), null);
        return customer;
    }


    /**
     * Converts various object types to LocalDate.
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
