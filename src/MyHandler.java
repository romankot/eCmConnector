import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class MyHandler implements SOAPHandler<SOAPMessageContext>
{
    public  static ByteArrayOutputStream out;
    private boolean isRequest;

    public boolean handleMessage(SOAPMessageContext smc) {

    Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    SOAPMessage message = smc.getMessage();

    // This if condition gets reversed when we write code on server side.
    if (outboundProperty.booleanValue()) {
        System.out.print(" SOAP Request ");
        isRequest = true;
    } else {
        System.out.print(" SOAP Respone ");
        isRequest = false;
    }
    try {
        String logfilename = eCmOutConnector.m_log_control_xml_filename;
        //String logfilename = "c:\\tmp\\ecmLogAPIv2.txt";
        if (!logfilename.isEmpty()) {
            out = new ByteArrayOutputStream();
            message.writeTo(out);
            FLogger.logToFile(out, logfilename, isRequest);
            //System.out.println(FLogger.prettyFormat(out.toString()));
        }
    } catch (SOAPException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
    System.out.println("");
    // if this function will return true, only then the chaining concept will work.
    // if we return outboundProperty which happens to be false in some cases
    // the chaining will not work. 
    //return outboundProperty; 
    return true;

    }

    public Set getHeaders() {
        return null;
    }

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    public void close(MessageContext context) {
    }
}
