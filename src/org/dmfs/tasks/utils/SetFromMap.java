/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.dmfs.tasks.utils;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Taken from http://javasourcecode.org/html/open-source/jdk/jdk-6u23/java.util/Collections.java.html
 * @author Arjun Naik
 *
 * @param <E>
 */
public class SetFromMap<E> extends AbstractSet<E> implements Set<E>, Serializable
{
	private final Map<E, Boolean> m; // The backing map
	private transient Set<E> s; // Its keySet


	public SetFromMap(Map<E, Boolean> map)
	{
		if (!map.isEmpty())
			throw new IllegalArgumentException("Map is non-empty");
		m = map;
		s = map.keySet();
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
		return s.iterator();
	}


	public Object[] toArray()
	{
		return s.toArray();
	}


	public <T> T[] toArray(T[] a)
	{
		return s.toArray(a);
	}


	public String toString()
	{
		return s.toString();
	}


	public int hashCode()
	{
		return s.hashCode();
	}


	public boolean equals(Object o)
	{
		return o == this || s.equals(o);
	}


	public boolean containsAll(Collection<?> c)
	{
		return s.containsAll(c);
	}


	public boolean removeAll(Collection<?> c)
	{
		return s.removeAll(c);
	}


	public boolean retainAll(Collection<?> c)
	{
		return s.retainAll(c);
	}

	// addAll is the only inherited implementation

	private static final long serialVersionUID = 2454657854757543876L;


	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		stream.defaultReadObject();
		s = m.keySet();
	}
}
