package com.wellysis.spatchcardio.ex.sdkexample

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.wellysis.spatchcardio.ex.lib.BLE
import com.wellysis.spatchcardio.ex.lib.exceptions.PermissionsDeniedException
import com.wellysis.spatchcardio.ex.lib.utils.ViewUtils.showToast
import com.wellysis.spatchcardio.ex.sdkexample.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private lateinit var mContext: Context

    private var ble: BLE? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.ble = BLE(this)
        mContext = this
    }

    override fun onStart() {
        super.onStart()

        requestPermissions()
    }

    @SuppressLint("MissingPermission")
    private fun requestPermissions() {
        Timber.d("requestPermissions...")

        lifecycleScope.launch {
            try {
                // Checks the bluetooth permissions
                val isPermissionsGranted = ble?.verifyPermissions(rationaleRequestCallback = { next ->
                    // Shows UI feedback if the bluetooth permissions are denied by user or rationale is required
                    showToast(mContext,"We need the bluetooth permissions!")
                    next()
                })
                // Shows UI feedback if the permissions were denied
                if (isPermissionsGranted == false) {
                    showToast(mContext, "Permissions denied!")
                    return@launch
                }

                // Checks the bluetooth adapter state
                val isBluetoothActive = ble?.verifyBluetoothAdapterState()
                // Shows UI feedback if the adapter is turned off
                if (isBluetoothActive == false) {
                    showToast(mContext, "Bluetooth adapter off!")
                    return@launch
                }

                // Checks the location services state
                val isLocationActive = ble?.verifyLocationState()
                // Shows UI feedback if location services are turned off
                if (isLocationActive == false) {
                    showToast(mContext, "Location services off!")
                    return@launch
                }

                startBluetoothScan()
            } catch(e: PermissionsDeniedException) {
                showToast(mContext, "Permissions were denied!")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startBluetoothScan() {
        lifecycleScope.launch {
//            ble?.scanAsync(
//                onUpdate = this@ScanDevicesFragment::onScanDevicesUpdate,
//                onError = { code ->
//                    showToast("Error code ${code}!")
//                },
//                duration = 0
//            )
        }
    }
}