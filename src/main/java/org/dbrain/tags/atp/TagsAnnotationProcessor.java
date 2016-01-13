/*
 * Copyright 2012 8D Technologies, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of 8D Technologies, Inc.
 * Use is subject to license terms.
 *
 */
package org.dbrain.tags.atp;


import org.dbrain.tags.Tag;
import org.dbrain.tags.impl.TagEntry;
import org.dbrain.tags.impl.TagUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tag annotation processor.
 */
@SupportedAnnotationTypes( value = { "*" } )
@SupportedSourceVersion( SourceVersion.RELEASE_8 )
@SuppressWarnings( "unused" ) // Used by compiler :)
public class TagsAnnotationProcessor extends AbstractProcessor {

    private Map<String, Set<TagEntry>> tagByClasses;
    private Set<String>                toValidateClasses;
    private Elements                   elements;
    private Types                      types;

    @Override
    public synchronized void init( ProcessingEnvironment processingEnv ) {
        super.init( processingEnv );

        elements = processingEnv.getElementUtils();
        types = processingEnv.getTypeUtils();

        processingEnv.getTypeUtils();
        log( Diagnostic.Kind.NOTE, "Initalizing the tag annotation processor." );
        try {

            try ( InputStream is = getFileForRead() ) {
                tagByClasses = mapTagByClass( TagUtils.loadEntries( is ) );
                toValidateClasses = new HashSet<>( tagByClasses.keySet() );
                log( Diagnostic.Kind.NOTE, "Incremental compilation:" + tagByClasses.size() + " entries loaded." );
            } catch ( FileNotFoundException e ) {
                log( Diagnostic.Kind.NOTE, "Class tag file not found." );
                tagByClasses = new HashMap<>();
            }
        } catch ( Throwable t ) {
            log( Diagnostic.Kind.ERROR, t.getMessage() );
            log( Diagnostic.Kind.ERROR, this.toString() );
        }


    }

    private InputStream getFileForRead() throws IOException {

        FileObject f = processingEnv.getFiler().getResource( StandardLocation.CLASS_OUTPUT, "", TagUtils.TAG_FILE_NAME );
        log( Diagnostic.Kind.NOTE, "Reading from " + f.toUri() );
        return f.openInputStream();
    }

    private Map<String, Set<TagEntry>> mapTagByClass( Set<TagEntry> tags ) {
        Map<String, Set<TagEntry>> result = new HashMap<>();
        for ( TagEntry tag : tags ) {
            if ( !result.containsKey( tag.getClassName() ) ) {
                result.put( tag.getClassName(), new HashSet<>() );
            }
            result.get( tag.getClassName() ).add( tag );
        }
        return result;
    }

    private OutputStream getFileForWrite() throws IOException {
        FileObject f = processingEnv.getFiler().createResource( StandardLocation.CLASS_OUTPUT, "", TagUtils.TAG_FILE_NAME );
        log( Diagnostic.Kind.NOTE, "Writing to " + f.toUri() );
        return f.openOutputStream();
    }

    /**
     * Log a message.
     */
    private void log( Diagnostic.Kind msgKind, String s ) {
        processingEnv.getMessager().printMessage( msgKind, getClass().getSimpleName() + ": " + s );
    }

