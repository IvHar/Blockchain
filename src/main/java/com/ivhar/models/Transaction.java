package com.ivhar.models;

import com.ivhar.threads.Miner;

import java.io.Serializable;

public final class Transaction implements Serializable {
    private final Miner sender;
    private final Miner receiver;
    private final Double transferAmount;
    private String signature;
    private final String publicKey;
    private Integer identifier;

    public Transaction(Miner sender, Miner receiver, Double transferAmount,
                       String signature, String publicKey, Integer identifier) {
        this.sender = sender;
        this.receiver = receiver;
        this.transferAmount = transferAmount;
        this.signature = signature;
        this.publicKey = publicKey;
        this.identifier = identifier;
    }

    public Double getTransferAmount() {
        return transferAmount;
    }

    public Miner getSender() {
        return sender;
    }

    public Integer getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Integer identifier) {
        this.identifier = identifier;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Miner getReceiver() {
        return receiver;
    }
}