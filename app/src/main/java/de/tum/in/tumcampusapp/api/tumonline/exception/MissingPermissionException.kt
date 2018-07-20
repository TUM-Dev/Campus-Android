package de.tum.`in`.tumcampusapp.api.tumonline.exception

import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject

class MissingPermissionException : TUMOnlineException() {

    override val message: String?
        get() = "Token is active but specific permission not given"

    override fun transformToErrorResponse(response: Response): Response {
        val responseBody = ResponseBody.create(JSON, JSONObject().toString())
        return response
                .newBuilder()
                .code(403)
                .body(responseBody)
                .build()
    }

    companion object {
        private val JSON = MediaType.parse("application/json")
    }

}