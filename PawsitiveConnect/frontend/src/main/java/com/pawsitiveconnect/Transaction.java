package com.pawsitiveconnect;

public class Transaction {
    private int transactionId;
    private String payerName;
    private double amount;
    private String method;
    private String status;
    private String date;
    private String category;

    // 5-parameter constructor (backward compatible)
    public Transaction(int transactionId, String payerName, double amount, String method, String status) {
        this(transactionId, payerName, amount, method, status, "Recently", "Adoption Fee");
    }

    // 7-parameter constructor used by the reworked controller
    public Transaction(int transactionId, String payerName, double amount, String method, String status, String date, String category) {
        this.transactionId = transactionId;
        this.payerName = payerName;
        this.amount = amount;
        this.method = method;
        this.status = status != null ? status : "COMPLETED";
        this.date = date != null ? date : "Recently";
        this.category = category != null ? category : "Adoption Fee";
    }

    public int getTransactionId() { return transactionId; }
    public String getPayerName() { return payerName; }
    public double getAmount() { return amount; }
    public String getMethod() { return method; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public String getCategory() { return category; }
}