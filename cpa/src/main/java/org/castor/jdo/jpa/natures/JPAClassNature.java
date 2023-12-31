/*
 * Copyright 2005 Werner Guttmann
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
package org.castor.jdo.jpa.natures;

import org.castor.core.nature.BaseNature;
import org.castor.core.nature.PropertyHolder;

/**
 * A {@link BaseNature} extension that gives access to information derived from
 * class bound JPA annotations.
 * 
 * @see PropertyHolder
 * @author Peter Schmidt
 * @since 1.3
 */
public class JPAClassNature extends BaseNature {

    /** 
     * Property Key for {@link javax.persistence.Entity#name()}. 
     */
    private static final String ENTITY_NAME = "ENTITY_NAME";
    /** 
     * Property Key for {@link javax.persistence.Table#name()}. 
     */
    private static final String TABLE_NAME = "TABLE_NAME";
    /** 
     * Property Key for {@link javax.persistence.Table#catalog()}. 
     */
    private static final String TABLE_CATALOG = "TABLE_CATALOG";
    /** 
     * Property Key for {@link javax.persistence.Table#schema()}. 
     */
    private static final String TABLE_SCHEMA = "TABLE_SCHEMA";

    /**
     * Instantiate a {@link JPAClassNature} to access the given
     * {@link PropertyHolder}.
     * 
     * @param holder
     *            The underlying {@link PropertyHolder} (obviously a
     *            {@link org.castor.jdo.jpa.info.ClassInfo ClassInfo}).
     * 
     * @see PropertyHolder
     * 
     */
    public JPAClassNature(final PropertyHolder holder) {
        super(holder);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.castor.core.nature.Nature#getId()
     */
    public String getId() {
        return getClass().getName();
    }

    /**
     * @see #ENTITY_NAME
     * @param entityname
     *            The value of {@link javax.persistence.Entity#name()}.
     */
    public void setEntityName(final String entityname) {
        super.setProperty (ENTITY_NAME, entityname);
    }

    /**
     * @see #ENTITY_NAME
     * @return The value of {@link javax.persistence.Entity#name()}.
     */
    public String getEntityName() {
        return (String) getProperty (ENTITY_NAME);
    }

    /**
     * @see #TABLE_NAME
     * @param tablename
     *            The value of {@link javax.persistence.Table#name()}
     */
    public void setTableName(final String tablename) {
        super.setProperty (TABLE_NAME, tablename);
    }

    /**
     * @see #TABLE_NAME
     * @return The value of {@link javax.persistence.Table#name()}
     */
    public String getTableName() {
        return (String) super.getProperty (TABLE_NAME);
    }

    /**
     * @see #TABLE_CATALOG
     * @param catalog
     *            The value of {@link javax.persistence.Table#catalog()}
     */
    public void setTableCatalog(final String catalog) {
        super.setProperty (TABLE_CATALOG, catalog);
    }

    /**
     * @see #TABLE_CATALOG
     * @return The value of {@link javax.persistence.Table#catalog()}
     */
    public String getTableCatalog() {
        return (String) super.getProperty (TABLE_CATALOG);
    }

    /**
     * @see #TABLE_SCHEMA
     * @param schema
     *            The value of {@link javax.persistence.Table#schema()}
     */
    public void setTableSchema(final String schema) {
        super.setProperty (TABLE_SCHEMA, schema);
    }

    /**
     * @see #TABLE_SCHEMA
     * @return The value of{@link javax.persistence.Table#schema()}
     */
    public String getTableSchema() {
        return (String) super.getProperty (TABLE_SCHEMA);
    }

}

