package org.scassandra;

import org.junit.Test;
import org.scassandra.http.client.ActivityClient;
import org.scassandra.http.client.PrimingClient;
import org.scassandra.http.client.PrimingRequest;

import java.util.Collections;

public class IntegrationTest {

    private static int binaryPort = 2345;
    private static int adminPort = 3456;

    @Test
    public void clientsShouldBeAbleToConnect() {
        //given
        Scassandra server = ScassandraFactory.createServer(binaryPort, adminPort);
        //when
        server.start();
        ActivityClient ac = new ActivityClient("localhost", adminPort);
        PrimingClient pc = new PrimingClient("localhost", adminPort);

        //then
        ac.clearConnections();
        pc.prime(new PrimingRequest("", Collections.emptyList()));

        server.stop();
    }
}