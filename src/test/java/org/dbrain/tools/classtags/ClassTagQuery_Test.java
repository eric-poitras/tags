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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by epoitras on 21/01/15.
 */
public class ClassTagQuery_Test {

    public static class ClassNotFoundConsumer implements Consumer<ClassNotFoundException> {

        public List<ClassNotFoundException> exceptions = new ArrayList<>();

        @Override
        public void accept( ClassNotFoundException e ) {
            exceptions.add( e );
        }
    }

    public static class CustomClassLoader extends ClassLoader {

        public List<String> requestedClassNames = new ArrayList<>();

        @Override
        public Class<?> loadClass( String name ) throws ClassNotFoundException {
            requestedClassNames.add( name );
            return super.loadClass( name );
        }
    }

    @Test
    public void testLoadCustomTag() throws Exception {
        List<String> q = new ClassTagQuery().resource( getClass().getResource( "/sample.txt" ) )
                                            .filter( tags -> tags.containsTag( "org.dbrain.tools.classtags.ResourceRest" ) )
                                            .listAsClassNames();
        Assert.assertEquals( 2, q.size() );
        Assert.assertTrue( q.contains( "org.dbrain.tools.classtags.Sample" ) );
        Assert.assertTrue( q.contains( "org.dbrain.tools.classtags.Sample2" ) );
    }


    /**
     * Attempt to load the invalid classes from the sample.txt file.
     */
    @Test
    public void testOnError() throws Exception {
        ClassNotFoundConsumer c = new ClassNotFoundConsumer();
        new ClassTagQuery().resource( getClass().getResource( "/sample.txt" ) )
                           .filter( tags -> tags.containsTag( "org.dbrain.tools.classtags.ResourceRest" ) )
                           .onClassNotFound( c )
                           .list();
        Assert.assertEquals( 2, c.exceptions.size() );
    }

    @Test
    public void testCustomClassLoader() throws Exception {
        CustomClassLoader c = new CustomClassLoader();
        new ClassTagQuery().resource( getClass().getResource( "/sample.txt" ) )
                           .filter( tags -> tags.containsTag( "org.dbrain.tools.classtags.ResourceRest" ) )
                           .classLoader( c )
                           .list();
        Assert.assertEquals( 2, c.requestedClassNames.size() );

    }
}
