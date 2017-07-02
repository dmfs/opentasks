/*
 * Copyright (C) 2014 Marten Gajda <marten@dmfs.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.dmfs.provider.tasks;

import java.util.HashSet;
import java.util.Set;

import org.dmfs.ngrams.NGramGenerator;
import org.dmfs.provider.tasks.TaskContract.Properties;
import org.dmfs.provider.tasks.TaskContract.TaskColumns;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.provider.tasks.TaskDatabaseHelper.Tables;
import org.dmfs.provider.tasks.model.TaskAdapter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;


/**
 * Supports the {@link TaskDatabaseHelper} in the manner of full-text-search.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * @author Marten Gajda <marten@dmfs.org>
 */
public class FTSDatabaseHelper
{

	private final static float SEARCH_RESULTS_MIN_SCORE = 0.4f;

	/**
	 * A Generator for 3-grams.
	 */
	private final static NGramGenerator TRIGRAM_GENERATOR = new NGramGenerator(3, 1).setAddSpaceInFront(true);

	/**
	 * A Generator for 4-grams.
	 */
	private final static NGramGenerator TETRAGRAM_GENERATOR = new NGramGenerator(4, 3 /* shorter words are fully covered by trigrams */).setAddSpaceInFront(true);

	/**
	 * Search content columns. Defines all the columns for the full text search
	 * 
	 * @author Tobias Reinsch <tobias@dmfs.org>
	 */
	public interface FTSContentColumns
	{
		/**
		 * The row id of the belonging task.
		 */
		public static final String TASK_ID = "fts_task_id";

		/**
		 * The the property id of the searchable entry or <code>null</code> if the entry is not related to a property.
		 */
		public static final String PROPERTY_ID = "fts_property_id";

		/**
		 * The the type of the searchable entry
		 */
		public static final String TYPE = "fts_type";

		/**
		 * An n-gram for a task.
		 */
		public static final String NGRAM_ID = "fts_ngram_id";

	}

	/**
	 * The columns of the N-gram table for the FTS search
	 * 
	 * @author Tobias Reinsch <tobias@dmfs.org>
	 */
	public interface NGramColumns
	{
		/**
		 * The row id of the N-gram.
		 */
		public static final String NGRAM_ID = "ngram_id";

		/**
		 * The content of the N-gram
		 */
		public static final String TEXT = "ngram_text";

	}

	public static final String FTS_CONTENT_TABLE = "FTS_Content";
	public static final String FTS_NGRAM_TABLE = "FTS_Ngram";
	public static final String FTS_TASK_VIEW = "FTS_Task_View";
	public static final String FTS_TASK_PROPERTY_VIEW = "FTS_Task_Property_View";

	/**
	 * SQL command to create the table for full text search and contains relationships between ngrams and tasks
	 */
	private final static String SQL_CREATE_SEARCH_CONTENT_TABLE = "CREATE TABLE " + FTS_CONTENT_TABLE + "( " + FTSContentColumns.TASK_ID + " Integer, "
		+ FTSContentColumns.NGRAM_ID + " Integer, " + FTSContentColumns.PROPERTY_ID + " Integer, " + FTSContentColumns.TYPE + " Integer, " + "FOREIGN KEY("
		+ FTSContentColumns.TASK_ID + ") REFERENCES " + Tables.TASKS + "(" + TaskColumns._ID + ")," + "FOREIGN KEY(" + FTSContentColumns.TASK_ID
		+ ") REFERENCES " + Tables.TASKS + "(" + TaskColumns._ID + ") UNIQUE (" + FTSContentColumns.TASK_ID + ", " + FTSContentColumns.TYPE + ", "
		+ FTSContentColumns.PROPERTY_ID + ") ON CONFLICT IGNORE )";

	/**
	 * SQL command to create the table that stores the NGRAMS
	 */
	private final static String SQL_CREATE_NGRAM_TABLE = "CREATE TABLE " + FTS_NGRAM_TABLE + "( " + NGramColumns.NGRAM_ID
		+ " Integer PRIMARY KEY AUTOINCREMENT, " + NGramColumns.TEXT + " Text)";

