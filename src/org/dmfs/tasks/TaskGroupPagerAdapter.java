package org.dmfs.tasks;

import java.util.HashMap;
import java.util.Map;
import org.dmfs.tasks.groupings.AbstractGroupingFactory;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;
import com.astuetz.PagerSlidingTabStrip.IconTabProvider;


/**
 * An adapter to populate the different views of grouped tasks for a ViewPager.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * @author Marten Gajda <marten@dmfs.org>
 */

public class TaskGroupPagerAdapter extends FragmentStatePagerAdapter implements IconTabProvider
{

	private static final String TAG = "TaskGroupPager";
	private final Map<Integer, AbstractGroupingFactory> mGroupingFactories = new HashMap<Integer, AbstractGroupingFactory>(16);
	private final Context mContext;
	private boolean mTwoPaneLayout;
	private final Menu mMenu;


	/**
	 * 
	 * @param fm
	 *            A {@link FragmentManager}
	 * @param groupingFactories
	 *            An array of {@link AbstractGroupingFactory}.
	 * @param context
	 *            A context to access resources
	 */
	@SuppressLint("NewApi")
	public TaskGroupPagerAdapter(FragmentManager fm, AbstractGroupingFactory[] groupingFactories, Context context, int menuRes)
	{
		super(fm);
		mContext = context;

		// TODO: add support for Android<SDK 11
		// this is a hack to get a Menu
		mMenu = new PopupMenu(context, null).getMenu();
		((Activity) context).getMenuInflater().inflate(menuRes, mMenu);

		// remove invisible menu items
		int itemCount = mMenu.size();
		int i = 0;
		while (i < itemCount)
		{
			MenuItem item = mMenu.getItem(i);
			if (!item.isVisible())
			{
				mMenu.removeItem(item.getItemId());
				--itemCount;
			}
			else
			{
				++i;
			}
		}
		for (AbstractGroupingFactory factory : groupingFactories)
		{
			mGroupingFactories.put(factory.getId(), factory);
		}
	}


	@Override
	public CharSequence getPageTitle(int position)
	{
		return mMenu.getItem(position).getTitle();
	}


	@Override
	public Fragment getItem(int position)
	{
		AbstractGroupingFactory factory = getGroupingFactoryForId(mMenu.getItem(position).getItemId());

		TaskListFragment fragment = TaskListFragment.newInstance(position, mTwoPaneLayout);
		fragment.setExpandableGroupDescriptor(factory.getExpandableGroupDescriptor());
		return fragment;

	}


	public AbstractGroupingFactory getGroupingFactoryForId(int id)
	{
		return mGroupingFactories.get(id);
	}


	@Override
	public int getCount()
	{
		return mMenu.size();
	}


	public void setTwoPaneLayout(boolean twoPane)
	{
		mTwoPaneLayout = twoPane;
	}


	@Override
	public int getPageIconResId(int position)
	{
		return -1;
	}


	@Override
	public Drawable getPageIconDrawable(int position)
	{
		return mMenu.getItem(position).getIcon();
	}
}
