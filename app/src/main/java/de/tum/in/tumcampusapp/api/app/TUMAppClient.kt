package de.tum.`in`.tumcampusapp.api.app

import com.google.common.util.concurrent.ListenableFuture
import com.google.protobuf.Empty
import de.tum.`in`.tumcampusapp.api.backend.CampusGrpc
import de.tum.`in`.tumcampusapp.api.backend.NewsSourceArray
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils

private var instance: TUMAppClient? = null
private lateinit var stub: CampusGrpc.CampusFutureStub

class TUMAppClient {

    init {
        val managedChannel = ManagedChannelBuilder.forAddress("api-grpc.tum.app", 443).build()
        stub = CampusGrpc.newFutureStub(managedChannel).withInterceptors(MetadataUtils.newAttachHeadersInterceptor(getHeaderMetaData()))
    }

    private fun getHeaderMetaData(): Metadata {
        val header = Metadata()
        val deviceId: Metadata.Key<String> = Metadata.Key.of("x-device-id", Metadata.ASCII_STRING_MARSHALLER)
        header.put(deviceId, "grpc-tests")
        return header
    }


    fun getNewsSources(): ListenableFuture<NewsSourceArray>? {
        return stub.getNewsSources(Empty.getDefaultInstance())
    }

    companion object {
        @Synchronized
        fun getInstance(): TUMAppClient {
            if (instance == null) {
                instance = TUMAppClient()
            }
            return instance!!
        }

    }

}
