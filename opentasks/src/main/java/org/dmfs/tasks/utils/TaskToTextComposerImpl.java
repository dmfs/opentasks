package org.dmfs.tasks.utils;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.Model;

import java.util.List;


/**
 * @author Gabor Keszthelyi
 */
public class TaskToTextComposerImpl implements TaskToTextComposer
{
    @Override
    public String title(Model model, ContentSet contentSet)
    {
        return "This will be the title";
    }


    @Override
    public String body(Model model, ContentSet contentSet)
    {
        StringBuilder sb = new StringBuilder();
        List<FieldDescriptor> fields = model.getFields();
        for (FieldDescriptor field : fields)
        {
            Object o = field.getFieldAdapter().get(contentSet);
            if (o != null)
            {
                sb.append(field.getTitle()).append(": ").append(o.toString()).append("\n");
            }
        }
        return sb.toString();
    }
}
