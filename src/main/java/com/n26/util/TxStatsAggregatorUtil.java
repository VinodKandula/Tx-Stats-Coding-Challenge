package com.n26.util;

import com.n26.common.config.ConfigConstants;
import com.n26.model.Transaction;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.function.BinaryOperator;

/**
 * @author Vinod Kandula
 *
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(of = {"sum", "count", "timestamp"})
@Component
public class TxStatsAggregatorUtil {

    public static final TxStatsAggregatorUtil EMPTY = new TxStatsAggregatorUtil();

    @Getter
    private BigDecimal sum = BigDecimal.ZERO;

    @Getter
    private long count = 0L;

    @Getter
    private BigDecimal max = BigDecimal.ZERO;

    @Getter
    private BigDecimal min = BigDecimal.ZERO;

    @Getter
    private long timestamp = 0L;

    private  static ConfigConstants configConstants;

    @Autowired
    private ConfigConstants configConstants0;

    @PostConstruct
    private void initStaticConfig () {
        configConstants = this.configConstants0;
    }


    public static final BinaryOperator<TxStatsAggregatorUtil> reducer = (prev, next) -> {
        if (prev == null)
            prev = TxStatsAggregatorUtil.EMPTY;
        if (next == null)
            return prev;

        TxStatsAggregatorUtil txStatsAggregator = new TxStatsAggregatorUtil();
        txStatsAggregator.sum = prev.getSum().add(next.getSum());
        txStatsAggregator.count = prev.getCount() + next.getCount();
        txStatsAggregator.max = prev.getMax().compareTo(next.getMax()) == 1 ? prev.getMax() : next.getMax();
        txStatsAggregator.min = (prev.getCount() == 0 || prev.getMin().compareTo(next.getMin()) == 1) ? next.getMin() : prev.getMin();
        txStatsAggregator.timestamp = prev.getTimestamp() < next.getTimestamp() ? prev.getTimestamp() : next.getTimestamp();

        return txStatsAggregator;
    };

    public static TxStatsAggregatorUtil createOrUpdateTxStats(TxStatsAggregatorUtil prev, Transaction tx) {
        TxStatsAggregatorUtil txStatsAggregator = new TxStatsAggregatorUtil();
        long now = Instant.now().toEpochMilli();

        if (prev == null || prev == TxStatsAggregatorUtil.EMPTY || (tx.getTimestamp() - prev.timestamp >= configConstants.getTxTimeIntervalMillis()) ) {
            txStatsAggregator.timestamp = getRange(tx.getTimestamp())[0];
            txStatsAggregator.sum = tx.getAmount();
            txStatsAggregator.count = 1;
            txStatsAggregator.max = tx.getAmount();
            txStatsAggregator.min = tx.getAmount();
        } else {
            txStatsAggregator.timestamp = prev.timestamp;
            txStatsAggregator.sum = prev.sum.add(tx.getAmount());
            txStatsAggregator.count = prev.count + 1;
            txStatsAggregator.max = tx.getAmount().compareTo(prev.max) == 1 ? tx.getAmount() : prev.max;
            txStatsAggregator.min = (prev.getCount() == 0 || prev.getMin().compareTo(tx.getAmount()) == 1) ? tx.getAmount() : prev.getMin();
        }

        return txStatsAggregator;
    }

    public BigDecimal getAvg() {
        return (count == 0) ? BigDecimal.ZERO : sum.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
    }

    public static int getIndex(long millis) {
        return Math.toIntExact((millis / configConstants.getTxTimeIntervalMillis()) % configConstants.getTxMaxTimeMillis());
    }

    public static long[] getRange(long millis) {
        long offset = millis % configConstants.getTxTimeIntervalMillis();
        return new long[] {millis - offset, millis + (configConstants.getTxTimeIntervalMillis() - offset - 1)};
    }

}
