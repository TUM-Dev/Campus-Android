package de.tum.`in`.tumcampusapp.component.ui.cafeteria.rating


import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForAccessingTumCabe
import de.tum.`in`.tumcampusapp.component.tumui.feedback.FeedbackPresenter
import de.tum.`in`.tumcampusapp.databinding.FragmentCafeteriaRatingBinding
import de.tum.`in`.tumcampusapp.utils.ImageUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import java.io.File
import java.io.IOException


class CreateCafeteriaRatingFragment : FragmentForAccessingTumCabe<List<String>>(
    R.layout.fragment_cafeteria_rating,
    R.string.create_cafeteria_rating
), AdapterView.OnItemSelectedListener {


    // used to access the photo functionality
    // todo potentially split to another camera "forwarder"
    //  @Inject
    //  lateinit var presenter: FeedbackContract.Presenter

    private val binding by viewBinding(FragmentCafeteriaRatingBinding::bind)


    private val itemsList = ArrayList<CreateTagRatingElement>()
    private lateinit var createTagRatingAdapter: CreateRatingAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cafeterias = arrayOf("Mensa Garching", "mensa leopoldstrasse")
        val meals = arrayOf("Only The Cafeteria", "Pizza Margeritha")
        //  binding.pickCafeteriaSpinner;
        //  val spinner = findViewById(R.id.pickCafeteriaSpinner)
        if (binding.pickCafeteriaCreateSpinner != null) {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item, cafeterias
            )
            binding.pickCafeteriaCreateSpinner.adapter = adapter
        }

        if (binding.pickDishCreateSpinner != null) {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item, meals
            )
            binding.pickDishCreateSpinner.adapter = adapter
        }
        prepareItems()

        // val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        createTagRatingAdapter = CreateRatingAdapter(itemsList)
        val layoutManager = LinearLayoutManager(requireContext())
        binding.tagOptionListRecyclerView.layoutManager = layoutManager
        binding.tagOptionListRecyclerView.adapter = createTagRatingAdapter

// todo eigeneer contract und  co benötigt?
        /*presenter.attachView(view)

        if (savedInstanceState != null) {
            presenter.onRestoreInstanceState(savedInstanceState)
        }*/

        binding.addImageButton.setOnClickListener { showImageOptionsDialog() }
    }

    private fun prepareItems() {
        itemsList.add(CreateTagRatingElement("Waiting time"))
        itemsList.add(CreateTagRatingElement("Variety General"))
        itemsList.add(CreateTagRatingElement("Variety Vegetarian"))
        itemsList.add(CreateTagRatingElement("Variety Vegan"))
        itemsList.add(CreateTagRatingElement("Enough free tables"))
    }


    private var currentPhotoPath: String? = null


    private fun showImageOptionsDialog() {
        val options =
            arrayOf(getString(R.string.feedback_take_picture), getString(R.string.gallery))
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.feedback_add_picture)
            .setItems(options) { _, index -> onImageOptionSelected(index) }
            .setNegativeButton(R.string.cancel, null)
            .create()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        alertDialog.show()
    }

    fun onImageOptionSelected(option: Int) {
        if (option == 0) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkPermission(Manifest.permission.CAMERA)) {
                takePicture()
            }
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                openGallery()
            }
        }
    }

    /**
     * @return true if user has given permission before
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun checkPermission(permission: String): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(requireContext(), permission)

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            val requestCode = when (permission) {
                Manifest.permission.READ_EXTERNAL_STORAGE -> FeedbackPresenter.PERMISSION_FILES
                Manifest.permission.CAMERA -> FeedbackPresenter.PERMISSION_CAMERA
                else -> FeedbackPresenter.PERMISSION_LOCATION
            }

            showPermissionRequestDialog(permission, requestCode)
            return false
        }
        return true
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun showPermissionRequestDialog(permission: String, requestCode: Int) {
        requestPermissions(arrayOf(permission), requestCode)
    }

    fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Create the file where the photo should go
        var photoFile: File? = null
        try {
            photoFile = ImageUtils.createImageFile(requireContext())
            currentPhotoPath = photoFile.absolutePath
        } catch (e: IOException) {
            Utils.log(e)
        }

        if (photoFile == null) {
            return
        }

        val authority = "de.tum.in.tumcampusapp.fileprovider"
        val photoURI = FileProvider.getUriForFile(requireContext(), authority, photoFile)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        try {
            openCamera(takePictureIntent)
        } catch (e: ActivityNotFoundException) {
            photoFile.delete()
        }
    }

    fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

       // val chooser = Intent.createChooser(intent, "Select file")
      //  openGallery(chooser)
    }

    fun openCamera(intent: Intent) {
        startActivityForResult(intent, FeedbackPresenter.REQUEST_TAKE_PHOTO)
    }
    //  val cafeterias = arrayOf("mensa garching", "mensa leopoldstrasse")

    // Drop-down navigation
/*    private val selectCafeteriasSpinner: Spinner
        get() {

            val groupAdapter = object : ArrayAdapter<String>(
                    context!!,
                    android.R.layout.simple_spinner_dropdown_item,
                    android.R.id.text1,
                    cafeterias
            ) {
                val inflater = LayoutInflater.from(context)

                override fun getDropDownView(pos: Int, ignored: View?, parent: ViewGroup): View {
                    val v = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
                    val studyRoomGroup = getItem(pos) ?: return v
                    val nameTextView = v.findViewById<TextView>(android.R.id.text1)
                    nameTextView.text = studyRoomGroup
                    return v
                }
            }

            groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            return binding.pickCafeteriaSpinner.apply {
                adapter = groupAdapter
                onItemSelectedListener = this@CafeteriaRatingFragment
            }
        }*/

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//todo toast mit id
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) = Unit

    /*override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(de.tum.`in`.tumcampusapp.R.layout.activity_cafeteria_rating, container, false)
        displayCafeterias()
        return rootView
    }*/

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//    }


    /*  private fun displayCafeterias() {
          selectCurrentSpinnerItem()
          // binding.spinnerContainer.visibility = View.VISIBLE
          //  showLoadingEnded()
      }

      private fun selectCurrentSpinnerItem() {
          cafeterias.forEachIndexed { i, a ->

            //  selectCafeteriasSpinner.setSelection(i)

          }
      }
  */
    companion object {

        private const val NONE_SELECTED = -1

        @JvmStatic
        fun newInstance() = CreateCafeteriaRatingFragment()
    }

}