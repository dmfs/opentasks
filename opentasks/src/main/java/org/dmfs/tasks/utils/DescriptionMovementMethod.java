package org.dmfs.tasks.utils;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;


/**
 * A movement method which allows moving the cursor with the arrow keys while still providing clickable links.
 * <p>
 * TODO: provide a way to act on entering or leaving a Clickable span with the cursor.
 */
public class DescriptionMovementMethod extends ArrowKeyMovementMethod
{
    @Override
    public boolean canSelectArbitrarily()
    {
        return true;
    }


    @Override
    public boolean onTouchEvent(TextView widget, Spannable spannable, MotionEvent event)
    {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN)
        {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] links = spannable.getSpans(off, off, ClickableSpan.class);

            if (links.length != 0)
            {
                ClickableSpan link = links[0];
                if (action == MotionEvent.ACTION_UP)
                {
                    if (link instanceof ClickableSpan)
                    {
                        link.onClick(widget);
                    }
                }
                else if (action == MotionEvent.ACTION_DOWN)
                {
                    Selection.setSelection(spannable, spannable.getSpanStart(link), spannable.getSpanEnd(link));
                }
                return true;
            }
            else
            {
                Selection.removeSelection(spannable);
            }
        }

        return super.onTouchEvent(widget, spannable, event);
    }


    public static MovementMethod getInstance()
    {
        if (sInstance == null)
        {
            sInstance = new DescriptionMovementMethod();
        }

        return sInstance;
    }


    private static DescriptionMovementMethod sInstance;
}
