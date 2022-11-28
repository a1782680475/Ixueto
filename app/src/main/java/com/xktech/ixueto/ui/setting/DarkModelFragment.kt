package com.xktech.ixueto.ui.setting

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.xktech.ixueto.R
import com.xktech.ixueto.data.local.DarkModel
import com.xktech.ixueto.databinding.FragmentDarkModelBinding
import com.xktech.ixueto.viewModel.DarkViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DarkModelFragment : Fragment() {
    private var binding: FragmentDarkModelBinding? = null
    private lateinit var navController: NavController
    private var isFirstLoad = true
    private var rootView: View? = null
    private lateinit var toolBar: Toolbar
    private lateinit var manualSwitch: MaterialSwitch
    private lateinit var darkModelTitle: TextView
    private lateinit var autoOperation: View
    private lateinit var autoType: RadioGroup
    private lateinit var autoOpenSwitch: MaterialSwitch
    private lateinit var follow: RadioButton
    private lateinit var timing: RadioButton
    private lateinit var openTime: View
    private lateinit var lightTimer: TextView
    private lateinit var darkTimer: TextView
    private lateinit var sunIconDrawable: Drawable
    private lateinit var moonIconDrawable: Drawable
    private var darkStartMinutes: Int = 0
    private var darkEndMinutes: Int = 0
    private val darkViewModel: DarkViewModel by viewModels()
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

    }

    override fun onResume() {
        super.onResume()
        initDarkModelOperation()
    }

    private fun initView() {
        binding = FragmentDarkModelBinding.inflate(layoutInflater)
        rootView = binding!!.root
        toolBar = binding!!.toolBar
        manualSwitch = binding!!.manualSwitch
        darkModelTitle = binding!!.darkModel
        autoOperation = binding!!.autoOperation
        autoType = binding!!.autoType!!
        autoOpenSwitch = binding!!.autoOpenSwitch
        follow = binding!!.follow
        timing = binding!!.timing
        openTime = binding!!.openTime
        lightTimer = binding!!.lightTimer
        darkTimer = binding!!.darkTimer
        sunIconDrawable = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.ic_sun_fill
        )!!
        moonIconDrawable = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.ic_moon
        )!!
    }

    private fun initDarkModelOperation() {
        darkViewModel.darkModel.observe(this) {
            if (DarkModel.model) {
                darkModelTitle.text = "深色"
                manualSwitch.thumbIconDrawable = moonIconDrawable
            } else {
                darkModelTitle.text = "浅色"
                manualSwitch.thumbIconDrawable = sunIconDrawable
            }
        }
        manualSwitch.isChecked = DarkModel.model
        if (DarkModel.autoRuleEnable) {
            manualSwitch.isEnabled = false
            autoOpenSwitch.isChecked = true
            autoOperation.visibility = View.VISIBLE
            when (DarkModel.autoRule) {
                0 -> {
                    follow.isChecked = true
                    openTime.visibility = View.GONE
                }
                1 -> {
                    timing.isChecked = true
                    openTime.visibility = View.VISIBLE
                }
            }
        } else {
            manualSwitch.isEnabled = true
            autoOpenSwitch.isChecked = false
            autoOperation.visibility = View.GONE
        }
        darkStartMinutes = DarkModel.darkStartTime
        darkEndMinutes = DarkModel.darkEndTime
        lightTimer.text = getTimeText(darkEndMinutes)
        darkTimer.text = getTimeText(darkStartMinutes)
        manualSwitch.setOnCheckedChangeListener { _view, isChecked ->
            if (_view.isPressed) {
                darkViewModel.saveAutoRuleEnabledState(false)
                darkViewModel.saveDarkModel(isChecked)
            }
        }
        autoOpenSwitch.setOnCheckedChangeListener { _view, isChecked ->
            if (_view.isPressed) {
                if (isChecked) {
                    manualSwitch.isEnabled = false
                    autoOperation.visibility = View.VISIBLE
                    darkViewModel.saveAutoRuleEnabledState(true)
                    when (DarkModel.autoRule) {
                        0 -> {
                            follow.isChecked = true
                            openTime.visibility = View.GONE
                        }
                        1 -> {
                            timing.isChecked = true
                            openTime.visibility = View.VISIBLE
                        }
                    }
                } else {
                    manualSwitch.isEnabled = true
                    autoOperation.visibility = View.GONE
                    darkViewModel.saveAutoRuleEnabledState(false)
                }
            }
        }
        autoType.setOnCheckedChangeListener { _view, checkId: Int ->
            when (checkId) {
                R.id.follow -> {
                    openTime.visibility = View.GONE
                    darkViewModel.saveAutoRule(0)
                }
                R.id.timing -> {
                    openTime.visibility = View.VISIBLE
                    darkViewModel.saveAutoRule(1)
                }
            }
        }
        lightTimer.setOnClickListener {
            val hourAndMinute = getHourAndMinute(darkEndMinutes)
            val picker =
                MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(hourAndMinute.first)
                    .setMinute(hourAndMinute.second)
                    .build()
            picker.show(parentFragmentManager, "tag")

            picker.addOnPositiveButtonClickListener {
                val hour = picker.hour
                val minute = picker.minute
                val minutes = hour * 60 + minute
                darkEndMinutes = minutes
                darkViewModel.saveAutoDarkEndTime(darkEndMinutes)
                lightTimer.text = getTimeText(minutes)
            }
        }
        darkTimer.setOnClickListener {
            val hourAndMinute = getHourAndMinute(darkStartMinutes)
            val picker =
                MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(hourAndMinute.first)
                    .setMinute(hourAndMinute.second)
                    .build()
            picker.show(parentFragmentManager, "tag")
            picker.addOnPositiveButtonClickListener {
                val hour = picker.hour
                val minute = picker.minute
                val minutes = hour * 60 + minute
                darkStartMinutes = minutes
                darkViewModel.saveAutoDarkStartTime(darkStartMinutes)
                darkTimer.text = getTimeText(minutes)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            DarkModelFragment()
    }

    private fun getTimeText(minutes: Int): String {
        var text = ""
        text += autoGenericCode(minutes / 60, 2)
        text += ":"
        text += autoGenericCode(minutes % 60, 2)
        return text
    }

    private fun getHourAndMinute(minutes: Int): Pair<Int, Int> {
        var hour = minutes / 60
        var minute = minutes % 60
        return Pair(hour, minute)
    }

    private fun autoGenericCode(code: Int, num: Int): String {
        var result = "";
        result = String.format("%0" + num + "d", code);

        return result;
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}