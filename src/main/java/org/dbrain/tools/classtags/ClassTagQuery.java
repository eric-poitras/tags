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

import org.dbrain.tools.classtags.impl.ClassTagEntry;
import org.dbrain.tools.classtags.impl.ClassTagUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Static methods to query tag databases built using the annotation processor.
 * <p>
 * If the class loader is not specified in a method, it's this class's class loader that is used.
 */
public class ClassTagQuery {

    private ClassLoader                      classLoader;
    private List<URL>                        externalResources;
    private Predicate<ClassTagEntry>         entryFilter;
    private Predicate<ClassTags>             filter;
    private Consumer<ClassNotFoundException> onClassLoadError;

    /**
     * Filter entries as read from the metadata info. Use this filter if you are interested only on
     * specific tags.
     */
    private ClassTagQuery filterEntries( Predicate<ClassTagEntry> entryFilter ) {
        this.entryFilter = entryFilter;
        return this;
    }

    /**
     * @return The class loader that should be used.
     */
    private ClassLoader getEffectiveClassLoader() {
        return classLoader != null ? classLoader : getClass().getClassLoader();
    }

    /**
     * Filter returned classes after tags have been grouped by class. Use this filter for more advanced
     * filters like classes having more than one tag or having one tag and not another.
     */
    public ClassTagQuery filter( Predicate<ClassTags> filter ) {
        this.filter = filter;
        return this;
    }

    /**
     * Defines class loaders to search for metadata info.
     *
     * @param cl
     * @return
     */
    public ClassTagQuery classLoader( ClassLoader cl ) {
        classLoader = cl;
        return this;
    }

    /**
     * Called when there is error on class loading.
     */
    public ClassTagQuery onClassNotFound( Consumer<ClassNotFoundException> e ) {
        onClassLoadError = e;
        return this;
    }

    /**
     * Add a user-defined entry file to be loaded.
     */
    public ClassTagQuery resource( URL resource ) {
        if ( externalResources == null ) {
            externalResources = new ArrayList<>();
        }
        externalResources.add( resource );
        return this;
    }

    /**
     * Load all entries and return it in a set.
     */
    private Set<ClassTagEntry> getEntries() throws IOException {
        List<URL> resources = ClassTagUtils.listResources( getEffectiveClassLoader() );
        // Add external resources, if any.
        if ( externalResources != null ) {
            resources.addAll( externalResources );
        }
        return ClassTagUtils.loadEntries( resources, new HashSet<>(), entryFilter );
    }

    /**
     * Aggregate the entries in classes and filter them if necessary.
     */
    public Map<String, ClassTags> mapTagsByClassName() throws IOException {
        Set<ClassTagEntry> entries = getEntries();

        Map<String, ClassTags> result = new HashMap<>( entries.size() );
        for ( ClassTagEntry e : entries ) {
            ClassTags tags = result.get( e.getClassName() );
            if ( tags == null ) {
                tags = new ClassTags( e.getClassName(), new HashSet<>() );
                result.put( e.getClassName(), tags );
            }
            tags.getTags().add( e.getTagName() );
        }

        // Filter the tags
        if ( filter != null ) {
            for ( ClassTags e : new ArrayList<>( result.values() ) ) {
                if ( !filter.test( e ) ) {
                    result.remove( e.getClassName() );
                }
            }
        }

        return result;
    }

    /**
     * List the tags.
     */
    public List<ClassTags> listAsClassTags() throws IOException {
        List<ClassTags> result = new ArrayList<>( mapTagsByClassName().values() );
        result.sort( Comparator.comparing( tags -> tags.getClassName() ) );
        return result;
    }

    /**
     * List the classes.
     */
    public List<String> listAsClassNames() throws IOException {
        return mapTagsByClassName().values()
                                   .stream()
                                   .map( tags -> tags.getClassName() )
                                   .sorted()
                                   .collect( Collectors.toList() );
    }

    /**
     * List the classes that match the query and loads without error.
     */
    public List<Class> list() throws Exception {
        return mapTagsByClassName().values()
                                   .stream()
                                   .map( tags -> ClassTagUtils.loadClass( getEffectiveClassLoader(),
                                                                          tags.getClassName(),
                                                                          onClassLoadError ) )
                                   .filter( ( c ) -> c != null )
                                   .collect( Collectors.toList() );
    }


    /**
     * Query all classes tagged with the specific tag.
     */
    public static List<String> listClassNameByTag( Class<? extends Annotation> tagAnnotation ) throws Exception {
        return new ClassTagQuery().filterEntries( entry -> entry.getTagName().equals( tagAnnotation.getName() ) )
                                  .listAsClassNames();
    }

    /**
     * Query all classes tagged with the specific tag.
     */
    public static List<Class> listByTag( Class<? extends Annotation> tagAnnotation ) throws Exception {
        return new ClassTagQuery().filterEntries( entry -> entry.getTagName().equals( tagAnnotation.getName() ) )
                                  .list();
    }


}
