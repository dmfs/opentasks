package org.dmfs.tasks;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.tasks.TaskListFragment.Callbacks;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;


/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView with a GridView.
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks} interface.
 */
public class VisibleListFragment extends ListFragment implements AbsListView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>
{

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;
	private Context mContext;
	private OnFragmentInteractionListener mListener;
	private VisibleListAdapter mAdapter;
	/**
	 * The fragment's ListView/GridView.
	 */
	private AbsListView mListView;


	/**
	 * The Adapter which will be used to populate the ListView/GridView with Views.
	 */

	// TODO: Rename and change types of parameters
	public static VisibleListFragment newInstance(String param1, String param2)
	{
		VisibleListFragment fragment = new VisibleListFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}


	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
	 */
	public VisibleListFragment()
	{
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (getArguments() != null)
		{
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}

		// TODO: Change Adapter to display your content

	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_visiblelist, container, false);

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
		// TODO: Update argument type and name
		public void onFragmentInteraction(String id);
	}


	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1)
	{
		// TODO Auto-generated method stub
		return new CursorLoader(mContext, TaskContract.TaskLists.CONTENT_URI, new String[] { TaskContract.TaskLists._ID, TaskContract.TaskLists.LIST_NAME },
			TaskContract.TaskLists.SYNC_ENABLED + "=?", new String[] { "1" }, null);
	}


	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1)
	{
		mAdapter.swapCursor(arg1);

	}


	@Override
	public void onLoaderReset(Loader<Cursor> arg0)
	{
		// TODO Auto-generated method stub

	}

	private class VisibleListAdapter extends CursorAdapter
	{
		LayoutInflater inflater;
		int accountNameColumn;


		public VisibleListAdapter(Context context, Cursor c, int flags)
		{
			super(context, c, flags);
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}


		@Override
		public void bindView(View v, Context c, Cursor cur)
		{
			accountNameColumn = cur.getColumnIndex(TaskContract.TaskLists.LIST_NAME);
			String accountName = cur.getString(accountNameColumn);
			TextView accountNameTV = (TextView) v.findViewById(R.id.visible_account_name);
			accountNameTV.setText(accountName);
		}


		@Override
		public View newView(Context c, Cursor cur, ViewGroup vg)
		{

			return inflater.inflate(R.layout.visible_task_list_item, null);
		}

	}

}
