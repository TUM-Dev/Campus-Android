package de.tum.`in`.tumcampusapp.service

import android.content.Context
import androidx.work.*
import androidx.work.ListenableWorker.Result.*
import androidx.work.NetworkType.CONNECTED
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.app.exception.NoPrivateKey
import de.tum.`in`.tumcampusapp.component.ui.chat.ChatMessageViewModel
import de.tum.`in`.tumcampusapp.component.ui.chat.repository.ChatMessageLocalRepository
import de.tum.`in`.tumcampusapp.component.ui.chat.repository.ChatMessageRemoteRepository
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Utils
import java.util.concurrent.TimeUnit

/**
 * Service used to send chat messages.
 */
class SendMessageWorker(context: Context, workerParams: WorkerParameters) :
        Worker(context, workerParams) {

    private val tcaDb by lazy { TcaDb.getInstance(applicationContext) }
    private val tumCabeClient by lazy { TUMCabeClient.getInstance(applicationContext) }
    private val authenticationManager by lazy { AuthenticationManager(applicationContext) }

    override fun doWork(): ListenableWorker.Result {
        ChatMessageRemoteRepository.tumCabeClient = tumCabeClient
        ChatMessageLocalRepository.db = tcaDb

        val viewModel = ChatMessageViewModel(ChatMessageLocalRepository, ChatMessageRemoteRepository)
        viewModel.deleteOldEntries()

        return try {
            viewModel.getUnsent()
                    .asSequence()
                    .onEach { it.signature = authenticationManager.sign(it.text) }
                    .forEach { viewModel.sendMessage(it.room, it, applicationContext) }
            success()
        } catch (noPrivateKey: NoPrivateKey) {
            // Retrying doesn't make any sense
            failure()
        } catch (e: Exception) {
            Utils.log(e)
            // Maybe the server is currently busy, but we really want to send the messages
            retry()
        }
    }

    companion object {
        @JvmStatic
        fun getWorkRequest(): WorkRequest {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(CONNECTED)
                    .build()
            return OneTimeWorkRequestBuilder<SendMessageWorker>()
                    .setConstraints(constraints)
                    .build()
        }

        fun getPeriodicWorkRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(CONNECTED)
                    .build()
            return PeriodicWorkRequestBuilder<SendMessageWorker>(3, TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .build()
        }
    }
}