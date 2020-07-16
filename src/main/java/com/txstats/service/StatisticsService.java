package com.txstats.service;

import com.txstats.model.Statistics;
import com.txstats.model.Transaction;

/**
 * @author Vinod Kandula
 */
public interface StatisticsService {

    public void registerTransaction(Transaction tx);

    public Statistics getStatistics();

    public void clearStatistics();
}
