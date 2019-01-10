package ru.oldowl

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import ru.oldowl.databinding.ActivityLaunchBinding

class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityLaunchBinding = DataBindingUtil.setContentView(this, R.layout.activity_launch)
        binding.message = "Privet!"
    }
}
