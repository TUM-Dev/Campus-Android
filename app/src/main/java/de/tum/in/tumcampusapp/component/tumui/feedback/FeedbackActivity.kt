package de.tum.`in`.tumcampusapp.component.tumui.feedback

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.RequiresApi
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
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackPresenter.Companion.REQUEST_GALLERY
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackPresenter.Companion.REQUEST_TAKE_PHOTO
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_feedback.*
import java.io.File
import javax.inject.Inject

class FeedbackActivity : BaseActivity(R.layout.activity_feedback), FeedbackContract.View {

    private lateinit var thumbnailsAdapter: FeedbackThumbnailsAdapter
    private var progressDialog: AlertDialog? = null

    @Inject
    lateinit var presenter: FeedbackContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        initPictureGallery()

        if (savedInstanceState == null) {
            presenter.initEmail()
        }
        initIncludeEmail()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState)
    }

    private fun initPictureGallery() {
        imageRecyclerView.layoutManager = LinearLayoutManager(this, HORIZONTAL, false)

        val imagePaths = presenter.feedback.picturePaths
        val thumbnailSize = resources.getDimension(R.dimen.thumbnail_size).toInt()
        thumbnailsAdapter = FeedbackThumbnailsAdapter(imagePaths, { onThumbnailRemoved(it) }, thumbnailSize)
        imageRecyclerView.adapter = thumbnailsAdapter

        addImageButton.setOnClickListener { showImageOptionsDialog() }
    }

    private fun onThumbnailRemoved(path: String) {
        val builder = AlertDialog.Builder(this)
        val view = View.inflate(this, R.layout.picture_dialog, null)

        val imageView = view.findViewById<ImageView>(R.id.feedback_big_image)
        imageView.setImageURI(Uri.fromFile(File(path)))

        builder.setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.feedback_remove_image) { _, _ -> removeThumbnail(path) }

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        dialog.show()
    }

    private fun removeThumbnail(path: String) {
        presenter.removeImage(path)
    }

    private fun showImageOptionsDialog() {
        val options = arrayOf(getString(R.string.feedback_take_picture), getString(R.string.gallery))
        val alertDialog = AlertDialog.Builder(this)
                .setTitle(R.string.feedback_add_picture)
                .setItems(options) { _, index -> presenter.onImageOptionSelected(index) }
                .setNegativeButton(R.string.cancel, null)
                .create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        alertDialog.show()
    }

    override fun getMessage(): Observable<String> =
            feedbackMessage.textChanges().map { it.toString() }

    override fun getEmail(): Observable<String> =
            customEmailInput.textChanges().map { it.toString() }

    override fun getTopicInput(): Observable<Int> = radioButtonsGroup.checkedChanges()
    override fun getIncludeEmail(): Observable<Boolean> = includeEmailCheckbox.checkedChanges()
    override fun getIncludeLocation(): Observable<Boolean> = includeLocationCheckBox.checkedChanges()


    @SuppressLint("MissingPermission")
    override fun getLocation(): Observable<Location> = RxLocation(this).location().updates(LocationRequest.create())

    override fun setFeedback(message: String) {
        feedbackMessage.setText(message)
    }

    override fun openCamera(intent: Intent) {
        startActivityForResult(intent, REQUEST_TAKE_PHOTO)
    }

    override fun openGallery(intent: Intent) {
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun showPermissionRequestDialog(permission: String, requestCode: Int) {
        requestPermissions(arrayOf(permission), requestCode)
    }

    private fun initIncludeLocation() {
        includeLocationCheckBox.isChecked = presenter.feedback.includeLocation
    }

    private fun initIncludeEmail() {
        val feedback = presenter.feedback
        val email = feedback.email
        includeEmailCheckbox.isChecked = feedback.includeEmail

        if (presenter.lrzId.isEmpty()) {
            includeEmailCheckbox.text = getString(R.string.feedback_include_email)
            customEmailInput.setText(email)
        } else {
            includeEmailCheckbox.text = getString(R.string.feedback_include_email_tum_id, email)
        }
    }

    override fun showEmailInput(show: Boolean) {
        customEmailLayout.isVisible = show
    }

    fun onSendClicked(view: View) {
        presenter.onSendFeedback()
    }

    override fun showEmptyMessageError() {
        feedbackMessage.error = getString(R.string.feedback_empty)
    }

    override fun showWarning(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showDialog(title: String, message: String) {
        val dialog = AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        dialog.show()
    }

    override fun showProgressDialog() {
        progressDialog = AlertDialog.Builder(this)
                .setTitle(R.string.feedback_sending)
                .setView(ProgressBar(this))
                .setCancelable(false)
                .setNeutralButton(R.string.cancel, null)
                .create()
        progressDialog?.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        progressDialog?.show()
    }

    override fun showSendErrorDialog() {
        progressDialog?.dismiss()

        val errorDialog = AlertDialog.Builder(this)
                .setMessage(R.string.feedback_sending_error)
                .setIcon(R.drawable.ic_error_outline)
                .setPositiveButton(R.string.try_again) { _, _ -> presenter.feedback }
                .setNegativeButton(R.string.cancel, null)
                .create()
        errorDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        errorDialog.show()
    }

    override fun onFeedbackSent() {
        progressDialog?.dismiss()
        Toast.makeText(this, R.string.feedback_send_success, LENGTH_SHORT).show()
        finish()
    }

    override fun showSendConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(this)
                .setMessage(R.string.send_feedback_question)
                .setPositiveButton(R.string.send) { _, _ -> presenter.onConfirmSend() }
                .setNegativeButton(R.string.cancel, null)
                .create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        alertDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            REQUEST_TAKE_PHOTO -> presenter.onNewImageTaken()
            REQUEST_GALLERY -> {
                val filePath = data?.data
                presenter.onNewImageSelected(filePath)
            }
        }
    }

    override fun onImageAdded(path: String) {
        thumbnailsAdapter.addImage(path)
    }

    override fun onImageRemoved(position: Int) {
        thumbnailsAdapter.removeImage(position)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (grantResults.isEmpty()) {
            return
        }

        val isGranted = grantResults[0] == PERMISSION_GRANTED

        when (requestCode) {
            PERMISSION_LOCATION -> {
                includeLocationCheckBox.isChecked = isGranted
                if (isGranted) {
                    presenter.listenForLocation()
                }
            }
            PERMISSION_CAMERA -> {
                if (isGranted) {
                    presenter.takePicture()
                }
            }
            PERMISSION_FILES -> {
                if (isGranted) {
                    presenter.openGallery()
                }
            }
        }
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

}
