package com.txstats.service;

import com.txstats.common.exception.StaleTransactionException;
import com.txstats.common.exception.UnProcessableEntityException;
import com.txstats.model.Transaction;

/**
 * @author Vinod Kandula
 */

public interface TransactionService {

    public void createTransaction(Transaction tx) throws StaleTransactionException, UnProcessableEntityException;

    public void deleteTransactions();
}
