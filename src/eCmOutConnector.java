
import com.ecircle.developer.ecmapi.*;
import org.apache.commons.codec.binary.Base64;
import streamserve.connector.StrsConfigVals;
import streamserve.connector.StrsConnectable;
import streamserve.connector.StrsServiceable;

import javax.naming.ConfigurationException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.HandlerResolver;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

public class eCmOutConnector implements StrsConnectable
{
    private static final String SOAP_URL = "Endpoint URL";       //endpoint URL. system specific
    private static final String USER_NAME = "User login (email)";       // username email
    private static final String USER_PASSWORD = "User password";
    private static final String PROXY_SERVER = "Proxy server";
    private static final String PROXY_SERVER_PORT = "Proxy server port";
    private static final String PROXY_USER = "Proxy user name";
    private static final String PROXY_USER_PASSWORD = "Proxy user password";
    private static final String GROUP_ID = "Group ID";
    private static final String GROUP_NAME = "Group Name";
    private static final String GROUP_EMAIL = "Group Email";
    private static final String LOG_CONTROLXML = "Log file for control XML";

    private static final String RECIPIENT = "Recipient Email";     // runtime
    private static final String CC_EMAIL = "CC Email";     // runtime
    private static final String BCC_EMAIL = "BCC Email";     // runtime
    private static final String SUBJECT = "Mail Subject";   // runtime
    private static final String MESSAGE_ID = "Message ID";  // runtime
    private static final String CUSTOM_VALUE = "Custom Value";

    private StrsServiceable m_service = null;
    private static final String ATTACHMENT1 = "Attachment #1";
    private static final String ATTACHMENT2 = "Attachment #2";
    private static final String ATTACHMENT3 = "Attachment #3";
    private static final String ATTACHMENT4 = "Attachment #4";
    private static final String ATTACHMENT5 = "Attachment #5";

    private String m_soapURL = "";         //platf field
    private String m_username = "";        //platf field
    private String m_userpassword = "";    //platf field
    private String m_proxyserver = "";     //platf field
    private String m_proxyserver_port = "";     //platf field
    private String m_proxy_user = "";     //platf field
    private String m_proxy_password = "";     //platf field
    public static String m_log_control_xml_filename = "";     //platf field

    private String m_recipient = "";     //rt field
    private String m_cc_email = "";     //rt field
    private String m_bcc_email = "";     //rt field
    private String m_subject = "";       // rt field
    private long m_messageId;      // rt field
    private String m_custom_value = ""; // rt field

    private ByteArrayOutputStream m_outputStream = null;
    private ArrayList<String> m_attachments = null;
    private ArrayList<String> m_recipients = null;
    private String m_jobid;

    public eCmOutConnector()
    {
        m_service = null;
        m_outputStream = new ByteArrayOutputStream();
        m_attachments = new ArrayList<String>();
        m_recipients = new ArrayList<String>();
    }

