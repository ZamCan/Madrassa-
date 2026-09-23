package com.zamcan.madrassa.domain.authorization;

import com.zamcan.madrassa.data.model.Madrassa;
import com.zamcan.madrassa.data.model.MadrassaPhone;
import com.zamcan.madrassa.data.model.Ustadh;
import com.zamcan.madrassa.data.repository.MadrassaPhoneRepository;
import com.zamcan.madrassa.data.repository.MadrassaRepository;
import com.zamcan.madrassa.data.repository.UstadhRepository;

public class MadrassaIdentityResolverImpl
        implements MadrassaIdentityResolver {

    private final MadrassaRepository madrassaRepository;
    private final MadrassaPhoneRepository phoneRepository;
    private final UstadhRepository ustadhRepository;

    public MadrassaIdentityResolverImpl(
            MadrassaRepository madrassaRepository,
            MadrassaPhoneRepository phoneRepository,
            UstadhRepository ustadhRepository
    ) {
        if (madrassaRepository == null) {
            throw new IllegalArgumentException(
                    "madrassaRepository must not be null"
            );
        }

        if (phoneRepository == null) {
            throw new IllegalArgumentException(
                    "phoneRepository must not be null"
            );
        }

        if (ustadhRepository == null) {
            throw new IllegalArgumentException(
                    "ustadhRepository must not be null"
            );
        }

        this.madrassaRepository =
                madrassaRepository;

        this.phoneRepository =
                phoneRepository;

        this.ustadhRepository =
                ustadhRepository;
    }

    @Override
    public Madrassa findByMadrassaName(
            String madrassaName
    ) {
        if (isBlank(madrassaName)) {
            return null;
        }

        return madrassaRepository.findByName(
                normalizeName(madrassaName)
        );
    }

    @Override
    public Madrassa findByRegisteredPhone(
            String phone
    ) {
        if (isBlank(phone)) {
            return null;
        }

        MadrassaPhone registeredPhone =
                phoneRepository.findByPhone(phone);

        if (registeredPhone == null ||
                !registeredPhone.active ||
                isBlank(registeredPhone.madrassaId)) {
            return null;
        }

        return madrassaRepository.findById(
                registeredPhone.madrassaId
        );
    }

    @Override
    public Madrassa findByUstadhPhone(
            String phone
    ) {
        if (isBlank(phone)) {
            return null;
        }

        Ustadh ustadh =
                ustadhRepository.findByPhone(phone);

        if (ustadh == null ||
                !ustadh.active ||
                isBlank(ustadh.madrassaId)) {
            return null;
        }

        return madrassaRepository.findById(
                ustadh.madrassaId
        );
    }

    @Override
    public Madrassa resolve(
            String identifier
    ) {
        if (isBlank(identifier)) {
            return null;
        }

        String value =
                identifier.trim();

        if (looksLikePhone(value)) {

            Madrassa madrassa =
                    findByRegisteredPhone(value);

            if (madrassa != null) {
                return madrassa;
            }

            /*
             * A phone may belong directly to an
             * active Ustadh rather than the Madrassa's
             * primary/secondary contact records.
             */
            madrassa =
                    findByUstadhPhone(value);

            if (madrassa != null) {
                return madrassa;
            }
        }

        return findByMadrassaName(value);
    }

    private boolean looksLikePhone(
            String value
    ) {
        boolean hasDigit = false;

        for (int i = 0; i < value.length(); i++) {

            char c = value.charAt(i);

            if (Character.isDigit(c)) {
                hasDigit = true;
                continue;
            }

            if (c == '+' ||
                    c == ' ' ||
                    c == '-' ||
                    c == '(' ||
                    c == ')' ||
                    c == '.') {
                continue;
            }

            return false;
        }

        return hasDigit;
    }

    private String normalizeName(
            String value
    ) {
        return value
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }

    private boolean isBlank(
            String value
    ) {
        return value == null ||
                value.trim().isEmpty();
    }
}
