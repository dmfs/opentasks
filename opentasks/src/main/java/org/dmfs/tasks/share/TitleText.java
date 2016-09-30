package org.dmfs.tasks.share;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.TaskFieldAdapters;


/**
 * {@link TaskText} for simply the title.
 *
 * @author Gabor Keszthelyi
 */
public class TitleText extends StringTaskText
{
    public TitleText(ContentSet contentSet)
    {
        super(TaskFieldAdapters.TITLE.get(contentSet));
    }
}
