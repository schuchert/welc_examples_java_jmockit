package com.schuchert.welc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class CurrencyConversion {
   static Map<String, String> allCurrenciesCache;
   static long lastCacheRead = Long.MAX_VALUE;

   public static Map<String, BigDecimal> allConversions() {
      Map<String, String> allSymbols = CurrencyConversion.currencySymbols();
      Map<String, BigDecimal> conversions 
          = new ConcurrentHashMap<String, BigDecimal>();
      for (String outerSymbol : allSymbols.keySet())
         for (String innerSymbol : allSymbols.keySet()) {
            BigDecimal conversion = null;
            try {
               conversion = CurrencyConversion.convertFromTo(outerSymbol,
                     innerSymbol);
            } catch (RuntimeException e) {
               conversion = BigDecimal.ZERO;
            }
            conversions.put(String.format("%s-%s", outerSymbol, innerSymbol),
                  conversion);
         }
      return conversions;
   }

   public static Map<String, String> currencySymbols() {
      if (allCurrenciesCache != null
            && System.currentTimeMillis() - lastCacheRead < 5 * 60 * 1000) {
         return allCurrenciesCache;
      }

      Map<String, String> symbolToName = new ConcurrentHashMap<String, String>();
      String url = "http://www.xe.com/iso4217.php";
      try {
         HttpClient httpclient = new DefaultHttpClient();
         HttpGet httpget = new HttpGet(url);
         HttpResponse response = httpclient.execute(httpget);
         HttpEntity entity = response.getEntity();
         if (entity != null) {
            InputStream instream = entity.getContent();
            InputStreamReader irs = new InputStreamReader(instream);
            BufferedReader br = new BufferedReader(irs);
            String l;
            boolean foundTable = false;
            while ((l = br.readLine()) != null) {
               if (foundTable) {
                  Pattern symbol = Pattern.compile("href=\"/currency/"
                        + "[^>]+>(...)</a></td><td class=\"[^\"]+\">"
                        + "([A-Za-z ]+)");
                  Matcher m = symbol.matcher(l);
                  while (m.find()) {
                     symbolToName.put(m.group(1), m.group(2));
                  }
               }
               if (l.indexOf("currencyTable") >= 0)
                  foundTable = true;
               else
                  continue;
            }
         }

      } catch (Exception e) {
         throw new RuntimeException(e);
      }

      allCurrenciesCache = symbolToName;
      lastCacheRead = System.currentTimeMillis();

      return symbolToName;
   }

   public static BigDecimal convertFromTo(String fromCurrency,
         String toCurrency) {
      Map<String, String> symbolToName = currencySymbols();
      if (!symbolToName.containsKey(fromCurrency))
         throw new IllegalArgumentException(String.format(
               "Invalid from currency: %s", fromCurrency));
      if (!symbolToName.containsKey(toCurrency))
         throw new IllegalArgumentException(String.format(
               "Invalid to currency: %s", toCurrency));
      String url = String
            .format("http://www.gocurrency.com/v2/dorate.php?inV=1&from=%s&to" +
            		"=%s&Calculate=Convert",
                  toCurrency, fromCurrency);
      try {
         HttpClient httpclient = new DefaultHttpClient();
         HttpGet httpget = new HttpGet(url);
         HttpResponse response = httpclient.execute(httpget);
         HttpEntity entity = response.getEntity();
         StringBuffer result = new StringBuffer();
         if (entity != null) {
            InputStream instream = entity.getContent();
            InputStreamReader irs = new InputStreamReader(instream);
            BufferedReader br = new BufferedReader(irs);
            String l;
            while ((l = br.readLine()) != null) {
               result.append(l);
            }
         }
         String theWholeThing = result.toString();
         int start = theWholeThing
               .lastIndexOf("<div id=\"converter_results\"><ul><li>");
         String substring = result.substring(start);
         int startOfInterestingStuff = substring.indexOf("<b>") + 3;
         int endOfIntererestingStuff = substring.indexOf("</b>",
               startOfInterestingStuff);
         String interestingStuff = substring.substring(
               startOfInterestingStuff, endOfIntererestingStuff);
         String[] parts = interestingStuff.split("=");
         String value = parts[1].trim().split(" ")[0];
         BigDecimal bottom = new BigDecimal(value);
         return bottom;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }
}
