package com.schuchert.welc;

import mockit.Expectations;
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
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(JMockit.class)
public class CurrencyConversionShould {
    @Test
    public void workWithValidConversionRequest() {
        BigDecimal result = CurrencyConversion.convertFromTo("USD", "EUR");
        assertThat(result, is(greaterThan(BigDecimal.valueOf(0.5))));
        assertThat(result, is(lessThan(BigDecimal.valueOf(2.0))));
    }

    private Map<String, String> mapOf(String... values) {
        ConcurrentHashMap<String, String> result = new ConcurrentHashMap<String, String>();
        for (String current : values) {
            result.put(current, current);
        }
        return result;
    }

    @Test
    public void justTestParsing(
            @Mocked final Request request,
            @Mocked final Response response,
            @Mocked final Content content) throws Exception {
        new Expectations(CurrencyConversion.class) {{
            CurrencyConversion.currencySymbols();
            result = mapOf("USD", "EUR");
        }};

        new NonStrictExpectations() {{
            Request.Get(anyString);
            result = request;
            request.execute();
            result = response;
            response.returnContent();
            result = content;
            content.asString();
            result = "<h1 class=\"page-title\">EUR to USD Exchange Rate <span id=\"currency-rate\">1 EUR = 42 USD</span>";
        }};

        BigDecimal result = CurrencyConversion.convertFromTo("USD", "EUR");
        assertThat(result, is(BigDecimal.valueOf(42)));
    }

}
