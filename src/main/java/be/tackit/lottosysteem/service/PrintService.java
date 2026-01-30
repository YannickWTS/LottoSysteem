package be.tackit.lottosysteem.service;

import be.tackit.lottosysteem.model.Bestelling;
import org.springframework.stereotype.Service;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import java.time.format.DateTimeFormatter;

@Service
public class PrintService {

    private static final String PRINTER_NAME = "LottoPrinter";

    public void printBestelling(Bestelling bestelling) {
        javax.print.PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        javax.print.PrintService myPrinter = null;

        // 1. Zoek de printer
        for (javax.print.PrintService printer : printServices) {
            if (printer.getName().equalsIgnoreCase(PRINTER_NAME)) {
                myPrinter = printer;
                break;
            }
        }

        if (myPrinter == null) {
            System.err.println("Printer '" + PRINTER_NAME + "' niet gevonden!");
            return; // Of gooi een exception / log een warning
        }

        // 2. Maak de bon layout
        String bonTekst = genereerBonTekst(bestelling);

        // 3. Printen
        try {
            DocPrintJob job = myPrinter.createPrintJob();

            // Zet String om naar bytes
            byte[] textBytes = bonTekst.getBytes();

            // Star CUT command: ESC d 2 (Hex: 1B 64 02)
            // 'd' = 100 decimal
            // 2 = Partial cut (of 3 for full cut)
            byte[] cutBytes = new byte[] { 0x1B, 0x64, 0x02 }; // ESC d 2

            // Combineer text + cut
            byte[] combined = new byte[textBytes.length + cutBytes.length];
            System.arraycopy(textBytes, 0, combined, 0, textBytes.length);
            System.arraycopy(cutBytes, 0, combined, textBytes.length, cutBytes.length);

            Doc doc = new SimpleDoc(combined, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);

            PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
            aset.add(new Copies(1));

            job.print(doc, aset);
            System.out.println("Bon afgedrukt naar " + PRINTER_NAME);

        } catch (PrintException e) {
            System.err.println("Fout tijdens printen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static final int PRINTER_WIDTH = 48; // 72mm is approx 48 chars (Font A)
    private static final int CONTENT_WIDTH = 44; // Width of the inner content block (Widened to 44)

    private String genereerBonTekst(Bestelling b) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        StringBuilder sb = new StringBuilder();

        // 1. Header (Centered)
        sb.append(centerText("--------------------------------------------")).append("\n"); // 44 dashes centered
        sb.append(centerText("Betaalbewijs Groepsspel")).append("\n");
        sb.append(centerText("--------------------------------------------")).append("\n\n");

        // 2. Body (Left aligned but centered as a block)
        // We want the block to look centered, so we pad the left side.
        // Left margin = (Total - Content) / 2
        String margin = " ".repeat((PRINTER_WIDTH - CONTENT_WIDTH) / 2);

        sb.append(centerText(b.getDatumRegistratie().format(dtf))).append("\n\n");

        // Use formatLine to justify (Key Left ..... Value Right)
        sb.append(margin).append(formatLine("Klant:", b.getKlant().getNaam())).append("\n");
        sb.append(margin).append(formatLine("Speltype:", b.getSpelType())).append("\n");
        sb.append(margin).append(formatLine("Maand:", b.getMaand())).append("\n");

        // Prijs bepalen
        String prijs = b.getSpelType().toLowerCase().contains("extra") ? "EUR 20" : "EUR 10";
        sb.append(margin).append(formatLine("Bedrag:", prijs + " betaald")).append("\n\n");

        // 3. Footer (Centered)
        sb.append(centerText("--------------------------------------------")).append("\n");
        sb.append(centerText("Bedankt voor uw deelname!")).append("\n");
        sb.append(centerText("--------------------------------------------")).append("\n");

        sb.append(centerText("PRESS SHOP ZELE 2374")).append("\n");
        sb.append(centerText("Kouterstraat 98A")).append("\n");
        sb.append(centerText("9240 Zele")).append("\n");
        sb.append(centerText("Tel. 052/44.51.99")).append("\n");

        sb.append("\n\n\n\n"); // Extra witregels voor 'cut'

        return sb.toString();
    }

    // Centres text within PRINTER_WIDTH
    private String centerText(String text) {
        if (text.length() >= PRINTER_WIDTH)
            return text;
        int padding = (PRINTER_WIDTH - text.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < padding; i++) {
            sb.append(" ");
        }
        sb.append(text);
        return sb.toString();
    }

    // Formats a line as "Key (spaces) Value" filling CONTENT_WIDTH
    private String formatLine(String key, String value) {
        int contentWidth = CONTENT_WIDTH;
        int spaceNeeded = contentWidth - key.length() - value.length();
        if (spaceNeeded < 1)
            spaceNeeded = 1; // Always at least 1 space

        StringBuilder sb = new StringBuilder();
        sb.append(key);
        for (int i = 0; i < spaceNeeded; i++) {
            sb.append(" ");
        }
        sb.append(value);
        return sb.toString();
    }
}
