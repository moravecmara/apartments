package cz.rentflow.utility

import cz.rentflow.domain.entity.RentalUnit
import cz.rentflow.domain.entity.UtilityType
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class UtilityBillingPdfService {

    private val dateFormat = DateTimeFormatter.ofPattern("d. M. yyyy")

    fun generate(
        unit: RentalUnit,
        tenantName: String,
        periodFrom: LocalDate,
        periodTo: LocalDate,
        lineItems: List<UtilityLineItem>,
        totalActualCost: BigDecimal,
        totalDeposits: BigDecimal,
        totalBalance: BigDecimal
    ): ByteArray {
        val doc = PDDocument()
        val page = PDPage(PDRectangle.A4)
        doc.addPage(page)

        val fontBold = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
        val fontReg = PDType1Font(Standard14Fonts.FontName.HELVETICA)
        val margin = 50f
        var y = PDRectangle.A4.height - margin
        val pageW = PDRectangle.A4.width - 2 * margin

        val cs = PDPageContentStream(doc, page)

        fun text(t: String, font: PDType1Font, size: Float, x: Float = margin, gap: Float = size + 5f) {
            cs.beginText(); cs.setFont(font, size); cs.newLineAtOffset(x, y); cs.showText(t); cs.endText()
            y -= gap
        }

        fun row(label: String, value: String, bold: Boolean = false) {
            val vFont = if (bold) fontBold else fontReg
            text(label, fontReg, 10f, margin, 0f)
            text(value, vFont, 10f, margin + 200f)
        }

        fun hline() {
            cs.moveTo(margin, y + 3f); cs.lineTo(margin + pageW, y + 3f); cs.stroke()
            y -= 8f
        }

        // Title
        text("VYUCTOVANI SLUZEB A ENERGII", fontBold, 16f, margin + 60f, 22f)
        text("za obdobi: ${periodFrom.format(dateFormat)} — ${periodTo.format(dateFormat)}", fontReg, 11f, margin + 100f)
        y -= 6f
        hline()

        // Header info
        text("Informace o jednotce:", fontBold, 11f)
        row("Adresa:", unit.property.address)
        row("Jednotka c.:", unit.unitNumber)
        row("Najemce:", tenantName)
        row("Datum vyhotoveni:", LocalDate.now().format(dateFormat))
        y -= 8f
        hline()

        // Table header
        text("Prehled nakladu na energie:", fontBold, 11f)
        y -= 4f

        val cols = listOf(margin, margin + 90f, margin + 160f, margin + 230f, margin + 310f, margin + 390f)
        fun tableRow(t0: String, t1: String, t2: String, t3: String, t4: String, t5: String, hdr: Boolean = false) {
            val f = if (hdr) fontBold else fontReg
            val sz = if (hdr) 9f else 9f
            cs.beginText(); cs.setFont(f, sz); cs.newLineAtOffset(cols[0], y); cs.showText(t0); cs.endText()
            cs.beginText(); cs.setFont(f, sz); cs.newLineAtOffset(cols[1], y); cs.showText(t1); cs.endText()
            cs.beginText(); cs.setFont(f, sz); cs.newLineAtOffset(cols[2], y); cs.showText(t2); cs.endText()
            cs.beginText(); cs.setFont(f, sz); cs.newLineAtOffset(cols[3], y); cs.showText(t3); cs.endText()
            cs.beginText(); cs.setFont(f, sz); cs.newLineAtOffset(cols[4], y); cs.showText(t4); cs.endText()
            cs.beginText(); cs.setFont(f, sz); cs.newLineAtOffset(cols[5], y); cs.showText(t5); cs.endText()
            y -= 14f
        }

        tableRow("Energie", "Pocatec.", "Konec.", "Spotreba", "Nakl. celk.", "Zalohy zap.", hdr = true)
        hline()

        for (item in lineItems) {
            tableRow(
                utilityLabel(item.utilityType),
                item.openingReading?.toPlainString() ?: "—",
                item.closingReading?.toPlainString() ?: "—",
                item.consumption?.let { "${it.toPlainString()} ${utilityUnit(item.utilityType)}" } ?: "—",
                "${item.actualCost} Kc",
                "${item.depositsCollected} Kc"
            )
        }

        hline()

        // Totals
        row("CELKOVE NAKLADY:", "$totalActualCost Kc", bold = true)
        row("CELKOVE ZALOHY:", "$totalDeposits Kc", bold = true)
        y -= 4f
        hline()

        val balanceLabel = if (totalBalance >= BigDecimal.ZERO)
            "DOPLATEK (najemce plati): $totalBalance Kc"
        else
            "PREPLATEK (pronajimatel vraci): ${totalBalance.abs()} Kc"
        text(balanceLabel, fontBold, 12f, margin)
        y -= 8f
        hline()

        // Legal notice
        text("Toto vyuctovani bylo vyhotoveno v souladu se zakonem c. 67/2013 Sb.", fontReg, 8f)
        text("o sluzbach spojených s uzivanim bytu. Najemce ma pravo nahlizet do podkladu.", fontReg, 8f)
        y -= 15f

        // Signatures
        text("V ________________________ dne ${LocalDate.now().format(dateFormat)}", fontReg, 10f)
        y -= 25f
        text(".....................................", fontReg, 10f, margin)
        text(".....................................", fontReg, 10f, margin + 260f, 5f)
        text("Pronajimatel", fontReg, 9f, margin + 30f)
        text("Najemce: $tenantName", fontReg, 9f, margin + 270f)

        cs.close()
        val out = ByteArrayOutputStream()
        doc.save(out)
        doc.close()
        return out.toByteArray()
    }

    private fun utilityLabel(type: UtilityType) = when (type) {
        UtilityType.ELECTRICITY -> "Elektrina"
        UtilityType.GAS -> "Plyn"
        UtilityType.WATER -> "Voda"
    }

    private fun utilityUnit(type: UtilityType) = when (type) {
        UtilityType.ELECTRICITY -> "kWh"
        UtilityType.GAS -> "m3"
        UtilityType.WATER -> "m3"
    }
}
