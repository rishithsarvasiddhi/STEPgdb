import java.util.ArrayList;

/*
 * ============================================================
 * EXCEPTION CLASSES
 * ============================================================
 */

class InvalidAmountException extends Exception {
    public InvalidAmountException(String message) {
        super(message);
    }
}

class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}

class MinimumBalanceViolationException extends Exception {
    public MinimumBalanceViolationException(String message) {
        super(message);
    }
}

class InactiveAccountException extends Exception {
    public InactiveAccountException(String message) {
        super(message);
    }
}

class InvalidPinException extends Exception {
    public InvalidPinException(String message) {
        super(message);
    }
}


/*
 * ============================================================
 * ABSTRACT ACCOUNT CLASS
 * ============================================================
 */

abstract class Account {

    protected static final int MIN_AGE = 18;
    protected static final int MIN_PIN = 1000;
    protected static final int MAX_PIN = 9999;

    private int accountNumber;
    private String name;
    private int age;
    private double balance;
    private String status;
    private Integer pin;

    public abstract double getMinimumBalance();

    public abstract String getAccountType();

    public Account(
            int accountNumber,
            String name,
            int age,
            double initialBalance) {

        if (age < MIN_AGE) {
            throw new IllegalArgumentException(
                "Customer must be at least "
                + MIN_AGE
                + " years old. Provided: "
                + age
            );
        }

        double minBalance = getMinimumBalance();

        if (initialBalance < minBalance) {
            throw new IllegalArgumentException(
                getAccountType()
                + " account requires minimum balance of Rs."
                + minBalance
                + ". Provided: Rs."
                + initialBalance
            );
        }

        this.accountNumber = accountNumber;
        this.name = name;
        this.age = age;
        this.balance = initialBalance;
        this.status = "Active";
        this.pin = null;
    }

    protected void validateActive()
            throws InactiveAccountException {

        if (!this.status.equals("Active")) {
            throw new InactiveAccountException(
                "Account is inactive. Please reopen the account or contact support."
            );
        }
    }

    protected void validatePin(int pin)
            throws InvalidPinException {

        if (this.pin == null) {
            throw new InvalidPinException(
                "PIN not set for this account"
            );
        }

        if (this.pin != pin) {
            throw new InvalidPinException(
                "Incorrect PIN"
            );
        }
    }

    protected void validateAmount(double amount)
            throws InvalidAmountException {

        if (amount <= 0) {
            throw new InvalidAmountException(
                "Amount must be positive. Provided: Rs."
                + amount
            );
        }
    }

    public void deposit(double amount)
            throws InvalidAmountException,
                   InactiveAccountException {

        validateActive();
        validateAmount(amount);

        this.balance = this.balance + amount;
    }

    public void withdraw(
            double amount,
            int pin)
            throws InvalidAmountException,
                   InsufficientBalanceException,
                   MinimumBalanceViolationException,
                   InactiveAccountException,
                   InvalidPinException {

        validateActive();

        validatePin(pin);

        validateAmount(amount);

        if (amount > this.balance) {
            throw new InsufficientBalanceException(
                "Insufficient balance. Available: Rs."
                + this.balance
                + ", Requested: Rs."
                + amount
            );
        }

        double minBalance = getMinimumBalance();

        if (this.balance - amount < minBalance) {
            throw new MinimumBalanceViolationException(
                "Cannot withdraw. Minimum balance of Rs."
                + minBalance
                + " required. Available after withdrawal: Rs."
                + (this.balance - amount)
            );
        }

        this.balance = this.balance - amount;
    }

    public void closeAccount() {

        if (!this.status.equals("Active")) {
            throw new IllegalStateException(
                "Account is already closed"
            );
        }

        this.status = "Inactive";
    }

    public void reopenAccount() {

        if (this.status.equals("Active")) {
            throw new IllegalStateException(
                "Account is already active"
            );
        }

        this.status = "Active";
    }

    public void setPin(int pin) {

        if (pin < MIN_PIN || pin > MAX_PIN) {
            throw new IllegalArgumentException(
                "PIN must be a 4-digit number ("
                + MIN_PIN
                + "-"
                + MAX_PIN
                + ")"
            );
        }

        this.pin = pin;
    }

    public boolean verifyPin(int pin) {

        if (this.pin == null) {
            return false;
        }

        return this.pin == pin;
    }

