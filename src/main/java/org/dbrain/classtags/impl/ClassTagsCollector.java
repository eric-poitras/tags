/*
 * Copyright [2015] [Eric Poitras]
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package org.dbrain.classtags.impl;

import org.dbrain.classtags.ClassTags;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Created by epoitras on 02/01/15.
 */
public class ClassTagsCollector implements Collector<ClassTagEntry, ClassTags, ClassTags> {

    @Override
    public Supplier<ClassTags> supplier() {
        return null;
    }

    @Override
    public BiConsumer<ClassTags, ClassTagEntry> accumulator() {
        return null;
    }

    @Override
    public BinaryOperator<ClassTags> combiner() {
        return null;
    }

    @Override
    public Function<ClassTags, ClassTags> finisher() {
        return null;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return null;
    }
}
