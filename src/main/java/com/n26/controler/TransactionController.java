package com.n26.controler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.n26.common.exception.StaleTransactionException;
import com.n26.common.exception.UnProcessableEntityException;
import com.n26.model.Transaction;
import com.n26.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Vinod Kandula
 */

@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transactions")
    public ResponseEntity<Void> add(@RequestBody Transaction tx) throws StaleTransactionException, UnProcessableEntityException {
        transactionService.createTransaction(tx);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/transactions")
    public ResponseEntity<Void> delete() {
        transactionService.deleteTransactions();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<Void> handleJacksonMappingError(InvalidFormatException ex) {
        return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
