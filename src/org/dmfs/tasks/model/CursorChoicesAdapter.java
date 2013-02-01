package org.dmfs.tasks.model;

import android.database.Cursor;
import android.graphics.drawable.Drawable;


public class CursorChoicesAdapter implements IChoicesAdapter
{

	private static final String TAG = "CursorChoicesAdapter";
	private final Cursor mCursor;
	private String mTitleColumn;
	private String mKeyColumn;


	public CursorChoicesAdapter(Cursor cursor)
	{
		mCursor = cursor;
	}


	@Override
	public String getTitle(Object object)
	{
		//return mCursor.getString(mCursor.getColumnIndex(mTitleColumn));
		return null;
	}


	@Override
	public Drawable getDrawable(Object object)
	{
		return null;

	}


	public String getKeyColumn()
	{
		return mKeyColumn;
	}


	public CursorChoicesAdapter setKeyColumn(String keyColumn)
	{
		mKeyColumn = keyColumn;
		return this;
	}


	public CursorChoicesAdapter setTitleColumn(String column)
	{
		mTitleColumn = column;
		return this;
	}


	public Cursor getChoices()
	{
		return mCursor;
	}

}
