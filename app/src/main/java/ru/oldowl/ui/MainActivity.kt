package ru.oldowl.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import org.koin.android.viewmodel.ext.android.viewModel
import ru.oldowl.R
import ru.oldowl.core.binding.RecyclerConfig
import ru.oldowl.core.extension.observe
import ru.oldowl.core.extension.replaceFragment
import ru.oldowl.core.extension.startActivity
import ru.oldowl.core.ui.BaseActivity
import ru.oldowl.databinding.ActivityMainBinding
import ru.oldowl.ui.adapter.SubscriptionNavItemAdapter
import ru.oldowl.viewmodel.MainViewModel

class MainActivity : BaseActivity() {

    private val mainViewModel: MainViewModel by viewModel()

    private val adapter = SubscriptionNavItemAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            viewModel = mainViewModel
            lifecycleOwner = this@MainActivity

            navigationView.recyclerConfig = RecyclerConfig(adapter, LinearLayoutManager(this@MainActivity))
            navigationView.viewModel = mainViewModel
            navigationView.lifecycleOwner = this@MainActivity

            navigationView.navigationHeader.viewModel = mainViewModel
            navigationView.navigationHeader.lifecycleOwner = this@MainActivity

            navigationView.setOnAllPage {
                openFragment(ArticleListFragment.openAllArticles())
            }

            navigationView.setOnFavoritePage {
                openFragment(ArticleListFragment.openFavorites(), addToBackStack = true)
            }

            navigationView.setOnAddSubscription {
                startActivity<AddSubscriptionActivity>()
            }

            navigationView.setOnSettings {
                openFragment(SettingsFragment(), addToBackStack = true)
            }
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val actionBarDrawerToggle = ActionBarDrawerToggle(this@MainActivity, drawer_view,
                toolbar,
                R.string.open_drawer,
                R.string.close_drawer)

        actionBarDrawerToggle.syncState()

        drawer_view.setStatusBarBackground(R.color.colorPrimaryDark)
        drawer_view.addDrawerListener(object : androidx.drawerlayout.widget.DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                updateDrawer()
            }
        })

        adapter.onItemClick = {
            openFragment(ArticleListFragment.openSubscription(it), addToBackStack = true)
        }

        observe(mainViewModel.subscriptions) {
            adapter.submitList(it)
        }

        if(savedInstanceState == null) {
            openFragment(ArticleListFragment.openAllArticles())
        }

        updateDrawer()
        mainViewModel.startScheduleUpdate()
    }

    private fun updateDrawer() {
        mainViewModel.updateLastSyncDate()
        mainViewModel.updateSubscriptions()
    }

    private fun openFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        replaceFragment(R.id.fragment_container, fragment, addToBackStack)
        drawer_view.closeDrawers()
    }
}
