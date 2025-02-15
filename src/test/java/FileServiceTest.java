import com.ivhar.models.Block;
import com.ivhar.services.Blockchain;
import com.ivhar.services.FileService;
import com.ivhar.services.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

public class FileServiceTest {
    private Blockchain blockchain;
    private FileService fileService;
    private static Path tempDirectory;
    private static Path tempMetadataFile;

    @BeforeEach
    void beforeEach() throws IOException {
        fileService = spy(FileService.getService());
        blockchain = new Blockchain(fileService, TransactionService.getTransactionService());

        tempDirectory = Files.createTempDirectory("temp_blockchain_blocks");
        tempMetadataFile = Files.createTempFile("temp_blockchain_metadata", ".ser");

        doReturn(tempDirectory.toString()).when(fileService).getDIRECTORY();
        doReturn(tempMetadataFile.toString()).when(fileService).getMetadataFile();

        Files.walk(tempDirectory).sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        Files.deleteIfExists(tempMetadataFile);
    }

    @Test
    void testLoadBlockchainEmpty() {
        fileService.loadBlockchain(blockchain);
        assertTrue(blockchain.getBlocks().isEmpty());
    }

    @Test
    void testSaveAndLoadBlockchain() throws IOException {
        Block block1 = new Block(1, "0");
        block1.setHash("12345");
        block1.setData(new ArrayList<>());
        Block block2 = new Block(2, "12345");
        block2.setHash("234567");
        block2.setData(new ArrayList<>());
        fileService.saveBlock(block1, 0);
        fileService.saveBlock(block2, 1);
        blockchain = new Blockchain(fileService, TransactionService.getTransactionService());

        fileService.loadBlockchain(blockchain);
        assertEquals(block1.getId(), blockchain.getBlocks().get(0).getId());
        assertEquals(block2.getId(), blockchain.getBlocks().get(1).getId());
        assertEquals(2, blockchain.getBlocks().size());
    }

    @Test
    void testSaveAndLoadMetadata() throws IOException {
        Block block = new Block(1, "0");
        block.setHash("00005");
        fileService.saveBlock(block, 4);
        blockchain = new Blockchain(fileService, TransactionService.getTransactionService());
        fileService.loadBlockchain(blockchain);
        assertEquals(4, blockchain.getZeros());
    }

    @Test
    void testGetFiles() {
        fileService = FileService.getService();
        assertEquals("blockchain_blocks", fileService.getDIRECTORY());
        assertEquals("blockchain_metadata.ser", fileService.getMetadataFile());
    }
}