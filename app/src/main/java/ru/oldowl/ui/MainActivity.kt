package ru.oldowl.ui

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
import org.koin.standalone.inject
import ru.oldowl.R
import ru.oldowl.databinding.ActivityMainBinding
import ru.oldowl.extension.replaceFragment
import ru.oldowl.ui.adapter.SubscriptionAndUnreadCountAdapter
import ru.oldowl.ui.fragment.AddSubscriptionFragment
import ru.oldowl.ui.fragment.ArticleListFragment
import ru.oldowl.ui.fragment.SettingsFragment
import ru.oldowl.viewmodel.MainViewModel

class MainActivity : BaseActivity() {
    private val mainViewModel: MainViewModel by inject()

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

        val adapter = SubscriptionAndUnreadCountAdapter(this)
        adapter.setOnItemClickListener {
            openFragment(ArticleListFragment.openSubscription(it))
        }

        subscription_list.adapter = adapter
        subscription_list.layoutManager = LinearLayoutManager(this)

        all_articles.setOnClickListener {
            openFragment(ArticleListFragment.openAllArticles())
        }

        favorite_articles.setOnClickListener {
            openFragment(ArticleListFragment.openFavorites())
        }

        add_subscription.setOnClickListener {
            openFragment(AddSubscriptionFragment())
        }

        settings.setOnClickListener {
            openFragment(SettingsFragment())
        }

        dataBinding.viewModel = mainViewModel
        dataBinding.navigationView.viewModel = mainViewModel
        dataBinding.navigationView.navigationHeader.viewModel = mainViewModel
        dataBinding.setLifecycleOwner(this)

        replaceFragment(R.id.fragment_container, ArticleListFragment.openAllArticles(), addToBackStack = false)
    }

    private fun openFragment(fragment: Fragment) {
        replaceFragment(R.id.fragment_container, fragment)

        drawer_view.closeDrawers()
    }
}
