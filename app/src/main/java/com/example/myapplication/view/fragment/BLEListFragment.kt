package com.example.myapplication.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.adapter.BLEDeviceAdapter
import com.example.myapplication.databinding.FragmentBleListBinding
import com.example.myapplication.util.*
import com.example.myapplication.viewModel.SharedViewModel
import java.util.*

class BLEListFragment : Fragment() {

    lateinit var binding: FragmentBleListBinding
    private val viewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            FragmentBleListBinding.inflate(
                inflater,
                container,
                false
            ).apply {
                lifecycleOwner = viewLifecycleOwner
                sharedViewModel = viewModel
            }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the library vertical recyclerView
        with(binding.recyclerViewBLEDeviceList) {
            layoutManager =
                androidx.recyclerview.widget.GridLayoutManager(
                    context,
                    1,
                    RecyclerView.VERTICAL,
                    false
                )
        }

        bleDeviceList.value?.let { list ->
            list[viewModel.selectedPosition].isConnected = false
        }

        bleDeviceList.observe(viewLifecycleOwner, { list ->
            if (list.isEmpty())
                return@observe

            viewModel.progressBarVisibility.value = false

            viewModel.bleDeviceAdapter = BLEDeviceAdapter(list).apply {
                itemClick = { position ->

                    viewModel.selectedPosition = position

                    val selectedBLEDevice = list[position].bleDevice
                    selectedBLEDevice?.let {
                        selectedDevice = selectedBLEDevice
                        connect()
                    }
                }
            }
            binding.recyclerViewBLEDeviceList.adapter = viewModel.bleDeviceAdapter
        })

        bleDeviceScanning.observe(viewLifecycleOwner, { scanning ->
            viewModel.progressBarVisibility.value = scanning
        })
    }

    private fun connect() {
        connectBLEDevice(requireContext()) { resultStatus ->
            when (resultStatus) {
                (ConnectionStatus.CONNECTED) -> {
                    showSnackBar(
                        binding.rootListLayout,
                        getString(R.string.message_connected)
                    )
                }
                (ConnectionStatus.DISCONNECTED) -> {
                    showSnackBar(
                        binding.rootListLayout,
                        getString(R.string.message_disconnected)
                    )
                }
                (ConnectionStatus.SERVICE_DISCOVERED) -> {
                    showSnackBar(
                        binding.rootListLayout,
                        getString(R.string.message_discovered)
                    )

                    requireActivity().openFragment(
                        R.id.frameLayout,
                        DetailFragment()
                    )
                }
                else -> {
                    showSnackBar(
                        binding.rootListLayout,
                        getString(R.string.message_not_connected)
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        scanLeDevice(false)
        viewModel.bleDeviceAdapter = null
    }
}