    /**
     * Return a loadable class name from a TypeElement. A loadable class name have correct syntax for inner classes, like : mypackage.MyClass$InnerCLass.
     */
    public static String getLoadableClassName( TypeElement typeElement ) {
        switch ( typeElement.getNestingKind() ) {
            case TOP_LEVEL:
                return typeElement.getQualifiedName().toString();
            case MEMBER:
            case LOCAL:
                return getLoadableClassName( (TypeElement) typeElement.getEnclosingElement() ) + "$" + typeElement.getSimpleName();
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Retrieve the tags from a TypedElement or null if there is none.
     */
    public Set<TagEntry> getTagsFrom( String className, TypeElement e ) {
        Set<TagEntry> result = null;
        Set<TypeElement> elementsToScan = new HashSet<>();

        // Get tags from all annotations
        for ( AnnotationMirror am: elements.getAllAnnotationMirrors( e ) ) {
            TypeElement te = asTypeElement( am.getAnnotationType() );
            if ( te != null ) {
                elementsToScan.add( te );
            }
        }

        // Get tags from interfaces as well
        listAllInterfacesTo( e, elementsToScan );

        // Loop on element to scan
        for ( TypeElement typeElement : elementsToScan ) {
            // Is this annotation a tag anotation ?
            if ( typeElement.getAnnotation( Tag.class ) != null ) {
                if ( result == null ) {
                    result = new HashSet<>();
                }
                result.add( new TagEntry( className, getLoadableClassName( typeElement ) ) );
            }
        }
        return result;
    }

    public TypeElement asTypeElement( TypeMirror tm ) {
        if ( tm instanceof DeclaredType ) {
            Element element = ( (DeclaredType) tm ).asElement();
            if ( element instanceof TypeElement ) {
                return (TypeElement) element;
            }

        }
        return null;
    }

    public void listAllInterfacesTo( TypeElement e, Set<TypeElement> to ) {
        if ( e != null ) {
            // List all interfaces for this class
            for ( TypeMirror tm : e.getInterfaces() ) {
                TypeElement te = asTypeElement( tm );
                if ( te != null ) {
                    to.add( te );
                }
            }

            // Continue to super-class if needed.
            if ( e.getSuperclass() != null ) {
                listAllInterfacesTo( asTypeElement( e.getSuperclass() ), to );
            }
        }
    }


    /**
     * Check if the element still belong to the list or not.
     */
    private void checkOneElement( Element element ) throws Exception {
        log( Diagnostic.Kind.NOTE, "Inspecting element class: " + element.getSimpleName().toString() );
        if ( element instanceof TypeElement ) {
            TypeElement typeElement = (TypeElement) element;
            String className = getLoadableClassName( typeElement );
            Set<TagEntry> tags = getTagsFrom( className, typeElement );

            if ( tags != null && !tagByClasses.keySet().contains( className ) ) {
                log( Diagnostic.Kind.NOTE, "Found a new tagged class: " + className );
                tagByClasses.put( className, tags );
            } else if ( tags != null ) {
                log( Diagnostic.Kind.NOTE, "Updating tagged class: " + className );
                tagByClasses.put( className, tags );
            } else if ( tagByClasses.keySet().contains( className ) ) {
                log( Diagnostic.Kind.NOTE, "Found a class that is no longer tagged: " + className );
                tagByClasses.remove( className );
            }

            // To validate classes.
            if ( toValidateClasses != null ) {
                toValidateClasses.remove( className );
            }
        }
    }

    /**
     * Check an element and all the enclosed elements.
     */
    private void checkElement( Element element ) throws Exception {

        // Check the element itself.
        checkOneElement( element );

        // Check enclosed elements
        List<? extends Element> childElements = element.getEnclosedElements();
        if ( childElements != null ) {
            for ( Element e : childElements ) {
                checkElement( e );
            }
        }

    }

    /**
     * Write the results back to the annotation list file.
     * Adds any new classes remove classes marked for removal.
     */
    public void complete() throws Exception {

        if ( toValidateClasses != null && toValidateClasses.size() > 0 ) {
            log( Diagnostic.Kind.NOTE, "Validating classes that has not been compiled." );
            for ( String className : new HashSet<>( toValidateClasses ) ) {
                try {
                    TypeElement element = elements.getTypeElement( className );
                    if ( element == null ) {
                        log( Diagnostic.Kind.NOTE, "Class not found: " + className );
                        tagByClasses.remove( className );
                    } else {
                        checkElement( element );
                    }
                } catch ( Exception e ) {
                    log( Diagnostic.Kind.ERROR, "Error validating class: " + className );
                }
            }
        }

        try ( OutputStream os = getFileForWrite() ) {
            Set<TagEntry> finalSet = new HashSet<>();
            tagByClasses.values().forEach( set -> finalSet.addAll( set ) );
            TagUtils.writeClassTags( finalSet, os );
        }

        log( Diagnostic.Kind.NOTE, "Completed class tags annotation processing." );
    }


    /**
     * Process all classes and search for tags.
     */
    @Override
    public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv ) {

        try {

            // Loop compiling
            if ( !roundEnv.processingOver() ) {

                for ( Element element : roundEnv.getRootElements() ) {
                    checkElement( element );
                }

            } else {
                complete();
            }


        } catch ( Throwable t ) {
            log( Diagnostic.Kind.ERROR, t.getMessage() );
            log( Diagnostic.Kind.ERROR, this.toString() );
        }
        return false;
    }

}