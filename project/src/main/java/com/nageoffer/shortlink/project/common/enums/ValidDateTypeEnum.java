package com.nageoffer.shortlink.project.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ValidDateTypeEnum {

    PERMANENT(0),

    CUSTOMER(1);

    @Getter
    private final int type;

}
