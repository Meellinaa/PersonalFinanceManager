import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class FinanceManager {
	private Map<String, User> users = new HashMap<>();
	private User currentUser;

	public boolean registerUser(String username, String password) {
		if (users.containsKey(username)) {
			return false;
		}
		users.put(username, new User(username, password));
		saveUsers(); // <- Save right after adding the user
		return true;
	}

	public boolean loginUser(String username, String password) {
		User user = users.get(username);

		if (user != null && user.checkPassword(password)) {
			currentUser = user;
			return true;
		}

		return false;
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public void saveUsers() {
		try (PrintWriter writer = new PrintWriter("users.txt")) {
			for (User user : users.values()) {
				writer.println(user.getUsername() + "," + user.getPassword());
			}
		} catch (IOException e) {
			System.out.println("Error saving users.");
			e.printStackTrace();
		}

	}

	public void loadUsers() { // Renamed to plural (loadUsers)
		try (Scanner fileScanner = new Scanner(new File("users.txt"))) {
			while (fileScanner.hasNextLine()) {
				String[] parts = fileScanner.nextLine().split(",");
				if (parts.length == 2) {
					String username = parts[0];
					String password = parts[1];
					users.put(username, new User(username, password));
				}
			}
			System.out.println("Successfully loaded users");
		} catch (FileNotFoundException e) {
			System.out.println("No users found. Starting fresh.");
		}

	}

	@SuppressWarnings("resource")
	public void saveTransactions(User user) {
		if (user == null)
			return;

		try (PrintWriter writer = new PrintWriter("transactions_" + user.getUsername() + ".txt")) {
			for (transactions t : user.getTransaction()) {
				System.out.println("📝 Writing transaction: " + t.getCategory() + "," + t.getAmount());

				writer.println(
						t.getCategory() + "," + t.getAmount() + "," + t.getDate().getTime() + "," + t.getIncome());
			}
			new PrintWriter("transactions_" + user.getUsername() + ".txt");

			String filename = "transactions_" + user.getUsername() + ".txt";
			System.out.println("📁 Saving to file: " + new File(filename).getAbsolutePath());
		} catch (IOException e) {
			System.out.println("Error saving transactions: " + e.getMessage());
		}
	}

	public void loadTransactions(User user) {
		if (user == null)
			return;

		String filename = "transactions_" + user.getUsername() + ".txt";
		File file = new File(filename);

		if (!file.exists())
			return;

		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				if (line.isEmpty())
					continue;

				String[] parts = line.split(",");
				if (parts.length != 4)
					continue;

				String category = parts[0];
				double amount = Double.parseDouble(parts[1]);
				long time = Long.parseLong(parts[2]);
				boolean isIncome = Boolean.parseBoolean(parts[3]);

				Date date = new Date(time);
				transactions t = new transactions(category, amount, date, isIncome);
				user.addTransaction(t); // ADD TO USER
			}
		} catch (Exception e) {
			System.out.println("Error loading transactions: " + e.getMessage());
		}
	}
	public void saveBudgets(User user) {
	    if (user == null) return;

	    String filename = "budgets_" + user.getUsername() + ".txt";
	    try (PrintWriter writer = new PrintWriter(filename)) {
	        for (Budget b : user.getBudgets().values()) {
	            writer.println(b.getCategory() + "," + b.getLimits());
	        }
	        System.out.println("✅ Budgets saved for " + user.getUsername());
	    } catch (IOException e) {
	        System.out.println("❌ Error saving budgets: " + e.getMessage());
	    }
	}
	public void loadBudgets(User user) {
	    if (user == null) return;

	    String filename = "budgets_" + user.getUsername() + ".txt";
	    File file = new File(filename);
	    if (!file.exists()) return;

	    try (Scanner scanner = new Scanner(file)) {
	        while (scanner.hasNextLine()) {
	            String[] parts = scanner.nextLine().split(",");
	            if (parts.length != 2) continue;

	            String category = parts[0];
	            double limit = Double.parseDouble(parts[1]);

	            user.setBudget(category, limit);
	        }
	        System.out.println("✅ Budgets loaded for " + user.getUsername());
	    } catch (IOException e) {
	        System.out.println("❌ Error loading budgets: " + e.getMessage());
	    }
	}



}
