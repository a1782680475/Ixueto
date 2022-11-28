package com.xktech.ixueto.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.xktech.ixueto.BuildConfig
import com.xktech.ixueto.NavGraphDirections
import com.xktech.ixueto.databinding.FragmentAboutBinding


class AboutFragment : Fragment() {
    private var binding: FragmentAboutBinding? = null
    private lateinit var toolBar: Toolbar
    private lateinit var navController: NavController
    private lateinit var versionNameText: TextView
    private var rootView: View? = null
    private lateinit var userAgreementButton: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAboutBinding.inflate(layoutInflater)
        rootView = binding!!.root
        toolBar = binding!!.toolBar
        userAgreementButton = binding?.userAgreement!!
        versionNameText = binding!!.versionName
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val packageManager = requireActivity().packageManager
        val packageInfo = packageManager!!.getPackageInfo(requireActivity().packageName, 0)
        val versionName = packageInfo.versionName
        versionNameText.text = versionName
        navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar.setupWithNavController(navController, appBarConfiguration)
        userAgreementButton.setOnClickListener {
            navController.navigate(
                NavGraphDirections.actionGlobalWebFragment(
                    "用户协议",
                    "${BuildConfig.REMOTE_DOMAIN}/Content/Protocol.html",
                    null
                )
            )
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            AboutFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}