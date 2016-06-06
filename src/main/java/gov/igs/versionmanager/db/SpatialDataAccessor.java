package gov.igs.versionmanager.db;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
//import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import gov.igs.versionmanager.model.Job;

@Component
public class SpatialDataAccessor {

	private static final String[] GEOM_TABLENAMES = { "TDSUTILITYINFRASTRUCTUREPOINT", "TDSUTILITYINFRASTRUCTURECURVE",
			"TDSTRANSPORTATIONGROUNDPOINT", "TDSSTRUCTUREPOINT", "TDSSTRUCTURECURVE", "TDSSTORAGEPOINT",
			"TDSRECREATIONPOINT", "TDSRECREATIONCURVE", "TDSCULTUREPOINT", "TDSAERONAUTICPOINT" };

	@PostConstruct
	public void init() throws SQLException {
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
	}

	public void createWorkspace(Long jobid, BigDecimal lat, BigDecimal lon) throws SQLException {
		String jobidStr = Long.toString(jobid);
		// DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
		Connection conn = getConnection();

		// Calls the “CreateWorkspace” procedure with a generated UUID as the
		// name.
		CallableStatement pstmt1 = conn.prepareCall("{call dbms_wm.createworkspace(?)}");
		pstmt1.setString(1, jobidStr);
		pstmt1.execute();
		pstmt1.close();

		List<Integer> bbox = getBBox(lat, lon);

		// Calls the “LockRows” for the features in the provided spatial
		// area, so that only the current workspace can modify these features.
		CallableStatement pstmt2 = conn.prepareCall(
				"{call dbms_wm.lockrows(workspace => ?, table_name => 'TDS_TOPO', lock_mode => 'E', Xmin => ?, Ymin => ?, Xmax => ?, Ymax => ?)}");
		pstmt2.setString(1, jobidStr);
		pstmt2.setInt(2, bbox.get(0));
		pstmt2.setInt(3, bbox.get(1));
		pstmt2.setInt(4, bbox.get(2));
		pstmt2.setInt(5, bbox.get(3));
		pstmt2.execute();
		pstmt2.close();

		conn.close();
	}

	public void exportWorkspace(Job job) throws SQLException {
		Map<String, String> featureIds = getFeatureIds(job.getJobid(), job.getLatitude(), job.getLongitude());

		int counter = 1;
		for (Map.Entry<String, String> entry : featureIds.entrySet()) {
			System.out.println(counter + " Table: " + entry.getKey() + " ID: " + entry.getValue());
			counter++;
		}
	}

	public void removeWorkspace(String jobid) throws SQLException {
		// DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
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

	private Map<String, String> getFeatureIds(Long jobid, BigDecimal lat, BigDecimal lon) throws SQLException {
		Map<String, String> featureIds = new HashMap<String, String>();
		List<Integer> bbox = getBBox(lat, lon);
		Connection conn = getConnection();

		for (String tableName : GEOM_TABLENAMES) {
			String query = "select globalid from " + tableName
					+ " where sdo_filter(geom, sdo_geometry(2003, null, null, sdo_elem_info_array(1, 1003, 3), sdo_ordinate_array("
					+ bbox.get(0) + "," + bbox.get(1) + "," + bbox.get(2) + "," + bbox.get(3) + "))) = 'TRUE'";
			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				featureIds.put(tableName, rs.getString("globalid"));
			}
		}

		conn.close();

		return featureIds;
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:oracle:thin:@54.152.233.204:1521:ORCL", "versionmanager", "Password1");
	}
}