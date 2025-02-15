package com.ivhar.threads;

import com.ivhar.models.Block;
import com.ivhar.services.Blockchain;
import com.ivhar.services.HashService;
import com.ivhar.services.TransactionService;

import java.io.Serializable;
import java.security.*;
import java.util.Random;
import java.util.concurrent.Callable;

public final class Miner implements Callable<Block>, Runnable, Serializable {
    private final Integer id;
    private Double virtualCoinsBalance = 100.0;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final Blockchain blockchain;
    private TransactionService transactionService = TransactionService.getTransactionService();
    public Miner(Integer id, Blockchain blockchain) throws NoSuchAlgorithmException {
        this.id = id;
        this.blockchain = blockchain;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
    }

    public Integer getId() {
        return id;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public Double getBalance() {
        return virtualCoinsBalance;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Block call() {
        Block block;
        synchronized (blockchain) {
            int id = blockchain.getBlocks().size() + 1;
            String hashPrevious = blockchain.getBlocks().isEmpty() ? "0"
                    : blockchain.getBlocks().get(blockchain.getBlocks().size() - 1).getHash();
            block = new Block(id, hashPrevious);
        }

        block.setData(transactionService.getTransactions());
        long start = System.currentTimeMillis();
        String hash = HashService.getService().hashString(block, blockchain.getZeros());
        block.setHash(hash);
        block.setMinedBy(this.id);
        long end = System.currentTimeMillis();
        block.setTimeTaken((end - start) / 1000);
        reward();
        return block;
    }

    @Override
    public void run() {
        Random random = new Random();
        while (true) {
            try {
                transactionService.sendTransfer(this, blockchain.getMiners()
                                .get(random.nextInt(blockchain.getMiners().size())),
                        random.nextDouble(virtualCoinsBalance * 0.80), privateKey, publicKey);
                Thread.sleep(1000 + id * 10);
            } catch (InterruptedException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                System.out.println("Thread was interrupted");
            }
        }
    }

    private void reward() {
        virtualCoinsBalance += 100;
    }

    public void addBalance(Double currencyAmount) {
        virtualCoinsBalance += currencyAmount;
    }
    public void reduceBalance(Double currencyAmount) {
        virtualCoinsBalance -= currencyAmount;
    }
}