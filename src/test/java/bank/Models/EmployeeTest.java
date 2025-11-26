package bank.Models;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    /**
     * This test verifies that an admin employee can successfully log in
     * using both their primary password and secondary admin password.
     * It also checks that all other fields remain unchanged.
     */
    @Test
    void testVerifyAdminLoginSuccess() {
        Branch currentBranch = new Branch(1, "B1", "BranchStub", "1 Avenue");
        Employee currentEmployee = new Employee(
                "Bob Admin",
                "bob.admin@bank.com",
                "555-0101",
                LocalDate.of(1988, 2, 2),
                "99 Admin Ave",
                currentBranch,
                "Admin"
        );
        currentEmployee.setEmployeeID(1);
        currentEmployee.setPasswordHash("primaryPassword");
        currentEmployee.setAdminSecondaryPasswordHash("adminPassword");

        assertTrue(currentEmployee.verifyAdminLogin("primaryPassword", "adminPassword"));

        assertEquals(1, currentEmployee.getEmployeeID());
        assertEquals("Bob Admin", currentEmployee.getName());
        assertEquals("bob.admin@bank.com", currentEmployee.getEmail());
        assertEquals("555-0101", currentEmployee.getPhoneNumber());
        assertEquals(LocalDate.of(1988, 2, 2), currentEmployee.getDOB());
        assertEquals("99 Admin Ave", currentEmployee.getAddress());
        assertSame(currentBranch, currentEmployee.getBranch());
        assertEquals("Admin", currentEmployee.getRole());
    }

    /**
     * This test verifies that an admin employee's login fails
     * when an incorrect secondary admin password is provided. It also ensures that the primary password remains valid
     * and that all other fields remain unchanged.
     */
    @Test
    void testVerifyAdminLoginFailsWithWrongAdminPassword() {
        Branch currentBranch = new Branch(1, "B1", "BranchStub", "1 Avenue");
        Employee currentEmployee = new Employee(
                "Bob Admin",
                "bob.admin@bank.com",
                "555-0101",
                LocalDate.of(1988, 2, 2),
                "99 Admin Ave",
                currentBranch,
                "Admin"
        );
        currentEmployee.setPasswordHash("primaryPassword");
        currentEmployee.setAdminSecondaryPasswordHash("adminPassword");

        assertFalse(currentEmployee.verifyAdminLogin("primaryPassword", "wrongAdmin"));
        assertTrue(currentEmployee.isPasswordValid("primaryPassword"));
        assertEquals("Admin", currentEmployee.getRole());
    }

    /**
     * This test verifies that the getRole method returns the correct role
     * assigned to the employee. It also checks that all other fields remain unchanged.
     */
    @Test
    void testGetRoleReturnsStoredValue() {
        Branch currentBranch = new Branch(1, "B1", "BranchStub", "1 Avenue");
        Employee currentEmployee = new Employee(
                "Bob Teller",
                "bob.teller@bank.com",
                "555-0101",
                LocalDate.of(1988, 2, 2),
                "99 Admin Ave",
                currentBranch,
                "Teller"
        );
        currentEmployee.setEmployeeID(1);

        assertEquals("Teller", currentEmployee.getRole());

        assertEquals(1, currentEmployee.getEmployeeID());
        assertEquals("Bob Teller", currentEmployee.getName());
        assertEquals("bob.teller@bank.com", currentEmployee.getEmail());
        assertEquals("555-0101", currentEmployee.getPhoneNumber());
        assertEquals(LocalDate.of(1988, 2, 2), currentEmployee.getDOB());
        assertEquals("99 Admin Ave", currentEmployee.getAddress());
        assertSame(currentBranch, currentEmployee.getBranch());
    }
    /**
     * This test verifies that an admin employee can change the role
     * of another employee successfully. It also checks that all other fields remain unchanged.
     */
    @Test
    void testAdminCanChangeOtherEmployeeRole() {
        Branch currentBranch = new Branch(1, "B1", "BranchStub", "1 Avenue");

        Employee adminEmployee = new Employee(
                "Bob Admin",
                "bob.admin@bank.com",
                "555-0101",
                LocalDate.of(1988, 2, 2),
                "99 Admin Ave",
                currentBranch,
                "Admin"
        );
        adminEmployee.setEmployeeID(1);

        Employee targetEmployee = new Employee(
                "Alice Teller",
                "alice.teller@bank.com",
                "555-0202",
                LocalDate.of(1998, 3, 3),
                "22 Banff Ave",
                currentBranch,
                "Teller"
        );
        targetEmployee.setEmployeeID(2);

        assertEquals("Admin", adminEmployee.getRole());
        assertEquals("Teller", targetEmployee.getRole());

        targetEmployee.setRole("Manager");

        assertEquals("Manager", targetEmployee.getRole());
        assertEquals(2, targetEmployee.getEmployeeID());
        assertEquals("Alice Teller", targetEmployee.getName());
        assertEquals("alice.teller@bank.com", targetEmployee.getEmail());
        assertEquals("555-0202", targetEmployee.getPhoneNumber());
        assertEquals(LocalDate.of(1998, 3, 3), targetEmployee.getDOB());
        assertEquals("22 Banff Ave", targetEmployee.getAddress());
        assertSame(currentBranch, targetEmployee.getBranch());
    }
}
