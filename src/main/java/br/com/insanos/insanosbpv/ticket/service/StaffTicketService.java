package br.com.insanos.insanosbpv.ticket.service;

import br.com.insanos.insanosbpv.ticket.bean.StaffTicket;
import com.controlid.cidprinter.CidPrinter;

import java.text.SimpleDateFormat;
import java.util.*;


public class StaffTicketService {
    public static void main1(String[] args) throws Exception {
        CidPrinter cidprint = null;
        try {
            cidprint = new CidPrinter("192.168.1.48", 9100);
            cidprint.Iniciar();
            cidprint.Alinhar(CidPrinter.Alinhamento.DIREITA);
            //cidprint.ImprimirLogo(32,32);
            cidprint.ImprimirLogo(34, 34);
            cidprint.AtivarGuilhotina(CidPrinter.TipoCorte.PARCIAL);
        } finally {
            cidprint.Finalizar();
        }

    }

    public static void main(String[] args) throws Exception {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        Date now = gregorianCalendar.getTime();
        gregorianCalendar.add(Calendar.MINUTE, 120);
        Date end = gregorianCalendar.getTime();
        printStaffTicket(StaffTicket.builder()
                .eventName("Bonde Pela Vida 5ª Edição")
                .place("Estacionamento 1")
                .coletName("Napalm")
                .regionName("Guarulhos")
                .divisionName("Divisão Extremo Norte")
                .startAt(now)
                .endAt(end)
                .message("Atribuição:")
                .message("Acompanhar e coordenar os demais staffs em campo, facilitar a " +
                        "comunicação e centralização das informações e estratégias, " +
                        "direcionar o staffs para os postos de rendição e dar baixa nos " +
                        "staffs que concluíram sua jornada")
                .message("")
                .message("Importante:")
                .message("Ao termino de sua jornada apresente-se ao gestor de campo para " +
                        "registro sistemico da conclusão de seu turno e habilitação para " +
                        "sorteio surpresa." +
                        "Caso não seja dada baixa em seu staff seu trabalho correrá risco " +
                        "de ser marcado como deserção")
                .qrCode("https://typebot.co/insanos-checkout?uuid=ae33a141-ce32-4a69-9a05-3868fb453813")
                .build());
    }

    public static void printStaffTicket(StaffTicket staffTicket) throws Exception {
        CidPrinter cidprint = null;
        String reg = staffTicket.getRegionName().replaceAll("REGIONAL", "");
        String div = staffTicket.getDivisionName();

        try {
            //cidprint = new CidPrinter("192.168.1.48", 9100);
            cidprint = new CidPrinter("COM3");
            cidprint.Iniciar();
            cidprint.Alinhar(CidPrinter.Alinhamento.DIREITA);
            SimpleDateFormat sdfDia = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm");

            cidprint.ImprimirLogo(34, 34);
            cidprint.ImprimirFormatado("Gestão Staff - Insanos" + "\n", true, false, true, false, false);
            cidprint.ImprimirFormatado(staffTicket.getEventName() + "\n\n", true, false, true, true, false);

            cidprint.Alinhar(CidPrinter.Alinhamento.CENTRAL);
            cidprint.ImprimirFormatado(staffTicket.getColetName() + "\n", false, true, true, true, false);
            cidprint.ImprimirFormatado(reg + "\n", false, false, reg.length() < 23, true, false);
            cidprint.ImprimirFormatado(div + "\n\n", false, false, div.length() < 23, true, false);
            cidprint.ImprimirFormatado(
                    staffTicket.getPlace()
                            // + "-"
                            // + sdfDia.format(staffTicket.getStartAt())
                            + "\n"
                    , false, true, true, true, false);
            cidprint.ImprimirFormatado(
                    "Dia: "
                            + sdfDia.format(staffTicket.getStartAt())
                            + "\n"
                    , false, false, false, true, false);
            cidprint.ImprimirFormatado(
                    "Turno: "
                            + sdfHora.format(staffTicket.getStartAt())
                            + " até "
                            + sdfHora.format(staffTicket.getEndAt())
                            + "\n\n"
                    , false, true, false, false, false);
            cidprint.Alinhar(CidPrinter.Alinhamento.ESQUERDA);
            for (String msg : staffTicket.getMessages()) {
                for (String ms : fullJustify(msg.replaceAll("\n", "").split("[ ]"), 60)) {
                    if (ms.trim().isEmpty()) continue;
                    cidprint.ImprimirFormatado(ms.trim() + "\n", false, false, msg.length() < 50, msg.length() < 50, msg.length() > 50);
                }
            }
            ;
            cidprint.Alinhar(CidPrinter.Alinhamento.CENTRAL);
            cidprint.ImprimirCodigoQR(staffTicket.getQrCode(), 5, CidPrinter.QRCorrecaoErro.MEDIO_BAIXO, CidPrinter.QRModelo.MICRO);

            System.out.println("Enviando comando para ativar a guilhotina");
            cidprint.AtivarGuilhotina(CidPrinter.TipoCorte.PARCIAL);

        } finally {
            assert cidprint != null;
            cidprint.Finalizar();
        }


    }

