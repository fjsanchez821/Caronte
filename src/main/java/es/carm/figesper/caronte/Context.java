package es.carm.figesper.caronte;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Context {

	private static final Logger LOGGER = Logger.getLogger(Context.class);
	
	public static final String SRC_WORKING_COPY;
	public static final String SRC_REPO_URL;
	public static final String SRC_REPO_USER;
	public static final String SRC_REPO_PASS;
	
	public static final String DST_WORKING_COPY;
	public static final String DST_REPO_URL;
	public static final String DST_REPO_USER;
	public static final String DST_REPO_PASS;
	
	public static final int EXCEL_PATH_COL;
	public static final int EXCEL_REVISION_COL;
	public static final int EXCEL_INITIAL_ROW;
	
	static {
	
		Properties properties = new Properties();
        
		try {
			
			ClassLoader classLoader = Context.class.getClassLoader();
			properties.load(classLoader.getResourceAsStream("caronte.properties"));
			
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			throw new ContextException(e.getLocalizedMessage(), e);
		}

		SRC_WORKING_COPY = (String) properties.get("src.working.copy");
		SRC_REPO_URL = (String) properties.get("src.repo.url");
		SRC_REPO_USER = (String) properties.get("src.repo.user");
		SRC_REPO_PASS = (String) properties.get("src.repo.pass");
		
		DST_WORKING_COPY = (String) properties.get("dst.working.copy");
		DST_REPO_URL = (String) properties.get("dst.repo.url");
		DST_REPO_USER = (String) properties.get("dst.repo.user");
		DST_REPO_PASS = (String) properties.get("dst.repo.pass");
		
		EXCEL_PATH_COL = Integer.parseInt((String) properties.get("excel.path.col")) - 1;
		EXCEL_REVISION_COL = Integer.parseInt((String) properties.get("excel.revision.col")) - 1;
		EXCEL_INITIAL_ROW = Integer.parseInt((String) properties.get("excel.initial.row")) - 1;
	}
}
