package com.txstats.util;

import com.txstats.common.config.ConfigConstants;
import com.txstats.model.Transaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Vinod Kandula
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class TxStatsAggregatorUtilTest {

    @Autowired
    private ConfigConstants configConstants;

    @Test
    public void getIndex_ReturnsIndex() {
        Instant instant = Instant.now();
        long now = instant.toEpochMilli();

        int indexNow = Math.toIntExact((now / configConstants.getTxTimeIntervalMillis()) % configConstants.getTxMaxTimeMillis());

        assertEquals(indexNow, TxStatsAggregatorUtil.getIndex(now));

    }

    @Test
    public void getIndex_WithCurrentAndPreviousTimestamps_ReturnsIndex() {
        Instant instant = Instant.now();
        long now = instant.toEpochMilli();
        long previous = instant.minusMillis(configConstants.getTxTimeIntervalMillis() * configConstants.getTxMaxTimeMillis()).toEpochMilli();

        assertEquals(TxStatsAggregatorUtil.getIndex(now), TxStatsAggregatorUtil.getIndex(previous));
    }

    @Test
    public void createOrUpdateTxStatsAndreducer_CheckImmutability() {
        TxStatsAggregatorUtil prev = TxStatsAggregatorUtil.EMPTY;
        Transaction newTx = new Transaction(BigDecimal.ZERO, 0L);

        // createOrUpdateTxStats should always return a new value;
        TxStatsAggregatorUtil next = TxStatsAggregatorUtil.createOrUpdateTxStats(prev, newTx);
        assertNotEquals(prev,next);

        // since reducer scope has access to fields, you never know ;)
        TxStatsAggregatorUtil r = TxStatsAggregatorUtil.reducer.apply(prev, next);
        assertNotEquals(prev, r);
        assertNotEquals(next, r);
    }

    @Test
    public void getIndex_EdgeValues() {
        Instant instant = Instant.now();
        long now = instant.toEpochMilli();
        long spanOffset = now % configConstants.getTxTimeIntervalMillis();

        // minimum long that belongs to particular index
        long lowEdge = now - spanOffset;
        assertEquals(TxStatsAggregatorUtil.getIndex(now), TxStatsAggregatorUtil.getIndex(lowEdge));
        assertNotEquals(TxStatsAggregatorUtil.getIndex(now), TxStatsAggregatorUtil.getIndex(lowEdge - 1));

        // max long that belongs to same index
        long highEdge = now + (configConstants.getTxTimeIntervalMillis() - spanOffset - 1);
        assertEquals(TxStatsAggregatorUtil.getIndex(now), TxStatsAggregatorUtil.getIndex(highEdge));
        assertNotEquals(TxStatsAggregatorUtil.getIndex(now), TxStatsAggregatorUtil.getIndex(highEdge + 1));
    }

    @Test
    public void createOrUpdateTxStats_WhenPreviousTxStatsNull_ReturnsNextTxStatsNotNull() {
        long now = Instant.now().toEpochMilli();

        Transaction tx = new Transaction(BigDecimal.ONE, now);

        TxStatsAggregatorUtil next = TxStatsAggregatorUtil.createOrUpdateTxStats(null, tx);
        assertNotNull(next);
        assertEquals(tx.getAmount(), next.getSum());
        assertEquals(1, next.getCount());
        assertEquals(tx.getAmount(), next.getMax());
        assertEquals(tx.getAmount(), next.getMin());
    }

    @Test
    public void reducer_CheckStats() {
        long now = Instant.now().toEpochMilli();
        TxStatsAggregatorUtil p = TxStatsAggregatorUtil.createOrUpdateTxStats(TxStatsAggregatorUtil.EMPTY, new Transaction(BigDecimal.ONE, now-1));
        TxStatsAggregatorUtil q = TxStatsAggregatorUtil.createOrUpdateTxStats(TxStatsAggregatorUtil.EMPTY, new Transaction(new BigDecimal("2"), now));

        TxStatsAggregatorUtil r = TxStatsAggregatorUtil.reducer.apply(p, q);

        assertEquals(new BigDecimal("3"), r.getSum());
        assertEquals(2, r.getCount());
        assertEquals(new BigDecimal("2"), r.getMax());
        assertEquals(BigDecimal.ONE, r.getMin());
    }


    @Test
    public void reducer_WhenNullArguments_ReturnTxStatsNotNull() {
        assertEquals(TxStatsAggregatorUtil.EMPTY, TxStatsAggregatorUtil.reducer.apply(null, null));

        TxStatsAggregatorUtil tx = TxStatsAggregatorUtil.createOrUpdateTxStats(TxStatsAggregatorUtil.EMPTY, new Transaction(BigDecimal.ONE, Instant.now().toEpochMilli()));

        TxStatsAggregatorUtil r = TxStatsAggregatorUtil.reducer.apply(tx, null);
        assertNotEquals(TxStatsAggregatorUtil.EMPTY, r);

        TxStatsAggregatorUtil q = TxStatsAggregatorUtil.reducer.apply(null, tx);
        assertNotEquals(TxStatsAggregatorUtil.EMPTY, q);
    }

    @Test
    public void getAvg_CheckAvg() {
        long now = Instant.now().toEpochMilli();
        Transaction tx = new Transaction(new BigDecimal("4"), now);
        TxStatsAggregatorUtil next = TxStatsAggregatorUtil.createOrUpdateTxStats(null, tx);
        assertEquals(new BigDecimal("4").setScale(2, RoundingMode.HALF_UP), next.getAvg());

        tx = new Transaction(new BigDecimal("8"), now);
        next = TxStatsAggregatorUtil.createOrUpdateTxStats(next, tx);
        assertEquals(new BigDecimal("6").setScale(2, RoundingMode.HALF_UP), next.getAvg());

        tx = new Transaction(new BigDecimal("6"), now);
        next = TxStatsAggregatorUtil.createOrUpdateTxStats(next, tx);
        assertEquals(new BigDecimal("6").setScale(2, RoundingMode.HALF_UP), next.getAvg());
    }


}
