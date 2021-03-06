/*
 * Copyright (C) 2014 Christopher Batey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scassandra.http.client;

import org.junit.Test;
import nl.jqno.equalsverifier.EqualsVerifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PrimingRequestTest {
    @Test
    public void throwsIllegalStateExceptionIfVariablesTypesSetForQueryPrime() {
        //given
        //when
        try {
            PrimingRequest.queryBuilder()
                    .withVariableTypes(ColumnTypes.Bigint)
                    .build();
            fail("Expected illegal state exception");
        } catch (IllegalStateException e) {
            //then
            assertEquals(e.getMessage(), "Variable types only applicable for a prepared statement prime. Not a query prime.");
        }
    }

    @Test
    public void throwsIllegalStateExceptionIfNoQuerySpecified() {
        //given
        //when
        try {
            PrimingRequest.queryBuilder()
                    .build();
            fail("Expected illegal state exception");
        } catch (IllegalStateException e) {
            //then
            assertEquals(e.getMessage(), "Must set query for PrimingRequest.");
        }
    }

    @Test
    public void testEqualsContract() {
        EqualsVerifier.forClass(PrimingRequest.class).verify();
    }
}
