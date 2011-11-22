package com.schuchert.welc;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Ignore;
import org.junit.Test;

public class CurrencyConversionTest {
	@Test
	@Ignore
	public void displayAllConversionRates() {
		Map<String, BigDecimal> conversions = CurrencyConversion.allConversions();
		for (Entry<String, BigDecimal> current : conversions.entrySet())
			System.out
					.printf("%s - %s\n", current.getKey(), current.getValue());
	}

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
