
public class Budget {
	private String category;
	private double limit;

	public Budget(String category, double limits) {
		this.category = category;
		this.limit = limits;
	}

	public String getCategory() {
		return category;
	}

	public double getLimits() {
		return limit;
	}

	public void setLimits(double limit) {
		this.limit = limit;

	}

	public String toString() {
		return "Budget for " + category + ": $" + limit;

	}
}
