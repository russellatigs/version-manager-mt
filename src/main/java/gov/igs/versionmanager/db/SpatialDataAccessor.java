package gov.igs.versionmanager.db;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import gov.igs.versionmanager.model.Job;
import gov.igs.versionmanager.util.GMLHandler;
import gov.igs.versionmanager.util.SpatialDataUtility;
import oracle.spatial.util.GeometryExceptionWithContext;

@Component
public class SpatialDataAccessor {

	@Autowired
	private JobDataAccessor jda;
	
	@Autowired 
	private SpatialDataUtility sdUtil;
	
	@Autowired
	private GMLHandler gmlHandler;

	private static final String[] GEOM_TABLENAMES = { "TDSUTILITYINFRASTRUCTUREPOINT", "TDSUTILITYINFRASTRUCTURECURVE",
			"TDSTRANSPORTATIONGROUNDPOINT", /*"TDSSTRUCTUREPOINT", */"TDSSTRUCTURECURVE", "TDSSTORAGEPOINT",
			"TDSRECREATIONPOINT", "TDSRECREATIONCURVE", "TDSCULTUREPOINT", "TDSAERONAUTICPOINT" };

	@PostConstruct
	public void init() throws SQLException {
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
	}

	public void createWorkspace(Job job) throws SQLException {
		String jobidStr = Long.toString(job.getJobid());
		// DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

		// Calls the “CreateWorkspace” procedure with a generated UUID as the
		// name.
		Connection conn1 = sdUtil.getConnection();
		CallableStatement pstmt1 = conn1.prepareCall("{call dbms_wm.createworkspace(?)}");
		pstmt1.setString(1, jobidStr);
		pstmt1.execute();
		pstmt1.close();
		conn1.close();
//
//		List<Integer> bbox = getBBox(job.getLatitude(), job.getLongitude());
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
		
		jda.createJob(job);
	}

	public File exportWorkspace(String jobid, String user)
			throws SQLException, IOException, GeometryExceptionWithContext {

		Job job = jda.getJob(jobid);
		List<Integer> bbox = sdUtil.getBBox(job.getLatitude(), job.getLongitude());
		Connection conn = sdUtil.getConnection();

		List<List<String>> listOfTables = new ArrayList<List<String>>();

		for (String tableName : GEOM_TABLENAMES) {
			String query = "select * from " + tableName
					+ " where sdo_filter(geom, sdo_geometry(2003, null, null, sdo_elem_info_array(1, 1003, 3), sdo_ordinate_array("
					+ bbox.get(0) + "," + bbox.get(1) + "," + bbox.get(2) + "," + bbox.get(3) + "))) = 'TRUE'";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			listOfTables.add(sdUtil.formTableForGMLFile(tableName, rs));
		}

		conn.close();

		File fileToReturn = sdUtil.createGMLFile(jobid, listOfTables);

		jda.updateJobToExported(user, jobid, sdUtil.countNumFeatures(listOfTables), sdUtil.countUniqueFeatureClasses(listOfTables));

		return fileToReturn;
	}

	public synchronized void checkInFile(String jobid, String user, InputStream inputStream) throws SQLException, SAXException, IOException, ParserConfigurationException {

		// Go To workspace 
		Connection conn = sdUtil.getConnection();
		CallableStatement pstmt1 = conn.prepareCall("{call dbms_wm.gotoworkspace(?)}");
		pstmt1.setString(1, jobid);
		pstmt1.execute();
		pstmt1.close();
		conn.close();

		SAXParserFactory.newInstance().newSAXParser().parse(inputStream, gmlHandler);
		
		jda.updateJobToCheckedIn(user, jobid, gmlHandler.getNumFeatures(), gmlHandler.getNumUniqueFeatureClasses());
	}
	
	public void postToGold(String jobid) {
		
	}
	
	public void removeWorkspace(String jobid) throws SQLException {
		Connection conn = sdUtil.getConnection();

		// Calls “RemoveWorkspace” procedure with a generated UUID as the name.
		CallableStatement pstmt1 = conn.prepareCall("{call dbms_wm.removeworkspace(?)}");
		pstmt1.setString(1, jobid);
		pstmt1.execute();
		pstmt1.close();
		conn.close();
		
		jda.deleteJob(jobid);
	}
}