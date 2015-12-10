/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.database.DataSetObservable;
import android.support.v4.util.SparseArrayCompat;


public class ObservableSparseArrayCompat<E> extends SparseArrayCompat<E>
{
	private final DataSetObservable mDataSetObservable;


	public ObservableSparseArrayCompat()
	{
		super();
		mDataSetObservable = new DataSetObservable();
	}


	public ObservableSparseArrayCompat(final int initialCapacity)
	{
		super(initialCapacity);
		mDataSetObservable = new DataSetObservable();
	}


	public DataSetObservable getDataSetObservable()
	{
		return mDataSetObservable;
	}


	private void notifyChanged()
	{
		mDataSetObservable.notifyChanged();
	}


	@Override
	public void append(final int key, final E value)
	{
		super.append(key, value);
		notifyChanged();
	}


	@Override
	public void clear()
	{
		super.clear();
		notifyChanged();
	}


	@Override
	public void delete(final int key)
	{
		super.delete(key);
		notifyChanged();
	}


	@Override
	public void put(final int key, final E value)
	{
		super.put(key, value);
		notifyChanged();
	}


	@Override
	public void remove(final int key)
	{
		super.remove(key);
		notifyChanged();
	}


	@Override
	public void removeAt(final int index)
	{
		super.removeAt(index);
		notifyChanged();
	}


	@Override
	public void removeAtRange(final int index, final int size)
	{
		super.removeAtRange(index, size);
		notifyChanged();
	}


	@Override
	public void setValueAt(final int index, final E value)
	{
		super.setValueAt(index, value);
		notifyChanged();
	}
}