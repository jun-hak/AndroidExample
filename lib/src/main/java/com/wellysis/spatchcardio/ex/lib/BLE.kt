package com.wellysis.spatchcardio.ex.lib

import android.Manifest.permission
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresFeature
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.wellysis.spatchcardio.ex.lib.contracts.BluetoothAdapterContract
import com.wellysis.spatchcardio.ex.lib.exceptions.DisabledAdapterException
import com.wellysis.spatchcardio.ex.lib.exceptions.HardwareNotPresentException
import com.wellysis.spatchcardio.ex.lib.exceptions.PermissionsDeniedException
import com.wellysis.spatchcardio.ex.lib.typealiases.Callback
import com.wellysis.spatchcardio.ex.lib.typealiases.EmptyCallback
import com.wellysis.spatchcardio.ex.lib.typealiases.PermissionReqCallback
import com.wellysis.spatchcardio.ex.lib.utils.PermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume

@Suppress("unused")
@RequiresFeature(name = PackageManager.FEATURE_BLUETOOTH_LE,
    enforcement = "android.content.pm.PackageManager#hasSystemFeature")
class BLE {

    /** For Jetpack Compose activities use*/
    private var componentActivity: ComponentActivity? = null

    /** For regular activities use */
    private var appCompatActivity: AppCompatActivity? = null

    /** For Fragment use */
    private var fragment: Fragment? = null

    /** The provided context, based on [componentActivity], [appCompatActivity] or [fragment] */
    private var context: Context

    /** Coroutine scope based on the given context provider [componentActivity], [appCompatActivity] or [fragment] */
    private val coroutineScope: CoroutineScope
        get() = componentActivity?.lifecycleScope ?: appCompatActivity?.lifecycleScope
        ?: fragment?.lifecycleScope ?: GlobalScope

    /* Bluetooth related variables */
    private var manager: BluetoothManager? = null
    private var adapter: BluetoothAdapter? = null
    private var scanner: BluetoothLeScanner? = null

    /* Contracts */
    private lateinit var adapterContract: ContractHandler<Unit, Boolean>
    private lateinit var permissionContract: ContractHandler<Array<String>, Map<String, Boolean>>
    private lateinit var locationContract: ContractHandler<IntentSenderRequest, ActivityResult>


    constructor(componentActivity: ComponentActivity) {
        Timber.d("Setting up on a ComponentActivity!")
        this.componentActivity = componentActivity
        this.context = componentActivity
        this.setup()
    }

    constructor(activity: AppCompatActivity) {
        Timber.d("Setting up on an AppCompatActivity!")
        this.appCompatActivity = activity
        this.context = activity
        this.setup()
    }

    constructor(fragment: Fragment) {
        Timber.d("Setting up on a Fragment!")
        this.fragment = fragment
        this.context = fragment.requireContext()
        this.setup()
    }

    private fun setup() {
        this.verifyBluetoothHardwareFeature()
        this.registerContracts()
        this.setupBluetoothService()
    }

    private fun setupBluetoothService() {
        Timber.d("Setting up bluetooth service...")

        this.manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        this.adapter = this.manager?.adapter
        this.scanner = this.adapter?.bluetoothLeScanner
    }

    // region Contracts related methods
    private fun registerContracts() {
        Timber.d("Registering contracts...")

        this.adapterContract = ContractHandler(BluetoothAdapterContract(),
            this.componentActivity,
            this.appCompatActivity,
            this.fragment)
        this.permissionContract =
            ContractHandler(ActivityResultContracts.RequestMultiplePermissions(),
                this.componentActivity,
                this.appCompatActivity,
                this.fragment)
        this.locationContract =
            ContractHandler(ActivityResultContracts.StartIntentSenderForResult(),
                this.componentActivity,
                this.appCompatActivity,
                this.fragment)
    }

    private fun verifyBluetoothHardwareFeature() {
        Timber.d("Checking bluetooth hardware on device...")

        context.packageManager.let {
            if (!PermissionUtils.isBluetoothLowEnergyPresentOnDevice(it) || !PermissionUtils.isBluetoothPresentOnDevice(
                    it)
            ) {
                Timber.d("No bluetooth hardware detected on this device!")
                throw HardwareNotPresentException()
            } else {
                Timber.d("Detected bluetooth hardware on this device!")
            }
        }
    }

