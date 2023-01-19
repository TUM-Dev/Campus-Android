package de.tum.`in`.tumcampusapp.component.tumui.feedback

import android.location.Location
import android.os.Bundle
import de.tum.`in`.tumcampusapp.component.tumui.feedback.model.Feedback
import io.reactivex.Observable

interface FeedbackContract {

    interface View {
        fun getTopicInput(): Observable<Int>
        fun getMessage(): Observable<String>
        fun getEmail(): Observable<String>
        fun getIncludeEmail(): Observable<Boolean>
        fun getIncludeLocation(): Observable<Boolean>
        fun getLocation(): Observable<Location>
        fun setFeedback(message: String)
        fun showEmailInput(show: Boolean)
        fun showEmptyMessageError()
        fun showSendConfirmationDialog()
        fun showWarning(message: String)
        fun showDialog(title: String, message: String)
        fun showProgressDialog()
        fun showSendErrorDialog()
        fun onFeedbackSent()
    }

    interface Presenter {
        val feedback: Feedback
        val lrzId: String
        fun attachView(view: View)
        fun onRestoreInstanceState(savedInstanceState: Bundle)
        fun initEmail()
        fun onSendFeedback()
        fun onConfirmSend()
        fun listenForLocation()
        fun onSaveInstanceState(outState: Bundle)
        fun detachView()
       // fun showImageOptionsDialog()
    }
}
