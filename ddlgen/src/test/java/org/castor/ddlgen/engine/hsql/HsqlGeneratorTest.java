/*
 * Copyright 2006 Le Duc Bao, Ralf Joachim
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.castor.ddlgen.engine.hsql;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.castor.ddlgen.DDLGenConfiguration;
import org.castor.ddlgen.KeyGeneratorRegistry;
import org.castor.ddlgen.test.framework.AbstractGeneratorTest;
import org.castor.ddlgen.test.framework.Expected;


/**
 * Hsql Generator test.
 * 
 * @author <a href="mailto:leducbao AT gmail DOT com">Le Duc Bao</a>
 * @author <a href="mailto:ralf DOT joachim AT syscon DOT eu">Ralf Joachim</a>
 * @version $Revision: 5951 $ $Date: 2006-04-25 16:09:10 -0600 (Tue, 25 Apr 2006) $
 * @since 1.1
 */
public final class HsqlGeneratorTest extends AbstractGeneratorTest {
    public HsqlGeneratorTest(final String testcase) {
        super(testcase);
    }

    public HsqlGeneratorTest(final String testcase, final boolean useDBEngine) {
        super(testcase);

        if (useDBEngine) { setEngine(Expected.ENGINE_HSQL); }
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite("All org.castor.ddlgen.engine.Hsql tests");

        // schema test
        suite.addTest(new HsqlGeneratorTest("testCreateSchema", true));

        // drop test
        suite.addTest(new HsqlGeneratorTest("testDropTable", true));
        
        // table test
        suite.addTest(new HsqlGeneratorTest("testSingleTable", false));
        suite.addTest(new HsqlGeneratorTest("testMultipleTable", false));
        suite.addTest(new HsqlGeneratorTest("testIgnoredTable", false));
        suite.addTest(new HsqlGeneratorTest("testNoTable", false));

        //field test
        suite.addTest(new HsqlGeneratorTest("testSingleField", false));
        suite.addTest(new HsqlGeneratorTest("testSingleFieldForAll", true));
        suite.addTest(new HsqlGeneratorTest("testIgnoredField", false));
        suite.addTest(new HsqlGeneratorTest("testNoField", false));
        suite.addTest(new HsqlGeneratorTest("testManyKeysReference", false));
        suite.addTest(new HsqlGeneratorTest("testManyClassKeysReference", false));
        suite.addTest(new HsqlGeneratorTest("test2LevelsReference", false));
        
        // primary key test
        suite.addTest(new HsqlGeneratorTest("testClassId", true));
        suite.addTest(new HsqlGeneratorTest("testClassMultipleId", true));
        suite.addTest(new HsqlGeneratorTest("testFieldId", true));
        suite.addTest(new HsqlGeneratorTest("testFieldMultipleId", true));
        suite.addTest(new HsqlGeneratorTest("testOverwriteFieldId", false));
        suite.addTest(new HsqlGeneratorTest("testNoId", false));

        // foreign key test
        suite.addTest(new HsqlGeneratorTest("testOneOneRelationship", false));
        suite.addTest(new HsqlGeneratorTest("testOneManyRelationship", false));
        suite.addTest(new HsqlGeneratorTest("testManyManyRelationship", false));

        // index test
        suite.addTest(new HsqlGeneratorTest("testCreateIndex", false));        
        
        // key generator test
        suite.addTest(new HsqlGeneratorTest("testKeyGenIdentity", true));
        suite.addTest(new HsqlGeneratorTest("testKeyGenHighLow", false));
        suite.addTest(new HsqlGeneratorTest("testKeyGenMax", false));
        suite.addTest(new HsqlGeneratorTest("testKeyGenSequence", true));
        suite.addTest(new HsqlGeneratorTest("testKeyGenUUID", false));

        return suite;
    }

    /** 
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        DDLGenConfiguration conf = new DDLGenConfiguration();
        conf.addProperties("org/castor/ddlgen/test/config/ddlgen.properties");
        conf.addProperties("org/castor/ddlgen/test/config/hsql.properties");
        setGenerator(new HsqlGenerator(conf));
        
        KeyGeneratorRegistry keyGenRegistry = new KeyGeneratorRegistry(conf);
        getGenerator().setKeyGenRegistry(keyGenRegistry);
        
        getGenerator().initialize();
    }

    /** 
     * {@inheritDoc}
     */
    protected void tearDown() throws Exception {
        super.tearDown();

        setGenerator(null);
    }
}
