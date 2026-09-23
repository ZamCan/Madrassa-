package com.zamcan.madrassa.domain.authorization;

import com.zamcan.madrassa.data.model.AccountRole;
import com.zamcan.madrassa.data.model.Madrassa;
import com.zamcan.madrassa.data.model.Ustadh;
import com.zamcan.madrassa.domain.repository.UstadhStore;

public final class AccountIdentityResolverImpl
        implements AccountIdentityResolver {

    private final MadrassaIdentityResolver madrassaIdentityResolver;
    private final UstadhStore ustadhStore;

    public AccountIdentityResolverImpl(
            MadrassaIdentityResolver madrassaIdentityResolver,
            UstadhStore ustadhStore
    ) {
        if (madrassaIdentityResolver == null) {
            throw new IllegalArgumentException(
                    "madrassaIdentityResolver must not be null"
            );
        }

        if (ustadhStore == null) {
            throw new IllegalArgumentException(
                    "ustadhStore must not be null"
            );
        }

        this.madrassaIdentityResolver =
                madrassaIdentityResolver;

        this.ustadhStore =
                ustadhStore;
    }

    @Override
    public AccountIdentity resolve(
            String identifier,
            AccountRole role
    ) {
        if (role == null ||
                UstadhIdentifierPolicy.isBlank(identifier)) {
            return null;
        }

        if (role == AccountRole.USTADH) {
            return resolveUstadh(identifier);
        }

        return null;
    }

    private AccountIdentity resolveUstadh(
            String identifier
    ) {
        if (UstadhIdentifierPolicy.looksLikePhone(
                identifier
        )) {
            return resolveUstadhByPhone(
                    identifier
            );
        }

        return resolveUstadhByMadrassaName(
                identifier
        );
    }

    private AccountIdentity resolveUstadhByPhone(String phone) {
        /*
         * First resolve official Madrassa-level phones.
         * A Madrassa may have multiple registered phones.
         */
        Madrassa madrassa =
                madrassaIdentityResolver.findByRegisteredPhone(phone);

        if (madrassa != null) {

            /*
             * If this phone belongs directly to an individual active
             * Ustadh, resolve that specific Ustadh account first.
             */
            Ustadh directUstadh = ustadhStore.findByPhone(phone);

            if (directUstadh != null
                    && directUstadh.active
                    && madrassa.id.equals(directUstadh.madrassaId)) {

                return new AccountIdentity(
                        directUstadh.id,
                        madrassa.id,
                        AccountRole.USTADH
                );
            }

            /*
             * Otherwise a registered Madrassa phone identifies the
             * active Head Ustadh account.
             *
             * This is only identity resolution. Password verification
             * is still performed by UstadhAuthenticationService.
             */
            Ustadh head =
                    ustadhStore.findActiveHeadByMadrassa(madrassa.id);

            if (head != null && head.active) {
                return new AccountIdentity(
                        head.id,
                        madrassa.id,
                        AccountRole.USTADH
                );
            }
        }

        /*
         * Individual Ustadh phone:
         * supports Ustadh phones that are not also stored as
         * Madrassa-level contact phones.
         */
        Madrassa ustadhMadrassa =
                madrassaIdentityResolver.findByUstadhPhone(phone);

        if (ustadhMadrassa == null) {
            return null;
        }

        Ustadh ustadh = ustadhStore.findByPhone(phone);

        if (ustadh == null
                || !ustadh.active
                || !ustadhMadrassa.id.equals(ustadh.madrassaId)) {
            return null;
        }

        return new AccountIdentity(
                ustadh.id,
                ustadhMadrassa.id,
                AccountRole.USTADH
        );
    }

    private AccountIdentity resolveUstadhByMadrassaName(
            String identifier
    ) {
        Madrassa madrassa =
                madrassaIdentityResolver
                        .findByMadrassaName(
                                identifier
                        );

        if (madrassa == null ||
                madrassa.id == null) {
            return null;
        }

        Ustadh headUstadh =
                ustadhStore
                        .findActiveHeadByMadrassa(
                                madrassa.id
                        );

        if (headUstadh == null ||
                headUstadh.id == null ||
                !headUstadh.active ||
                !headUstadh.headUstadh ||
                !madrassa.id.equals(
                        headUstadh.madrassaId
                )) {
            return null;
        }

        return new AccountIdentity(
                headUstadh.id,
                madrassa.id,
                AccountRole.USTADH
        );
    }
}
