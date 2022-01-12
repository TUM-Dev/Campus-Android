package de.tum.`in`.tumcampusapp.api.app


import com.google.protobuf.Empty
import de.tum.`in`.tumcampusapp.api.backend.*
import io.grpc.ManagedChannelBuilder


class BackendHelper {

    public fun getBackendConnection() {
        var managedChannel = ManagedChannelBuilder.forAddress("https://www.google.com", 443).build()
        var blockingStub = CampusGrpc.newBlockingStub(managedChannel)
    }
}