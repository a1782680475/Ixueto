package com.xktech.ixueto.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.materialswitch.MaterialSwitch
import com.xktech.ixueto.databinding.FragmentVideoPlayBinding
import com.xktech.ixueto.viewModel.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoPlayFragment : Fragment() {
    private var binding: FragmentVideoPlayBinding? = null
    private lateinit var navController: NavController
    private lateinit var alertAtNotWifiSwitch: MaterialSwitch
    private lateinit var gestureSwitch: MaterialSwitch
    private var rootView: View? = null
    private lateinit var toolBar: Toolbar
    private lateinit var preSavedStateHandle: SavedStateHandle
    private val settingViewModel: SettingViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initView()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        preSavedStateHandle = navController.previousBackStackEntry!!.savedStateHandle
        preSavedStateHandle["setting"] = "player"
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar.setupWithNavController(navController, appBarConfiguration)
        initSettingOperation()
        alertAtNotWifiSwitch.setOnCheckedChangeListener { _view, isChecked ->
            if (_view.isPressed) {
                settingViewModel.setAlertAtNotWifi(isChecked)
            }
        }
        gestureSwitch.setOnCheckedChangeListener { _view, isChecked ->
            if (_view.isPressed) {
                settingViewModel.setGesture(isChecked)
            }
        }
    }

    private fun initView() {
        binding = FragmentVideoPlayBinding.inflate(layoutInflater)
        rootView = binding!!.root
        toolBar = binding!!.toolBar
        alertAtNotWifiSwitch = binding!!.alertAtNotWifiSwitch
        gestureSwitch = binding!!.gestureSwitch
    }

    private fun initSettingOperation(){
        settingViewModel.getSetting().observe(viewLifecycleOwner){
            var videoPlaySetting = it.videoPlay
            var alertAtNotWifi: Boolean = if(!videoPlaySetting.hasAlertAtNotWifi()){
                true
            }else{
                videoPlaySetting.alertAtNotWifi
            }
            alertAtNotWifiSwitch.isChecked = alertAtNotWifi
            var gesture: Boolean = if(!videoPlaySetting.hasGesture()){
                true
            }else{
                videoPlaySetting.gesture
            }
            gestureSwitch.isChecked = gesture
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            VideoPlayFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}