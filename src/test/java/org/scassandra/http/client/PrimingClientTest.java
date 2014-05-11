package org.scassandra.http.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.*;

public class PrimingClientTest {

    private static final int PORT = 1234;
    public static final String PRIME_PREPARED_PATH = "/prime-prepared-single";
    public static final String PRIME_QUERY_PATH = "/prime-query-single";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    private PrimingClient underTest;

    @Before
    public void setup() {
        underTest = new PrimingClient("localhost", PORT);
    }


    @Test
    public void testPrimingQueryEmptyResults() {
        //given
        stubFor(post(urlEqualTo(PRIME_QUERY_PATH)).willReturn(aResponse().withStatus(200)));
        PrimingRequest pr = PrimingRequest.queryBuilder()
                .withQuery("select * from people")
                .withRows(Collections.<Map<String, ?>>emptyList())
                .build();
        //when
        underTest.primeQuery(pr);
        //then
        verify(postRequestedFor(urlEqualTo(PRIME_QUERY_PATH))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
                .withRequestBody(equalTo("{\"when\":{\"query\":\"select * from people\"},\"then\":{\"rows\":[],\"result\":\"success\"}}")));
    }

    @Test
    public void testPrimingQueryWithMultipleRows() {
        //given
        stubFor(post(urlEqualTo(PRIME_QUERY_PATH)).willReturn(aResponse().withStatus(200)));
        List<Map<String,? extends Object>> rows = new ArrayList<Map<String,? extends Object>>();
        Map<String, String> row = new HashMap<String, String>();
        row.put("name","Chris");
        rows.add(row);
        PrimingRequest pr = PrimingRequest.queryBuilder()
                .withQuery("select * from people")
                .withRows(rows)
                .build();
        //when
        underTest.primeQuery(pr);
        //then
        verify(postRequestedFor(urlEqualTo(PRIME_QUERY_PATH))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
                .withRequestBody(equalTo("{\"when\":{\"query\":\"select * from people\"},\"then\":{\"rows\":[{\"name\":\"Chris\"}],\"result\":\"success\"}}")));
    }

    @Test
    public void testPrimingQueryReadRequestTimeout() {
        //given
        stubFor(post(urlEqualTo(PRIME_QUERY_PATH)).willReturn(aResponse().withStatus(200)));
        PrimingRequest pr = PrimingRequest.queryBuilder()
                .withQuery("select * from people")
                .withResult(PrimingRequest.Result.read_request_timeout)
                .build();
        //when
        underTest.primeQuery(pr);
        //then
        verify(postRequestedFor(urlEqualTo(PRIME_QUERY_PATH))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
                .withRequestBody(equalTo("{\"when\":{\"query\":\"select * from people\"},\"then\":{\"result\":\"read_request_timeout\"}}")));
    }

    @Test
    public void testPrimingQueryUnavailableException() {
        //given
        stubFor(post(urlEqualTo(PRIME_QUERY_PATH)).willReturn(aResponse().withStatus(200)));
        PrimingClient pc = new PrimingClient("localhost", PORT);
        PrimingRequest pr = PrimingRequest.queryBuilder()
                .withQuery("select * from people")
                .withResult(PrimingRequest.Result.unavailable)
                .build();
        //when
        pc.primeQuery(pr);
        //then
        verify(postRequestedFor(urlEqualTo(PRIME_QUERY_PATH))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
                .withRequestBody(equalTo("{\"when\":{\"query\":\"select * from people\"},\"then\":{\"result\":\"unavailable\"}}")));
    }

    @Test
    public void testPrimingQueryWriteRequestTimeout() {
        //given
        stubFor(post(urlEqualTo(PRIME_QUERY_PATH)).willReturn(aResponse().withStatus(200)));
        PrimingRequest pr = PrimingRequest.queryBuilder()
                .withQuery("select * from people")
                .withResult(PrimingRequest.Result.write_request_timeout)
                .build();
        //when
        underTest.primeQuery(pr);
        //then
        verify(postRequestedFor(urlEqualTo(PRIME_QUERY_PATH))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
                .withRequestBody(equalTo("{\"when\":{\"query\":\"select * from people\"},\"then\":{\"result\":\"write_request_timeout\"}}")));
    }

