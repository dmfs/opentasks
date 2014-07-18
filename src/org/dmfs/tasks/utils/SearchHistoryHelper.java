package org.dmfs.tasks.utils;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Wraps functionality to store and restore the search history as {@link SharedPreferences}.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * 
 */
public class SearchHistoryHelper
{
	public static final String SEARCH_PREFERENCES = "search_preferences";
	public static final String KEY_SEARCH_HISTORY = "search_history";

	public static final int HISTORY_SIZE = 5;

	private static final String SERIALIZER_SEPARATOR = ":;:";


	public static void newSearch(Context context, String searchString)
	{
		SharedPreferences searchPrefs = context.getSharedPreferences(SEARCH_PREFERENCES, 0);
		SharedPreferences.Editor editor = searchPrefs.edit();

		String historyString = searchPrefs.getString(KEY_SEARCH_HISTORY, null);
		String[] historyArray = deserialize(historyString);
		if (historyArray.length >= HISTORY_SIZE)
		{
			String[] persistArray = new String[HISTORY_SIZE];
			persistArray[0] = searchString;
			for (int i = 0; i < HISTORY_SIZE - 1; i++)
			{
				String string = historyArray[i];
				persistArray[i + 1] = string;
			}
			String persistString = serialize(persistArray);
			editor.putString(KEY_SEARCH_HISTORY, persistString);

		}
		else
		{
			String[] persistArray = new String[historyArray.length + 1];
			persistArray[0] = searchString;
			for (int i = 0; i < historyArray.length; i++)
			{
				String string = historyArray[i];
				persistArray[i + 1] = string;
			}
			String persistString = serialize(persistArray);
			editor.putString(KEY_SEARCH_HISTORY, persistString);
		}

		editor.commit();
	}


	public static void updateSearch(Context context, String searchString)
	{
		SharedPreferences searchPrefs = context.getSharedPreferences(SEARCH_PREFERENCES, 0);
		SharedPreferences.Editor editor = searchPrefs.edit();

		String historyString = searchPrefs.getString(KEY_SEARCH_HISTORY, null);
		String[] historyArray = deserialize(historyString);

		if (historyArray.length > 0)
		{
			historyArray[0] = searchString;
			String persistString = serialize(historyArray);
			editor.putString(KEY_SEARCH_HISTORY, persistString);
			editor.commit();
		}

	}


	public static void endSearch(Context context)
	{
		SharedPreferences searchPrefs = context.getSharedPreferences(SEARCH_PREFERENCES, 0);
		SharedPreferences.Editor editor = searchPrefs.edit();

		String historyString = searchPrefs.getString(KEY_SEARCH_HISTORY, null);
		String[] historyArray = deserialize(historyString);

		if (historyArray.length > 0 && historyArray[0].equals(""))
		{
			String[] persistArray = new String[historyArray.length - 1];
			for (int i = 1; i < historyArray.length; i++)
			{
				String string = historyArray[i];
				persistArray[i - 1] = string;

			}
			String persistString = serialize(persistArray);
			editor.putString(KEY_SEARCH_HISTORY, persistString);
			editor.commit();
		}

	}


	public static String[] loadSearchHistory(Context context)
	{
		SharedPreferences searchPrefs = context.getSharedPreferences(SEARCH_PREFERENCES, 0);
		String historyString = searchPrefs.getString(KEY_SEARCH_HISTORY, null);

		if (historyString == null)
		{
			return new String[0];
		}
		else
		{
			return deserialize(historyString);
		}

	}


	private static String serialize(String[] history)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < history.length; i++)
		{
			builder.append(history[i]);
			if (i < history.length - 1)
			{
				builder.append(SERIALIZER_SEPARATOR);
			}

		}
		return builder.toString();
	}


	private static String[] deserialize(String historyString)
	{
		if (historyString == null)
		{
			return new String[0];
		}
		return historyString.split(SERIALIZER_SEPARATOR);
	}

}
