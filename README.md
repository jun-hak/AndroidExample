# SPATCH_EX_SDK_DOC


S-Patch Ex is the next generation's wearable electrocardiogram (ECG) monitoring device. For the benefit of developers, makers,
and advanced users, we are sharing the Bluetooth Low Energy (BLE) protocol that S-Patch Ex uses to communicate with our app.
Using this, you can communicate with S-Patch Ex using any hardware that supports BLE using the language of your choice!


## Imports/Usage

```kotlin
// please make the object responseInterface 
// scanTime unit: milliseconds
BLE(context: Context, responseInterface: ExResponseInterface, scanTime: ExMode)
ex) val ble = BLE(context, responseInterface, ExMode.ANDROID);
```


## API Reference
- Before we start, we need an id to send commands to Spatch. The id is used as a security key, and the command cannot be used if the id does not match before. The id will be initialized when the Spatch is reset (The data also resetted).
- The id is 4byte integer and it must not be 0.
- Two state transitions are required to use Spatch ( Lock -> Unlock -> Login ). For using the Spatch, you have to make the Spatch Login state.
- If you make the Spatch login state, you can send the command without login. However, if you are disconnected with S-Patch, then you have to re-login.


#### ConnectState
- get or subscribe connectState from SPatch BLE Object
```kotlin  
  public final val connectState: StateFlow<SpatchConnectState>
```


#### Connect
- connect to Spatch EX
```kotlin
  suspend fun connect(serialNumber: String, scanTimeMilliSeconds: Long): Boolean
```


#### unlock
- set the Spatch unlock when the Spatch is locked
```kotlin
  suspend fun unlock(id: Int): SpatchResponse
```


#### login
- set the Spatch login when the Spatch is unlocked
```kotlin
  suspend fun login(id: Int): SpatchResponse
```


#### osInput
- set the Android os to the Spatch
```kotlin
  suspend fun osInput(id: Int): SpatchResponse
```


#### start
- set the Spatch starts to measure signal
```kotlin
  suspend fun start(id: Int): SpatchResponse
```


#### restart
- set the Spatch which is paused restarts to measure signal 
```kotlin
  suspend fun restart(id: Int): SpatchResponse
```


#### stop
- set the Spatch stops to measure signal
```kotlin
  suspend fun stop(id: Int): SpatchResponse
```


#### pause
- set the Spatch pauses to measure signal
```kotlin
  suspend fun pause(id: Int): SpatchResponse
```


#### getLastpacketNumber
- get the sequence number of the last data stored in the spatch memory
```kotlin
  suspend fun getLastpacketNumber(id: Int): SpatchResponse
```


#### fetching
- request data to Spatch
  - fetching(id, 5) // request the data which has sequence number is 5
  - fetching(id, 5, 10) // request the data from sequence numbers 5 to 10
```kotlin
  suspend fun fetching(id: Int, start: Int, end: Int?): SpatchResponse
```


#### setDuration
- set the Duration hour
  - setDuration(id, 1) // When the spatch's duration is set to 1 hour, only sequence numbers up to 60000 are stored.
```kotlin
  suspend fun setDuration(id: Int, duration: Int): SpatchResponse
```


#### reset
- reset the Spatch Memory
- id is also initialized
```kotlin
  suspend fun reset(id: Int): SpatchResponse
```


#### getHardwareRevision
- get hardware revision
```kotlin
  fun getHardwareRevision(): String?
```


#### getFirmwareRevision
- get firmware revision
```kotlin
  fun getFirmwareRevision(): String?
```


#### getSoftwareRevision
- get software revision
```kotlin
  fun getSoftwareRevision(): String?
```


#### disconnect
- clean the gatt instance
```kotlin
  fun disconnect(): Unit
```


#### SpatchResponse 
```java
class SpatchResponse {
    public static int FAIL_RESPONSE_VALIDATION = -1; // The response's opcode is not correct
    public static int SUCCESS_OPERATION = 0; // operation success
    public static int FAIL_OPERATION = 1; // operation fail
    public static int FAIL_INVALID_CONTENT = 2; // The parameter is not invalid
    public static int FAIL_INVALID_ID = 3; // The id is not invalid
    public static int ALREADY_STOP = 4; // The Spatch is already stopped
    public static int NOT_STOPPED = 5; // The Spatch is not stopped (When you send reset command to Spatch which measures data, you can get the response)
    public static int NOT_STARTED = 6;
    public static int NOT_PAUSED = 7;
    public static int LOCKED = 8; // The Spatch is lock state.
    public static int UNLOCKED = 9; //  The Spatch is unlock state.
    public static int INVALID_FETCHING_RANGE = 13; // The Fetching Range is not correct


    // only data2
    public static int BP_DISABLED = 10; // The Spatch' BioProcessor has some problem. 
    public static int LOW_BATTERY = 11; // The battery is low
    public static int TEST_FAIL = 12;
    public static int TIMEWARP = 100;
    public static int BP_BUSY = 101;
    public static int FDS_BUSY = 102;
    public static int INVALID_MOBILE_OS = 103;
    public static int NAND_BUSY = 102;

    public static int TIME_OUT = 1000;
    public static int WEAK_CONNECTION = 1001;

    // cmd == null or
    public static int FAIL_OPERATION_WRITE = 1002;
    public static int FAIL_RESPONSE = 1003;
    public static int UNKNOWN_FAIL_REASON = 1004;
}
```


## Events



#### ExResponseInterface
```kotlin
  interface ExResponseInterface {
    fun sendBatteryData(battery: Int) // 0 ~ 100
    fun sendHeartRate(heartRate: Int) // 0 ~ 255
    fun sendExLiveData(packet: ExLivePacket)
    fun sendExRecordData(packet: ExRecordPacket)
  }
```


#### ExLivePacket
```kotlin
  // Sampling rate: 256hz
  // every 1 second, you can get ExLivePacket when you send start command to measure
  class ExLivePacket {
    var seqNum: Int // sequence number, it is started from 0.
    var ecgData: ShortArray // ECG data, 256 short array
    var crcValidation: Boolean // crc validation, if it is false, the data is corrupted.
    fun hasLiveSymptom(): Boolean // When you click the button on the Spatch, you can get the symptom true.
    fun isLiveMode(): Boolean // Fail to write the data in the Spatch memory. 
  }
```


#### SpatchConnectState
```kotlin
  enum class SpatchConnectState { STATE_DISCONNECTED, STATE_CONNECTING, STATE_CONNECTED }
```

#### ExRecordPacket
```kotlin
  class ExRecordPacket: ExLivePacket {
    fun hasRecordSymptom(): Boolean
    fun hasBootLoad(): Boolean // When the Spatch turns off and on, the method returns true
    fun hasReadWriteFail(): Boolean // Read or Write Fail in/to Spatch memory
  }
```

## Well-known bug
- It does not support secure Mode
- When you fetching the data, the Spatch sometimes fail to fetch data which has sequence number 0


## What we are discussing
- Manage Device State(lock, unlock, login) in SDK, because It makes the SDk much easier to use.
