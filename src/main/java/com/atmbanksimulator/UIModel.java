package com.atmbanksimulator;

// ===== 🧠 UIModel (Brain) =====

// The UIModel represents all the actual content and functionality of the app
// For the ATM, it keeps track of the information shown in the display
// (the laMsg and two tfInput boxes), and the interaction with the bank, executes
// commands provided by the controller and tells the view to update when
// something changes
public class UIModel {
    View view; // Reference to the View (part of the MVC setup)
    private Bank bank; // The ATM communicates with this Bank
    private int loginAttempts = 0;
    public String targetAccount;
    // The ATM UIModel can be in one of three states:
    // 1. Waiting for an account number
    // 2. Waiting for a password
    // 3. Logged in (ready to process requests for the logged-in account)
    // We represent each state with a String constant.
    // The 'final' keyword ensures these values cannot be changed.
    private final String STATE_ACCOUNT_NO = "account_no";
    private final String STATE_PASSWORD = "password";
    private final String STATE_LOGGED_IN = "logged_in";
    private final String STATE_CHANGE_PW_NEW = "change_pw_new";
    private final String STATE_CHANGE_PW_OLD = "change_pw_old";
    private final String STATE_NEW_ACC_NUMBER = "new_acc_number";
    private final String STATE_NEW_ACC_PASSWD = "new_acc_passwd";
    private final String STATE_NEW_ACC_BALANCE = "new_acc_balance";
    private final String STATE_NEW_ACC_TYPE = "new_acc_type";
    private final String STATE_TRANSFER_ACC = "transfer_acc";
    private final String STATE_TRANSFER_AMOUNT = "transfer_amount";
    private final String STATE_CONFIRM_WITHDRAW = "confirm_withdraw";
    private final String STATE_CONFIRM_TRANSFER = "confirm_transfer";

    private static final int LARGE_WITHDRAW_THRESHOLD = 200;
    private int pendingWithdrawAmount = 0;
    private int pendingTransferAmount = 0;

    // Variables representing the state and data of the ATM UIModel
    private String state = STATE_ACCOUNT_NO;    // Current state of the ATM
    private String accNumber = "";         // Account number being typed
    private String accPasswd = "";         // Password being typed
    private String oldPasswdInput = "";
    private String newAccNumber = "";
    private String newAccPasswd = "";
    private int newAccBalance = 0;

    // Variables shown on the View display
    private String message;                // Message label text
    private String numberPadInput;         // Current number displayed in the TextField (as a string)
    private String result;                 // Contents of the TextArea (may be multiple lines)

    // UIModel constructor: pass a Bank object that the ATM interacts with
    public UIModel(Bank bank) {
        this.bank = bank;
    }

    // Initialize the ATM UIModel: this method is called by Main when starting the app
    // - Set state to STATE_ACCOUNT_NO
    // - Clear the numberPadInput - numbers displayed in the TextField
    // - Display the welcome message and user instructions
    public void initialise() {
        setState(STATE_ACCOUNT_NO);
        numberPadInput = "";
        message = "Welcome to the ATM";
        result = "Enter your account number\nFollowed by \"Enter\"";
        update();
    }

    // Reset the ATM UIModel after an invalid action or logout:
    // - Set state to STATE_ACCOUNT_NO
    // - Clear the numberPadInput
    // - Display the provided message and user instructions
    private void reset(String msg) {
        setState(STATE_ACCOUNT_NO);
        numberPadInput = "";
        message = msg;
        result = "Enter your account number\nFollowed by \"Enter\"";
    }

    // Change the ATM state and print a debug message whenever the state changes
    private void setState(String newState)
    {
        if ( !state.equals(newState) )
        {
            String oldState = state;
            state = newState;
            System.out.println("UIModel::setState: changed state from "+ oldState + " to " + newState);
        }
    }

    // These process**** methods are called by the Controller
    // in response to specific button presses on the GUI.

    // Handle a number button press: append the digit to numberPadInput
    public void processNumber(String numberOnButton) {
        // Optional extension:
        // Improve feedback by showing what the number is being entered for based on the current state.
        // e.g.  if state is STATE_ACCOUNT_NO, display "Receiving Account Number, Beep 5 received"
        numberPadInput += numberOnButton;
        message = "Beep! " + numberOnButton + " received";
        update();
    }