    @RequiresPermission(allOf = [permission.BLUETOOTH, permission.BLUETOOTH_ADMIN, permission.ACCESS_FINE_LOCATION])
    suspend fun verifyPermissions(rationaleRequestCallback: Callback<EmptyCallback>? = null): Boolean {
        return suspendCancellableCoroutine { continuation ->
            this.verifyPermissionsAsync(rationaleRequestCallback) { status ->
                if (status) continuation.resume(true)
                else continuation.cancel(PermissionsDeniedException())
            }
        }
    }

    @RequiresPermission(allOf = [permission.BLUETOOTH, permission.BLUETOOTH_ADMIN, permission.ACCESS_FINE_LOCATION])
    fun verifyPermissionsAsync(
        rationaleRequestCallback: Callback<EmptyCallback>? = null,
        callback: PermissionReqCallback? = null,
    ) {
        Timber.d("Checking App bluetooth permissions...")

        if (PermissionUtils.isEveryBluetoothPermissionsGranted(context)) {
            Timber.d("All permissions granted!")
            callback?.invoke(true)
        } else {
            // Fetch an Activity from the given context providers
            val providedActivity = this.componentActivity ?: this.appCompatActivity
            ?: this.fragment?.requireActivity()!!
            if (PermissionUtils.isPermissionRationaleNeeded(providedActivity) && rationaleRequestCallback != null) {
                Timber.d("Permissions denied, requesting permission rationale callback...")
                rationaleRequestCallback {
                    launchPermissionRequestContract { granted ->
                        callback?.invoke(granted)
                    }
                }
            } else {
                launchPermissionRequestContract { granted ->
                    callback?.invoke(granted)
                }
            }
        }
    }

    @RequiresPermission(permission.BLUETOOTH_ADMIN)
    suspend fun verifyBluetoothAdapterState(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            this.verifyBluetoothAdapterStateAsync { status ->
                if (status) continuation.resume(true)
                else continuation.cancel(DisabledAdapterException())
            }
        }
    }

    @RequiresPermission(permission.BLUETOOTH_ADMIN)
    fun verifyBluetoothAdapterStateAsync(callback: PermissionReqCallback? = null) {
        Timber.d("Checking bluetooth adapter state...")

        if (this.adapter == null || this.adapter?.isEnabled != true) {
            Timber.d("Bluetooth adapter turned off!")
            launchBluetoothAdapterActivationContract(callback)
        } else callback?.invoke(true)
    }

    @RequiresPermission(anyOf = [permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION])
    fun verifyLocationStateAsync(callback: PermissionReqCallback? = null) {
        Timber.d("Checking location services state...")

        // Builds a location request
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        // Builds a location settings request
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setNeedBle(true)
            .setAlwaysShow(true)
            .build()

        // Execute the location request
        LocationServices.getSettingsClient(context).checkLocationSettings(settingsRequest).apply {
            addOnSuccessListener {
                callback?.invoke(true)
            }

            addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    // If resolution is required from the Google services Api, build an intent to do it and launch the locationContract
                    locationContract.launch(IntentSenderRequest.Builder(e.resolution).build()) {
                        // Check the contract result
                        if (it.resultCode == Activity.RESULT_OK) callback?.invoke(true)
                        else callback?.invoke(false)
                    }
                } else {
                    e.printStackTrace()
                    callback?.invoke(false)
                }
            }
        }
    }

    /**
     * Checks if location services are active
     *
     * If not, automatically requests it's activation to the user
     *
     * @see verifyLocationStateAsync For a variation using callbacks
     *
     * @return True when the location services are active
     **/
    @RequiresPermission(anyOf = [permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION])
    suspend fun verifyLocationState(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            verifyLocationStateAsync { status ->
                if (status) continuation.resume(true)
                else continuation.cancel(DisabledAdapterException())
            }
        }
    }

    private fun launchBluetoothAdapterActivationContract(callback: PermissionReqCallback? = null) {
        Timber.d("Requesting to enable bluetooth adapter to the user...")

        this.adapterContract.launch(Unit) { enabled ->
            Timber.d("Bluetooth adapter activation request result: $enabled")
            callback?.invoke(enabled)
        }
    }

    private fun launchPermissionRequestContract(callback: PermissionReqCallback) {
        Timber.d("Requesting permissions to the user...")

        this.permissionContract.launch(PermissionUtils.permissions) { permissions: Map<String, Boolean> ->
            Timber.d("Permission request result: $permissions")
            callback(permissions.all { it.value })
        }
    }
}