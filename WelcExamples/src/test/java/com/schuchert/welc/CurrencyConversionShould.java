package com.schuchert.welc;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CurrencyConversionShould {
    @Test
    public void workWithValidConversionRequest() {
        BigDecimal result = CurrencyConversion.convertFromTo("USD", "EUR");
        assertThat(result, is(greaterThan(BigDecimal.valueOf(0.5))));
        assertThat(result, is(lessThan(BigDecimal.valueOf(2.0))));
    }

    private Map<String, String> mapOf(String... values) {
        ConcurrentHashMap<String, String> result = new ConcurrentHashMap<>();
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
            result = "<div id=\"converter_results\"><ul><li><strong>1 x = 42 Y</strong>";
        }};

        BigDecimal result = CurrencyConversion.convertFromTo("USD", "EUR");
        assertThat(result, is(BigDecimal.valueOf(42)));
    }

}
