package cz.rentflow.pdf

import cz.rentflow.domain.entity.Contract
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
class PdfService {

    private val dateFormat = DateTimeFormatter.ofPattern("d. M. yyyy")

    /**
     * Generate a PDF amendment ("Dodatek ke smlouvě") for an inflation-adjusted rent.
     */
    fun generateInflationAmendment(
        contract: Contract,
        oldRent: BigDecimal,
        newRent: BigDecimal,
        inflationRate: BigDecimal,
        year: Int
    ): ByteArray {
        val doc = PDDocument()
        val page = PDPage(PDRectangle.A4)
        doc.addPage(page)

        val fontBold = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
        val fontRegular = PDType1Font(Standard14Fonts.FontName.HELVETICA)

        val landlord = contract.unit.property.landlord
        val tenant = contract.tenant
        val today = LocalDate.now().format(dateFormat)
        val margin = 50f
        var y = PDRectangle.A4.height - margin

        val content = PDPageContentStream(doc, page)

        fun write(text: String, font: PDType1Font, size: Float, x: Float = margin, newLine: Float = size + 4f) {
            content.beginText()
            content.setFont(font, size)
            content.newLineAtOffset(x, y)
            content.showText(text)
            content.endText()
            y -= newLine
        }

        fun writeLine(label: String, value: String) {
            write(label, fontBold, 10f, margin, 0f)
            write(value, fontRegular, 10f, margin + 150f)
        }

        // Title
        write("DODATEK KE SMLOUVE O NAJMU BYTU", fontBold, 16f, margin + 50f, 22f)
        write("c. ${contract.id} — Inflacni dolozka ($year)", fontRegular, 12f, margin + 80f)
        y -= 10f

        // Parties
        write("Smluvni strany:", fontBold, 12f)
        writeLine("Pronajimatel:", "${landlord.name} (${landlord.email})")
        writeLine("Najemce:", "${tenant.name} (${tenant.email})")
        writeLine("Nemovitost:", "${contract.unit.property.address}, j.c. ${contract.unit.unitNumber}")
        writeLine("Smlouva od:", contract.startDate.format(dateFormat))
        y -= 8f

        // Legal basis
        write("Duvod zmeny:", fontBold, 12f)
        write("Na zaklade inflacni dolozky dle § 2249 obcanskeho zakoniku a v souladu", fontRegular, 10f)
        write("s rocni mirou inflace za rok $year dle CSU ($inflationRate %) se smluvni", fontRegular, 10f)
        write("strany dohodly na uprave vyse najemneho.", fontRegular, 10f)
        y -= 8f

        // Change table
        write("Zmena najemneho:", fontBold, 12f)
        writeLine("Najemne pred upravou:", "$oldRent Kc / mesic")
        writeLine("Mira inflace ($year):", "$inflationRate %")
        writeLine("Najemne po uprave:", "$newRent Kc / mesic")
        writeLine("Ucinnost od:", "1. 1. ${year + 1}")
        y -= 8f

        write("Ostatni ustanoveni smlouvy zustavaji bez zmeny.", fontRegular, 10f)
        write("Tento dodatek nabyvá ucinnosti podpisem obou stran.", fontRegular, 10f)
        y -= 20f

        write("V ________________________ dne $today", fontRegular, 10f)
        y -= 30f

        write(".....................................", fontRegular, 10f, margin)
        write(".....................................", fontRegular, 10f, margin + 260f)
        y -= 5f
        write("Pronajimatol: ${landlord.name}", fontRegular, 9f, margin)
        write("Najemce: ${tenant.name}", fontRegular, 9f, margin + 260f)

        content.close()
        val out = ByteArrayOutputStream()
        doc.save(out)
        doc.close()
        return out.toByteArray()
    }
}
