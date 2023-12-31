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
package org.castor.ddlgen.engine.db2;

import org.castor.ddlgen.SchemaFactory;
import org.castor.ddlgen.schemaobject.DefaultForeignKey;
import org.castor.ddlgen.schemaobject.DefaultIndex;
import org.castor.ddlgen.schemaobject.DefaultSchema;
import org.castor.ddlgen.schemaobject.DefaultTable;
import org.castor.ddlgen.schemaobject.Field;
import org.castor.ddlgen.schemaobject.ForeignKey;
import org.castor.ddlgen.schemaobject.Index;
import org.castor.ddlgen.schemaobject.PrimaryKey;
import org.castor.ddlgen.schemaobject.Schema;
import org.castor.ddlgen.schemaobject.Table;

/**
 * DB2 schema factory.
 * 
 * @author <a href="mailto:leducbao AT gmail DOT com">Le Duc Bao</a>
 * @author <a href="mailto:ralf DOT joachim AT syscon DOT eu">Ralf Joachim</a>
 * @version $Revision: 5951 $ $Date: 2006-04-25 16:09:10 -0600 (Tue, 25 Apr 2006) $
 * @since 1.1
 */
public final class Db2SchemaFactory implements SchemaFactory {
    //--------------------------------------------------------------------------
    
    /**
     * {@inheritDoc}
     */
    public Schema createSchema() { return new DefaultSchema(); }

    /**
     * {@inheritDoc}
     */
    public Table createTable() { return new DefaultTable(); }

    /**
     * {@inheritDoc}
     */
    public Field createField() { return new Db2Field(); }

    /**
     * {@inheritDoc}
     */
    public ForeignKey createForeignKey() { return new DefaultForeignKey(); }

    /**
     * {@inheritDoc}
     */
    public Index createIndex() { return new DefaultIndex(); }

    /**
     * {@inheritDoc}
     */
    public PrimaryKey createPrimaryKey() { return new Db2PrimaryKey(); }

    //--------------------------------------------------------------------------
}
