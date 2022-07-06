package de.tum.`in`.tumcampusapp.api.app

import com.google.protobuf.Empty
import de.tum.`in`.tumcampusapp.api.backend.CampusGrpc
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata.ASCII_STRING_MARSHALLER

class BackendHelper {

    // Dummy example code for testing the grpc connection
    companion object {
        private fun getHeaderMetaData(): io.grpc.Metadata {
            val header: io.grpc.Metadata = io.grpc.Metadata()
            val deviceId: io.grpc.Metadata.Key<String> = io.grpc.Metadata.Key.of("x-device-id", ASCII_STRING_MARSHALLER)
            header.put(deviceId, "grpc-tests")
            return header
        }

        fun getBackendConnection() {
            val managedChannel = ManagedChannelBuilder.forAddress("api.tum.app", 50052).usePlaintext().build()
            var blockingStub = CampusGrpc.newBlockingStub(managedChannel)
            blockingStub = io.grpc.stub.MetadataUtils.attachHeaders(blockingStub, getHeaderMetaData())

            val topNews = blockingStub.getTopNews(Empty.getDefaultInstance())
            val link = topNews.link
            println(link)

            val newsSources = blockingStub.getNewsSources(Empty.getDefaultInstance())
            println(newsSources.getSources(0))
        }
    }
}