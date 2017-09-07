/*
 * Copyright 2017 dmfs GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dmfs.tasks.notification.signals;

import android.app.Notification;

import org.junit.Test;

import static org.dmfs.tasks.utils.BitFlagUtils.containsFlag;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Unit test for {@link Toggled}.
 *
 * @author Gabor Keszthelyi
 */
public final class ToggledTest
{
    @Test
    public void testValidFlags()
    {
        new Toggled(Notification.DEFAULT_SOUND, true, new NoSignal());
        new Toggled(Notification.DEFAULT_VIBRATE, true, new NoSignal());
        new Toggled(Notification.DEFAULT_LIGHTS, true, new NoSignal());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testInValidFlag()
    {
        new Toggled(15, true, new NoSignal());
    }


    @Test
    public void testAddingFlag()
    {
        assertTrue(containsFlag(
                new Toggled(Notification.DEFAULT_SOUND, true, new NoSignal()).value(),
                Notification.DEFAULT_SOUND));

        assertFalse(containsFlag(
                new Toggled(Notification.DEFAULT_SOUND, false, new NoSignal()).value(),
                Notification.DEFAULT_SOUND));
    }
}
