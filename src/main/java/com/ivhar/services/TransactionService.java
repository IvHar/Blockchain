package com.ivhar.services;

import com.ivhar.models.Block;
import com.ivhar.models.Transaction;
import com.ivhar.threads.Miner;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class TransactionService implements Serializable {
    private static final TransactionService transactionService = new TransactionService();
    private TransactionService() { }
    public static TransactionService getTransactionService() {
        return transactionService;
    }

    private Integer uniqueIdentifier = 0;
    private static List<Transaction> transactions = new ArrayList<>();
    private static final String TRANSACTIONS_FILE = "transactions.dat";
    private static final String IDENTIFIER_FILE = "identifier.dat";

    public synchronized List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public static void setTransactions(List<Transaction> transactions) {
        TransactionService.transactions = transactions;
    }

    public Integer getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(Integer uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public void clearTransactions() {
        transactions.clear();
    }

    public synchronized void sendTransfer(Miner from, Miner to, Double amount,
                                          PrivateKey privateKey, PublicKey publicKey) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        int identifier = ++uniqueIdentifier;
        String signature = signTransaction(String.valueOf(amount + identifier), privateKey);
        String stringPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        transactions.add(new Transaction(from, to, amount, signature, stringPublicKey, identifier));
        commit(from, to, amount);
        saveTransactions();
        saveIdentifier();
    }

    private void commit(Miner from, Miner to, Double amount) {
        from.reduceBalance(amount);
        to.addBalance(amount);
    }

    private String signTransaction(String text, PrivateKey privateKey) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(text.getBytes());
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    public boolean validateTransactions(Block block) {
        int identifier = 0;
        for (Transaction transaction : block.getData()) {
            if (transaction.getIdentifier() <= identifier) {
                return false;
            }
            identifier = transaction.getIdentifier();
            try {
                PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(
                        new X509EncodedKeySpec(Base64.getDecoder().decode(transaction.getPublicKey()))
                );
                if (!verifyTransaction(String.valueOf(transaction.getTransferAmount() + identifier),
                        transaction.getSignature(), publicKey)) {
                    return false;
                }
            } catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    private boolean verifyTransaction(String text, String signature, PublicKey publicKey) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initVerify(publicKey);
        sign.update(text.getBytes());
        return sign.verify(Base64.getDecoder().decode(signature));
    }

    public void loadTransactions() {
        File transactionsFile = new File(getTransactionsFile());
        if (transactionsFile.exists() && transactionsFile.length() > 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(transactionsFile))) {
                clearTransactions();
                transactions.addAll((ArrayList<Transaction>) ois.readObject());
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("No previous transactions found or they didn't managed to load (most likely).");
            }
        }
    }

    public synchronized void saveTransactions() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getTransactionsFile()))) {
            oos.writeObject(transactions);
        } catch (IOException e) {
            System.err.println("Error while saving transactions: " + e.getMessage());
        }
    }

    public void loadIdentifier() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getIdentifierFile()))) {
            uniqueIdentifier = (Integer) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No previous identifier found.");
        }
    }

    public void saveIdentifier() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getIdentifierFile()))) {
            oos.writeObject(uniqueIdentifier);
        } catch (IOException e) {
            System.err.println("Error while saving identifier: " + e.getMessage());
        }
    }

    public String getTransactionsFile() {
        return TRANSACTIONS_FILE;
    }

    public String getIdentifierFile() {
        return IDENTIFIER_FILE;
    }

}