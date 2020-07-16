package com.txstats.service;

import com.txstats.aspect.EnableStatistics;
import com.txstats.common.config.ConfigConstants;
import com.txstats.common.exception.StaleTransactionException;
import com.txstats.common.exception.UnProcessableEntityException;
import com.txstats.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * @author Vinod Kandula
 */

@Service
@EnableStatistics
public class TransactionServiceImpl implements TransactionService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigConstants configConstants;

    @Override
    public void createTransaction(Transaction tx) throws StaleTransactionException, UnProcessableEntityException {
        createTransactionAtTimestamp(tx, Instant.now());
    }

    protected void createTransactionAtTimestamp(Transaction tx, Instant instant)
            throws StaleTransactionException, UnProcessableEntityException {

        if (tx.getTimestamp() - instant.toEpochMilli() > 0) {
            logger.error("Transaction time is in the feature");
            throw new UnProcessableEntityException();
        }

        if (tx.getTimestamp() - instant.minusMillis(configConstants.getTxMaxTimeMillis()).toEpochMilli() < 0) {
            logger.error("Transaction time is older than "+configConstants.getTxMaxTimeMillis() +" ms");
            throw new StaleTransactionException();
        }

        // EnableStatisticsAspect does the registering it and used for statistical computations

        // Spring Data save transaction
        // Produce Tx Created event using Kafka Producer (spring cloud stream)
        // and compute the Tx Statistics using Kafka streams for production scale
    }

    @Override
    public void deleteTransactions() {
        // EnableStatisticsAspect does the cleanup

        // Spring Data delete transactions (for production usage)
        // Produce Tx deleted event using Kafka Producer (spring cloud stream)
    }
}
