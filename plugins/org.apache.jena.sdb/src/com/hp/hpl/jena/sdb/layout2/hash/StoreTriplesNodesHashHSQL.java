/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sdb.layout2.hash;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.layout2.LoaderTuplesNodes;
import com.hp.hpl.jena.sdb.layout2.SQLBridgeFactory2;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.StoreBaseHSQL;


public class StoreTriplesNodesHashHSQL extends StoreBaseHSQL
{
    public StoreTriplesNodesHashHSQL(SDBConnection connection, StoreDesc desc)
    {
        // HSQL can't handle complex RHS of a left join so no optional spotting. 
        super(connection, desc,
              new FmtLayout2HashHSQL(connection),
              //new LoaderHashHSQL(connection),
              new LoaderTuplesNodes(connection, TupleLoaderHashHSQL.class),
              new QueryCompilerFactoryHash(),
              new SQLBridgeFactory2(),
              new TableDescTriples(),
              new TableDescQuads(),
              new TableNodesHash()) ; 
        
        ((LoaderTuplesNodes) this.getLoader()).setStore(this);
    }

    @Override
    public long getSize(Node node)
    {
        return StoreBaseHash.getSize(getConnection(), getQuadTableDesc(), node) ;
    }
}
