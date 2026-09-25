import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Holds the in-memory list of expenses and monthly budgets, and persists
 * them to simple local data files so data survives between runs.
 */
public class ExpenseManager extends ExpenseTracker {
    private static final String DATA_FILE = "expenses.dat";
    private static final String BUDGET_FILE = "budgets.dat";

    private final List<Expense> expenses = new ArrayList<>();
    private final Map<Integer, Double> monthlyBudgets = new HashMap<>(); // key: month 1-12, current year
    private int nextId = 1;

    public ExpenseManager() {
        load();
    }

    private void load() {
        Path path = Paths.get(DATA_FILE);
        if (Files.exists(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    Expense e = Expense.fromDataLine(line);
                    expenses.add(e);
                    if (e.getId() >= nextId) {
                        nextId = e.getId() + 1;
                    }
                }
            } catch (IOException e) {
                System.err.println("Warning: could not read " + DATA_FILE + ": " + e.getMessage());
            }
        }

        Path budgetPath = Paths.get(BUDGET_FILE);
        if (Files.exists(budgetPath)) {
            try (BufferedReader reader = Files.newBufferedReader(budgetPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] parts = line.split("\\|");
                    monthlyBudgets.put(Integer.parseInt(parts[0]), Double.parseDouble(parts[1]));
                }
            } catch (IOException e) {
                System.err.println("Warning: could not read " + BUDGET_FILE + ": " + e.getMessage());
            }
        }
    }

    private void saveExpenses() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(DATA_FILE))) {
            for (Expense e : expenses) {
                writer.write(e.toDataLine());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving expenses: " + e.getMessage());
        }
    }

    private void saveBudgets() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(BUDGET_FILE))) {
            for (Map.Entry<Integer, Double> entry : monthlyBudgets.entrySet()) {
                writer.write(entry.getKey() + "|" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving budgets: " + e.getMessage());
        }
    }

    public Expense addExpense(String description, double amount, String category) {
        Expense e = new Expense(nextId++, LocalDate.now(), description, amount, category);
        expenses.add(e);
        saveExpenses();
        return e;
    }

    public boolean updateExpense(int id, String description, Double amount, String category) {
        for (Expense e : expenses) {
            if (e.getId() == id) {
                if (description != null) e.setDescription(description);
                if (amount != null) e.setAmount(amount);
                if (category != null) e.setCategory(category);
                saveExpenses();
                return true;
            }
        }
        return false;
    }

    public boolean deleteExpense(int id) {
        boolean removed = expenses.removeIf(e -> e.getId() == id);
        if (removed) {
            saveExpenses();
        }
        return removed;
    }

    public List<Expense> listExpenses(String categoryFilter) {
        return expenses.stream()
                .filter(e -> categoryFilter == null || categoryFilter.equalsIgnoreCase(e.getCategory()))
                .sorted(Comparator.comparingInt(Expense::getId))
                .collect(Collectors.toList());
    }

    public double totalExpenses() {
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

    public double totalForMonth(int month, int year) {
        return expenses.stream()
                .filter(e -> e.getDate().getMonthValue() == month && e.getDate().getYear() == year)
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public Map<String, Double> totalsByCategory() {
        Map<String, Double> totals = new TreeMap<>();
        for (Expense e : expenses) {
            String cat = (e.getCategory() == null || e.getCategory().isBlank()) ? "Uncategorized" : e.getCategory();
            totals.merge(cat, e.getAmount(), Double::sum);
        }
        return totals;
    }

    public void setBudget(int month, double amount) {
        monthlyBudgets.put(month, amount);
        saveBudgets();
    }

    public Double getBudget(int month) {
        return monthlyBudgets.get(month);
    }

    public void exportToCsv(String filename) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename))) {
            writer.write("ID,Date,Description,Amount,Category");
            writer.newLine();
            for (Expense e : listExpenses(null)) {
                writer.write(String.format("%d,%s,%s,%.2f,%s",
                        e.getId(), e.getDate(), csvEscape(e.getDescription()), e.getAmount(),
                        csvEscape(e.getCategory() == null ? "" : e.getCategory())));
                writer.newLine();
            }
        }
    }

    private String csvEscape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static String monthName(int month) {
        return Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }
}
