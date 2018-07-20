package de.tum.`in`.tumcampusapp.api.tumonline.exception

import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject

abstract class TokenException(private val errorCode: Int) : TUMOnlineException() {

    override fun transformToErrorResponse(response: Response): Response {
        val json = JSONObject().apply {
            put("error_code", errorCode)
        }

        val responseBody = ResponseBody.create(JSON, json.toString())
        return response
                .newBuilder()
                .code(498)
                .body(responseBody)
                .build()
    }

    companion object {
        private val JSON = MediaType.parse("application/json")
    }

}