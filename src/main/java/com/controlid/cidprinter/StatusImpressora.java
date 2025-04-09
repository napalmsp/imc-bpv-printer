package com.controlid.cidprinter;

public class StatusImpressora {
    public boolean Online;
    public boolean Erro;
    public boolean SemPapel;
    public boolean TampaAberta;

    public StatusImpressora(boolean onLine, boolean erro, boolean semPapel, boolean tampaAberta) {
        this.Online = onLine;
        this.Erro = erro;
        this.SemPapel = semPapel;
        this.TampaAberta = tampaAberta;
    }
}
