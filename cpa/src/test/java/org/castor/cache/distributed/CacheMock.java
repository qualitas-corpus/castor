/*
 * Copyright 2005 Ralf Joachim
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
package org.castor.cache.distributed;

import java.util.HashMap;

/**
 * Cache to test AbstractDistributedCache.
 * 
 * @author <a href="mailto:ralf DOT joachim AT syscon DOT eu">Ralf Joachim</a>
 * @version $Revision: 8143 $ $Date: 2006-04-29 04:11:14 -0600 (Sat, 29 Apr 2006) $
 * @since 1.0
 */
public final class CacheMock extends AbstractDistributedCache {
    //--------------------------------------------------------------------------
    
    /**
     * Default constructor.
     */
    public CacheMock() {
        HashMap<Object, Object> map = new HashMap<Object, Object>();
        
        // put some entries for testing into the cache
        map.put("first key", "first value");
        map.put("second key", "second value");
        map.put("third key", "third value");
        
        setCache(map);
    }
    
    //--------------------------------------------------------------------------

    /**
     * @see org.castor.cache.Cache#getType()
     */
    public String getType() { return "dummy"; }

    //--------------------------------------------------------------------------
}
