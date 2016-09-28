package org.dmfs.tasks.share;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;


/**
 * Creates a text description for a task.
 *
 * @author Gabor Keszthelyi
 */
public interface TaskTextDescriptionComposer
{
    /**
     * Creates a title for the task text description.
     */
    String title(ContentSet contentSet, Model model);

    /**
     * Creates the body of the task text description.
     */
    String body(ContentSet contentSet, Model model);

}
