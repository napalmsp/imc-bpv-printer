package com.controlid.cidprinter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import jssc.SerialPort;
import jssc.SerialPortException;
import lombok.Getter;

public class CidPrinter {
    String IP;
    int Port;
    Socket socket;
    SerialPort serialPort;
    String serialPortString;
    OutputStream output;
    InputStream input;
    OperationType operationType;

    public CidPrinter(String ip, int port) {
        this.IP = ip;
        this.Port = port;
        this.operationType = CidPrinter.OperationType.TCP_SOCKET;
    }

    public CidPrinter(String serialPort) {
        this.serialPortString = serialPort;
        this.operationType = CidPrinter.OperationType.USB_SERIAL;
    }

    public void Iniciar() throws Exception {
        if (this.operationType == CidPrinter.OperationType.TCP_SOCKET) {
            this.socket = new Socket(this.IP, this.Port);
            this.output = this.socket.getOutputStream();
            this.input = this.socket.getInputStream();
        } else if (this.operationType == CidPrinter.OperationType.USB_SERIAL) {
            this.serialPort = new SerialPort(this.serialPortString);
            this.serialPort.openPort();
            this.serialPort.setParams(115200, 8, 1, 0);
            this.output = new SerialPortOutputStream(this.serialPort);
            this.input = new SerialPortInputStream(this.serialPort);
        }

    }

    public void SendDataToPrinter(byte[] text) throws Exception {
        this.output.write(text);
    }

    public byte[] ReadDataFromPrinter(byte[] text, int var2) throws Exception {
        this.output.write(text);
        Object var3 = null;
        byte[] var6;
        if (var2 == 0) {
            byte[] var4 = new byte[1024];
            int var5 = this.input.read(var4, 0, var4.length);
            if (var5 < 0) {
                throw new Exception("Não foi possível ler " + var4.length + " da impressora");
            }

            var6 = new byte[var5 - 1];
            System.arraycopy(var4, 0, var6, 0, var5 - 1);
        } else {
            var6 = new byte[var2];
            int var7 = this.input.read(var6, 0, var2);
            if (var7 != var2) {
                throw new Exception("Não foi possível ler " + var2 + " da impressora");
            }
        }

        return var6;
    }

    public String GetFirmwareVersion() throws Exception {
        byte[] var1 = new byte[]{29, 73, 65};
        byte[] var2 = this.ReadDataFromPrinter(var1, 0);
        return this.GetStringFromData(var2);
    }

    public String GetManufacturersName() throws Exception {
        byte[] var1 = new byte[]{29, 73, 66};
        byte[] var2 = this.ReadDataFromPrinter(var1, 0);
        return this.GetStringFromData(var2);
    }

    public String GetModelName() throws Exception {
        byte[] var1 = new byte[]{29, 73, 67};
        byte[] var2 = this.ReadDataFromPrinter(var1, 0);
        return this.GetStringFromData(var2);
    }

    public String GetSerialNumber() throws Exception {
        byte[] var1 = new byte[]{29, 73, 68};
        byte[] var2 = this.ReadDataFromPrinter(var1, 0);
        return this.GetStringFromData(var2);
    }

    private String GetStringFromData(byte[] var1) throws Exception {
        String var2 = new String(var1);
        return var2.substring(1);
    }

