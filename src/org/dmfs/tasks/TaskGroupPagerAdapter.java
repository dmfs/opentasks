package org.dmfs.tasks;

import org.dmfs.tasks.groupings.AbstractGroupingFactory;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.astuetz.PagerSlidingTabStrip.IconTabProvider;


/**
 * An adapter to populate the different views of grouped tasks for a ViewPager
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 */

public class TaskGroupPagerAdapter extends FragmentStatePagerAdapter implements IconTabProvider
{

	private static final String TAG = "TaskGroupPager";
	private final AbstractGroupingFactory[] mGroupingFactories;
	private final Context mContext;
	private boolean mTwoPaneLayout;


	/**
	 * 
	 * @param fm
	 *            A {@link FragmentManager}
	 * @param groupingFactories
	 *            An array of {@link AbstractGroupingFactory}.
	 * @param context
	 *            A context to access resources
	 */
	public TaskGroupPagerAdapter(FragmentManager fm, AbstractGroupingFactory[] groupingFactories, Context context)
	{
		super(fm);
		mGroupingFactories = groupingFactories;
		mContext = context;
	}


	@Override
	public CharSequence getPageTitle(int position)
	{
		AbstractGroupingFactory factory = mGroupingFactories[position];
		CharSequence title = "";
		try
		{
			title = mContext.getString(factory.getTitle());
		}
		catch (Resources.NotFoundException e)
		{
			Log.e(TAG, "Missing or invalid title resource for ExpandableGroupDescriptor " + factory);
		}
		return title;
	}


	@Override
	public Fragment getItem(int position)
	{
		TaskListFragment fragment = TaskListFragment.newInstance(position, mTwoPaneLayout);
		fragment.setExpandableGroupDescriptor(mGroupingFactories[position].getExpandableGroupDescriptor());
		return fragment;
	}


	@Override
	public int getCount()
	{
		return mGroupingFactories.length;
	}


	public void setTwoPaneLayout(boolean twoPane)
	{
		mTwoPaneLayout = twoPane;
	}


	@Override
	public int getPageIconResId(int position)
	{
		AbstractGroupingFactory factory = mGroupingFactories[position];

		try
		{
			return factory.getIconRessource();
		}
		catch (Resources.NotFoundException e)
		{
			Log.e(TAG, "Missing or invalid icon resource for ExpandableGroupDescriptor " + factory);
		}
		return 0;
	}


	@Override
	public Drawable getPageIconDrawable(int position)
	{
		return null;
	}
}
