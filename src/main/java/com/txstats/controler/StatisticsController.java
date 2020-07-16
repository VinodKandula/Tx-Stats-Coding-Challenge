package com.txstats.controler;

import com.txstats.model.Statistics;
import com.txstats.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Vinod Kandula
 */

@RestController
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/statistics")
    public Statistics statistics() {
        return statisticsService.getStatistics();
    }
}
