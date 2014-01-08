package org.dmfs.tasks;

import org.dmfs.tasks.utils.ExpandableGroupDescriptor;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;


/**
 * An adapter to populate the different views of grouped tasks for a ViewPager
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 */

public class TaskGroupPagerAdapter extends FragmentPagerAdapter
{

	private static final String TAG = "TaskGroupPager";
	private final ExpandableGroupDescriptor[] mGroupDescriptors;
	private final Context mContext;


	/**
	 * 
	 * @param fm
	 *            A {@link FragmentManager}
	 * @param groupDescriptors
	 *            An array of ExpandableGroupDescriptors that describes the groupings and how to display them
	 * @param context
	 *            A context to access resources
	 */
	public TaskGroupPagerAdapter(FragmentManager fm, ExpandableGroupDescriptor[] groupDescriptors, Context context)
	{
		super(fm);
		mGroupDescriptors = groupDescriptors;
		mContext = context;
	}


	@Override
	public CharSequence getPageTitle(int position)
	{
		ExpandableGroupDescriptor descriptor = mGroupDescriptors[position];
		CharSequence title = "";
		try
		{
			title = mContext.getString(descriptor.getTitle());
		}
		catch (Resources.NotFoundException e)
		{
			Log.e(TAG, "Missing or invalid title resource for ExpandableGroupDescriptor " + descriptor);
		}
		return title;
	}


	@Override
	public Fragment getItem(int position)
	{
		TaskListFragment fragment = new TaskListFragment();
		fragment.setExpandableGroupDescriptor(mGroupDescriptors[position]);
		fragment.setInstanceId("TaskListFragment" + position);
		return fragment;
	}


	@Override
	public int getCount()
	{
		return mGroupDescriptors.length;
	}

}
