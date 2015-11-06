package com.schuchert.welc;

import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurrencyConversion {
    static Map<String, String> allCurrenciesCache;
    static long lastCacheRead = Long.MAX_VALUE;

    public static Map<String, BigDecimal> allConversions() {
        Map<String, String> allSymbols = CurrencyConversion.currencySymbols();
        Map<String, BigDecimal> conversions
                = new ConcurrentHashMap<String, BigDecimal>();
        for (String outerSymbol : allSymbols.keySet())
            for (String innerSymbol : allSymbols.keySet()) {
                BigDecimal conversion;
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
        String url = "http://en.wikipedia.org/wiki/ISO_4217";

        try {
            Request request = Request.Get(url);
            String proxyProperty = System.getenv("HTTP_PROXY");
            if (proxyProperty != null) {
                String proxy = proxyProperty.replace("http://", "").replaceAll(":.*", "");
                int port = Integer.parseInt(proxyProperty.replaceAll(".*:", ""));
                request.viaProxy(new HttpHost(proxy, port));
            }

            Content content = request.execute().returnContent();
            InputStreamReader irs = new InputStreamReader(content.asStream());
            BufferedReader br = new BufferedReader(irs);
            String l;
            Pattern search = Pattern.compile("<td>([A-Z]{3})<[/]td>");
            while ((l = br.readLine()) != null) {
                Matcher m = search.matcher(l);
                if (m.find()) {
                    String currencyCode = m.group(1);
                    symbolToName.put(currencyCode, currencyCode);
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
            Request request = Request.Get(url);
            String proxyProperty = System.getenv("HTTP_PROXY");
            if (proxyProperty != null) {
                String proxy = proxyProperty.replace("http://", "").replaceAll(":.*", "");
                int port = Integer.parseInt(proxyProperty.replaceAll(".*:", ""));
                request.viaProxy(new HttpHost(proxy, port));
            }
            Content content = request.execute().returnContent();
            String theWholeThing = content.asString();
            int start = theWholeThing
                    .lastIndexOf("<div id=\"converter_results\"><ul><li>");
            String substring = theWholeThing.substring(start);
            int startOfInterestingStuff = substring.indexOf("<strong>") + 8;
            int endOfIntererestingStuff = substring.indexOf("</strong>",
                    startOfInterestingStuff);
            String interestingStuff = substring.substring(
                    startOfInterestingStuff, endOfIntererestingStuff);
            String[] parts = interestingStuff.split("=");
            String value = parts[1].trim().split(" ")[0];
            return new BigDecimal(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
