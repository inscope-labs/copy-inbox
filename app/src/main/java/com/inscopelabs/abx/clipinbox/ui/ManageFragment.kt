package com.inscopelabs.abx.clipinbox.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.ui.category.CategoryTabBar

class ManageFragment : Fragment() {

    private companion object {
        private const val TAG = "ManageFragment"
    }

    private lateinit var categoryTabBar: CategoryTabBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryTabBar = view.findViewById(R.id.categoryTabBar)

        categoryTabBar.setTabs(
            listOf(
                getString(R.string.manage_tab_categories),
                getString(R.string.manage_tab_connection)
            )
        ) { index ->
            showTab(index)
        }

        if (childFragmentManager.findFragmentById(R.id.manage_content_container) == null) {
            showTab(0)
        }
    }

    private fun showTab(index: Int) {
        val currentFragment = childFragmentManager.findFragmentById(R.id.manage_content_container)
        val targetFragment = when (index) {
            0 -> if (currentFragment !is CategoriesFragment) CategoriesFragment() else null
            1 -> if (currentFragment !is SessionFragment) SessionFragment() else null
            else -> null
        }

        if (targetFragment != null) {
            Logger.i(TAG, "Switching manage tab to index=$index")
            childFragmentManager.beginTransaction()
                .replace(R.id.manage_content_container, targetFragment)
                .commit()
        }
    }
}
