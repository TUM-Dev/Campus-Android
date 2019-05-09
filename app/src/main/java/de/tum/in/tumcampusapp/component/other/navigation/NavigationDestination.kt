package de.tum.`in`.tumcampusapp.component.other.navigation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

sealed class NavigationDestination

data class SystemActivity(
        val clazz: Class<out AppCompatActivity>,
        val options: Bundle?
) : NavigationDestination()

data class SystemIntent(
        val intent: Intent
) : NavigationDestination()