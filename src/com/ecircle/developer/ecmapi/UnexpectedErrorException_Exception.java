
package com.ecircle.developer.ecmapi;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebFault(name = "UnexpectedErrorException", targetNamespace = "http://ecircle.com/developer/ecmapi")
public class UnexpectedErrorException_Exception
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private UnexpectedErrorException faultInfo;

    /**
     * 
     * @param message
     * @param faultInfo
     */
    public UnexpectedErrorException_Exception(String message, UnexpectedErrorException faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public UnexpectedErrorException_Exception(String message, UnexpectedErrorException faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: com.ecircle.developer.ecmapi.UnexpectedErrorException
     */
    public UnexpectedErrorException getFaultInfo() {
        return faultInfo;
    }

}