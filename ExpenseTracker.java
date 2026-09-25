import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command-line entry point for the Expense Tracker application.
 *
 * Usage:
 *   java ExpenseTracker add --description <text> --amount <value> [--category <text>]
 *   java ExpenseTracker update --id <id> [--description <text>] [--amount <value>] [--category <text>]
 *   java ExpenseTracker delete --id <id>
 *   java ExpenseTracker list [--category <text>]
 *   java ExpenseTracker summary [--month <1-12>]
 *   java ExpenseTracker budget --month <1-12> --amount <value>
 *   java ExpenseTracker export [--file <filename.csv>]
 */
public class ExpenseTracker {

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        ExpenseManager manager = new ExpenseManager();
        String command = args[0];
        Map<String, String> options = parseOptions(args);

        try {
            switch (command) {
                case "add" -> handleAdd(manager, options);
                case "update" -> handleUpdate(manager, options);
                case "delete" -> handleDelete(manager, options);
                case "list" -> handleList(manager, options);
                case "summary" -> handleSummary(manager, options);
                case "budget" -> handleBudget(manager, options);
                case "export" -> handleExport(manager, options);
                case "help", "--help", "-h" -> printUsage();
                default -> {
                    System.out.println("Unknown command: " + command);
                    printUsage();
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: invalid number provided (" + e.getMessage() + ")");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static Map<String, String> parseOptions(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                String key = arg.substring(2);
                String value = (i + 1 < args.length && !args[i + 1].startsWith("--")) ? args[++i] : "";
                options.put(key, value);
            }
        }
        return options;
    }

    private static void handleAdd(ExpenseManager manager, Map<String, String> options) {
        String description = options.get("description");
        String amountStr = options.get("amount");
        String category = options.get("category");

        if (description == null || description.isBlank() || amountStr == null) {
            System.out.println("Usage: add --description <text> --amount <value> [--category <text>]");
            return;
        }

        double amount = Double.parseDouble(amountStr);
        if (amount <= 0) {
            System.out.println("Error: amount must be greater than 0");
            return;
        }

        Expense e = manager.addExpense(description, amount, category);
        System.out.println("Expense added successfully (ID: " + e.getId() + ")");

        LocalDate today = LocalDate.now();
        Double budget = manager.getBudget(today.getMonthValue());
        if (budget != null) {
            double spent = manager.totalForMonth(today.getMonthValue(), today.getYear());
            if (spent > budget) {
                System.out.printf("Warning: you have exceeded your budget of %.2f for %s (spent: %.2f)%n",
                        budget, ExpenseManager.monthName(today.getMonthValue()), spent);
            }
        }
    }

    private static void handleUpdate(ExpenseManager manager, Map<String, String> options) {
        String idStr = options.get("id");
        if (idStr == null) {
            System.out.println("Usage: update --id <id> [--description <text>] [--amount <value>] [--category <text>]");
            return;
        }

        int id = Integer.parseInt(idStr);
        String description = options.get("description");
        String amountStr = options.get("amount");
        Double amount = amountStr != null ? Double.parseDouble(amountStr) : null;
        String category = options.get("category");

        if (description == null && amount == null && category == null) {
            System.out.println("Provide at least one of --description, --amount, --category to update");
            return;
        }

        boolean updated = manager.updateExpense(id, description, amount, category);
        System.out.println(updated ? "Expense updated successfully" : "Error: no expense found with ID " + id);
    }

    private static void handleDelete(ExpenseManager manager, Map<String, String> options) {
        String idStr = options.get("id");
        if (idStr == null) {
            System.out.println("Usage: delete --id <id>");
            return;
        }

        int id = Integer.parseInt(idStr);
        boolean deleted = manager.deleteExpense(id);
        System.out.println(deleted ? "Expense deleted successfully" : "Error: no expense found with ID " + id);
    }

    private static void handleList(ExpenseManager manager, Map<String, String> options) {
        String category = options.get("category");
        List<Expense> list = manager.listExpenses(category);

        if (list.isEmpty()) {
            System.out.println("No expenses found.");
            return;
        }

        System.out.printf("%-4s %-12s %-25s %-10s %-15s%n", "ID", "Date", "Description", "Amount", "Category");
        for (Expense e : list) {
            System.out.printf("%-4d %-12s %-25s %-10.2f %-15s%n",
                    e.getId(), e.getDate(), e.getDescription(), e.getAmount(),
                    e.getCategory() == null ? "-" : e.getCategory());
        }
    }

    private static void handleSummary(ExpenseManager manager, Map<String, String> options) {
        String monthStr = options.get("month");

        if (monthStr != null) {
            int month = Integer.parseInt(monthStr);
            if (month < 1 || month > 12) {
                System.out.println("Error: month must be between 1 and 12");
                return;
            }
            int year = LocalDate.now().getYear();
            double total = manager.totalForMonth(month, year);
            System.out.printf("Total expenses for %s: %.2f%n", ExpenseManager.monthName(month), total);

            Double budget = manager.getBudget(month);
            if (budget != null) {
                if (total > budget) {
                    System.out.printf("Budget for %s: %.2f (OVER BUDGET by %.2f)%n",
                            ExpenseManager.monthName(month), budget, total - budget);
                } else {
                    System.out.printf("Budget for %s: %.2f (within budget)%n",
                            ExpenseManager.monthName(month), budget);
                }
            }
        } else {
            double total = manager.totalExpenses();
            System.out.printf("Total expenses: %.2f%n", total);

            Map<String, Double> byCategory = manager.totalsByCategory();
            if (!byCategory.isEmpty()) {
                System.out.println("By category:");
                for (Map.Entry<String, Double> entry : byCategory.entrySet()) {
                    System.out.printf("  %-15s %.2f%n", entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private static void handleBudget(ExpenseManager manager, Map<String, String> options) {
        String monthStr = options.get("month");
        String amountStr = options.get("amount");

        if (monthStr == null || amountStr == null) {
            System.out.println("Usage: budget --month <1-12> --amount <value>");
            return;
        }

        int month = Integer.parseInt(monthStr);
        double amount = Double.parseDouble(amountStr);

        if (month < 1 || month > 12) {
            System.out.println("Error: month must be between 1 and 12");
            return;
        }

        manager.setBudget(month, amount);
        System.out.printf("Budget for %s set to %.2f%n", ExpenseManager.monthName(month), amount);
    }

    private static void handleExport(ExpenseManager manager, Map<String, String> options) {
        String filename = options.getOrDefault("file", "expenses.csv");
        try {
            manager.exportToCsv(filename);
            System.out.println("Expenses exported to " + filename);
        } catch (Exception e) {
            System.out.println("Error exporting expenses: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("""
                Expense Tracker - manage your expenses from the command line

                Usage:
                  add     --description <text> --amount <value> [--category <text>]
                  update  --id <id> [--description <text>] [--amount <value>] [--category <text>]
                  delete  --id <id>
                  list    [--category <text>]
                  summary [--month <1-12>]
                  budget  --month <1-12> --amount <value>
                  export  [--file <filename.csv>]

                Examples:
                  java ExpenseTracker add --description "Lunch" --amount 20 --category Food
                  java ExpenseTracker list --category Food
                  java ExpenseTracker summary --month 8
                  java ExpenseTracker budget --month 8 --amount 500
                  java ExpenseTracker export --file my-expenses.csv""");
    }
}
