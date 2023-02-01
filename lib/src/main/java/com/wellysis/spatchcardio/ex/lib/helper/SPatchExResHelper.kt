package com.wellysis.spatchcardio.ex.lib.helper

import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.ECG_OP
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.LAST_PACKET_OP
import com.wellysis.spatchcardio.ex.lib.SPatchExConstants.WRITE_CONFIG_OP
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.ALREADY_STOP
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.BP_BUSY
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.BP_DISABLED
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.FAIL_INVALID_CONTENT
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.FAIL_INVALID_ID
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.FAIL_OPERATION
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.FAIL_RESPONSE_VALIDATION
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.FDS_BUSY
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.INVALID_MOBILE_OS
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.LOCKED
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.LOW_BATTERY
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.NAND_BUSY
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.NOT_PAUSED
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.NOT_STARTED
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.NOT_STOPPED
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.SUCCESS_OPERATION
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.TEST_FAIL
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.TIMEWARP
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.UNKNOWN_FAIL_REASON
import com.wellysis.spatchcardio.ex.lib.models.SPatchExRes.Companion.UNLOCKED
import com.wellysis.spatchcardio.ex.lib.utils.DataUtils.parseInt

class SPatchExResHelper {

    companion object {
        private const val RESPONSE_LENGTH = 0x80.toByte()
        private const val LENGTH_INDEX = 0
        private const val OPCODE_INDEX = 1
        private const val OPERATION_RESPONSE_INDEX = 2
        private const val OPERATION_FAIL_REASON_INDEX = 3
        private const val OS_CONFIG_OPERATION_INDEX = 3
        private const val PAUSE_RESPONSE_LENGTH = 7

        fun getSPatchExRes(opcode: Int, response: ByteArray): SPatchExRes =
            if (checkValidation(opcode, response)) {
                parseResponse(opcode, response)
            } else {
                SPatchExRes(
                    FAIL_RESPONSE_VALIDATION,
                    "opcode: $opcode response: ${response.contentToString()}"
                )
            }

        private fun Byte.getSPatchExRes(): Int =
            when (this) {
                0.toByte() -> SUCCESS_OPERATION
                2.toByte() -> FAIL_INVALID_CONTENT
                3.toByte() -> FAIL_OPERATION
                5.toByte() -> FAIL_INVALID_ID
                6.toByte() -> ALREADY_STOP
                7.toByte() -> NOT_STOPPED
                8.toByte() -> NOT_STARTED
                9.toByte() -> NOT_PAUSED
                11.toByte() -> LOCKED
                12.toByte() -> UNLOCKED
                16.toByte() -> TEST_FAIL
                else -> FAIL_RESPONSE_VALIDATION
            }

        private fun Byte.getSPatchExFailReason(): Int =
            when (this) {
                14.toByte() -> BP_DISABLED
                15.toByte() -> LOW_BATTERY
                16.toByte() -> TIMEWARP
                17.toByte() -> BP_BUSY
                18.toByte() -> FDS_BUSY
                19.toByte() -> INVALID_MOBILE_OS
                20.toByte() -> NAND_BUSY
                else -> UNKNOWN_FAIL_REASON
            }

        private fun parseResponse(opcode: Int, response: ByteArray): SPatchExRes {
            if (response[OPCODE_INDEX].getSPatchExRes() == FAIL_OPERATION) {
                return SPatchExRes(
                    FAIL_OPERATION,
                    response[OPERATION_FAIL_REASON_INDEX].getSPatchExFailReason(),
                    response.contentToString()
                )
            }

            // array size가 4 7 특정 바이트면 처리하는 것을 일관성있게 수정
            return when (opcode) {
                ECG_OP -> ecgOperationParser(response)
                LAST_PACKET_OP -> lastPacketOperationParser(response)
                WRITE_CONFIG_OP -> osConfigOperationParser(response)
                else -> SPatchExRes(
                    response[OPERATION_RESPONSE_INDEX].getSPatchExRes(),
                    response.contentToString()
                )
            }
        }

        private fun ecgOperationParser(response: ByteArray): SPatchExRes {
            val spatchResponse = response[OPERATION_RESPONSE_INDEX].getSPatchExRes()
            // pause인 경우에만 pause time을 추출
            if (spatchResponse == SUCCESS_OPERATION && response.size == PAUSE_RESPONSE_LENGTH) {
                val pauseTime =
                    parseInt(
                        response.takeLast(Int.SIZE_BYTES).toByteArray()
                    )
                return SPatchExRes(spatchResponse, pauseTime, response.contentToString())
            }
            return SPatchExRes(spatchResponse, response.contentToString())
        }

        private fun osConfigOperationParser(response: ByteArray): SPatchExRes {
            val spatchResponse = response[OPERATION_RESPONSE_INDEX].getSPatchExRes()
            if (response.size == 4) {
                return SPatchExRes(
                    spatchResponse,
                    response[OS_CONFIG_OPERATION_INDEX].toInt(),
                    response.contentToString()
                )
            }
            return SPatchExRes(spatchResponse, response.contentToString())
        }


        private fun lastPacketOperationParser(response: ByteArray): SPatchExRes {
            val spatchResponse = response[OPERATION_RESPONSE_INDEX].getSPatchExRes()
            if (response.size > 7) {
                val firstPacketNumber = parseInt(
                    response.take(7).takeLast(Int.SIZE_BYTES).toByteArray()
                )

                val lastPacketNumber =
                    parseInt(
                        response.takeLast(Int.SIZE_BYTES).toByteArray()
                    )

                return SPatchExRes(
                    spatchResponse,
                    firstPacketNumber,
                    lastPacketNumber,
                    response.contentToString()
                )
            }
            return SPatchExRes(spatchResponse, response.contentToString())
        }

        private fun checkValidation(opcode: Int, array: ByteArray): Boolean =
            array[LENGTH_INDEX] == RESPONSE_LENGTH &&
                    array[OPCODE_INDEX].toInt() == opcode &&
                    array.size > OPERATION_RESPONSE_INDEX
    }

}