    @Test(expected = PrimeFailedException.class)
    public void testPrimeQueryFailed() {
        //given
        stubFor(post(urlEqualTo(PRIME_QUERY_PATH)).willReturn(aResponse().withStatus(500)));
        PrimingRequest pr = PrimingRequest.queryBuilder()
                .withQuery("select * from people")
                .withResult(PrimingRequest.Result.read_request_timeout)
                .build();
        //when
        underTest.primeQuery(pr);
        //then
    }

    @Test
    public void testPrimingQueryConsistency() {
        //given
        stubFor(post(urlEqualTo(PRIME_QUERY_PATH)).willReturn(aResponse().withStatus(200)));
        PrimingRequest pr = PrimingRequest.queryBuilder()
                .withQuery("select * from people")
                .withConsistency(PrimingRequest.Consistency.ALL, PrimingRequest.Consistency.ONE)
                .build();

        //when
        underTest.primeQuery(pr);

        //then
        verify(postRequestedFor(urlEqualTo(PRIME_QUERY_PATH))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
                .withRequestBody(equalTo("{\"when\":{\"query\":\"select * from people\",\"consistency\":[\"ALL\",\"ONE\"]},\"then\":{\"rows\":[],\"result\":\"success\"}}")));

    }

    @Test
    public void testRetrieveOfPreviousQueryPrimes() {
        //given
        Map<String, Object> rows = new HashMap<String, Object>();
        rows.put("name","Chris");
        PrimingRequest pr = PrimingRequest.queryBuilder()
                .withQuery("select * from people")
                .withRows(rows)
                .build();
        stubFor(get(urlEqualTo(PRIME_QUERY_PATH)).willReturn(aResponse().withStatus(200).withBody(
                "[{\n" +
                        "  \"when\": {\n" +
                        "    \"query\": \"select * from people\"\n" +
                        "  },\n" +
                        "  \"then\": {\n" +
                        "    \"rows\": [{\n" +
                        "      \"name\": \"Chris\"\n" +
                        "    }],\n" +
                        "    \"result\":\"success\""+
                        "  }\n" +
                        "}]"
        )));
        //when
        List<PrimingRequest> primingRequests = underTest.retrievePrimes();
        //then
        assertEquals(1, primingRequests.size());
        assertEquals(pr, primingRequests.get(0));
    }

    @Test
    public void testDeletingOfQueryPrimes() {
        //given
        stubFor(delete(urlEqualTo(PRIME_QUERY_PATH)).willReturn(aResponse().withStatus(200)));
        //when
        underTest.clearPrimes();
        //then
        verify(deleteRequestedFor(urlEqualTo(PRIME_QUERY_PATH)));
    }

    @Test(expected = PrimeFailedException.class)
    public void testDeletingOfQueryPrimesFailedDueToStatusCode() {
        //given
        stubFor(delete(urlEqualTo(PRIME_QUERY_PATH)).willReturn(aResponse().withStatus(500)));
        //when
        underTest.clearPrimes();
        //then
    }
    @Test(expected = PrimeFailedException.class)
    public void testRetrievingOfQueryPrimesFailedDueToStatusCode() {
        //given
        stubFor(get(urlEqualTo(PRIME_QUERY_PATH)).willReturn(aResponse().withStatus(500)));
        //when
        underTest.retrievePrimes();
        //then
    }

