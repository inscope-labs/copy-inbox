package com.inscopelabs.abx.clipinbox.ui.category

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.inscopelabs.abx.clipinbox.R

class CategoryTabBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val tabScrollView: HorizontalScrollView
    private val tabContainer: LinearLayout
    private val btnMore: ImageButton

    private var tabs: List<String> = emptyList()
    private var selectedIndex: Int = 0
    private var onTabSelectedListener: ((Int) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.view_category_tab_bar, this, true)

        tabScrollView = findViewById(R.id.tabScrollView)
        tabContainer = findViewById(R.id.tabContainer)
        btnMore = findViewById(R.id.btnMore)

        btnMore.setOnClickListener { showOverflowMenu() }

        tabScrollView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                checkOverflow()
            }
        })
    }

    fun setTabs(tabTitles: List<String>, onSelected: (Int) -> Unit) {
        tabs = tabTitles
        onTabSelectedListener = onSelected
        selectedIndex = 0
        rebuildTabs()
    }

    fun selectTab(index: Int) {
        if (index in tabs.indices && index != selectedIndex) {
            selectedIndex = index
            updateTabStyles()
            scrollToTab(index)
            onTabSelectedListener?.invoke(index)
        }
    }

    fun getSelectedIndex(): Int = selectedIndex

    private fun rebuildTabs() {
        tabContainer.removeAllViews()
        val inflater = LayoutInflater.from(context)

        tabs.forEachIndexed { index, title ->
            val tabView = TextView(context).apply {
                text = title
                setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
                val paddingH = (16 * resources.displayMetrics.density).toInt()
                val paddingV = (8 * resources.displayMetrics.density).toInt()
                setPadding(paddingH, paddingV, paddingH, paddingV)
                val margin = (4 * resources.displayMetrics.density).toInt()
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(margin, 0, margin, 0)
                }
                setOnClickListener {
                    selectTab(index)
                }
            }
            tabContainer.addView(tabView)
        }

        updateTabStyles()
        post { checkOverflow() }
    }

    private fun updateTabStyles() {
        for (i in 0 until tabContainer.childCount) {
            val child = tabContainer.getChildAt(i) as? TextView ?: continue
            if (i == selectedIndex) {
                child.setBackgroundResource(R.drawable.bg_category_tab_selected)
                child.setTextColor(ContextCompat.getColor(context, R.color.periwinkle_text))
            } else {
                child.setBackgroundResource(R.drawable.bg_category_tab_unselected)
                child.setTextColor(ContextCompat.getColor(context, R.color.periwinkle))
            }
        }
    }

    private fun checkOverflow() {
        val scrollWidth = tabScrollView.width
        val contentWidth = tabContainer.width
        if (contentWidth > scrollWidth && scrollWidth > 0) {
            btnMore.visibility = View.VISIBLE
        } else {
            btnMore.visibility = View.GONE
        }
    }

    private fun scrollToTab(index: Int) {
        val tabView = tabContainer.getChildAt(index) ?: return
        val scrollX = tabView.left - (tabScrollView.width - tabView.width) / 2
        tabScrollView.smoothScrollTo(scrollX.coerceAtLeast(0), 0)
    }

    private fun showOverflowMenu() {
        val popup = PopupMenu(context, btnMore)
        tabs.forEachIndexed { index, title ->
            popup.menu.add(0, index, index, title)
        }
        popup.setOnMenuItemClickListener { item ->
            selectTab(item.itemId)
            true
        }
        popup.show()
    }
}
