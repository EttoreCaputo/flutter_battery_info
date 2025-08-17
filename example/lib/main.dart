import 'package:flutter/material.dart';
import 'package:battery_info/battery_info_plugin.dart';
import 'package:battery_info/model/android_battery_info.dart';
import 'package:battery_info/enums/charging_status.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Battery Info plugin example'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              FutureBuilder<AndroidBatteryInfo?>(
                  future: BatteryInfoPlugin().androidBatteryInfo,
                  builder: (context, snapshot) {
                    if (snapshot.hasData && snapshot.data != null) {
                      return Text(
                          'Battery Health: ${snapshot.data!.health?.toUpperCase() ?? "Unknown"}');
                    }
                    return CircularProgressIndicator();
                  }),
              SizedBox(
                height: 20,
              ),
              StreamBuilder<AndroidBatteryInfo?>(
                  stream: BatteryInfoPlugin().androidBatteryInfoStream,
                  builder: (context, snapshot) {
                    if (snapshot.hasData && snapshot.data != null) {
                      final data = snapshot.data!;
                      return Column(
                        children: [
                          Text("Voltage: ${data.voltage ?? "Unknown"} mV"),
                          SizedBox(
                            height: 20,
                          ),
                          Text(
                              "Charging status: ${data.chargingStatus?.toString().split(".")[1] ?? "Unknown"}"),
                          SizedBox(
                            height: 20,
                          ),
                          Text(
                              "Battery Level: ${data.batteryLevel ?? "Unknown"} %"),
                          SizedBox(
                            height: 20,
                          ),
                          Text(
                              "Battery Capacity: ${data.batteryCapacity != null ? (data.batteryCapacity! / 1000) : "Unknown"} mAh"),
                          SizedBox(
                            height: 20,
                          ),
                          Text(
                              "Battery current average: ${data.currentAverage != null ? (data.currentAverage! ) : "Unknown"} mAh"),
                          SizedBox(
                            height: 20,
                          ),
                          Text(
                              "Temperature: ${data.temperature != null ? (data.temperature!) : "Unknown"} Degree"),
                          SizedBox(
                            height: 20,
                          ),
                          Text("Technology: ${data.technology ?? "Unknown"} "),
                          SizedBox(
                            height: 20,
                          ),
                          Text(
                              "Battery present: ${data.present == true ? "Yes" : "False"} "),
                          SizedBox(
                            height: 20,
                          ),
                          Text("Scale: ${data.scale ?? "Unknown"} "),
                          SizedBox(
                            height: 20,
                          ),
                          Text(
                              "Remaining energy: ${data.remainingEnergy != null ? -(data.remainingEnergy! * 1.0E-9) : "Unknown"} Watt-hours,"),
                          SizedBox(
                            height: 20,
                          ),
                          Text(
                              "Cycles: ${data.cycles != null ? data.cycles! : "Unknown"}"),
                          SizedBox(
                            height: 20,
                          ),
                          Text(
                              "HealthInt: ${data.healthInt != null ? data.healthInt! : "Unknown"}"),
                          SizedBox(
                            height: 20,
                          ),
                          Text(
                              "maxDesignedCapacity: ${data.maxDesignedCapacity != null ? data.maxDesignedCapacity! : "Unknown"}"),
                          SizedBox(
                            height: 20,
                          ),
                          _getChargeTime(data),
                        ],
                      );
                    }
                    return CircularProgressIndicator();
                  })
            ],
          ),
        ),
      ),
    );
  }

  Widget _getChargeTime(AndroidBatteryInfo data) {
    if (data.chargingStatus == ChargingStatus.Charging) {
      return data.chargeTimeRemaining == -1 || data.chargeTimeRemaining == null
          ? Text("Calculating charge time remaining")
          : Text(
              "Charge time remaining: ${(data.chargeTimeRemaining! / 1000 / 60).truncate()} minutes");
    }
    return Text("Battery is full or not connected to a power source");
  }

}
