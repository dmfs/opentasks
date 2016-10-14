package org.dmfs.tasks.model.adapters;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.dmfs.tasks.model.adapters.DescriptionStringFieldAdapter.extractDescription;


/**
 * Test for the description extraction from the combined description-checklist string.
 *
 * @author Gabor Keszthelyi
 */
public class DescriptionExtractingTest
{

    @Test
    public void testExtractDescription()
    {
        // No checklist
        assertEquals("desc", extractDescription("desc"));
        assertEquals("desc ", extractDescription("desc "));
        assertEquals("desc\n", extractDescription("desc\n"));

        assertEquals("des[c", extractDescription("des[c"));
        assertEquals("desc]", extractDescription("desc]"));
        assertEquals("[de]sc", extractDescription("[de]sc"));

        assertEquals("desc [ ] no newline", extractDescription("desc [ ] no newline"));

        // [ ]
        assertEquals("", extractDescription("[ ]"));
        assertEquals("", extractDescription("[ ][ ]"));
        assertEquals("", extractDescription("[ ]\n[ ]"));
        assertEquals("desc", extractDescription("desc\n[ ]"));
        assertEquals("[desc", extractDescription("[desc\n[ ]"));
        assertEquals("desc", extractDescription("desc\n[ ]hello"));
        assertEquals("desc ", extractDescription("desc \n[ ]"));
        assertEquals("desc", extractDescription("desc\n[ ]"));

        // [x]
        assertEquals("", extractDescription("[x]"));
        assertEquals("", extractDescription("[x][x]"));
        assertEquals("", extractDescription("[x]\n[x]"));
        assertEquals("desc", extractDescription("desc\n[x]"));
        assertEquals("[desc", extractDescription("[desc\n[x]"));
        assertEquals("desc", extractDescription("desc\n[x]hello"));
        assertEquals("desc ", extractDescription("desc \n[x]"));
        assertEquals("desc", extractDescription("desc\n[x]"));

        // [X]
        assertEquals("", extractDescription("[X]"));
        assertEquals("", extractDescription("[X]\n[X]"));
        assertEquals("desc", extractDescription("desc\n[X]"));
        assertEquals("[desc", extractDescription("[desc\n[X]"));
        assertEquals("desc", extractDescription("desc\n[X]hello"));
        assertEquals("desc ", extractDescription("desc \n[X]"));
        assertEquals("desc", extractDescription("desc\n[X]"));

        // []
        assertEquals("", extractDescription("[]"));
        assertEquals("", extractDescription("[][]"));
        assertEquals("", extractDescription("[]\n[]"));
        assertEquals("desc", extractDescription("desc\n[]"));
        assertEquals("[desc", extractDescription("[desc\n[]"));
        assertEquals("desc", extractDescription("desc\n[]hello"));
        assertEquals("desc ", extractDescription("desc \n[]"));
        assertEquals("desc", extractDescription("desc\n[]"));

        assertEquals("desc", extractDescription("desc\n[ ] item1"));
        assertEquals("desc", extractDescription("desc\n[x] item1\n[] item2"));
    }


    @Test
    public void testExtractDescription_CR_removal()
    {
        assertEquals("desc", extractDescription("desc\r\n[x] item"));
        assertEquals("desc ", extractDescription("desc \r\n[] item"));
    }

}