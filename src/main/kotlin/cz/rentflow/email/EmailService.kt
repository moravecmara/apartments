package cz.rentflow.email

import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class EmailService(private val mailSender: JavaMailSender) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Send inflation amendment PDF to landlord.
     * If mail is not configured, logs the notification instead.
     */
    fun sendInflationAmendment(
        to: String,
        landlordName: String,
        tenantName: String,
        oldRent: BigDecimal,
        newRent: BigDecimal,
        inflationRate: BigDecimal,
        year: Int,
        pdfBytes: ByteArray
    ) {
        runCatching {
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            helper.setTo(to)
            helper.setSubject("RentFlow: Inflační doložka $year — ke schválení")
            helper.setText(buildEmailBody(landlordName, tenantName, oldRent, newRent, inflationRate, year), true)
            helper.addAttachment("dodatek_inflace_${year}.pdf", org.springframework.core.io.ByteArrayResource(pdfBytes))
            mailSender.send(message)
            log.info("Inflation amendment email sent to {}", to)
        }.onFailure {
            log.warn("Email not sent (mail server not configured?): {}", it.message)
            log.info(
                "INFLATION NOTICE [{}]: {} → {} ({} -> {} Kč, +{}%)",
                to, tenantName, oldRent, newRent, inflationRate
            )
        }
    }

    private fun buildEmailBody(
        landlordName: String,
        tenantName: String,
        oldRent: BigDecimal,
        newRent: BigDecimal,
        inflationRate: BigDecimal,
        year: Int
    ) = """
        <html><body>
        <h2>RentFlow — Inflační doložka $year</h2>
        <p>Dobrý den, <b>$landlordName</b>,</p>
        <p>systém RentFlow automaticky zpracoval inflační doložku pro nájemce <b>$tenantName</b>.</p>
        <table border="1" cellpadding="6" style="border-collapse:collapse">
          <tr><td><b>Míra inflace ($year)</b></td><td>$inflationRate %</td></tr>
          <tr><td><b>Původní nájemné</b></td><td>$oldRent Kč/měsíc</td></tr>
          <tr><td><b>Nové nájemné</b></td><td><b>$newRent Kč/měsíc</b></td></tr>
          <tr><td><b>Účinnost od</b></td><td>1. 1. ${year + 1}</td></tr>
        </table>
        <p>V příloze naleznete PDF dodatek ke smlouvě. Prosíme o podpis a zaslání nájemci.</p>
        <p>— RentFlow automatický systém</p>
        </body></html>
    """.trimIndent()
}
