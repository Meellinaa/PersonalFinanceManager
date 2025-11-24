import java.util.*;

public class User {
	private  String username;
	private String password;
	private List<transactions> transactions;
	private Map<String, Budget> budgets;

	public User(String username, String password) {
        this.username = username;
		this.password = password;
		this.transactions = new ArrayList<>();
		this.budgets = new HashMap<>();

	}

	// getters
	public  String getUsername() {
		return username;
	}

	public boolean checkPassword(String input) {
		return password.equals(input);
	}

	// transaction
	public  void addTransaction(transactions t) {
	    transactions.add(t);
	}


	public List<transactions> getTransaction() {

		return transactions;
	}

	// Budget
	public void setBudget(String category, double limit) {
	    budgets.put(category, new Budget(category, limit));
	}

	public Budget getBudget(String category) {
		return budgets.get(category);

	}

	public Map<String, Budget> getBudgets() {
		return budgets;
	}

	public String getPassword() {
		// TODO Auto-generated method stub
		return password;
	}
}
