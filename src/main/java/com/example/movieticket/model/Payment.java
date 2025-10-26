package com.example.movieticket.model;

import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private int bookingId;
    private int userId;
    private PaymentMethod paymentMethod;
    private MobileBankingProvider mobileBankingProvider;
    private double amount;
    private LocalDateTime paymentDate;
    private PaymentStatus status;
    private String transactionId;
    private String cardLastFourDigits; // For credit/debit cards
    private String mobileNumber; // For mobile banking

    public enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, MOBILE_BANKING
    }

    public enum MobileBankingProvider {
        BKASH, NAGAD, ROCKET, NONE
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }

    public Payment() {
        this.mobileBankingProvider = MobileBankingProvider.NONE;
        this.status = PaymentStatus.PENDING;
    }

    public Payment(int bookingId, PaymentMethod paymentMethod, double amount) {
        this.bookingId = bookingId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.paymentDate = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
        this.mobileBankingProvider = MobileBankingProvider.NONE;
    }

    // Getters and Setters
    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public MobileBankingProvider getMobileBankingProvider() {
        return mobileBankingProvider;
    }

    public void setMobileBankingProvider(MobileBankingProvider mobileBankingProvider) {
        this.mobileBankingProvider = mobileBankingProvider;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCardLastFourDigits() {
        return cardLastFourDigits;
    }

    public void setCardLastFourDigits(String cardLastFourDigits) {
        this.cardLastFourDigits = cardLastFourDigits;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getPaymentMethodDisplay() {
        if (paymentMethod == PaymentMethod.MOBILE_BANKING && mobileBankingProvider != MobileBankingProvider.NONE) {
            return "Mobile Banking (" + mobileBankingProvider + ")";
        }
        return paymentMethod.toString().replace("_", " ");
    }

    @Override
    public String toString() {
        return "Payment #" + paymentId + " - " + getPaymentMethodDisplay() + " - $" + amount;
    }
}
