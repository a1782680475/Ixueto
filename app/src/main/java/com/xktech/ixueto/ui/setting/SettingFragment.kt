package com.xktech.ixueto.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentSettingBinding
import com.xktech.ixueto.viewModel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : Fragment() {
    private var binding: FragmentSettingBinding? = null
    private lateinit var logoutButton: LinearLayout
    private lateinit var toolBar: Toolbar
    private lateinit var navController: NavController
    private lateinit var clearCache: LinearLayout
    private lateinit var cacheSizeView: TextView
    private lateinit var passwordModifyView: ViewGroup
    private lateinit var interfaceAndInteractiveView: ViewGroup
    private lateinit var currencyView: ViewGroup

    private val userViewModel: UserViewModel by activityViewModels()
    private var rootView: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun initView() {
        binding = FragmentSettingBinding.inflate(layoutInflater)
        rootView = binding!!.root
        logoutButton = binding!!.logout!!
        toolBar = binding!!.toolBar!!
        clearCache = binding!!.clearCache
        cacheSizeView = binding!!.cacheSize
        passwordModifyView = binding!!.passwordModify
        interfaceAndInteractiveView = binding!!.interfaceAndInteractive
        currencyView = binding!!.currency
        navController = findNavController()
        logoutButton.setOnClickListener {
            context?.let { it1 ->
                MaterialAlertDialogBuilder(it1)
                    .setTitle("退出确认")
                    .setMessage("是否退出登录？")
                    .setNegativeButton(resources.getString(R.string.cancel), null)
                    .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                        userViewModel.logout()
                        navController.popBackStack()
                        navController.navigate(R.id.action_global_loginFragment)
                    }
                    .show()
            }
        }
        passwordModifyView.setOnClickListener {
            navController.navigate(
                R.id.action_settingFragment_to_passwordModifyFragment,
                null,
                null,
                null
            )
        }
        interfaceAndInteractiveView.setOnClickListener {
            navController.navigate(
                R.id.action_settingFragment_to_interfaceAndInteractiveFragment,
                null,
                null,
                null
            )
        }
        currencyView.setOnClickListener {
            navController.navigate(
                R.id.action_settingFragment_to_currencyFragment,
                null,
                null,
                null
            )
        }
        cacheSizeView.text = context?.let { CacheDataManager.getTotalCacheSize(it) }
        clearCache.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("清除确认")
                .setMessage("此操作将清除应用所有缓存、下载的安装包以及本地学习数据，确认吗？")
                .setNegativeButton(resources.getString(R.string.cancel), null)
                .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                    CacheDataManager.clearAllCache(requireContext())
                    cacheSizeView.text = context?.let { CacheDataManager.getTotalCacheSize(it) }
                }
                .show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            initView()
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar.setupWithNavController(navController, appBarConfiguration)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}