    // Handle the Clear button: reset the current number stored in numberPadInput
    public void processClear() {
        if (state.equals(STATE_CONFIRM_WITHDRAW) || state.equals(STATE_CONFIRM_TRANSFER)) {
            pendingWithdrawAmount = 0;
            pendingTransferAmount = 0;
            numberPadInput = "";
            setState(STATE_LOGGED_IN);
            message = "Cancelled";
            result = "Now enter the amount\nThen press transaction\n(Deposit, Withdraw, or Transfer)";
            update();
            return;
        }
        if (!numberPadInput.isEmpty()) {
            numberPadInput = "";
            message = "Input Cleared";
            update();
        }
    }

    public void processChangePassword(){
        //make sure they are logged in
        if (!state.equals(STATE_LOGGED_IN)){
            reset("You are not logged in");
        }
        else {
            setState(STATE_CHANGE_PW_OLD);
            numberPadInput = "";
            message = "Change Password";
            result = "Enter your OLD password\nFollowed by \"Enter\"";
        }
        update();
    }

    public void processCreateAccount(){
        setState(STATE_NEW_ACC_NUMBER);
        numberPadInput = "";
        message = "Create New Account";
        result = "Enter new account number\nFollowed by \"Enter\"";
        update();
    }

    // Handle the Enter button.
    // This is a more complex method: pressing Enter causes the ATM to change state,
    // progressing from STATE_ACCOUNT_NO → STATE_PASSWORD → STATE_LOGGED_IN,
    // and back to STATE_ACCOUNT_NO when logging out.
    public void processEnter()
    {
        // The action depends on the current ATM state
        switch ( state )
        {
            case STATE_ACCOUNT_NO:
                // Waiting for a complete account number
                // If nothing was entered, reset with "Invalid Account Number"
                if (numberPadInput.equals("")) {
                    message = "Invalid Account Number";
                    reset(message);
                }
                else{
                    // Save the entered number as accNumber, clear numberPadInput,
                    // update the state to expect password, and provide instructions
                    accNumber = numberPadInput;
                    numberPadInput = "";
                    setState(STATE_PASSWORD);
                    message = "Account Number Accepted";
                    result = "Now enter your password\nFollowed by \"Enter\"";
                }
                break;
            case STATE_PASSWORD:
                accPasswd = numberPadInput;
                numberPadInput = "";

                if (bank.login(accNumber, accPasswd)) {
                    loginAttempts = 0; // initialise loginAttempt
                    setState(STATE_LOGGED_IN);
                    message = "Logged In";
                    result = "Now enter the amount\nThen press transaction\n(Deposit, Withdraw, or Transfer)";
                } else {
                    loginAttempts++; // Adds 1 if the password is wrong

                    if (loginAttempts >= 3) {
                        message = "Too many failed attempts. Returning to start.";
                        reset(message);
                        loginAttempts = 0; // reset after 3 trials
                    } else {
                        message = "Login failed (" + loginAttempts + "/3)";
                        result = "Try again\nEnter your password";
                        setState(STATE_PASSWORD);
                    }
                }
                break;

            case STATE_CHANGE_PW_OLD:
                oldPasswdInput = numberPadInput;
                numberPadInput = "";
                setState(STATE_CHANGE_PW_NEW);
                message = "Old password received";
                result = "Now enter your NEW password\nFollowed by \"Enter\"";
                break;

            case STATE_CHANGE_PW_NEW:
                String newPw = numberPadInput;
                numberPadInput = "";
                if (bank.changePassword(oldPasswdInput, newPw)){
                    setState(STATE_LOGGED_IN);
                    message = "Password Changed Successfully";
                    result = "Now enter the amount\nThen press transaction\n(Deposit, Withdraw, or Transfer)";
                }
                else {
                    setState(STATE_LOGGED_IN);
                    message = "Password Change Failed";
                    result = "Old password incorrect or new password invalid";
                }
                break;

            case STATE_NEW_ACC_NUMBER:
                newAccNumber = numberPadInput; numberPadInput ="";
                setState(STATE_NEW_ACC_PASSWD);
                message = "Account number saved";
                result = "Enter new account password\nFollowed by \"Enter\"";
                break;

            case STATE_NEW_ACC_PASSWD:
                newAccPasswd = numberPadInput;  numberPadInput = "";
                setState(STATE_NEW_ACC_BALANCE);
                message = "Password saved";
                result = "Enter initial balance\nFollowed by \"Enter\"";
                break;

            case STATE_NEW_ACC_BALANCE:
                newAccBalance = parseValidAmount(numberPadInput);  numberPadInput = "";
                setState(STATE_NEW_ACC_TYPE);
                message = "Balance saved";
                result = "Enter type: 0=Standard 1=Student\n2=Prime 3=Saving — then \"Enter\"";
                break;

            case STATE_NEW_ACC_TYPE:
                String[] types = {"standard","student","prime","saving"};
                String chosenType = "standard";
                try {
                    int idx = Integer.parseInt(numberPadInput);
                    if (idx >= 0 && idx < types.length) chosenType = types[idx];
                } catch (NumberFormatException e) { /* keep default */ }
                numberPadInput = "";
                int result_code = bank.createNewAccount(newAccNumber, newAccPasswd, newAccBalance, chosenType);
                switch (result_code) {
                    case 0: message = "Account Created!";    result = "Account: " + newAccNumber; break;
                    case 1: message = "Account already exists"; result = "Choose a different number"; break;
                    case 2: message = "Bank is full";          result = "Cannot add more accounts"; break;
                }
                setState(STATE_ACCOUNT_NO);
                break;

            case STATE_TRANSFER_ACC://Afonso
                targetAccount = numberPadInput;
                numberPadInput = "";
                setState(STATE_TRANSFER_AMOUNT);
                message = "Target account saved";
                result = "Enter amount\nThen press Enter";
                break;
            case STATE_TRANSFER_AMOUNT:
                int amount = parseValidAmount(numberPadInput);
                numberPadInput = "";

                if (amount <= 0){
                    message = "Invalid amount";//Transfer amount cannot be 0;
                    result = "Try again";
                    setState(STATE_TRANSFER_AMOUNT);
                }
                else if (!bank.accountValid(targetAccount)) {
                    message = "Target account does not exist";
                    setState(STATE_LOGGED_IN);
                }
                else if (targetAccount.equals(accNumber)) {
                    message = "Cannot transfer to same account";
                    setState(STATE_LOGGED_IN);
                }
                else {
                    pendingTransferAmount = amount;
                    setState(STATE_CONFIRM_TRANSFER);
                    message = "Confirm Transfer";
                    result = "Send £" + amount + " to " + targetAccount + "?\nPress Enter to confirm\nPress CLR to cancel";
                }
                break;

            case STATE_CONFIRM_WITHDRAW:
                numberPadInput = "";
                executeWithdraw(pendingWithdrawAmount);
                pendingWithdrawAmount = 0;
                break;

            case STATE_CONFIRM_TRANSFER:
                numberPadInput = "";
                if (!bank.withdraw(pendingTransferAmount)) {
                    message = "Insufficient funds";
                } else {
                    bank.depositTo(targetAccount, pendingTransferAmount);
                    message = "Transfer successful";
                    result = "Sent: £" + pendingTransferAmount + " to " + targetAccount;
                    if (bank.isLoggedInLowBalance()) {
                        result += "\n⚠ Low balance warning!";
                    }
                    bank.saveToFile();
                }
                pendingTransferAmount = 0;
                setState(STATE_LOGGED_IN);
                break;
            case STATE_LOGGED_IN:
            default:

                // Do nothing for other states (user is already logged in)
        }

        update(); // Refresh the GUI to show messages and input
    }

