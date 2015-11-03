package com.schuchert.welc.backup_plan;

import com.schuchert.welc.CurrencyConversion;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;

@RunWith(JMockit.class)
public class CurrencyConversionTestDoubledTest {
   private Map<String, String> mapFrom(String... keyValuePairs) {
      Map<String, String> result = new ConcurrentHashMap<String, String>();
      for (String keyValuePair : keyValuePairs) result.put(keyValuePair, keyValuePair);
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
   public void convertsCorreclty(@Mocked final Request request, @Mocked final Response response, @Mocked final Content content) throws Exception {
      final String stubbedResult = "<div id=\"converter_results\"><ul><li><strong>5 x = 42 Y</strong>";

      new NonStrictExpectations() {
         {
            Request.Get(anyString);
            result = request;
            request.execute();
            result = response;
            response.returnContent();
            result = content;
            content.asString();
            result = stubbedResult;
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
