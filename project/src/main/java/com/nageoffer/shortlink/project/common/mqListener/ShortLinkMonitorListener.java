package com.nageoffer.shortlink.project.common.mqListener;

import com.nageoffer.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShortLinkMonitorListener {

    final ShortLinkService shortLinkService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "shortLink.monitor.queue",durable = "true"),
            exchange = @Exchange(name = "shortLink.exchange",type = ExchangeTypes.TOPIC),
            key = "shortLink.monitor"
    ))
    public void ShortLinkMonitor(ShortLinkStatsRecordDTO uv) {
        shortLinkService.updateMonitor(uv);
    }
}
