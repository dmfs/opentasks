/*
 * Copyright (c) 2008-2012, Hazel Bilisim Ltd. All Rights Reserved.
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

package org.dmfs.tasks.utils;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Found at <a
 * href="http://grepcode.com/file_/repo1.maven.org/maven2/com.hazelcast/hazelcast/2.1.2/com/hazelcast/util/SetFromMap.java/?v=source">SetFromMap.java</a>
 * <p>
 * This class is used on SDK Level 8 only.
 */
public class SetFromMap<E> extends AbstractSet<E> implements Set<E>, Serializable
{

    /**
     * Generated serial id.
     */
    private static final long serialVersionUID = 9178569914836151896L;

    private final Map<E, Boolean> m;


    public SetFromMap(final Map<E, Boolean> map)
    {
        super();
        this.m = map;
    }


    public void clear()
    {
        m.clear();
    }


    public int size()
    {
        return m.size();
    }


    public boolean isEmpty()
    {
        return m.isEmpty();
    }


    public boolean contains(Object o)
    {
        return m.containsKey(o);
    }


    public boolean remove(Object o)
    {
        return m.remove(o) != null;
    }


    public boolean add(E e)
    {
        return m.put(e, Boolean.TRUE) == null;
    }


    public Iterator<E> iterator()
    {
        return m.keySet().iterator();
    }


    public Object[] toArray()
    {
        return m.keySet().toArray();
    }


    public <T> T[] toArray(T[] a)
    {
        return m.keySet().toArray(a);
    }


    public String toString()
    {
        return m.keySet().toString();
    }


    public int hashCode()
    {
        return m.keySet().hashCode();
    }


    public boolean equals(Object o)
    {
        return o == this || m.keySet().equals(o);
    }


    public boolean containsAll(Collection<?> c)
    {
        return m.keySet().containsAll(c);
    }


    public boolean removeAll(Collection<?> c)
    {
        return m.keySet().removeAll(c);
    }


    public boolean retainAll(Collection<?> c)
    {
        return m.keySet().retainAll(c);
    }
}
