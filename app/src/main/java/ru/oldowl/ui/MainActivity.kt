package ru.oldowl.ui

import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import ru.oldowl.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        drawer_view.setStatusBarBackground(R.color.colorPrimaryDark)

        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawer_view,
                toolbar,
                R.string.open_drawer,
                R.string.close_drawer)

        actionBarDrawerToggle.syncState()

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ArticleListFragment())
                .commit()
    }
}
