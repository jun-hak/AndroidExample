package com.wellysis.spatchcardio.ex.lib

import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.ECG_OP
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.ECG_PAUSE_OP
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.ECG_RESTART_OP
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.ECG_START_OP
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.ECG_STOP_OP
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.FETCHING_OP
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.LAST_PACKET_OP
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.POWER_OFF_OP
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.READ_CONFIG_OP
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.RESET_OP
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.SECURE_OP
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.SET_TEST_DURATION_OP
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.WRITE_CONFIG_OP
import com.wellysis.spatchcardio.ex.lib.typealiases.CommandCallback

//typealias cmdCB = ((ans: ByteArray) -> Unit)

class SPatchExSender() {
    private fun unLockWithID(id: Int): ByteArray =
        byteArrayOfInts(SPatchExConstants.UNLOCK_OP) + intToByteArray(id)

    private fun ecgOpWithID(id: Int): ByteArray = byteArrayOfInts(ECG_OP) + intToByteArray(id)
    private val stopPayload: ByteArray =
        byteArrayOfInts(ECG_STOP_OP, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    private val startPayload: ByteArray =
        byteArrayOfInts(ECG_START_OP, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    private val pausePayload: ByteArray =
        byteArrayOfInts(ECG_PAUSE_OP, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    private val restartPayload: ByteArray =
        byteArrayOfInts(ECG_RESTART_OP, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)

    private fun readConfigWithID(id:Int): ByteArray =
        byteArrayOfInts(READ_CONFIG_OP) + intToByteArray(id)

    private fun lastPacketWithID(id: Int): ByteArray =
        byteArrayOfInts(LAST_PACKET_OP) + intToByteArray(id)

    private fun resetWithID(id: Int): ByteArray =
        byteArrayOfInts(RESET_OP) + intToByteArray(id)

    private fun writeConfigWithID(id: Int, os: Int): ByteArray =
        byteArrayOfInts(WRITE_CONFIG_OP) + intToByteArray(id) + shortToByteArray(os.toShort())

    private fun fetchWithID(id: Int): ByteArray =
        byteArrayOfInts(FETCHING_OP) + intToByteArray(id) + byteArrayOfInts(0x01)

    private fun secureWithID(id: Int, secure: Boolean): ByteArray =
        byteArrayOfInts(SECURE_OP) + intToByteArray(id) + if (secure) 0x01 else 0x00

    private fun setDurationWithID(id: Int, duration: Int): ByteArray =
        byteArrayOfInts(SET_TEST_DURATION_OP) + intToByteArray(id) + intToByteArray(duration)

    private fun setPowerOffTimeWithID(id: Int, powerOffTime: Int): ByteArray =
        byteArrayOfInts(POWER_OFF_OP) + intToByteArray(id) + powerOffTime.toByte()

    fun unLock(id: Int, cb: CommandCallback? = null) = EXCmd(unLockWithID(id), cb)
    fun login(id: Int, cb: CommandCallback? = null) = unLock(id, cb)
    fun readConfig(id: Int, cb: CommandCallback? = null) = EXCmd(readConfigWithID(id), cb)
    fun start(id: Int, cb: CommandCallback? = null) = EXCmd(ecgOpWithID(id) + startPayload, cb)
    fun restart(id: Int, cb: CommandCallback? = null) = EXCmd(ecgOpWithID(id) + restartPayload, cb)
    fun stop(id: Int, cb: CommandCallback? = null) = (EXCmd(ecgOpWithID(id) + stopPayload, cb))
    fun pause(id: Int, cb: CommandCallback? = null) = (EXCmd(ecgOpWithID(id) + pausePayload, cb))
    fun reset(id: Int, cb: CommandCallback? = null) = (EXCmd(resetWithID(id), cb))
    fun lastPacket(id: Int, cb: CommandCallback? = null) = (EXCmd(lastPacketWithID(id), cb))
    fun secure(id: Int, secure: Boolean, cb: CommandCallback? = null) = (EXCmd(secureWithID(id, secure), cb))

    fun writeConfig(id: Int, os: Int, cb: CommandCallback? = null) = (EXCmd(writeConfigWithID(id, os), cb))

    fun fetching(id: Int, from: Int, cb: CommandCallback? = null) =
        (EXCmd(fetchWithID(id) + intToByteArray(from), cb))

    fun fetching(id: Int, from: Int, to: Int, cb: CommandCallback? = null) =
        (EXCmd(fetchWithID(id) + intToByteArray(from) + intToByteArray(to), cb))

    fun setDuration(id: Int, duration: Int, cb: CommandCallback? = null) =
        (EXCmd(setDurationWithID(id, duration), cb))

    fun powerOffTime(id: Int, powerOffTime: Int, cb: CommandCallback? = null) =
        (EXCmd(setPowerOffTimeWithID(id, powerOffTime), cb))


    companion object S {
        private fun byteArrayToString(res: ByteArray): String = res.fold("") { acc, byte ->
            acc + String.format("%02X", byte.toInt() and 0xff)
        }

        private fun byteArrayOfInts(vararg ints: Int): ByteArray =
            ByteArray(ints.size) { pos -> ints[pos].toByte() }

        private fun intToByteArray(data: Int): ByteArray = ByteArray(Int.SIZE_BYTES).apply {
            this[0] = (data shr 0).toByte()
            this[1] = (data shr 8).toByte()
            this[2] = (data shr 16).toByte()
            this[3] = (data shr 24).toByte()
        }

        private fun shortToByteArray(data: Short): ByteArray = ByteArray(Short.SIZE_BYTES).apply {
            this[0] = (data.toInt() shr 0).toByte()
            this[1] = (data.toInt() shr 8).toByte()
        }
    }

    class EXCmd(val cmd: ByteArray, val cb: CommandCallback? = null)
}