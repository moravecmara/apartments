package cz.rentflow.banking

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.math.BigDecimal

/**
 * Generates payment QR codes in Czech SPD (Short Payment Descriptor) format.
 * Spec: https://qr-platba.cz/pro-vyvojare/specifikace-formatu/
 */
@Service
class QrCodeService {

    /**
     * Generate a QR code PNG as byte array for a payment.
     * @param iban Target bank account in IBAN format
     * @param amount Amount to pay
     * @param variableSymbol Variable symbol (10 digits max)
     * @param message Optional payment message
     */
    fun generatePaymentQrCode(
        iban: String,
        amount: BigDecimal,
        variableSymbol: String,
        message: String? = null
    ): ByteArray {
        val spdString = buildSpdString(iban, amount, variableSymbol, message)
        return generateQrPng(spdString)
    }

    /**
     * Build SPD string per Czech QR payment standard.
     * Format: SPD*1.0*ACC:CZ...*AM:1234.50*CC:CZK*X-VS:1234567890*MSG:text
     */
    fun buildSpdString(
        iban: String,
        amount: BigDecimal,
        variableSymbol: String,
        message: String? = null
    ): String {
        val sb = StringBuilder()
        sb.append("SPD*1.0")
        sb.append("*ACC:$iban")
        sb.append("*AM:${amount.setScale(2)}")
        sb.append("*CC:CZK")
        sb.append("*X-VS:$variableSymbol")
        if (!message.isNullOrBlank()) {
            sb.append("*MSG:${message.take(60)}")
        }
        return sb.toString()
    }

    private fun generateQrPng(content: String, size: Int = 300): ByteArray {
        val hints = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
        val writer = QRCodeWriter()
        val matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val out = ByteArrayOutputStream()
        MatrixToImageWriter.writeToStream(matrix, "PNG", out)
        return out.toByteArray()
    }
}
