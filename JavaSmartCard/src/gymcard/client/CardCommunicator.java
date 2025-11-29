package gymcard.client;

/**
 * Mock Card Communicator - Simulates card operations for UI demo
 * This version works WITHOUT physical card reader
 * Perfect for demo with JCIDE or testing UI functionality
 */
public class CardCommunicator {
    
    // Simulated card state
    private boolean connected;
    private boolean authenticated;
    private int pinTries;
    private String currentPin;
    private static final String DEFAULT_PIN = "1234";
    private static final String ADMIN_PIN = "9999";
    
    // Simulated member data
    private MemberInfo memberInfo;
    private PackageInfo packageInfo;
    private CheckInInfo lastCheckIn;
    private short balance;
    private int checkInCount;
    private TransactionInfo[] transactions;
    private int transactionCount;
    
    public CardCommunicator() {
        connected = false;
        authenticated = false;
        pinTries = 3;
        currentPin = DEFAULT_PIN;
        balance = 0;
        checkInCount = 0;
        transactions = new TransactionInfo[10];
        transactionCount = 0;
        
        // Initialize with default data
        initializeDefaultData();
    }
    
    /**
     * Initialize default demo data
     */
    private void initializeDefaultData() {
        memberInfo = new MemberInfo();
        memberInfo.name = "";
        memberInfo.birthDate = "";
        memberInfo.phone = "";
        memberInfo.address = "";
        
        packageInfo = new PackageInfo();
        packageInfo.type = 0;
        packageInfo.expiry = "";
        packageInfo.registration = "";
        packageInfo.remainingSessions = 0;
        
        lastCheckIn = new CheckInInfo();
        lastCheckIn.date = "";
        lastCheckIn.checkInTime = "";
        lastCheckIn.checkOutTime = "";
    }
    
    /**
     * Simulate card connection (always succeeds for demo)
     */
    public void connect() throws Exception {
        // Simulate connection delay
        Thread.sleep(500);
        connected = true;
        System.out.println("[MOCK] Card connected successfully");
    }
    
    /**
     * Disconnect from card
     */
    public void disconnect() {
        connected = false;
        authenticated = false;
        System.out.println("[MOCK] Card disconnected");
    }
    
    /**
     * Check if connected
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Verify PIN
     */
    public boolean verifyPin(String pin) throws Exception {
        if (!connected) {
            throw new Exception("Card not connected");
        }
        
        if (pinTries == 0) {
            throw new Exception("Card is blocked");
        }
        
        // Simulate processing time
        Thread.sleep(300);
        
        if (pin.equals(currentPin)) {
            authenticated = true;
            pinTries = 3; // Reset on success
            System.out.println("[MOCK] PIN verified successfully");
            return true;
        } else {
            pinTries--;
            authenticated = false;
            System.out.println("[MOCK] Invalid PIN. Tries remaining: " + pinTries);
            return false;
        }
    }
    
