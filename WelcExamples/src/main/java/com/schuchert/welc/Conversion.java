package com.schuchert.welc;

import java.math.BigDecimal;

public class Conversion {
	public final String from;
	public final String to;
	public final BigDecimal rate;

	public Conversion(String from, String to, BigDecimal rate) {
		this.from = from;
		this.to = to;
		this.rate = rate;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Conversion) {
			Conversion rhs = (Conversion) obj;
			return from.equals(rhs.from) && to.equals(rhs.to);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return from.hashCode() * to.hashCode();
	}
}
