package com.txstats.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Vinod Kandula
 */

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class UnProcessableEntityException extends RuntimeException {

}
