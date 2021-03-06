
package com.ecircle.developer.ecmapi;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebFault(name = "ObjectAlreadyExistsException", targetNamespace = "http://ecircle.com/developer/ecmapi")
public class ObjectAlreadyExistsException_Exception
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private ObjectAlreadyExistsException faultInfo;

    /**
     * 
     * @param message
     * @param faultInfo
     */
    public ObjectAlreadyExistsException_Exception(String message, ObjectAlreadyExistsException faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public ObjectAlreadyExistsException_Exception(String message, ObjectAlreadyExistsException faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: com.ecircle.developer.ecmapi.ObjectAlreadyExistsException
     */
    public ObjectAlreadyExistsException getFaultInfo() {
        return faultInfo;
    }

}
