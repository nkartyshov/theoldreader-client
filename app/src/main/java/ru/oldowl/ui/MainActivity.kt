package ru.oldowl.ui

import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.navigation_layout.*
import kotlinx.android.synthetic.main.toolbar.*
import org.koin.android.viewmodel.ext.android.viewModel
import ru.oldowl.Jobs
import ru.oldowl.R
import ru.oldowl.core.binding.RecyclerConfig
import ru.oldowl.databinding.ActivityMainBinding
import ru.oldowl.core.extension.replaceFragment
import ru.oldowl.core.ui.BaseActivity
import ru.oldowl.ui.adapter.SubscriptionAndUnreadCountAdapter
import ru.oldowl.ui.fragment.AddSubscriptionFragment
import ru.oldowl.ui.fragment.ArticleListFragment
import ru.oldowl.ui.fragment.SettingsFragment
import ru.oldowl.viewmodel.MainViewModel

class MainActivity : BaseActivity() {
    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataBinding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawer_view,
                toolbar,
                R.string.open_drawer,
                R.string.close_drawer)

        actionBarDrawerToggle.syncState()

        drawer_view.setStatusBarBackground(R.color.colorPrimaryDark)
        drawer_view.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)

                mainViewModel.updateLastSyncDate()
                mainViewModel.updateSubscriptions()
            }
        })

        val adapter = SubscriptionAndUnreadCountAdapter()
        adapter.onItemClick = {
            openFragment(ArticleListFragment.openSubscription(it), addToBackStack = true)
        }

        dataBinding.viewModel = mainViewModel
        dataBinding.navigationView.viewModel = mainViewModel
        dataBinding.navigationView.recyclerConfig = RecyclerConfig(adapter, LinearLayoutManager(this))

        dataBinding.navigationView.navigationHeader.viewModel = mainViewModel
        dataBinding.lifecycleOwner = this

        mainViewModel.subscriptions.observe(this, Observer {
            adapter.submitList(it)
        })

        all_articles.setOnClickListener {
            openFragment(ArticleListFragment.openAllArticles())
        }

        favorite_articles.setOnClickListener {
            openFragment(ArticleListFragment.openFavorites(), addToBackStack = true)
        }

        add_subscription.setOnClickListener {
            openFragment(AddSubscriptionFragment(), addToBackStack = true)
        }

        settings.setOnClickListener {
            openFragment(SettingsFragment(), addToBackStack = true)
        }

        openFragment(ArticleListFragment.openAllArticles())

        Jobs.scheduleUpdate(this)
    }

    private fun openFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        replaceFragment(R.id.fragment_container, fragment, addToBackStack)

        drawer_view.closeDrawers()
    }
}
