package eu.choreos.monitoring.platform.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.choreos.monitoring.platform.daemon.datatypes.Gmetric;
import eu.choreos.monitoring.platform.exception.GangliaException;

public class GmondDataReader {

		private String host;
	private int port;

	private Map<String, Gmetric> metrics;
	private Socket socket;

	public GmondDataReader(String host, int port) {
		this.host = host;
		this.port = port;
		metrics = new HashMap<String, Gmetric>();
	}

	public Map<String, Gmetric> getAllMetrics() throws GangliaException {
		setMetrics(getGangliaCurrentMetrics());
		return metrics;
	}

	private void setMetrics(Document gangliaXML) {
		gangliaXML.getDocumentElement().normalize();
		NodeList clusterNodeList = gangliaXML.getElementsByTagName("CLUSTER");
		Element clusterNode = (Element) clusterNodeList.item(0);

		NodeList hostNodeList = clusterNode.getElementsByTagName("HOST");
		Element hostNode = (Element) hostNodeList.item(0);

		NodeList metricNodeList = hostNode.getElementsByTagName("METRIC");

		for (int i = 0; i < metricNodeList.getLength(); i++) {

			Element el = (Element) metricNodeList.item(i);

			metrics.put(el.getAttributeNode("NAME").getNodeValue(),
					new Gmetric(el.getAttributeNode("NAME").getNodeValue(), el
							.getAttributeNode("VAL").getNodeValue()));
		}
	}

	private Document getGangliaCurrentMetrics() throws GangliaException {
		try {
			if (socket == null)
				createSocket();
			else if (socket.isClosed())
				createSocket();
		} catch (UnknownHostException e) {
			throw new GangliaException(GangliaException.CONNECTION_ERROR_UNKNOWN_HOST);
		} catch (IOException e) {
			throw new GangliaException(
					GangliaException.CONNECTION_ERROR_COULD_NOT_READ_FROM_SOCKET);
		}
		Document dom = getGangliaMetricsFromSocket(socket);
		closeSocket(socket);
		return dom;

	}

	private Document getGangliaMetricsFromSocket(Socket socket)
			throws GangliaException {
		InputStream in = getStreamFromSocket(socket);
		Document dom = convertToDomDocument(in);
		closeInputStream(in);
		return dom;
	}

	private void closeSocket(Socket socket) throws GangliaException {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException ex) {
				throw new GangliaException(
						GangliaException.CONNECTION_ERROR_COULD_NOT_CLOSE_SOCKET);
			}
		}
	}

	public Document convertToDomDocument(InputStream in) throws GangliaException {
		Document dom = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			dom = db.parse(in);
		} catch (ParserConfigurationException e) {
			throw new GangliaException(
					GangliaException.CONNECTION_ERROR_COULD_NOT_PARSE_METRICS_INPUT);

		} catch (SAXException e) {
			throw new GangliaException(
					GangliaException.CONNECTION_ERROR_COULD_NOT_PARSE_METRICS_INPUT);
		} catch (IOException e) {
			throw new GangliaException(
					GangliaException.CONNECTION_ERROR_COULD_NOT_READ_FROM_SOCKET);
		}

		return dom;

	}

	private void closeInputStream(InputStream in) throws GangliaException {

		if (in != null) {
			try {
				in.close();
			} catch (IOException e1) {
				throw new GangliaException(
						GangliaException.CONNECTION_ERROR_COULD_NOT_CLOSE_SOCKET);
			}
		}
	}

	private InputStream getStreamFromSocket(Socket socket)
			throws GangliaException {
		InputStream in = null;
		try {
			in = socket.getInputStream();
		} catch (IOException e) {
			throw new GangliaException(
					GangliaException.CONNECTION_ERROR_COULD_NOT_READ_FROM_SOCKET);
		}
		return in;
	}

	private void createSocket() throws UnknownHostException, IOException {
		socket = new Socket(InetAddress.getByName(host), port);
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}
}
