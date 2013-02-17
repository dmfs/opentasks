package org.dmfs.tasks.utils;

import android.database.Cursor;
import android.view.View;


public interface ViewDescriptor
{
	public void populateView(View view, Cursor cursor);


	public int getView();
}