    /**
     * Parses a string into a valid transaction amount.
     * - If the string is empty, invalid, or consists only of zeros, returns 0.
     * - Otherwise, returns the integer value.
     *
     * Purpose:
     * Helper method for validating user-entered amounts in transactions (Deposit, Withdraw, etc.).
     *
     * Note: If you later add features like Transfer, this method can be reused.
     */
    private int parseValidAmount(String number) {
        if (number.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return 0; // Invalid input -> treated as 0
        }
    }

    // Handle the Balance button:
    // - If the user is logged in, retrieve the current balance and update messages/results accordingly
    // - Otherwise, reset the ATM and display an error message
    public void processBalance() {
        if (state.equals(STATE_LOGGED_IN) ) {
            numberPadInput = "";
            message = "Balance Available";
            result = "Your Balance is: " + bank.getBalance();
        } else {
            reset("You are not logged in");
        }
        update();
    }

    // Handle the Withdraw button:
    // If the user is logged in, attempt to withdraw the amount entered;
    // otherwise, reset the ATM and display an error message.
    // Reads the amount from numberPadInput, validates it, and updates messages/results accordingly.
    public void processWithdraw() {
        if (state.equals(STATE_LOGGED_IN)) {
            int amount = parseValidAmount(numberPadInput);
            if (amount > 0) {
                if (amount >= LARGE_WITHDRAW_THRESHOLD) {
                    pendingWithdrawAmount = amount;
                    numberPadInput = "";
                    setState(STATE_CONFIRM_WITHDRAW);
                    message = "Confirm Withdraw";
                    result = "Withdraw £" + amount + "?\nPress Enter to confirm\nPress CLR to cancel";
                } else {
                    executeWithdraw(amount);
                }
            }
            else{
                message = "Invalid Amount";
                result = "Now enter the amount\nThen press transaction\n(Deposit, Withdraw, or Transfer)";
            }
            numberPadInput = "";
        }
        else {
            reset("You are not logged in");
        }
        update();
    }

