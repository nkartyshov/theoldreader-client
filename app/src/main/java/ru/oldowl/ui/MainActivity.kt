package ru.oldowl.ui

import android.content.Intent
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
import ru.oldowl.core.UiEvent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel.loadEmail()
        mainViewModel.updateDrawer()
        mainViewModel.startScheduleUpdate()

        val adapter = SubscriptionNavItemAdapter()
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).also {
            it.viewModel = mainViewModel
            it.lifecycleOwner = this@MainActivity

            it.navigationView.recyclerConfig = RecyclerConfig(adapter, LinearLayoutManager(this@MainActivity))
            it.navigationView.viewModel = mainViewModel
            it.navigationView.lifecycleOwner = this@MainActivity

            it.navigationView.navigationHeader.viewModel = mainViewModel
            it.navigationView.navigationHeader.lifecycleOwner = this@MainActivity

            it.navigationView.setOnNavClick { v ->
                when (v.id) {
                    R.id.all -> openFragment(ArticleListFragment.openAllArticles())
                    R.id.favorite -> openFragment(ArticleListFragment.openFavorites(), addToBackStack = true)
                    R.id.add_subscription -> startActivity<AddSubscriptionActivity>()
                    R.id.setting -> openFragment(SettingsFragment(), addToBackStack = true)
                    R.id.logout -> mainViewModel.logout()
                }
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
                mainViewModel.updateDrawer()
            }
        })

        adapter.onItemClick = {
            openFragment(ArticleListFragment.openSubscription(it), addToBackStack = true)
        }

        observe(mainViewModel.subscriptions) {
            adapter.submitList(it)
        }

        observe(mainViewModel.event) {
            when (it) {
                UiEvent.CloseScreen -> startActivity<LoginActivity>(
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            }
        }

        if (savedInstanceState == null) {
            openFragment(ArticleListFragment.openAllArticles())
        }


    }

    private fun openFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        replaceFragment(R.id.fragment_container, fragment, addToBackStack)
        drawer_view.closeDrawers()
    }
}
