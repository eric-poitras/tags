# Tags

#### Use case

1. Use marker interfaces or annotations to tag classes and discover them later while avoiding class path scanning.

#### Usage

Import the project from maven central:

```
<dependency>
	<groupId>org.dbrain</groupId>
	<artifactId>dbrain-tags</artifactId>
	<version>3.0</version>
</dependency>
```

Start tracking classes by creating annotations and interfaces annotated with @Tag:
```

// Annotation
@Tag
@Target( ElementType.TYPE )
public @interface MyTag {}

// Interface
@Tag
public interface MyRootSomething {}

```

Tag classes:
```
@MyTag
public class ToDiscover {}

// or

public class MyImplSomething implements MyRootSomething {}


```

Then query tagged classes using the API:
```
  List<String> myTaggedClasses = Tags.listClassNameByTag( MyTag.class );
  
  List<String> myTaggedClasses = Tags.listClassNameByTag( MyRootSomething.class );
  
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
