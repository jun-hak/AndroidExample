package com.wellysis.spatchcardio.ex.lib.typealiases

typealias CommandCallback = ((ans: ByteArray) -> Unit)

internal typealias Callback <T> = (T) -> Unit

internal typealias EmptyCallback = () -> Unit

internal typealias PermissionReqCallback = (granted: Boolean) -> Unit