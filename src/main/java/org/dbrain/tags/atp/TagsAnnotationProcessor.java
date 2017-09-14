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
package org.dbrain.tags.atp;

import org.dbrain.tags.Tag;
import org.dbrain.tags.config.Configuration;
import org.dbrain.tags.impl.TagEntry;
import org.dbrain.tags.impl.TagUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/** Tag annotation processor. */
@SupportedAnnotationTypes(value = {"*"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SuppressWarnings("unused") // Used by compiler :)
public class TagsAnnotationProcessor extends AbstractProcessor {

  private Map<String, Set<TagEntry>> tagByClasses;
  private Set<String> classesToValidate;
  private Elements elements;
  private Types types;
  private Configuration config = new Configuration();

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    try (InputStream is = getConfigFileForRead()) {
      Configuration.load(config, is);
    } catch (FileNotFoundException e) {
      log(Diagnostic.Kind.NOTE, "Tags configuration file not found.");
    } catch (IOException e) {
      log(Diagnostic.Kind.ERROR, "Error while processing tags configuration:" + e.getMessage());
    }

    elements = processingEnv.getElementUtils();
    types = processingEnv.getTypeUtils();

    processingEnv.getTypeUtils();
    log(Diagnostic.Kind.NOTE, "Initializing the tag annotation processor.");
    try {
      try (InputStream is = getTagFileForRead()) {
        tagByClasses = mapTagByClass(TagUtils.loadTagEntries(is));
        classesToValidate = new HashSet<>(tagByClasses.keySet());
        log(
            Diagnostic.Kind.NOTE,
            "Incremental compilation:" + tagByClasses.size() + " entries loaded.");
      } catch (FileNotFoundException e) {
        log(Diagnostic.Kind.NOTE, "Tag file not found.");
        tagByClasses = new HashMap<>();
      }
    } catch (Throwable t) {
      log(Diagnostic.Kind.ERROR, t.getMessage());
    }
  }

  private InputStream getTagFileForRead() throws IOException {
    FileObject f =
        processingEnv
            .getFiler()
            .getResource(StandardLocation.CLASS_OUTPUT, "", TagUtils.TAG_FILE_NAME);
    log(Diagnostic.Kind.NOTE, "Reading tags from " + f.toUri());
    return f.openInputStream();
  }

  private InputStream getConfigFileForRead() throws IOException {
    FileObject f =
        processingEnv
            .getFiler()
            .getResource(StandardLocation.CLASS_OUTPUT, "", TagUtils.CONFIG_FILE_NAME);
    log(Diagnostic.Kind.NOTE, "Reading configuration from " + f.toUri());
    return f.openInputStream();
  }

  private Map<String, Set<TagEntry>> mapTagByClass(Set<TagEntry> tags) {
    Map<String, Set<TagEntry>> result = new HashMap<>();
    for (TagEntry tag : tags) {
      if (!result.containsKey(tag.getClassName())) {
        result.put(tag.getClassName(), new HashSet<>());
      }
      result.get(tag.getClassName()).add(tag);
    }
    return result;
  }

  private OutputStream getTagFileForWrite() throws IOException {
    try {
      FileObject f =
          processingEnv
              .getFiler()
              .getResource(StandardLocation.CLASS_OUTPUT, "", TagUtils.TAG_FILE_NAME);
      f.delete();
    } catch (FileNotFoundException e) {
      log(Diagnostic.Kind.NOTE, "File did not exists: " + e.getMessage());
    }
    FileObject f =
        processingEnv
            .getFiler()
            .createResource(StandardLocation.CLASS_OUTPUT, "", TagUtils.TAG_FILE_NAME);
    log(Diagnostic.Kind.NOTE, "Writing to " + f.toUri());
    return f.openOutputStream();
  }

  /** Log a message. */
  private void log(Diagnostic.Kind msgKind, String s) {
    processingEnv.getMessager().printMessage(msgKind, getClass().getSimpleName() + ": " + s);
  }

  /**
   * Return a loadable class name from a TypeElement. A loadable class name have correct syntax for
   * inner classes, like : mypackage.MyClass$InnerCLass.
   */
  public static String getLoadableClassName(TypeElement typeElement) {
    switch (typeElement.getNestingKind()) {
      case TOP_LEVEL:
        return typeElement.getQualifiedName().toString();
      case MEMBER:
      case LOCAL:
        return getLoadableClassName((TypeElement) typeElement.getEnclosingElement())
            + "$"
            + typeElement.getSimpleName();
      default:
        throw new IllegalStateException();
    }
  }

  /**
   * Get tags from a specific element.
   *
   * @param loadableClassName The fully qualified java class name. Used to build TagEntry
   * @param e The TypeElement to read tags from.
   * @return The tags from a TypedElement or null if there is none.
   */
  public Set<TagEntry> getTagsFrom(String loadableClassName, TypeElement e) {
    Set<TagEntry> result = null;
    Set<TypeElement> elementsToScan = new HashSet<>();

    // Get tags from all annotations
    for (AnnotationMirror am : elements.getAllAnnotationMirrors(e)) {
      TypeElement te = asTypeElement(am.getAnnotationType());
      if (te != null) {
        elementsToScan.add(te);
      }
    }

    // Get tags from interfaces as well
    listAllInterfacesTo(e, elementsToScan);

    // Loop on element to scan
    for (TypeElement typeElement : elementsToScan) {

      String tagLoadableClassName = getLoadableClassName(typeElement);
      boolean include = config.getExternalTags().contains(tagLoadableClassName);
      if (!include) include = typeElement.getAnnotation(Tag.class) != null;

      // Is this annotation a tag anotation ?
      if (include) {
        if (result == null) {
          result = new HashSet<>();
        }
        result.add(new TagEntry(loadableClassName, tagLoadableClassName));
      }
    }
    return result;
  }

  public TypeElement asTypeElement(TypeMirror tm) {
    if (tm instanceof DeclaredType) {
      Element element = ((DeclaredType) tm).asElement();
      if (element instanceof TypeElement) {
        return (TypeElement) element;
      }
    }
    return null;
  }

  public void listAllInterfacesTo(TypeElement e, Set<TypeElement> to) {
    if (e != null) {
      // List all interfaces for this class
      for (TypeMirror tm : e.getInterfaces()) {
        TypeElement te = asTypeElement(tm);
        if (te != null) {
          to.add(te);

          // List all super-interfaces
          listAllInterfacesTo(te, to);
        }
      }

      // Continue to super-class if needed.
      if (e.getSuperclass() != null) {
        listAllInterfacesTo(asTypeElement(e.getSuperclass()), to);
      }
    }
  }

  /** Check if the element still belong to the list or not. */
  private void validateThisClass(Element element) throws Exception {
    if (element instanceof TypeElement) {
      TypeElement typeElement = (TypeElement) element;
      String className = getLoadableClassName(typeElement);
      Set<TagEntry> tags = getTagsFrom(className, typeElement);

      if (tags != null && !tagByClasses.keySet().contains(className)) {
        // Found a new tagged class.
        tagByClasses.put(className, tags);
      } else if (tags != null) {
        // Updating tagged class.
        tagByClasses.put(className, tags);
      } else if (tagByClasses.keySet().contains(className)) {
        // Found a class that is no longer tagged.
        tagByClasses.remove(className);
      }

      // To validate classes.
      if (classesToValidate != null) {
        classesToValidate.remove(className);
      }
    }
  }

  /** Check an element and all the enclosed elements. */
  private void validateClass(Element element) throws Exception {

    // Check the element itself.
    validateThisClass(element);

    // Check enclosed elements
    List<? extends Element> childElements = element.getEnclosedElements();
    if (childElements != null) {
      for (Element e : childElements) {
        validateClass(e);
      }
    }
  }

  /**
   * Write the results back to the annotation list file. Adds any new classes remove classes marked
   * for removal.
   */
  public void complete() throws Exception {

    if (classesToValidate != null && classesToValidate.size() > 0) {
      log(Diagnostic.Kind.NOTE, "Validating classes that has not been compiled.");
      for (String className : new HashSet<>(classesToValidate)) {
        try {
          TypeElement element = elements.getTypeElement(className);
          if (element == null) {
            //            log(Diagnostic.Kind.NOTE, "Class not found: " + className);
            tagByClasses.remove(className);
          } else {
            validateClass(element);
          }
        } catch (Exception e) {
          log(Diagnostic.Kind.ERROR, "Error validating class: " + className);
        }
      }
    }

    try (OutputStream os = getTagFileForWrite()) {
      Set<TagEntry> finalSet = new HashSet<>();
      tagByClasses.values().forEach(set -> finalSet.addAll(set));
      TagUtils.writeClassTags(finalSet, os);
    }

    log(Diagnostic.Kind.NOTE, "Completed class tags annotation processing.");
  }

  /** Process all classes and search for tags. */
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    try {

      // Loop compiling
      if (!roundEnv.processingOver()) {

        for (Element element : roundEnv.getRootElements()) {
          validateClass(element);
        }

      } else {
        complete();
      }

    } catch (Throwable t) {
      log(Diagnostic.Kind.ERROR, t.getMessage());
      log(Diagnostic.Kind.ERROR, this.toString());
    }
    return false;
  }
}
