package org.dmfs.tasks.share;

/**
 * @author Gabor Keszthelyi
 */
public abstract class StringTaskText implements TaskText
{
    private final String mValue;


    public StringTaskText(String value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("value cannot be null");
        }
        mValue = value;
    }

    @Override
    public final int length()
    {
        return mValue.length();
    }


    @Override
    public final char charAt(int index)
    {
        return mValue.charAt(index);
    }


    @Override
    public final CharSequence subSequence(int start, int end)
    {
        return mValue.subSequence(start, end);
    }


    @Override
    public String toString()
    {
        return mValue;
    }
}
