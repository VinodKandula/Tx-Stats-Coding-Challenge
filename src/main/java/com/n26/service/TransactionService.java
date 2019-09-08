package com.n26.service;

import com.n26.common.exception.StaleTransactionException;
import com.n26.common.exception.UnProcessableEntityException;
import com.n26.model.Transaction;

/**
 * @author Vinod Kandula
 */

public interface TransactionService {

    public void createTransaction(Transaction tx) throws StaleTransactionException, UnProcessableEntityException;

    public void deleteTransactions();
}
