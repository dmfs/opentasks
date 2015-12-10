package org.dmfs.tasks.model;

import android.text.TextUtils;


public final class CheckListItem
{
	public boolean checked;
	public String text;


	public CheckListItem(boolean checked, String text)
	{
		this.checked = checked;
		this.text = text;
	}


	@Override
	public int hashCode()
	{
		return text != null ? (text.hashCode() << 1) + (checked ? 1 : 0) : (checked ? 1 : 0);
	}


	public boolean equals(Object o)
	{
		if (!(o instanceof CheckListItem))
		{
			return false;
		}
		CheckListItem other = (CheckListItem) o;
		return TextUtils.equals(text, other.text) && checked == other.checked;
	};
}
