package com.xktech.ixueto.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.xktech.ixueto.databinding.FragmentPictureDialogListDialogBinding

class PictureDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentPictureDialogListDialogBinding? = null
    private val binding get() = _binding!!
    private var onCameraClickListener: (() -> Unit)? = null
    private var onPictureClickListener: (() -> Unit)? = null
    fun setOnCameraClickListener(listener: () -> Unit) {
        onCameraClickListener = listener
    }

    fun setOnPictureClickListener(listener: () -> Unit) {
        onPictureClickListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPictureDialogListDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.camera?.setOnClickListener {
            onCameraClickListener?.let { listener->
                this.dismiss()
                listener()
            }
        }
        _binding?.picture?.setOnClickListener {
            onPictureClickListener?.let { listener->
                this.dismiss()
                listener()
            }
        }
    }

    companion object {
        fun newInstance(): PictureDialogFragment =
            PictureDialogFragment()

        const val TAG = "PictureDialogFragment"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}