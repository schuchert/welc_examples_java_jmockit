package com.schuchert.welc;

public class PropertyUtil {
	static String getNamed(String name, String defaultValue) {
		String candidate = System.getProperty(name);
		return candidate != null ? candidate : defaultValue;
	}
	
	static int getIntNamed(String name, int defaultValue) {
		String candidate = System.getProperty(name);
		if(candidate != null)
			return Integer.parseInt(candidate);
		return defaultValue;
	}
}
