package br.com.insanos.insanosbpv.ticket;

import com.controlid.cidprinter.CidPrinter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class StaffTicketService {
    public static void main1(String[] args) throws Exception {
        CidPrinter cidprint = null;
        try {
            cidprint = new CidPrinter("192.168.1.48", 9100);
            cidprint.Iniciar();
            cidprint.Alinhar(CidPrinter.Alinhamento.DIREITA);
            //cidprint.ImprimirLogo(32,32);
            cidprint.ImprimirLogo(34,34);
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
                .message("Acompanhar e coordenar os demais staffs em campo, facilitar a\n" +
                         "comunicação e centralização das informações e estratégias, \n" +
                         "direcionar o staffs para os postos de rendição e dar baixa nos\n" +
                         "staffs que concluíram sua jornada")
                .message("")
                .message("Importante:")
                .message("Ao termino de sua jornada apresente-se ao gestor de campo para\n" +
                         "registro sistemico da conclusão de seu turno e habilitação para\n" +
                         "sorteio surpresa.\n" +
                         "Caso não seja dada baixa em seu staff seu trabalho correrá risco\n" +
                         "de ser marcado como deserção")
                .qrCode("https://typebot.co/insanos-checkout?uuid=ae33a141-ce32-4a69-9a05-3868fb453813")
                .build());
    }

    public static void printStaffTicket(StaffTicket staffTicket) throws Exception {
        CidPrinter cidprint = null;

        try {
            //cidprint = new CidPrinter("192.168.1.48", 9100);
            cidprint = new CidPrinter("COM3");
            cidprint.Iniciar();
            cidprint.Alinhar(CidPrinter.Alinhamento.DIREITA);
            SimpleDateFormat sdfDia = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm");

            cidprint.ImprimirLogo(34,34);
            cidprint.ImprimirFormatado("Gestão Staff - Insanos" + "\n", true, false, true, false, false);
            cidprint.ImprimirFormatado(staffTicket.getEventName() + "\n\n", true, false, true, true, false);

            cidprint.Alinhar(CidPrinter.Alinhamento.CENTRAL);
            cidprint.ImprimirFormatado(staffTicket.getColetName() + "\n", false, false, true, true, false);
            cidprint.ImprimirFormatado(staffTicket.getRegionName() + "\n", false, false, true, true, false);
            cidprint.ImprimirFormatado(staffTicket.getDivisionName() + "\n\n", false, false, true, true, false);
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
                cidprint.ImprimirFormatado(msg + "\n", false, false, msg.length() < 50, msg.length() < 50, msg.length() > 50);
            };
            cidprint.Alinhar(CidPrinter.Alinhamento.DIREITA);
            cidprint.ImprimirCodigoQR(staffTicket.getQrCode(),5, CidPrinter.QRCorrecaoErro.MEDIO_BAIXO, CidPrinter.QRModelo.MICRO);

            System.out.println("Enviando comando para ativar a gilhotina");
            cidprint.AtivarGuilhotina(CidPrinter.TipoCorte.PARCIAL);

        } finally {
            assert cidprint != null;
            cidprint.Finalizar();
        }


    }
}
