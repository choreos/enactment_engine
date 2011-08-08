package br.usp.ime.choreos.nodepoolmanager.utils;

import org.apache.geronimo.mail.util.StringBufferOutputStream;

import br.usp.ime.choreos.nodepoolmanager.Configuration;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SshUtil {

    private final String hostname;

    public SshUtil(String hostname) {
        this.hostname = hostname;
    }

    public String runCommand(String command) throws Exception {
        JSch jsch = new JSch();

        String user = "ubuntu";

        Session session = jsch.getSession(user, hostname);
        JSch.setConfig("StrictHostKeyChecking", "no");
        jsch.addIdentity(Configuration.get("AMAZON_SSH_IDENTITY"));

        session.connect(3000);

        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);
        StringBuffer sb = new StringBuffer();
        channel.setOutputStream(new StringBufferOutputStream(sb));

        channel.connect();

        while (!channel.isClosed()) {
            Thread.sleep(10);
        }

        channel.disconnect();
        session.disconnect();
        return sb.toString();
    }

}
