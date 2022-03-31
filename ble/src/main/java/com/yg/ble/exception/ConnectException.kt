package com.yg.ble.exception


class ConnectException : BleException(
    ERROR_CODE_GATT, "Gatt Exception Occurred! "
) {
}