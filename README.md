classtags
=========

The goals:

1. Easily tag classes with annotations for later discovery.
2. Avoid using class path scanning.
3. Easy to define new tags.

The components:

1. An annotation processor that keeps track of classes tagged with specific custom annotations.
2. An engine to query gathered information.

The limits:

1. Only works for code that you compile.
2. This version only target JDK 8 because it is the version I use but it can easily be backported to 7 and 6.

Standard use cases:

1. Discover classes that are to be fed to a JERSEY end-point.
2. Discover classes that are to be registred in a DI.
3. You have old code that uses evil static declarations and you need to eagerly load thoses classes.
