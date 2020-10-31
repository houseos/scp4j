/*
 * secure_control_protocol
 * ScpStatus Class
 * SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2020 Marcel Jaehn
 */
package org.houseos.scp4j;

public final class ScpStatus {

    private ScpStatus() {
        // this class has no accessible methods
    }

    static final String RESULT_DONE = "done";
    static final String RESULT_SUCCESS = "success";
    static final String RESULT_ERROR = "error";
}
