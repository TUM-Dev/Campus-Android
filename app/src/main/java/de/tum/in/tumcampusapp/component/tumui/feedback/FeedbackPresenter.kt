package de.tum.`in`.tumcampusapp.component.tumui.feedback


import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.util.Patterns
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.tumui.feedback.di.LrzId
import de.tum.`in`.tumcampusapp.component.tumui.feedback.model.Feedback
import de.tum.`in`.tumcampusapp.component.tumui.feedback.model.FeedbackResult
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.disposables.CompositeDisposable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class FeedbackPresenter @Inject constructor(
    private val context: Context,
    @LrzId override val lrzId: String,
    private val tumCabeClient: TUMCabeClient
) : FeedbackContract.Presenter {

    private val compositeDisposable = CompositeDisposable()
    override var feedback = Feedback()

    private var sendFeedbackCall = tumCabeClient.sendFeedback(feedback)
    private var sendImagesCalls = mutableListOf<Call<FeedbackResult>>()

    private var imagesSent: Int = 0

    private var view: FeedbackContract.View? = null

    override fun attachView(view: FeedbackContract.View) {
        this.view = view

        compositeDisposable += view.getMessage().subscribe { feedback.message = it }
        compositeDisposable += view.getTopicInput().subscribe { updateFeedbackTopic(it) }

        compositeDisposable += view.getEmail().subscribe { feedback.email = it }
        compositeDisposable += view.getIncludeEmail().subscribe { onIncludeEmailChanged(it) }
        compositeDisposable += view.getIncludeLocation().subscribe { onIncludeLocationChanged(it) }
        listenForLocation()
    }

    override fun listenForLocation() {
        // todo add again
        if (SDK_INT < M/* || PermissionHelper.checkPermission(ACCESS_FINE_LOCATION, context,view )*/) {
            compositeDisposable += checkNotNull(view).getLocation()
                .subscribe { feedback.location = it }
        }
    }

    private fun updateFeedbackTopic(topicButton: Int) {
        if (topicButton == R.id.tumInGeneralRadioButton) {
            feedback.topic = Const.FEEDBACK_TOPIC_GENERAL
        } else {
            feedback.topic = Const.FEEDBACK_TOPIC_APP
        }
    }

    private fun onIncludeLocationChanged(includeLocation: Boolean) {
        feedback.includeLocation = includeLocation
        if (includeLocation) {
            listenForLocation()
        }

    }

    private fun onIncludeEmailChanged(includeEmail: Boolean) {
        feedback.includeEmail = includeEmail
        view?.showEmailInput(includeEmail && lrzId.isEmpty())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        if (savedInstanceState.containsKey(Const.FEEDBACK)) {
            feedback = savedInstanceState.getParcelable(Const.FEEDBACK)!!

            feedback.message?.let {
                view?.setFeedback(it)
            }
        }
    }

    override fun initEmail() {
        val hasLrzId = lrzId.isNotEmpty()
        feedback.includeEmail = hasLrzId

        if (hasLrzId) {
            feedback.email = "$lrzId@mytum.de"
        }
    }

    private fun isEmailValid(email: String?): Boolean {
        if (email == null) {
            return false
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onSendFeedback() {
        if (feedback.message?.isEmpty() == true) {
            view?.showEmptyMessageError()
        } else {
            view?.showSendConfirmationDialog()
        }
    }

    override fun onConfirmSend() {
        if (!feedback.includeEmail) {
            feedback.email = null
        }

        if (!feedback.includeLocation) {
            feedback.latitude = null
            feedback.longitude = null
        } else {
            if (feedback.location == null) {
                showNoLocationAccessDialog()
                return
            }
        }

        val images = feedback.picturePaths.size
        feedback.imageCount = images

        imagesSent = 0

        if (feedback.includeEmail && !isEmailValid(feedback.email)) {
            view?.showWarning(context.getString(R.string.invalid_email))
            return
        }

        view?.showProgressDialog()
        sendFeedbackCall.enqueue(object : Callback<FeedbackResult> {
            override fun onResponse(
                call: Call<FeedbackResult>,
                response: Response<FeedbackResult>
            ) {
                val result = response.body()
                if (result == null || result.isSuccess) {
                    view?.showSendErrorDialog()
                }

                if (feedback.imageCount == 0) {
                    view?.onFeedbackSent()
                } else {
                    sendImages()
                }
            }

            override fun onFailure(call: Call<FeedbackResult>, t: Throwable) {
                if (!call.isCanceled) {
                    view?.showSendErrorDialog()
                }
            }
        })
    }

    private fun sendImages() {
        val imagePaths = feedback.picturePaths.toTypedArray()
        sendImagesCalls = tumCabeClient.sendFeedbackImages(feedback, imagePaths)

        for (call in sendImagesCalls) {
            call.enqueue(object : Callback<FeedbackResult> {
                override fun onResponse(
                    call: Call<FeedbackResult>,
                    response: Response<FeedbackResult>
                ) {
                    val result = response.body()
                    if (result == null || !result.isSuccess) {
                        view?.showSendErrorDialog()
                        return
                    }

                    imagesSent++

                    if (imagesSent == feedback.imageCount) {
                        view?.onFeedbackSent()
                    }

                    Utils.log("Sent " + imagesSent + " of " + feedback.imageCount + " images")
                }

                override fun onFailure(call: Call<FeedbackResult>, t: Throwable) {
                    if (!call.isCanceled) {
                        view?.showSendErrorDialog()
                    }
                }
            })
        }
    }

    private fun showNoLocationAccessDialog() {
        val title = context.getString(R.string.location_services_off_title)
        val message = context.getString(R.string.location_services_off_message)
        view?.showDialog(title, message)
    }

    override fun detachView() {
        clearPictures()
        sendFeedbackCall.cancel()

        for (call in sendImagesCalls) {
            call.cancel()
        }

        compositeDisposable.dispose()
        view = null
    }

    private fun clearPictures() {
        for (path in feedback.picturePaths) {
            File(path).delete()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(Const.FEEDBACK, feedback)
    }

    companion object {
        const val PERMISSION_LOCATION = 13
        const val PERMISSION_CAMERA = 14
        const val PERMISSION_FILES = 15
    }
}