	// FIXME: at present the minimum score is hard coded can we leave that decision to the caller?
	private final static String SQL_RAW_QUERY_SEARCH_TASK = "SELECT %s " + ", min(1.0*count(*)/?, 1.0) as " + TaskContract.Tasks.SCORE + " from "
		+ FTS_NGRAM_TABLE + " join " + FTS_CONTENT_TABLE + " on (" + FTS_NGRAM_TABLE + "." + NGramColumns.NGRAM_ID + "=" + FTS_CONTENT_TABLE + "."
		+ FTSContentColumns.NGRAM_ID + ") join " + Tables.INSTANCE_VIEW + " on (" + Tables.INSTANCE_VIEW + "." + Tasks._ID + " = " + FTS_CONTENT_TABLE + "."
		+ FTSContentColumns.TASK_ID + ") where %s group by " + Tasks._ID + " having " + TaskContract.Tasks.SCORE + " >= " + SEARCH_RESULTS_MIN_SCORE
		+ " order by %s;";

	private final static String SQL_RAW_QUERY_SEARCH_TASK_DEFAULT_PROJECTION = Tables.INSTANCE_VIEW + ".* ," + FTS_NGRAM_TABLE + "." + NGramColumns.TEXT;

	private final static String SQL_CREATE_SEARCH_TASK_DELETE_TRIGGER = "CREATE TRIGGER search_task_delete_trigger AFTER DELETE ON " + Tables.TASKS + " BEGIN "
		+ " DELETE FROM " + FTS_CONTENT_TABLE + " WHERE " + FTSContentColumns.TASK_ID + " =  old." + Tasks._ID + "; END";

	private final static String SQL_CREATE_SEARCH_TASK_DELETE_PROPERTY_TRIGGER = "CREATE TRIGGER search_task_delete_property_trigger AFTER DELETE ON "
		+ Tables.PROPERTIES + " BEGIN " + " DELETE FROM " + FTS_CONTENT_TABLE + " WHERE " + FTSContentColumns.TASK_ID + " =  old." + Properties.TASK_ID
		+ " AND " + FTSContentColumns.PROPERTY_ID + " = old." + Properties.PROPERTY_ID + "; END";

	/**
	 * The different types of searchable entries for tasks linked to the <code>TYPE</code> column.
	 * 
	 * @author Tobias Reinsch <tobias@dmfs.org>
	 * @author Marten Gajda <marten@dmfs.org>
	 */
	public interface SearchableTypes
	{
		/**
		 * This is an entry for the title of a task.
		 */
		public static final int TITLE = 1;

		/**
		 * This is an entry for the description of a task.
		 */
		public static final int DESCRIPTION = 2;

		/**
		 * This is an entry for the location of a task.
		 */
		public static final int LOCATION = 3;

		/**
		 * This is an entry for a property of a task.
		 */
		public static final int PROPERTY = 4;

	}


