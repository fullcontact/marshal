HBase utility libraries.

Marshal
=======

A library for custom serialization to/from byte arrays.

The Marshal library performs serialization in a byte-comparable manner. This
allows the resulting byte arrays to be lexographically compared. This is
useful in systems such as HBase, which only deals with byte arrays. A Marshal
is similar to a list of primitive types or a limited set of objects (of
potentially different types) backed by objects serialized in a ByteArray.
Marshals may include other marshals or byte arrays as a nested component. Type
information is stored for each object, allowing for typed reconstruction of
known types, pretty printing, and stable sort orders without mixing of types
in the sorting.

Marshal-MapReduce
=================

A library for using Marshal objects as Hadoop writables.
