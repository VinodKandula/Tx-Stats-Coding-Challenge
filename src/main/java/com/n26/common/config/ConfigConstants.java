package com.n26.common.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Vinod Kandula
 */

@Configuration
@Getter
public class ConfigConstants {

    @Value("${transactions.max.time.millis:60000}")
    private int txMaxTimeMillis;

    @Value("${transactions.time-interval.millis:10}")
    private int txTimeIntervalMillis;

}
