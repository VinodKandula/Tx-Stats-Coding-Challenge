package com.txstats.controler;

import com.txstats.common.exception.StaleTransactionException;
import com.txstats.common.exception.UnProcessableEntityException;
import com.txstats.model.Transaction;
import com.txstats.service.TransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Vinod Kandula
 */

@RunWith(SpringRunner.class)
@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired MockMvc mvc;
    @MockBean TransactionService service;

    private static final String messageBody="{\"amount\":\"%s\",\"timestamp\":\"%s\"}";

    private String toJson(Transaction tx) {
        return String.format(messageBody, tx.getAmount(), Instant.ofEpochMilli(tx.getTimestamp()).toString());
    }

    @Test
    public void testDeleteTransactions() throws Exception{

        mvc.perform(delete("/transactions")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        verify(service, times(1)).deleteTransactions();

    }

    @Test
    public void testCreateTransactionCreated() throws Exception{
        // happy tx
        Transaction tx = new Transaction(BigDecimal.ONE, Instant.now().toEpochMilli());

        mvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(toJson(tx)))
                .andExpect(status().is(HttpStatus.CREATED.value()));

        verify(service, times(1)).createTransaction(any(Transaction.class));
    }

    @Test
    public void testCreateTransactionNoContent() throws Exception{
        // stale tx
        Transaction tx = new Transaction(BigDecimal.ONE, 0L);

        doThrow(new StaleTransactionException()).when(service).createTransaction(any(Transaction.class));

        mvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(toJson(tx)))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        verify(service, times(1)).createTransaction(any(Transaction.class));
    }

    @Test
    public void testCreateTransactionUnprocessableEntity() throws Exception{
        // a tx in future
        Transaction tx = new Transaction(BigDecimal.ONE, Instant.now().plusSeconds(60).toEpochMilli());
        doThrow(new UnProcessableEntityException()).when(service).createTransaction(Mockito.any(Transaction.class));

        mvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(toJson(tx)))
                .andExpect(status().is(HttpStatus.UNPROCESSABLE_ENTITY.value()));

        verify(service, times(1)).createTransaction(any(Transaction.class));
    }

    @Test
    public void testCreateTransactionInvalidJsonFormat() throws Exception{

        // Invalid amount
        String body = String.format(messageBody, "Hello", Instant.now().toString());

        mvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(body))
                .andExpect(status().is(HttpStatus.UNPROCESSABLE_ENTITY.value()));

        verify(service, times(0)).createTransaction(any(Transaction.class));

        // Invalid datetimeformat
        String dateString = Instant.now().toString().replaceFirst(":", "!");
        body = String.format(messageBody, "2.34", dateString);

        mvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(body))
                .andExpect(status().is(HttpStatus.UNPROCESSABLE_ENTITY.value()));

        verify(service, times(0)).createTransaction(any(Transaction.class));

        // bad json
        body = "{\"amount\":,\"timestamp\":\"%s\"}";

        mvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(body))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        verify(service, times(0)).createTransaction(any(Transaction.class));
    }
}