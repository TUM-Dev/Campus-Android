package de.tum.`in`.tumcampusapp.api.tumonline.exception

import okhttp3.Response
import java.io.IOException

abstract class TUMOnlineException : IOException() {

    abstract fun transformToErrorResponse(response: Response): Response

}