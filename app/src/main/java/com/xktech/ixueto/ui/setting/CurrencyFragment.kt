package com.xktech.ixueto.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentCurrencyBinding
import com.xktech.ixueto.viewModel.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CurrencyFragment : Fragment() {
    private var binding: FragmentCurrencyBinding? = null
    private lateinit var navController: NavController
    private var isFirstLoad = true
    private var rootView: View? = null
    private lateinit var toolBar: Toolbar
    private lateinit var startupPageView: ViewGroup
    private lateinit var startupPageNameView: TextView
    private lateinit var facePageBrightSwitch: MaterialSwitch
    private var startupPageIndex = 0
    private val settingViewModel: SettingViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (isFirstLoad) {
            isFirstLoad = false
            initView()
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar.setupWithNavController(navController, appBarConfiguration)
        initSettingOperation()
        startupPageView.setOnClickListener {
            val singleItems = arrayOf("课程", "学习")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("选择启动页")
                .setNeutralButton(resources.getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                    settingViewModel.setStartupPage(startupPageIndex)
                    updateStartupPageNameView()
                    dialog.dismiss()
                    Snackbar.make(binding!!.root, "设置成功，将在下次启动APP时生效", Snackbar.LENGTH_SHORT)
                        .show()
                }.setSingleChoiceItems(singleItems, startupPageIndex) { _, which ->
                    startupPageIndex = which
                }
                .show()
        }
        facePageBrightSwitch.setOnCheckedChangeListener { _view, isChecked ->
            if (_view.isPressed) {
                settingViewModel.setFacePageBright(isChecked)
            }
        }
    }

    private fun initView() {
        binding = FragmentCurrencyBinding.inflate(layoutInflater)
        rootView = binding!!.root
        toolBar = binding!!.toolBar
        startupPageView = binding!!.startupPage
        startupPageNameView = binding!!.startupPageName
        facePageBrightSwitch = binding!!.facePageBrightSwitch
    }

    private fun initSettingOperation() {
        settingViewModel.getSetting().observe(viewLifecycleOwner) {
            startupPageIndex = if (!it.hasStartupPage()) {
                0
            } else {
                it.startupPage
            }
            updateStartupPageNameView()
            var facePageBright: Boolean = if(!it.hasFacePageBright()){
                true
            }else{
                it.facePageBright
            }
            facePageBrightSwitch.isChecked = facePageBright
        }
    }

    private fun updateStartupPageNameView(){
        when (startupPageIndex) {
            0 -> {
                startupPageNameView.text = "课程"
            }
            1 -> {
                startupPageNameView.text = "学习"
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CurrencyFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}