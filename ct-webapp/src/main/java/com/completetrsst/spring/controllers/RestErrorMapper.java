package com.completetrsst.spring.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.xml.crypto.dsig.XMLSignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice()
public class RestErrorMapper {

    private final static Logger log = LoggerFactory.getLogger(RestErrorMapper.class);

    // 406 if Xml Signature couldn't be parsed or found
    @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(XMLSignatureException.class)
    @ResponseBody
    public String handleXmlSignatureProblem(HttpServletRequest req, Exception ex) {
        StringBuilder message = new StringBuilder();
        message.append("Expected a well formed XML Digital Signature. ");
        message.append(ex.getLocalizedMessage());
        String messageString = message.toString();
        log.info(messageString);
        return messageString;
    }

    // 406 if we can't parse the input or it's not what we want
    @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public String handleBadInputException(HttpServletRequest req, Exception ex) {
        log.info(ex.getLocalizedMessage());
        return ex.getLocalizedMessage();
    }
}
