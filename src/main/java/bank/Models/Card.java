// package bank.Models;

// import java.time.LocalDateTime;

// public class Card extends Account {

//     public Card(int accountId,
//                 int customerId,
//                 int branchId,
//                 String accountNumber,
//                 double balance,
//                 double interestRate,
//                 String bankCode,
//                 LocalDateTime createdAt) {

//         super(accountId,
//               customerId,
//               branchId,
//               accountNumber,
//               "CARD",
//               balance,
//               interestRate,
//               null,
//               bankCode,
//               createdAt);
//     }

//     @Override
//     public void pay() {
//         System.out.println("Card payment processed for account " + accountNumber + ".");
//     }

//     @Override
//     public void receipt() {
//         System.out.println("Card receipt generated for account " + accountNumber + ".");
//     }
// }
