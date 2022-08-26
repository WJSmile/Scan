package com.palmpay.scan.bean

enum class ScanCodeType(val type: Int) {
    None(0),
    Aztec(1),
    PARTIAL(2),
    EAN2(3),
    EAN5(4),
    EAN8(5),
    UPCE(6),
    ISBN10(7),
    UPCA(8),
    EAN13(8),
    ISBN13(10),
    COMPOSITE(11),
    I25(12),
    DataBar(13),
    DataBarExpanded(14),
    DataMatrix(15),
    ITF(16),
    Codabar(17),
    CODE39(18),
    PDF417(19),
    QRCODE(20),
    CODE93(21),
    CODE128(22),
    MaxiCode(23),
    MicroQRCode(24)
}