package de.tum.`in`.tumcampusapp.component.other.generic.drawer

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.AccessTokenManager
import de.tum.`in`.tumcampusapp.component.ui.onboarding.WizNavStartActivity
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import java.util.Locale

class DrawerHeaderInflater(
    private val context: Context
) {

    fun inflater(navigationView: NavigationView) {
        val headerView = navigationView.inflateHeaderView(R.layout.drawer_header)
        val imageView = headerView.findViewById<CircleImageView>(R.id.profileImageView)
        val nameTextView = headerView.findViewById<TextView>(R.id.nameTextView)
        val emailTextView = headerView.findViewById<TextView>(R.id.emailTextView)
        val loginButton = headerView.findViewById<MaterialButton>(R.id.loginButton)

        val isLoggedIn = AccessTokenManager.hasValidAccessToken(context)

        if (isLoggedIn) {
            val name = Utils.getSetting(context, Const.CHAT_ROOM_DISPLAY_NAME, "")
            if (name.isNotEmpty()) {
                nameTextView.text = name
            } else {
                nameTextView.visibility = View.INVISIBLE
            }

            val lrzId = Utils.getSetting(context, Const.LRZ_ID, "")
            val email = if (lrzId.isNotEmpty()) "$lrzId@mytum.de" else ""
            if (email.isNotEmpty()) {
                emailTextView.text = email
            } else {
                emailTextView.visibility = View.GONE
            }

            loginButton.visibility = View.GONE
        } else {
            nameTextView.visibility = View.GONE
            emailTextView.visibility = View.GONE
            imageView.visibility = View.GONE

            loginButton.visibility = View.VISIBLE
            loginButton.setOnClickListener {
                val intent = Intent(context, WizNavStartActivity::class.java)
                context.startActivity(intent)
            }
        }

        fetchProfilePicture(headerView)

        val divider = headerView.findViewById<View>(R.id.divider)
        val rainbowBar = headerView.findViewById<View>(R.id.rainbow_bar)

        if (Utils.getSettingBool(context, Const.RAINBOW_MODE, false)) {
            divider.visibility = View.GONE
            rainbowBar.visibility = View.VISIBLE
        } else {
            divider.visibility = View.VISIBLE
            rainbowBar.visibility = View.GONE
        }
    }

    private fun fetchProfilePicture(headerView: View) {
        val id = Utils.getSetting(context, Const.TUMO_PIDENT_NR, "")
        val parts = id.split("\\*".toRegex()).toTypedArray()
        if (parts.size != 2) {
            return
        }

        val group = parts[0]
        val personId = parts[1]
        val url = String.format(Locale.getDefault(),
            Const.TUM_ONLINE_PROFILE_PICTURE_URL_FORMAT_STRING, group, personId)

        val imageView = headerView.findViewById<CircleImageView>(R.id.profileImageView)
        Picasso.get()
            .load(url)
            .error(R.drawable.photo_not_available)
            .placeholder(R.drawable.photo_not_available)
            .into(imageView)
    }

}
