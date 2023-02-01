package com.wellysis.spatchcardio.ex.lib.helper

import android.util.Log
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.ECG_FIRST_PACKET
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.ECG_LIVE_LAST_PACKET
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.ECG_RECORD_LAST_PACKET
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.MEDICAL_FIRST_PACKET
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.MEDICAL_LIVE_LAST_PACKET
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.MEDICAL_RECORD_LAST_PACKET
import timber.log.Timber

class PacketParseHelper {

    private var _liveData = byteArrayOf()
    private var _recordData = byteArrayOf()

    fun parseLiveData(bytes: ByteArray) {
        when (bytes.size) {
            ECG_FIRST_PACKET, MEDICAL_FIRST_PACKET -> {
                _liveData = bytes
            }
            ECG_LIVE_LAST_PACKET, MEDICAL_LIVE_LAST_PACKET -> {
                if (_liveData.size == ECG_LIVE_LAST_PACKET && bytes.size == ECG_LIVE_LAST_PACKET ||
                    _liveData.size == MEDICAL_FIRST_PACKET && bytes.size == MEDICAL_LIVE_LAST_PACKET
                ) {
                    _liveData += bytes
                }
            }
            else -> {
                Timber.e("Packet size is wrong - ${bytes.size} in live")
            }
        }
    }


    fun parseRecordData(bytes: ByteArray) {
        Timber.d("parseRecordData - ${bytes.size}")
        when (bytes.size) {
            ECG_FIRST_PACKET -> {
                _recordData = bytes
            }
            ECG_RECORD_LAST_PACKET -> {
                if (_recordData.size == ECG_FIRST_PACKET) {
                    _recordData += bytes
                }
            }
            MEDICAL_FIRST_PACKET, MEDICAL_RECORD_LAST_PACKET -> {
                if (checkFirstPacket(bytes)) {
                    _recordData = bytes
                } else {
                    if (_recordData.size == MEDICAL_FIRST_PACKET) {
                        _recordData += bytes
                    }
                }
            }
            else -> {
                Timber.d("Packet size is wrong - ${bytes.size}")
            }
        }
    }

    private fun checkFirstPacket(bytes: ByteArray) =
        bytes[LENGTH] == 0x80.toByte() && bytes[LENGTH + 1] == 0x01.toByte()

    private fun checkSecondPacket(bytes: ByteArray) =
        bytes[bytes.lastIndex - ETX_FROM_BACK] == 0x55.toByte()

    fun getLiveArray(): ByteArray = _liveData
    fun getRecordArray(): ByteArray = _recordData

    fun isReadyLivePacket(): Boolean =
        _liveData.size == ECG_FIRST_PACKET + ECG_LIVE_LAST_PACKET

    fun isReadyRecordPacket(): Boolean =
        _recordData.size == ECG_FIRST_PACKET + ECG_RECORD_LAST_PACKET


    companion object {
        const val LENGTH = 4
        const val ETX_FROM_BACK = 16
    }
}