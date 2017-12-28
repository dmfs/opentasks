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

package org.dmfs.opentaskspal.utils.binarybooleans;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * Unit test for {@link BinaryLongBoolean}.
 *
 * @author Gabor Keszthelyi
 */
public final class BinaryLongBooleanTest
{

    @Test
    public void test()
    {
        assertThat(new BinaryLongBoolean(1L).value(), is(true));
        assertThat(new BinaryLongBoolean(0L).value(), is(false));
        assertThat(new BinaryLongBoolean(null).value(), is(false));

        // Invalid values, considered as false to avoid validation code:
        assertThat(new BinaryLongBoolean(-1L).value(), is(false));
        assertThat(new BinaryLongBoolean(2L).value(), is(false));
    }

}