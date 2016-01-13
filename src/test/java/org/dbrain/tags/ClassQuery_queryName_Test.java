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

import org.dbrain.tags.samples.complex.ComplexClass1;
import org.dbrain.tags.samples.complex.ComplexClass2;
import org.dbrain.tags.samples.complex.ComplexTag1;
import org.dbrain.tags.samples.complex.ComplexTag2;
import org.dbrain.tags.samples.inheritance.InheritedClass1;
import org.dbrain.tags.samples.inheritance.InheritedClass2;
import org.dbrain.tags.samples.inheritance.InheritedTag;
import org.dbrain.tags.samples.inheritanceinft.InheritedIntf1;
import org.dbrain.tags.samples.inheritanceinft.InheritedIntf2;
import org.dbrain.tags.samples.inheritanceinft.InheritedIntfTag;
import org.dbrain.tags.samples.multiple.MultiClass;
import org.dbrain.tags.samples.multiple.MultiTag1;
import org.dbrain.tags.samples.simple.SimpleClass1;
import org.dbrain.tags.samples.simple.SimpleClass2;
import org.dbrain.tags.samples.simple.SimpleTag;
import org.dbrain.tags.samples.simpleintf.SimpleIntf1;
import org.dbrain.tags.samples.simpleintf.SimpleIntf2;
import org.dbrain.tags.samples.simpleintf.SimpleIntfTag;
import org.dbrain.tags.samples.taggedintf.TaggedIntf;
import org.dbrain.tags.samples.taggedintf.TaggedIntfClass1;
import org.dbrain.tags.samples.taggedintf.TaggedIntfClass2;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Integration tests with Classtags.
 */
public class ClassQuery_queryName_Test {

    /**
     * Simple query.
     */
    @Test
    public void testQuerySimple1() throws Exception {
        List<String> result1 = Tags.listClassNameByTag( SimpleTag.class );

        Assert.assertEquals( 2, result1.size() );
        Assert.assertTrue( result1.contains( SimpleClass1.class.getName() ) );
        Assert.assertTrue( result1.contains( SimpleClass2.class.getName() ) );
    }


    /**
     * Simple query.
     */
    @Test
    public void testQuerySimple2() throws Exception {
        List<Tags.ClassTags> result1 = Tags.query().filter( ct -> ct.containsTag( SimpleTag.class ) ).listAsClassTags();

        Assert.assertEquals( 2, result1.size() );
        Assert.assertTrue( result1.stream().anyMatch( ct -> ct.getClassName().equals( SimpleClass1.class.getName() ) ) );
        Assert.assertTrue( result1.stream().anyMatch( ct -> ct.getClassName().equals( SimpleClass2.class.getName() ) ) );
        Assert.assertTrue( result1.stream().allMatch( ct -> ct.containsTag( SimpleTag.class ) ) );
    }

    /**
     * Simple query.
     */
    @Test
    public void testQuerySimple3() throws Exception {
        List<Class> result1 = Tags.query().filter( ct -> ct.containsTag( SimpleTag.class ) ).list();

        Assert.assertEquals( 2, result1.size() );
        Assert.assertTrue( result1.contains( SimpleClass1.class ) );
        Assert.assertTrue( result1.contains( SimpleClass2.class ) );
    }

    /**
     * Simple query.
     */
    @Test
    public void testQuerySimple4() throws Exception {
        List<Class> result1 = Tags.listByTag( SimpleTag.class );

        Assert.assertEquals( 2, result1.size() );
        Assert.assertTrue( result1.contains( SimpleClass1.class ) );
        Assert.assertTrue( result1.contains( SimpleClass2.class ) );
    }

    /**
     * Query over interfaces.
     */
    @Test
    public void testQuerySimpleIntf1() throws Exception {
        List<Class> result1 = Tags.listByTag( SimpleIntfTag.class );

        Assert.assertEquals( 2, result1.size() );
        Assert.assertTrue( result1.contains( SimpleIntf1.class ) );
        Assert.assertTrue( result1.contains( SimpleIntf2.class ) );
    }


    /**
     * Complex query.
     */
    @Test
    public void testQueryComplex1() throws Exception {
        Map<String, Tags.ClassTags> result1 = Tags.query().
                filter( ct -> ct.containsTag( ComplexTag1.class ) ).mapTagsByClassName();

        Assert.assertEquals( 2, result1.size() );
        Tags.ClassTags sc1 = result1.get( ComplexClass1.class.getName() );
        Tags.ClassTags sc2 = result1.get( ComplexClass2.class.getName() );
        Assert.assertNotNull( sc1 );
        Assert.assertNotNull( sc2 );
        Assert.assertEquals( sc1.getTags().size(), 1 );
        Assert.assertEquals( sc2.getTags().size(), 2 );
        Assert.assertTrue( sc1.getTags().contains( ComplexTag1.class.getName() ) );
        Assert.assertTrue( sc2.getTags().contains( ComplexTag1.class.getName() ) );
        Assert.assertTrue( sc2.getTags().contains( ComplexTag2.class.getName() ) );

    }


    /**
     * Test multiple tags on the same class.
     */
    @Test
    public void testQueryMulti() throws Exception {
        List<String> result1 = Tags.listClassNameByTag( MultiTag1.class );
        List<String> result2 = Tags.listClassNameByTag( MultiTag1.class );
        List<String> result3 = Tags.listClassNameByTag( MultiTag1.class );

        Assert.assertEquals( 1, result1.size() );
        Assert.assertEquals( 1, result2.size() );
        Assert.assertEquals( 1, result3.size() );
        Assert.assertTrue( result1.contains( MultiClass.class.getName() ) );
        Assert.assertTrue( result2.contains( MultiClass.class.getName() ) );
        Assert.assertTrue( result3.contains( MultiClass.class.getName() ) );
    }


    /**
     * Query over an inherited tag.
     */
    @Test
    public void testInheritedTag() throws Exception {
        List<String> result = Tags.listClassNameByTag( InheritedTag.class );

        Assert.assertEquals( 2, result.size() );
        Assert.assertTrue( result.contains( InheritedClass1.class.getName() ) );
        Assert.assertTrue( result.contains( InheritedClass2.class.getName() ) );
    }

    /**
     * Query over an inherited tag.
     */
    @Test
    public void testTaggedIntf() throws Exception {
        List<String> result = Tags.listClassNameByTag( TaggedIntf.class );

        Assert.assertEquals( 2, result.size() );
        Assert.assertTrue( result.contains( TaggedIntfClass1.class.getName() ) );
        Assert.assertTrue( result.contains( TaggedIntfClass2.class.getName() ) );
    }


    /**
     * Query over an inherited tag on interfaces.
     */
    @Test
    public void testInheritedTagOnInterface() throws Exception {
        List<String> result = Tags.listClassNameByTag( InheritedIntfTag.class );

        // Annotation are not inherited on interfaces.
        Assert.assertEquals( 1, result.size() );
        Assert.assertTrue( result.contains( InheritedIntf1.class.getName() ) );
        Assert.assertFalse( result.contains( InheritedIntf2.class.getName() ) );
    }


}
