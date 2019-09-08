package com.n26.aspect;

import com.n26.model.Transaction;
import com.n26.service.StatisticsService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Vinod Kandula
 */
@Component
@Aspect
public class EnableStatisticsAspect {

    @Pointcut("within(@com.n26.aspect.EnableStatistics *)")
    public void classAnnotated() {

    }

    @Autowired
    private StatisticsService statisticsService;

    @Around("classAnnotated()")
    public Object registerTransactions(ProceedingJoinPoint pjp) throws Throwable{

        Object result = pjp.proceed();

        String methodName = pjp.getSignature().getName();

        if ("createTransaction".equals(methodName)) {
            statisticsService.registerTransaction((Transaction) pjp.getArgs()[0]);
        } else if ("deleteTransactions".equals(methodName)) {
            statisticsService.clearStatistics();
        }

        return result;
    }


}
