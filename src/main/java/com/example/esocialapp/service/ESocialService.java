package com.example.esocialapp.service;

public interface ESocialService {
    String gerarXml(Object evento);
    String assinarXml(String xml);
    boolean transmitirEvento(String xmlAssinado);
}
