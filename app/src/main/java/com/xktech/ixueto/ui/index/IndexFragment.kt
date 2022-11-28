package com.xktech.ixueto.ui.index

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialElevationScale
import com.lxj.xpopup.XPopup
import com.xktech.ixueto.R
import com.xktech.ixueto.adapter.IndexFragmentStateAdapter
import com.xktech.ixueto.databinding.FragmentIndexBinding
import com.xktech.ixueto.model.Region
import com.xktech.ixueto.viewModel.RegionViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class IndexFragment : Fragment() {
    private var binding: FragmentIndexBinding? = null
    private lateinit var pager: ViewPager2
    private lateinit var tab: TabLayout
    private lateinit var regionAllButton: ImageButton
    private lateinit var navController: NavController
    private lateinit var toolBar: Toolbar
    private var regionPopup: RegionPopup? = null
    private var rootView: View? = null
    private val regionViewModel: RegionViewModel by viewModels()
    private var isFirstLoad = true
    private var currentRegionIndex: Int = 0
    private var currentRegionId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialElevationScale(false)
        reenterTransition = MaterialElevationScale(true)
//        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z,  true)
//        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        if (isFirstLoad) {
            initView()
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar
            .setupWithNavController(navController, appBarConfiguration)
        if(isFirstLoad) {
            loadData()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            IndexFragment()
    }

    private fun getFragmentIds(regions: MutableList<Region>): MutableList<Int> {
        val fragmentIds = mutableListOf<Int>()
        for (region in regions) {
            fragmentIds.add(region.Id)
        }
        return fragmentIds
    }

    private fun initView() {
        binding = FragmentIndexBinding.inflate(layoutInflater)
        rootView = binding?.root
        pager = binding?.pager!!
        pager.isSaveEnabled = false
        tab = binding?.tab!!
        regionAllButton = binding?.regionAll!!
        toolBar = binding?.toolBar!!
        navController = findNavController()
        toolBar.setOnMenuItemClickListener {
            var action =
                IndexFragmentDirections.actionIndexFragmentToSearchFragment(currentRegionId)
            navController.navigate(action)
            true
        }
        //pager.setPageTransformer(ZoomOutPageTransformer())
    }

    private fun updateTab(position: Int) {
        regionPopup?.dismiss()
        pager.setCurrentItem(position, true)
    }

    private fun loadData() {
        regionViewModel.regions.observe(viewLifecycleOwner, Observer {
            isFirstLoad = false
            val regions = it
            if (regions != null) {
                currentRegionId = regions[0].Id
                regionAllButton.setOnClickListener {
                    val regionPopupInstance = context?.let { regionPopup ->
                        RegionPopup(
                            regionPopup,
                            regions,
                            currentRegionIndex
                        )
                    }
                    regionPopupInstance!!.setOnSelectedListener { position, _ -> updateTab(position) }
                    if (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                        regionPopup = XPopup.Builder(context)
                            .isDarkTheme(true)
                            .isLightNavigationBar(false)
                            .navigationBarColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.md_theme_dark_surface_navigation
                                )
                            )
                            .atView(tab)
                            .asCustom(regionPopupInstance)
                            .show() as RegionPopup?
                    } else {
                        regionPopup = XPopup.Builder(context)
                            .isDarkTheme(false)
                            .isLightStatusBar(true)
                            .isLightNavigationBar(true)
                            .navigationBarColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.md_theme_light_surface_navigation
                                )
                            )
                            .atView(tab)
                            .asCustom(regionPopupInstance)
                            .show() as RegionPopup?
                    }
                }
                pager.adapter =
                    IndexFragmentStateAdapter(
                        this.childFragmentManager,
                        lifecycle,
                        getFragmentIds(regions)
                    )
                TabLayoutMediator(
                    tab,
                    pager
                ) { tab, position ->
                    val region = regions[position]
                    tab.tag = region.Id
                    tab.text = region.Name
                }.attach()
                tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        currentRegionIndex = tab?.position!!
                        currentRegionId = tab.tag as Int
                    }

                    override fun onTabReselected(p0: TabLayout.Tab?) {

                    }

                    override fun onTabUnselected(p0: TabLayout.Tab?) {

                    }
                })
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}