    @Override
    public boolean strsoStartJob(StrsConfigVals strsConfigVals) throws RemoteException {

        try
        {
            LoadPlatformConfig(strsConfigVals);
            //logDebug("enter StartJob");
        }
        catch (ConfigurationException e)
        {
            logException(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean strsoOpen(StrsConfigVals strsConfigVals) throws RemoteException {
        //logDebug("enter StrsOpen");
        try
        {
            LoadRuntimeConfig(strsConfigVals);
        }
        catch (ConfigurationException e)
        {
            logException(e);
            return false;
        }

        m_outputStream = new ByteArrayOutputStream();
        return true;
    }

    @Override
    public boolean strsoWrite(byte[] bytes) throws RemoteException {
        //logDebug("enter StrsWrite");
        if(bytes != null)
        {
            try
            {
                m_outputStream.write(bytes);
                return true;
            }
            catch (IOException e) {
                logError(e.getLocalizedMessage());
            }
            catch (Throwable e)  {
                logError(e.getLocalizedMessage());
            }
        }
        return false;
    }

    @Override
    public boolean strsoClose(StrsConfigVals strsConfigVals) throws RemoteException {
        //logDebug("enter StartClose");
        try
        {
            m_recipients.clear();
            m_attachments.clear();
            LoadRuntimeConfig(strsConfigVals);

            if ( !m_proxyserver.isEmpty() && !m_proxyserver_port.isEmpty() )
            {
                final String authUser = m_proxy_user;
                final String authPassword = m_proxy_password;
                Authenticator.setDefault(new Authenticator()
                {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(authUser, authPassword.toCharArray());
                    }
                });

                System.setProperty("proxySet", "true");
                System.setProperty("http.proxyHost", m_proxyserver);
                System.setProperty("http.proxyPort", m_proxyserver_port);

                System.setProperty("https.proxyHost", this.m_proxyserver);
                System.setProperty("https.proxyPort", this.m_proxyserver_port);

                System.setProperty("http.proxyUser", authUser);
                System.setProperty("http.proxyPassword", authPassword);

                if (m_proxy_user.isEmpty() || m_proxy_password.isEmpty() )
                {
                    logDebug(String.format("Proxy anonymous %s:%s enabled. Credentials is not provided", m_proxyserver, m_proxyserver_port) );
                }
                else
                {
                    logDebug(String.format("Authentication proxy %s:%s enabled. Proxy User: %s", m_proxyserver, m_proxyserver_port, m_proxy_user  ));
                }
            }

            logDebug("m_endpointURL: " + m_soapURL);
            logDebug("m_username: " + m_username);
            logDebug("m_userpassword: " + m_userpassword);
            logDebug("m_proxyserver: " + m_proxyserver);
            logDebug("m_proxyserver_port: " + m_proxyserver_port);
            logDebug("m_proxy_user: " + m_proxy_user);
            logDebug("m_proxy_password: " + m_proxy_password);
            logDebug("m_recipient: " + m_recipient);
            logDebug("m_cc_email: " + m_cc_email);
            logDebug("m_bcc_email: " + m_bcc_email);
            logDebug("m_subject: " + m_subject);
            logDebug("m_Message_id: " + m_messageId);
            logDebug("m_custom_value: " + m_custom_value);
            logDebug("m_log_control_xml_filename: " + m_log_control_xml_filename);
            for ( String att : m_attachments) {
                logDebug("m_attachment: " + att);
            }

            EcmWS ecm = new EcmWS();

            HandlerResolver myHanlderResolver = new MyHandlerResolver();
            ecm.setHandlerResolver(myHanlderResolver);

            Ecm ecmService = ecm.getEcmWSPort();

            Map<String, Object> ctx = ((BindingProvider) ecmService).getRequestContext();
            URL accessUrl = new URL(m_soapURL);
            ctx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, accessUrl.toExternalForm());
            ctx.put(BindingProvider.USERNAME_PROPERTY, m_username);
            ctx.put(BindingProvider.PASSWORD_PROPERTY, m_userpassword);

            ecmService.messageValidate(String.valueOf(m_messageId));

            MessageContent messageContent = new MessageContent();

            // adding message body
            Attribute attribute = new Attribute();
            attribute.setName("MSG_BODY");
            attribute.setValue(m_outputStream.toString());
            messageContent.getParameters().add(attribute);
            Attribute attribute2 = new Attribute();
            attribute2.setName("SUBJECT");
            attribute2.setValue(m_subject);
            messageContent.getParameters().add(attribute2);

            // adding attachments
            if (m_attachments.size() > 0)
            {
                for (String attach_item : m_attachments)
                {
                    Attachment attachment = new Attachment();
                    Path path = new File(attach_item).toPath();
                    attachment.setName(path.getFileName().toString());
                    String contentType = null;
                    try
                    {
                        contentType = Files.probeContentType(path);
                        //contentType = null;
                    }
                    catch ( IOException e)
                    {
                        logError(e.getLocalizedMessage());
                    }
                    catch (SecurityException e)
                    {
                        logError("If a security manager is installed and it denies an unspecified permission required by a file type detector implementation." + e.getLocalizedMessage());
                    }

                    if (contentType == null )
                    {
                        FileNameMap fileNameMap = URLConnection.getFileNameMap();
                        contentType = fileNameMap.getContentTypeFor(path.toString());
                        if (path.toString().endsWith("pdf") && !contentType.contains("pdf"))
                        {
                            contentType = "application/pdf";
                        }
                        if (contentType == null)
                        {
                            contentType = "text/plain";
                        }
                        logDebug("Attachment will be send like " + contentType);
                    }
                    attachment.setContentType(contentType);
                    byte[] atta_bytes = Files.readAllBytes(Paths.get(attach_item));
                    byte[] atta_encoded = Base64.encodeBase64(atta_bytes);
                    String printMe = new String(atta_encoded, "US-ASCII");
                    attachment.setContent(printMe);
                    messageContent.getAttachments().add(attachment);
                }
                m_attachments.clear();
            }

            //parsing recipients field . ";" must be token
            StringTokenizer recipientTokenizer = new StringTokenizer(m_recipient, ";");
            while (recipientTokenizer.hasMoreElements())
            {
                m_recipients.add((String) recipientTokenizer.nextElement());
            }

            // sending message to all recipients
            for (String recipient_item : m_recipients)
            {
                User curr_user = new User();
                try {
                    //logDebug("Checking if user exist");
                    curr_user = ecmService.userGetByEmail(recipient_item);

                }
                catch (NoSuchObjectException_Exception e)
                {
                    logError(e.getLocalizedMessage());

                    logDebug("User with email=$s does not exist. It will be created" );
                    try {
                        curr_user  = ecmService.userCreate(recipient_item, null, null);
                    }
                    catch (InvalidParameterException_Exception ec)
                    {
                        logError("User wasn't created. Invalid parameter");
                        logError(ec.getLocalizedMessage());
                    }
                    catch (ObjectAlreadyExistsException_Exception ec )
                    {
                        logError("User wasn't created. Object already exist");
                        logError(ec.getLocalizedMessage());
                    }
                    catch ( UnexpectedErrorException_Exception ec)
                    {
                        logError("Unexpected Error Exception. User wasn't created !");
                        logError(ec.getLocalizedMessage());
                    }
                    logDebug(String.format("User %s was created", recipient_item ));

                }
                catch (InvalidParameterException_Exception e)
                {
                    logError(e.getLocalizedMessage());
                }
                catch (UnexpectedErrorException_Exception e)
                {
                    logError(e.getLocalizedMessage());
                }
                finally
                {
                    if (curr_user.getId() != 0)
                    {
                        ecmService.messageSendTransactional(m_messageId, m_custom_value, curr_user.getId(), messageContent);
                        logDebug(String.format("Message has been sent to %s. ", curr_user.getEmail()));
                    }
                }
            }

        }
        catch (UnexpectedErrorException_Exception e)
        {
            logError(e.getLocalizedMessage());
            return false;
        }
        catch (NoSuchObjectException_Exception e )
        {
            logError(e.getLocalizedMessage());
            return false;
        }

        catch (Exception e)
        {
            logError(e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean strsoEndJob() throws RemoteException {
        //logDebug("enter EndJob");
        return true;
    }

    private boolean LoadRuntimeConfig(StrsConfigVals cfg) throws RemoteException, ConfigurationException{

        //reading runtime fields
        String recipient = cfg.getValue(RECIPIENT);
        if (recipient != null && recipient.length() > 0)
        {
            m_recipient = recipient;
        }

        String ccemail = "";
        ccemail = cfg.getValue(CC_EMAIL);

        if (ccemail != null && ccemail.length() > 0)
        {
            m_cc_email = ccemail;
        }

        String bccemail = "";
        bccemail = cfg.getValue(BCC_EMAIL);

        if (bccemail != null && bccemail.length() > 0)
        {
            m_bcc_email = bccemail;
        }

        String subject = cfg.getValue(SUBJECT);
        if (subject != null && subject.length() > 0)
        {
            m_subject = subject;
        }

        String message_id = cfg.getValue(MESSAGE_ID);
        if (message_id != null && message_id.length() > 0)
        {
            m_messageId = Long.parseLong(message_id);
        }

        String custom_value = cfg.getValue(CUSTOM_VALUE);
        if (custom_value != null && custom_value.length() > 0)
        {
            m_custom_value = custom_value;
        }

        m_attachments.clear();
        String attachment_url = cfg.getValue(ATTACHMENT1);
        if (attachment_url != null && attachment_url.length() > 0)
        {
            m_attachments.add(attachment_url);
        }
        String attachment_url2 = cfg.getValue(ATTACHMENT2);
        if (attachment_url2 != null && attachment_url2.length() > 0)
        {
            m_attachments.add(attachment_url2);
        }
        String attachment_url3 = cfg.getValue(ATTACHMENT3);
        if (attachment_url3 != null && attachment_url3.length() > 0)
        {
            m_attachments.add(attachment_url3);
        }
        String attachment_url4 = cfg.getValue(ATTACHMENT4);
        if (attachment_url4 != null && attachment_url4.length() > 0)
        {
            m_attachments.add(attachment_url4);
        }
        String attachment_url5 = cfg.getValue(ATTACHMENT5);
        if (attachment_url5 != null && attachment_url5.length() > 0)
        {
            m_attachments.add(attachment_url5);
        }

        return true;
    }

    // Utility methods
    private boolean LoadPlatformConfig(StrsConfigVals cfg) throws RemoteException, ConfigurationException
    {
        if (cfg == null)
        {
            logError("LoadPlatformConfig(StrsConfigVals cfg) is not correct");
            return false;
        }

        StrsServiceable service = cfg.getStrsService();
        if(service != null)
        {
            m_service = service;
        }

        m_jobid = cfg.getSystemValue("jobid");

// reading platform fields
        String soapURL = cfg.getValue(SOAP_URL);
        if (soapURL != null && soapURL.length() > 0)
        {
            m_soapURL = soapURL;
        }

        String username = cfg.getValue(USER_NAME);
        if(username != null && username.length() > 0)
        {
            m_username = username;
        }

        String userpassword = cfg.getValue(USER_PASSWORD);
        if(userpassword != null && userpassword.length() > 0)
        {
            m_userpassword = userpassword;
        }

        String proxy_server = cfg.getValue(PROXY_SERVER);
        if (proxy_server != null && proxy_server.length() > 0)
        {
            m_proxyserver = proxy_server;
        }

        String proxy_server_port = cfg.getValue(PROXY_SERVER_PORT);
        if (proxy_server != null && proxy_server.length() > 0)
        {
            m_proxyserver_port = proxy_server_port;
        }

        String proxy_user = cfg.getValue(PROXY_USER);
        if (proxy_user != null && proxy_user.length() > 0)
        {
            m_proxy_user = proxy_user;
        }

        String proxy_pass = cfg.getValue(PROXY_USER_PASSWORD);
        if (proxy_pass != null && proxy_pass.length() > 0)
        {
            m_proxy_password = proxy_pass;
        }

        String log_ctrlxml_filename = cfg.getValue(LOG_CONTROLXML);
        if (log_ctrlxml_filename != null && log_ctrlxml_filename.length() > 0)
        {
            m_log_control_xml_filename = log_ctrlxml_filename;
        }
        return true;
    }
    private void logError(String message) throws RemoteException
    {
        if (m_service != null)
        {
            m_service.writeMsg(StrsServiceable.MSG_ERROR, 0, "jobID: " + m_jobid + " eCmOutConnector ERROR: " + message);
        }
        else
        {
            System.out.println(message);
        }
    }

    private void logDebug(String message) throws RemoteException
    {
        if (m_service != null)
        {
            m_service.writeMsg(StrsServiceable.MSG_DEBUG, 4, "jobID: " + m_jobid + " eCmOutConnector: "+ message);
        }
        else
        {
            System.out.println(message);
        }
    }

    private void logException(Throwable t)
    {
        try
        {
            logError(t.getMessage()); //1

            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            printWriter.write("Exception stacktrace: ");
            t.printStackTrace(printWriter);

            logDebug(writer.toString()); //99

            try
            {
                writer.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        } catch (Throwable e)
        {
            System.out.println(t.getMessage());
        }
    }
}
