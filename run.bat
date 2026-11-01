@echo off
echo Starting Personal Expense Tracker...
if exist PersonalExpenseTracker.jar (
    java -jar PersonalExpenseTracker.jar
) else (
    java -cp bin expense.Main
)
