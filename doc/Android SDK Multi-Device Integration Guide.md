# SDK Integration Guide

> Audience: App developers integrating this Bluetooth SDK
> 
> Devices are uniquely identified by their **Bluetooth MAC address (mac)**.

---

## 0. Key Conventions

- SDK unified entry point: `ControlBleTools.getInstance()`.
- Device protocol constants (`BleCommonAttributes`):
  - `DEVICE_PROTOCOL_APRICOT` —— first-generation device (Apricot)
  - `DEVICE_PROTOCOL_BERRY` —— second-generation device (Berry, SPP)
  - `DEVICE_PROTOCOL_BERRY_RING` —— second-generation ring (BERRY_RING, GATT)
  - `DEVICE_PROTOCOL_BERRY_TEST` —— second-generation test protocol
- Initialization (**only needed once globally**):
  ```java
  ControlBleTools.getInstance().init(context, bleStateCallBack);
  ```
- Data callbacks are registered uniformly via `CallBackUtils.setXxx(...)` (see Section 4).

---

## 1. How to Handle a Single Device

For a single device, you can simply rely on the "default device" semantics and there is no need to deal with the mac:

```java
// 1) Initialize (once)
ControlBleTools.getInstance().init(context, new BleStateCallBack() {
    @Override
    public void onConnectState(int state) {
        // Single device: there is only this one, just handle it based on state
    }
    // If you need the mac as well, override the mac-bearing overload
    // @Override public void onConnectState(String mac, int state) { ... }
});

// 2) Connect (pass the protocol according to the actual device)
ControlBleTools.getInstance().connect(deviceName, deviceAddress,
        BleCommonAttributes.DEVICE_PROTOCOL_APRICOT);

// 3) Register data callback (single global instance, no ambiguity for a single device)
CallBackUtils.setFitnessDataCallBack(new FitnessDataCallBack() {
    @Override
    public void onDailyData(DailyDataBean bean) { /* handle this device's data */ }
    // ... other onXxx
});

// 4) Business command (e.g., get device info)
ControlBleTools.getInstance().getDeviceInfo(listener);

// 5) Disconnect
ControlBleTools.getInstance().disconnect();
```

> Under a single device, parameterless (or device-ignoring) interfaces such as
> `connect / disconnect / getDeviceInfo` operate on the "current default device",
> behaving exactly the same as the previous version.

---

## 2. How to Connect Multiple Devices

The SDK supports connecting multiple devices simultaneously, with each device's connection state managed independently by its mac.

**Recommended approach: obtain a client for each device by its mac, then connect.**

```java
// Assume two devices have been scanned: devA(macA), devB(macB)
String macA = devA.getAddress();
String macB = devB.getAddress();

// Connect each device separately (pass the protocol according to its type)
ControlBleTools.getInstance().getDeviceClient(macA)
        .connect(devA.getName(), BleCommonAttributes.DEVICE_PROTOCOL_APRICOT);

ControlBleTools.getInstance().getDeviceClient(macB)
        .connect(devB.getName(), BleCommonAttributes.DEVICE_PROTOCOL_BERRY_RING);
```

**Distinguishing connection states**: In a multi-device setup, override the **mac-bearing overload** in `BleStateCallBack`; the callback will explicitly tell you which device it is:

```java
ControlBleTools.getInstance().setBleStateCallBack(new BleStateCallBack() {
    @Override
    public void onConnectState(String mac, int state) {
        // mac indicates which device; state uses the BleCommonAttributes.STATE_* constants
        if (state == BleCommonAttributes.STATE_CONNECTED) {
            updateDeviceUI(mac, "Connected");
        }
    }
});
```

**Disconnect a specific device**:

```java
ControlBleTools.getInstance().disconnect(macA);   // Disconnect one specific device
// ControlBleTools.getInstance().disconnect();    // No-arg: disconnect the default device
```

**Check whether a specific device is connected**:

```java
boolean connected = ControlBleTools.getInstance().isConnect(macA);
```

---

## 3. Calling "Specific Device" APIs in a Multi-Device Setup

The SDK provides two equivalent ways to **bind subsequent operations to a device's mac**, ensuring that commands are sent only to that device and that state is read only from that device.

### Approach 1: `getDeviceClient(mac)` (per-device client, recommended)

```java
IDeviceClient clientA = ControlBleTools.getInstance().getDeviceClient(macA);

clientA.connect(name, protocol);   // Connect this device
clientA.disconnect();              // Disconnect this device
boolean ok = clientA.isConnected();// Check this device
String mac = clientA.getMac();     // The mac (uppercase) of the device this client represents

// When you need to execute any SDK command in this device's context:
// run takes a DeviceAction; the lambda parameter is the current ControlBleTools instance,
// and the business methods called within it all act on macA.
clientA.run(tools -> {
    tools.getDeviceInfo(listener);  // Only for macA
    tools.bindDevice(listener);
});
```

### Approach 2: `runForDevice(mac, action)` (temporarily execute a block in a device's context)

