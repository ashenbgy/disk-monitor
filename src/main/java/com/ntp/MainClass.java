package com.ntp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JPanel;

public class MainClass extends JPanel {

    public static void main(String[] args) {
        MainClass mainClass = new MainClass();

        String deletedBackup = mainClass.deleteOldestBackup("/home/user/Documents");
        String diskStatus = mainClass.getFolderSizeInfo();

        StringBuilder emailBody = new StringBuilder();

        if (deletedBackup != null) {
            emailBody.append("Deleted oldest backup folder: ").append(deletedBackup).append("\n\n");
        } else {
            emailBody.append("No backup was deleted.\n\n");
        }

        emailBody.append(diskStatus);

        String smtpHostServer = "192.168.150.25";
        String emailID = "nadeesha.pallewatta@drivegreen.lk";

        Properties props = System.getProperties();
        props.put("mail.smtp.host", smtpHostServer);

        Session session = Session.getInstance(props, null);

        mainClass.sendEmail(session, emailID,
                "Alfresco Server Report: Disk Monitoring & Backup Cleanup",
                emailBody.toString());
    }

    public String getFolderSizeInfo() {
        File file = new File("/home");
        long totalSpace = file.getTotalSpace();
        long freeSpace = file.getFreeSpace();
        long usedSpace = totalSpace - freeSpace;

        long homeFreeSpaceGB = freeSpace / 1024 / 1024 / 1024;
        long totalSpaceGB = totalSpace / 1024 / 1024 / 1024;
        long usedSpaceGB = usedSpace / 1024 / 1024 / 1024;

        long percentFree = (freeSpace * 100) / totalSpace;
        long percentUsed = 100 - percentFree;

        return String.format(
            "Alfresco Server /home disk usage:\nTotal: %d GB\nUsed: %d GB (%d%%)\nFree: %d GB (%d%%)",
            totalSpaceGB, usedSpaceGB, percentUsed, homeFreeSpaceGB, percentFree);
    }

    public String deleteOldestBackup(String backupDirPath) {
        File backupDir = new File(backupDirPath);

        File[] directories = backupDir.listFiles(file ->
            file.isDirectory() && file.getName().matches("AlfrescoBackup\\d{4}_\\d{2}_\\d{2}_\\d{2}_\\d{2}")
        );

        if (directories == null || directories.length == 0) {
            System.out.println("No backup directories found in: " + backupDirPath);
            return null;
        }

        File oldestBackup = null;
        FileTime oldestTime = null;

        for (File dir : directories) {
            try {
                FileTime fileTime = Files.readAttributes(dir.toPath(), BasicFileAttributes.class).creationTime();
                if (oldestTime == null || fileTime.compareTo(oldestTime) < 0) {
                    oldestTime = fileTime;
                    oldestBackup = dir;
                }
            } catch (IOException e) {
                System.err.println("Could not read attributes for: " + dir.getName());
            }
        }

        if (oldestBackup != null) {
            long sizeInBytes = getDirectorySize(oldestBackup);
            boolean deleted = deleteDirectoryRecursively(oldestBackup);

            if (deleted) {
                long sizeInMB = sizeInBytes / 1024 / 1024;
                System.out.println("Deleted oldest backup: " + oldestBackup.getName() + " (Size: " + sizeInMB + " MB)");
                return oldestBackup.getName() + " (Size: " + sizeInMB + " MB)";
            } else {
                System.out.println("Failed to delete oldest backup.");
            }
        }

        return null;
    }

    public long getDirectorySize(File dir) {
        if (dir.isFile()) return dir.length();
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                size += getDirectorySize(file);
            }
        }
        return size;
    }

    public boolean deleteDirectoryRecursively(File file) {
        boolean success = true;

        if (file.isDirectory()) {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File child : contents) {
                    success &= deleteDirectoryRecursively(child);
                }
            }
        }
        return success && file.delete();
    }

    public void sendEmail(Session session, String toEmail, String subject, String body) {
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.addHeader("Content-type", "text/plain; charset=UTF-8");
            msg.setFrom(new InternetAddress("no_reply@example.com", "NoReply-JD"));
            msg.setReplyTo(InternetAddress.parse("no_reply@example.com", false));
            msg.setSubject(subject, "UTF-8");
            msg.setText(body, "UTF-8");
            msg.setSentDate(new Date());
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));

            msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse("testmail@tester.com", false));

            Transport.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}