package org.dmfs.tasks.utils;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


/**
 * Helper class to record recently used lists in order to provide a pre-selection to the user.
 */
public class RecentlyUsedLists
{
    public static final String PREFERENCE_KEY = "RecentlyUsedLists";


    /**
     * Gets the lists of TaskLists ordered by recently use.
     *
     * @param context
     *         Context
     *
     * @return List of TaskLists where the most recently used list is on position 0.
     */
    public static List<Long> getList(Context context)
    {
        String strLists = PreferenceManager.getDefaultSharedPreferences(context).getString(PREFERENCE_KEY, "");
        Log.v(RecentlyUsedLists.class.getSimpleName(), "getList:  " + strLists);
        List<Long> lists = new ArrayList<>();
        if (strLists.length() > 0)
        {
            for (String lid : strLists.split(","))
            {
                lists.add(Long.parseLong(lid));
            }
        }
        return lists;
    }


    /**
     * Saves the ordered lists of TaskLists.
     *
     * @param context
     *         Context
     * @param lists
     *         List of TaskLists where the most recently used list is on position 0.
     */
    private static void setList(Context context, List<Long> lists)
    {
        String strLists = TextUtils.join(",", lists);
        Log.v(RecentlyUsedLists.class.getSimpleName(), "setList:  " + strLists);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREFERENCE_KEY, strLists).commit();
    }


    /**
     * Searches for the best suitable TaskList in dependence of which is most recently used.
     *
     * @param context
     *         Context
     * @param allowedLists
     *         List of TaskLists
     *
     * @return The most recently used TaskLists from <code>allowedLists</code>
     */
    public static Long getRecentFromList(Context context, List<Long> allowedLists)
    {
        List<Long> recentlyLists = getList(context);
        for (Long listId : recentlyLists)
        {
            if (allowedLists.contains(listId))
            {
                return listId;
            }
        }
        return allowedLists.get(0);
    }


    /**
     * Mark a TaskLists as "used", which means that as new task was just created.
     *
     * @param context
     *         Context
     * @param listId
     *         Id of the TaskList, where a task was just created.
     */
    public static void use(Context context, Long listId)
    {
        List<Long> lists = getList(context);
        lists.remove(listId); // does nothing, if "listId" is not in "lists"
        lists.add(0, listId);
        setList(context, lists);
    }
}
