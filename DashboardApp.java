import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.FileNotFoundException;

public class DashboardApp extends Application {
    private FinanceManager manager;
    private Stage primaryStage;
    private VBox mainContainer;
    private User currentUser;
    
    // UI Components
    private TextField usernameField, passwordField;
    private TableView<TransactionTableModel> transactionTable;
    private TableView<BudgetTableModel> budgetTable;
    private Label balanceLabel, totalIncomeLabel, totalExpenseLabel;
    private PieChart expenseChart;
    private BarChart<String, Number> monthlyChart;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.manager = new FinanceManager();
        
        try {
            manager.loadUsers();
        } catch (Exception e) {
            showAlert("Error", "Failed to load users: " + e.getMessage());
        }
        
        primaryStage.setTitle("Personal Finance Manager");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        
        showLoginScreen();
        primaryStage.show();
    }

    private void showLoginScreen() {
        VBox loginContainer = new VBox(20);
        loginContainer.setAlignment(Pos.CENTER);
        loginContainer.setPadding(new Insets(40));
        loginContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);");

        // Title
        Label titleLabel = new Label("💰 Personal Finance Manager");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.WHITE);

        // Login Form
        VBox formContainer = new VBox(15);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setMaxWidth(300);
        formContainer.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10; -fx-padding: 30;");

        Label loginLabel = new Label("Login");
        loginLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(40);
        usernameField.setStyle("-fx-font-size: 14;");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-font-size: 14;");

        Button loginButton = new Button("Login");
        loginButton.setPrefHeight(40);
        loginButton.setPrefWidth(200);
        loginButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");

        Button registerButton = new Button("Register");
        registerButton.setPrefHeight(40);
        registerButton.setPrefWidth(200);
        registerButton.setStyle("-fx-background-color: #764ba2; -fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");

        loginButton.setOnAction(e -> handleLogin());
        registerButton.setOnAction(e -> showRegistrationDialog());

        formContainer.getChildren().addAll(loginLabel, usernameField, passwordField, loginButton, registerButton);
        loginContainer.getChildren().addAll(titleLabel, formContainer);

        Scene scene = new Scene(loginContainer);
        primaryStage.setScene(scene);
    }

    private void showRegistrationDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Register New User");
        dialog.setHeaderText("Create a new account");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField newUsernameField = new TextField();
        newUsernameField.setPromptText("Username");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(newUsernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Confirm:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);

        dialogPane.setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String username = newUsernameField.getText();
                String password = newPasswordField.getText();
                String confirmPassword = confirmPasswordField.getText();
                
                if (username.isEmpty() || password.isEmpty()) {
                    showAlert("Error", "Please fill in all fields");
                    return null;
                }
                
                if (!password.equals(confirmPassword)) {
                    showAlert("Error", "Passwords do not match");
                    return null;
                }
                
                if (manager.registerUser(username, password)) {
                    showAlert("Success", "Registration successful! You can now login.");
                    return new User(username, password);
                } else {
                    showAlert("Error", "Username already exists");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter username and password");
            return;
        }
        
        if (manager.loginUser(username, password)) {
            currentUser = manager.getCurrentUser();
            manager.loadTransactions(currentUser);
            manager.loadBudgets(currentUser);
            showMainDashboard();
        } else {
            showAlert("Error", "Invalid username or password");
        }
    }

    private void showMainDashboard() {
        mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        // Header
        HBox header = createHeader();
        
        // Main content area
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        Tab dashboardTab = new Tab("Dashboard", createDashboardTab());
        Tab transactionsTab = new Tab("Transactions", createTransactionsTab());
        Tab budgetsTab = new Tab("Budgets", createBudgetsTab());
        Tab analyticsTab = new Tab("Analytics", createAnalyticsTab());
        
        tabPane.getTabs().addAll(dashboardTab, transactionsTab, budgetsTab, analyticsTab);
        
        mainContainer.getChildren().addAll(header, tabPane);
        
        Scene scene = new Scene(mainContainer);
        primaryStage.setScene(scene);
        
        updateDashboard();
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 20, 10, 20));
        header.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label welcomeLabel = new Label("Welcome, " + currentUser.getUsername() + "!");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold;");
        logoutButton.setOnAction(e -> {
            manager.saveTransactions(currentUser);
            manager.saveBudgets(currentUser);
            showLoginScreen();
        });

        HBox.setHgrow(welcomeLabel, Priority.ALWAYS);
        header.getChildren().addAll(welcomeLabel, logoutButton);
        
        return header;
    }

    private VBox createDashboardTab() {
        VBox dashboard = new VBox(20);
        dashboard.setPadding(new Insets(20));

        // Summary Cards
        HBox summaryCards = new HBox(20);
        summaryCards.setAlignment(Pos.CENTER);

        VBox balanceCard = createSummaryCard("Total Balance", "$0.00", "#4CAF50");
        VBox incomeCard = createSummaryCard("Total Income", "$0.00", "#2196F3");
        VBox expenseCard = createSummaryCard("Total Expenses", "$0.00", "#F44336");

        balanceLabel = (Label) balanceCard.getChildren().get(1);
        totalIncomeLabel = (Label) incomeCard.getChildren().get(1);
        totalExpenseLabel = (Label) expenseCard.getChildren().get(1);

        summaryCards.getChildren().addAll(balanceCard, incomeCard, expenseCard);

        // Charts
        HBox chartsContainer = new HBox(20);
        chartsContainer.setAlignment(Pos.CENTER);

        // Expense Pie Chart
        VBox pieChartContainer = new VBox(10);
        pieChartContainer.setAlignment(Pos.CENTER);
        Label pieChartLabel = new Label("Expenses by Category");
        pieChartLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        expenseChart = new PieChart();
        expenseChart.setPrefSize(300, 300);
        pieChartContainer.getChildren().addAll(pieChartLabel, expenseChart);

        // Monthly Bar Chart
        VBox barChartContainer = new VBox(10);
        barChartContainer.setAlignment(Pos.CENTER);
        Label barChartLabel = new Label("Monthly Overview");
        barChartLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        monthlyChart = new BarChart<>(xAxis, yAxis);
        monthlyChart.setPrefSize(400, 300);
        barChartContainer.getChildren().addAll(barChartLabel, monthlyChart);

        chartsContainer.getChildren().addAll(pieChartContainer, barChartContainer);

        dashboard.getChildren().addAll(summaryCards, chartsContainer);
        return dashboard;
    }

    private VBox createSummaryCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10; -fx-text-fill: white;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.WHITE);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.WHITE);

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private VBox createTransactionsTab() {
        VBox transactionsTab = new VBox(20);
        transactionsTab.setPadding(new Insets(20));

        // Add Transaction Section
        VBox addTransactionSection = new VBox(15);
        addTransactionSection.setPadding(new Insets(20));
        addTransactionSection.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label addLabel = new Label("Add New Transaction");
        addLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        HBox transactionForm = new HBox(15);
        transactionForm.setAlignment(Pos.CENTER_LEFT);

        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");
        categoryField.setPrefWidth(150);

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        amountField.setPrefWidth(100);

        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton incomeRadio = new RadioButton("Income");
        RadioButton expenseRadio = new RadioButton("Expense");
        incomeRadio.setToggleGroup(typeGroup);
        expenseRadio.setToggleGroup(typeGroup);
        expenseRadio.setSelected(true);

        Button addButton = new Button("Add Transaction");
        addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        addButton.setOnAction(e -> {
            try {
                String category = categoryField.getText();
                double amount = Double.parseDouble(amountField.getText());
                boolean isIncome = incomeRadio.isSelected();

                if (category.isEmpty()) {
                    showAlert("Error", "Please enter a category");
                    return;
                }

                transactions t = new transactions(category, amount, new Date(), isIncome);
                currentUser.addTransaction(t);
                manager.saveTransactions(currentUser);

                categoryField.clear();
                amountField.clear();
                updateDashboard();
                updateTransactionTable();

                showAlert("Success", "Transaction added successfully!");
            } catch (NumberFormatException ex) {
                showAlert("Error", "Please enter a valid amount");
            }
        });

        transactionForm.getChildren().addAll(
            new Label("Category:"), categoryField,
            new Label("Amount:"), amountField,
            incomeRadio, expenseRadio, addButton
        );

        addTransactionSection.getChildren().addAll(addLabel, transactionForm);

        // Transactions Table
        VBox tableSection = new VBox(15);
        tableSection.setPadding(new Insets(20));
        tableSection.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label tableLabel = new Label("Transaction History");
        tableLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        transactionTable = new TableView<>();
        transactionTable.setPrefHeight(400);

        TableColumn<TransactionTableModel, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(120);

        TableColumn<TransactionTableModel, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(150);

        TableColumn<TransactionTableModel, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(100);

        TableColumn<TransactionTableModel, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(100);

        transactionTable.getColumns().addAll(dateCol, categoryCol, amountCol, typeCol);

        tableSection.getChildren().addAll(tableLabel, transactionTable);

        transactionsTab.getChildren().addAll(addTransactionSection, tableSection);
        return transactionsTab;
    }

    private VBox createBudgetsTab() {
        VBox budgetsTab = new VBox(20);
        budgetsTab.setPadding(new Insets(20));

        // Add Budget Section
        VBox addBudgetSection = new VBox(15);
        addBudgetSection.setPadding(new Insets(20));
        addBudgetSection.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label addLabel = new Label("Set New Budget");
        addLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        HBox budgetForm = new HBox(15);
        budgetForm.setAlignment(Pos.CENTER_LEFT);

        TextField budgetCategoryField = new TextField();
        budgetCategoryField.setPromptText("Category");
        budgetCategoryField.setPrefWidth(150);

        TextField budgetAmountField = new TextField();
        budgetAmountField.setPromptText("Monthly Limit");
        budgetAmountField.setPrefWidth(100);

        Button addBudgetButton = new Button("Set Budget");
        addBudgetButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        addBudgetButton.setOnAction(e -> {
            try {
                String category = budgetCategoryField.getText();
                double limit = Double.parseDouble(budgetAmountField.getText());

                if (category.isEmpty()) {
                    showAlert("Error", "Please enter a category");
                    return;
                }

                currentUser.setBudget(category, limit);
                manager.saveBudgets(currentUser);

                budgetCategoryField.clear();
                budgetAmountField.clear();
                updateDashboard();
                updateBudgetTable();

                showAlert("Success", "Budget set successfully!");
            } catch (NumberFormatException ex) {
                showAlert("Error", "Please enter a valid amount");
            }
        });

        budgetForm.getChildren().addAll(
            new Label("Category:"), budgetCategoryField,
            new Label("Limit:"), budgetAmountField, addBudgetButton
        );

        addBudgetSection.getChildren().addAll(addLabel, budgetForm);

        // Budgets Table
        VBox tableSection = new VBox(15);
        tableSection.setPadding(new Insets(20));
        tableSection.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label tableLabel = new Label("Current Budgets");
        tableLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        budgetTable = new TableView<>();
        budgetTable.setPrefHeight(400);

        TableColumn<BudgetTableModel, String> budgetCategoryCol = new TableColumn<>("Category");
        budgetCategoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        budgetCategoryCol.setPrefWidth(200);

        TableColumn<BudgetTableModel, String> budgetLimitCol = new TableColumn<>("Monthly Limit");
        budgetLimitCol.setCellValueFactory(new PropertyValueFactory<>("limit"));
        budgetLimitCol.setPrefWidth(150);

        TableColumn<BudgetTableModel, String> budgetSpentCol = new TableColumn<>("Spent");
        budgetSpentCol.setCellValueFactory(new PropertyValueFactory<>("spent"));
        budgetSpentCol.setPrefWidth(100);

        TableColumn<BudgetTableModel, String> budgetRemainingCol = new TableColumn<>("Remaining");
        budgetRemainingCol.setCellValueFactory(new PropertyValueFactory<>("remaining"));
        budgetRemainingCol.setPrefWidth(100);

        budgetTable.getColumns().addAll(budgetCategoryCol, budgetLimitCol, budgetSpentCol, budgetRemainingCol);

        tableSection.getChildren().addAll(tableLabel, budgetTable);

        budgetsTab.getChildren().addAll(addBudgetSection, tableSection);
        return budgetsTab;
    }

    private VBox createAnalyticsTab() {
        VBox analyticsTab = new VBox(20);
        analyticsTab.setPadding(new Insets(20));

        Label analyticsLabel = new Label("Financial Analytics");
        analyticsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // Add more analytics content here
        Label comingSoonLabel = new Label("Advanced analytics coming soon...");
        comingSoonLabel.setFont(Font.font("Arial", 16));

        analyticsTab.getChildren().addAll(analyticsLabel, comingSoonLabel);
        return analyticsTab;
    }

    private void updateDashboard() {
        List<transactions> transactions = currentUser.getTransaction();
        Map<String, Budget> budgets = currentUser.getBudgets();

        double totalIncome = 0;
        double totalExpense = 0;
        Map<String, Double> categoryExpenses = new HashMap<>();

        for (transactions t : transactions) {
            if (t.getIncome()) {
                totalIncome += t.getAmount();
            } else {
                totalExpense += t.getAmount();
                categoryExpenses.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }

        double balance = totalIncome - totalExpense;

        // Update summary labels
        balanceLabel.setText(String.format("$%.2f", balance));
        totalIncomeLabel.setText(String.format("$%.2f", totalIncome));
        totalExpenseLabel.setText(String.format("$%.2f", totalExpense));

        // Update pie chart
        updateExpenseChart(categoryExpenses);

        // Update monthly chart
        updateMonthlyChart(transactions);
    }

    private void updateExpenseChart(Map<String, Double> categoryExpenses) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
        
        expenseChart.setData(pieChartData);
    }

    private void updateMonthlyChart(List<transactions> transactions) {
        // Group transactions by month
        Map<String, Double> monthlyIncome = new HashMap<>();
        Map<String, Double> monthlyExpense = new HashMap<>();
        
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy");
        
        for (transactions t : transactions) {
            String month = monthFormat.format(t.getDate());
            if (t.getIncome()) {
                monthlyIncome.merge(month, t.getAmount(), Double::sum);
            } else {
                monthlyExpense.merge(month, t.getAmount(), Double::sum);
            }
        }

        // Create chart data
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");

        Set<String> allMonths = new HashSet<>();
        allMonths.addAll(monthlyIncome.keySet());
        allMonths.addAll(monthlyExpense.keySet());

        for (String month : allMonths) {
            incomeSeries.getData().add(new XYChart.Data<>(month, monthlyIncome.getOrDefault(month, 0.0)));
            expenseSeries.getData().add(new XYChart.Data<>(month, monthlyExpense.getOrDefault(month, 0.0)));
        }

        monthlyChart.getData().clear();
        monthlyChart.getData().addAll(incomeSeries, expenseSeries);
    }

    private void updateTransactionTable() {
        ObservableList<TransactionTableModel> data = FXCollections.observableArrayList();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        for (transactions t : currentUser.getTransaction()) {
            data.add(new TransactionTableModel(
                dateFormat.format(t.getDate()),
                t.getCategory(),
                String.format("$%.2f", t.getAmount()),
                t.getIncome() ? "Income" : "Expense"
            ));
        }
        
        transactionTable.setItems(data);
    }

    private void updateBudgetTable() {
        ObservableList<BudgetTableModel> data = FXCollections.observableArrayList();
        List<transactions> transactions = currentUser.getTransaction();
        Map<String, Budget> budgets = currentUser.getBudgets();
        
        // Calculate spent amounts for each category
        Map<String, Double> categorySpent = new HashMap<>();
        for (transactions t : transactions) {
            if (!t.getIncome()) {
                categorySpent.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        
        for (Budget budget : budgets.values()) {
            double spent = categorySpent.getOrDefault(budget.getCategory(), 0.0);
            double remaining = budget.getLimits() - spent;
            
            data.add(new BudgetTableModel(
                budget.getCategory(),
                String.format("$%.2f", budget.getLimits()),
                String.format("$%.2f", spent),
                String.format("$%.2f", remaining)
            ));
        }
        
        budgetTable.setItems(data);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Table Model Classes
    public static class TransactionTableModel {
        private final String date, category, amount, type;

        public TransactionTableModel(String date, String category, String amount, String type) {
            this.date = date;
            this.category = category;
            this.amount = amount;
            this.type = type;
        }

        public String getDate() { return date; }
        public String getCategory() { return category; }
        public String getAmount() { return amount; }
        public String getType() { return type; }
    }

    public static class BudgetTableModel {
        private final String category, limit, spent, remaining;

        public BudgetTableModel(String category, String limit, String spent, String remaining) {
            this.category = category;
            this.limit = limit;
            this.spent = spent;
            this.remaining = remaining;
        }

        public String getCategory() { return category; }
        public String getLimit() { return limit; }
        public String getSpent() { return spent; }
        public String getRemaining() { return remaining; }
    }
}
