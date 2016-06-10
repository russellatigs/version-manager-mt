package gov.igs.versionmanager.util;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.GML;
import oracle.sql.STRUCT;

@Component
public class GMLHandler extends DefaultHandler {

	@Autowired
	private SpatialDataUtility sdUtil;

	Map<String, String> fields = null;
	String currElement = null;
	String currTable = null;
	boolean startElement = false;
	boolean geom = false;
	StringBuilder geomSB = null;
	STRUCT jGeom = null;
	PreparedStatement ps = null;
	int numFeatures = 0;
	Set<String> featureClasses = new HashSet<String>();

	public int getNumFeatures() {
		return numFeatures;
	}

	public int getNumUniqueFeatureClasses() {
		return featureClasses.size();
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (qName.equalsIgnoreCase("FeatureCollection") || qName.equalsIgnoreCase("gml:featureMember")) {
			// do nothing
		} else if (atts.getLength() == 1) {
			numFeatures++;
			fields = new HashMap<String, String>();
			currTable = qName;
		} else if (qName.equalsIgnoreCase("geometryProperty")) {
			geom = true;
			geomSB = new StringBuilder();
		} else if (geom) {
			geomSB.append("<" + qName);
			for (int i = 0; i < atts.getLength(); i++) {
				geomSB.append(" " + atts.getQName(i) + "=\"" + atts.getValue(i) + "\"");
			}
			geomSB.append(">");
		} else {
			startElement = true;
			currElement = qName.toUpperCase();
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		String elementValue = new String(ch, start, length);

		if (startElement) {
			fields.put(currElement, elementValue);

			if (currElement.equals("F_CODE")) {
				featureClasses.add(elementValue);
			}
		} else if (geom) {
			geomSB.append(elementValue);
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		startElement = false;

		if (qName.equalsIgnoreCase(currTable)) {
			try {
				Connection conn = sdUtil.getConnection();
				StringBuilder sb = new StringBuilder();
				sb.append("UPDATE ");
				sb.append(currTable);
				sb.append(" SET ");

				int counter = 1;

				for (Map.Entry<String, String> entry : fields.entrySet()) {
					if (entry.getKey().equals("geometryProperty")) {
						Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
								.parse(new InputSource(new ByteArrayInputStream(entry.getValue().getBytes("UTF-8"))));
						sb.append("geom=? ");
						jGeom = JGeometry.store(GML.fromNodeToGeometry(doc.getDocumentElement()), conn);
					} else {
						sb.append(entry.getKey());
						sb.append("='");
						sb.append(entry.getValue());
						sb.append("'");
					}
					if (counter < fields.size()) {
						sb.append(", ");
					}
					counter++;
				}

				sb.append(" WHERE GLOBALID='");
				sb.append(fields.get("GLOBALID"));
				sb.append("'");

				ps = conn.prepareStatement(sb.toString());
				ps.setObject(1, jGeom);
				ps.executeUpdate();
				ps.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			currElement = null;
			currTable = null;
			fields = null;
		} else if (qName.equalsIgnoreCase("geometryProperty")) {
			System.out.println("Setting geom: " + geomSB.toString());
			fields.put(qName, geomSB.toString());

			geom = false;
			geomSB = new StringBuilder();
		} else if (geom) {
			geomSB.append("</" + qName + ">");
		}
	}
}
