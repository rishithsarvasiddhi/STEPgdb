/**
 * Enhanced Account entity class with validation and security.
 * All methods return boolean to indicate success/failure.
*/
class AccountEnhanced {
    
    // ===== Constants =====
    private static final double MIN_BALANCE_SAVINGS = 500.0;
    private static final double MIN_BALANCE_CURRENT = 1000.0;
    private static final int MIN_AGE = 18;
    private static final int MIN_PIN = 1000;
    private static final int MAX_PIN = 9999;
    
    // ===== Fields =====
    private int accountNumber;
    private String name;
    private int age;
    private double balance;
    private String accountType;
    private String status;
    private Integer pin;  // null if not set
    
    // ===== Constructor =====
    public AccountEnhanced(int accountNumber, String name, int age, 
                           double initialBalance, String accountType) {
        this.accountNumber = accountNumber;
        this.name = name;
        
        // Validate and correct age
        this.age = (age >= MIN_AGE) ? age : MIN_AGE;
        
        // Validate and correct account type
        if (accountType.equals("Savings") || accountType.equals("Current")) {
            this.accountType = accountType;
        } else {
            this.accountType = "Savings";
        }
        
        // Validate and correct minimum balance
        double minBalance = getMinimumBalance();
        this.balance = (initialBalance >= minBalance) ? initialBalance : minBalance;
        
        this.status = "Active";
        this.pin = null;
    }
    
    // ===== Helper Methods =====
    private double getMinimumBalance() {
        if (this.accountType.equals("Savings")) {
            return MIN_BALANCE_SAVINGS;
        } else {
            return MIN_BALANCE_CURRENT;
        }
    }
    
    private boolean isActive() {
        return this.status.equals("Active");
    }
    
    // ===== Business Methods =====
    
    public boolean deposit(double amount) {
        // Check if account is active
        if (!isActive()) {
            return false;
        }
        
        // Check amount is positive
        if (amount <= 0) {
            return false;
        }
        
        this.balance = this.balance + amount;
        return true;
    }
    
    public boolean withdraw(double amount, int pin) {
        // Check if account is active
        if (!isActive()) {
            return false;
        }
        
        // Check PIN is set
        if (this.pin == null) {
            return false;
        }
        
        // Verify PIN
        if (this.pin != pin) {
            return false;
        }
        
        // Check amount is positive
        if (amount <= 0) {
            return false;
        }
        
        // Check minimum balance after withdrawal
        double minBalance = getMinimumBalance();
        if (this.balance - amount < minBalance) {
            return false;
        }
        
        this.balance = this.balance - amount;
        return true;
    }
    
    // ===== Account Status Management =====
    
    public boolean closeAccount() {
        if (!isActive()) {
            return false;
        }
        this.status = "Inactive";
        return true;
    }
    
    public boolean reopenAccount() {
        if (isActive()) {
            return false;
        }
        this.status = "Active";
        return true;
    }
    
    // ===== PIN Management =====
    
    public boolean setPin(int pin) {
        if (pin >= MIN_PIN && pin <= MAX_PIN) {
            this.pin = pin;
            return true;
        }
        return false;
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
    
    // ===== Getters =====
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
    
    // ===== Setters =====
    public void setName(String name) {
        this.name = name;
    }
    
    public void setAge(int age) {
        if (age >= MIN_AGE) {
            this.age = age;
        }
        // If age < 18, we just ignore the update
    }
}

/**
 * Test class for Enhanced Account entity with boolean returns.
 * All display logic is handled here, not in the Account class.
 */
class TestAccountEnhanced {
    
    private static void printAccountInfo(AccountEnhanced acc) {

    }
    
    public static void main(String[] args) {

    }
}

