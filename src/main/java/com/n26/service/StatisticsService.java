package com.n26.service;

import com.n26.model.Statistics;
import com.n26.model.Transaction;

/**
 * @author Vinod Kandula
 */
public interface StatisticsService {

    public void registerTransaction(Transaction tx);

    public Statistics getStatistics();

    public void clearStatistics();
}
