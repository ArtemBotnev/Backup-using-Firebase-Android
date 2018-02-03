package ru.artembotnev.basesaver

import android.os.Bundle
import android.support.v4.app.Fragment

import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Created by Artem Botnev on 08.12.2017.
 */

class MainActivity : ParentActivity() {
    override fun createFragment(): Fragment =
            ListFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAnalytics.getInstance(this)
    }
}
