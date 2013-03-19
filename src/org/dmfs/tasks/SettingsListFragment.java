package org.dmfs.tasks;

import java.util.ArrayList;
import java.util.HashMap;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.tasks.TaskListFragment.Callbacks;

import android.annotation.SuppressLint;
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
import android.widget.TextView;


/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView with a GridView.
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks} interface.
 */

@SuppressLint("ValidFragment")
public class SettingsListFragment extends ListFragment implements AbsListView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>
{

	private static final String TAG = "SettingsListFragment";
	public static final String LIST_SELECTION_ARGS = "list_selection_args";
	public static final String LIST_STRING_PARAMS = "list_string_params";
	public static final String LIST_FRAGMENT_LAYOUT = "list_fragment_layout";
	public static final String COMPARE_COLUMN_NAME = "column_name";
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

	private int fragmentLayout;


	public SettingsListFragment()
	{
		
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Bundle args = getArguments();
		listSelectionArguments = args.getString(LIST_SELECTION_ARGS);
		listSelectionParam = args.getStringArray(LIST_STRING_PARAMS);
		fragmentLayout = args.getInt(LIST_FRAGMENT_LAYOUT);
		listCompareColumnName = args.getString(COMPARE_COLUMN_NAME);
		View view = inflater.inflate(fragmentLayout, container, false);
		return view;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().restartLoader(-2, null, this);
		mAdapter = new VisibleListAdapter(mContext, null, 0);
		setListAdapter(mAdapter);
		getListView().setOnItemClickListener(this);
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
	}


	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId)
	{
		Log.d(TAG, "Item Clicked");
		VisibleListAdapter adapter = (VisibleListAdapter) adapterView.getAdapter();
		VisibleListAdapter.CheckableItem item = (VisibleListAdapter.CheckableItem) view.getTag();
		boolean checked = item.cb.isChecked();
		item.cb.setChecked(!checked);
		adapter.addToState(rowId, !checked);
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
		int listNameColumn, listColorColumn, compareColumn;
		private HashMap<Long, Boolean> savedPositions = new HashMap<Long, Boolean>();


		@Override
		public Cursor swapCursor(Cursor c)
		{
			if (c != null)
			{
				listNameColumn = c.getColumnIndex(TaskContract.TaskLists.LIST_NAME);
				listColorColumn = c.getColumnIndex(TaskContract.TaskLists.LIST_COLOR);
				compareColumn = c.getColumnIndex(listCompareColumnName);
			}
			else
			{
				listNameColumn = -1;
				listColorColumn = -1;
				compareColumn = -1;
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
			String listName = cur.getString(listNameColumn);
			CheckableItem item = (CheckableItem) v.getTag();
			item.nameTV.setText(listName);
			int listColor = cur.getInt(listColorColumn);
			item.bgColor.setBackgroundColor(listColor);

			if (!cur.isNull(compareColumn))
			{
				int checkValue = cur.getInt(compareColumn);
				item.cb.setChecked(checkValue == 1);
			}
			/*
			 * cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			 * 
			 * @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { Integer posInt = Integer.valueOf(listId); if
			 * (savedPositions.containsKey(posInt)) { savedPositions.remove(posInt); Log.d(TAG, "Removed ID with : " + posInt.toString()); } else {
			 * savedPositions.put(posInt, isChecked); Log.d(TAG, "Added ID with : " + posInt.toString()); } Log.d(TAG, "Length of ops is : " +
			 * savedPositions.size()); }
			 * 
			 * });
			 */

		}

		public class CheckableItem
		{
			TextView nameTV;
			View bgColor;
			CheckBox cb;
		}


		@Override
		public View newView(Context c, Cursor cur, ViewGroup vg)
		{
			View newInflatedView = inflater.inflate(R.layout.visible_task_list_item, null);
			CheckableItem item = new CheckableItem();
			item.nameTV = (TextView) newInflatedView.findViewById(R.id.visible_account_name);
			item.bgColor = newInflatedView.findViewById(R.id.visible_task_list_color);
			item.cb = (CheckBox) newInflatedView.findViewById(R.id.visible_task_list_checked);
			newInflatedView.setFocusable(false);
			newInflatedView.setTag(item);
			return newInflatedView;
		}


		public boolean addToState(long id, boolean val)
		{
			if (savedPositions.containsKey(Long.valueOf(id)))
			{
				savedPositions.remove(id);
				return false;
			}
			else
			{
				savedPositions.put(id, val);
				return true;
			}
		}


		public HashMap<Long, Boolean> getState()
		{
			return savedPositions;
		}

	}
	
	public boolean saveListState(){
		HashMap<Long, Boolean> savedPositions = ((VisibleListAdapter) getListAdapter()).getState();
		Log.d(TAG, "Length of Changes: " + savedPositions.size());
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		for (Long posInt : savedPositions.keySet())
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
			return false;
		}
		catch (OperationApplicationException e)
		{
			Log.e(TAG, "OperationApplicationException : " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
