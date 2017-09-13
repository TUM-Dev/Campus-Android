package de.tum.`in`.tumcampusapp.models.tumcabe

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.auxiliary.Utils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class ChatMessage {

    var id: Int = 0
    var text: String? = null
    var member: ChatMember? = null
    var timestamp: String? = null
    var signature: String? = null
    var status: Int = 0
    var previous: Int = 0
    var room: Int = 0
    private var read: Boolean = false
    var internalID: Int = 0

    /**
     * Default constructor: called by gson when parsing an element
     */
    constructor() {
        this.status = STATUS_SENT
    }

    constructor(text: String) : super() {
        this.text = text
    }

    /**
     * Called when creating a new chat message
     * @param text Chat message text
     * *
     * @param member Member who sent the message
     */
    constructor(text: String, member: ChatMember) : super() {
        this.text = text
        this.member = member
        this.status = STATUS_SENDING
        this.previous = 0
        this.setNow()
    }

    constructor(id: Int, text: String, member: ChatMember, timestamp: String, previous: Int) : super() {
        this.id = id
        this.text = text
        this.member = member
        this.timestamp = timestamp
        this.status = STATUS_SENT
        this.previous = previous
    }

    val timestampDate: Date
        get() {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
            var time = Date()
            try {
                time = formatter.parse(this.timestamp)
            } catch (e: ParseException) {
                Utils.log(e)
            }

            return time
        }

    val statusStringRes: Int
        get() {
            if (status == STATUS_SENT) {
                return R.string.status_sent
            } else if (status == STATUS_SENDING) {
                return R.string.status_sending
            } else {
                return R.string.status_sending_failed
            }
        }

    fun setNow() {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH) // 2014-06-30T16:31:57.878Z
        timestamp = formatter.format(Date())
    }

    fun setRead(read: Boolean) {
        this.read = read
    }

    fun isRead(): Boolean {
        return read
    }

    fun getRead(): Boolean {
        return read
    }

    companion object {

        val STATUS_SENDING = 1
        val STATUS_SENT = 0
        val STATUS_SENDING_FAILED = -1

        private fun isToday(date: Date): Boolean {
            val passedDate = Calendar.getInstance()
            passedDate.time = date // your date

            val today = Calendar.getInstance() // today

            return today.get(Calendar.YEAR) == passedDate.get(Calendar.YEAR) && today.get(Calendar.DAY_OF_YEAR) == passedDate.get(Calendar.DAY_OF_YEAR)
        }

        private fun isYesterday(date: Date): Boolean {
            val passedDate = Calendar.getInstance()
            passedDate.time = date

            val yesterday = Calendar.getInstance() // today
            yesterday.add(Calendar.DAY_OF_YEAR, -1) // yesterday

            return yesterday.get(Calendar.YEAR) == passedDate.get(Calendar.YEAR) && yesterday.get(Calendar.DAY_OF_YEAR) == passedDate.get(Calendar.DAY_OF_YEAR)
        }
    }
}
