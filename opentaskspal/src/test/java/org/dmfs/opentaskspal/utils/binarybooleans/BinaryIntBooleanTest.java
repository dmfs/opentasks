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
 * Unit test for {@link BinaryIntBoolean}.
 *
 * @author Gabor Keszthelyi
 */
public final class BinaryIntBooleanTest
{

    @Test
    public void test()
    {
        assertThat(new BinaryIntBoolean(1).value(), is(true));
        assertThat(new BinaryIntBoolean(0).value(), is(false));
        assertThat(new BinaryIntBoolean(null).value(), is(false));

        // Invalid values, considered as false to avoid validation code:
        assertThat(new BinaryIntBoolean(-1).value(), is(false));
        assertThat(new BinaryIntBoolean(2).value(), is(false));
    }

}