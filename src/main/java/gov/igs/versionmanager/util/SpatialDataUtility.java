package gov.igs.versionmanager.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.GML2;
import oracle.spatial.util.GeometryExceptionWithContext;
import oracle.sql.STRUCT;

@SuppressWarnings("deprecation")
@Component
public class SpatialDataUtility {

	public List<Integer> getBBox(String cid) {
		Integer[] latLon = parseCidForLatLon(cid);
		List<Integer> bbox = new ArrayList<Integer>();

		bbox.add(latLon[1]); // xmin
		bbox.add(latLon[0]); // ymin
		bbox.add(latLon[1] + 1); // xmax
		bbox.add(latLon[0] + 1); // ymax

		return bbox;
	}

	private Integer[] parseCidForLatLon(String cid) {
		Integer lat = null, lon = null;

		if (cid.contains("N")) {
			lat = Integer.parseInt(cid.substring(0, cid.indexOf("N")));

			if (cid.endsWith("E")) {
				lon = Integer.parseInt(cid.substring(cid.indexOf("N") + 1, cid.length() - 1));
			} else if (cid.endsWith("W")) {
				lon = Integer.parseInt(cid.substring(cid.indexOf("N") + 1, (cid.length() - 1))) * -1;
			}
		} else if (cid.contains("S")) {
			lat = Integer.parseInt(cid.substring(0, cid.indexOf("S"))) * -1;

			if (cid.endsWith("E")) {
				lon = Integer.parseInt(cid.substring(cid.indexOf("S") + 1, cid.length() - 1));
			} else if (cid.endsWith("W")) {
				lon = Integer.parseInt(cid.substring(cid.indexOf("S") + 1, (cid.length() - 1))) * -1;
			}
		}

		return new Integer[] { lat, lon };
	}

	public int countNumFeatures(List<List<String>> listOfTables) {
		int recordCount = 0;

		for (List<String> table : listOfTables) {
			for (String row : table) {
				if (row.contains("<gml:featureMember>")) {
					recordCount++;
				}
			}
		}

		return recordCount;
	}

	public int countUniqueFeatureClasses(List<List<String>> listOfTables) {
		Set<String> featureClasses = new HashSet<String>();

		for (List<String> table : listOfTables) {
			for (String row : table) {
				if (row.contains("<F_CODE>")) {
					featureClasses.add(row);
				}
			}
		}

		return featureClasses.size();
	}

	public List<String> formTableForGMLFile(String tableName, ResultSet rs)
			throws SQLException, GeometryExceptionWithContext, IOException {
		if (rs == null)
			return null;

		List<String> table = new ArrayList<String>();

		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int NumOfCol = rsmd.getColumnCount();

			for (int counter = 0; rs.next(); counter++) {
				table.add("  <gml:featureMember>");
				table.add("    <" + tableName.toLowerCase() + " fid=\"" + Integer.toHexString(counter) + "\">");

				for (int i = 1; i <= NumOfCol; i++) {
					if (rsmd.getColumnTypeName(i).equals("MDSYS.SDO_GEOMETRY")) {
						table.add("      <geometryProperty>"
								+ GML2.to_GMLGeometry(JGeometry.load((STRUCT) rs.getObject(i)))
								+ "</geometryProperty>");

					} else {
						table.add("      <" + rsmd.getColumnName(i).toUpperCase() + ">" + rs.getObject(i) + "</"
								+ rsmd.getColumnName(i).toUpperCase() + ">");
					}
				}

				table.add("    </" + tableName.toLowerCase() + ">");
				table.add("  </gml:featureMember>");
			}
		} catch (SQLException e) {
			throw e;
		}

		return table;
	}

	public File createGMLFile(String jobid, List<List<String>> listOfTables) throws IOException {
		File fileToReturn = new File("D:/" + jobid + ".gml");
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileToReturn));

		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.newLine();
		writer.write(
				"<FeatureCollection xmlns:xsi=\"http://www.w3c.org/2001/XMLSchema-instance\" xmlns:gml=\"http://www.opengis.net/gml\">");
		writer.newLine();

		for (List<String> table : listOfTables) {
			for (String row : table) {
				writer.write(row);
				writer.newLine();
			}
		}

		writer.write("</FeatureCollection>");
		writer.newLine();
		writer.close();

		return fileToReturn;
	}

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:oracle:thin:@54.172.145.108:1521:ORCL", "versionmanager", "Password1");
	}

	// public List<String> formTableForDumpFile(String tableName, ResultSet rs)
	// throws SQLException, GeometryExceptionWithContext,
	// UnsupportedEncodingException {
	// if (rs == null)
	// return null;
	//
	// List<String> table = new ArrayList<String>();
	//
	// try {
	// ResultSetMetaData rsmd = rs.getMetaData();
	// int NumOfCol = rsmd.getColumnCount();
	//
	// StringBuilder tableHeaders = new StringBuilder();
	// for (int h = 1; h <= NumOfCol; h++) {
	// tableHeaders.append(rsmd.getColumnName(h).toUpperCase());
	// if (h < NumOfCol) {
	// tableHeaders.append(",");
	// }
	// }
	//
	// table.add(tableName.toUpperCase());
	// table.add(tableHeaders.toString());
	//
	// while (rs.next()) {
	// StringBuilder currentRow = new StringBuilder();
	//
	// for (int i = 1; i <= NumOfCol; i++) {
	// if (rsmd.getColumnTypeName(i).equals("MDSYS.SDO_GEOMETRY")) {
	// JGeometry jGeom = JGeometry.load((STRUCT) rs.getObject(i));
	// WKT wkt = new WKT();
	// currentRow.append(new String(wkt.fromJGeometry(jGeom), "UTF-8"));
	// } else {
	// currentRow.append(rs.getObject(i));
	// }
	//
	// if (i < NumOfCol) {
	// currentRow.append(",");
	// }
	// }
	//
	// table.add(currentRow.toString());
	// }
	// } catch (SQLException e) {
	// throw e;
	// }
	//
	// return table;
	// }
	//
	// public File createDumpFile(Long jobid, List<List<String>> listOfTables)
	// throws IOException {
	// File fileToReturn = new File("D:/" + Long.toString(jobid) + ".dump");
	// BufferedWriter writer = new BufferedWriter(new FileWriter(fileToReturn));
	//
	// for (List<String> table : listOfTables) {
	//
	// for (String row : table) {
	// writer.write(row);
	// writer.newLine();
	// }
	// writer.newLine();
	// writer.newLine();
	// }
	//
	// writer.close();
	//
	// return fileToReturn;
	// }
}