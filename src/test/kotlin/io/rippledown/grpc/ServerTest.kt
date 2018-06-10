
package io.rippledown.grpc

import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.rippledown.server.getServer
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ServerTest {

    /**
     * This rule manages automatic graceful shutdown for the registered servers and channels at the
     * end of test.
     */
    @get:Rule
    val grpcCleanup = GrpcCleanupRule()

    @Test
    fun createKB() {
        val serverName = InProcessServerBuilder.generateName()

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(Server.RippleDownImpl()).build().start())

        val blockingStub = RippledownGrpc.newBlockingStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor()
                        .build()))

        val reply = blockingStub.createKB(KBCreateRequest.newBuilder().setKbName("Pets").build())
        assertEquals("Pets created OK", reply.message)
    }

    @Test
    fun deleteAllKBS() {
        val serverName = InProcessServerBuilder.generateName()

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(Server.RippleDownImpl()).build().start())

        val blockingStub = RippledownGrpc.newBlockingStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor()
                        .build()))

        val reply = blockingStub.deleteAllKBs(KBDeleteAllRequest.newBuilder().build())
        assertEquals("all kbs deleted", reply.message)
    }

    @Test
    fun interpret() {
        val serverName = InProcessServerBuilder.generateName()

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(Server.RippleDownImpl()).build().start())

        val blockingStub = RippledownGrpc.newBlockingStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor()
                        .build()))

        getServer().create("Pets")
        val reply = blockingStub.interpret(InterpretationRequest.newBuilder()
                .setKbName("Pets")
                .setCaseName("case A")
                .setText("Is this a cat?").build())
        assertEquals("report was []", reply.report)
    }

    @Before
    fun cleanup() {
        getServer().deleteAll()
    }
}
