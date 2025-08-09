package com.igrik12.battery_info

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.EventChannel.StreamHandler
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** BatteryInfoPlugin */
public class BatteryInfoPlugin : FlutterPlugin, MethodCallHandler, StreamHandler {
    private var applicationContext: Context? = null
    private var channel: MethodChannel? = null
    private var streamChannel: EventChannel? = null
    private lateinit var filter: IntentFilter
    private lateinit var batteryManager: BatteryManager
    private var chargingStateChangeReceiver: BroadcastReceiver? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        onAttachedToEngine(flutterPluginBinding.applicationContext, flutterPluginBinding.binaryMessenger)
    }

    private fun onAttachedToEngine(applicationContext: Context, messenger: BinaryMessenger) {
        this.applicationContext = applicationContext
        channel = MethodChannel(messenger, "com.igrik12.battery_info/channel")
        streamChannel = EventChannel(messenger, "com.igrik12.battery_info/stream")
        channel?.setMethodCallHandler(this)
        streamChannel?.setStreamHandler(this)
        filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        batteryManager = applicationContext?.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "getBatteryInfo" -> result.success(getBatteryCall())
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel?.setMethodCallHandler(null)
        streamChannel?.setStreamHandler(null)
        channel = null;
        streamChannel = null;
        if(chargingStateChangeReceiver != null) {
            applicationContext?.unregisterReceiver(chargingStateChangeReceiver);
            applicationContext = null;
            chargingStateChangeReceiver = null;
        }
    }

    /** Gets battery information*/
    private fun getBatteryInfo(intent: Intent): Map<String, Any?> {
        var chargingStatus = getChargingStatus(intent)
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val health = getBatteryHealth(intent)
        val pluggedStatus = getBatteryPluggedStatus(intent)
        var batteryLevel = -1
        var batteryCapacity = -1
        var currentAverage = -1
        var currentNow = -1
        var present = intent.extras?.getBoolean(BatteryManager.EXTRA_PRESENT);
        var scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        var remainingEnergy = -1;
        var cyclesCount = -1;
        var batteryTemperature = -1;
        var technology = intent.extras?.getString(BatteryManager.EXTRA_TECHNOLOGY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            batteryCapacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            currentAverage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
            currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            remainingEnergy = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            cyclesCount = intent.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)
        }

        val chargeTimeRemaining = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            batteryManager.computeChargeTimeRemaining()
        } else {
            -1
        }
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        return mapOf(
                "batteryLevel" to batteryLevel,
                "batteryCapacity" to batteryCapacity,
                "chargeTimeRemaining" to chargeTimeRemaining,
                "chargingStatus" to chargingStatus,
                "currentAverage" to currentAverage,
                "currentNow" to currentNow,
                "health" to health,
                "present" to present,
                "pluggedStatus" to pluggedStatus,
                "remainingEnergy" to remainingEnergy,
                "scale" to scale,
                "technology" to technology,
                "temperature" to temperature / 10,
                "voltage" to voltage,
                "cyclesCount" to cyclesCount
        )
    }

    /** Gets the current charging state of the device */
    private fun getChargingStatus(intent: Intent): String {
        return when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "charging"
            BatteryManager.BATTERY_STATUS_FULL -> "full"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "discharging"
            else -> {
                "unknown"
            }
        }
    }

    /** Gets the battery health */
    private fun getBatteryHealth(intent: Intent): String {
        return when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "health_good"
            BatteryManager.BATTERY_HEALTH_DEAD -> "dead"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "over_heat"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "over_voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "cold"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "unspecified_failure"
            else -> {
                "unknown"
            }
        }
    }

    /** Gets the battery plugged status */
    private fun getBatteryPluggedStatus(intent: Intent): String {
        return when (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "wireless"
            else -> {
                "unknown"
            }
        }
    }

    /**This call acts as a MethodChannel handler to retrieve battery information*/
    private fun getBatteryCall(): Map<String, Any?> {
        val intent: Intent? = applicationContext?.registerReceiver(null, filter)
        return intent?.let { getBatteryInfo(it) }!!;
    }

    override fun onListen(arguments: Any?, events: EventSink?) {
        chargingStateChangeReceiver = createChargingStateChangeReceiver(events);
        applicationContext?.registerReceiver(
                chargingStateChangeReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    override fun onCancel(arguments: Any?) {
        applicationContext!!.unregisterReceiver(chargingStateChangeReceiver);
        chargingStateChangeReceiver = null;
    }

    /** Creates broadcast receiver object that provides battery information upon subscription to the stream */
    private fun createChargingStateChangeReceiver(events: EventSink?): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                events?.success(intent?.let { getBatteryInfo(it) })
            }
        }
    }
}
