package ru.oldowl.extension

import android.app.Activity
import android.content.Intent
import android.net.Uri

fun Activity.openWebsite(url: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
