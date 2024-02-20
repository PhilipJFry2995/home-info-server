package com.filiahin.home.airconditioner.binding.exception;

import com.filiahin.home.airconditioner.GreeAirconditionerDevice;

public class BindingUnsuccessfulException extends RuntimeException {
    private final GreeAirconditionerDevice device;

    public BindingUnsuccessfulException(GreeAirconditionerDevice device) {
        this.device = device;
    }
}
