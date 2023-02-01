package com.wellysis.spatchcardio.ex.lib.utils

import java.util.*

object DataUtils {
    fun parseShort(byteArray: ByteArray, offset: Int = 0): Short =
        ((byteArray[offset + 0].toUByte().toInt() or
                byteArray[offset + 1].toUByte().toInt() shl 8)).toShort()

    fun parseInt(byteArray: ByteArray, offset: Int = 0): Int =
        (byteArray[offset + 3].toUByte().toInt() shl 24) or
                (byteArray[offset + 2].toUByte().toInt() shl 16) or
                (byteArray[offset + 1].toUByte().toInt() shl 8) or
                (byteArray[offset + 0].toUByte().toInt())

    //convertor for uuid
    fun convertFromInteger(i: Int): UUID {
        val MSB = 0x0000000000001000L
        val LSB = -0x7fffff7fa064cb05L
        val value = (i and -0x1).toLong()
        return UUID(MSB or (value shl 32), LSB)
    }
}