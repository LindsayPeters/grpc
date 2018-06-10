/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rippledown.grpc


import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.stub.StreamObserver
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.AdditionalAnswers.delegatesTo
import org.mockito.ArgumentCaptor
import org.mockito.Matchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/**
 * Unit tests for [Client].
 * For demonstrating how to write gRPC unit test only.
 * Not intended to provide a high code coverage or to test every major usecase.
 *
 *
 * For more unit test examples see [io.grpc.examples.routeguide.RouteGuideClientTest] and
 * [io.grpc.examples.routeguide.RouteGuideServerTest].
 */
@RunWith(JUnit4::class)
class ClientTest {
    /**
     * This rule manages automatic graceful shutdown for the registered servers and channels at the
     * end of test.
     */
    @get:Rule
    val grpcCleanup = GrpcCleanupRule()

    private val serviceImpl = mock(RippledownGrpc.RippledownImplBase::class.java, delegatesTo<Any>(object : RippledownGrpc.RippledownImplBase() {

    }))
    private lateinit var client: Client

    @Before
    @Throws(Exception::class)
    fun setUp() {
        // Generate a unique in-process server name.
        val serverName = InProcessServerBuilder.generateName()

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(serviceImpl).build().start())

        // Create a client channel and register for automatic graceful shutdown.
        val channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build())

        // Create a Client using the in-process channel;
        client = Client(channel)
    }

    @Test
    fun interpret() {
        val requestCaptor = ArgumentCaptor.forClass(InterpretationRequest::class.java)

        val kbName = "glucose"
        val caseName = "white"
        val text = "some clinical note"
        client.interpret(kbName, caseName, text)

        verify<RippledownGrpc.RippledownImplBase>(serviceImpl)
                .interpret(requestCaptor.capture(), Matchers.any<StreamObserver<InterpretationResponse>>())
        assertEquals(kbName, requestCaptor.value.kbName)
        assertEquals(caseName, requestCaptor.value.caseName)
        assertEquals(text, requestCaptor.value.text)
    }

    @Test
    fun `create kb`() {
        val requestCaptor = ArgumentCaptor.forClass(InterpretationRequest::class.java)

        val kbName = "glucose"
        val caseName = "white"
        val text = "some clinical note"
        client.interpret(kbName, caseName, text)

        verify<RippledownGrpc.RippledownImplBase>(serviceImpl)
                .interpret(requestCaptor.capture(), Matchers.any<StreamObserver<InterpretationResponse>>())
        assertEquals(kbName, requestCaptor.value.kbName)
        assertEquals(caseName, requestCaptor.value.caseName)
        assertEquals(text, requestCaptor.value.text)
    }
}
