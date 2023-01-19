package de.tum.`in`.tumcampusapp.component.tumui.feedback

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.google.android.gms.location.LocationRequest
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges
import com.patloew.rxlocation.RxLocation
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackPresenter.Companion.PERMISSION_CAMERA
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackPresenter.Companion.PERMISSION_FILES
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackPresenter.Companion.PERMISSION_LOCATION
import de.tum.`in`.tumcampusapp.databinding.ActivityFeedbackBinding
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.ThemedAlertDialogBuilder
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.camera.CameraContract
import de.tum.`in`.tumcampusapp.utils.camera.CameraThumbnailsAdapter
import de.tum.`in`.tumcampusapp.utils.camera.CameraUtils
import io.reactivex.Observable
import java.io.File
import javax.inject.Inject

class FeedbackActivity : BaseActivity(R.layout.activity_feedback), FeedbackContract.View, CameraContract.View {
    private var progressDialog: AlertDialog? = null

    @Inject
    lateinit var presenter: FeedbackContract.Presenter
    @Inject
    lateinit var presenterCamera: CameraContract.Presenter
    private lateinit var cameraUtils: CameraUtils

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

        presenter.attachView(this)

        if (savedInstanceState != null) {
            presenter.onRestoreInstanceState(savedInstanceState)
        }

        initIncludeLocation()
        initCameraUI()

        if (savedInstanceState == null) {
            presenter.initEmail()
        }
        initIncludeEmail()
    }

    private fun initCameraUI() {
        cameraUtils = CameraUtils(this, this as CameraContract.View)
        binding.addImageButton.setOnClickListener { cameraUtils.showImageOptionsDialog() }
        cameraUtils.initRecyclerView(binding.imageRecyclerView)

    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenterCamera.onSaveInstanceState(outState)
    }

    override fun getMessage(): Observable<String> =
            binding.feedbackMessage.textChanges().map { it.toString() }

    override fun getEmail(): Observable<String> =
            binding.customEmailInput.textChanges().map { it.toString() }

    override fun getTopicInput(): Observable<Int> = binding.radioButtonsGroup.checkedChanges()
    override fun getIncludeEmail(): Observable<Boolean> = binding.includeEmailCheckbox.checkedChanges()
    override fun getIncludeLocation(): Observable<Boolean> = binding.includeLocationCheckBox.checkedChanges()

    @SuppressLint("MissingPermission")
    override fun getLocation(): Observable<Location> = RxLocation(this).location().updates(LocationRequest.create())

    override fun setFeedback(message: String) {
        binding.feedbackMessage.setText(message)
    }

    private val cameraLauncher = registerForActivityResult(StartActivityForResult()) {
        cameraUtils.onNewImageTaken()
    }

    private val galleryLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        val filePath = result.data?.data
        cameraUtils.onNewImageSelected(filePath)
    }

    override fun openCamera(intent: Intent) {
        try {
            cameraLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_unknown, LENGTH_SHORT).show()
        }
    }

    override fun openGallery(intent: Intent) {
        try {
            galleryLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_unknown, LENGTH_SHORT).show()
        }
    }

    private fun initIncludeLocation() {
        binding.includeLocationCheckBox.isChecked = presenter.feedback.includeLocation
    }

    private fun initIncludeEmail() {
        val feedback = presenter.feedback
        val email = feedback.email
        with(binding) {
            includeEmailCheckbox.isChecked = feedback.includeEmail

            if (presenter.lrzId.isEmpty()) {
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
        presenter.onSendFeedback()
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
                .setPositiveButton(R.string.try_again) { _, _ -> presenter.feedback }
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
                .setPositiveButton(R.string.send) { _, _ -> presenter.onConfirmSend() }
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    override fun onImageAdded(path: String) {
        cameraUtils.onImageAdded(path)
    }

    override fun onImageRemoved(position: Int) {
        cameraUtils.onImageRemoved(position)
    }

    override fun onDestroy() {
        presenterCamera.detachView()
        super.onDestroy()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        cameraUtils.processPermissionResult(permissions)
    }

    override fun showPermissionRequestDialog(permission: String) {
        requestPermissionLauncher.launch(arrayOf(permission))
    }
}
