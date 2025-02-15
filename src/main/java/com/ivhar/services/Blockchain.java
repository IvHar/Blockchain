package com.ivhar.services;

import com.ivhar.models.Block;
import com.ivhar.threads.Miner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class Blockchain implements Serializable {
    private final transient FileService fileService;
    private final TransactionService transactionService;
    private final List<Miner> miners = new ArrayList<>();
    private final List<Block> blocks = new LinkedList<>();
    private Integer zeros = 0;

    public Blockchain(FileService fileService, TransactionService transactionService) {
        this.fileService = fileService;
        this.transactionService = transactionService;
    }

    public List<Block> getBlocks() {
        return blocks;
    }
    public synchronized Integer getZeros() {
        return zeros;
    }
    public void setZeros(Integer zeros) {
        this.zeros = zeros;
    }
    public synchronized List<Miner> getMiners() {
        return miners;
    }

    public void loadBlockchain() {
        transactionService.loadTransactions();
        transactionService.loadIdentifier();
        fileService.loadBlockchain(this);
    }

    public synchronized void addLoadedBlock(Block block) {
        if (validate(block)) {
            blocks.add(block);
        }
    }

    public synchronized void addBlock(Block block) {
        if (validate(block)) {
            blocks.add(block);
            fileService.saveBlock(block, getZeros());
            transactionService.clearTransactions();
        }
    }

    public synchronized void registerMiner(Miner miner) {
        miners.add(miner);
    }

    public synchronized void setDifficulty(long timeTaken) {
        if (timeTaken < 1) {
            zeros++;
            System.out.println("N was increased to " + zeros);
        } else if (timeTaken > 60) {
            zeros--;
            System.out.println("N was decreased to " + zeros);
        } else {
            System.out.println("N stays the same");
        }
        System.out.println();
    }

    private synchronized boolean validate(Block block) {
        if (blocks.isEmpty()) {
            return block.getHashPrevious().equals("0")
                    && block.getHash().startsWith("0".repeat(zeros));
        } else {
            return block.getHashPrevious().equals(blocks.get(blocks.size() - 1).getHash())
                    && block.getHash().startsWith("0".repeat(zeros))
                    && transactionService.validateTransactions(block);
        }
    }
}