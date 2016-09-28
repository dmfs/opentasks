package org.dmfs.tasks.share;

import android.content.Context;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;


/**
 * Shares the text description of the whole task with other apps.
 *
 * @author Gabor Keszthelyi
 */
public class TextDescriptionTaskSharer implements TaskSharer
{
    private final TaskTextDescriptionComposer mTextComposer;
    private final TextSharer mTextSharer;


    public TextDescriptionTaskSharer(Context context)
    {
        mTextComposer = new BasicTaskTextDescriptionComposer(context);
        mTextSharer = new TextSharer(context);
    }


    @Override
    public void share(ContentSet contentSet, Model model)
    {
        String title = mTextComposer.title(contentSet, model);
        String body = mTextComposer.body(contentSet, model);
        mTextSharer.share(title, body);
    }
}
