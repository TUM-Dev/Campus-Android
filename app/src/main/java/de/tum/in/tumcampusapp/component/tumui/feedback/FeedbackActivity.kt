package de.tum.`in`.tumcampusapp.component.tumui.feedback

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.android.gms.location.LocationRequest
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges
import com.patloew.rxlocation.RxLocation
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.databinding.ActivityFeedbackBinding
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.ThemedAlertDialogBuilder
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.camera.CameraInterface
import io.reactivex.Observable
import javax.inject.Inject

class FeedbackActivity : BaseActivity(R.layout.activity_feedback), FeedbackContract.View {
    private var progressDialog: AlertDialog? = null

    @Inject
    lateinit var feedbackPresenter: FeedbackContract.Presenter

    @Inject
    lateinit var cameraManager: CameraInterface

    private lateinit var binding: ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarFeedback.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val lrzId = Utils.getSetting(this, Const.LRZ_ID, "")
        injector.feedbackComponent()
            .lrzId(lrzId)
            .build()
            .inject(this)

        feedbackPresenter.attachView(this)

        if (savedInstanceState != null) {
            feedbackPresenter.onRestoreInstanceState(savedInstanceState)
        }

        initIncludeLocation()

        if (savedInstanceState == null) {
            feedbackPresenter.initEmail()
        }
        initIncludeEmail()
        binding.addImageButton.setOnClickListener { cameraManager.requestNewImage() }
        cameraManager.init(binding.imageRecyclerView, this as ComponentActivity)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        feedbackPresenter.onSaveInstanceState(outState)
    }

    override fun getMessage(): Observable<String> =
        binding.feedbackMessage.textChanges().map { it.toString() }

    override fun getEmail(): Observable<String> =
        binding.customEmailInput.textChanges().map { it.toString() }

    override fun getTopicInput(): Observable<Int> = binding.radioButtonsGroup.checkedChanges()
    override fun getIncludeEmail(): Observable<Boolean> =
        binding.includeEmailCheckbox.checkedChanges()

    override fun getIncludeLocation(): Observable<Boolean> =
        binding.includeLocationCheckBox.checkedChanges()

    @SuppressLint("MissingPermission")
    override fun getLocation(): Observable<Location> =
        RxLocation(this).location().updates(LocationRequest.create())

    override fun setFeedback(message: String) {
        binding.feedbackMessage.setText(message)
    }

    private fun initIncludeLocation() {
        binding.includeLocationCheckBox.isChecked = feedbackPresenter.feedback.includeLocation
    }

    private fun initIncludeEmail() {
        val feedback = feedbackPresenter.feedback
        val email = feedback.email
        with(binding) {
            includeEmailCheckbox.isChecked = feedback.includeEmail

            if (feedbackPresenter.lrzId.isEmpty()) {
                includeEmailCheckbox.text = getString(R.string.feedback_include_email)
                customEmailInput.setText(email)
            } else {
                includeEmailCheckbox.text = getString(R.string.feedback_include_email_tum_id, email)
            }
        }
    }

    override fun showEmailInput(show: Boolean) {
        binding.customEmailLayout.isVisible = show
    }

    fun onSendClicked(view: View) {
        feedbackPresenter.onSendFeedback()
    }

    override fun showEmptyMessageError() {
        binding.feedbackMessage.error = getString(R.string.feedback_empty)
    }

    override fun showWarning(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showDialog(title: String, message: String) {
        ThemedAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    override fun showProgressDialog() {
        progressDialog = ThemedAlertDialogBuilder(this)
            .setTitle(R.string.feedback_sending)
            .setView(ProgressBar(this))
            .setCancelable(false)
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    override fun showSendErrorDialog() {
        progressDialog?.dismiss()

        ThemedAlertDialogBuilder(this)
            .setMessage(R.string.feedback_sending_error)
            .setIcon(R.drawable.ic_error_outline)
            .setPositiveButton(R.string.try_again) { _, _ -> feedbackPresenter.feedback }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onFeedbackSent() {
        progressDialog?.dismiss()
        Toast.makeText(this, R.string.feedback_send_success, LENGTH_SHORT).show()
        finish()
    }

    override fun showSendConfirmationDialog() {
        ThemedAlertDialogBuilder(this)
            .setMessage(R.string.send_feedback_question)
            .setPositiveButton(R.string.send) { _, _ ->
                feedbackPresenter.onConfirmSend(
                    cameraManager.getImagePaths()
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Delete all Images which were created for this feedback. They will never be used again
     * and only block the memory.
     */
    override fun onDestroy() {
        cameraManager.deleteImageCopies()
        super.onDestroy()
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        feedbackPresenter.processLocationPermissionResult(permissions)
    }

    override fun showLocationPermissionRequestDialog() {
        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }
}
