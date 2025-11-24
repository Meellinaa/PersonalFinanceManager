import java.util.Date;
public class transactions {
	private String category;
	private double amount;
	private Date date;
	private boolean isIncome;

	// constructor
	public transactions(String category, double amount, Date date, boolean isIncome) {
		this.category = category;
		this.amount = amount;
		this.date = date;
		this.isIncome = isIncome;

	}

	// getters
	public String getCategory() {
		return category; 
		}

	public double getAmount() {
		return amount;
	}
	public Date getDate() {
		return date;
		
	}
	public boolean getIncome() {
		return isIncome;
	}
	
	}
