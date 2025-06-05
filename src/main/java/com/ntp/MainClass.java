package com.ntp;

import java.awt.Dimension;
import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class MainClass  extends JPanel {
    
     JTable jt;
    
    public static void main(String[] args) {                   
        MainClass mainClass = new MainClass();
        mainClass.getFolderSize();
    }
    
    public void getFolderSize() {
        File file = new File("/home");
        long totalSpace = file.getTotalSpace(); //total disk space in bytes.
        long usableSpace = file.getUsableSpace(); ///unallocated / free disk space in bytes.
        long freeSpace = file.getFreeSpace(); //unallocated / free disk space in bytes.

        long homeFreeSpaceGB = freeSpace / 1024 / 1024 / 1024;
        long totalSpaceGB = totalSpace / 1024 / 1024 / 1024;
        long percentFree = (freeSpace * 100) / totalSpace;
        
        String smtpHostServer = "0.0.0.0";
        String emailID = "bcd@mail.com";
      
        Properties props = System.getProperties();

        props.put("mail.smtp.host", smtpHostServer);

        Session session = Session.getInstance(props, null);
      
        sendEmail(session, emailID,
    "Alfresco Server /home free space: " + percentFree + "%",  // subject
    "Alfresco Server /home free space is currently " + homeFreeSpaceGB + " GB out of " + totalSpaceGB + " GB (" + percentFree + "% free).");  // body

    }
    
    public void sendEmail(Session session, String toEmail, String subject, String body){
        try
        {
          MimeMessage msg = new MimeMessage(session);
         
         
        //set message headers
          msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
          msg.addHeader("format", "flowed");
          msg.addHeader("Content-Transfer-Encoding", "8bit");

          msg.setFrom(new InternetAddress("no_reply@example.com", "NoReply-JD"));

          msg.setReplyTo(InternetAddress.parse("no_reply@example.com", false));

          msg.setSubject(subject, "UTF-8");

          msg.setText(body, "UTF-8");

          msg.setSentDate(new Date());

          msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
          msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse("abc@mail.com", false));
         
          Transport.send(msg);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
    }
}