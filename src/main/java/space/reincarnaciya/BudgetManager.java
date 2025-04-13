// Новый класс BudgetManager.java
package space.reincarnaciya;

import java.util.ArrayList;
import java.util.List;

public class BudgetManager {
    private double currentBudget;
    private final double initialBudget;
    private final List<String> transactionHistory;

    public BudgetManager(double initialBudget) {
        this.initialBudget = initialBudget;
        this.currentBudget = initialBudget;
        this.transactionHistory = new ArrayList<>();
        addHistory("Инициализация бюджета", initialBudget);
    }

    public void addFunds(double amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
        currentBudget += amount;
        addHistory(description, amount);
    }

    public void subtractFunds(double amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
        if (currentBudget < amount) {
            throw new IllegalStateException("Недостаточно средств на бюджете");
        }
        currentBudget -= amount;
        addHistory(description, -amount);
    }

    public double getCurrentBudget() {
        return currentBudget;
    }

    public double getInitialBudget() {
        return initialBudget;
    }

    public List<String> getTransactionHistory() {
        return transactionHistory;
    }

    private void addHistory(String description, double amount) {
        String transaction = String.format("%s: %s %.2f руб. (Текущий бюджет: %.2f руб.)",
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                description,
                amount,
                currentBudget);
        transactionHistory.add(transaction);
    }
}