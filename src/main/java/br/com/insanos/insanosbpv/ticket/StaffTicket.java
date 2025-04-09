package br.com.insanos.insanosbpv.ticket;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class StaffTicket {
    private String eventName;
    private String place;
    private Date startAt;
    private Date endAt;
    private String coletName;
    private String regionName;
    private String divisionName;
    private String qrCode;

    @Singular("message")
    private List<String> messages;
}
