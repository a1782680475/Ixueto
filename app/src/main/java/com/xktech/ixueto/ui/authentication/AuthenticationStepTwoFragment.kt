package com.xktech.ixueto.ui.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.LinearLayout
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
import com.xktech.ixueto.databinding.FragmentAuthenticationStepTwoBinding
import com.xktech.ixueto.model.AuthenticationInfo
import com.xktech.ixueto.model.AuthenticationItemConfig
import com.xktech.ixueto.model.AuthenticationStateEnum
import com.xktech.ixueto.model.Selector
import com.xktech.ixueto.utils.RenderStateManager
import com.xktech.ixueto.utils.ValidityUtils
import com.xktech.ixueto.viewModel.AuthenticationViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern

@AndroidEntryPoint
class AuthenticationStepTwoFragment : Fragment() {
    private var binding: FragmentAuthenticationStepTwoBinding? = null
    private var rootView: View? = null
    private lateinit var toolBar: Toolbar
    private lateinit var navController: NavController
    private lateinit var loading: View
    private lateinit var form: View
    private lateinit var name: TextInputLayout
    private lateinit var phone: TextInputLayout
    private lateinit var formComposeAddress: LinearLayout
    private lateinit var province: TextInputLayout
    private lateinit var city: TextInputLayout
    private lateinit var county: TextInputLayout
    private lateinit var street: TextInputLayout
    private lateinit var address: TextInputLayout
    private lateinit var formComposeUnit: LinearLayout
    private lateinit var unitProvince: TextInputLayout
    private lateinit var unitCity: TextInputLayout
    private lateinit var unit: TextInputLayout
    private lateinit var nextStep: Button
    private lateinit var authenticationState: AuthenticationStateEnum
    private lateinit var authenticationConfigMap: Map<String, AuthenticationItemConfig>
    private lateinit var authenticationInfoMap: MutableMap<String, String?>
    private var authenticationInfo: AuthenticationInfo? = null
    private val authenticationViewModel: AuthenticationViewModel by viewModels()
    private val gson: Gson = Gson().newBuilder()
        .setLenient()
        .serializeNulls()
        .create()
    private var aloneConfigItems = mutableListOf<View>()
    private var configKeyViewMap = mutableMapOf<String, Pair<View?, View?>>()
    private var viewAuthenticationInfo = mutableMapOf<View, String>()
    private val renderStateManager = RenderStateManager()
    private var isFirstLoad = true
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
        binding = FragmentAuthenticationStepTwoBinding.inflate(layoutInflater)
        rootView = binding!!.root
        navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolBar = binding!!.toolBar!!
        toolBar.setupWithNavController(navController, appBarConfiguration)
        loading = binding!!.loading
        form = binding!!.form
        name = binding!!.name
        phone = binding!!.phone
        formComposeAddress = binding!!.formComposeAddress
        province = binding!!.province
        city = binding!!.city
        county = binding!!.county
        street = binding!!.street
        address = binding!!.address
        formComposeUnit = binding!!.formComposeUnit
        unitProvince = binding!!.unitProvince
        unitCity = binding!!.unitCity
        unit = binding!!.unit
        nextStep = binding!!.nextStep
        authenticationState =
            arguments?.getSerializable("authenticationState") as AuthenticationStateEnum
        authenticationConfigMap =
            arguments?.getSerializable("authenticationConfig") as Map<String, AuthenticationItemConfig>
        authenticationInfo = arguments?.getSerializable("authenticationInfo") as AuthenticationInfo
        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<Boolean>("isSubmitted").observe(currentBackStackEntry){
            navController.previousBackStackEntry!!.savedStateHandle["isSubmitted"] = true
            navController.popBackStack()
        }
    }

    private fun loadData() {
        showLoading()
        renderStateManager.setOnAllRenderedListener {
            hiddenLoading()
        }
        initMapper()
        renderAuthority {
            setAuthenticationInfo {
                //所有下拉选择器都需要单独初始化，因为需要加载下拉列表数据
                initAddressSelector()
                initUnitSelector()
            }
        }
        nextStep.setOnClickListener {
            if (submitVerify()) {
                this.authenticationInfo = getFormValues()
                var bundle = bundleOf(
                    "authenticationState" to authenticationState,
                    "authenticationConfig" to authenticationConfigMap,
                    "authenticationInfo" to authenticationInfo,
                )
                parentFragment?.findNavController()?.navigate(
                    R.id.action_authenticationStepTwoFragment_to_authenticationStepThirdFragment,
                    bundle,
                    null,
                    null
                )
            }
        }
    }

    private fun showLoading() {
        province.isHintAnimationEnabled = false
        city.isHintAnimationEnabled = false
        county.isHintAnimationEnabled = false
        street.isHintAnimationEnabled = false
        unitProvince.isHintAnimationEnabled = false
        unitCity.isHintAnimationEnabled = false
        unit.isHintAnimationEnabled = false
    }

    private fun hiddenLoading() {
        province.isHintAnimationEnabled = true
        city.isHintAnimationEnabled = true
        county.isHintAnimationEnabled = true
        street.isHintAnimationEnabled = true
        unitProvince.isHintAnimationEnabled = true
        unitCity.isHintAnimationEnabled = true
        unit.isHintAnimationEnabled = true
        form.visibility = View.VISIBLE
        loading.visibility = View.GONE
    }

    private fun initMapper() {
        aloneConfigItems = mutableListOf(
            unitProvince,
            unitCity,
            unit
        )
        configKeyViewMap = mutableMapOf(
            Pair("userName", Pair(name, null)),
            Pair("phone", Pair(phone, null)),
            Pair("province", Pair(province, formComposeAddress)),
            Pair("city", Pair(city, formComposeAddress)),
            Pair("county", Pair(county, formComposeAddress)),
            Pair("street", Pair(street, formComposeAddress)),
            Pair("address", Pair(address, formComposeAddress)),
            Pair("un-province", Pair(unitProvince, formComposeUnit)),
            Pair("un-city", Pair(unitCity, formComposeUnit)),
            Pair("unit", Pair(unit, formComposeUnit)),
        )
        viewAuthenticationInfo =
            mutableMapOf(
                Pair(name, "Name"),
                Pair(phone, "Phone"),
                Pair(province, "ProvinceId"),
                Pair(city, "CityId"),
                Pair(county, "CountyId"),
                Pair(street, "StreetId"),
                Pair(address, "Address"),
                Pair(unitProvince, "UnProvinceId"),
                Pair(unitCity, "UnCityId"),
                Pair(unit, "UnitUsername"),
            )
        authenticationInfo?.let {
            authenticationInfoMap =
                gson.fromJson<MutableMap<String, String?>>(
                    gson.toJson(authenticationInfo),
                    MutableMap::class.java
                )
        }
    }

    private fun setAuthenticationInfo(callback: () -> Unit) {
        authenticationInfo?.let {
            authenticationConfigMap.forEach { configMap ->
                val formItemAndComposePair = getFormItemAndComposePair(configMap.key)
                val view = formItemAndComposePair.first
                view?.let {
                    val formItemConfig = configMap.value
                    val value = viewAuthenticationInfo[view]
                    when (formItemConfig.Type) {
                        0 -> {
                            view as TextInputLayout
                            view.editText?.setText(authenticationInfoMap[value])
                        }
                        1 -> {

                        }
                    }
                }
            }
        }
        callback()
    }

    private fun renderAuthority(callback: () -> Unit) {
        authenticationConfigMap.forEach { configMap ->
            val formItemAndComposePair = getFormItemAndComposePair(configMap.key)
            var formItem: View? = formItemAndComposePair.first
            var compose: View? = formItemAndComposePair.second
            formItem?.let {
                val formItemConfig = configMap.value
                //可否显示
                if (formItemConfig.Show) {
                    formItem.visibility = View.VISIBLE
                    compose?.let {
                        compose.visibility = View.VISIBLE
                    }
                } else {
                    formItem.visibility = View.GONE
                    compose?.let {
                        compose.visibility = View.GONE
                    }
                }
                //可否编辑
                formItem.isEnabled = formItemConfig.Editable
                //是否必填
                if (formItemConfig.Verify == "disabled") {
                    when (formItemConfig.Type) {
                        0, 1 -> {
                            formItem as TextInputLayout
                            formItem.hint = formItem.hint.toString() + "(可选)"
                        }
                    }
                }
            }
        }
        //未认证状态下，培训单位所在地、培训单位和身份类型规则设置无效，以上依据内容判断，若有值，不允许编辑，反之则允许编辑。
        when (authenticationState) {
            AuthenticationStateEnum.UN_AUTHENTICATION -> {
                aloneConfigItems.forEach {
                    if ((!authenticationInfoMap[viewAuthenticationInfo[it]].isNullOrEmpty()) && (authenticationInfoMap[viewAuthenticationInfo[it]] != "-1")) {
                        it.isEnabled = false
                    }
                }
            }
            else -> {

            }
        }
        callback()
    }

    private fun getFormItemAndComposePair(itemName: String): Pair<View?, View?> {
        var viewPair: Pair<View?, View?>? = try {
            configKeyViewMap[itemName]
        } catch (e: ArithmeticException) {
            null
        }
        return viewPair ?: Pair(null, null)
    }

    private fun loadProvinceData(defaultValue: String?, rendered: ((String) -> Unit)?) {
        val selectorFlag = "province"
        renderStateManager.register(selectorFlag)
        authenticationViewModel.province.observe(viewLifecycleOwner) { provinces ->
            renderSelector(
                province,
                provinces,
                defaultValue,
                rendered = {
                    if (rendered != null) {
                        rendered(selectorFlag)
                    }
                },
                selected = { isChanged, selectedValue ->
                    if (isChanged) {
                        clearSelector(mutableListOf(city, county, street))
                    }
                    loadCityData(selectedValue, null, null)
                })
        }
    }

    private fun loadCityData(
        provinceId: String?,
        defaultValue: String?,
        rendered: ((String) -> Unit)?
    ) {
        val selectorFlag = "city"
        renderStateManager.register(selectorFlag)
        if (!provinceId.isNullOrEmpty()) {
            authenticationViewModel.city(provinceId).observe(viewLifecycleOwner) { cities ->
                renderSelector(
                    city,
                    cities,
                    defaultValue,
                    rendered = {
                        if (rendered != null) {
                            rendered(selectorFlag)
                        }
                    },
                    selected = { isChanged, selectedValue ->
                        if (isChanged) {
                            clearSelector(mutableListOf(county, street))
                        }
                        loadCountyData(selectedValue, null, null)
                    })
            }
        } else {
            if (rendered != null) {
                rendered(selectorFlag)
            }
        }
    }

    private fun loadCountyData(
        cityId: String?,
        defaultValue: String?,
        rendered: ((String) -> Unit)?
    ) {
        val selectorFlag = "county"
        renderStateManager.register(selectorFlag)
        if (!cityId.isNullOrEmpty()) {
            authenticationViewModel.county(cityId).observe(viewLifecycleOwner) { counties ->
                renderSelector(
                    county,
                    counties,
                    defaultValue,
                    rendered = {
                        if (rendered != null) {
                            rendered(selectorFlag)
                        }
                    },
                    selected = { isChanged, selectedValue ->
                        if (isChanged) {
                            clearSelector(mutableListOf(street))
                        }
                        loadStreetData(selectedValue, null, null)
                    })
            }
        } else {
            if (rendered != null) {
                rendered(selectorFlag)
            }
        }
    }

    private fun loadStreetData(
        countyId: String?,
        defaultValue: String?,
        rendered: ((String) -> Unit)?
    ) {
        val selectorFlag = "street"
        renderStateManager.register(selectorFlag)
        if (!countyId.isNullOrEmpty()) {
            authenticationViewModel.street(countyId).observe(viewLifecycleOwner) { streets ->
                renderSelector(
                    street,
                    streets,
                    defaultValue,
                    rendered = {
                        if (rendered != null) {
                            rendered(selectorFlag)
                        }
                    },
                    selected = { isChanged, selectedValue ->

                    })
            }
        } else {
            if (rendered != null) {
                rendered(selectorFlag)
            }
        }
    }


    private fun initUnitSelector() {
        var defaultValue = authenticationInfo?.UnProvinceId
        loadUnitProvinceData(defaultValue) {
            renderStateManager.rendered(it)
        }
        loadUnitCityData(authenticationInfo?.UnProvinceId, authenticationInfo?.UnCityId) {
            renderStateManager.rendered(it)
        }
        loadUnitData(authenticationInfo?.UnCityId, authenticationInfo?.UnitUsername) {
            renderStateManager.rendered(it)
        }
    }

    private fun loadUnitProvinceData(defaultValue: String?, rendered: ((String) -> Unit)?) {
        val selectorFlag = "unitProvince"
        renderStateManager.register(selectorFlag)
        authenticationViewModel.province.observe(viewLifecycleOwner) { provinces ->
            renderSelector(
                unitProvince,
                provinces,
                defaultValue,
                rendered = {
                    if (rendered != null) {
                        rendered(selectorFlag)
                    }
                },
                selected = { isChanged, selectedValue ->
                    if (isChanged) {
                        clearSelector(mutableListOf(unitCity, unit))
                    }
                    loadUnitCityData(selectedValue, null, null)
                })
        }
    }

    private fun loadUnitCityData(
        provinceId: String?,
        defaultValue: String?,
        rendered: ((String) -> Unit)?
    ) {
        val selectorFlag = "unitCity"
        renderStateManager.register(selectorFlag)
        if (!provinceId.isNullOrEmpty()) {
            authenticationViewModel.city(provinceId).observe(viewLifecycleOwner) { cities ->
                renderSelector(
                    unitCity,
                    cities,
                    defaultValue,
                    rendered = {
                        if (rendered != null) {
                            rendered(selectorFlag)
                        }
                    },
                    selected = { isChanged, selectedValue ->
                        if (isChanged) {
                            clearSelector(mutableListOf(unit))
                        }
                        loadUnitData(selectedValue, null, null)
                    })
            }
        } else {
            if (rendered != null) {
                rendered(selectorFlag)
            }
        }
    }


    private fun loadUnitData(
        cityId: String?,
        defaultValue: String?,
        rendered: ((String) -> Unit)?
    ) {
        val selectorFlag = "unit"
        renderStateManager.register(selectorFlag)
        if (!cityId.isNullOrEmpty()) {
            authenticationViewModel.unit(cityId).observe(viewLifecycleOwner) { units ->
                renderSelector(
                    unit,
                    units,
                    defaultValue,
                    rendered = {
                        if (rendered != null) {
                            rendered(selectorFlag)
                        }
                    },
                    selected = { isChanged, selectedValue ->

                    })
            }
        } else {
            if (rendered != null) {
                rendered(selectorFlag)
            }
        }
    }

    private fun renderSelector(
        view: TextInputLayout,
        list: MutableList<Selector>,
        defaultValue: String?,
        rendered: (() -> Unit)?,
        selected: ((Boolean, String) -> Unit)?
    ) {
        val selectorAdapter = SelectorAdapter(context!!, list)
        if (!defaultValue.isNullOrEmpty()) {
            selectorAdapter.value = defaultValue
            for (item in list) {
                if (item.Value == defaultValue) {
                    view.editText?.setText(item.Title)
                }
            }
        }
        val materialAutoCompleteTextView = (view.editText as? MaterialAutoCompleteTextView)
        materialAutoCompleteTextView?.setAdapter(selectorAdapter)
        materialAutoCompleteTextView?.onItemClickListener =
            OnItemClickListener { parent, _, position, _ ->
                val value = list[position].Value
                val isChange = (parent.adapter as SelectorAdapter).value != value
                (parent.adapter as SelectorAdapter).value = value
                selected?.let {
                    selected(isChange, value)
                }
            }
        rendered?.let {
            rendered()
        }
    }

    private fun clearSelector(views: List<TextInputLayout>) {
        views.forEach {
            (it.editText as? MaterialAutoCompleteTextView)?.setAdapter(null)
            it.editText?.text?.clear()
        }
    }

    private fun submitVerify(): Boolean {
        var verifyPass = true
        authenticationConfigMap.forEach { configMap ->
            val formItemAndComposePair = getFormItemAndComposePair(configMap.key)
            var formItem: View? = formItemAndComposePair.first
            formItem?.let {
                val formItemConfig = configMap.value
                formItem as TextInputLayout
                if (formItemConfig.Verify != "disabled") {
                    val verifyString = formItemConfig.Verify
                    val verifyArray = verifyString.split("|")
                    formItem as TextInputLayout
                    when (formItemConfig.Type) {
                        0 -> {
                            val text = formItem.editText?.text
                            var error: String? = null
                            if ("required" in verifyArray) {
                                if (text?.trim().isNullOrEmpty()) {
                                    verifyPass = false
                                    error = "此项不能为空"
                                } else if ("number" in verifyArray) {
                                    if (!Pattern.matches("^[0-9]*\$", text?.trim())) {
                                        verifyPass = false
                                        error = "请输入数字"
                                    }
                                } else if ("phone" in verifyArray) {
                                    if (!ValidityUtils.isPhone(text?.trim().toString())
                                    ) {
                                        verifyPass = false
                                        error = "请输入正确电话号码"
                                    }
                                }
                            }
                            if (error.isNullOrEmpty()) {
                                formItem.error = null
                            } else {
                                formItem.error = error
                            }
                            formItem.isErrorEnabled = !formItem.error.isNullOrEmpty()
                        }
                        1 -> {
                            val text = formItem.editText?.text
                            var error: String? = null
                            if ("required" in verifyArray || "dropDownList" in verifyArray) {
                                if (text?.trim().isNullOrEmpty()) {
                                    verifyPass = false
                                    error = "此项不能为空"
                                }
                            }
                            if (error.isNullOrEmpty()) {
                                formItem.error = null
                            } else {
                                formItem.error = error
                            }
                            formItem.isErrorEnabled = !formItem.error.isNullOrEmpty()
                        }
                    }
                } else {
                    when (formItemConfig.Type) {
                        0, 1 -> {
                            formItem.isErrorEnabled = false
                        }
                    }
                }
            }
        }
        return verifyPass
    }

    private fun getFormValues(): AuthenticationInfo {
        authenticationConfigMap.forEach { configMap ->
            val formItemAndComposePair = getFormItemAndComposePair(configMap.key)
            val view = formItemAndComposePair.first
            view?.let {
                val formItemConfig = configMap.value
                val formName = viewAuthenticationInfo[view]
                if (!formName.isNullOrEmpty()) {
                    if (formItemConfig.Show) {
                        val formValue: String? = when (formItemConfig.Type) {
                            0 -> {
                                view as TextInputLayout
                                view.editText?.text.toString()
                            }
                            1 -> {
                                view as TextInputLayout
                                (view.editText as? MaterialAutoCompleteTextView)?.adapter?.let {
                                    val selectorAdapter =
                                        it as SelectorAdapter
                                    selectorAdapter.value
                                }
                            }
                            else -> {
                                null
                            }
                        }
                        authenticationInfoMap[formName!!] = formValue
                    }
                }
            }
        }
        var formValuesJson = gson.toJson(authenticationInfoMap)
        return gson.fromJson(formValuesJson, AuthenticationInfo::class.java)
    }

    private fun initAddressSelector() {
        var defaultValue = authenticationInfo?.ProvinceId
        loadProvinceData(defaultValue) {
            renderStateManager.rendered(it)
        }
        loadCityData(authenticationInfo?.ProvinceId, authenticationInfo?.CityId) {
            renderStateManager.rendered(it)
        }
        loadCountyData(authenticationInfo?.CityId, authenticationInfo?.CountyId) {
            renderStateManager.rendered(it)
        }
        loadStreetData(authenticationInfo?.CountyId, authenticationInfo?.StreetId) {
            renderStateManager.rendered(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            AuthenticationStepTwoFragment()
    }
}