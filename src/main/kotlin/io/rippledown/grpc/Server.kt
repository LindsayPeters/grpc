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

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import io.rippledown.server.getServer
import io.rippledown.textanalysis.kase.TokenCase
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

class Server {

    private lateinit var server: Server

    @Throws(IOException::class)
    private fun start() {
        /* The port on which the server should run */
        val port = 50051
        server = ServerBuilder.forPort(port)
                .addService(RippleDownImpl())
                .build()
                .start()
        logger.log(Level.INFO, "1. Server started, listening on {0}", port)
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down")
                this@Server.stop()
                System.err.println("*** server shut down")
            }
        })
    }

    private fun stop() {
        server.shutdown()
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    @Throws(InterruptedException::class)
    private fun blockUntilShutdown() {
        server.awaitTermination()
    }

    internal class RippleDownImpl : RippledownGrpc.RippledownImplBase() {
        val rdrServer = getServer()

        override fun deleteAllKBs(request: KBDeleteAllRequest, responseObserver: StreamObserver<StatusMessage>) {
            logger.info("server: deleting all kbs")
            rdrServer.deleteAll()

            val status = StatusMessage.newBuilder()
                    .setMessage("all kbs deleted")
                    .build()
            responseObserver.onNext(status)
            responseObserver.onCompleted()
        }

        override fun createKB(request: KBCreateRequest, responseObserver: StreamObserver<StatusMessage>) {
            logger.info("server: creating kb ${request.kbName}")
            rdrServer.create(request.kbName)

            val status = StatusMessage.newBuilder()
                    .setMessage("${request.kbName} created OK")
                    .build()
            responseObserver.onNext(status)
            responseObserver.onCompleted()
        }

        override fun interpret(request: InterpretationRequest, responseObserver: StreamObserver<InterpretationResponse>) {
            logger.info("server: interpreting case message $request")
            val kb = rdrServer.get(request.kbName)
            val case = TokenCase(request.caseName, request.text)
            val interpretation = kb.interpret(case)
            val response = InterpretationResponse.newBuilder()
                    .setReport("report was " + interpretation.conclusions().toString())
                    .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }
    }

    companion object {
        private val logger = Logger.getLogger(Server::class.java.name)

        @Throws(IOException::class, InterruptedException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val server = io.rippledown.grpc.Server()
            server.start()
            server.blockUntilShutdown()
        }
    }
}
