package org.scassandra.http.client;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class PrimingRequestTest {

    @Test
    public void testEqualsContract() {
        EqualsVerifier.forClass(PrimingRequest.class).verify();
    }
}
