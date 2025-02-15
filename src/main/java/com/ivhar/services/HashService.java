package com.ivhar.services;

import com.ivhar.models.Block;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

public final class HashService {
    private static final HashService hashService = new HashService();
    private HashService() { }
    public static HashService getService() {
        return hashService;
    }

    private static final Random random = new Random();

    public String hashString(Block block, int zeros) {
        String initialString = block.getId() + block.getHashPrevious()
                + block.getTimeStamp() + block.getData() + block.getMinedBy();
        String targetZero = zeroString(zeros);
        String hash;
        long magicNumber;
        do {
            magicNumber = random.nextLong();
            hash = initialString + magicNumber;
            hash = applySha256(hash);
        } while (!hash.startsWith(targetZero));
        block.setMagicNumber(magicNumber);
        return hash;
    }

    private String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte elem : hash) {
                String hex = Integer.toHexString(0xff & elem);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String zeroString(int zeros) {
        return "0".repeat(zeros);
    }
}