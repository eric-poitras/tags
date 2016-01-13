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

package org.dbrain.tags.impl;

import java.util.Objects;

/**
 * Single entry in the meta-info database.
 */
public class TagEntry {

    private final String      className;
    private final String      tagName;

    public TagEntry( String className, String tagName ) {
        Objects.nonNull( className );
        Objects.nonNull( tagName );
        this.className = className;
        this.tagName = tagName;
    }

    /**
     * @return The name of the class.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return The name of the tag's class.
     */
    public String getTagName() {
        return tagName;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        TagEntry impl = (TagEntry) o;

        if ( !className.equals( impl.className ) ) return false;
        if ( !tagName.equals( impl.tagName ) ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = className.hashCode();
        result = 31 * result + tagName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return className + ':' + tagName;
    }


}
