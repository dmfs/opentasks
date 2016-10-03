package org.dmfs.tasks.share;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.ShareActionProvider;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;


/**
 * Creates intent for sharing a whole task's text description with other apps.
 *
 * @author Gabor Keszthelyi
 */
public class ShareIntentFactory
{
    private final TaskTextDescriptionComposer mTextComposer;


    public ShareIntentFactory(Context context)
    {
        mTextComposer = new BasicTaskTextDescriptionComposer(context);
    }


    /**
     * Creates an intent for sharing the description of the whole task in the {@link ContentSet} with other apps.
     *
     * @param contentSet
     *         actual {@link ContentSet} for the task
     * @param model
     *         the model used currently
     *
     * @return the created intent
     */
    public Intent createTaskTextShareIntent(ContentSet contentSet, Model model)
    {
        String title = mTextComposer.title(contentSet, model);
        String body = mTextComposer.body(contentSet, model);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        sendIntent.putExtra(Intent.EXTRA_TEXT, body);
        sendIntent.setType("text/plain");

        return sendIntent;
    }


    /**
     * Convenience method to set the share intent on the {@link ShareActionProvider} on a background thread, to not
     * block UI thread while text composing is ongoing.
     *
     * @param shareActionProvider
     *         it's {@link ShareActionProvider#setShareIntent(Intent)} will be called with the created Intent
     * @param contentSet
     *         actual {@link ContentSet} for the task
     * @param model
     *         the model used currently
     */
    public void setShareIntentAsync(final ShareActionProvider shareActionProvider, final ContentSet contentSet, final Model model)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                shareActionProvider.setShareIntent(createTaskTextShareIntent(contentSet, model));
            }
        }).start();
    }
}
