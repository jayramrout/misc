package info._7chapters.utils.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;

public class ReadEmail {
    public static void main(String[] args) {

        ReadEmail gmail = new ReadEmail();
        gmail.read();

    }

    public void read() {

        Properties props = new Properties();

        try {

            props.load(ReadEmail.class.getClassLoader().getResourceAsStream("smtp.properties"));

            Session session = Session.getDefaultInstance(props, null);

            Store store = session.getStore("imaps");
            store.connect("smtp.gmail.com", "", "");

            Folder inbox = store.getFolder("SPAM");

            inbox.open(Folder.READ_ONLY);
            int messageCount = inbox.getMessageCount();

            System.out.println("Total Messages:- " + messageCount);

            Message[] messages = inbox.getMessages();
            System.out.println("------------------------------");

            for (int i = 0; i < 10; i++) {
                System.out.println("Mail Subject:- " + messages[i].getSubject());
            }

            inbox.close(true);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
