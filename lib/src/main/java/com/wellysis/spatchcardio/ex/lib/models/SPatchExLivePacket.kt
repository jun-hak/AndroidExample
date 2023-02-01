package com.wellysis.spatchcardio.ex.lib.models

import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.ECG_FIRST_PACKET
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.ECG_LIVE_LAST_PACKET
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.ECG_SAMPLING_RATE
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.ECG_SIZE
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.LIVE_MODE_BYTE
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.SYMPTOM_BYTE
import com.wellysis.spatchcardio.ex.lib.utils.DataUtils.parseInt
import com.wellysis.spatchcardio.ex.lib.utils.DataUtils.parseShort
import kotlin.experimental.and

open class SPatchExLivePacket {
    var mSeqNum: Int = -1
    var mLength: Short = 0
    var mEcgData: ShortArray = ShortArray(ECG_SAMPLING_RATE)

    //    var crc: Byte = 0
    var crcValidation: Boolean = false
    var etx: Byte = 0
    var mLiveStatus: Byte = 0
    var rawData = byteArrayOf() // This can be record data and live data buffer.

    fun hasLiveSymptom() =
        ((mLiveStatus and SYMPTOM_BYTE) == SYMPTOM_BYTE)

    fun isLiveMode() = ((mLiveStatus and LIVE_MODE_BYTE) == LIVE_MODE_BYTE)

    open fun update(bytes: ByteArray): Int {
        var offSet = 0
        mSeqNum = parseInt(bytes, offSet)
        offSet += Int.SIZE_BYTES

        mLength = parseShort(bytes, offSet)
        offSet += Short.SIZE_BYTES

        val calcCRC = convertToECGData(bytes, offSet)
        offSet += ECG_SIZE

        val crc = bytes[offSet]
        offSet += Byte.SIZE_BYTES
        crcValidation = (crc.toInt() == calcCRC)

        etx = bytes[offSet]
        offSet += Byte.SIZE_BYTES

        mLiveStatus = bytes[offSet]
        offSet += Byte.SIZE_BYTES

        assert(offSet == ECG_FIRST_PACKET + ECG_LIVE_LAST_PACKET)

        rawData = bytes

        return offSet
    }

    private fun convertToECGData(byteArray: ByteArray, offset: Int): Int {
        var j = 0
        var calcCRC = 0
        for (i in offset until offset + ECG_SIZE step 3) {
            mEcgData[j++] =
                (((byteArray[i + 1].toUByte().toInt() and 0x0F) shl 8) or (byteArray[i].toUByte()
                    .toInt() and 0xFF)).toShort()
            mEcgData[j++] =
                (((byteArray[i + 1].toUByte()
                    .toInt() and 0xF0) shr 4) or ((byteArray[i + 2].toUByte()
                    .toInt() and 0xFF) shl 4)).toShort()

            calcCRC = calcCRC xor byteArray[i].toInt()
            calcCRC = calcCRC xor byteArray[i + 1].toInt()
            calcCRC = calcCRC xor byteArray[i + 2].toInt()
        }

        return calcCRC
    }
}