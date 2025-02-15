package com.ivhar;

import com.ivhar.models.Block;
import com.ivhar.services.Blockchain;
import com.ivhar.services.FileService;
import com.ivhar.services.TransactionService;
import com.ivhar.threads.Miner;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        FileService fileService = FileService.getService();
        TransactionService transactionService = TransactionService.getTransactionService();
        Blockchain blockchain = new Blockchain(fileService, transactionService);
        blockchain.loadBlockchain();
        ExecutorService transactionsPool = Executors.newFixedThreadPool(10);
        ExecutorService minerPool = Executors.newFixedThreadPool(10);

        List<Callable<Block>> miners = new ArrayList<>();
        for (int j = 1; j <= 10; j++) {
            try {
                Miner miner = new Miner(j, blockchain);
                blockchain.registerMiner(miner);
                miners.add(miner);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        for (Miner miner : blockchain.getMiners()) {
            transactionsPool.submit((Runnable) miner);
        }

        for (int i = 0; i < 15; i++) {
            try {
                Block block = minerPool.invokeAny(miners);
                blockchain.addBlock(block);
                System.out.println(block);
                blockchain.setDifficulty(block.getTimeTaken());
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
        }

        minerPool.shutdown();
        transactionsPool.shutdown();
    }
}