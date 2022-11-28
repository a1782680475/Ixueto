package com.xktech.ixueto.ui.index

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.xktech.ixueto.R
import com.xktech.ixueto.adapter.ProfessionAdapter
import com.xktech.ixueto.databinding.FragmentProfessionBinding
import com.xktech.ixueto.di.NetworkModule
import com.xktech.ixueto.viewModel.ProfessionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfessionFragment : Fragment() {
    private var binding: FragmentProfessionBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var professionAdapter: ProfessionAdapter
    private lateinit var swiperLayout: SwipeRefreshLayout
    private lateinit var navController: NavController
    private val professionViewModel: ProfessionViewModel by viewModels()
    private var regionId: Int? = null
    private var rootView: View? = null
    private var isLogin: Boolean = false
    private var isFirstLoad = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            regionId = it.getInt("regionId")
        }
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
        if(isFirstLoad) {
            isFirstLoad = false
            navController = findNavController()
            professionAdapter = ProfessionAdapter(context, regionId!!) { profession, viewHolder ->
                val extras = FragmentNavigatorExtras(viewHolder.card to "profession")
                var bundle = bundleOf("professionId" to profession.Id)
                bundle.putString("professionName", profession.Name)
                bundle.putString("professionImage", profession.CoverImageUrl)
                bundle.putString("regionName", profession.RegionName)
                checkLogin()
                if (isLogin) {
                    parentFragment?.findNavController()?.navigate(
                        R.id.action_indexFragment_to_subjectFragment,
                        bundle,
                        null,
                        extras
                    )
                } else {
                    navController.navigate(R.id.action_global_loginFragment)
                }
            }
            swiperLayout.setOnRefreshListener {
                professionAdapter.refresh()
            }
            professionAdapter.addLoadStateListener {
                swiperLayout.isRefreshing = it.refresh is LoadState.Loading
            }
            loadData()
        }
        super.onViewCreated(view, savedInstanceState)
    }


    companion object {
        @JvmStatic
        fun newInstance(regionId: Int) =
            ProfessionFragment().apply {
                arguments = Bundle().apply {
                    putInt("regionId", regionId)
                }
            }
    }

    private fun loadData() {
        val layoutManager = GridLayoutManager(context, 2)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = professionAdapter
        lifecycleScope.launch {
            professionViewModel.getProfessions(regionId!!).collectLatest {
                professionAdapter.submitData(it)
            }
        }
    }


    private fun initView() {
        binding = FragmentProfessionBinding.inflate(layoutInflater)
        recyclerView = binding!!.professionRectangles!!
        swiperLayout = binding!!.professionSwiper!!
        rootView = binding!!.root!!
    }

    private fun checkLogin() {
        isLogin = !NetworkModule.token.isNullOrEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}