# Tags

#### Use case

1. Use marker interfaces or annotations to tag classes and discover them later while avoiding class path scanning.

#### Usage

Import the project from maven central:

```
<dependency>
	<groupId>org.dbrain</groupId>
	<artifactId>dbrain-tags</artifactId>
	<version>3.3</version>
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
  List<Class> myTaggedClasses = Tags.listClassByTag( MyTag.class );
  
  List<Class> myTaggedClasses = Tags.listClassByTag( MyRootSomething.class );
  
```

#### Configuration
You can configure tags in the context of a specific project by creating a property file in the source code.
```
META-INF/tags.properties  
```
##### property: externalTags
Allows to pick classes and annotation that are not defined within your project and ask the annotation processor to track 
them. Please note that this works only within the current compilation scope. 

```
externalTags=org.dbrain.tags.samples.external.ExternalTag;org.dbrain.tags.samples.external.ExternalTag2  
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
