package bank;

import bank.Models.Customer;
import bank.Models.Employee;

/**
 * Stores which user is currently logged-in for this session (either customer or employee).
 */
public final class SessionManager {
    private static Employee currentEmployee;
    private static Customer currentCustomer;

    /**
     * This empty constructor is set to private and is used to ensure that no instances of this class will
     * be created.
     */
    private SessionManager() {
    }

    /**
     * If an employee is logging in, store their information
     * 
     * @param employee the employee that logged in
     */
    public static void setCurrentEmployee(Employee employee) {
        currentEmployee = employee;
        currentCustomer = null; 
    }

    
    /**
     * If a customer is logging in, store their information
     * 
     * @param customer the customer that logged in
     */
    public static void setCurrentCustomer(Customer customer) {
        currentCustomer = customer;
        currentEmployee = null; // ensure only one type is set
    }

    /**
     * Get the current active employee
     * 
     * @return currentEmployee the current active employee
     */
    public static Employee getCurrentEmployee() {
        return currentEmployee;
    }

    /**
     * Get the current active customer
     * 
     * @return currentCustomer the current active customer
     */
    public static Customer getCurrentCustomer() {
        return currentCustomer;
    }

    /**
     * This method can be used whenever the "logout" feature is used to ensure no active session is left
     */
    public static void clear() {
        currentEmployee = null;
        currentCustomer = null;
    }
}
