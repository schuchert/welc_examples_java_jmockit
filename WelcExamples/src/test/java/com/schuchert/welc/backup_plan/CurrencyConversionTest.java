package com.schuchert.welc.backup_plan;

import com.schuchert.welc.CurrencyConversion;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertTrue;

public class CurrencyConversionTest {
   @Test
   public void canGetAConversionRate() {
      BigDecimal result = CurrencyConversion.convertFromTo("USD", "EUR");
      assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
      assertTrue(result.compareTo(new BigDecimal(5)) < 0);
   }

   @Test(expected = IllegalArgumentException.class)
   public void doesntAllowBadFromCurrency() {
      CurrencyConversion.convertFromTo("bogus", "EUR");
   }

   @Test(expected = IllegalArgumentException.class)
   public void doesntAllowBadToCurrency() {
      CurrencyConversion.convertFromTo("USD", "Bogus");
   }
}
