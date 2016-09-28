package org.dmfs.tasks.share;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;


/**
 * Shares a task with other apps.
 *
 * @author Gabor Keszthelyi
 */
public interface TaskSharer
{
    /**
     * Shares the task contained in the {@link ContentSet} with other apps.
     */
    void share(ContentSet contentSet, Model model);
}
