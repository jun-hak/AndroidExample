package com.wellysis.spatchcardio.ex.lib.models

class SPatchExRes {
    var state: Int?
    var data1: Any? = null
    var data2: Any? = null
    var raw: String? = ""

    constructor(state: Int, strRaw: String) {
        this.state = state
        raw = strRaw
    }

    constructor(state: Int, data1: Any?, strRaw: String) {
        this.state = state
        this.data1 = data1
        raw = strRaw
    }

    constructor(state: Int, data1: Any?, data2: Any?, strRaw: String) {
        this.state = state
        this.data1 = data1
        this.data2 = data2
        raw = strRaw
    }

    companion object {
        var FAIL_RESPONSE_VALIDATION = -1

        // operation success
        var SUCCESS_OPERATION = 0

        // operation fail
        var FAIL_OPERATION = 1
        var FAIL_INVALID_CONTENT = 2
        var FAIL_INVALID_ID = 3
        var ALREADY_STOP = 4
        var NOT_STOPPED = 5
        var NOT_STARTED = 6
        var NOT_PAUSED = 7
        var LOCKED = 8
        var UNLOCKED = 9
        var INVALID_FETCHING_RANGE = 13

        // only data2
        var BP_DISABLED = 10
        var LOW_BATTERY = 11
        var TEST_FAIL = 12
        var TIMEWARP = 100
        var BP_BUSY = 101
        var FDS_BUSY = 102
        var INVALID_MOBILE_OS = 103
        var NAND_BUSY = 102
        var TIME_OUT = 1000
        var WEAK_CONNECTION = 1001

        // cmd == null or
        var FAIL_OPERATION_WRITE = 1002
        var FAIL_RESPONSE = 1003
        var UNKNOWN_FAIL_REASON = 1004
        var NOT_CONNECTED = 1005
    }
}