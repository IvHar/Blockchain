import com.ivhar.models.Block;
import com.ivhar.models.Transaction;
import com.ivhar.services.Blockchain;
import com.ivhar.services.FileService;
import com.ivhar.services.TransactionService;
import com.ivhar.threads.Miner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class BlockchainTest {
    private Blockchain blockchain;
    private FileService fileService;
    private TransactionService transactionService;

    @BeforeEach
    void beforeEach() {
        fileService = mock(FileService.class);
        transactionService = mock(TransactionService.class);
        blockchain = new Blockchain(fileService, transactionService);
    }

    @Test
    void testRegisterMiner() throws NoSuchAlgorithmException {
        Miner miner = new Miner(1, blockchain);
        blockchain.registerMiner(miner);
        assertTrue(blockchain.getMiners().contains(miner));
    }

    @Test
    void testDifficultyIncrease() {
        blockchain.setDifficulty(0);
        assertEquals(1, blockchain.getZeros());

        blockchain.setDifficulty(15);
        assertEquals(1, blockchain.getZeros());

        blockchain.setDifficulty(65);
        assertEquals(0, blockchain.getZeros());
    }

    @Test
    void testAddLoadedBlock() {
        Block block1 = new Block(1, "0");
        block1.setHash("12345");
        Block block2 = new Block(1, block1.getHash());
        block2.setHash("12346");
        when(transactionService.validateTransactions(block2)).thenReturn(true);

        blockchain.addLoadedBlock(block1);
        blockchain.addLoadedBlock(block2);
        assertTrue(blockchain.getBlocks().contains(block1));
        assertTrue(blockchain.getBlocks().contains(block2));
    }

    @Test
    void testAddBlockValid() {
        Block block1 = new Block(1, "0");
        block1.setHash("12345");
        Block block2 = new Block(1, block1.getHash());
        block2.setHash("12346");
        when(transactionService.validateTransactions(block2)).thenReturn(true);

        blockchain.addBlock(block1);
        blockchain.addBlock(block2);
        verify(fileService, times(2)).saveBlock(any(Block.class), anyInt());
        verify(transactionService, times(2)).clearTransactions();
        assertTrue(blockchain.getBlocks().contains(block1));
        assertTrue(blockchain.getBlocks().contains(block2));
    }

    @Test
    void testAddBlockInvalid() {
        Block block = new Block(1, "1224");
        block.setHash("12346");
        blockchain.addBlock(block);
        assertFalse(blockchain.getBlocks().contains(block));
    }

    @Test
    void testLoadBlockchain() {
        blockchain.loadBlockchain();
        verify(transactionService).loadTransactions();
        verify(transactionService).loadIdentifier();
        verify(fileService).loadBlockchain(blockchain);
    }

    @Test
    void testMinerCall() throws NoSuchAlgorithmException {
        Miner miner = new Miner(1, blockchain);
        Block block1 = miner.call();
        blockchain.addLoadedBlock(block1);
        Block block2 = miner.call();
        assertNotNull(block1.getHash());
        assertEquals("0", block1.getHashPrevious());
        assertEquals(block1.getHash(), block2.getHashPrevious());
        assertEquals(1, block1.getMinedBy());
    }

    @Test
    void testChangeMinerBalance() throws NoSuchAlgorithmException {
        Miner miner = new Miner(1, blockchain);
        miner.addBalance(100.0);
        assertEquals(200.0, miner.getBalance());
        miner.reduceBalance(50.0);
        assertEquals(150.0, miner.getBalance());
    }

    @Test
    void testMinerRun() throws NoSuchAlgorithmException, InterruptedException, SignatureException, InvalidKeyException {
        Miner miner = new Miner(1, blockchain);
        miner.setTransactionService(transactionService);
        blockchain.registerMiner(miner);

        CountDownLatch latch = new CountDownLatch(1);
        Thread minerThread = new Thread(() -> {
            miner.run();
            latch.countDown();
        });

        minerThread.interrupt();
        minerThread.start();
        latch.await(1, TimeUnit.SECONDS);
        verify(transactionService, atLeastOnce()).sendTransfer(eq(miner), any(), anyDouble(), any(PrivateKey.class), any(PublicKey.class));
    }

    @Test
    void testLoadTransactionsEmpty() {
        transactionService.loadTransactions();
        assertTrue(transactionService.getTransactions().isEmpty());
    }

    @Test
    void testLoadTransactionsException() {
        doThrow(new RuntimeException(new IOException())).when(transactionService).saveTransactions();
        transactionService.loadTransactions();
        assertThrows(RuntimeException.class, transactionService::saveTransactions);
        assertTrue(transactionService.getTransactions().isEmpty());
    }

    @Test
    void testBlockToString() throws NoSuchAlgorithmException {
        Miner miner = new Miner(1, blockchain);
        Transaction transaction = new Transaction(miner, miner, 100.0, "signature", "publicKey", 1);
        Block block = new Block(1, "0");
        block.setData(List.of(transaction));
        block.setHash("12345");
        block.setMinedBy(1);
        block.setMagicNumber(100L);
        block.setTimeTaken(5L);
        String string = "Block:"
                + "\nCreated by miner1"
                + "\nminer1 gets 100 VC"
                + "\nId: 1"
                + "\nTimestamp: " + block.getTimeStamp()
                + "\nMagic number: 100"
                + "\nHash of the previous block:\n0"
                + "\nHash of the block:\n12345";
        String expected = string
                + "\nBlock data: "
                + "\nminer1 sent 100.0 VC to miner1"
                + "\nBlock was generating for 5 seconds";
        assertEquals(expected, block.toString());

        block.setData(null);
        expected = string
                + "\nBlock data: No transactions"
                + "\nBlock was generating for 5 seconds";
        assertEquals(expected, block.toString());

        block.setData(new ArrayList<>());
        assertEquals(expected, block.toString());
    }
}