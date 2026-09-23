package com.zamcan.madrassa.domain.repository;

import com.zamcan.madrassa.data.model.SmsMessage;

import java.util.List;

public interface SmsStore {

    SmsMessage findById(String messageId);

    List<SmsMessage> findByMadrassa(
            String madrassaId
    );

    boolean save(SmsMessage message);

    boolean update(SmsMessage message);
}
