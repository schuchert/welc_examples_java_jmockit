package com.schuchert.welc;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DealPathFinder {
	private String baseCurrency;

	public DealPathFinder startingWith(String currency) {
		this.baseCurrency = currency;
		return this;
	}

	LinkedList<LinkedList<Conversion>> findDealsOfLengthLessThan(
			int maxPathLength) {
		Map<String, String> currencySymbols = CurrencyConversion.currencySymbols();
		List<Conversion> allPairs = allPairs(baseCurrency, currencySymbols.keySet());
		Collection<String> allCurrenciesWithConversion = currenciesWithConversions(allPairs);
		allCurrenciesWithConversion.remove(baseCurrency);

		for (String current : allCurrenciesWithConversion) {
			List<String> allMinusCurrent = new LinkedList<String>(
					allCurrenciesWithConversion);
			allMinusCurrent.remove(current);
			allPairs.addAll(new DealPathFinder().startingWith(current)
					.allPairs(current, allMinusCurrent));
		}

		return findDealsLessThan(maxPathLength, allPairs);
	}

	private LinkedList<LinkedList<Conversion>> findDealsLessThan(
			int maxPathLength, List<Conversion> allPairs) {
		LinkedList<LinkedList<Conversion>> result = new LinkedList<LinkedList<Conversion>>();
		return result;
	}

	List<Conversion> allPairs(String baseCurrency,
			Collection<String> set) {
		List<Conversion> allPairs = new LinkedList<Conversion>();
		for (String current : set) {
			conditionallyAddNewConversion(allPairs, current, baseCurrency);
			conditionallyAddNewConversion(allPairs, baseCurrency, current);
		}
		return allPairs;
	}

	Set<String> currenciesWithConversions(List<Conversion> conversions) {
		Set<String> result = new HashSet<String>();

		for (Conversion current : conversions) {
			result.add(current.from);
			result.add(current.to);
		}

		return result;
	}

	void conditionallyAddNewConversion(List<Conversion> allPairs,
			String fromCurrency, String toCurrency) {
		BigDecimal factor = CurrencyConversion.convertFromTo(toCurrency,
				fromCurrency);
		if (factor.compareTo(BigDecimal.ZERO) != 0) {
			allPairs.add(new Conversion(baseCurrency, fromCurrency, factor));
		}
	}
}
