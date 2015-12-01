Marshal data serialization format and library.

A Marshal is a tuple that knows how to serialize and deserialize itself.
Marshals can contain nested marshals and byte arrays as will as basic primitive
types. In addition, type information is stored for each object, allowing for
typed reconstruction of known types, pretty printing, and stable sort orders
without mixing of types in the sorting.

## Lexicographical Format

This is the "native" mode for the marshal library, the reason for its
existence. In this format, the serialized byte arrays can be lexicographically
ordered, and the ordering will match the sorted order of the tuples.

This is useful in systems such as HBase that deal only with byte arrays.

The format looks a bit like this:

    [type] [data] [SEPARATOR] [type] [data] [SEPARATOR] ... [SEPARATOR]

### Primitive Types

```java
Marshal m1 = Marshal.builder()
    .addByte((byte)1)
    .addDouble(3.14)
    .addString("Hello, world!")
    .addInteger(42)
    .addLong(1337l)
    .build();

byte[] s1 = m1.toBytes();
Marshal m2 = Marshal.fromBytes(s1);

byte b = m2.getByteAt(0);
double d = m2.getDoubleAt(1);
String s = m2.getStringAt(2);
int i = m2.getIntegerAt(3);
long l = m2.getLongAt(4);
```

### Nested Types

```java
byte[] bytes = { ... };
ByteArray array1 = new ByteArray(bytes);
Marshal m3 = Marshal.builder()
    .addByteArray(array1)
    .addMarshal(m2)
    .build();

byte[] serialized2 = m3.toBytes();
Marshal m4 = Marshal.fromBytes(serialized2);

ByteArray array2 = m4.getByteArrayAt(0);
Marshal m5 = m4.getMarshalAt(1);
// m5 is the same as m1/m2, so we can access nested objects
double d2 = m5.getDoubleAt(1); // 3.14
```

### Prefixes

When serialized, the byte arrays can be sorted. Prefixes can be used for
queries into databases such as HBase. Prefixes come in two forms: terminated,
and unterminated.

```java
Marshal m6 = Marshal.builder()
    .addByte((byte)1)
    .addDouble(3.14)
    .addString("Hello")
    .build();

// takes 3 elements from the marshal
// terminates the prefix with a separator byte
// p1 is not a prefix of s1, since strings are not equal
byte[] p1 = m6.prefixTerminatedBytes(3);

// p2 is a prefix of s1, since the last string is a prefix of the last
// string in m1
byte[] p2 = m6.prefixUnterminatedBytes(3);

```

## Writable Format

The lexicographical format is great when you need prefixes, but it can be
inefficient to encode nested structures when sorting and prefixes are not
needed. Thus, the writable format provides an alternative, faster serialization
mechanism that does not have the same properties, but can still decode and
encode marshals using an alternative serialization mechanism.

Writing:

```java
DataOutput output = ...;
m1.write(output);
```

Reading:

```java
DataInput input = ...;
Marshal m7 = Marshal.read(input);
```

### Hadoop

For convenience, since the writable format is normally used with Hadoop, a
`MarshalWritable` is provided as a separate subpackage. It uses the writable
serialization mechanism behind the scenes.

## Exceptions

All marshal encoding and decoding operations will throw a `MarshalException`
upon failure. For convenience, `MarshalException` extends `IOException`.

Reading and writing in the writable format will throw an `IOException` if the
underlying data source or sink throws an exception.

## Build Process

    ./gradlew clean build

Please use `--no-ff` when merging feature branches.

Pushing the master branch will run the
[CI process](https://jenkins.fullcontact.com/job/datascience/job/marshal/),
which will upload new JARs to artifactory.

## Benchmarks
Run JMH benchmarks with:

    ./gradlew clean jmh

Results will be under `marshal/build/reports/jmh`
