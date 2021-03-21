package com.example.myapplication.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentDetailBinding
import com.example.myapplication.util.*
import com.example.myapplication.viewModel.SharedViewModel

class DetailFragment : Fragment() {

    lateinit var binding: FragmentDetailBinding
    private val viewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            FragmentDetailBinding.inflate(
                inflater,
                container,
                false
            ).apply {
                lifecycleOwner = viewLifecycleOwner
                detail = bleDeviceDetail
                sharedViewModel = viewModel
            }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonDisconnect.setOnClickListener {
            disconnectBLEDevice()
            requireActivity().openFragment(R.id.frameLayout, BLEListFragment())
            showSnackBar(binding.rootDetailLayout, getString(R.string.message_disconnected))
        }

        //for scrollable textView..
        enableScroll(binding.textViewGattServices)
    }
}