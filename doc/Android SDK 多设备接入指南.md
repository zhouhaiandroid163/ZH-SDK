# SDK 接入指南

> 适用对象：接入本蓝牙 SDK 的 App 开发者
> 
> 设备以 **蓝牙 MAC 地址（mac）** 作为唯一标识。

---

## 0. 关键约定

- SDK 统一入口：`ControlBleTools.getInstance()`。
- 设备协议常量（`BleCommonAttributes`）：
  - `DEVICE_PROTOCOL_APRICOT` —— 一代设备（Apricot）
  - `DEVICE_PROTOCOL_BERRY` —— 二代设备（Berry，SPP）
  - `DEVICE_PROTOCOL_BERRY_RING` —— 二代戒指（BERRY_RING，GATT）
  - `DEVICE_PROTOCOL_BERRY_TEST` —— 二代测试协议
- 初始化（**全局只需一次**）：
  ```java
  ControlBleTools.getInstance().init(context, bleStateCallBack);
  ```
- 数据回调统一通过 `CallBackUtils.setXxx(...)` 注册（见第 4 节）。

---

## 1. 单设备如何处理

单设备时，直接走「默认设备」语义即可，无需关心 mac：

```java
// 1) 初始化（一次）
ControlBleTools.getInstance().init(context, new BleStateCallBack() {
    @Override
    public void onConnectState(int state) {
        // 单设备：只会有这一台，直接按 state 处理即可
    }
    // 如需 mac 也能重写带 mac 的重载
    // @Override public void onConnectState(String mac, int state) { ... }
});

// 2) 连接（协议按实际设备传入）
ControlBleTools.getInstance().connect(deviceName, deviceAddress,
        BleCommonAttributes.DEVICE_PROTOCOL_APRICOT);

// 3) 注册数据回调（全局唯一一份，单设备无歧义）
CallBackUtils.setFitnessDataCallBack(new FitnessDataCallBack() {
    @Override
    public void onDailyData(DailyDataBean bean) { /* 处理这台设备的数据 */ }
    // ... 其它 onXxx
});

// 4) 业务指令（如获取设备信息）
ControlBleTools.getInstance().getDeviceInfo(listener);

// 5) 断开
ControlBleTools.getInstance().disconnect();
```

> 单设备下 `connect / disconnect / getDeviceInfo` 等无参（或忽略设备）接口
> 都会作用于「当前默认设备」，行为与旧版完全一致。

---

## 2. 多设备如何连接

SDK 支持同时连接多台设备，每台设备以 mac 独立管理连接状态。

**推荐做法：用每台设备的 mac 获取客户端，再连接。**

```java
// 假设已扫描到两台设备：devA(macA)、devB(macB)
String macA = devA.getAddress();
String macB = devB.getAddress();

// 分别为每台设备连接（协议按各自类型传入）
ControlBleTools.getInstance().getDeviceClient(macA)
        .connect(devA.getName(), BleCommonAttributes.DEVICE_PROTOCOL_APRICOT);

ControlBleTools.getInstance().getDeviceClient(macB)
        .connect(devB.getName(), BleCommonAttributes.DEVICE_PROTOCOL_BERRY_RING);
```

**连接状态区分**：多设备时请在 `BleStateCallBack` 里重写**带 mac 的重载**，回调会明确告知是哪台设备：

```java
ControlBleTools.getInstance().setBleStateCallBack(new BleStateCallBack() {
    @Override
    public void onConnectState(String mac, int state) {
        // mac 标明是哪台设备；state 见 BleCommonAttributes.STATE_* 常量
        if (state == BleCommonAttributes.STATE_CONNECTED) {
            updateDeviceUI(mac, "已连接");
        }
    }
});
```

**断开指定设备**：

```java
ControlBleTools.getInstance().disconnect(macA);   // 断开某一台
// ControlBleTools.getInstance().disconnect();    // 无参：断开默认设备
```

**判断某台是否已连接**：

```java
boolean connected = ControlBleTools.getInstance().isConnect(macA);
```

---

## 3. 多设备时如何调用「指定设备」的独立接口

SDK 提供两种等价方式，把后续操作**绑定到某台设备的 mac**，
确保指令只发给那台设备、状态也只读取那台设备。

### 方式一：`getDeviceClient(mac)`（面向单设备的客户端，推荐）

```java
IDeviceClient clientA = ControlBleTools.getInstance().getDeviceClient(macA);

clientA.connect(name, protocol);   // 连这台
clientA.disconnect();              // 断这台
boolean ok = clientA.isConnected();// 查这台
String mac = clientA.getMac();     // 该客户端对应的设备 mac（大写）

// 需要在这台设备的上下文中执行任意 SDK 指令时：
// run 接收 DeviceAction，lambda 入参为当前 ControlBleTools 实例，
// 其内调用的业务方法都会作用于 macA。
clientA.run(tools -> {
    tools.getDeviceInfo(listener);  // 只针对 macA
    tools.bindDevice(listener);
});
```

### 方式二：`runForDevice(mac, action)`（临时在某设备上下文执行一段代码）

