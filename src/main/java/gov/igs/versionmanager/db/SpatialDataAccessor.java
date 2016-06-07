package gov.igs.versionmanager.db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.igs.versionmanager.model.Job;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.GeometryExceptionWithContext;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

@Component
public class SpatialDataAccessor {

	@Autowired
	private JobDataAccessor jda;

	private static final String[] GEOM_TABLENAMES = { "TDSUTILITYINFRASTRUCTUREPOINT", "TDSUTILITYINFRASTRUCTURECURVE",
			"TDSTRANSPORTATIONGROUNDPOINT", /*"TDSSTRUCTUREPOINT", */"TDSSTRUCTURECURVE", "TDSSTORAGEPOINT",
			"TDSRECREATIONPOINT", "TDSRECREATIONCURVE", "TDSCULTUREPOINT", "TDSAERONAUTICPOINT" };

	@PostConstruct
	public void init() throws SQLException {
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
	}

	public void createWorkspace(Long jobid, BigDecimal lat, BigDecimal lon) throws SQLException {
		String jobidStr = Long.toString(jobid);
		// DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

		// Calls the “CreateWorkspace” procedure with a generated UUID as the
		// name.
		Connection conn1 = getConnection();
		CallableStatement pstmt1 = conn1.prepareCall("{call dbms_wm.createworkspace(?)}");
		pstmt1.setString(1, jobidStr);
		pstmt1.execute();
		pstmt1.close();
		conn1.close();
//
//		List<Integer> bbox = getBBox(lat, lon);
//
//		// Calls the “LockRows” for the features in the provided spatial
//		// area, so that only the current workspace can modify these features.
//		Connection conn2 = getConnection();
//		CallableStatement pstmt2 = conn2.prepareCall(
//				"{call dbms_wm.lockrows(workspace => ?, table_name => 'TDS_TOPO', lock_mode => 'E', Xmin => ?, Ymin => ?, Xmax => ?, Ymax => ?)}");
//		pstmt2.setString(1, jobidStr);
//		pstmt2.setInt(2, bbox.get(0));
//		pstmt2.setInt(3, bbox.get(1));
//		pstmt2.setInt(4, bbox.get(2));
//		pstmt2.setInt(5, bbox.get(3));
//		pstmt2.execute();
//		pstmt2.close();
//		conn2.close();
	}

	public File exportWorkspace(String jobid, String user)
			throws SQLException, IOException, GeometryExceptionWithContext {

		Job job = jda.getJob(jobid);
		List<Integer> bbox = getBBox(job.getLatitude(), job.getLongitude());
		Connection conn = getConnection();

		List<List<String>> listOfTables = new ArrayList<List<String>>();

		for (String tableName : GEOM_TABLENAMES) {
			String query = "select * from " + tableName
					+ " where sdo_filter(geom, sdo_geometry(2003, null, null, sdo_elem_info_array(1, 1003, 3), sdo_ordinate_array("
					+ bbox.get(0) + "," + bbox.get(1) + "," + bbox.get(2) + "," + bbox.get(3) + "))) = 'TRUE'";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			listOfTables.add(formTable(tableName, rs));
		}

		conn.close();

		File fileToReturn = createDumpFile(job.getJobid(), listOfTables);

		jda.updateJobToExported(user, jobid, countNumFeatures(listOfTables), countUniqueFeatureClasses(listOfTables));

		return fileToReturn;
	}

	public void removeWorkspace(String jobid) throws SQLException {
		Connection conn = getConnection();

		// Calls the “RemoveWorkspace” procedure with a generated UUID as the
		// name.
		CallableStatement pstmt1 = conn.prepareCall("{call dbms_wm.removeworkspace(?)}");
		pstmt1.setString(1, jobid);
		pstmt1.execute();
		pstmt1.close();

		conn.close();
	}

	private List<Integer> getBBox(BigDecimal lat, BigDecimal lon) {
		List<Integer> bbox = new ArrayList<Integer>();

		bbox.add(lon.setScale(0, RoundingMode.FLOOR).intValue()); // xmin
		bbox.add(lat.setScale(0, RoundingMode.FLOOR).intValue()); // ymin
		bbox.add(lon.setScale(0, RoundingMode.CEILING).intValue()); // xmax
		bbox.add(lat.setScale(0, RoundingMode.CEILING).intValue()); // ymax

		return bbox;
	}

	private int countNumFeatures(List<List<String>> listOfTables) {
		int recordCount = 0;

		for (List<String> table : listOfTables) {
			for (String row : table) {
				recordCount++;
			}

			recordCount -= 2; // To account for the table name and column header
								// rows.
		}

		return recordCount;
	}

	private int countUniqueFeatureClasses(List<List<String>> listOfTables) {
		List<String> featureClasses = new ArrayList<String>();

		for (List<String> table : listOfTables) {
			for (int i = 0; i < table.size(); i++) {
				String row = table.get(i);

				if (row.contains(",") && i > 1) {
					String[] cols = row.split(",");
					if (cols.length > 2 && !featureClasses.contains(cols[2])) {
						featureClasses.add(cols[2]);
					}
				}
			}
		}

		return featureClasses.size();
	}

	private List<String> formTable(String tableName, ResultSet rs)
			throws SQLException, GeometryExceptionWithContext, UnsupportedEncodingException {
		if (rs == null)
			return null;

		List<String> table = new ArrayList<String>();

		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int NumOfCol = rsmd.getColumnCount();

			StringBuilder tableHeaders = new StringBuilder();
			for (int h = 1; h <= NumOfCol; h++) {
				tableHeaders.append(rsmd.getColumnName(h).toUpperCase());
				if (h < NumOfCol) {
					tableHeaders.append(",");
				}
			}

			table.add(tableName.toUpperCase());
			table.add(tableHeaders.toString());

			while (rs.next()) {
				StringBuilder currentRow = new StringBuilder();

				for (int i = 1; i <= NumOfCol; i++) {
					if (rsmd.getColumnTypeName(i).equals("MDSYS.SDO_GEOMETRY")) {
						JGeometry jGeom = JGeometry.load((STRUCT) rs.getObject(i));
						WKT wkt = new WKT();
						currentRow.append(new String(wkt.fromJGeometry(jGeom), "UTF-8"));
					} else {
						currentRow.append(rs.getObject(i));
					}

					if (i < NumOfCol) {
						currentRow.append(",");
					}
				}

				table.add(currentRow.toString());
			}
		} catch (SQLException e) {
			throw e;
		}

		return table;
	}

	private File createDumpFile(Long jobid, List<List<String>> listOfTables) throws IOException {
		File fileToReturn = new File("D:/" + Long.toString(jobid) + ".dump");
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileToReturn));

		for (List<String> table : listOfTables) {

			for (String row : table) {
				writer.write(row);
				writer.newLine();
			}
			writer.newLine();
			writer.newLine();
		}

		writer.close();

		return fileToReturn;
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:oracle:thin:@54.152.233.204:1521:ORCL", "versionmanager", "Password1");
	}
}