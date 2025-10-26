package com.example.movieticket.controller;

import com.example.movieticket.model.*;
import com.example.movieticket.service.DataService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class PaymentDialogController implements Initializable {

    @FXML private Label movieLabel;
    @FXML private Label seatsLabel;
    @FXML private Label totalAmountLabel;

    @FXML private ToggleGroup paymentMethodGroup;
    @FXML private RadioButton creditCardRadio;
    @FXML private RadioButton debitCardRadio;
    @FXML private RadioButton mobileBankingRadio;

    @FXML private VBox creditCardDetails;
    @FXML private TextField creditCardNumberField;
    @FXML private TextField creditCardExpiryField;
    @FXML private TextField creditCardCvvField;
    @FXML private TextField creditCardHolderField;

    @FXML private VBox debitCardDetails;
    @FXML private TextField debitCardNumberField;
    @FXML private TextField debitCardExpiryField;
    @FXML private TextField debitCardCvvField;
    @FXML private TextField debitCardHolderField;
    @FXML private TextField debitCardPinField;

    @FXML private VBox mobileBankingDetails;
    @FXML private ToggleGroup mobileBankingProviderGroup;
    @FXML private RadioButton bkashRadio;
    @FXML private RadioButton nagadRadio;
    @FXML private RadioButton rocketRadio;
    @FXML private TextField mobileNumberField;

    @FXML private Button payButton;
    @FXML private Button cancelButton;

    private DataService dataService = DataService.getInstance();
    private Stage dialogStage;
    private boolean paymentCompleted = false;
    private List<String> seatNumbers;
    private double totalAmount;
    private String movieTitle;
    private Payment completedPayment;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupPaymentMethodListeners();
    }

    public void setBookingDetails(String movieTitle, List<String> seatNumbers, double totalAmount) {
        this.movieTitle = movieTitle;
        this.seatNumbers = seatNumbers;
        this.totalAmount = totalAmount;

        movieLabel.setText(movieTitle);
        seatsLabel.setText(String.join(", ", seatNumbers));
        totalAmountLabel.setText(String.format("$%.2f", totalAmount));
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isPaymentCompleted() {
        return paymentCompleted;
    }

    public Payment getCompletedPayment() {
        return completedPayment;
    }

    private void setupPaymentMethodListeners() {
        // Show/hide card details based on selection
        creditCardRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            creditCardDetails.setVisible(newVal);
            creditCardDetails.setManaged(newVal);
        });

        debitCardRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            debitCardDetails.setVisible(newVal);
            debitCardDetails.setManaged(newVal);
        });

        mobileBankingRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            mobileBankingDetails.setVisible(newVal);
            mobileBankingDetails.setManaged(newVal);
        });
    }

    @FXML
    private void handlePayment() {
        // Validate payment method selection
        if (paymentMethodGroup.getSelectedToggle() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a payment method.");
            return;
        }

        Payment payment = new Payment();
        payment.setAmount(totalAmount);

        // Determine payment method and validate
        if (creditCardRadio.isSelected()) {
            if (!validateCreditCard()) return;
            payment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
            payment.setCardLastFourDigits(creditCardNumberField.getText().substring(12));
        } else if (debitCardRadio.isSelected()) {
            if (!validateDebitCard()) return;
            payment.setPaymentMethod(Payment.PaymentMethod.DEBIT_CARD);
            payment.setCardLastFourDigits(debitCardNumberField.getText().substring(12));
        } else if (mobileBankingRadio.isSelected()) {
            if (!validateMobileBanking()) return;
            payment.setPaymentMethod(Payment.PaymentMethod.MOBILE_BANKING);
            payment.setMobileNumber(mobileNumberField.getText());

            // Get selected mobile banking provider
            if (bkashRadio.isSelected()) {
                payment.setMobileBankingProvider(Payment.MobileBankingProvider.BKASH);
            } else if (nagadRadio.isSelected()) {
                payment.setMobileBankingProvider(Payment.MobileBankingProvider.NAGAD);
            } else if (rocketRadio.isSelected()) {
                payment.setMobileBankingProvider(Payment.MobileBankingProvider.ROCKET);
            }
        }

        // Process payment
        boolean success = processPayment(payment);

        if (success) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            paymentCompleted = true;
            completedPayment = payment;
            showAlert(Alert.AlertType.INFORMATION, "Payment Successful",
                     "Your payment has been processed successfully!\nTransaction ID: " + payment.getTransactionId());

            if (dialogStage != null) {
                dialogStage.close();
            }
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            showAlert(Alert.AlertType.ERROR, "Payment Failed",
                     "Payment processing failed. Please try again or use a different payment method.");
        }
    }

    private boolean validateCreditCard() {
        if (creditCardNumberField.getText().isEmpty() || creditCardNumberField.getText().length() != 16) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid 16-digit card number.");
            return false;
        }
        if (creditCardExpiryField.getText().isEmpty() || !creditCardExpiryField.getText().matches("\\d{2}/\\d{2}")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter expiry date in MM/YY format.");
            return false;
        }
        if (creditCardCvvField.getText().isEmpty() || creditCardCvvField.getText().length() != 3) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid 3-digit CVV.");
            return false;
        }
        if (creditCardHolderField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter card holder name.");
            return false;
        }
        return true;
    }

    private boolean validateDebitCard() {
        if (debitCardNumberField.getText().isEmpty() || debitCardNumberField.getText().length() != 16) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid 16-digit card number.");
            return false;
        }
        if (debitCardExpiryField.getText().isEmpty() || !debitCardExpiryField.getText().matches("\\d{2}/\\d{2}")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter expiry date in MM/YY format.");
            return false;
        }
        if (debitCardCvvField.getText().isEmpty() || debitCardCvvField.getText().length() != 3) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid 3-digit CVV.");
            return false;
        }
        if (debitCardHolderField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter card holder name.");
            return false;
        }
        if (debitCardPinField.getText().isEmpty() || debitCardPinField.getText().length() != 4) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid 4-digit PIN.");
            return false;
        }
        return true;
    }

    private boolean validateMobileBanking() {
        if (mobileBankingProviderGroup.getSelectedToggle() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a mobile banking provider.");
            return false;
        }
        if (mobileNumberField.getText().isEmpty() || !mobileNumberField.getText().matches("01\\d{9}")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid mobile number (01XXXXXXXXX).");
            return false;
        }
        return true;
    }

    private boolean processPayment(Payment payment) {
        // Simulate payment processing
        try {
            // Generate transaction ID
            String transactionId = generateTransactionId(payment.getPaymentMethod());
            payment.setTransactionId(transactionId);

            // Note: We don't save payment to database yet because we don't have booking_id
            // The payment will be saved after booking is created in SeatSelectionController

            return true; // Payment processing successful
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String generateTransactionId(Payment.PaymentMethod method) {
        String prefix = "";
        switch (method) {
            case CREDIT_CARD:
                prefix = "CC";
                break;
            case DEBIT_CARD:
                prefix = "DC";
                break;
            case MOBILE_BANKING:
                prefix = "MB";
                break;
        }
        Random random = new Random();
        long number = 10000000000000L + (long)(random.nextDouble() * 90000000000000L);
        return prefix + number;
    }

    @FXML
    private void handleCancel() {
        paymentCompleted = false;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
