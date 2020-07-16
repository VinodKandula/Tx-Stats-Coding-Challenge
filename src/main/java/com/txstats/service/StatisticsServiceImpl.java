package com.txstats.service;

import com.txstats.common.config.ConfigConstants;
import com.txstats.model.Statistics;
import com.txstats.model.Transaction;
import com.txstats.util.TxStatsAggregatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.IntStream;

/**
 * @author Vinod Kandula
 */

@Service
public class StatisticsServiceImpl implements StatisticsService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AtomicReferenceArray<TxStatsAggregatorUtil> txStatsAggregatorUtil;

    @Autowired
    private ConfigConstants configConstants;

    @PostConstruct
    private void postConstruct() {
        if (configConstants.getTxMaxTimeMillis() <= 0 || configConstants.getTxTimeIntervalMillis() <= 0)
            throw new IllegalArgumentException("Configuration is missing valid positive values for transactions.stale.after.millis or transactions.time-interval.millis");

        logger.info(String.format("Initializing Tx Stats Aggregator intervel of %s ms", configConstants.getTxTimeIntervalMillis()));
        logger.info(String.format("Initializing Tx Stats Aggregator size of %s", configConstants.getTxMaxTimeMillis()));
        initialize();
    }

    private void initialize() {
        //txStatsAggregatorUtil = new AtomicReferenceArray<>(TxStatsAggregatorUtil.COUNT);
        txStatsAggregatorUtil = new AtomicReferenceArray<>(configConstants.getTxMaxTimeMillis());
    }

    @Override
    public void registerTransaction(Transaction tx) {
        int index = TxStatsAggregatorUtil.getIndex(tx.getTimestamp());
        txStatsAggregatorUtil.getAndUpdate(index, prev -> TxStatsAggregatorUtil.createOrUpdateTxStats(prev, tx));
    }

    @Override
    public Statistics getStatistics() {
        return getStatistics(Instant.now().toEpochMilli());
    }

    protected Statistics getStatistics(long now) {
        TxStatsAggregatorUtil txContainer = IntStream.range(0, configConstants.getTxMaxTimeMillis())
                .mapToObj(txStatsAggregatorUtil::get)
                .filter(Objects::nonNull)
                .filter(tx -> now - tx.getTimestamp() < configConstants.getTxMaxTimeMillis())
                .reduce(TxStatsAggregatorUtil.EMPTY, TxStatsAggregatorUtil.reducer);

        return Statistics.from(txContainer);
    }

    @Override
    public synchronized void clearStatistics() {
        logger.info("Re-Initializing the Tx Containers");
        initialize(); //To clear the Statistics
    }
}
