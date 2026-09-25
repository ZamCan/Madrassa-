package com.zamcan.madrassa.domain.integration;

import java.util.Collections;
import java.util.Map;

/**
 * Provider-neutral payment boundary.
 *
 * Implementations belong outside the domain core. The local FeeService
 * remains authoritative for the local payment lifecycle until a provider
 * confirms a transaction.
 */
public interface PaymentGateway {
    PaymentResult initiate(PaymentRequest request);

    final class PaymentRequest {
        public final String reference;
        public final String payer;
        public final String phone;
        public final double amount;
        public final String currency;
        public final Map<String, String> metadata;

        public PaymentRequest(String reference, String payer, String phone,
                              double amount, String currency,
                              Map<String, String> metadata) {
            this.reference = reference;
            this.payer = payer;
            this.phone = phone;
            this.amount = amount;
            this.currency = currency;
            this.metadata = metadata == null
                    ? Collections.emptyMap()
                    : Collections.unmodifiableMap(metadata);
        }
    }

    final class PaymentResult {
        public final boolean accepted;
        public final String providerReference;
        public final String status;
        public final String message;

        private PaymentResult(boolean accepted, String providerReference,
                              String status, String message) {
            this.accepted = accepted;
            this.providerReference = providerReference;
            this.status = status;
            this.message = message;
        }

        public static PaymentResult accepted(
                String providerReference, String status) {
            return new PaymentResult(true, providerReference, status, null);
        }

        public static PaymentResult rejected(String status, String message) {
            return new PaymentResult(false, null, status, message);
        }
    }
}
