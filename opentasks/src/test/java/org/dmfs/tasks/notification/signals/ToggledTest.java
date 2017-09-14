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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Unit test for {@link Toggled}.
 *
 * @author Gabor Keszthelyi
 */
public final class ToggledTest
{
    @Test
    public void testValidFlags_dontThrowException()
    {
        new Toggled(Notification.DEFAULT_SOUND, true, new NoSignal()).value();
        new Toggled(Notification.DEFAULT_VIBRATE, true, new NoSignal()).value();
        new Toggled(Notification.DEFAULT_LIGHTS, true, new NoSignal()).value();
    }


    @Test(expected = IllegalArgumentException.class)
    public void testInValidFlag_throwsException()
    {
        new Toggled(15, true, new NoSignal()).value();
    }


    @Test
    public void testAddingFlag()
    {
        assertThat(new Toggled(Notification.DEFAULT_SOUND, true, new NoSignal()).value(), is(new NoSignal().value() | Notification.DEFAULT_SOUND));
        assertThat(new Toggled(Notification.DEFAULT_SOUND, false, new NoSignal()).value(), is(new NoSignal().value()));
    }
}
