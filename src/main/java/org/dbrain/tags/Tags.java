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

package org.dbrain.tags;

import org.dbrain.tags.impl.TagEntry;
import org.dbrain.tags.impl.TagUtils;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Tags API. */
public class Tags {

  /** Query all classes, including interfaces and abstract classes, tagged with the specific tag. */
  public static List<Class> listAllClassByTag(Class<?> tagIntfOrAnnotation) throws Exception {
    return new Query().filter(tagIntfOrAnnotation).listAllClass();
  }

  /** Query all concrete classes tagged with the specific tag. */
  public static List<Class> listClassByTag(Class<?> tagIntfOrAnnotation) throws Exception {
    return new Query().filter(tagIntfOrAnnotation).listClass();
  }

  /** @return A query builder. */
  public static Query query() {
    return new Query();
  }

  /**
   * Static methods to query tag databases built using the annotation processor.
   *
   * <p>If the class loader is not specified in a method, it's this class's class loader that is
   * used.
   */
  public static class Query {

    private ClassLoader classLoader;
    private List<URL> externalResources;
    private Predicate<ClassTags> filter;
    private Consumer<ClassNotFoundException> onClassLoadError;

    private Query() {}

    /** @return The class loader that should be used. */
    private ClassLoader getEffectiveClassLoader() {
      return classLoader != null ? classLoader : getClass().getClassLoader();
    }

    /** Filter returned classes that have the specific tag * */
    public Query filter(Class<?> tagClass) {
      return filter(tag -> tag.containsTag(tagClass));
    }

    /** Filter returned classes that have the specific tag * */
    public Query filter(String tagName) {
      return filter(tag -> tag.containsTag(tagName));
    }
    /**
     * Filter returned classes after tags have been grouped by class. Use this filter for more
     * advanced filters like classes having more than one tag or having one tag and not another.
     */
    public Query filter(Predicate<ClassTags> filter) {
      if (this.filter != null) {
        Predicate<ClassTags> existingFilter = this.filter;
        this.filter = e -> existingFilter.test(e) && filter.test(e);
      } else {
        this.filter = filter;
      }
      return this;
    }

    /**
     * Defines class loaders to search for metadata info.
     *
     * @param cl
     * @return
     */
    public Query classLoader(ClassLoader cl) {
      classLoader = cl;
      return this;
    }

    /** Called when there is error on class loading. */
    public Query onClassNotFound(Consumer<ClassNotFoundException> e) {
      onClassLoadError = e;
      return this;
    }

    /** Add a user-defined entry file to be loaded. */
    public Query resource(URL resource) {
      if (externalResources == null) {
        externalResources = new ArrayList<>();
      }
      externalResources.add(resource);
      return this;
    }

    /** Load all entries and return it in a set. */
    private Set<TagEntry> loadEntries() throws IOException {
      List<URL> resources = TagUtils.listTagFileResources(getEffectiveClassLoader());
      // Add external resources, if any.
      if (externalResources != null) {
        resources.addAll(externalResources);
      }
      return TagUtils.loadTagEntries(resources, new HashSet<>());
    }

    /** Aggregate the entries in classes and filter them if necessary. */
    public List<ClassTags> listClassTags() throws IOException {
      Set<TagEntry> entries = loadEntries();

      // Build the class tags
      Map<String, ClassTags> classTagsMap = new HashMap<>(entries.size());
      for (TagEntry e : entries) {
        ClassTags tags = classTagsMap.get(e.getClassName());
        if (tags == null) {
          tags = new ClassTags(e.getClassName(), new HashSet<>());
          classTagsMap.put(e.getClassName(), tags);
        }
        tags.getTags().add(e.getTagName());
      }

      // Filter the class tags
      ArrayList<ClassTags> result = new ArrayList<>(classTagsMap.size());
      if (filter != null) {
        for (ClassTags e : new ArrayList<>(classTagsMap.values())) {
          if (filter.test(e)) {
            result.add(e);
          }
        }
      }

      return result;
    }

    /** List the classes. */
    public List<String> listClassNames() throws IOException {
      return listClassTags()
          .stream()
          .map(tags -> tags.getClassName())
          .sorted()
          .collect(Collectors.toList());
    }

    /** List the classes or interfaces that match the query and loads without error. */
    public List<Class> listAllClass() throws Exception {
      return listClassTags()
          .stream()
          .map(
              tags ->
                  TagUtils.loadClass(
                      getEffectiveClassLoader(), tags.getClassName(), onClassLoadError)) //
          .filter((c) -> c != null) //
          .collect(Collectors.toList()); //
    }

    /** List the concrete class that match the query and loads without error. */
    public List<Class> listClass() throws Exception {
      return listClassTags() //
          .stream() //
          .map(
              tags ->
                  TagUtils.loadClass(
                      getEffectiveClassLoader(), tags.getClassName(), onClassLoadError)) //
          .filter(
              (c) -> c != null && !c.isInterface() && !Modifier.isAbstract(c.getModifiers())) //
          .collect(Collectors.toList()); //
    }
  }

  /** Contains the tags over a single class. */
  public static class ClassTags {

    private final String className;
    private final Set<String> tags;

    public ClassTags(String className, Set<String> tags) {
      this.className = className;
      this.tags = tags;
    }

    /** @return The name of the class. */
    public String getClassName() {
      return className;
    }

    /** @return The name of the tag's class. */
    public Set<String> getTags() {
      return tags;
    }

    /** True if the class has the specific tag. */
    public boolean containsTag(String tagName) {
      return tagName != null && tags.contains(tagName);
    }

    /** True if the class has the specific tag. */
    public boolean containsTag(Class<?> tag) {
      return tag != null && tags.contains(tag.getName());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      ClassTags impl = (ClassTags) o;

      if (!className.equals(impl.className)) return false;

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
}
