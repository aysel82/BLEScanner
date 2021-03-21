package com.example.myapplication.data.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.BLEDevice
import com.example.myapplication.databinding.ItemViewBleDeviceBinding

class BLEDeviceAdapter(
    private var values: List<BLEDevice>
) : RecyclerView.Adapter<BLEDeviceAdapter.ViewHolder>() {

    var itemClick: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemViewBleDeviceBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_view_ble_device,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        values.let { list ->
            holder.binding.bleDevice = list[position]
            holder.binding.executePendingBindings()
        }

        holder.binding.buttonConnect.setOnClickListener {
            values[position].isConnected = values[position].isConnected != true
            itemClick?.invoke(position)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(val binding: ItemViewBleDeviceBinding) :
        RecyclerView.ViewHolder(binding.root)
}