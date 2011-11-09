package com.schuchert.welc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class CurrencyConversion {
	static List<String> allCurrenciesCache;
	static long lastCacheRead = Long.MAX_VALUE;

	public static Map<String, BigDecimal> allConversions() {
		List<String> allSymbols = CurrencyConversion.currencySymbols();
		Map<String, BigDecimal> conversions = new ConcurrentHashMap<String, BigDecimal>();
		for (String outerSymbol : allSymbols)
			for (String innerSymbol : allSymbols) {
				BigDecimal conversion = null;
				try {
					conversion = CurrencyConversion.convertFromTo(outerSymbol,
							innerSymbol);
				} catch (RuntimeException e) {
					conversion = BigDecimal.ZERO;
				}
				conversions.put(
						String.format("%s-%s", outerSymbol, innerSymbol),
						conversion);
			}
		return conversions;
	}

	public static List<String> currencySymbols() {
		if (allCurrenciesCache != null
				&& System.currentTimeMillis() - lastCacheRead < 5 * 60 * 1000) {
			return allCurrenciesCache;
		}

		LinkedList<String> result = new LinkedList<String>();
		String url = "http://www.jhall.demon.co.uk/currency/by_currency.html";
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
						if (l.matches("\\s+<td valign=top>[A-Z]{3}</td>")) {
							result.add(l.replaceAll(".*top>", "").replaceAll(
									"</td>", ""));
						}
					}
					if (l.startsWith("<h3>Currency Data"))
						foundTable = true;
					else
						continue;
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		allCurrenciesCache = result;
		lastCacheRead = System.currentTimeMillis();

		return result;
	}

	public static BigDecimal convertFromTo(String fromCurrency,
			String toCurrency) {
		List<String> valid = currencySymbols();
		if (!valid.contains(fromCurrency))
			throw new IllegalArgumentException(String.format(
					"Invalid from currency: %s", fromCurrency));
		if (!valid.contains(toCurrency))
			throw new IllegalArgumentException(String.format(
					"Invalid to currency: %s", toCurrency));
		String url = String
				.format("http://www.gocurrency.com/v2/dorate.php?inV=1&from=%s&to=%s&Calculate=Convert",
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
