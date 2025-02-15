package com.ivhar.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public final class Block implements Serializable {
    private final Integer id;
    private String hash;
    private final String hashPrevious;
    private Long magicNumber;
    private final Long timeStamp;
    private Long timeTaken;
    private Integer minedBy;
    private List<Transaction> data;

    public Block(Integer id, String hashPrevious) {
        this.id = id;
        this.hashPrevious = hashPrevious;
        timeStamp = new Date().getTime();
    }

    @Override
    public String toString() {
        return "Block:"
                + "\nCreated by miner" + minedBy
                + "\nminer" + minedBy + " gets 100 VC"
                + "\nId: " + id
                + "\nTimestamp: " + timeStamp
                + "\nMagic number: " + magicNumber
                + "\nHash of the previous block:\n" + hashPrevious
                + "\nHash of the block:\n" + hash
                + "\nBlock data: " + toStringData()
                + "\nBlock was generating for " + timeTaken + " seconds";
    }

    private String toStringData() {
        if (data == null || data.isEmpty()) {
            return "No transactions";
        }
        return data.stream()
                .map(transaction ->
                        "\nminer" + transaction.getSender().getId()
                                + " sent " + transaction.getTransferAmount()
                                + " VC to miner" + transaction.getReceiver().getId())
                .reduce((t1, t2) -> t1 + t2).orElse("");
    }

    public Integer getId() {
        return id;
    }

    public String getHash() {
        return hash;
    }

    public String getHashPrevious() {
        return hashPrevious;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setMagicNumber(Long magicNumber) {
        this.magicNumber = magicNumber;
    }

    public Long getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(Long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public Integer getMinedBy() {
        return minedBy;
    }

    public void setMinedBy(Integer minedBy) {
        this.minedBy = minedBy;
    }

    public List<Transaction> getData() {
        return data;
    }

    public void setData(List<Transaction> data) {
        this.data = data;
    }
}