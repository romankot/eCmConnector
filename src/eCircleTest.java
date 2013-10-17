import com.ecircle.developer.ecmapi.*;
import org.apache.commons.codec.binary.Base64;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.HandlerResolver;
import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;


public class eCircleTest {
    private static final String m_soapURL = "https://secure.ecircle-ag.com/cm42/api/soap/v2/"; // platform option
    private static final String m_username = "mmazur@opentext.com";                 // platform option
    private static final String m_userpassword = "Test00?";                       // platform option
    private static final String DRAFT_MESSAGE_NAME = "myMsg";
    private static final long DRAFT_MESSAGE_ID = 401444594; // runtime

    private static ArrayList<String> m_attachments = new ArrayList<String>();
    static {
        m_attachments.add("C:\\tmp\\eCmTriger.xml");
        m_attachments.add("C:\\tmp\\test.zip");
    }

    public static void main(String[] args) throws InvalidParameterException_Exception, NoSuchObjectException_Exception, UnexpectedErrorException_Exception, ObjectAlreadyExistsException_Exception, IOException, AsyncException_Exception, InterruptedException {


        if (false)
        {
            final String authUser = "";
            final String authPassword = "";
            Authenticator.setDefault(new Authenticator()
            {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(authUser, authPassword.toCharArray());
                }
            });

            // 195.114.128.9	 3128
            System.setProperty("proxySet", "true");
            System.setProperty("http.proxyHost", "195.114.128.9");
            System.setProperty("http.proxyPort", "3128");

            System.setProperty("https.proxyHost", "195.114.128.9");
            System.setProperty("https.proxyPort", "3128");

            System.setProperty("http.proxyUser", authUser);
            System.setProperty("http.proxyPassword", authPassword);
        }

        EcmWS ecm = new EcmWS();
        HandlerResolver myHanlderResolver = new MyHandlerResolver();
        ecm.setHandlerResolver(myHanlderResolver);
        System.out.println("myHandlerResolver has been set.");
        Ecm ecmService = ecm.getEcmWSPort();

        Map<String, Object> ctx = ((BindingProvider) ecmService).getRequestContext();
        URL accessUrl = new URL(m_soapURL);
        ctx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, accessUrl.toExternalForm());
        ctx.put(BindingProvider.USERNAME_PROPERTY, m_username);
        ctx.put(BindingProvider.PASSWORD_PROPERTY, m_userpassword);

        User new_user = new User();
        String newuser_email = "wrong1108@open_text.com";

        try {
            new_user = ecmService.userGetByEmail(newuser_email);
        }
        catch (InvalidParameterException_Exception|NoSuchObjectException_Exception e)
        {
            try {
                new_user  = ecmService.userCreate(newuser_email, null, null);
            } catch (InvalidParameterException_Exception e1) {
                e1.printStackTrace();  //To change
            } catch (ObjectAlreadyExistsException_Exception e1) {
                e1.printStackTrace();  //To change
            } catch (UnexpectedErrorException_Exception e1) {
                e1.printStackTrace();  //To change
            }
            System.out.println("User created");
        }


        byte[] encoded = Files.readAllBytes(Paths.get("C:/tmp/email.htm"));
        String body = new String(encoded, "UTF-8");

        MessageContent messageContent = new MessageContent();

        // adding message body
        Attribute attribute = new Attribute();
        attribute.setName("MSG_BODY");
        attribute.setValue(body);
        messageContent.getParameters().add(attribute);

        // adding attachments
        if (m_attachments.size() > 10)
        {
            for (String attach_item : m_attachments)
            {
                Attachment attachment = new Attachment();
                attachment.setName(new File(attach_item).getName() );
                attachment.setContentType("text/binary");
                byte[] atta_bytes = Files.readAllBytes(Paths.get(attach_item));
                byte[] atta_encoded = Base64.encodeBase64(atta_bytes);
                String printMe = new String(atta_encoded, "US-ASCII");
                attachment.setContent(printMe);
                messageContent.getAttachments().add(attachment);
            }
        }

        ecmService.messageSendTransactional(DRAFT_MESSAGE_ID, "6453820", new_user.getId(), messageContent);
//        Thread.sleep(500);
//        List<AsyncResult> resultList = ecmService.getEcmWSPort().asyncPoll("response", 100);
//
//        for (AsyncResult asyncResult : resultList)
//        {
//            System.out.println("Result on Queue Id " + asyncResult.getQueueId());
//            List<Attribute> attributes = asyncResult.getOutput();
//            if (attributes != null){
//                for (Attribute att: attributes)
//                {
//                    System.out.println("   " + att.getName() + ": " + att.getValue());
//                }
//            }
//        }

//        try {
//            String sessionID = ecmPort.logon(realm, user, password);
//            System.out.printf("Session ID: %s", sessionID);
//            String messageID = DRAFT_MESSAGE_ID; // runtime
//            // Checking if prepared message exists and has correct format
//            MessageContent content = ecmPort.lookupMessageById(sessionID, messageID);
//            if (!content.getSubject().contains("<%parameter.SUBJECT%>")) {
//                throw new RuntimeException("Invalid prepared message subject format");
//            }
//            if (!content.getHtml().contains("<%parameter.MSG_BODY%>")) {
//                throw new RuntimeException("Invalid prepared message body format");
//            }
//            //
//            String userId = ecmPort.lookupUserIdByEmail(sessionID, "kremor@gmail.com"); // runtime: recipient
//            if (userId.isEmpty()) {
//                // ToDo: Throw exception or create a new user
//            }
//
//            // Preparing parameter for sendParametrizedSingleMessageToUser method
//            List<String> names = new ArrayList<String>();
//            List<String> values = new ArrayList<String>();
//
//            // Reading e-mail from file
//            // ToDo: use byte array received from StreamServe
//            byte[] encoded = Files.readAllBytes(Paths.get("C:/tmp/email.htm"));
//            String body = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
//
//            names.add("SUBJECT");
//            values.add("Test E-Mail from Java connector"); // runtime option: Subject
//
//            names.add("MSG_BODY");
//            values.add("Message body 1053am");
//
//            // Sending email
//
//            ecmPort.sendParametrizedSingleMessageToUser(sessionID, messageID, userId, names, values);
//
//
//            // Logout
//            ecmPort.logout(sessionID);
//
//        } catch (EcMException_Exception e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }

    }
}

