package com.mycompany;


import java.io.IOException;

public class InsufficientFundsException extends IOException{

    public InsufficientFundsException(String message){
        super(message);
    }
}
