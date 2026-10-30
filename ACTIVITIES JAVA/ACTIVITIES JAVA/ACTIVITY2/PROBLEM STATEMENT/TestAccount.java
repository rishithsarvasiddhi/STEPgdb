class Account {
    
    // ===== Fields =====
    private int accountNumber;
    private String name;
    private int age;
    private double balance;
    private String accountType;
    private String status;
    
    // ===== Constructor =====
    public Account(int accountNumber, String name, int age, 
                   double initialBalance, String accountType) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.age = age;
        this.balance = initialBalance;
        this.accountType = accountType;
        this.status = "Active";
    }
    
    // ===== Business Methods =====
    
    /**
     * Deposits amount into the account.
     * @param amount Amount to deposit (must be positive)
     * @return true if successful, false if amount <= 0
     */
    public boolean deposit(double amount) {
        if (amount <= 0) {
            return false;
        }
        this.balance = this.balance + amount;
        return true;
    }
    
    /**
     * Withdraws amount from the account.
     * @param amount Amount to withdraw (must be positive and <= balance)
     * @return true if successful, false if invalid amount or insufficient balance
     */
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            return false;
        }
        if (amount > this.balance) {
            return false;
        }
        this.balance = this.balance - amount;
        return true;
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
    
    // ===== Setters =====
    public void setName(String name) {
        this.name = name;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
}

class TestAccount {
    
    private static void printAccountInfo(Account acc) {
    
    }
    
    public static void main(String[] args) {

    }
}
