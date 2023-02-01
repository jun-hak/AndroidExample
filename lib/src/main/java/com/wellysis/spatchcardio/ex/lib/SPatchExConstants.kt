package com.wellysis.spatchcardio.ex.lib

import com.wellysis.spatchcardio.ex.lib.utils.DataUtils.convertFromInteger
import java.util.*

object SPatchExConstants {
    const val UNLOCK_OP = 0x00
    const val LOGIN_OP = UNLOCK_OP
    const val ECG_OP = 0x01
    const val READ_CONFIG_OP = 0x02
    const val ECG_STOP_OP = 0x00
    const val ECG_START_OP = 0x01
    const val ECG_PAUSE_OP = 0x02
    const val ECG_RESTART_OP = 0x03
    const val POWER_OFF_OP = 0x0E
    const val FETCHING_OP = 0x10
    const val RESET_OP = 0x11
    const val LAST_PACKET_OP = 0x12
    const val WRITE_CONFIG_OP = 0x13
    const val SECURE_OP = 0x14
    const val HR_QUEUE_OP = 0x15
    const val SET_BATTERY_CUT_OFF_OP = 0x17
    const val SET_TEST_DURATION_OP = 0x20
    const val INPUT_ALL_DATA_OP = 0x1F
    const val ERROR_LOG_OP = 0xFF

    const val OPERATION_SUCCESS = 0x00.toByte()
    const val OPERATION_FAIL = 0x0B.toByte()

    const val SYMPTOM_BYTE: Byte = 0x80.toByte()
    const val LIVE_MODE_BYTE: Byte = 0x1.toByte()
    const val BOOT_LOAD_BYTE: Byte = 0x20.toByte()
    const val READ_WRITE_FAIL_BYTE: Byte = 0x10.toByte()


    const val SPATCH_EX = "S-Patch EX"

    const val ECG_SIZE = 393
    const val ECG_SAMPLING_RATE = 256
    const val ECG_FIRST_PACKET = 244
    const val ECG_LIVE_LAST_PACKET = 149

    const val ECG_RECORD_LAST_PACKET = 164
    const val MEDICAL_FIRST_PACKET = 204
    const val MEDICAL_LIVE_LAST_PACKET = 189
    const val MEDICAL_RECORD_LAST_PACKET = 204
    const val MTU_PAYLOAD = 3 // ANDROID_FIRST_PACKET + MTU_PAYLOAD 식으로 사용? 이름이 맞나

    val ECG_SERVICE_UUID: UUID = UUID.fromString("66900001-da64-5a97-8c4f-04b8593ff99b")
    val WRITE_SERVICE_UUID: UUID = UUID.fromString("66900002-da64-5a97-8c4f-04b8593ff99b")
    val LIVE_ECG_SERVICE_UUID: UUID = UUID.fromString("66900003-da64-5a97-8c4f-04b8593ff99b")
    val LIVE_ECG2_SERVICE_UUID: UUID = UUID.fromString("66900004-da64-5a97-8c4f-04b8593ff99b")
    val RECORDED_ECG_SERVICE_UUID: UUID = UUID.fromString("66900005-da64-5a97-8c4f-04b8593ff99b")
    val IMU_ECG_SERVICE_UUID: UUID = UUID.fromString("66900006-da64-5a97-8c4f-04b8593ff99b")
    val ERROR_SERVICE_UUID: UUID = UUID.fromString("66900007-da64-5a97-8c4f-04b8593ff99b")

    val DESCRIPTION_UUID = convertFromInteger(0x2902)
    val HEARTRATE_SERVICE_UUID = convertFromInteger(0x180D)
    val HEARTRATE_CHAR_UUID = convertFromInteger(0x2A37)
    val DEVICE_INFO_UUID = convertFromInteger(0x180A)
    val FIRMWARE_CHAR_UUID = convertFromInteger(0x2A26)
    val SOFTWARE_CHAR_UUID = convertFromInteger(0x2A28)
    val HARDWARE_CHAR_UUID = convertFromInteger(0x2A27)
    val BATTERY_SERVICE_UUID = convertFromInteger(0x180F)
    val BATTERY_CHAR_UUID = convertFromInteger(0x2A19)
}