    @Test(expected = PrimeFailedException.class)
    public void testDeletingOfQueryPrimesFailed() {
        //given
        stubFor(delete(urlEqualTo(PRIME_QUERY_PATH)).willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));
        //when
        underTest.clearPrimes();
        //then
    }
    @Test(expected = PrimeFailedException.class)
    public void testRetrievingOfQueryPrimesFailed() {
        //given
        stubFor(get(urlEqualTo(PRIME_QUERY_PATH))
                .willReturn(aResponse()
                .withFault(Fault.RANDOM_DATA_THEN_CLOSE)));
        //when
        underTest.retrievePrimes();
        //then
    }

    @Test
    public void testPrimingQueryWithSets() {
        //given
        stubFor(post(urlEqualTo(PRIME_QUERY_PATH)).willReturn(aResponse().withStatus(200)));
        List<Map<String,? extends Object>> rows = new ArrayList<Map<String,? extends Object>>();
        Map<String, Object> row = new HashMap<String, Object>();
        List<String> set = Arrays.asList("one", "two", "three");
        row.put("set",set);
        rows.add(row);
        PrimingRequest pr = PrimingRequest.queryBuilder()
                .withQuery("select * from people")
                .withRows(rows)
                .build();
        //when
        underTest.primeQuery(pr);
        //then
        verify(postRequestedFor(urlEqualTo(PRIME_QUERY_PATH))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
                .withRequestBody(equalTo("{\"when\":{\"query\":\"select * from people\"}," +
                        "\"then\":{" +
                        "\"rows\":[" +
                        "{\"set\":[\"one\",\"two\",\"three\"]}]," +
                        "\"result\":\"success\"}}")));

    }

    @Test
    public void testPrimingQueryWithCustomTypes() {
        //given
        stubFor(post(urlEqualTo(PRIME_QUERY_PATH)).willReturn(aResponse().withStatus(200)));
        Map<String, ColumnTypes> types = ImmutableMap.of("set", ColumnTypes.Set);
        List<Map<String,? extends Object>> rows = new ArrayList<Map<String, ? extends Object>>();
        Map<String, Object> row = new HashMap<String, Object>();
        Set<String> set = Sets.newHashSet("one", "two", "three");
        row.put("set",set);
        rows.add(row);
        PrimingRequest pr = PrimingRequest.queryBuilder()
                .withQuery("select * from people")
                .withRows(rows)
                .withColumnTypes(types)
                .build();
        //when
        underTest.primeQuery(pr);

        //then
        verify(postRequestedFor(urlEqualTo(PRIME_QUERY_PATH))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
                .withRequestBody(equalToJson("{\"when\":{\"query\":\"select * from people\"}," +
                        "\"then\":{" +
                        "\"rows\":[" +
                        "{\"set\":[\"two\",\"three\",\"one\"]}]," +
                        "\"result\":\"success\"" +
                        ",\"column_types\":{\"set\":\"Set\"}}}")));
    }

    @Test
    public void testPrimingPreparedStatementWithJustQueryText() {
        //given
        stubFor(post(urlEqualTo(PRIME_PREPARED_PATH)).willReturn(aResponse().withStatus(200)));
        PrimingRequest primingRequest = PrimingRequest.preparedStatementBuilder()
                .withQuery("select * from people where people = ?")
                .build();

        //when
        underTest.primePreparedStatement(primingRequest);

        //then
        verify(postRequestedFor(urlEqualTo(PRIME_PREPARED_PATH))
                .withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
                .withRequestBody(equalToJson("{\n" +
                        "   \"when\": { \n" +
                        "     \"query\" :\"select * from people where people = ?\"\n" +
                        "   },\n" +
                        "   \"then\": { \n" +
                        "     \"rows\" :[], \n" +
                        "     \"result\":\"success\" " +
                        "   }\n" +
                        " }")));
    }

    @Test(expected = PrimeFailedException.class)
    public void testPrimingPreparedStatementFailureDueToStatusCode() {
        //given
        stubFor(post(urlEqualTo(PRIME_PREPARED_PATH))
                .willReturn(aResponse().withStatus(500)));
        //when
        underTest.primePreparedStatement(PrimingRequest.preparedStatementBuilder().build());
        //then
    }
    @Test(expected = PrimeFailedException.class)
    public void testPrimingPreparedStatementFailureDueToHttpError() {
        //given
        stubFor(post(urlEqualTo(PRIME_PREPARED_PATH))
                .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));
        //when
        underTest.primePreparedStatement(PrimingRequest.preparedStatementBuilder().build());
        //then
    }

}