```java
// runForDevice takes a DeviceAction; the lambda parameter is the current ControlBleTools instance,
// and the business methods called within it all act only on macB.
ControlBleTools.getInstance().runForDevice(macB, tools -> {
    tools.bindDevice(listener);
    tools.setUserInformation(distanceUnit, temperatureUnit, userInfo, listener);
});
```

> Key point: **In concurrent multi-device scenarios, do not use parameterless / default-device
> interfaces to operate on a specific device**, otherwise you may accidentally affect the
> "default device". Always specify the target via `getDeviceClient(mac)` or `runForDevice(mac, …)`.

---

## 4. Multi-Device: How to Identify Which Device a Data Callback Belongs To

### 4.1 Facts

`CallBackUtils.setXxx(...)` registers a **single global** callback object.
Data from multiple devices all enters the **same** callback method, and the callback method's parameters **do not include the device mac** (for example, `onDailyData(bean)` carries only business data).

Therefore: to know "which device this data came from" inside a callback, you must read the source device of the current callback within the callback method.

### 4.2 Correct Approach: Call `getCallbackDeviceMac()` Inside the Callback

```java
CallBackUtils.setFitnessDataCallBack(new FitnessDataCallBack() {
    @Override
    public void onDailyData(DailyDataBean bean) {
        // ① You must retrieve the "source device of this callback" synchronously, on the first line of the callback
        String mac = CallBackUtils.getCallbackDeviceMac();
        if (mac == null) mac = ControlBleTools.getInstance().getCurrentDeviceMac();

        // ② Route to the corresponding device's data model / UI accordingly
        myDeviceRepository.get(mac).onDailyData(bean);
    }
    // onSleepData / onContinuousHeartRateData / ... same rule: capture the mac on the first line
});
```

- `CallBackUtils.getCallbackDeviceMac()` returns the **mac of the device that is currently triggering the callback**; it returns `null` when called outside of a callback.
- Even if devices A and B pull daily data **simultaneously** and each triggers multiple `onDailyData / onSleepData / ...`, the SDK guarantees that each device's multiple callbacks are correctly attributed to the corresponding mac — no cross-device mixing.

> ⚠️ **Important constraint**: `getCallbackDeviceMac()` is only valid **during the synchronous execution of the callback**.
> If you `post` the data to another thread inside `onXxx` / read the mac after `runOnUiThread`,
> the callback has already ended at that point, and the value obtained will be `null` or belong to another device.
> **Always capture the mac on the first line of the callback before forwarding.**

### 4.3 Connection-State Callbacks Do Not Need This

Connection-state callbacks (e.g., `BleStateCallBack.onConnectState(String mac, int state)`) **already carry the mac parameter** in their interface signature, so just use it directly — there is no need to call `getCallbackDeviceMac()`.

### 4.4 Recommended Multi-Device Data Callback Patterns

- **Pattern A (Simple)**: Register only one global `FitnessDataCallBack`, capture the mac with `getCallbackDeviceMac()` on the first line of the callback, then dispatch to `Map<mac, DeviceModel>`.
- **Pattern B (Strong isolation)**: Maintain your own `Map<String, FitnessDataCallBack>` routing table; in the global callback, forward to the corresponding device's sub-callback based on the captured mac, so that each device's UI subscribes to its own sub-callback.

---

## Appendix: Quick Reference of Common Public APIs

| Capability | API                                                                                                                  |
| --- |----------------------------------------------------------------------------------------------------------------------|
| Initialization | `ControlBleTools.getInstance().init(context, bleStateCallBack)`                                                      |
| Register connection-state callback | `setBleStateCallBack(BleStateCallBack)`                                                                              |
| Connect (specify protocol) | `connect(String name, String address, String deviceProtocol)`                                                        |
| Disconnect (specify device) | `disconnect(String mac)` / no-arg `disconnect()` (default device)                                                     |
| Is connected (specify device) | `isConnect(String mac)`                                                                                              |
| Get per-device client | `getDeviceClient(String mac)` → `IDeviceClient` (getMac / connect / disconnect / isConnected / run)                 |
| Execute in a device's context | `runForDevice(String mac, DeviceAction)` (`DeviceAction.run(ControlBleTools tools)`, the lambda parameter is the current instance) |
| Read callback source device | `CallBackUtils.getCallbackDeviceMac()` (call inside a data callback)                                                 |
| Current device mac | `getCurrentDeviceMac()` (prefers context, otherwise the default device; returns an empty string when there is no device) |
| Connected device list | `getConnectedDevices()` → `List<String>` (mac list)                                                                  |
| Max simultaneous connections | `setMaxConnections(int)` / `getMaxConnections()` (default 5)                                                         |
| Data callback registration | `CallBackUtils.setFitnessDataCallBack(...)` and the other `setXxx` series                                             |
| Protocol constants | `BleCommonAttributes.DEVICE_PROTOCOL_APRICOT / BERRY / BERRY_RING / BERRY_TEST`                                      |
| Connection-state constants | `BleCommonAttributes.STATE_DISCONNECTED / STATE_CONNECTING / STATE_CONNECTED / STATE_DISCONNECTING / STATE_TIME_OUT` |
