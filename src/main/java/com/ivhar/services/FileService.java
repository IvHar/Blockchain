package com.ivhar.services;

import com.ivhar.models.Block;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public final class FileService {
    private static final FileService fileService = new FileService();
    private FileService() { }
    public static FileService getService() {
        return fileService;
    }

    private static final String DIRECTORY = "blockchain_blocks";
    private static final String METADATA_FILE = "blockchain_metadata.ser";

    public String getDIRECTORY() {
        return DIRECTORY;
    }

    public String getMetadataFile() {
        return METADATA_FILE;
    }

    public void loadBlockchain(Blockchain blockchain) {
        try {
            Path dir = Paths.get(getDIRECTORY());
            if (Files.exists(dir)) {
                Files.list(dir).sorted(Comparator.comparing(path -> path.getFileName().toString())).
                        forEach(path -> {
                            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
                                Block block = (Block) ois.readObject();
                                blockchain.addLoadedBlock(block);
                            } catch (IOException | ClassNotFoundException e) {
                                System.err.println("Error while loading block" + e.getMessage());
                            }
                        });
                System.out.println("Blockchain was loaded\n");
            }

            File metadata = new File(getMetadataFile());
            if (metadata.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(metadata))) {
                    blockchain.setZeros((Integer) ois.readObject());
                } catch (ClassNotFoundException e) {
                    System.err.println("Error while loading metadata: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error while loading\n");
        }
    }

    public void saveBlock(Block block, Integer zeros) {
        try {
            Path dir = Paths.get(getDIRECTORY());
            if (!Files.exists(dir)) {
                Files.createDirectory(dir);
            }

            String fileBlockName = getDIRECTORY() + "/block_" + String.format("%06d", block.getId()) + ".ser";
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileBlockName))) {
                oos.writeObject(block);
            }
            saveMetadata(zeros);
            System.out.println("Block and metadata were saved successfully");
        } catch (IOException e) {
            System.err.println("Error while saving: " + e.getMessage());
        }
    }

    private void saveMetadata(Integer zeros) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getMetadataFile()))) {
            oos.writeObject(zeros);
        } catch (IOException e) {
            System.err.println("Error while saving metadata: " + e.getMessage());
        }
    }
}