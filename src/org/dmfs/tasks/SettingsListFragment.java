package org.dmfs.tasks;

import java.util.ArrayList;
import java.util.HashMap;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.tasks.TaskListFragment.Callbacks;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView with a GridView.
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks} interface.
 */
public class SettingsListFragment extends ListFragment implements AbsListView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>
{

	private static final String TAG = "SettingsListFragment";
	private Context mContext;
	private OnFragmentInteractionListener mListener;
	private VisibleListAdapter mAdapter;
	/**
	 * The fragment's ListView/GridView.
	 */
	private AbsListView mListView;
	private String listSelectionArguments;
	private String[] listSelectionParam;
	private String listCompareColumnName;
	private HashMap<Integer, Boolean> savedPositions = new HashMap<Integer, Boolean>();

	private int fragmentLayout;


	public SettingsListFragment()
	{

	}


	public SettingsListFragment(String args, String[] params, int layout, String columnName)
	{
		listSelectionArguments = args;
		listSelectionParam = params;
		fragmentLayout = layout;
		listCompareColumnName = columnName;
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(fragmentLayout, container, false);

		// Set the adapter
		mListView = (AbsListView) view.findViewById(android.R.id.list);
		((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

		// Set OnItemClickListener so we can be notified on item clicks
		mListView.setOnItemClickListener(this);

		getLoaderManager().restartLoader(-2, null, this);
		mAdapter = new VisibleListAdapter(mContext, null, 0);
		setListAdapter(mAdapter);
		
		return view;
	}


	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try
		{
			mListener = (OnFragmentInteractionListener) activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
		}

		mContext = activity.getBaseContext();
	}


	@Override
	public void onDetach()
	{
		super.onDetach();
		mListener = null;
		Log.d(TAG, "Length of Changes: " + savedPositions.size());
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		for (Integer posInt : savedPositions.keySet())
		{
			boolean val = savedPositions.get(posInt);
			ContentProviderOperation op = ContentProviderOperation.newUpdate(TaskContract.TaskLists.CONTENT_URI)
				.withSelection(TaskContract.TaskLists._ID + "=?", new String[] { posInt.toString() }).withValue(listCompareColumnName, val ? "1" : "0").build();
			ops.add(op);
		}
		try
		{
			mContext.getContentResolver().applyBatch(TaskContract.AUTHORITY, ops);
		}
		catch (RemoteException e)
		{
			Log.e(TAG, "Remote Exception :" + e.getMessage());
			e.printStackTrace();
		}
		catch (OperationApplicationException e)
		{
			Log.e(TAG, "OperationApplicationException : " + e.getMessage());
			e.printStackTrace();
		}
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		if (null != mListener)
		{
			// Notify the active callbacks interface (the activity, if the
			// fragment is attached to one) that an item has been selected.

		}
	}


	/**
	 * The default content for this Fragment has a TextView that is shown when the list is empty. If you would like to change the text, call this method to
	 * supply the text it should use.
	 */
	public void setEmptyText(CharSequence emptyText)
	{
		View emptyView = mListView.getEmptyView();

		if (emptyText instanceof TextView)
		{
			((TextView) emptyView).setText(emptyText);
		}
	}

	/**
	 * This interface must be implemented by activities that contain this fragment to allow an interaction in this fragment to be communicated to the activity
	 * and potentially other fragments contained in that activity.
	 * <p>
	 * See the Android Training lesson <a href= "http://developer.android.com/training/basics/fragments/communicating.html" >Communicating with Other
	 * Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener
	{
		public void viewSyncedLists();


		public void savedUpdatedSyncedLists();


		public void cancelFromSyncedLists();
	}


	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1)
	{
		return new CursorLoader(mContext, TaskContract.TaskLists.CONTENT_URI, new String[] { TaskContract.TaskLists._ID, TaskContract.TaskLists.LIST_NAME,
			TaskContract.TaskLists.LIST_COLOR, TaskContract.TaskLists.SYNC_ENABLED, TaskContract.TaskLists.VISIBLE }, listSelectionArguments,
			listSelectionParam, null);
	}


	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1)
	{
		mAdapter.swapCursor(arg1);

	}


	@Override
	public void onLoaderReset(Loader<Cursor> arg0)
	{
		mAdapter.changeCursor(null);

	}

	private class VisibleListAdapter extends CursorAdapter
	{
		LayoutInflater inflater;
		int listNameColumn, listColorColumn, compareColumn, idColumn;


		@Override
		public Cursor swapCursor(Cursor c)
		{
			if (c != null)
			{
				listNameColumn = c.getColumnIndex(TaskContract.TaskLists.LIST_NAME);
				listColorColumn = c.getColumnIndex(TaskContract.TaskLists.LIST_COLOR);
				compareColumn = c.getColumnIndex(listCompareColumnName);
				idColumn = c.getColumnIndex(TaskContract.TaskLists._ID);
			}
			else
			{
				listNameColumn = -1;
				listColorColumn = -1;
				compareColumn = -1;
				idColumn = -1;
			}
			return super.swapCursor(c);

		}


		public VisibleListAdapter(Context context, Cursor c, int flags)
		{
			super(context, c, flags);
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}


		@Override
		public void bindView(View v, Context c, final Cursor cur)
		{
			final int listId = cur.getInt(idColumn);
			String listName = cur.getString(listNameColumn);
			TextView listNameTV = (TextView) v.findViewById(R.id.visible_account_name);
			listNameTV.setText(listName);
			View listColorView = v.findViewById(R.id.visible_task_list_color);
			int listColor = cur.getInt(listColorColumn);
			listColorView.setBackgroundColor(listColor);

			CheckBox cb = (CheckBox) v.findViewById(R.id.visible_task_list_checked);
			if (!cur.isNull(compareColumn))
			{
				int checkValue = cur.getInt(compareColumn);
				cb.setChecked(checkValue == 1);
			}
			cb.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					Integer posInt = Integer.valueOf(listId);
					if (savedPositions.containsKey(posInt))
					{
						savedPositions.remove(posInt);
						Log.d(TAG, "Removed ID with : " + posInt.toString());
					}
					else
					{
						savedPositions.put(posInt, isChecked);
						Log.d(TAG, "Added ID with : " + posInt.toString());
					}
					Log.d(TAG, "Length of ops is : " + savedPositions.size());
				}

			});
		}


		@Override
		public View newView(Context c, Cursor cur, ViewGroup vg)
		{

			return inflater.inflate(R.layout.visible_task_list_item, null);
		}

	}

}
