TSK4j - A Java Binding for Sleuthkit
====================================

A Java binding to Carrier's [Sleuthkit]
(http://www.sleuthkit.org/sleuthkit/) C library for host-based
forensics.

These Java bindings are not related to an existing effort described 
[here](http://wiki.sleuthkit.org/index.php?title=TSK_Bindings).  Those
bindings are used more to populate Java objects from an SQL database of
pre-acquired data.  The bindings described here are geared more to image
and filesystems traversal as performed by the core sleuthkit C/C++
code.  These bindings are essentially just a Java veneer over libtsk, with
some additions.

Motivation
----------

Why use these bindings?

* You prefer coding in Java over C (and thus likely prefer Maven over make).

* You want to compose a larger application that needs Sleuthkit's
  filesystem traversal power but want to use existing Java
  libraries/codebases.

* You do not need/want to store your results in SQL.

* You want a fast result to a simple task such as 'locate all PE
  executables' (see this [sample](./samples/src/main/java/tsk4jsamples/PEFinder.java)).

* The included 'digests' module is useful to you.

* The included sample programs are useful to you.

* You want to use or build upon the [Armour](./armour/src/main/java/edu/uw/apl/commons/tsk4j/armour/Armour.java) tool, a method for
  comparing BodyFiles and thus filesystem contents.

Required Tools
--------------

This codebase is Java, and so needs a Java compiler system, aka a
'Java Developmment Kit (JDK)'.  A 1.7 or later JDK is required.
Sun/Oracle's JDK works well, as does OpenJDK's JDK.

The build is via Maven, a build and project management tool for Java
artifacts. So Maven is required too.  All code dependencies are
resolved by Maven. At time of writing (Mar 2015), the author uses
Maven 3.2.5 on both Ubuntu 12.04 LTS and Mint 17. See
http://maven.apache.org/download.cgi for more details.

To verify you are running suitable versions of Java and Maven, run
'mvn -v' and inspect the output, like this:

```
[stuart-vaio]$ mvn -v
Apache Maven 3.2.5 (12a6b3acb947671f09b81f49094c53f426d8cea1; 2014-12-14T09:29:23-08:00)
Maven home: /usr/local/apache-maven/apache-maven-3.2.5
Java version: 1.7.0_17, vendor: Oracle Corporation
Java home: /usr/lib/jvm/jdk1.7.0_17/jre
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "2.6.32-73-generic", arch: "i386", family: "unix"
```

If you wish to take on the task of compiling the native C parts for
either MacOS or Windows platforms, you will need a suitable C compiler
and make/build system.  These native parts are already built for
Linux.

Build/Install
------------

```
$ cd /path/to/tsk4j

$ mvn install

$ mvn javadoc:aggregate
```

The Javadoc APIs should then be available at ./target/site/apidocs.

There are unit tests for some modules.  These are only run when the
'tester' profile is activated.  If you want to run unit tests, try:

```
$ mvn test -Ptester
```

Modules
-------

The tsk4j codebase is organised as four Maven 'modules', with a
parent pom at the root level.  The modules are as follows

* core

* samples

* digests

* armour

# Core

The primary tsk4j module, the one that wraps existing
Sleuthkit C routines with their Java equivalents.  If you just want to
write new filesystem traversal routines in Java, this module is the
only artifact you need.  To build:

```
$ cd /path/to/tsk4j/core

$ mvn install

// Unit tests optional, need profile activation
$ mvn test -Ptester
```

The core module is mostly a thin Java wrapper for libtsk.  Perhaps the
one value-added feature is the ability to extract data from various
objects via the familiar java.io.InputStream api.  An example:

```
InputStream is = new Image( "/dev/sda" ).getInputStream();
```

The following core tsk4j classes support the InputStream API:

* [image.Image] (./core/src/main/java/edu/uw/apl/commons/tsk4j/image/Image.java)

* [filesys.Attribute] (./core/src/main/java/edu/uw/apl/commons/tsk4j/filesys/Attribute.java)

* [filesys.File] (./core/src/main/java/edu/uw/apl/commons/tsk4j/filesys/File.java)

* [volsys.Partition] (./core/src/main/java/edu/uw/apl/commons/tsk4j/volsys/Partition.java)

So far we have built the native parts of the core tsk4j module for
Linux 32bit and Linux 64bit.  Still to do are MacOS and Windows
builds.  We use the [Java Native Loader]
(https://github.com/uw-dims/java-native-loader) framework to handle
the split Java/C language build.


# Samples

This module contains some sample programs built around the core tsk4j
artifact, which is a dependency of the sample module:

```
$ cd /path/to/tsk4j/samples

$ mvn package
```

Included are some Unix-style shell scripts to drive the sample program
invocation, on Linux/MacOS at least, e.g:

```
// Find files with Alternate Data Streams (NTFS only)
$ ./adsfind /path/to/fileSystem

// Find all Windows Registry Hive Files (NTFS only)
$ ./hivefind /path/to/some/ntfsFilesystem

// Find all Windows PE Executables (NTFS only)
$ ./pefind /path/to/some/ntfsFilesystem

// Locate all unused parts of an image and hash content of each
$ ./unallochash /dev/sda
```

# Digests

The digests module contains Java classes that represent summaries of
FileSystem traversals in the form of [body files]
(http://wiki.sleuthkit.org/index.php?title=Body_file).  The Sleuthkit
tool 'fls' can produce this file format:

```
$ fls -r -m / /dev/sda1 > sda1.bf
```

tsk4j views such a file not as *being* the bodyfile, but as being the
external representation *of* a bodyfile.  A [codec class]
(./digests/src/main/java/edu/uw/apl/commons/tsk4j/digests/BodyFileCodec.java)
marshals and unmarshals bodyfiles.  The [object representation]
(./digests/src/main/java/edu/uw/apl/commons/tsk4j/digests/BodyFile.java)
is simply a list of 'records', one per line in the external
representation.  A bodyfile record then corresponds to a single file in a
filesystem.

BodyFile objects are the unit of work (the operands) in what we view
as an algebra of bodyfile operations.  We define both unary and binary
operands on bodyfiles.

A unary operator, like
[WinPEOperator](./digests/src/main/java/edu/uw/apl/commons/tsk4j/digests/WinPEOperator.java),
simply filters one bodyfile A into a smaller one B by 'accepting' only
those records which are deemed to be Windows executables.

Binary operators, like those found in [BodyFileOperators]
(./digests/src/main/java/edu/uw/apl/commons/tsk4j/digests/BodyFileOperators.java),
take two bodyfiles as operands and perform set remove or retain logic
based on some notion of bodyfile record equality.  Note how we do
**not** define record equality in the record class itself. Instead, we
define an Equals interface, allowing the developer to create many
implementations of this and thus to define many ways of asserting that
one Bodyfile record is the same as another.  This 'postponement' of
record equality allows the set membership operations of the [algebra]
(./digests/src/main/java/edu/uw/apl/commons/tsk4j/digests/BodyFileAlgebra.java)
to be concise and use standard Java Collections methods, even though
the number of equality predicates is unlimited. Some sample record
equality implementations are
[supplied](./digests/src/main/java/edu/uw/apl/commons/tsk4j/digests/BodyFileOperators.java).


# Armour

[Armour](./armour/src/main/java/edu/uw/apl/commons/tsk4j/armour/Armour.java)
is a command shell (think bash) for bodyfile manipulation and
visualization.  It lets the user execute operations within the
bodyfile algebra mentioned above.  The user loads bodyfiles from disk,
then applies transforming operations on them, always producing new
bodyfiles (in memory, not on disk).  Each new bodyfile is added to the
available set for further processing.

Basic command line help is available.  On Linux/MacOS, a shell script driver
is included:

```
$ cd /path/to/tsk4j/armour

$ mvn package

$ ./armour -h
```

Armour takes bodyfiles as input.  These can be generated by
Sleuthkit's own fls tool.  Imagine we had 2 NTFS filesystems available
at /dev/sda1 and /dev/sda2:

```
$ fls -r -m / /dev/sda1 > sda1.bf
$ fls -r -m / /dev/sda2 > sda2.bf

$ ./armour sda1.bf sda2.bf

// List all loaded bodyfiles
> ls

// Print one of the loaded bodyfiles
> cat 1

// List available operations
> ops

// Extract all files in sda1 that are not in sda2 (Newer Files)
> op 2 1 2

// And show this new bodyfile in a Swing table
> tb 3
```

Armour is not limited to interactive use.  Like any good shell, its
invocation can be 'batch-driven', so might be placed in some larger
workflow.  An example might be its usage in a
[Cuckoo Sandbox](http://www.cuckoosandbox.org) workflow, with two
bodyfiles containing the 'before and after' disk contents associated
with some malware sample run:

```
$ ./armour -c "op 2 1 2; cat 3" sda1.bf sda2.bf > diffs.bf
```

Local Repository
----------------

The Maven artifacts built here themselves depend on the following
existing Maven artifacts which are not (yet) available on a public
Maven repository (like Maven Central):

* edu.uw.apl.commons:native-lib-loader:jar:2.1.0

* edu.uw.apl.commons:shell-base:jar:1.0

The source for the first Maven artifact is available on [github]
(https://github.com/uw-dims/java-native-loader).  But to save the
TSK4J user the chore of building and installing the dependencies, we
are bundling these artifacts in a 'project-local Maven repo' at
[./.repository](./.repository).  The relevant poms refer to this repo to
resolve the artifact dependencies.  The project-local repo looks like
this:

```
$ cd /path/to/tsk4j
$ tree .repository/
.repository/
`-- edu
    `-- uw
        `-- apl
            `-- commons
                `-- native-lib-loader
                    `-- 2.1.0
                        |-- native-lib-loader-2.1.0.jar
                        `-- native-lib-loader-2.1.0.pom
                `-- shell-base
                    `-- 1.0
                        |-- shell-base-1.0.jar
                        `-- shell-base-1.0.pom
```

# To Do

* Core needs native C code builds for Windows (32-bit and 64-bit) and
  MacOS (32, 64), plus any other platforms on which tsk4j might be
  run.
  
* Armour should be able to read file systems directly, and not rely on
fls for BodyFile creation.  That way, Armour's operator set could be
enriched with operators that can read file content.  The current
operators are limited to filtering on BodyFile record fields alone.


# Video/Slides

Ideas related to this work were presented at the [OSDFCon]
(http://www.osdfcon.org/2013-event/) workshop in 2013.  A local copy
of the slides is also included [here](./doc/Maclean-OSDF2013-tsk4j.pdf).

