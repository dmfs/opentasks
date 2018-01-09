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

package org.dmfs.provider.tasks.utils.iterable;

import org.dmfs.iterables.EmptyIterable;
import org.dmfs.iterables.elementary.Seq;
import org.dmfs.jems.iterable.generators.ConstantGenerator;
import org.junit.Test;

import static org.dmfs.jems.hamcrest.matchers.IterableMatcher.iteratesTo;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertThat;


/**
 * @author Marten Gajda
 */
public class AlternatingTest
{
    @Test
    public void testEmpty() throws Exception
    {
        assertThat(new Alternating<>(EmptyIterable.instance(), EmptyIterable.instance()), emptyIterable());
        assertThat(new Alternating<>(new Seq<>("a"), EmptyIterable.instance()), emptyIterable());
        assertThat(new Alternating<>(new Seq<>("a", "b"), EmptyIterable.instance()), emptyIterable());
        assertThat(new Alternating<>(new Seq<>("a", "b", "c"), EmptyIterable.instance()), emptyIterable());
        assertThat(new Alternating<>(EmptyIterable.instance(), new Seq<>("1")), emptyIterable());
        assertThat(new Alternating<>(EmptyIterable.instance(), new Seq<>("1", "2")), emptyIterable());
        assertThat(new Alternating<>(EmptyIterable.instance(), new Seq<>("1", "2", "3")), emptyIterable());
    }


    @Test
    public void testNonEmpty() throws Exception
    {
        assertThat(new Alternating<>(new Seq<>("a"), new Seq<>("1")), iteratesTo("a"));
        assertThat(new Alternating<>(new Seq<>("a", "b"), new Seq<>("1")), iteratesTo("a", "1"));
        assertThat(new Alternating<>(new Seq<>("a", "b", "c"), new Seq<>("1")), iteratesTo("a", "1"));
        assertThat(new Alternating<>(new Seq<>("a"), new Seq<>("1")), iteratesTo("a"));
        assertThat(new Alternating<>(new Seq<>("a"), new Seq<>("1", "2")), iteratesTo("a"));
        assertThat(new Alternating<>(new Seq<>("a"), new Seq<>("1", "2", "3")), iteratesTo("a"));

        assertThat(new Alternating<>(new Seq<>("a"), new Seq<>("1", "2")), iteratesTo("a"));
        assertThat(new Alternating<>(new Seq<>("a", "b"), new Seq<>("1", "2")), iteratesTo("a", "1", "b"));
        assertThat(new Alternating<>(new Seq<>("a", "b", "c"), new Seq<>("1", "2")), iteratesTo("a", "1", "b", "2"));
        assertThat(new Alternating<>(new Seq<>("a", "b"), new Seq<>("1")), iteratesTo("a", "1"));
        assertThat(new Alternating<>(new Seq<>("a", "b"), new Seq<>("1", "2")), iteratesTo("a", "1", "b"));
        assertThat(new Alternating<>(new Seq<>("a", "b"), new Seq<>("1", "2", "3")), iteratesTo("a", "1", "b"));

        assertThat(new Alternating<>(new Seq<>("a"), new Seq<>("1", "2", "3")), iteratesTo("a"));
        assertThat(new Alternating<>(new Seq<>("a", "b"), new Seq<>("1", "2", "3")), iteratesTo("a", "1", "b"));
        assertThat(new Alternating<>(new Seq<>("a", "b", "c"), new Seq<>("1", "2", "3")), iteratesTo("a", "1", "b", "2", "c"));
        assertThat(new Alternating<>(new Seq<>("a", "b", "c"), new Seq<>("1")), iteratesTo("a", "1"));
        assertThat(new Alternating<>(new Seq<>("a", "b", "c"), new Seq<>("1", "2")), iteratesTo("a", "1", "b", "2"));
        assertThat(new Alternating<>(new Seq<>("a", "b", "c"), new Seq<>("1", "2", "3")), iteratesTo("a", "1", "b", "2", "c"));
    }


    @Test
    public void testGenerator() throws Exception
    {
        assertThat(new Alternating<>(new Seq<>("a"), new ConstantGenerator<>(",")), iteratesTo("a"));
        assertThat(new Alternating<>(new Seq<>("a", "b"), new ConstantGenerator<>(",")), iteratesTo("a", ",", "b"));
        assertThat(new Alternating<>(new Seq<>("a", "b", "c"), new ConstantGenerator<>(",")), iteratesTo("a", ",", "b", ",", "c"));
    }

}