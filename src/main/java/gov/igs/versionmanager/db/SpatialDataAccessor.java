package gov.igs.versionmanager.db;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
//import java.sql.Statement;

import org.springframework.stereotype.Component;

@Component
public class SpatialDataAccessor {

	public void createWorkspace(Long jobid, BigDecimal lat, BigDecimal lon) throws SQLException {
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@54.152.233.204:1521:ORCL", "versionmanager",
				"Password1");

		// Calls the “CreateWorkspace” procedure with a generated UUID as the
		// name.
		CallableStatement pstmt1 = conn.prepareCall("{call dbms_wm.createworkspace(?)}");
		pstmt1.setString(1, Long.toString(jobid));
		pstmt1.execute();
		pstmt1.close();

		// Integer[] bbox = getBBox(lat, lon);
		//
		// // Calls the “LockRows” for the features in the provided spatial
		// area, so that only the current workspace can modify these features.
		// CallableStatement pstmt2 = conn.prepareCall("{call dbms_wm.lockrows
		// (workspace => ?, table_name => 'TDS_TOPO', lock_mode => 'E', Xmin =>
		// ?, Ymin => ?, Xmax => ?, Ymax => ?)}");
		// pstmt2.setString(1, jobid);
		// pstmt2.setInt(2, xmin);
		// pstmt2.setInt(3, ymin);
		// pstmt2.setInt(4, xmax);
		// pstmt2.setInt(5, ymax);
		// pstmt2.execute();
		// pstmt2.close();

		conn.close();
	}

	// private getBBox(BigDecimal lat, BigDecimal lon) {
	//
	// }

	public void removeWorkspace(String jobid) throws SQLException {
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@54.152.233.204:1521:ORCL", "versionmanager",
				"Password1");

		// Calls the “RemoveWorkspace” procedure with a generated UUID as the
		// name.
		CallableStatement pstmt1 = conn.prepareCall("{call dbms_wm.removeworkspace(?)}");
		pstmt1.setString(1, jobid);
		pstmt1.execute();
		pstmt1.close();
	}
}