    public boolean hasPin() {
        return this.pin != null;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public double getBalance() {
        return balance;
    }

    public String getStatus() {
        return status;
    }

    public Integer getPin() {
        return pin;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {

        if (age >= MIN_AGE) {
            this.age = age;
        }
    }

    protected void setBalance(double balance) {
        this.balance = balance;
    }
}


/*
 * ============================================================
 * SAVINGS ACCOUNT
 * ============================================================
 */

class SavingsAccount extends Account {

    private static final double MINIMUM_BALANCE = 500.0;
    private static final String ACCOUNT_TYPE = "Savings";
    private static final double INTEREST_RATE = 4.0;

    public SavingsAccount(
            int accountNumber,
            String name,
            int age,
            double initialBalance) {

        super(
            accountNumber,
            name,
            age,
            initialBalance
        );
    }

    @Override
    public double getMinimumBalance() {
        return MINIMUM_BALANCE;
    }

    @Override
    public String getAccountType() {
        return ACCOUNT_TYPE;
    }

    public double calculateInterest(int years) {

        if (years < 0) {
            throw new IllegalArgumentException(
                "Years must be non-negative"
            );
        }

        return getBalance()
                * (INTEREST_RATE / 100)
                * years;
    }

    public double getInterestRate() {
        return INTEREST_RATE;
    }
}


/*
 * ============================================================
 * CURRENT ACCOUNT
 * ============================================================
 */

class CurrentAccount extends Account {

    private static final double MINIMUM_BALANCE = 1000.0;
    private static final String ACCOUNT_TYPE = "Current";
    private static final double OVERDRAFT_LIMIT = 5000.0;

    private double overdraftUsed;

    public CurrentAccount(
            int accountNumber,
            String name,
            int age,
            double initialBalance) {

        super(
            accountNumber,
            name,
            age,
            initialBalance
        );

        this.overdraftUsed = 0.0;
    }

    @Override
    public double getMinimumBalance() {
        return MINIMUM_BALANCE;
    }

    @Override
    public String getAccountType() {
        return ACCOUNT_TYPE;
    }

    @Override
    public void withdraw(
            double amount,
            int pin)
            throws InvalidAmountException,
                   InsufficientBalanceException,
                   MinimumBalanceViolationException,
                   InactiveAccountException,
                   InvalidPinException {

        validateActive();

        validatePin(pin);

        validateAmount(amount);

        double availableBalance =
                getBalance()
                - getMinimumBalance()
                + OVERDRAFT_LIMIT
                - overdraftUsed;

        if (amount > availableBalance) {

            throw new InsufficientBalanceException(
                "Insufficient funds. Available: Rs."
                + availableBalance
                + " (including Rs."
                + OVERDRAFT_LIMIT
                + " overdraft), Requested: Rs."
                + amount
            );
        }

        double newBalance =
                getBalance() - amount;

        if (newBalance < getMinimumBalance()) {

            double overdraftAmount =
                    getMinimumBalance() - newBalance;

            overdraftUsed =
                    overdraftUsed + overdraftAmount;

        } else {

            overdraftUsed = 0.0;
        }

        setBalance(newBalance);
    }

    public double getOverdraftLimit() {
        return OVERDRAFT_LIMIT;
    }

    public double getOverdraftUsed() {
        return overdraftUsed;
    }

    public double getAvailableOverdraft() {
        return OVERDRAFT_LIMIT - overdraftUsed;
    }

    public boolean isUsingOverdraft() {
        return overdraftUsed > 0;
    }

    public void repayOverdraft(double amount) {

        if (amount <= 0) {

            throw new IllegalArgumentException(
                "Repayment amount must be positive"
            );
        }

        if (amount > overdraftUsed) {

            throw new IllegalArgumentException(
                "Amount exceeds overdraft used (Rs."
                + overdraftUsed
                + ")"
            );
        }

        overdraftUsed =
                overdraftUsed - amount;

        setBalance(
            getBalance() + amount
        );
    }
}


/*
 * ============================================================
 * TEST CLASS
 * ============================================================
 */

public class TestAccountSubclasses {

    private static void printAccountInfo(Account acc) {

        String pinStatus =
                acc.hasPin()
                ? "Yes"
                : "No";

        System.out.println(
            "Account #"
            + acc.getAccountNumber()
            + " | "
            + acc.getName()
            + " ("
            + acc.getAge()
            + " yrs)"
            + " | "
            + acc.getAccountType()
            + " | Rs."
            + acc.getBalance()
            + " | "
            + acc.getStatus()
            + " | PIN: "
            + pinStatus
        );
    }

    private static void printException(Exception e) {

        System.out.println(
            "EXCEPTION: "
            + e.getMessage()
        );
    }

    private static void printSavingsDetails(
            SavingsAccount acc) {

        System.out.println(
            "Savings Account - Type: "
            + acc.getAccountType()
            + ", Minimum Balance: Rs."
            + acc.getMinimumBalance()
        );

        System.out.println(
            "Interest Rate: "
            + acc.getInterestRate()
            + "% per annum"
        );
    }

    private static void printCurrentDetails(
            CurrentAccount acc) {

        System.out.println(
            "Current Account - Type: "
            + acc.getAccountType()
            + ", Minimum Balance: Rs."
            + acc.getMinimumBalance()
        );

        System.out.println(
            "Overdraft Limit: Rs."
            + acc.getOverdraftLimit()
        );

        System.out.println(
            "Available Overdraft: Rs."
            + acc.getAvailableOverdraft()
        );

        System.out.println(
            "Overdraft Used: Rs."
            + acc.getOverdraftUsed()
        );

        System.out.println(
            "Is Using Overdraft: "
            + acc.isUsingOverdraft()
        );
    }


    public static void main(String[] args) {
        
    }
}