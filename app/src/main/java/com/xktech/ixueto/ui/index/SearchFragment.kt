package com.xktech.ixueto.ui.index

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.gyf.immersionbar.ktx.immersionBar
import com.xktech.ixueto.R
import com.xktech.ixueto.adapter.SearchProfessionAdapter
import com.xktech.ixueto.components.LCEERecyclerView
import com.xktech.ixueto.databinding.FragmentSearchBinding
import com.xktech.ixueto.viewModel.ProfessionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private var binding: FragmentSearchBinding? = null
    private lateinit var toolBar: Toolbar
    private lateinit var clearButton: MenuItem
    private lateinit var searchSrcText: EditText
    private lateinit var searchResultList: LCEERecyclerView
    private var regionId: Int = -1
    private var rootView: View? = null
    private var isFirstLoad = true
    private var isDark = false
    private val args: SearchFragmentArgs by navArgs()
    private val professionViewModel: ProfessionViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (isFirstLoad) {
            isFirstLoad = false
            initView()
        }
        isDark =
            requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        if (isDark) {
            immersionBar {
                transparentStatusBar()
                statusBarDarkFont(false)
                statusBarColor(R.color.md_theme_dark_surface_navigation)
            }
        }else{
            immersionBar {
                transparentStatusBar()
                statusBarDarkFont(true)
                statusBarColor(R.color.md_theme_light_surface_navigation)
            }
        }
        return rootView
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SearchFragment()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        searchSrcText.requestFocus()
        context?.let { forceOpenSoftKeyboard(it) }
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar.setupWithNavController(navController, appBarConfiguration)
        searchSrcText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val searchText = searchSrcText.text
                clearButton.isVisible = !searchText.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        searchSrcText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_SEARCH) {
                hiddenSoftKeyboard()
                val searchText = searchSrcText.text
                if (!searchText.isNullOrEmpty()) {
                    search(regionId, searchText.toString())
                }
            }
            true
        }
        clearButton.setOnMenuItemClickListener {
            searchSrcText.text.clear()
            searchResultList.recyclerView.adapter =
                SearchProfessionAdapter(listOf()) { _, _ ->
                }
            true
        }
    }

    private fun hiddenSoftKeyboard(){
        view?.let {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        }
    }

    private fun initView() {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        rootView = binding!!.root
        toolBar = binding!!.toolBar!!
        clearButton = toolBar.menu[0]
        searchSrcText = binding!!.searchSrcText!!
        searchResultList = binding!!.searchResult!!
        regionId = args.regionId
    }

    private fun search(regionId: Int, searchText: String) {
        searchResultList.showLoadingView()
        professionViewModel.searchProfession(regionId, searchText).observe(viewLifecycleOwner) {
            searchResultList.hideAllViews()
            if (it.isNotEmpty()) {
                val searchProfessionAdapter = SearchProfessionAdapter(it) { profession, holder ->
                    val extras = FragmentNavigatorExtras(holder.professionItem to "professionInfo")
                    var bundle = bundleOf("professionId" to profession.Id)
                    bundle.putString("professionName", profession.Name)
                    bundle.putString("professionImage", profession.CoverImageUrl)
                    bundle.putString("regionName", profession.RegionName)
                    findNavController()?.navigate(
                        R.id.action_searchFragment_to_subjectFragment,
                        bundle,
                        null,
                        extras
                    )
                }
                searchResultList.recyclerView.visibility = View.VISIBLE
                searchResultList.recyclerView.adapter = searchProfessionAdapter
            } else {
                searchResultList.showEmptyView()
                searchResultList.recyclerView.visibility = View.GONE
            }
        }
    }

    private fun forceOpenSoftKeyboard(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchSrcText, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}