    /**
     * Change PIN
     */
    public boolean changePin(String oldPin, String newPin) throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Not authenticated");
        }
        
        Thread.sleep(300);
        
        if (oldPin.equals(currentPin)) {
            currentPin = newPin;
            System.out.println("[MOCK] PIN changed successfully");
            return true;
        }
        
        return false;
    }
    
    /**
     * Unlock PIN with admin PIN
     */
    public boolean unlockPin(String adminPin) throws Exception {
        if (!connected) {
            throw new Exception("Card not connected");
        }
        
        Thread.sleep(300);
        
        if (adminPin.equals(ADMIN_PIN)) {
            pinTries = 3;
            currentPin = DEFAULT_PIN;
            authenticated = false;
            System.out.println("[MOCK] Card unlocked, PIN reset to default");
            return true;
        }
        
        return false;
    }
    
    /**
     * Get remaining PIN tries
     */
    public int getPinTries() throws Exception {
        if (!connected) {
            throw new Exception("Card not connected");
        }
        return pinTries;
    }
    
    /**
     * Set member information
     */
    public boolean setMemberInfo(String name, String birthDate, String phone, String address) 
            throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Not authenticated");
        }
        
        Thread.sleep(400);
        
        memberInfo.name = name;
        memberInfo.birthDate = birthDate;
        memberInfo.phone = phone;
        memberInfo.address = address;
        
        System.out.println("[MOCK] Member info saved: " + name);
        return true;
    }
    
    /**
     * Get member information
     */
    public MemberInfo getMemberInfo() throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Not authenticated");
        }
        
        Thread.sleep(300);
        return memberInfo;
    }
    
    /**
     * Set package information
     */
    public boolean setPackage(byte type, String expiry, String registration, short sessions) 
            throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Not authenticated");
        }
        
        Thread.sleep(400);
        
        packageInfo.type = type;
        packageInfo.expiry = expiry;
        packageInfo.registration = registration;
        packageInfo.remainingSessions = sessions;
        
        System.out.println("[MOCK] Package set: Type " + type);
        return true;
    }
    
    /**
     * Get package information
     */
    public PackageInfo getPackage() throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Not authenticated");
        }
        
        Thread.sleep(300);
        return packageInfo;
    }
    
    /**
     * Check-in
     */
    public boolean checkIn(String date, String time) throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Not authenticated");
        }
        
        Thread.sleep(400);
        
        lastCheckIn.date = date;
        lastCheckIn.checkInTime = time;
        lastCheckIn.checkOutTime = ""; // Clear checkout time
        
        checkInCount++;
        
        // Deduct session if session-based package
        if (packageInfo.type == 2 && packageInfo.remainingSessions > 0) {
            packageInfo.remainingSessions--;
        }
        
        System.out.println("[MOCK] Checked in at " + time);
        return true;
    }
    
    /**
     * Check-out
     */
    public boolean checkOut(String time) throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Not authenticated");
        }
        
        Thread.sleep(400);
        
        lastCheckIn.checkOutTime = time;
        
        System.out.println("[MOCK] Checked out at " + time);
        return true;
    }
    
    /**
     * Get check-in count this month
     */
    public int getCheckInCount() throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Not authenticated");
        }
        
        Thread.sleep(200);
        return checkInCount;
    }
    
    /**
     * Get last check-in information
     */
    public CheckInInfo getLastCheckIn() throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Not authenticated");
        }
        
        Thread.sleep(300);
        return lastCheckIn;
    }
    
    /**
     * Get balance
     */
    public short getBalance() throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Not authenticated");
        }
        
        Thread.sleep(200);
        return balance;
    }
    
    /**
     * Add balance
     */
    public boolean addBalance(short amount) throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Not authenticated");
        }
        
        Thread.sleep(400);
        
        balance += amount;
        
        // Record transaction
        recordTransaction((byte) 1, amount);
        
        System.out.println("[MOCK] Added balance: " + amount + "k VND. New balance: " + balance + "k VND");
        return true;
    }
    
    /**
     * Deduct balance
     */
    public boolean deductBalance(short amount) throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Not authenticated");
        }
        
        Thread.sleep(400);
        
        if (balance < amount) {
            System.out.println("[MOCK] Insufficient balance");
            return false;
        }
        
        balance -= amount;
        
        // Record transaction
        recordTransaction((byte) 2, amount);
        
        System.out.println("[MOCK] Deducted balance: " + amount + "k VND. New balance: " + balance + "k VND");
        return true;
    }
    
    /**
     * Get transaction
     */
    public TransactionInfo getTransaction(byte index) throws Exception {
        if (!connected || !authenticated) {
            throw new Exception("Not authenticated");
        }
        
        Thread.sleep(200);
        
        if (index >= transactionCount) {
            return null;
        }
        
        return transactions[index];
    }
    
    /**
     * Record a transaction
     */
    private void recordTransaction(byte type, short amount) {
        if (transactionCount >= 10) {
            // Shift array left
            for (int i = 1; i < 10; i++) {
                transactions[i - 1] = transactions[i];
            }
            transactionCount = 9;
        }
        
        TransactionInfo trans = new TransactionInfo();
        trans.date = lastCheckIn.date.isEmpty() ? 
            new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date()) : 
            lastCheckIn.date;
        trans.time = new java.text.SimpleDateFormat("HHmmss").format(new java.util.Date());
        trans.amount = amount;
        trans.type = type;
        
        transactions[transactionCount++] = trans;
    }
    
    /**
     * Reset card to default state (for testing)
     */
    public void resetCard() {
        currentPin = DEFAULT_PIN;
        pinTries = 3;
        authenticated = false;
        balance = 0;
        checkInCount = 0;
        transactionCount = 0;
        initializeDefaultData();
        System.out.println("[MOCK] Card reset to default state");
    }
    
    /**
     * Load demo data (for testing UI)
     */
    public void loadDemoData() {
        memberInfo.name = "Nguyễn Văn Demo";
        memberInfo.birthDate = "19900101";
        memberInfo.phone = "0987654321";
        memberInfo.address = "123 Đường ABC, Quận XYZ, Hà Nội";
        
        packageInfo.type = 3; // VIP
        packageInfo.expiry = "20261231";
        packageInfo.registration = "20250101";
        packageInfo.remainingSessions = 50;
        
        lastCheckIn.date = "20251129";
        lastCheckIn.checkInTime = "080000";
        lastCheckIn.checkOutTime = "100000";
        
        balance = 500; // 500k VND
        checkInCount = 15;
        
        // Add some demo transactions
        for (int i = 0; i < 5; i++) {
            recordTransaction((byte) (i % 2 + 1), (short) (50 + i * 10));
        }
        
        System.out.println("[MOCK] Demo data loaded");
    }
}
