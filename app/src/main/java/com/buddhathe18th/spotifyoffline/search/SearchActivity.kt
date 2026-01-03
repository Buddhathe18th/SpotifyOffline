package com.buddhathe18th.spotifyoffline.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.buddhathe18th.spotifyoffline.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SearchActivity : FragmentActivity() {

    private lateinit var editSearch: EditText
    private lateinit var buttonClear: ImageButton
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: SearchPagerAdapter

    private val tabTitles = listOf("All", "Songs", "Albums", "Artists", "Playlists")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        editSearch = findViewById(R.id.editSearchQuery)
        buttonClear = findViewById(R.id.buttonClearSearch)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        // Back button
        findViewById<ImageButton>(R.id.buttonBack).setOnClickListener { finish() }

        // Setup ViewPager with tabs
        setupViewPager()

        // Setup search
        setupSearch()

        // Auto-focus search box
        editSearch.requestFocus()
    }

    private fun setupViewPager() {
        pagerAdapter = SearchPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position -> tab.text = tabTitles[position] }
                .attach()

        viewPager.registerOnPageChangeCallback(
                object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)

                        // Re-run search for newly visible tab
                        val query = editSearch.text.toString()
                        if (query.isNotEmpty()) {
                            pagerAdapter.getFragmentAt(position).search(query)
                        }
                    }
                }
        )
    }

    private fun setupSearch() {
        editSearch.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {}

                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {
                        val query = s.toString()

                        // Show/hide clear button
                        buttonClear.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE

                        Log.d("SearchActivity", "Search query changed: $query")
                        // Search in current tab
                        performSearch(query)
                    }

                    override fun afterTextChanged(s: Editable?) {}
                }
        )

        buttonClear.setOnClickListener { editSearch.text.clear() }
    }

    private fun performSearch(query: String) {
        // Trigger search in current fragment
        val currentPosition = viewPager.currentItem
        pagerAdapter.getFragmentAt(currentPosition).search(query)
    }
}
