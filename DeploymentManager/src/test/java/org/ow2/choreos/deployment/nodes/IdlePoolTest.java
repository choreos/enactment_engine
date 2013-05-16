package org.ow2.choreos.deployment.nodes;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.ow2.choreos.deployment.nodes.cloudprovider.CloudProvider;
import org.ow2.choreos.nodes.NodeNotCreatedException;
import org.ow2.choreos.nodes.datamodel.Node;
import org.ow2.choreos.services.datamodel.ResourceImpact;
import org.ow2.choreos.utils.LogConfigurator;

public class IdlePoolTest {

	private CloudProvider cp;
	
	@BeforeClass
	public static void setUpClass() {
		LogConfigurator.configLog();
	}
	
	@Before
	public void setUp() throws NodeNotCreatedException {
	
		int N = 10;
		cp = mock(CloudProvider.class);
		OngoingStubbing<Node> ongoingStubbing = 
				when(cp.createNode(any(Node.class), any(ResourceImpact.class)));
		for (int i=0; i<N; i++) {
			Node node = new Node();
			node.setId("node" + i);
			ongoingStubbing = ongoingStubbing.thenReturn(node);
		}
	}
	
	@Test
	public void shouldCreateExtraVMs() throws InterruptedException {
		
		int N = 3;
		IdlePool pool = IdlePool.getInstance(N, cp);
		int howManyVMs = N;
		pool.createExtraVMs(howManyVMs);
		
		Thread.sleep(100);
		
		Set<Node> idlePool = pool.getIdleNodes();
		assertEquals(howManyVMs, idlePool.size());
	}
	
	@Test
	public void shouldFillThePool() throws InterruptedException {
		
		int N = 3;
		IdlePool pool = IdlePool.getCleanInstance(N, cp);
		pool.createExtraVMs(1);
		Thread.sleep(100);
		pool.fillPool();
		Thread.sleep(100);
		
		Set<Node> idlePool = pool.getIdleNodes();
		assertEquals(N, idlePool.size());
	}
	
	@Test
	public void shouldFillThePoolConcurrently() throws InterruptedException {
		
		int N = 5;
		IdlePool pool = IdlePool.getCleanInstance(N, cp);
		pool.createExtraVMs(1);
		Thread.sleep(100);
		for (int i=0; i<3; i++) {
			PoolFiller filler = new PoolFiller(pool);
			Thread thrd = new Thread(filler);
			thrd.start();
		}
		
		Thread.sleep(100);
		
		Set<Node> idlePool = pool.getIdleNodes();
		assertEquals(N, idlePool.size());
	}
	
	@Test
	public void multipleClientsShouldNotRetrieveTheSameNode() throws InterruptedException {
		
		int N = 5;
		IdlePool pool = IdlePool.getCleanInstance(N, cp);
		pool.fillPool();
		Thread.sleep(100);
		List<PoolClient> clients = new ArrayList<PoolClient>();
		for (int i=0; i<3; i++) {
			PoolClient client = new PoolClient(pool);
			clients.add(client);
			Thread thrd = new Thread(client);
			thrd.start();
		}
		
		Thread.sleep(200);
		
		Iterator<PoolClient> it = clients.iterator();
		Node previousNode = it.next().retrievedNode;
		assertNotNull(previousNode);
		while (it.hasNext()) {
			Node node = it.next().retrievedNode;
			assertNotNull(node);
			assertNotSame(previousNode, node);
			previousNode = node;
		}
	}
	
	@Test
	public void multipleRequestsShouldLeaveThePoolFull() throws InterruptedException {

		int N = 5;
		IdlePool pool = IdlePool.getCleanInstance(N, cp);
		pool.fillPool();
		Thread.sleep(100);
		for (int i=0; i<3; i++) {
			PoolConsumerAndFiller client = new PoolConsumerAndFiller(pool);
			Thread thrd = new Thread(client);
			thrd.start();
		}

		Thread.sleep(300);
		
		int poolSize = pool.getIdleNodes().size();
		assertEquals(N, poolSize);
	}
	
	@Test
	public void shouldRetrieveANodeEvenWithAnEmptyPool() {
		
		int N = 5;
		IdlePool pool = IdlePool.getCleanInstance(N, cp);
		Node node = pool.retriveNode();
		assertNotNull(node);
		assertNotNull(node.getId());
		assertFalse(node.getId().isEmpty());
	}
	
	@Test
	public void multipleClientsShouldRetrieveNodesEvenWithAnEmptyPool() throws InterruptedException {
		
		int N = 5;
		IdlePool pool = IdlePool.getCleanInstance(N, cp);
		List<PoolClient> clients = new ArrayList<PoolClient>();
		for (int i=0; i<3; i++) {
			PoolClient client = new PoolClient(pool);
			clients.add(client);
			Thread thrd = new Thread(client);
			thrd.start();
		}
		
		Thread.sleep(200);
		
		Iterator<PoolClient> it = clients.iterator();
		Node previousNode = it.next().retrievedNode;
		assertNotNull(previousNode);
		while (it.hasNext()) {
			Node node = it.next().retrievedNode;
			assertNotNull(node);
			assertNotSame(previousNode, node);
			previousNode = node;
		}
	}

	
	private class PoolFiller implements Runnable {
		
		IdlePool pool;

		public PoolFiller(IdlePool pool) {
			this.pool = pool;
		}
		
		@Override
		public void run() {
			this.pool.fillPool();
		}
	}
	
	private class PoolClient implements Runnable {
		
		IdlePool pool;
		Node retrievedNode;

		public PoolClient(IdlePool pool) {
			this.pool = pool;
		}
		
		@Override
		public void run() {
			this.retrievedNode = pool.retriveNode();
		}
	}

	private class PoolConsumerAndFiller implements Runnable {
		
		IdlePool pool;

		public PoolConsumerAndFiller(IdlePool pool) {
			this.pool = pool;
		}
		
		@Override
		public void run() {
			pool.retriveNode();
			pool.fillPool();
		}
	}

}
