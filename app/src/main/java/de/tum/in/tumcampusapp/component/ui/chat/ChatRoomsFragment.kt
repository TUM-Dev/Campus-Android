package de.tum.`in`.tumcampusapp.component.ui.chat

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.model.TUMCabeVerification
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl.BYPASS_CACHE
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl.USE_CACHE
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.NoResultsAdapter
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForAccessingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.Lecture
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LecturesResponse
import de.tum.`in`.tumcampusapp.component.ui.chat.activity.ChatActivity
import de.tum.`in`.tumcampusapp.component.ui.chat.activity.JoinRoomScanActivity
import de.tum.`in`.tumcampusapp.component.ui.chat.adapter.ChatRoomListAdapter
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatMember
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatRoom
import de.tum.`in`.tumcampusapp.component.ui.chat.model.ChatRoomAndLastMessage
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import kotlinx.android.synthetic.main.fragment_chat_rooms.chatRoomTabs
import kotlinx.android.synthetic.main.fragment_chat_rooms.chatRoomsListView
import org.jetbrains.anko.support.v4.runOnUiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.UnknownHostException

class ChatRoomsFragment : FragmentForAccessingTumOnline<LecturesResponse>(
    R.layout.fragment_chat_rooms,
    R.string.chat_rooms
) {

    private var currentMode = ChatRoom.MODE_JOINED
    private val manager: ChatRoomController by lazy {
        ChatRoomController(requireContext())
    }

    private var currentChatRoom: ChatRoom? = null
    private var currentChatMember: ChatMember? = null

    private lateinit var chatRoomsAdapter: ChatRoomListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatRoomsListView.setOnItemClickListener(this::onItemClick)

        chatRoomTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // show the given tab
                currentMode = 1 - tab.position
                loadPersonalLectures(USE_CACHE)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit

            override fun onTabReselected(tab: TabLayout.Tab) {
                chatRoomsListView.smoothScrollToPosition(0)
            }
        })

        chatRoomTabs.addTab(chatRoomTabs.newTab().setText(R.string.joined))
        chatRoomTabs.addTab(chatRoomTabs.newTab().setText(R.string.not_joined))
    }

    override fun onStart() {
        super.onStart()
        loadPersonalLectures(USE_CACHE)
    }

    override fun onRefresh() {
        loadPersonalLectures(BYPASS_CACHE)
    }

    private fun loadPersonalLectures(cacheControl: CacheControl) {
        val apiCall = apiClient.getPersonalLectures(cacheControl)
        fetch(apiCall)
    }

    override fun onDownloadSuccessful(response: LecturesResponse) {
        val lectures = response.lectures

        // We're starting more background work, so we show a loading indicator again
        showLoadingStart()

        val handlerThread = HandlerThread("UpdateDatabaseThread")
        handlerThread.start()

        val handler = Handler(handlerThread.looper)
        handler.post { createLectureRoomsAndUpdateDatabase(lectures) }
    }

    private fun createLectureRoomsAndUpdateDatabase(lectures: List<Lecture>) {
        manager.createLectureRooms(lectures)

        populateCurrentChatMember()

        val currentChatMember = currentChatMember
        if (currentChatMember != null) {
            try {
                val verification = TUMCabeVerification.create(requireContext(), null)
                if (verification == null) {
                    requireActivity().finish()
                    return
                }

                val rooms = TUMCabeClient
                    .getInstance(requireContext())
                    .getMemberRooms(currentChatMember.id, verification)
                manager.replaceIntoRooms(rooms)
            } catch (e: IOException) {
                Utils.log(e)

                if (e is UnknownHostException) {
                    showErrorSnackbar(R.string.error_no_internet_connection)
                } else {
                    showErrorSnackbar(R.string.error_something_wrong)
                }
            }
        }

        val chatRoomAndLastMessages = manager.getAllByStatus(currentMode)
        runOnUiThread {
            displayChatRoomsAndMessages(chatRoomAndLastMessages)
            showLoadingEnded()
        }
    }

    private fun displayChatRoomsAndMessages(results: List<ChatRoomAndLastMessage>) {
        if (results.isEmpty()) {
            chatRoomsListView.adapter = NoResultsAdapter(requireContext())
        } else {
            chatRoomsAdapter = ChatRoomListAdapter(requireContext(), results, currentMode)
            chatRoomsListView.adapter = chatRoomsAdapter
        }
    }

    /**
     * Gets the saved local information for the user
     */
    private fun populateCurrentChatMember() {
        if (currentChatMember == null) {
            currentChatMember = Utils.getSetting(
                requireContext(), Const.CHAT_MEMBER, ChatMember::class.java)
        }
    }

    private fun onItemClick(adapterView: AdapterView<*>, v: View, position: Int, id: Long) {
        val item = chatRoomsListView.getItemAtPosition(position) as ChatRoomAndLastMessage

        // set bundle for LectureDetails and show it
        val bundle = Bundle()
        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtras(bundle)

        val chatRoomUid = item.chatRoomDbRow!!
            .semesterId + ':'.toString() + item.chatRoomDbRow!!
            .name
        createOrJoinChatRoom(chatRoomUid)
    }

    /**
     * Creates a given chat room if it does not exist and joins it
     * Works asynchronously.
     */
    private fun createOrJoinChatRoom(name: String) {
        Utils.logv("create or join chat room $name")
        if (this.currentChatMember == null) {
            Utils.showToast(requireContext(), getString(R.string.chat_not_setup))
            return
        }

        currentChatRoom = ChatRoom(name)

        val verification = TUMCabeVerification.create(requireContext(), null)
        if (verification == null) {
            requireActivity().finish()
            return
        }

        val callback = object : Callback<ChatRoom> {
            override fun onResponse(call: Call<ChatRoom>, response: Response<ChatRoom>) {
                if (!response.isSuccessful) {
                    Utils.logv("Error creating&joining chat room: " + response.message())
                    return
                }

                // The POST request is successful: go to room. API should have auto joined it
                Utils.logv("Success creating&joining chat room: " + response.body()!!)
                currentChatRoom = response.body()

                manager.join(currentChatRoom)

                // When we show joined chat rooms open chat room directly
                if (currentMode == ChatRoom.MODE_JOINED) {
                    moveToChatActivity()
                } else { // Otherwise show a nice information, that we added the room
                    val rooms = manager.getAllByStatus(currentMode)

                    runOnUiThread {
                        chatRoomsAdapter.updateRooms(rooms)
                        Utils.showToast(requireContext(), R.string.joined_chat_room)
                    }
                }
            }

            override fun onFailure(call: Call<ChatRoom>, t: Throwable) {
                Utils.log(t, "Failure creating/joining chat room - trying to GET it from the server")
                Utils.showToastOnUIThread(requireActivity(), R.string.activate_key)
            }
        }

        TUMCabeClient
            .getInstance(requireContext())
            .createRoom(currentChatRoom, verification, callback)
    }

    /**
     * Opens [ChatActivity]
     */
    private fun moveToChatActivity() {
        // We are sure that both currentChatRoom and currentChatMember exist at this point
        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra(Const.CURRENT_CHAT_ROOM, Gson().toJson(currentChatRoom))
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_activity_chat_rooms, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_chat_room -> {
                showChatRoomCreationDialog()
                true
            }
            R.id.action_join_chat_room -> {
                joinChatRoom()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Prompt the user to type in a name for the new chat room
     */
    private fun showChatRoomCreationDialog() {
        // Set an EditText view to get user input
        val view = View.inflate(requireContext(), R.layout.dialog_input, null)
        val input = view.findViewById<EditText>(R.id.inputEditText)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.new_chat_room)
            .setMessage(R.string.new_chat_room_desc)
            .setView(view)
            .setPositiveButton(R.string.create) { dialogInterface, whichButton ->
                val value = input.text
                    .toString()
                val randId = Integer.toHexString((Math.random() * 4096).toInt())
                createOrJoinChatRoom("$randId:$value")
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .apply {
                window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
            }
            .show()
    }

    private fun joinChatRoom() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionCheck = ActivityCompat
                .checkSelfPermission(requireContext(), Manifest.permission.CAMERA)

            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
            } else {
                startJoinRoom()
            }
        } else {
            startJoinRoom()
        }
    }

    private fun startJoinRoom() {
        val intent = Intent(requireContext(), JoinRoomScanActivity::class.java)
        startActivityForResult(intent, JOIN_ROOM_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val grantResult = grantResults.firstOrNull()
        if (requestCode == CAMERA_REQUEST_CODE && grantResult == PackageManager.PERMISSION_GRANTED) {
            startJoinRoom()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == JOIN_ROOM_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val name = data.getStringExtra("name") ?: return
            if (name.getOrNull(3) == ':') {
                createOrJoinChatRoom(name)
            } else {
                Utils.showToast(requireContext(), R.string.invalid_chat_room)
            }
        }
    }

    private companion object {
        private const val CAMERA_REQUEST_CODE = 34
        private const val JOIN_ROOM_REQUEST_CODE = 22

        @JvmStatic
        fun newInstance() = ChatRoomsFragment()
    }
}