    public static List<String> fullJustify(String[] words, int maxWidth) {
        int n = words.length;
        List<String> justifiedText = new ArrayList<>();
        int currLineIndex = 0;
        int nextLineIndex = getNextLineIndex(currLineIndex, maxWidth, words);
        while (currLineIndex < n) {
            StringBuilder line = new StringBuilder();
            for (int i = currLineIndex; i < nextLineIndex; i++) {
                line.append(words[i] + " ");
            }
            currLineIndex = nextLineIndex;
            nextLineIndex = getNextLineIndex(currLineIndex, maxWidth, words);
            justifiedText.add(line.toString());
        }
        for (int i = 0; i < justifiedText.size() - 1; i++) {
            String fullJustifiedLine = getFullJustifiedString(justifiedText.get(i).trim(), maxWidth);
            justifiedText.remove(i);
            justifiedText.add(i, fullJustifiedLine);
        }
        String leftJustifiedLine = getLeftJustifiedLine(justifiedText.get(justifiedText.size() - 1).trim(), maxWidth);
        justifiedText.remove(justifiedText.size() - 1);
        justifiedText.add(leftJustifiedLine);
        return justifiedText;
    }

    public static int getNextLineIndex(int currLineIndex, int maxWidth, String[] words) {
        int n = words.length;
        int width = 0;
        while (currLineIndex < n && width < maxWidth) {
            width += words[currLineIndex++].length() + 1;
        }
        if (width > maxWidth + 1)
            currLineIndex--;
        return currLineIndex;
    }

    public static String getFullJustifiedString(String line, int maxWidth) {
        StringBuilder justifiedLine = new StringBuilder();
        String[] words = line.split(" ");
        int occupiedCharLength = 0;
        for (String word : words) {
            occupiedCharLength += word.length();
        }
        int remainingSpace = maxWidth - occupiedCharLength;
        int spaceForEachWordSeparation = words.length > 1 ? remainingSpace / (words.length - 1) : remainingSpace;
        int extraSpace = remainingSpace - spaceForEachWordSeparation * (words.length - 1);
        for (int j = 0; j < words.length - 1; j++) {
            justifiedLine.append(words[j]);
            for (int i = 0; i < spaceForEachWordSeparation; i++)
                justifiedLine.append(" ");
            if (extraSpace > 0) {
                justifiedLine.append(" ");
                extraSpace--;
            }
        }
        justifiedLine.append(words[words.length - 1]);
        for (int i = 0; i < extraSpace; i++)
            justifiedLine.append(" ");
        return justifiedLine.toString();
    }

    public static String getLeftJustifiedLine(String line, int maxWidth) {
        int lineWidth = line.length();
        StringBuilder justifiedLine = new StringBuilder(line);
        for (int i = 0; i < maxWidth - lineWidth; i++)
            justifiedLine.append(" ");
        return justifiedLine.toString();
    }
}
