import com.ivhar.models.Transaction;
import com.ivhar.services.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class TransactionFilesTest {
    private TransactionService transactionService;
    private static final String TEMP_TRANSACTIONS_FILE = "temp_transactions.dat";
    private static final String TEMP_IDENTIFIER_FILE = "temp_identifier.dat";

    @BeforeEach
    void beforeEach() {
        transactionService = spy(TransactionService.getTransactionService());
        doReturn(TEMP_TRANSACTIONS_FILE).when(transactionService).getTransactionsFile();
        doReturn(TEMP_IDENTIFIER_FILE).when(transactionService).getIdentifierFile();
        new File(TEMP_TRANSACTIONS_FILE).delete();
        new File(TEMP_IDENTIFIER_FILE).delete();
    }

    @AfterEach
    void afterEach() {
        transactionService.setUniqueIdentifier(0);
        transactionService.clearTransactions();
    }

    @Test
    void testLoadTransactionsEmpty() {
        transactionService.loadTransactions();
        assertTrue(transactionService.getTransactions().isEmpty());
    }

    @Test
    void testSaveAndLoadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(null, null, 100.0, "signature", "publicKey", 1));
        transactions.add(new Transaction(null, null, 100.0, "signature", "publicKey", 1));

        TransactionService.setTransactions(transactions);
        transactionService.saveTransactions();
        transactionService.clearTransactions();
        transactionService.loadTransactions();
        assertEquals(transactions, transactionService.getTransactions());
    }

    @Test
    void testSaveAndLoadIdentifier() {
        transactionService.setUniqueIdentifier(42);
        transactionService.saveIdentifier();
        transactionService.setUniqueIdentifier(0);
        transactionService.loadIdentifier();
        assertEquals(42, transactionService.getUniqueIdentifier());
    }

    @Test
    void testLoadIdentifierEmpty() {
        transactionService.loadIdentifier();
        assertEquals(0, transactionService.getUniqueIdentifier());
    }

}