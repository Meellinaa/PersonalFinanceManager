import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    private Scanner sc = new Scanner(System.in);
    private FinanceManager manager = new FinanceManager();

    public static void main(String[] args) throws FileNotFoundException {
        new Main().run();
    }

    private void run() throws FileNotFoundException {
        manager.loadUsers();
        System.out.println("=== Personal Finance Manager ===");
        
        boolean running = true;
        while (running) {
            System.out.println("\n1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an Option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    register();
                    break;
                case "2":
                    login();
                    break;
                case "3":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
        System.out.println("Goodbye");
    }

    private void register() {
        System.out.print("Enter your username: ");
        String user = sc.nextLine();
        System.out.print("Enter your password: ");
        String pass = sc.nextLine();
        
        if (manager.registerUser(user, pass)) {
            System.out.println("Registration successful.");
            // Automatically log in
            if (manager.loginUser(user, pass)) {
                userMenu();
            }
        } else {
            System.out.println("Registration failed (username might be taken).");
        }
    }

    private void login() {
        System.out.print("Enter your username: ");
        String user = sc.nextLine();
        System.out.print("Enter your password: ");
        String pass = sc.nextLine();
        
        if (manager.loginUser(user, pass)) {
            System.out.println("Login successful.");
            manager.loadTransactions(manager.getCurrentUser());
            userMenu();
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private void userMenu() {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n1. Add Transaction");
            System.out.println("2. View Transactions");
            System.out.println("3. Set Budget");
            System.out.println("4. View Budgets");
            System.out.println("5. Logout");
            System.out.print("Choose option: ");
            String choice = sc.nextLine();
            
            switch (choice) {
                case "1":
                    addTransaction();
                    break;
                case "2":
                    viewTransactions();
                    break;
                case "3":
                    setBudget();
                    break;
                case "4":
                    viewBudgets();
                    break;
                case "5":
                    manager.saveTransactions(manager.getCurrentUser());
                    loggedIn = false;
                    System.out.println("Logged out.");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void addTransaction() {
        System.out.print("Enter Category: ");
        String category = sc.nextLine();
        System.out.print("Enter the amount: ");
        double amount = Double.parseDouble(sc.nextLine());
        System.out.print("Is it Income? (yes/no): ");
        boolean isIncome = sc.nextLine().equalsIgnoreCase("yes");
        
        transactions t = new transactions(category, amount, new Date(), isIncome);
//        manager.getCurrentUser().addTransaction(t);
        manager.getCurrentUser().addTransaction(t);

        manager.saveTransactions(manager.getCurrentUser());
        System.out.println("🔁 saveTransactions called for " + manager.getCurrentUser().getUsername());

        System.out.println("Transaction Added");
    }

    private void viewTransactions() {
        List<transactions> transactions = manager.getCurrentUser().getTransaction();
        if (transactions.isEmpty()) {
            System.out.println("No transactions yet.");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            System.out.println("\n--- Transactions ---");
            for (transactions t : transactions) {
                System.out.printf("%s: %.2f (%s) on %s%n",
                        t.getCategory(),
                        t.getAmount(),
                        t.getIncome() ? "Income" : "Expense",
                        sdf.format(t.getDate()));
            }
        }
    }

    private void setBudget() {
        System.out.print("Enter the Category: ");
        String category = sc.nextLine();
        System.out.print("Enter the monthly limit: ");
        double amount = Double.parseDouble(sc.nextLine());
        manager.getCurrentUser().setBudget(category, amount);
        System.out.println("Budget set");
    }

    private void viewBudgets() {
        Map<String, Budget> budgets = manager.getCurrentUser().getBudgets();
        if (budgets.isEmpty()) {
            System.out.println("No budgets set.");
        } else {
            System.out.println("\n--- Budgets ---");
            for (Map.Entry<String, Budget> entry : budgets.entrySet()) {
                System.out.printf("%s: %.2f%n", entry.getKey(), entry.getValue().getLimits());
            }
        }
    }
}