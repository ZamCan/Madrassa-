package com.zamcan.madrassa.domain.authorization;

import com.zamcan.madrassa.data.model.AccountRole;

public interface AccountIdentityResolver {

    AccountIdentity resolve(
            String identifier,
            AccountRole role
    );

    final class AccountIdentity {

        public final String accountId;
        public final String madrassaId;
        public final AccountRole role;

        public AccountIdentity(
                String accountId,
                String madrassaId,
                AccountRole role
        ) {
            this.accountId = accountId;
            this.madrassaId = madrassaId;
            this.role = role;
        }
    }
}
