# Expense-Tracker

A simple command-line expense tracker built in Java. Add, update, delete, and
view expenses, get spending summaries, set monthly budgets, and export your
data to CSV.

## Requirements

- Java 17 or later (uses `switch` expressions and text blocks)

## Compiling

```bash
cd src
javac -d ../bin *.java
```

## Running

From the `bin` directory (or add it to your classpath):

```bash
java ExpenseTracker <command> [options]
```

Data is stored in two plain-text files created next to wherever you run the
program from: `expenses.dat` (your expenses) and `budgets.dat` (your monthly
budgets). Delete these files to reset the tracker.

## Commands

| Command  | Description |
|----------|-------------|
| `add`    | Add a new expense |
| `update` | Update an existing expense |
| `delete` | Delete an expense |
| `list`   | View all expenses (optionally filtered by category) |
| `summary`| View a total summary, or a summary for one month |
| `budget` | Set a spending budget for a given month |
| `export` | Export all expenses to a CSV file |

### Add an expense

```bash
java ExpenseTracker add --description "Lunch" --amount 20 --category Food
```
`--category` is optional.

### Update an expense

```bash
java ExpenseTracker update --id 1 --description "Lunch with client" --amount 25
```
Only the fields you pass are changed.

### Delete an expense

```bash
java ExpenseTracker delete --id 1
```

### List expenses

```bash
java ExpenseTracker list
java ExpenseTracker list --category Food
```

### View a summary

```bash
java ExpenseTracker summary            # total + breakdown by category
java ExpenseTracker summary --month 8  # total for August of the current year
```

### Set a monthly budget

```bash
java ExpenseTracker budget --month 8 --amount 500
```

Once a budget is set for a month, adding an expense that pushes that month's
total over budget prints a warning, and `summary --month <n>` shows whether
you're within or over budget.

### Export to CSV

```bash
java ExpenseTracker export                    # writes expenses.csv
java ExpenseTracker export --file my-data.csv # or a custom filename
```

## Example session

```bash
$ java ExpenseTracker add --description "Lunch" --amount 20 --category Food
Expense added successfully (ID: 1)

$ java ExpenseTracker add --description "Bus fare" --amount 5.50 --category Transport
Expense added successfully (ID: 2)

$ java ExpenseTracker list
ID   Date         Description               Amount     Category
1    2026-09-25   Lunch                     20.00      Food
2    2026-09-25   Bus fare                  5.50       Transport

$ java ExpenseTracker summary
Total expenses: 25.50
By category:
  Food            20.00
  Transport       5.50
```

## Project structure


https://github.com/MALAMBO21/Expense-Tracker
```
src/
  Expense.java         Expense model (id, date, description, amount, category)
  ExpenseManager.java  Storage, CRUD logic, totals, budgets, CSV export
  ExpenseTracker.java  Command-line entry point / argument parsing
```
