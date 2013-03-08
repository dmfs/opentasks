package org.dmfs.tasks.groups;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.dmfs.provider.tasks.TaskContract.Instances;
import org.dmfs.provider.tasks.TaskContract.TaskLists;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.R;
import org.dmfs.tasks.groups.cursorloaders.CursorLoaderFactory;
import org.dmfs.tasks.utils.ExpandableChildDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptor;
import org.dmfs.tasks.utils.ViewDescriptor;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.format.Time;
import android.view.View;
import android.widget.TextView;


public interface ByList
{
	/**
	 * A {@link ViewDescriptor} that knows how to present the tasks in the task list.
	 */
	public final ViewDescriptor TASK_VIEW_DESCRIPTOR = new ViewDescriptor()
	{
		/**
		 * We use this to get the current time.
		 */
		private Time mNow;

		/**
		 * The formatter we use for due dates other than today.
		 */
		private final DateFormat mDateFormatter = DateFormat.getDateInstance(SimpleDateFormat.MEDIUM);

		/**
		 * The formatter we use for tasks that are due today.
		 */
		private final DateFormat mTimeFormatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);


		@Override
		public void populateView(View view, Cursor cursor)
		{
			TextView title = (TextView) view.findViewById(android.R.id.title);
			Integer status = cursor.getInt(11);

			if (title != null)
			{
				String text = cursor.getString(5);
				title.setText(text);
				if (status != null && status > Tasks.STATUS_IN_PROCESS)
				{
					title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
				}
			}

			TextView dueDateField = (TextView) view.findViewById(R.id.task_due_date);
			if (dueDateField != null)
			{
				Time dueDate = Common.DUE_ADAPTER.get(cursor);

				if (dueDate != null)
				{
					if (mNow == null)
					{
						mNow = new Time();
					}
					mNow.clear(TimeZone.getDefault().getID());
					mNow.setToNow();

					dueDateField.setText(makeDueDate(dueDate, view.getContext()));

					// highlight overdue dates & times
					if (dueDate.before(mNow) && (status == null || status < Tasks.STATUS_COMPLETED))
					{
						dueDateField.setTextColor(Color.RED);
					}
					else
					{
						dueDateField.setTextColor(Color.argb(255, 0x80, 0x80, 0x80));
					}
				}
				else
				{
					dueDateField.setText("");
				}
			}

			View colorbar = view.findViewById(R.id.colorbar);
			if (colorbar != null)
			{
				colorbar.setBackgroundColor(cursor.getInt(6));
			}
		}


		@Override
		public int getView()
		{
			return R.layout.task_list_element;
		}


		/**
		 * Get the due date to show. It returns just a time for tasks that are due today and a date otherwise.
		 * 
		 * @param due
		 *            The due date to format.
		 * @return A String with the formatted date.
		 */
		private String makeDueDate(Time due, Context context)
		{
			due.switchTimezone(TimeZone.getDefault().getID());
			if (due.year == mNow.year && due.yearDay == mNow.yearDay)
			{
				return context.getString(R.string.today) + ", " + mTimeFormatter.format(new Date(due.toMillis(false)));
			}
			else
			{
				return mDateFormatter.format(new Date(due.toMillis(false)));
			}
		}
	};

	/**
	 * A {@link ViewDescriptor} that knows how to present list groups.
	 */
	public final ViewDescriptor GROUP_VIEW_DESCRIPTOR = new ViewDescriptor()
	{

		@Override
		public void populateView(View view, Cursor cursor)
		{
			TextView title = (TextView) view.findViewById(android.R.id.title);
			if (title != null)
			{
				title.setText(getTitle(cursor, view.getContext()));
			}
			TextView text1 = (TextView) view.findViewById(android.R.id.text1);
			if (text1 != null)
			{
				text1.setText(cursor.getString(3));
			}
			// view.setBackgroundColor(cursor.getInt(2));
		}


		@Override
		public int getView()
		{
			return R.layout.task_list_group;
		}


		/**
		 * Return the title of a list group.
		 * 
		 * @param cursor
		 *            A {@link Cursor} pointing to the current group.
		 * @return A {@link String} with the group name.
		 */
		private String getTitle(Cursor cursor, Context context)
		{
			return cursor.getString(1);
		}

	};

	/**
	 * A descriptor that knows how to load elements in a list group.
	 */
	public final static ExpandableChildDescriptor CHILD_DESCRIPTOR = new ExpandableChildDescriptor(Instances.CONTENT_URI, Common.INSTANCE_PROJECTION,
		Instances.VISIBLE + "=1 and " + Instances.LIST_ID + "=?", Instances.DEFAULT_SORT_ORDER, 0).setViewDescriptor(TASK_VIEW_DESCRIPTOR);

	/**
	 * A descriptor for the "grouped by list" view.
	 */
	public final static ExpandableGroupDescriptor GROUP_DESCRIPTOR = new ExpandableGroupDescriptor(new CursorLoaderFactory(TaskLists.CONTENT_URI, new String[] {
		TaskLists._ID, TaskLists.LIST_NAME, TaskLists.LIST_COLOR, TaskLists.ACCOUNT_NAME }, null, null, TaskLists.ACCOUNT_NAME + ", " + TaskLists.LIST_NAME),
		CHILD_DESCRIPTOR).setViewDescriptor(GROUP_VIEW_DESCRIPTOR);

}
