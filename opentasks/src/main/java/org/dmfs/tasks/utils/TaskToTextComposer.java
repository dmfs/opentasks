package org.dmfs.tasks.utils;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;


/**
 * @author Gabor Keszthelyi
 */
public interface TaskToTextComposer
{
    String title(ContentSet contentSet);

    String body(ContentSet contentSet, Model model);

}