    public void SendStringToPrinter(String var1) throws Exception {
        String var2 = Normalizer.normalize(var1, Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        byte[] var3 = var2.getBytes();
        this.SendDataToPrinter(var3);
    }

    public void ImprimirFormatado(String texto, boolean italic, boolean underline, boolean big, boolean bold, boolean small) throws Exception {
        StringBuilder command = new StringBuilder();
        command.append('\u001b');
        command.append('!');
        int style = (small ? 1 : 0) << 0 | (italic ? 1 : 0) << 6 | (big ? 1 : 0) << 5 | (bold ? 1 : 0) << 3;
        command.append((char)style);
        if (underline) {
            command.append('\u001b');
            command.append('-');
            command.append('\u0001');
        }
        command.append(texto);
        this.SendStringToPrinter(command.toString());
    }

    public void ImprimirTeste() throws Exception {
        StringBuilder command = new StringBuilder();
        command.append('\u001d');
        command.append('(');
        command.append('A');
        command.append('\u0002');
        command.append('\u0000');
        command.append('\u0001');
        command.append('\u0001');
        this.SendStringToPrinter(command.toString());
        Thread.sleep(4000L);
    }

    public void ConfigurarCodigoDeBarras(int altura, int largura, PosicaoCaracteresBarras posicaoCaracteresBarras) throws Exception {
        StringBuilder command = new StringBuilder();
        command.append('\u001d');
        command.append('w');
        command.append((char)largura);
        command.append('\u001d');
        command.append('h');
        command.append((char)altura);
        command.append('\u001d');
        command.append('H');
        command.append((char)this.GetPosicaoCaracteresBarras(posicaoCaracteresBarras));
        this.SendStringToPrinter(command.toString());
    }

    public void ImprimirCodigoDeBarras(String texto, TipoCodigoBarras tipoCodigoBarras) throws Exception {
        StringBuilder var3 = new StringBuilder();
        var3.append('\u001d');
        var3.append('k');
        var3.append((char)this.GetTipoCodigoBarras(tipoCodigoBarras));
        if (this.GetTipoCodigoBarras(tipoCodigoBarras) <= this.GetTipoCodigoBarras(CidPrinter.TipoCodigoBarras.CODABAR)) {
            var3.append(texto);
            var3.append('\u0000');
        } else {
            byte[] var4 = texto.getBytes();
            var3.append((char)var4.length);
            var3.append(texto);
        }

        this.SendStringToPrinter(var3.toString());
    }

    public void ImprimirCodigoQR(String texto, int tamanho, QRCorrecaoErro qrCorrecaoErro, QRModelo qrModelo) throws Exception {
        StringBuilder printerCommand = new StringBuilder();
        printerCommand.append('\u001d');
        printerCommand.append('(');
        printerCommand.append('k');
        printerCommand.append('\u0004');
        printerCommand.append('\u0000');
        printerCommand.append('1');
        printerCommand.append('A');
        printerCommand.append((char)this.GetQRModelo(qrModelo));
        printerCommand.append('\u0000');
        printerCommand.append('\u001d');
        printerCommand.append('(');
        printerCommand.append('k');
        printerCommand.append('\u0003');
        printerCommand.append('\u0000');
        printerCommand.append('1');
        printerCommand.append('C');
        printerCommand.append((char)tamanho);
        printerCommand.append('\u001d');
        printerCommand.append('(');
        printerCommand.append('k');
        printerCommand.append('\u0003');
        printerCommand.append('\u0000');
        printerCommand.append('1');
        printerCommand.append('E');
        printerCommand.append((char)this.GetQRCorrecaoErro(qrCorrecaoErro));
        int var6 = texto.getBytes().length + 3;
        printerCommand.append('\u001d');
        printerCommand.append('(');
        printerCommand.append('k');
        this.SendStringToPrinter(printerCommand.toString());
        byte[] var7 = new byte[]{(byte)(var6 & 255), (byte)((var6 & '\uff00') >> 8)};
        this.SendDataToPrinter(var7);
        printerCommand = new StringBuilder();
        printerCommand.append('1');
        printerCommand.append('P');
        printerCommand.append('0');
        printerCommand.append(texto);
        printerCommand.append('\u001d');
        printerCommand.append('(');
        printerCommand.append('k');
        printerCommand.append('\u0003');
        printerCommand.append('\u0000');
        printerCommand.append('1');
        printerCommand.append('Q');
        printerCommand.append('0');
        this.SendStringToPrinter(printerCommand.toString());
    }

    public void ImprimirCodigoQR(String texto) throws Exception {
        this.ImprimirCodigoQR(texto, 8, CidPrinter.QRCorrecaoErro.BAIXO, CidPrinter.QRModelo.MICRO);
    }

    public void AtivarGuilhotina(TipoCorte tipoCorte) throws Exception {
        StringBuilder var2 = new StringBuilder();
        var2.append('\u001d');
        var2.append('V');
        var2.append(this.GetTipoCorte(tipoCorte));
        this.SendStringToPrinter(var2.toString());
    }

    public void AbrirGaveta(PinoGaveta pinoGaveta, int var2, int var3) throws Exception {
        StringBuilder command = new StringBuilder();
        command.append('\u001b');
        command.append('p');
        command.append((char)this.GetPinoGaveta(pinoGaveta));
        command.append((char)var2);
        if (var3 < var2) {
            command.append((char)var2);
        } else {
            command.append((char)var3);
        }

        this.SendStringToPrinter(command.toString());
    }

    public void AbrirGaveta() throws Exception {
        this.AbrirGaveta(CidPrinter.PinoGaveta.PINO2_RJ12, 200, 250);
    }

    public void ImprimirLogo(int key, int code) throws Exception {
        byte[] var3 = new byte[]{29, 40, 76, 6, 0, 48, 69, (byte)key, (byte)code, 1, 1};
        this.SendDataToPrinter(var3);
    }

    public void Alinhar(Alinhamento alinhamento) throws Exception {
        byte[] var2 = new byte[]{27, 97, (byte)this.GetAlinhamento(alinhamento)};
        this.SendDataToPrinter(var2);
    }

    public StatusImpressora LerStatus() throws Exception {
        byte[] var1 = new byte[]{16, 4, 1};
        byte[] var2 = this.ReadDataFromPrinter(var1, 1);
        boolean var3 = (var2[0] & 8) == 0;
        byte[] var4 = new byte[]{16, 4, 2};
        byte[] var5 = this.ReadDataFromPrinter(var4, 1);
        boolean var6 = (var5[0] & 64) != 0;
        boolean var7 = (var5[0] & 4) != 0;
        boolean var8 = this.LerStatusPapel() == CidPrinter.StatusPapel.SEM_PAPEL;
        return new StatusImpressora(var3, var6, var8, var7);
    }

    public StatusGaveta LerStatusGaveta() throws Exception {
        byte[] var1 = new byte[]{16, 4, 1};
        byte[] var2 = this.ReadDataFromPrinter(var1, 1);
        byte var3 = var2[0];
        StatusGaveta var4 = (var3 & 4) == 0 ? CidPrinter.StatusGaveta.FECHADA : CidPrinter.StatusGaveta.ABERTA;
        return var4;
    }

    public StatusPapel LerStatusPapel() throws Exception {
        byte[] var1 = new byte[]{29, 114, 1};
        byte[] var2 = this.ReadDataFromPrinter(var1, 1);
        byte var3 = var2[0];
        StatusPapel var4 = CidPrinter.StatusPapel.SEM_PAPEL;
        if ((var3 & 12) == 12) {
            var4 = CidPrinter.StatusPapel.SEM_PAPEL;
        } else if ((var3 & 12) == 0 && (var3 & 3) == 3) {
            var4 = CidPrinter.StatusPapel.POUCO_PAPEL;
        } else {
            var4 = CidPrinter.StatusPapel.PAPEL_PRESENTE;
        }

        return var4;
    }

    public void Finalizar() throws Exception {
        if (this.operationType == CidPrinter.OperationType.TCP_SOCKET) {
            if (this.input != null) {
                this.input.close();
            }

            if (this.output != null) {
                this.output.close();
            }

            if (this.socket != null) {
                this.socket.close();
            }
        } else if (this.operationType == CidPrinter.OperationType.USB_SERIAL) {
            if (this.input != null) {
                this.input.close();
            }

            if (this.output != null) {
                this.output.close();
            }

            if (this.serialPort != null) {
                this.serialPort.closePort();
            }
        }

    }

    private int GetPosicaoCaracteresBarras(PosicaoCaracteresBarras posicao) {
        return posicao.getValue();
        /*
        if (posicao == CidPrinter.PosicaoCaracteresBarras.SEM_CARACTERES) {
            return 0;
        } else {
            return posicao == CidPrinter.PosicaoCaracteresBarras.CARACTERES_ABAIXO ? 2 : 0;
        }
         */
    }

    private int GetTipoCodigoBarras(TipoCodigoBarras tipoCodigoBarras) {
        if (tipoCodigoBarras == CidPrinter.TipoCodigoBarras.UPC_A) {
            return 0;
        } else if (tipoCodigoBarras == CidPrinter.TipoCodigoBarras.EAN13) {
            return 2;
        } else if (tipoCodigoBarras == CidPrinter.TipoCodigoBarras.EAN8) {
            return 3;
        } else if (tipoCodigoBarras == CidPrinter.TipoCodigoBarras.CODE39) {
            return 4;
        } else if (tipoCodigoBarras == CidPrinter.TipoCodigoBarras.ITF) {
            return 5;
        } else if (tipoCodigoBarras == CidPrinter.TipoCodigoBarras.CODABAR) {
            return 6;
        } else if (tipoCodigoBarras == CidPrinter.TipoCodigoBarras.CODE93) {
            return 72;
        } else {
            return tipoCodigoBarras == CidPrinter.TipoCodigoBarras.CODE128 ? 73 : 0;
        }
    }

    private int GetTipoCorte(TipoCorte tipoCorte) {
        if (tipoCorte == CidPrinter.TipoCorte.TOTAL) {
            return 0;
        } else {
            return tipoCorte == CidPrinter.TipoCorte.PARCIAL ? 1 : 0;
        }
    }

    private int GetQRCorrecaoErro(QRCorrecaoErro qrCorrecaoErro) {
        if (qrCorrecaoErro == CidPrinter.QRCorrecaoErro.BAIXO) {
            return 48;
        } else if (qrCorrecaoErro == CidPrinter.QRCorrecaoErro.MEDIO_BAIXO) {
            return 49;
        } else if (qrCorrecaoErro == CidPrinter.QRCorrecaoErro.MEDIO_ALTO) {
            return 50;
        } else {
            return qrCorrecaoErro == CidPrinter.QRCorrecaoErro.ALTO ? 51 : 48;
        }
    }

    private int GetQRModelo(QRModelo var1) {
        if (var1 == CidPrinter.QRModelo.MODELO_1) {
            return 49;
        } else if (var1 == CidPrinter.QRModelo.MODELO_2) {
            return 50;
        } else {
            return var1 == CidPrinter.QRModelo.MICRO ? 51 : 49;
        }
    }

    private int GetAlinhamento(Alinhamento alinhamento) {
        if (alinhamento == CidPrinter.Alinhamento.ESQUERDA) {
            return 0;
        } else if (alinhamento == CidPrinter.Alinhamento.CENTRAL) {
            return 1;
        } else {
            return alinhamento == CidPrinter.Alinhamento.DIREITA ? 2 : 0;
        }
    }

    private int GetPinoGaveta(PinoGaveta pinoGaveta) {
        if (pinoGaveta == CidPrinter.PinoGaveta.PINO2_RJ12) {
            return 0;
        } else {
            return pinoGaveta == CidPrinter.PinoGaveta.PINO5_RJ12 ? 1 : 0;
        }
    }

    private static enum OperationType {
        USB_SERIAL,
        TCP_SOCKET;

        private OperationType() {
        }
    }

    @Getter
    public static enum PosicaoCaracteresBarras {
        SEM_CARACTERES(0),
        CARACTERES_ACIMA(1),
        CARACTERES_ABAIXO(2);

        private int value = 0;
        private PosicaoCaracteresBarras(int i) {
            this.value = i;
        }
    }

    public static enum TipoCodigoBarras {
        UPC_A,
        EAN13,
        EAN8,
        CODE39,
        ITF,
        CODABAR,
        CODE93,
        CODE128;

        private TipoCodigoBarras() {
        }
    }

    public static enum TipoCorte {
        TOTAL,
        PARCIAL;

        private TipoCorte() {
        }
    }

    public static enum QRCorrecaoErro {
        BAIXO,
        MEDIO_BAIXO,
        MEDIO_ALTO,
        ALTO;

        private QRCorrecaoErro() {
        }
    }

    public static enum QRModelo {
        MODELO_1,
        MODELO_2,
        MICRO;

        private QRModelo() {
        }
    }

    public static enum Alinhamento {
        ESQUERDA,
        CENTRAL,
        DIREITA;

        private Alinhamento() {
        }
    }

    public static enum PinoGaveta {
        PINO2_RJ12,
        PINO5_RJ12;

        private PinoGaveta() {
        }
    }

    public static enum StatusGaveta {
        FECHADA,
        ABERTA;

        private StatusGaveta() {
        }
    }

    public static enum StatusPapel {
        SEM_PAPEL,
        POUCO_PAPEL,
        PAPEL_PRESENTE;

        private StatusPapel() {
        }
    }

    private class SerialPortInputStream extends InputStream {
        private SerialPort serialPort;

        public SerialPortInputStream(SerialPort var2) throws SerialPortException {
            this.serialPort = var2;
        }

        public int read() throws IOException {
            throw new IOException("Read is not supported over USB");
        }

        public int read(byte[] var1) throws IOException {
            throw new IOException("Read is not supported over USB");
        }

        public int read(byte[] var1, int var2, int var3) throws IOException {
            throw new IOException("Read is not supported over USB");
        }
    }

    private class SerialPortOutputStream extends OutputStream {
        SerialPort serialPort = null;

        public SerialPortOutputStream(SerialPort var2) {
            this.serialPort = var2;
        }

        public void write(int var1) throws IOException {
            try {
                this.serialPort.writeInt(var1);
            } catch (SerialPortException var3) {
                throw new IOException(var3);
            }
        }

        public void write(byte[] var1) throws IOException {
            this.write(var1, 0, var1.length);
        }

        public void write(byte[] var1, int var2, int var3) throws IOException {
            byte[] var4 = new byte[var3];
            System.arraycopy(var1, var2, var4, 0, var3);

            try {
                this.serialPort.writeBytes(var4);
            } catch (SerialPortException var6) {
                throw new IOException(var6);
            }
        }
    }
}