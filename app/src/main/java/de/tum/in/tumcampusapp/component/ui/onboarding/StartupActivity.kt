package de.tum.`in`.tumcampusapp.component.ui.onboarding

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.lifecycle.LiveDataReactiveStreams
import com.crashlytics.android.Crashlytics
import de.tum.`in`.tumcampusapp.BuildConfig.DEBUG
import de.tum.`in`.tumcampusapp.BuildConfig.VERSION_CODE
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.AuthenticationManager
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseActivity
import de.tum.`in`.tumcampusapp.component.other.generic.activity.BaseNavigationActivity
import de.tum.`in`.tumcampusapp.service.DownloadWorker
import de.tum.`in`.tumcampusapp.service.StartSyncReceiver
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.observe
import io.fabric.sdk.android.Fabric
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_startup.container
import kotlinx.android.synthetic.main.activity_startup.startupLoadingProgressBar
import kotlinx.android.synthetic.main.activity_startup.startupTumLogo
import org.jetbrains.anko.doAsync
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class StartupActivity : BaseActivity(R.layout.activity_startup) {

    private val initializationFinished = AtomicBoolean(false)
    private var tapCounter = 0 // for easter egg

    @Inject
    lateinit var workerActions: DownloadWorker.WorkerActions

    @Inject
    lateinit var authManager: AuthenticationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.downloadComponent().inject(this)

        // Only use Crashlytics if we are not compiling debug
        val isDebuggable = applicationInfo.isDebuggable
        if (!DEBUG && !isDebuggable) {
            Fabric.with(this, Crashlytics())
            Crashlytics.setString("TUMID", Utils.getSetting(this, Const.LRZ_ID, ""))
            Crashlytics.setString("DeviceID", AuthenticationManager.getDeviceID(this))
        }

        val savedAppVersion = Utils.getSettingInt(this, Const.SAVED_APP_VERSION, VERSION_CODE)
        if (savedAppVersion < VERSION_CODE) {
            Utils.setSetting(this, Const.SHOW_UPDATE_NOTE, true)
            Utils.setSetting(this, Const.UPDATE_MESSAGE, "")
        }
        // Always set current app version, otherwise it will never be initialized and the update
        // note is never displayed
        Utils.setSetting(this, Const.SAVED_APP_VERSION, VERSION_CODE)

        initEasterEgg()
        doAsync {
            initApp()
        }
    }

    private fun initEasterEgg() {
        if (Utils.getSettingBool(this, Const.RAINBOW_MODE, false)) {
            startupTumLogo.setImageResource(R.drawable.tum_logo_rainbow)
        }

        container.setOnClickListener {
            if (tapCounter++ % 3 == 0) {
                // Switch to the other logo and invert the setting
                val shouldEnableRainbow = Utils.getSettingBool(this, Const.RAINBOW_MODE, false).not()

                if (shouldEnableRainbow) {
                    startupTumLogo.setImageResource(R.drawable.tum_logo_rainbow)
                } else {
                    startupTumLogo.setImageResource(R.drawable.tum_logo_blue)
                }

                Utils.setSetting(this, Const.RAINBOW_MODE, shouldEnableRainbow)
            }
        }
    }

    private fun initApp() {
        // Migrate all settings - we somehow ended up having two different shared prefs: join them
        // back together
        Utils.migrateSharedPreferences(this)

        // Check that we have a private key setup in order to authenticate this device
        authManager.generatePrivateKey(null)

        // On first setup show remark that loading could last longer than normally
        runOnUiThread {
            startupLoadingProgressBar.show()
        }

        // Start download workers and listen for finalization
        val downloadActions = Flowable
            .fromCallable(this::performAllWorkerActions)
            .onErrorReturnItem(Unit)
            .subscribeOn(Schedulers.io())

        runOnUiThread {
            LiveDataReactiveStreams
                .fromPublisher(downloadActions)
                .observe(this) { openMainActivityIfInitializationFinished() }
        }

        // Start background service and ensure cards are set
        sendBroadcast(Intent(this, StartSyncReceiver::class.java))

        // Request permissions for Android 6 and up
        requestLocationPermission()
    }

    private fun performAllWorkerActions() {
        for (action in workerActions.actions) {
            action.execute(CacheControl.USE_CACHE)
        }
    }

    private fun requestLocationPermission() {
        when {
            hasLocationPermissions -> openMainActivityIfInitializationFinished()
            shouldShowRationale -> runOnUiThread { showLocationPermissionRationaleDialog() }
            else -> requestPermissions(this, PERMISSIONS_LOCATION, REQUEST_LOCATION)
        }
    }

    private val hasLocationPermissions: Boolean
        get() = PERMISSIONS_LOCATION.all { checkSelfPermission(this, it) == PERMISSION_GRANTED }

    private val shouldShowRationale: Boolean
        get() = PERMISSIONS_LOCATION.all { shouldShowRequestPermissionRationale(this, it) }

    /**
     * Displays a dialog to the user explaining why we need the location permissions
     */
    private fun showLocationPermissionRationaleDialog() {
        AlertDialog.Builder(this, R.style.Theme_MaterialComponents_Light_Dialog)
            .setMessage(R.string.permission_location_explanation)
            .setPositiveButton(R.string.ok) { _, _ ->
                requestPermissions(this, PERMISSIONS_LOCATION, REQUEST_LOCATION)
            }
            .create()
            .apply {
                window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
            }
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        openMainActivityIfInitializationFinished()
    }

    private fun openMainActivityIfInitializationFinished() {
        if (initializationFinished.compareAndSet(false, true) || isFinishing) {
            // If the initialization process is not yet finished or if the Activity is
            // already being finished, there's no need to open MainActivity.
            return
        }
        openMainActivity()
    }

    private fun openMainActivity() {
        val intent = Intent(this, BaseNavigationActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
    }

    private val ApplicationInfo.isDebuggable: Boolean
        get() = 0 != (flags and ApplicationInfo.FLAG_DEBUGGABLE)

    private companion object {
        private const val REQUEST_LOCATION = 0
        private val PERMISSIONS_LOCATION = arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
    }
}
