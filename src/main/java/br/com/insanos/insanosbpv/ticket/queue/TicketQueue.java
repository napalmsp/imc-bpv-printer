package br.com.insanos.insanosbpv.ticket.queue;

import br.com.insanos.insanosbpv.ticket.bean.StaffTicket;
import br.com.insanos.insanosbpv.ticket.service.StaffTicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class TicketQueue {

    @Bean
    public Queue receiveTicketQueue() {
        return new Queue("imc.sgs.ticket", true);
    }

    @Bean
    TopicExchange ticketExchange() {
        return new TopicExchange("imc-amqp.topic.exchange", true, false);
    }

    @Bean
    Binding TicketBinding(Queue receiveBillingQueue, TopicExchange financialExchange) {
        return BindingBuilder.bind(receiveBillingQueue).to(financialExchange).with("imc.sgs.bpv.ticket");
    }
    @RabbitListener( queues = {"imc.sgs.ticket"},
            autoStartup = "${queue.bpv.ticket:false}")
    public void receiveTicketMessage(
            @Payload StaffTicket payload,
            @Header("amqp_receivedRoutingKey") String routingKey,
            Message message) throws IOException {
        try {
            log.info("Received receiveMessageFromMatriculas (" + routingKey + ")\n payload: " + payload + "\n message: " + message);
            StaffTicketService.printStaffTicket(payload);

        } catch (Exception ex) {
            log.error("Erro ao processar ", ex);
        }
    }

}
