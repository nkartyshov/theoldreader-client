package ru.oldowl.core.ui

import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.koin.standalone.KoinComponent
import kotlin.coroutines.CoroutineContext

abstract class BaseFragment : Fragment(), CoroutineScope, KoinComponent {
    private val job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onDestroy() {
        super.onDestroy()

        if (job.isActive) {
            job.cancel()
        }
    }
}