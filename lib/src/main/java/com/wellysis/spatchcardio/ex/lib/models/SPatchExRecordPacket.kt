package com.wellysis.spatchcardio.ex.lib.models

import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.BOOT_LOAD_BYTE
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.ECG_FIRST_PACKET
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.ECG_RECORD_LAST_PACKET
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.ECG_SAMPLING_RATE
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.READ_WRITE_FAIL_BYTE
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.SYMPTOM_BYTE
import com.wellysis.spatchcardio.ex.lib.utils.DataUtils.parseInt
import com.wellysis.spatchcardio.ex.lib.utils.DataUtils.parseShort
import kotlin.experimental.and

class SPatchExRecordPacket : SPatchExLivePacket() {
    var mDuration: Short = 0
    var mHr: Byte = 0
    var mBattery: Short = 0
    var mRecordStatus: Byte = 0
    var mCluck: Int = 0
    var mOffReason: Int = 0
    var mSampleNo: Byte = 0

    fun hasRecordSymptom() =
        ((mRecordStatus and SYMPTOM_BYTE) == SYMPTOM_BYTE)

    fun hasBootLoad() = ((mLiveStatus and BOOT_LOAD_BYTE) == BOOT_LOAD_BYTE)
    fun hasReadWriteFail() =
        ((mRecordStatus and READ_WRITE_FAIL_BYTE) == READ_WRITE_FAIL_BYTE)

    override fun update(bytes: ByteArray): Int {
        var offset = super.update(bytes)
        mDuration = parseShort(bytes, offset)
        offset += Short.SIZE_BYTES

        mHr = bytes[offset]
        offset += Byte.SIZE_BYTES

        mBattery = parseShort(bytes, offset)
        offset += Short.SIZE_BYTES

        mRecordStatus = bytes[offset]
        offset += Byte.SIZE_BYTES

        mCluck = parseInt(bytes, offset)
        offset += Int.SIZE_BYTES

        mOffReason = parseInt(bytes, offset)
        offset += Int.SIZE_BYTES

        mSampleNo = bytes[offset]
        offset += Byte.SIZE_BYTES

        assert(offset == ECG_FIRST_PACKET + ECG_RECORD_LAST_PACKET)

        return offset
    }

    companion object {
        // zero padding data
        fun makeDummyData(): ShortArray =
            ShortArray(ECG_SAMPLING_RATE).apply { fill(0) }
    }
}