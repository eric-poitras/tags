# class tags

#### Use cases

1. Allows to define tags for each specific uses.
2. Tag classes with annotations for later discovery while avoiding class path scanning.

#### Usage

Import the project from maven central:

```
<dependency>
	<groupId>org.dbrain.lib</groupId>
	<artifactId>dbrain-classtags</artifactId>
	<version>1.2</version>
</dependency>
```

Define your own tag annotation by annotating it with the @ClassTagAnnotation:
```
@ClassTagAnnotation
@Target( ElementType.TYPE )
public @interface MyTag {}
```

Tag classes:
```
@MyTag
public class ToDiscover {}
```

Then query tagged classes using the API:
```
  List<String> myTaggedClasses = ClassTagQuery.listClassNameByTag( MyTag.class );
```

#### Components

1. An annotation processor that keeps track of classes tagged with specific custom annotations.
2. An engine to query gathered information.

#### Troubleshooting

For this to works as expected, you have to make sure:

1. Your development environment must support and discover the annotation processor.
2. For most cases, the annotation processor works fine with incremental compilation but if the meta-file go out-of-sync, you have to perform a full compilation.

#### Roadmap 

1. User-defined tag definition files to works with 3rd party libraries.
2. Enhanced error handling.
3. Support annotations on packages.
4. Support older JDK versions ?
