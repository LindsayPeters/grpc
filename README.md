grpc Kotlin example
==============================================


You may want to read through the
[Quick Start Guide](https://grpc.io/docs/quickstart/java.html)
before trying out the examples.

To build:

```
$ ./gradlew installDist
```

This creates the scripts `rdr-server`, `rdr-client`,
The server needs to be running before the client.

For example, to try the hello world example first run:

```
$ ./build/install/rdr/bin/rdr-server
```

And in a different terminal window run:

```
$ ./build/install/rdr/bin/rdr-client
```

Please refer to gRPC Java's [README](../README.md) and
[tutorial](https://grpc.io/docs/tutorials/basic/java.html) for more
information.

Unit test examples
==============================================

Examples for unit testing gRPC clients and servers are located in [./src/test](./src/test).

In general, we DO NOT allow overriding the client stub.
We encourage users to leverage `InProcessTransport` as demonstrated in the examples to
write unit tests. `InProcessTransport` is light-weight and runs the server
and client in the same process without any socket/TCP connection.

For testing a gRPC client, create the client with a real stub
using an InProcessChannelBuilder.java and test it against an InProcessServer.java
with a mock/fake service implementation.

For testing a gRPC server, create the server as an InProcessServer,
and test it against a real client stub with an InProcessChannel.

The gRPC-java library also provides a JUnit rule, GrpcCleanupRule.java, to do the graceful shutdown
boilerplate for you.