	public static void onCreate(SQLiteDatabase db)
	{
		initializeFTS(db);
	}


	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		if (oldVersion < 8)
		{
			initializeFTS(db);
			initializeFTSContent(db);
		}
		if (oldVersion < 16)
		{
			db.execSQL(TaskDatabaseHelper.createIndexString(FTS_CONTENT_TABLE, true, FTSContentColumns.TYPE, FTSContentColumns.TASK_ID,
				FTSContentColumns.PROPERTY_ID));
		}
	}


	/**
	 * Creates the tables and triggers used in FTS.
	 * 
	 * @param db
	 *            The {@link SQLiteDatabase}.
	 */
	private static void initializeFTS(SQLiteDatabase db)
	{
		db.execSQL(SQL_CREATE_SEARCH_CONTENT_TABLE);
		db.execSQL(SQL_CREATE_NGRAM_TABLE);
		db.execSQL(SQL_CREATE_SEARCH_TASK_DELETE_TRIGGER);
		db.execSQL(SQL_CREATE_SEARCH_TASK_DELETE_PROPERTY_TRIGGER);

		// create indices
		db.execSQL(TaskDatabaseHelper.createIndexString(FTS_NGRAM_TABLE, true, NGramColumns.TEXT));
		db.execSQL(TaskDatabaseHelper.createIndexString(FTS_CONTENT_TABLE, false, FTSContentColumns.NGRAM_ID));
		db.execSQL(TaskDatabaseHelper.createIndexString(FTS_CONTENT_TABLE, false, FTSContentColumns.TASK_ID));
		db.execSQL(TaskDatabaseHelper.createIndexString(FTS_CONTENT_TABLE, true, FTSContentColumns.PROPERTY_ID, FTSContentColumns.TASK_ID,
			FTSContentColumns.NGRAM_ID));

		db.execSQL(TaskDatabaseHelper.createIndexString(FTS_CONTENT_TABLE, true, FTSContentColumns.TYPE, FTSContentColumns.TASK_ID,
			FTSContentColumns.PROPERTY_ID));

	}


	/**
	 * Creates the FTS entries for the existing tasks.
	 * 
	 * @param db
	 *            The writable {@link SQLiteDatabase}.
	 */
	private static void initializeFTSContent(SQLiteDatabase db)
	{
		String[] task_projection = new String[] { Tasks._ID, Tasks.TITLE, Tasks.DESCRIPTION, Tasks.LOCATION };
		Cursor c = db.query(Tables.TASKS_PROPERTY_VIEW, task_projection, null, null, null, null, null);
		while (c.moveToNext())
		{
			insertTaskFTSEntries(db, c.getLong(0), c.getString(1), c.getString(2), c.getString(3));
		}
		c.close();
	}


	/**
	 * Inserts the searchable texts of the task in the database.
	 * 
	 * @param db
	 *            The writable {@link SQLiteDatabase}.
	 * 
	 * @param taskId
	 *            The row id of the task.
	 * @param title
	 *            The title of the task.
	 * @param description
	 *            The description of the task.
	 */
	private static void insertTaskFTSEntries(SQLiteDatabase db, long taskId, String title, String description, String location)
	{
		// title
		if (title != null && title.length() > 0)
		{
			updateEntry(db, taskId, -1, SearchableTypes.TITLE, title);
		}

		// location
		if (location != null && location.length() > 0)
		{
			updateEntry(db, taskId, -1, SearchableTypes.LOCATION, location);
		}

		// description
		if (description != null && description.length() > 0)
		{
			updateEntry(db, taskId, -1, SearchableTypes.DESCRIPTION, description);
		}

	}


	/**
	 * Updates the existing searchables entries for the task.
	 * 
	 * @param db
	 *            The writable {@link SQLiteDatabase}.
	 * @param task
	 *            The {@link TaskAdapter} containing the new values.
	 */
	public static void updateTaskFTSEntries(SQLiteDatabase db, TaskAdapter task)
	{
		// title
		if (task.isUpdated(TaskAdapter.TITLE))
		{
			updateEntry(db, task.id(), -1, SearchableTypes.TITLE, task.valueOf(TaskAdapter.TITLE));
		}

		// location
		if (task.isUpdated(TaskAdapter.LOCATION))
		{
			updateEntry(db, task.id(), -1, SearchableTypes.LOCATION, task.valueOf(TaskAdapter.LOCATION));
		}

		// description
		if (task.isUpdated(TaskAdapter.DESCRIPTION))
		{
			updateEntry(db, task.id(), -1, SearchableTypes.DESCRIPTION, task.valueOf(TaskAdapter.DESCRIPTION));
		}

	}


	/**
	 * Updates or creates the searchable entries for a property. Passing <code>null</code> as searchable text will remove the entry.
	 * 
	 * @param db
	 *            The writable {@link SQLiteDatabase}.
	 * @param taskId
	 *            the row id of the task this property belongs to.
	 * @param propertyId
	 *            the id of the property
	 * @param searchableText
	 *            the searchable text value of the property
	 */
	public static void updatePropertyFTSEntry(SQLiteDatabase db, long taskId, long propertyId, String searchableText)
	{
		updateEntry(db, taskId, propertyId, SearchableTypes.PROPERTY, searchableText);
	}


	/**
	 * Inserts NGrams into the NGram database.
	 * 
	 * @param db
	 *            A writable {@link SQLiteDatabase}.
	 * @param ngrams
	 *            The set of NGrams.
	 * @return The ids of the ngrams in the given set.
	 */
	private static Set<Long> insertNGrams(SQLiteDatabase db, Set<String> ngrams)
	{
		Set<Long> nGramIds = new HashSet<Long>(ngrams.size());
		ContentValues values = new ContentValues(1);
		for (String ngram : ngrams)
		{
			values.put(NGramColumns.TEXT, ngram);
			long nGramId = db.insertWithOnConflict(FTS_NGRAM_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			if (nGramId == -1)
			{
				// the docs say insertWithOnConflict returns the existing row id when CONFLICT_IGNORE is specified an the values conflict with an existing
				// column, however, that doesn't seem to work reliably, so we when for an error condition and get the row id ourselves
				Cursor c = db
					.query(FTS_NGRAM_TABLE, new String[] { NGramColumns.NGRAM_ID }, NGramColumns.TEXT + "=?", new String[] { ngram }, null, null, null);
				try
				{
					if (c.moveToFirst())
					{
						nGramId = c.getLong(0);
					}
				}
				finally
				{
					c.close();
				}

			}
			nGramIds.add(nGramId);
		}
		return nGramIds;

	}


	private static void updateEntry(SQLiteDatabase db, long taskId, long propertyId, int type, String searchableText)
	{
		// delete existing NGram relations
		deleteNGramRelations(db, taskId, propertyId, type);

		if (searchableText != null && searchableText.length() > 0)
		{
			// generate nGrams
			Set<String> propertyNgrams = TRIGRAM_GENERATOR.getNgrams(searchableText);

			TETRAGRAM_GENERATOR.getNgrams(propertyNgrams, searchableText);

			// insert ngrams
			Set<Long> propertyNgramIds = insertNGrams(db, propertyNgrams);

			// insert ngram relations
			insertNGramRelations(db, propertyNgramIds, taskId, propertyId, type);
		}
	}


	/**
	 * Inserts NGrams relations for a task entry.
	 * 
	 * @param db
	 *            A writable {@link SQLiteDatabase}.
	 * @param ngramIds
	 *            The set of NGram ids.
	 * @param taskId
	 *            The row id of the task.
	 * @param propertyId
	 *            The row id of the property.
	 * @param The
	 *            entry type of the relation (title, description, property).
	 */
	private static void insertNGramRelations(SQLiteDatabase db, Set<Long> ngramIds, long taskId, Long propertyId, int contentType)
	{
		ContentValues values = new ContentValues(4);
		for (Long ngramId : ngramIds)
		{
			values.put(FTSContentColumns.TASK_ID, taskId);
			values.put(FTSContentColumns.NGRAM_ID, ngramId);
			values.put(FTSContentColumns.TYPE, contentType);
			if (contentType == SearchableTypes.PROPERTY)
			{
				values.put(FTSContentColumns.PROPERTY_ID, propertyId);
			}
			else
			{
				values.putNull(FTSContentColumns.PROPERTY_ID);
			}
			db.insertWithOnConflict(FTS_CONTENT_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		}

	}


	/**
	 * Deletes the NGram relations of a task
	 * 
	 * @param db
	 *            The writable {@link SQLiteDatabase}.
	 * @param taskId
	 *            The task row id.
	 * @param propertyId
	 *            The property row id, ignored if <code>contentType</code> is not {@link SearchableTypes#PROPERTY}.
	 * @param contentType
	 *            The {@link SearchableTypes} type.
	 * @return The number of deleted relations.
	 */
	private static int deleteNGramRelations(SQLiteDatabase db, long taskId, long propertyId, int contentType)
	{
		StringBuilder whereClause = new StringBuilder(FTSContentColumns.TASK_ID).append(" = ").append(taskId);
		whereClause.append(" AND ").append(FTSContentColumns.TYPE).append(" = ").append(contentType);
		if (contentType == SearchableTypes.PROPERTY)
		{
			whereClause.append(" AND ").append(FTSContentColumns.PROPERTY_ID).append(" = ").append(propertyId);
		}
		return db.delete(FTS_CONTENT_TABLE, whereClause.toString(), null);
	}


	/**
	 * Queries the task database to get a cursor with the search results.
	 * 
	 * @param db
	 *            The {@link SQLiteDatabase}.
	 * @param searchString
	 *            The search query string.
	 * @param projection
	 *            The database projection for the query.
	 * @param selection
	 *            The selection for the query.
	 * @param selectionArgs
	 *            The arguments for the query.
	 * @param sortOrder
	 *            The sorting order of the query.
	 * @return A cursor of the task database with the search result.
	 */
	public static Cursor getTaskSearchCursor(SQLiteDatabase db, String searchString, String[] projection, String selection, String[] selectionArgs,
		String sortOrder)
	{

		StringBuilder selectionBuilder = new StringBuilder(1024);

		if (!TextUtils.isEmpty(selection))
		{
			selectionBuilder.append(" (");
			selectionBuilder.append(selection);
			selectionBuilder.append(") AND (");
		}
		else
		{
			selectionBuilder.append(" (");
		}

		Set<String> ngrams = TRIGRAM_GENERATOR.getNgrams(searchString);
		TETRAGRAM_GENERATOR.getNgrams(ngrams, searchString);

		String[] queryArgs;

		if (searchString != null && searchString.length() > 1)
		{

			selectionBuilder.append(NGramColumns.TEXT);
			selectionBuilder.append(" in (");

			for (int i = 0, count = ngrams.size(); i < count; ++i)
			{
				if (i > 0)
				{
					selectionBuilder.append(",");
				}
				selectionBuilder.append("?");

			}

			// selection arguments
			if (selectionArgs != null && selectionArgs.length > 0)
			{
				queryArgs = new String[selectionArgs.length + ngrams.size() + 1];
				queryArgs[0] = String.valueOf(ngrams.size());
				System.arraycopy(selectionArgs, 0, queryArgs, 1, selectionArgs.length);
				String[] ngramArray = ngrams.toArray(new String[ngrams.size()]);
				System.arraycopy(ngramArray, 0, queryArgs, selectionArgs.length + 1, ngramArray.length);
			}
			else
			{
				String[] temp = ngrams.toArray(new String[ngrams.size()]);

				queryArgs = new String[temp.length + 1];
				queryArgs[0] = String.valueOf(ngrams.size());
				System.arraycopy(temp, 0, queryArgs, 1, temp.length);
			}
			selectionBuilder.append(" ) ");
		}
		else
		{
			selectionBuilder.append(NGramColumns.TEXT);
			selectionBuilder.append(" like ?");

			// selection arguments
			if (selectionArgs != null && selectionArgs.length > 0)
			{
				queryArgs = new String[selectionArgs.length + 2];
				queryArgs[0] = String.valueOf(ngrams.size());
				System.arraycopy(selectionArgs, 0, queryArgs, 1, selectionArgs.length);
				queryArgs[queryArgs.length - 1] = " " + searchString + "%";
			}
			else
			{
				queryArgs = new String[2];
				queryArgs[0] = String.valueOf(ngrams.size());
				queryArgs[1] = " " + searchString + "%";
			}

		}

		selectionBuilder.append(") AND ");
		selectionBuilder.append(Tasks._DELETED);
		selectionBuilder.append(" = 0");

		if (sortOrder == null)
		{
			sortOrder = Tasks.SCORE + " desc";
		}
		else
		{
			sortOrder = Tasks.SCORE + " desc, " + sortOrder;
		}

		Cursor c = db.rawQueryWithFactory(null,
			String.format(SQL_RAW_QUERY_SEARCH_TASK, SQL_RAW_QUERY_SEARCH_TASK_DEFAULT_PROJECTION, selectionBuilder.toString(), sortOrder), queryArgs, null);
		return c;
	}
}
