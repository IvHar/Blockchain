import com.ivhar.models.Block;
import com.ivhar.services.Blockchain;
import com.ivhar.services.TransactionService;
import com.ivhar.threads.Miner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionServiceTest {
    private Blockchain blockchain;
    private TransactionService transactionService;
    Miner miner1;
    Miner miner2;

    @BeforeEach
    void beforeEach() throws NoSuchAlgorithmException {
        transactionService = spy(TransactionService.getTransactionService());
        transactionService.clearTransactions();
        doNothing().when(transactionService).saveTransactions();
        doNothing().when(transactionService).saveIdentifier();
        miner1 = spy(new Miner(1, blockchain));
        miner2 = spy(new Miner(2, blockchain));
    }

    @Test
    void testSendTransfer() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        transactionService.sendTransfer(miner1, miner2, 25.0, miner1.getPrivateKey(), miner1.getPublicKey());
        verify(miner1).reduceBalance(25.0);
        verify(miner2).addBalance(25.0);
        assertEquals(75.0, miner1.getBalance());
        assertEquals(125.0, miner2.getBalance());
    }

    @Test
    void testValidTransactions() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        transactionService.sendTransfer(miner1, miner2, 50.0, miner1.getPrivateKey(), miner1.getPublicKey());
        Block block = new Block(1, "0");
        block.setData(transactionService.getTransactions());
        assertTrue(transactionService.validateTransactions(block));
    }

    @Test
    void testInvalidTransactionsIdentifier() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        transactionService.sendTransfer(miner1, miner2, 50.0, miner1.getPrivateKey(), miner1.getPublicKey());
        Block block = new Block(1, "0");
        block.setData(transactionService.getTransactions());
        block.getData().get(0).setIdentifier(-2);
        assertFalse(transactionService.validateTransactions(block));
    }

    @Test
    void testInvalidTransactionsSignature() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        transactionService.sendTransfer(miner1, miner2, 50.0, miner1.getPrivateKey(), miner1.getPublicKey());
        Block block = new Block(1, "0");
        block.setData(transactionService.getTransactions());
        block.getData().get(0).setSignature(Base64.getEncoder().encodeToString(new byte[256]));
        assertFalse(transactionService.validateTransactions(block));
    }
}