package com.schuchert.welc.backup_plan;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import mockit.NonStrictExpectations;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import com.schuchert.welc.CurrencyConversion;

public class CurrencyConversionTestDoubledTest {
   private Map<String, String> mapFrom(String... keyValuePairs) {
      Map<String, String> result = new ConcurrentHashMap<String, String>();
      for (int i = 0; i < keyValuePairs.length; ++i)
         result.put(keyValuePairs[i], keyValuePairs[i]);
      return result;
   }

   @Test(expected = IllegalArgumentException.class)
   public void willThrowExceptionWhenToCurrencyNotFound() {
      new NonStrictExpectations(CurrencyConversion.class) {
         {
            CurrencyConversion.currencySymbols();
            result = mapFrom("Foo");
         }
      };
      CurrencyConversion.convertFromTo("FOO", "BAR");
   }

   @Test(expected = IllegalArgumentException.class)
   public void willThrowExceptionWhenFromCurrencyNotFound() {
      new NonStrictExpectations(CurrencyConversion.class) {
         {
            CurrencyConversion.currencySymbols();
            result = new ConcurrentHashMap<String, String>();
         }
      };
      CurrencyConversion.convertFromTo("EUR", "EUR");
   }

   @Test
   public void convertsCorreclty() throws Exception {
      final ByteArrayInputStream bais = new ByteArrayInputStream(
            "<div id=\"converter_results\"><ul><li><b>5 x = 42 Y</b>"
                  .getBytes());

      new NonStrictExpectations() {
         DefaultHttpClient httpclient;
         HttpResponse response;
         HttpEntity entity;
         {
            httpclient.execute((HttpUriRequest) any);
            result = response;
            response.getEntity();
            result = entity;
            entity.getContent();
            result = bais;
         }
      };

      new NonStrictExpectations(CurrencyConversion.class) {
         {
            CurrencyConversion.currencySymbols();
            result = mapFrom("X", "Y");
         }
      };

      BigDecimal result = CurrencyConversion.convertFromTo("X", "Y");
      assertEquals(new BigDecimal(42), result);
   }

   @Test
   public void developAllCurrencyConversions() {
      new NonStrictExpectations(CurrencyConversion.class) {
         {
            CurrencyConversion.currencySymbols();
            result = mapFrom("A", "B", "C", "D", "E", "F", "G", "H", "I",
                  "J", "K");
            CurrencyConversion.convertFromTo(anyString, anyString);
            result = BigDecimal.ONE;
         }
      };
      Map<String, BigDecimal> conversions = CurrencyConversion
            .allConversions();
      for (Entry<String, BigDecimal> current : conversions.entrySet())
         System.out.printf("%s - %s\n", current.getKey(), current.getValue());
   }
}
