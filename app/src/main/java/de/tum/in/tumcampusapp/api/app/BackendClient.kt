package de.tum.`in`.tumcampusapp.api.app
import app.tum.campus.api.CampusGrpc
import app.tum.campus.api.GetNewsRequest
import app.tum.campus.api.GetNewsSourcesRequest
import app.tum.campus.api.GetUpdateNoteRequest
import de.tum.`in`.tumcampusapp.BuildConfig
import de.tum.`in`.tumcampusapp.component.ui.news.model.News
import de.tum.`in`.tumcampusapp.component.ui.news.model.NewsSources
import de.tum.`in`.tumcampusapp.component.ui.updatenote.model.UpdateNote
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils

class BackendClient private constructor() {
    private var stub: CampusGrpc.CampusFutureStub

    init {
        val managedChannel = ManagedChannelBuilder.forAddress("api.tum.app", 443).build()
        stub = CampusGrpc.newFutureStub(managedChannel).withInterceptors(MetadataUtils.newAttachHeadersInterceptor(getHeaderMetaData()))
    }

    private fun getHeaderMetaData(): Metadata {
        val header = Metadata()
        val deviceId: Metadata.Key<String> = Metadata.Key.of("x-device-id", Metadata.ASCII_STRING_MARSHALLER)
        header.put(deviceId, "grpc-tests")
        return header
    }

    private fun getStatus(it: Throwable): io.grpc.StatusRuntimeException {
        return when (it) {
            is java.util.concurrent.ExecutionException -> getStatus(it.cause!!)
            is io.grpc.StatusRuntimeException -> it
            else -> io.grpc.StatusRuntimeException(io.grpc.Status.UNKNOWN.withDescription(it.message).withCause(it))
        }
    }

    /**
     * getUpdateNote calls @callback with an UpdateNote currently in the api
     * On error, errorCallback is called with an error message.
     */
    fun getUpdateNote(callback: (UpdateNote) -> Unit, errorCallback: (message: io.grpc.StatusRuntimeException) -> Unit) {
        val request = GetUpdateNoteRequest.newBuilder().setVersion(BuildConfig.VERSION_CODE.toLong()).build()
        val response = stub.getUpdateNote(request)
        response.runCatching { get() }.fold(
            onSuccess = { callback(UpdateNote.fromProto(it)) },
            onFailure = { errorCallback(getStatus(it)) }
        )
    }

    /**
     * getNewsSources calls @callback with an UpdateNote currently in the api
     * On error, errorCallback is called with an error message.
     */
    fun getNewsSources(callback: (List<NewsSources>) -> Unit, errorCallback: (message: io.grpc.StatusRuntimeException) -> Unit) {
        val response = stub.getNewsSources(GetNewsSourcesRequest.getDefaultInstance())
        response.runCatching { get() }.fold(
            onSuccess = { callback(NewsSources.fromProto(it)) },
            onFailure = { errorCallback(getStatus(it)) }
        )
    }

    /**
     * getNewsSources calls @callback with an UpdateNote currently in the api
     * On error, errorCallback is called with an error message.
     */
    fun getNews(lastNewsID: Int, callback: (List<News>) -> Unit, errorCallback: (message: io.grpc.StatusRuntimeException) -> Unit) {
        val request = GetNewsRequest.newBuilder().setLastNewsId(lastNewsID).build()
        val response = stub.getNews(request)
        response.runCatching { get() }.fold(
            onSuccess = { callback(News.fromProto(it)) },
            onFailure = { errorCallback(getStatus(it)) }
        )
    }

    companion object {
        @Volatile
        private var instance: BackendClient? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: BackendClient().also { instance = it }
        }
    }
}
