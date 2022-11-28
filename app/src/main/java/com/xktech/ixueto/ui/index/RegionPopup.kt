package com.xktech.ixueto.ui.index

import android.content.Context
import android.content.res.Configuration
import android.widget.ListView
import androidx.core.content.ContextCompat
import com.lxj.xpopup.impl.PartShadowPopupView
import com.xktech.ixueto.R
import com.xktech.ixueto.adapter.RegionAdapter
import com.xktech.ixueto.model.Region

class RegionPopup(
    context: Context,
    private val regions: List<Region>,
    private val currentRegionIndex: Int,
) :
    PartShadowPopupView(context) {
    private lateinit var regionList: ListView
    lateinit var onSelectedListen: (Int, Int) -> Unit
    override fun getImplLayoutId(): Int {
        return R.layout.popup_region
    }

    override fun onCreate() {
        super.onCreate()
        var selectedColor = ContextCompat.getColor(context, R.color.md_theme_light_primary)
        if(context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES){
            selectedColor = ContextCompat.getColor(context, R.color.md_theme_dark_primary)
        }
        regionList = findViewById(R.id.region_list)
        regionList.adapter = RegionAdapter(regions, currentRegionIndex,selectedColor)
        regionList.setSelection(currentRegionIndex)
        regionList.setOnItemClickListener { _, _, position, id ->
            onSelectedListen.invoke(position, id.toInt())
        }
    }


    fun setOnSelectedListener(listener: (Int, Int) -> Unit) {
        this.onSelectedListen = listener
    }
}
