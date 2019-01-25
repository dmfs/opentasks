/*
 * Copyright 2019 dmfs GmbH
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

package org.dmfs.provider.tasks.utils;

import org.dmfs.iterables.elementary.Seq;
import org.junit.Test;

import static org.dmfs.jems.hamcrest.matchers.BrokenFragileMatcher.isBroken;
import static org.dmfs.jems.hamcrest.matchers.IterableMatcher.iteratesTo;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * @author Marten Gajda
 */
public class ChunkedTest
{
    @Test
    public void test()
    {
        // error case, illegal chnuk size
        assertThat(() -> new Chunked<>(-1, new Seq<>(1)), isBroken(IllegalArgumentException.class));
        assertThat(() -> new Chunked<>(0, new Seq<>(1)), isBroken(IllegalArgumentException.class));

        // edge case chunk size 1
        assertThat(new Chunked<>(1, new Seq<>()), is(emptyIterable()));
        assertThat(new Chunked<>(1, new Seq<>(1)), iteratesTo(iteratesTo(1)));
        assertThat(new Chunked<>(1, new Seq<>(1, 2, 3)), iteratesTo(iteratesTo(1), iteratesTo(2), iteratesTo(3)));

        // regular case chunk size >1
        assertThat(new Chunked<>(3, new Seq<>()), is(emptyIterable()));
        assertThat(new Chunked<>(3, new Seq<>(1)), iteratesTo(iteratesTo(1)));
        assertThat(new Chunked<>(3, new Seq<>(1, 2, 3)), iteratesTo(iteratesTo(1, 2, 3)));
        assertThat(new Chunked<>(3, new Seq<>(1, 2, 3, 4)), iteratesTo(iteratesTo(1, 2, 3), iteratesTo(4)));
        assertThat(new Chunked<>(3, new Seq<>(1, 2, 3, 4, 5, 6)), iteratesTo(iteratesTo(1, 2, 3), iteratesTo(4, 5, 6)));
        assertThat(new Chunked<>(3, new Seq<>(1, 2, 3, 4, 5, 6, 7)), iteratesTo(iteratesTo(1, 2, 3), iteratesTo(4, 5, 6), iteratesTo(7)));
    }
}