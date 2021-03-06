package com.txstats.common.convert;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @author Vinod Kandula
 */

public class BigDecimalSerializer extends JsonSerializer<BigDecimal> {
 
	private static final DecimalFormat df = new DecimalFormat("0.00"); 
  
    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider arg2) throws IOException, JsonProcessingException {
    	gen.writeString(df.format(value.setScale(2, BigDecimal.ROUND_HALF_UP)));
    }
}
