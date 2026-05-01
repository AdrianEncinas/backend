package com.assetstrack.backend.exception;

public class NotFoundException extends RuntimeException{

    public NotFoundException (String msj){
        super(msj);
    }
}
