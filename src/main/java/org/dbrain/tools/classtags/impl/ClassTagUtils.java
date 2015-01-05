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

package org.dbrain.tools.classtags.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Utilities about class tags.
 */
public class ClassTagUtils {

    /**
     * Standard encoding for tag files.
     */
    public static final String TAG_FILE_CHARSET = "utf-8";
    public static final String TAG_FILE_NAME    = "META-INF/" + ClassTagEntry.class.getName();


    /**
     * List all resources files containing tags.
     */
    public static List<URL> listResources( ClassLoader cl ) throws IOException {
        return Collections.list( cl.getResources( TAG_FILE_NAME ) );
    }

    /**
     * Query all classes tagged with the specific tag.
     */
    public static <T extends Collection<ClassTagEntry>> T loadEntries( ClassLoader cl,
                                    T to,
                                    Predicate<ClassTagEntry> filter ) throws IOException {
        for ( URL u : ClassTagUtils.listResources( cl ) ) {
            try ( InputStream is = u.openStream() ) {
                ClassTagUtils.loadEntries( is, to, filter );
            }
        }
        return to;
    }

    /**
     * Read a file of class tags.
     *
     * It returns a map of class names with each a list of tag annotation names found on the class.
     */
    public static <T extends Collection<ClassTagEntry>> T loadEntries( InputStream inf, T to, Predicate<ClassTagEntry> filter ) throws IOException {

        BufferedReader in = new BufferedReader( new InputStreamReader( inf, Charset.forName( TAG_FILE_CHARSET ) ) );
        String ln;
        try {
            while ( ( ln = in.readLine() ) != null ) {
                String[] parts = ln.split( ":" );
                if ( parts.length == 2 ) {
                    ClassTagEntry entry = new ClassTagEntry( parts[0], parts[1] );
                    if ( filter == null || ( filter != null && filter.test( entry ) ) ) {
                        to.add( entry );
                    }
                } else {
                    // log( Diagnostic.Kind.WARNING, "Invalid entry found in file: " + ln );
                }
            }
        } finally {
            in.close();
        }
        return to;
    }

    public static Set<ClassTagEntry> loadEntries( InputStream inf ) throws IOException {
        return loadEntries( inf, new HashSet<>( ), null );
    }

    /**
     * Write tags to file.
     */
    public static void writeClassTags( Set<ClassTagEntry> tags, OutputStream os ) throws IOException {

        List<ClassTagEntry> tagsList = new ArrayList<>( tags );
        Collections.sort( tagsList, Comparator.comparing( e -> e.toString() ) );

        PrintWriter out = new PrintWriter( new OutputStreamWriter( os, TAG_FILE_CHARSET ) );
        for ( ClassTagEntry e : tagsList ) {
            out.println( e );
        }
        out.close();
    }

    /**
     * Load a single class. In case of error, redirect error to a specific consumer and return null.
     */
    public static Class<?> loadClass( ClassLoader cl, String className, Consumer<ClassNotFoundException> onError) {
        try {
            return cl.loadClass( className );
        } catch ( ClassNotFoundException e ) {
            if ( onError != null ) {
                onError.accept( e );
            }
            return null;
        }
    }

}
