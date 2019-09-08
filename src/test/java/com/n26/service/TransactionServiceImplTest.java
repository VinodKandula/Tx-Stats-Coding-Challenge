package com.n26.service;

import com.n26.common.config.ConfigConstants;
import com.n26.common.exception.StaleTransactionException;
import com.n26.common.exception.UnProcessableEntityException;
import com.n26.model.Transaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Vinod Kandula
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionServiceImplTest {

    @Autowired
    TransactionService transactionService;

    @Autowired
    private ConfigConstants configConstants;

    @Test
    public void createTransaction_CheckStaleAndNotStaleTransactions() throws Exception {
        TransactionServiceImpl impl = (TransactionServiceImpl) transactionService;

        Instant now = Instant.now();
        Instant notStale = now.minusMillis(configConstants.getTxMaxTimeMillis()-10);
        Instant stale = now.minusMillis(configConstants.getTxMaxTimeMillis()).minusMillis(1);

        // expect no exception
        Transaction tx = new Transaction(BigDecimal.ONE, notStale.toEpochMilli());
        transactionService.createTransaction(tx);

        // expect exception
        Transaction tx2 = new Transaction(BigDecimal.ONE, stale.toEpochMilli());
        assertThatExceptionOfType(StaleTransactionException.class)
                .isThrownBy(() -> ((TransactionServiceImpl) transactionService).createTransactionAtTimestamp(tx2, now));
    }

    @Test
    public void createTransaction_TestFutureTransaction() throws Exception {
        Instant now = Instant.now();
        Instant future = now.plusMillis(1);

        // expect no exception
        Transaction tx = new Transaction(BigDecimal.ONE, now.toEpochMilli());
        transactionService.createTransaction(tx);

        // expect exception
        Transaction tx2 = new Transaction(BigDecimal.ONE, future.toEpochMilli());
        assertThatExceptionOfType(UnProcessableEntityException.class)
                .isThrownBy(() -> ((TransactionServiceImpl) transactionService).createTransactionAtTimestamp(tx2, now));
    }
}