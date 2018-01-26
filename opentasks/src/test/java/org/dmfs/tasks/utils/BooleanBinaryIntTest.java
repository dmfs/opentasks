/*
 * Copyright 2018 dmfs GmbH
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

package org.dmfs.tasks.utils;

import org.dmfs.optional.Present;
import org.junit.Test;

import static org.dmfs.optional.Absent.absent;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * Unit test for {@link BooleanBinaryInt}.
 *
 * @author Gabor Keszthelyi
 */
public final class BooleanBinaryIntTest
{

    @Test
    public void test()
    {
        assertThat(new BooleanBinaryInt(true).value(), is(1));
        assertThat(new BooleanBinaryInt(false).value(), is(0));
        assertThat(new BooleanBinaryInt((Boolean) null).value(), is(0));

        assertThat(new BooleanBinaryInt(new Present<>(true)).value(), is(1));
        assertThat(new BooleanBinaryInt(new Present<>(false)).value(), is(0));
        assertThat(new BooleanBinaryInt(absent()).value(), is(0));
    }

}