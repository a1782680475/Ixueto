package com.xktech.ixueto.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentInterfaceAndInteractiveBinding

class InterfaceAndInteractiveFragment : Fragment() {
    private var binding: FragmentInterfaceAndInteractiveBinding? = null
    private lateinit var navController: NavController
    private var isFirstLoad = true
    private var rootView: View? = null
    private lateinit var toolBar: Toolbar
    private lateinit var videoPlayView: ViewGroup
    private lateinit var darkModelView: ViewGroup
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
        darkModelView.setOnClickListener {
            navController.navigate(
                R.id.action_interfaceAndInteractiveFragment_to_darkModelFragment,
                null,
                null,
                null
            )
        }
        videoPlayView.setOnClickListener {
            navController.navigate(
                R.id.action_interfaceAndInteractiveFragment_to_videoPlayFragment,
                null,
                null,
                null
            )
        }
    }

    private fun initView() {
        binding = FragmentInterfaceAndInteractiveBinding.inflate(layoutInflater)
        rootView = binding!!.root
        toolBar = binding!!.toolBar
        videoPlayView = binding!!.videoPlay
        darkModelView = binding!!.darkSwitch
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            InterfaceAndInteractiveFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}