package com.zamcan.madrassa.domain.authorization;

import com.zamcan.madrassa.data.model.Madrassa;

public interface MadrassaIdentityResolver {

    Madrassa findByMadrassaName(
            String madrassaName
    );

    Madrassa findByRegisteredPhone(
            String phone
    );

    Madrassa findByUstadhPhone(
            String phone
    );

    Madrassa resolve(
            String identifier
    );
}
