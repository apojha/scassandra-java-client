package org.scassandra.http.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

public class ActivityClientTest {
    private static final int PORT = 1235;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    private ActivityClient underTest;

    @Before
    public void setup() {
        underTest = new ActivityClient("localhost", PORT);
    }

    @Test
    public void testRetrievalOfZeroQueries() {
        //given
        stubFor(get(urlEqualTo("/query")).willReturn(aResponse().withBody("[]")));
        //when
        List<Query> queries = underTest.retrieveQueries();
        //then
        assertEquals(0, queries.size());
    }

    @Test
    public void testRetrievalOfASingleQuery() {
        //given
        stubFor(get(urlEqualTo("/query")).willReturn(aResponse().withBody("[{\"query\":\"select * from people\"}]")));
        //when
        List<Query> queries = underTest.retrieveQueries();
        //then
        assertEquals(1, queries.size());
        assertEquals("select * from people", queries.get(0).getQuery());
    }

    @Test(expected = ActivityRequestFailed.class)
    public void testErrorDuringRetrieval() {
        //given
        stubFor(get(urlEqualTo("/query"))
                .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));
                //when
        underTest.retrieveQueries();
        //then

    }
}