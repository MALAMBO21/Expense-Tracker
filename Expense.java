import java.time.LocalDate;

/**
 * Represents a single expense entry.
 */
public class Expense extends ExpenseTracker {
    private final int id;
    private final LocalDate date;
    private String description;
    private double amount;
    private String category;

    public Expense(int id, LocalDate date, String description, double amount, String category) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Serializes this expense into a single line for the local data file.
     * Format: id|date|description|amount|category
     */
    public String toDataLine() {
        return id + "|" + date + "|" + escape(description) + "|" + amount + "|"
                + escape(category == null ? "" : category);
    }

    public static Expense fromDataLine(String line) {
        String[] parts = line.split("\\|", -1);
        int id = Integer.parseInt(parts[0]);
        LocalDate date = LocalDate.parse(parts[1]);
        String description = unescape(parts[2]);
        double amount = Double.parseDouble(parts[3]);
        String category = unescape(parts[4]);
        return new Expense(id, date, description, amount, category.isEmpty() ? null : category);
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("|", "\\|");
    }

    private static String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                sb.append(s.charAt(++i));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
