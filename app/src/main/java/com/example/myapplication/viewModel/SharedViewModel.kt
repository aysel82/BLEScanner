package com.example.myapplication.viewModel

import android.app.Application
import android.view.View
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.R
import com.example.myapplication.data.GattService
import com.example.myapplication.data.adapter.BLEDeviceAdapter
import com.example.myapplication.util.scanLeDevice
import com.example.myapplication.util.showSnackBar

class SharedViewModel(
    application: Application
) : AndroidViewModel(application), Observable {

    var bleDeviceAdapter: BLEDeviceAdapter? = null

    @Bindable
    val progressBarVisibility = MutableLiveData<Boolean>()

    @Bindable
    var isClicked = MutableLiveData<Boolean>()

    var selectedPosition: Int = -1

    fun onClickScan(view: View) {
        if (isClicked.value == null) {
            isClicked.value = false
        }

        isClicked.value = !(isClicked.value)!!

        if (isClicked.value == false) {
            progressBarVisibility.value = false
            scanLeDevice(false)
            showSnackBar(view, view.context.getString(R.string.stop_scan))
            return
        }
        scanLeDevice(true)
        showSnackBar(view, view.context.getString(R.string.start_scan))
    }

    fun getDetailText(gattServices: List<GattService>?): String {
        var deviceDetail = ""

        gattServices?.let { services ->
            for (i in services.indices) {

                if (!services[i].message.isNullOrEmpty()) {
                    deviceDetail += "\t-> ${services[i].message}\n"
                    break
                }

                deviceDetail += "\n\n"
                deviceDetail += "${i + 1}. Gatt Service: \n"
                deviceDetail += "\t- UUID: ${services[i].gattServiceUUID}\n"

                services[i].gattServiceCharacteristic?.let { characteristics ->
                    for (j in characteristics.indices) {

                        if (!characteristics[j].message.isNullOrEmpty()) {
                            deviceDetail += "\t-> ${characteristics[j].message}\n"
                            break
                        }

                        deviceDetail += "\n"
                        deviceDetail += "\t- ${j + 1}. Characteristic: \n"
                        deviceDetail += "\t\t-> UUID: ${characteristics[j].characteristicUUID}\n"


                        deviceDetail += "\n\t\t-> Properties:\n"
                        characteristics[j].characteristicProperties?.let { properties ->
                            deviceDetail += "\t\t\t- ${properties.propertyRead}\n"
                            deviceDetail += "\t\t\t- ${properties.propertyWrite}\n"
                            deviceDetail += "\t\t\t- ${properties.propertyNotify}\n"
                            deviceDetail += "\t\t\t- ${properties.propertyIndicate}\n"
                        }

                        deviceDetail += "\n\t\t-> Descriptor:\n"
                        characteristics[j].characteristicDescriptor?.let { descriptorList ->
                            for (k in descriptorList.indices) {
                                if (!descriptorList[k].descriptorUUID.isNullOrEmpty())
                                    deviceDetail += "\t\t\t- UUID: ${descriptorList[k].descriptorUUID}\n"

                                if (!descriptorList[k].message.isNullOrEmpty())
                                    deviceDetail += "\t\t\t- ${descriptorList[k].message}\n"
                            }
                        }
                    }
                }
            }
        }
        return deviceDetail
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
    }

}
