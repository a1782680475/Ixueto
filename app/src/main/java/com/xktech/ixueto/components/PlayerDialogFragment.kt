package com.xktech.ixueto.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.xktech.ixueto.databinding.FragmentPlayerDialogListDialogBinding

class PlayerDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentPlayerDialogListDialogBinding? = null
    private val binding get() = _binding!!
    private var onSettingClickListener: (() -> Unit)? = null
    private var onNoticeClickListener: (() -> Unit)? = null
    fun setOnSettingClickListener(listener: () -> Unit) {
        onSettingClickListener = listener
    }

    fun setOnNoticeClickListener(listener: () -> Unit) {
        onNoticeClickListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayerDialogListDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.setting?.setOnClickListener {
            onSettingClickListener?.let { listener->
                this.dismiss()
                listener()
            }
        }
        _binding?.notice?.setOnClickListener {
            onNoticeClickListener?.let { listener->
                this.dismiss()
                listener()
            }
        }
    }

    companion object {
        fun newInstance(): PlayerDialogFragment =
            PlayerDialogFragment()

        const val TAG = "PlayerDialogFragment"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}