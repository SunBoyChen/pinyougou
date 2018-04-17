package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;

@Component
public class pageDeleteListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;

        try {
            Long[] goodsIds = (Long[]) objectMessage.getObject();

            boolean b = itemPageService.deleteItemHtml(goodsIds);

            System.out.println("网页删除成功!");
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
}