    private void executeWithdraw(int amount) {
        if (bank.withdraw(amount)) {
            message = "Withdraw Successful";
            result = "Withdrawn: £" + amount;
            if (bank.isLoggedInLowBalance()) {
                result += "\n⚠ Low balance warning!";
            }
            bank.saveToFile();
        } else {
            message = "Withdraw Failed: Insufficient Funds";
            result = "Now enter the amount\nThen press transaction\n(Deposit, Withdraw, or Transfer)";
        }
        setState(STATE_LOGGED_IN);
    }

    // Handle the Deposit button:
    // - If the user is logged in, deposit the amount entered into the bank
    // - Reads the amount from numberPadInput, validates it, and updates messages/results accordingly
    // - Otherwise, reset the ATM and display an error message
    public void processDeposit() {
        if (state.equals(STATE_LOGGED_IN)) {
            int amount = parseValidAmount(numberPadInput);
            if (amount > 0) {
                bank.deposit( amount );
                message = "Deposit Successful";
                result = "Deposited: £" + amount;
                bank.saveToFile();
            }
            else {
                message = "Invaild Amount";
                result = "Now enter the amount\nThen press transaction\n(Deposit, Withdraw, or Transfer)";
            }
            numberPadInput = "";
        }
        else {
            reset("You are not logged in");
        }
        update();
    }

    // Handle the Finish button:
    // - If the user is logged in, log out
    // - Otherwise, reset the ATM and display an error message
    public void processFinish() {
        if (state.equals(STATE_LOGGED_IN) ) {
            reset("Thank you for using the Bank ATM");
            bank.logout();
        } else {
            reset("You are not logged in");
        }
        update();
    }

    // Handle unknown or invalid buttons for the current state:
    // - Reset the ATM and display an "Invalid Command" message
    public void processUnknownKey(String action) {
        reset("Invalid Command");
        update();
    }

    // Handle the Mini Statement button:
    // - If the user is logged in, display the last 10 transactions for this account
    // - Otherwise, reset the ATM and display an error message
    public void processMiniStatement() {
        if (state.equals(STATE_LOGGED_IN)) {
            numberPadInput = "";
            message = "Mini Statement";
            result = bank.getMiniStatement();
        } else {
            reset("You are not logged in");
        }
        update();
    }

    public void processTransfer() {
        if (!state.equals(STATE_LOGGED_IN)) {
            reset("You are not logged in");
        } else {
            setState(STATE_TRANSFER_ACC);
            numberPadInput = "";
            message = "Transfer";
            result = "Enter target account number\nThen press Enter";
        }
        update();
    }

    // Notify the View of changes by calling its update method
    private void update() {
        view.update(message,numberPadInput, result);
    }
}

