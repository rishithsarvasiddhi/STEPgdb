/**
 * Test class for Account with exceptions.
 * Compatible with OnlineGDB.
 */
public class TestAccountExceptions {

    private static void printAccountInfo(Account acc) {
        String pinStatus = acc.hasPin() ? "Yes" : "No";

        System.out.println(
            "Account #" + acc.getAccountNumber() +
            " | " + acc.getName() +
            " (" + acc.getAge() + " yrs)" +
            " | " + acc.getAccountType() +
            " | ₹" + acc.getBalance() +
            " | " + acc.getStatus() +
            " | PIN: " + pinStatus
        );
    }

    private static void printException(Exception e) {
        System.out.println(
            "EXCEPTION: " +
            e.getClass().getSimpleName() +
            " - " +
            e.getMessage()
        );
    }

    public static void main(String[] args) {

        
}


/*
 * ============================================================
 * ACCOUNT CLASS
 * ============================================================
 */

class Account {

    private static final double MIN_BALANCE_SAVINGS = 500.0;
    private static final double MIN_BALANCE_CURRENT = 1000.0;

    private static final int MIN_AGE = 18;
    private static final int MIN_PIN = 1000;
    private static final int MAX_PIN = 9999;

    private int accountNumber;
    private String name;
    private int age;
    private double balance;
    private String accountType;
    private String status;
    private Integer pin;


    public Account(
            int accountNumber,
            String name,
            int age,
            double initialBalance,
            String accountType)
            throws IllegalArgumentException {

        if (age < MIN_AGE) {
            throw new IllegalArgumentException(
                "Customer must be at least " +
                MIN_AGE +
                " years old. Provided: " +
                age
            );
        }


        if (!"Savings".equals(accountType)
                && !"Current".equals(accountType)) {

            throw new IllegalArgumentException(
                "Account type must be 'Savings' or 'Current'. " +
                "Provided: " +
                accountType
            );
        }


        double minBalance =
            getMinimumBalance(accountType);


        if (initialBalance < minBalance) {

            throw new IllegalArgumentException(
                accountType +
                " account requires minimum balance of ₹" +
                minBalance +
                ". Provided: ₹" +
                initialBalance
            );
        }


        this.accountNumber = accountNumber;
        this.name = name;
        this.age = age;
        this.balance = initialBalance;
        this.accountType = accountType;
        this.status = "Active";
        this.pin = null;
    }


    private double getMinimumBalance(String accountType) {

        if ("Savings".equals(accountType)) {
            return MIN_BALANCE_SAVINGS;
        }

        return MIN_BALANCE_CURRENT;
    }


    private double getMinimumBalance() {
        return getMinimumBalance(this.accountType);
    }


    private void validateActive()
            throws InactiveAccountException {

        if (!"Active".equals(this.status)) {

            throw new InactiveAccountException(
                "Account is inactive. " +
                "Please reopen the account or contact support."
            );
        }
    }


    public void deposit(double amount)
            throws InvalidAmountException,
                   InactiveAccountException {

        validateActive();


        if (amount <= 0) {

            throw new InvalidAmountException(
                "Deposit amount must be positive. " +
                "Provided: ₹" +
                amount
            );
        }


        this.balance += amount;
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


        if (this.pin == null) {

            throw new InvalidPinException(
                "PIN not set for this account."
            );
        }


        if (!this.pin.equals(pin)) {

            throw new InvalidPinException(
                "Incorrect PIN."
            );
        }


        if (amount <= 0) {

            throw new InvalidAmountException(
                "Withdrawal amount must be positive. " +
                "Provided: ₹" +
                amount
            );
        }


        if (amount > this.balance) {

            throw new InsufficientBalanceException(
                "Insufficient balance. Available: ₹" +
                this.balance +
                ", Requested: ₹" +
                amount
            );
        }


        double minBalance =
            getMinimumBalance();


        if (this.balance - amount < minBalance) {

            throw new MinimumBalanceViolationException(
                "Cannot withdraw. Minimum balance of ₹" +
                minBalance +
                " required. " +
                "Available after withdrawal: ₹" +
                (this.balance - amount)
            );
        }


        this.balance -= amount;
    }


    public void closeAccount()
            throws IllegalStateException {

        if (!"Active".equals(this.status)) {

            throw new IllegalStateException(
                "Account is already closed."
            );
        }

        this.status = "Inactive";
    }


    public void reopenAccount()
            throws IllegalStateException {

        if ("Active".equals(this.status)) {

            throw new IllegalStateException(
                "Account is already active."
            );
        }

        this.status = "Active";
    }


    public void setPin(int pin)
            throws IllegalArgumentException {

        if (pin < MIN_PIN || pin > MAX_PIN) {

            throw new IllegalArgumentException(
                "PIN must be a 4-digit number (" +
                MIN_PIN +
                "-" +
                MAX_PIN +
                ")"
            );
        }

        this.pin = pin;
    }


    public boolean verifyPin(int pin) {

        if (this.pin == null) {
            return false;
        }

        return this.pin.equals(pin);
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


    public String getAccountType() {
        return accountType;
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

        if (age < MIN_AGE) {

            throw new IllegalArgumentException(
                "Customer must be at least " +
                MIN_AGE +
                " years old."
            );
        }

        this.age = age;
    }
}


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