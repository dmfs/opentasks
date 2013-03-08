package org.dmfs.tasks.groups;

public final class ConstantFilter extends AbstractFilter
{
	private final String mSelection;
	private final String[] mSelectionArgs;


	public ConstantFilter(String selection, String selectionArgs)
	{
		mSelection = selection;
		mSelectionArgs = new String[] { selectionArgs };
	}


	@Override
	public String getSelection()
	{
		return mSelection;
	}


	@Override
	public String[] getSelectionArgs()
	{
		return mSelectionArgs;
	}

}
