/*
 * Copyright 2008 Udai Gupta, Ralf Joachim
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
package org.castor.cpa.test.test70;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Ignore;

@Ignore
public class ColEmptyIterator implements Iterator<Item> {
    public boolean hasNext() {
        return false;
    }

    public Item next() {
        throw new NoSuchElementException();
    }

    public void remove() {
        throw new IllegalStateException();
    }
}
