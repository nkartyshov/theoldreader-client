package ru.oldowl.ui

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.navigation_layout.*
import kotlinx.android.synthetic.main.toolbar.*
import org.koin.standalone.inject
import ru.oldowl.R
import ru.oldowl.databinding.ActivityMainBinding
import ru.oldowl.ui.adapter.SubscriptionWithUnreadAdapter
import ru.oldowl.viewmodel.MainViewModel

class MainActivity : BaseActivity() {
    private val mainViewModel: MainViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataBinding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        dataBinding.setLifecycleOwner(this)

        dataBinding.viewModel = mainViewModel
        dataBinding.navigationView.viewModel = mainViewModel
        dataBinding.navigationView.navigationHeader.viewModel = mainViewModel

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawer_view,
                toolbar,
                R.string.open_drawer,
                R.string.close_drawer)

        actionBarDrawerToggle.syncState()

        drawer_view.setStatusBarBackground(R.color.colorPrimaryDark)

        subscription_list.adapter = SubscriptionWithUnreadAdapter(this)
        subscription_list.layoutManager = LinearLayoutManager(this)
        subscription_list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ArticleListFragment())
                .commit()
    }
}
