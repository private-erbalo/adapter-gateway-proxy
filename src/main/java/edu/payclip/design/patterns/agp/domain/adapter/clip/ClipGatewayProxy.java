package edu.payclip.design.patterns.agp.domain.adapter.clip;

import edu.payclip.design.patterns.agp.domain.GatewayProxy;
import edu.payclip.design.patterns.agp.domain.PaymentRequestReady;
import edu.payclip.design.patterns.agp.domain.ProxyTransaction;
import edu.payclip.design.patterns.agp.representation.OrderRepresentation;
import edu.payclip.design.patterns.agp.representation.request.PaymentRequest;
import edu.payclip.design.patterns.agp.util.OrderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class ClipGatewayProxy implements GatewayProxy {

    @Override
    public PaymentRequestReady prepare(PaymentRequest paymentRequest) {
        log.info("Preparing request {}", paymentRequest);
        OrderRepresentation order = paymentRequest.getOrder();
        BigDecimal amount = OrderUtil.calculateAmount(order);

        PaymentRequestReady paymentRequestReady = PaymentRequestReady.builder()
                .amount(amount)
                .order(order)
                .proxy(paymentRequest.getProxy())
                .tax(amount.multiply(BigDecimal.TEN))
                .build();

        return paymentRequestReady;
    }

    @Override
    public ProxyTransaction pay(PaymentRequestReady paymentReady) {
        // Consumer the payment integration (paypal sdk, loand client)
        BigDecimal taxesAmount = paymentReady.getAmount().add(paymentReady.getTax());

        ProxyTransaction transaction = ProxyTransaction.builder()
                .amount(taxesAmount)
                .createAt(Instant.now())
                .orderId(paymentReady.getOrder().getId())
                .proxy(paymentReady.getProxy())
                .reference(UUID.randomUUID().toString())
                .build();

        return transaction;
    }
}
