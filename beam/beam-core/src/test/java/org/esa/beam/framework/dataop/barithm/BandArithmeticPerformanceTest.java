/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.beam.framework.dataop.barithm;

import com.bc.jexp.ParseException;
import com.bc.jexp.Term;
import com.bc.jexp.impl.DefaultNamespace;
import com.bc.jexp.impl.ParserImpl;
import junit.framework.TestCase;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.ProductData;

import java.io.IOException;

public class BandArithmeticPerformanceTest extends TestCase {

    private static final int MAX_NUM_TEST_LOOPS = 10000000;
    private static final int MIN_NUM_OPS_PER_SECOND = 2500000;

    public BandArithmeticPerformanceTest(String s) {
        super(s);
    }

    public void testThatPerformanceIsSufficient() throws ParseException, IOException {
        final Band flags = new Band("flags", ProductData.TYPE_INT8, 1, 1);
        final SingleFlagSymbol s1 = new SingleFlagSymbol("flags.WATER", flags, 0x01);
        final SingleFlagSymbol s2 = new SingleFlagSymbol("flags.LAND", flags, 0x02);
        final SingleFlagSymbol s3 = new SingleFlagSymbol("flags.CLOUD", flags, 0x04);
        final int[] dataElems = new int[]{-1};
        s1.setData(dataElems);
        s2.setData(dataElems);
        s3.setData(dataElems);
        final DefaultNamespace namespace = new DefaultNamespace();
        namespace.registerSymbol(s1);
        namespace.registerSymbol(s2);
        namespace.registerSymbol(s3);
        final String code = "(flags.WATER OR flags.LAND) AND NOT flags.CLOUD";
        final Term term = new ParserImpl(namespace, true).parse(code);

        final RasterDataEvalEnv evalEnv = new RasterDataEvalEnv(0, 0, 1, 1);

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < MAX_NUM_TEST_LOOPS; i++) {
        }
        long t2 = System.currentTimeMillis();
        for (int i = 0; i < MAX_NUM_TEST_LOOPS; i++) {
            term.evalI(evalEnv);
        }
        long t3 = System.currentTimeMillis();
        long dt = (t3 - t2) - (t2 - t1);
        long numOps = Math.round(MAX_NUM_TEST_LOOPS * (1000.0 / dt));

        // System.out.println("numOps = " + numOps);
        assertTrue(String.format("Low evaluation performance detected (%d ops/s for term \"%s\"): Term implementation change?", numOps, code),
                   numOps > MIN_NUM_OPS_PER_SECOND);
    }

}
