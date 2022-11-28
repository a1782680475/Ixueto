package com.xktech.ixueto.ui.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.xktech.ixueto.R
import com.xktech.ixueto.adapter.SelectorAdapter
import com.xktech.ixueto.databinding.FragmentAuthenticationStepOneBinding
import com.xktech.ixueto.model.AuthenticationInfo
import com.xktech.ixueto.model.AuthenticationItemConfig
import com.xktech.ixueto.model.AuthenticationStateEnum
import com.xktech.ixueto.viewModel.AuthenticationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthenticationStepOneFragment : Fragment() {
    private var binding: FragmentAuthenticationStepOneBinding? = null
    private var rootView: View? = null
    private lateinit var toolBar: Toolbar
    private lateinit var navController: NavController
    private lateinit var loading: View
    private lateinit var form: View
    private lateinit var identity: TextInputLayout
    private lateinit var nextStep: Button
    private val authenticationViewModel: AuthenticationViewModel by viewModels()
    private lateinit var authenticationState: AuthenticationStateEnum
    private lateinit var authenticationConfigMap: Map<String, AuthenticationItemConfig>
    private lateinit var authenticationInfo: AuthenticationInfo
    private var isFirstLoad = true
    private val gson: Gson = Gson().newBuilder()
        .setLenient()
        .serializeNulls()
        .create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        super.onViewCreated(view, savedInstanceState)
        if(isFirstLoad) {
            isFirstLoad = false
            loadData()
        }
    }

    private fun initView() {
        binding = FragmentAuthenticationStepOneBinding.inflate(layoutInflater)
        rootView = binding!!.root
        nextStep = binding!!.nextStep
        navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar = binding!!.toolBar!!
        toolBar.setupWithNavController(navController, appBarConfiguration)
        loading = binding!!.loading
        form = binding!!.form
        identity = binding!!.identity
        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<Boolean>("isSubmitted").observe(currentBackStackEntry){
            navController.previousBackStackEntry!!.savedStateHandle["isSubmitted"] = true
            navController.popBackStack()
        }
    }


    private fun loadData() {
        identity.isHintAnimationEnabled = false
        getAuthenticationInfo { authenticationState, authenticationInfo ->
            this.authenticationState = authenticationState
            this.authenticationInfo = authenticationInfo
            renderAuthority { authenticationConfig ->
                authenticationConfigMap = authenticationConfig
                loadIdentityData {
                    hiddenLoading()
                }
            }
        }
    }

    private fun showLoading(){
        form.visibility = View.GONE
        loading.visibility = View.VISIBLE
        identity.isHintAnimationEnabled = false
    }

    private fun hiddenLoading() {
        form.visibility = View.VISIBLE
        loading.visibility = View.GONE
        identity.isHintAnimationEnabled = true
    }

    private fun renderAuthority(
        callback: (Map<String, AuthenticationItemConfig>) -> Unit
    ) {
        getAuthenticationConfig(authenticationInfo.IdentityId ?: -1) { authenticationConfig ->
            when (authenticationState) {
                //未认证状态下，培训单位所在地、培训单位和身份类型规则设置无效，以上依据内容判断，若有值，不允许编辑，反之则允许编辑。
                AuthenticationStateEnum.UN_AUTHENTICATION -> {
                    authenticationInfo.IdentityId?.let {
                        identity.editText?.setTextIsSelectable(false)
                        identity.isEnabled = false
                    }
                }
                else -> {
                    val formItemConfig = authenticationConfig["identity"]
                    formItemConfig?.let {
                        identity.isEnabled = formItemConfig.Editable
                    }
                }
            }
            callback(authenticationConfig)
        }
    }

    private fun getAuthenticationConfig(
        identityId: Int?,
        callback: (Map<String, AuthenticationItemConfig>) -> Unit
    ) {
        authenticationViewModel.getAuthenticationConfig(identityId ?: -1)
            .observe(viewLifecycleOwner) {
                callback(it)
            }
    }

    private fun getAuthenticationInfo(callback: (AuthenticationStateEnum, AuthenticationInfo) -> Unit) {
        authenticationViewModel.getAuthenticationBasis().observe(viewLifecycleOwner) {
            callback(it.authenticationState, it.authenticationInfo)
        }
    }

    private fun loadIdentityData(callback: () -> Unit) {
        authenticationViewModel.identity.observe(viewLifecycleOwner) { identities ->
            val selectorAdapter = SelectorAdapter(context!!, identities)
            if ((authenticationInfo.IdentityId != null) && (authenticationInfo.IdentityId!! > 0)) {
                selectorAdapter.value = authenticationInfo.IdentityId.toString()
                for (item in identities) {
                    if (item.Value == authenticationInfo.IdentityId.toString()) {
                        identity.editText?.setText(item.Title)
                        nextStep.isEnabled = true
                    }
                }
            }
            val materialAutoCompleteTextView = (identity.editText as? MaterialAutoCompleteTextView)
            materialAutoCompleteTextView?.setAdapter(selectorAdapter)
            materialAutoCompleteTextView?.onItemClickListener =
                AdapterView.OnItemClickListener { parent, _, position, _ ->
                    authenticationInfo.IdentityId = identities[position].Value.toInt()
                    (parent.adapter as SelectorAdapter).value =
                        authenticationInfo.IdentityId.toString()
                    showLoading()
                    nextStep.isEnabled = true
                    getAuthenticationConfig(authenticationInfo.IdentityId ?: -1) { authenticationConfig ->
                        authenticationConfigMap = authenticationConfig
                        hiddenLoading()
                    }
                }
            nextStep.setOnClickListener {
                if (authenticationInfo.IdentityId != -1) {
                    var bundle = bundleOf(
                        "authenticationState" to authenticationState,
                        "authenticationConfig" to authenticationConfigMap,
                        "authenticationInfo" to authenticationInfo,
                    )
                    parentFragment?.findNavController()?.navigate(
                        R.id.action_authenticationStepOneFragment_to_authenticationStepTwoFragment,
                        bundle,
                        null,
                        null
                    )
                }
            }
            callback()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            AuthenticationStepOneFragment()
    }
}