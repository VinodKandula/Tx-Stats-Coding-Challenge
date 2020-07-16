package com.txstats.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Vinod Kandula
 */

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NO_CONTENT)
public class StaleTransactionException extends Exception {

}
