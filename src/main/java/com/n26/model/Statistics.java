package com.n26.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.n26.common.convert.BigDecimalSerializer;
import com.n26.util.TxStatsAggregatorUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author Vinod Kandula
 */

@AllArgsConstructor
@ToString
public class Statistics {

    @JsonSerialize(using = BigDecimalSerializer.class)
    @Getter
    private BigDecimal sum;

    @JsonSerialize(using = BigDecimalSerializer.class)
    @Getter
    private BigDecimal avg;

    @JsonSerialize(using = BigDecimalSerializer.class)
    @Getter
    private BigDecimal max;

    @JsonSerialize(using = BigDecimalSerializer.class)
    @Getter
    private BigDecimal min;

    @Getter
    private long count;

    public static Statistics from(TxStatsAggregatorUtil txContainer) {
        return new Statistics(txContainer.getSum(), txContainer.getAvg(), txContainer.getMax(), txContainer.getMin(), txContainer.getCount());
    }

}