```java
// runForDevice 接收 DeviceAction，lambda 入参为当前 ControlBleTools 实例，
// 其内调用的业务方法都只作用于 macB。
ControlBleTools.getInstance().runForDevice(macB, tools -> {
    tools.bindDevice(listener);
    tools.setUserInformation(distanceUnit, temperatureUnit, userInfo, listener);
});
```

> 要点：**多设备并发时，不要使用无参/默认设备的接口去操作特定设备**，
> 否则会误操作「默认设备」。始终通过 `getDeviceClient(mac)` 或 `runForDevice(mac, …)` 指定目标。

---

## 4. 多设备时：CallBackUtils 数据回调如何区分是哪台设备的数据

### 4.1 事实

`CallBackUtils.setXxx(...)` 注册的是**全局唯一一份**回调对象。
多台设备的数据都会进入**同一个**回调方法，且回调方法的入参里**不含设备 mac**（例如 `onDailyData(bean)` 只有业务数据）。

所以：要在回调里知道「这条数据来自哪台设备」，请在回调方法内读取当前回调来源设备。

### 4.2 正确做法：在回调内调用 `getCallbackDeviceMac()`

```java
CallBackUtils.setFitnessDataCallBack(new FitnessDataCallBack() {
    @Override
    public void onDailyData(DailyDataBean bean) {
        // ① 必须在回调第一行、同步地取出「本次回调来源设备」
        String mac = CallBackUtils.getCallbackDeviceMac();
        if (mac == null) mac = ControlBleTools.getInstance().getCurrentDeviceMac();

        // ② 据此路由到对应设备的数据模型 / UI
        myDeviceRepository.get(mac).onDailyData(bean);
    }
    // onSleepData / onContinuousHeartRateData / ... 同理，第一行都先取 mac
});
```

- `CallBackUtils.getCallbackDeviceMac()` 返回**当前正在触发的回调所属的设备 mac**；不在回调中调用时返回 `null`。
- 即使 A、B 两台设备**同时**拉取日常、各自触发多次 `onDailyData / onSleepData / ...`，
  SDK 会保证每台设备的多次回调都被正确归到对应 mac，不会串台。

> ⚠️ **重要约束**：`getCallbackDeviceMac()` 仅在回调**同步执行期间**有效。
> 若你在 `onXxx` 内把数据 `post` 到别的线程 / `runOnUiThread` 之后再去读 mac，
> 此刻回调已结束，取到的值会是 `null` 或别的设备。
> **务必在回调第一行先捕获 mac，再转发。**

### 4.3 连接状态类回调无需此处理

连接状态回调（如 `BleStateCallBack.onConnectState(String mac, int state)`）
在接口签名里**已经带 mac 参数**，直接用它即可，不必再调用 `getCallbackDeviceMac()`。

### 4.4 推荐的多设备数据回调模式

- **模式 A（简单）**：只注册一个全局 `FitnessDataCallBack`，在回调第一行用 `getCallbackDeviceMac()` 捕获 mac，再分发到 `Map<mac, DeviceModel>`。
- **模式 B（强隔离）**：自己维护 `Map<String, FitnessDataCallBack>` 路由表；在全局回调里根据捕获到的 mac 转发给对应设备的子回调，让各设备 UI 各自订阅自己的子回调。

---

## 附录：常用公开 API 速查

| 能力 | API                                                                                                                  |
| --- |----------------------------------------------------------------------------------------------------------------------|
| 初始化 | `ControlBleTools.getInstance().init(context, bleStateCallBack)`                                                      |
| 注册连接状态回调 | `setBleStateCallBack(BleStateCallBack)`                                                                              |
| 连接（指定协议） | `connect(String name, String address, String deviceProtocol)`                                                        |
| 断开（指定设备） | `disconnect(String mac)` / 无参 `disconnect()`（默认设备）                                                           |
| 是否连接（指定设备） | `isConnect(String mac)`                                                                                              |
| 获取单设备客户端 | `getDeviceClient(String mac)` → `IDeviceClient`（getMac / connect / disconnect / isConnected / run）                 |
| 在某设备上下文执行 | `runForDevice(String mac, DeviceAction)`（`DeviceAction.run(ControlBleTools tools)`，lambda 入参为当前实例）         |
| 读取回调来源设备 | `CallBackUtils.getCallbackDeviceMac()`（数据回调内调用）                                                             |
| 当前设备 mac | `getCurrentDeviceMac()`（优先上下文，否则默认设备；无设备时返回空串）                                                |
| 已连接设备列表 | `getConnectedDevices()` → `List<String>`（mac 列表）                                                                 |
| 最大同时连接数 | `setMaxConnections(int)` / `getMaxConnections()`（默认 5）                                                           |
| 数据回调注册 | `CallBackUtils.setFitnessDataCallBack(...)` 等 `setXxx` 系列                                                         |
| 协议常量 | `BleCommonAttributes.DEVICE_PROTOCOL_APRICOT / BERRY / BERRY_RING / BERRY_TEST`                                      |
| 连接状态常量 | `BleCommonAttributes.STATE_DISCONNECTED / STATE_CONNECTING / STATE_CONNECTED / STATE_DISCONNECTING / STATE_TIME_OUT` |
