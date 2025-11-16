package bank;

public class Customer {

    private String name;

    // Constructor
    public Customer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Display customers info
    public void printCustomerInfo() {
        System.out.println("Customer's info: " );
        System.out.println("name: "+ name);
    }
}

