# class tags

#### Use cases

1. Allows to define tags for each specific uses.
2. Tag classes with annotations for later discovery while avoiding class path scanning.

#### Usage

Import the project from maven central:

```
<dependency>
	<groupId>org.dbrain.tool</groupId>
	<artifactId>classtags</artifactId>
	<version>0.1</version>
</dependency>
```

Define your own tag annotation by annotating it with the @ClassTagAnnotation:
```
@ClassTagAnnotation
@Target( ElementType.TYPE )
public @interface MyTag {}
```

Then query tags using the API:
```
  List<String> myTaggedClasses = ClassTagQuery.listClassNameByTag( MyTag.class );
```

For this to works as expected, you have to make sure:

1. Your development environment support and discovered the annotation processor. It is auto-discoverable and works fine with IntelliJ but I did not tested interoperability with Eclipse or NetBean. Check for the compilation switch: -proc:XXX.
2. For most cases, the annotation processor support incremental compilation but if the meta-file go out-of-sync, just perform a full compilation.

#### Components

1. An annotation processor that keeps track of classes tagged with specific custom annotations.
2. An engine to query gathered information.

#### Roadmap 

1. User-defined tag definition files to works with 3rd party libraries.
2. Enhanced error handling.
3. Support annotations on packages.
4. Support older JDK versions ?
