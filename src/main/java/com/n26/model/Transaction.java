package com.n26.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.n26.common.exception.UnProcessableEntityException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 * @author Vinod Kandula
 */

@AllArgsConstructor
@ToString
public class Transaction {

    @Getter
    @NonNull
    private BigDecimal amount;

    @Getter
    private long timestamp;

    public Transaction(@JsonProperty("amount") BigDecimal amount, @JsonProperty("timestamp") Date date) {
        if (Objects.isNull(amount) || Objects.isNull(date) )
            throw new UnProcessableEntityException();
        this.amount = amount;
        this.timestamp = date.getTime();
    }

}
