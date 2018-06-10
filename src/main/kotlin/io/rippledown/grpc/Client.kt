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

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

class Client
internal constructor(private val channel: ManagedChannel) {

    private val blockingStub: RippledownGrpc.RippledownBlockingStub = RippledownGrpc.newBlockingStub(channel)

    /** Construct client connecting to HelloWorld server at `host:port`.  */
    constructor(host: String, port: Int) : this(ManagedChannelBuilder.forAddress(host, port)
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext()
            .build()) {
    }

    @Throws(InterruptedException::class)
    fun shutdown() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    fun deleteAllKBS(): Client {
        logger.info("about to delete all KBs")
        val request = KBDeleteAllRequest.newBuilder()
                .build()

        val response: StatusMessage = try {
            blockingStub.deleteAllKBs(request)
        } catch (e: StatusRuntimeException) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.status)
            return this
        }
        logger.info("Response to deleting all KBs was : ${response.message}")
        return this
    }

    fun createKB(kbName: String): Client {
        logger.info("about to create KB $kbName")
        val request = KBCreateRequest.newBuilder()
                .setKbName(kbName)
                .build()

        val response: StatusMessage = try {
            blockingStub.createKB(request)
        } catch (e: StatusRuntimeException) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.status)
            return this
        }
        logger.info("Response to create KB was : ${response.message}")
        return this
    }

    fun interpret(kbName: String, caseName: String, text: String): Client {
        logger.info("about to request interpretation of case $caseName with kb $kbName")
        val request = InterpretationRequest.newBuilder()
                .setKbName(kbName)
                .setCaseName(caseName)
                .setText(text)
                .build()

        val response: InterpretationResponse = try {
            blockingStub.interpret(request)
        } catch (e: StatusRuntimeException) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.status)
            return this
        }
        logger.info("Interpretation was : ${response.report}")
        return this
    }

    companion object {
        private val logger = Logger.getLogger(Client::class.java.name)

        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val client = Client("localhost", 50051)
            logger.info("starting client.....")
            try {
                client.deleteAllKBS() //cleanup
                        .createKB("RIS")
                        .interpret("RIS", "White", "no fracture detected")
                        .deleteAllKBS()
            } finally {
                client.shutdown()
            }
            logger.info("cleaned up client.....")
        }
    }
}
