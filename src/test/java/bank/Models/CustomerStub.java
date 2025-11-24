package bank.Models;

/**
 * This is a stub for our Customer model. It only contains basic attributes:
 * customerID and customerName.
 */
public class CustomerStub {

    private final int customerID;
    private final String customerName;

    /**
     * This is our simplified constructor to represent Customers
     */
    public CustomerStub(int customerID, String customerName) {
        this.customerID = customerID;
        this.customerName = customerName;
    }

    /**
     * This is to get the customer ID. 
     * @return customerID the customer's ID
     */
    public int getId() {
        return customerID;
    }
}
