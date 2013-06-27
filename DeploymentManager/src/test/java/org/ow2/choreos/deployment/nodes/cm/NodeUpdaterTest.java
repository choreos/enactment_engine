package org.ow2.choreos.deployment.nodes.cm;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.ow2.choreos.nodes.datamodel.CloudNode;
import org.ow2.choreos.utils.SshNotConnected;
import org.ow2.choreos.utils.SshUtil;
import org.ow2.choreos.utils.SshWaiter;

public class NodeUpdaterTest {

    private CloudNode node;
    private SshUtil ssh;
    private SshWaiter waiter;
    
    @Before
    public void setUp() throws Exception {
        setNode();
        setSsh();
    }

    private void setNode() {
        node = new CloudNode();
        node.setIp("192.168.56.101");
        node.setUser("ubuntu");
        node.setPrivateKey("ubuntu.pem");
    }
    
    private void setSsh() throws SshNotConnected {
        ssh = mock(SshUtil.class);
        waiter = mock(SshWaiter.class);
        when(waiter.waitSsh(anyString(), anyString(), anyString(), anyInt())).thenReturn(ssh);
    }
    
    @Test
    public void test() throws Exception {
        NodeUpdater updater = new NodeUpdater();
        updater.sshWaiter = waiter;
        updater.update(node);
        verify(ssh).runCommand(NodeUpdater.CHEF_SOLO_COMMAND);
    }

}
