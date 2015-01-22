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

package org.dbrain.tools.classtags;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Contains the tags over a single class.
 */
public class ClassTags {

    private final String      className;
    private final Set<String> tags;

    public ClassTags( String className, Set<String> tags ) {
        this.className = className;
        this.tags = tags;
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
    public Set<String> getTags() {
        return tags;
    }

    /**
     * True if the class has the specific tag.
     */
    public boolean containsTag( String tagName ) {
        return tagName != null && tags.contains( tagName );
    }

    /**
     * True if the class has the specific tag.
     */
    public boolean containsTag( Class<? extends Annotation> tag ) {
        return tag != null && tags.contains( tag.getName() );
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        ClassTags impl = (ClassTags) o;

        if ( !className.equals( impl.className ) ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return className.hashCode();
    }

    @Override
    public String toString() {
        return className + ':' + tags;
    }

}
