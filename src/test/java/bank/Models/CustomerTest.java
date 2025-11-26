package bank.Models;

// For unit testing
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
// For assertions
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class is used to perform JUnit testing on the Customer model
 */
class CustomerTest {

    @Test
    /**
     * This function reflects the first unit test for Customers.
     * It ensures that a Customer can successfully change their password
     * if they provide the correct security answer. It also checks that all
     * other fields remain unchanged.
     */
    void testChangePassword() {
        // First create a customer 
        Customer currentCustomer = new Customer(
                "Test Customer",
                "test@example.com",
                "555-0001",
                LocalDate.of(1990, 1, 1),
                "gov 1",
                "123 Test St"
        );

        // set the ID, password, security question, and answer
        currentCustomer.setCustomerID(1);
        currentCustomer.setPassword("Old#1234");
        currentCustomer.setSecurityQuestion("What is your program?");
        currentCustomer.setSecurityAnswer("Computer Science");

        // The security answer must be correct
        // We simulate it by setting a string
        String securityAnswerProvided = "Computer Science";
        assertEquals(securityAnswerProvided, currentCustomer.getSecurityAnswer());

        // If verification passes, change the password
        currentCustomer.setPassword("New#1234");

        // Check that the password was updated (use public API)
        assertTrue(currentCustomer.isPasswordValid("New#1234"));

        // Make sure other fields are not changed
        assertEquals(1, currentCustomer.getCustomerID());
        assertEquals("Test Customer", currentCustomer.getName());
        assertEquals("test@example.com", currentCustomer.getEmail());
        assertEquals("555-0001", currentCustomer.getPhoneNumber());
        assertEquals(LocalDate.of(1990, 1, 1), currentCustomer.getDOB());
        assertEquals("123 Test St", currentCustomer.getAddress());
        assertEquals("gov 1", currentCustomer.getGovtID());
        assertEquals("What is your program?", currentCustomer.getSecurityQuestion());
        assertEquals("Computer Science", currentCustomer.getSecurityAnswer());
    }

    @Test
    /**
     * This function reflects the second transaction unit test for Customer from Assignment 03.
     * It tests whether a customer can successfully update their phone number and address.
     */
    void testUpdateContactInfo() {
        // First create a customer 
        Customer currentCustomer = new Customer(
                "Test Customer",
                "test@example.com",
                "555-0001",
                LocalDate.of(1990, 1, 1),
                "gov 1",
                "123 Test St"
        );

        // Set customerID as well since it's part of the unit test
        currentCustomer.setCustomerID(1);

        // Update the phone number and address
        currentCustomer.setPhoneNumber("555-2000");
        currentCustomer.setAddress("456 New Ave");

        // Check that they were correct updated
        assertEquals("555-2000", currentCustomer.getPhoneNumber());
        assertEquals("456 New Ave", currentCustomer.getAddress());

        // Check the other fields 
        assertEquals(1, currentCustomer.getCustomerID());
        assertEquals("Test Customer", currentCustomer.getName());
        assertEquals("test@example.com", currentCustomer.getEmail());
        assertEquals(LocalDate.of(1990, 1, 1), currentCustomer.getDOB());
        assertEquals("gov 1", currentCustomer.getGovtID());
    }
}
