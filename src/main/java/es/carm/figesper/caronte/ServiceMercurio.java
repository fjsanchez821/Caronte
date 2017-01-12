package es.carm.figesper.caronte;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Table;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatusClient;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import javafx.beans.property.SimpleBooleanProperty;

@SuppressWarnings("unused")
public class ServiceMercurio {

	private static final Logger LOGGER = Logger.getLogger(ServiceMercurio.class);

	private Service servicio;

	private String workingCopy;
	
	private ClassLoader classLoader;
	private Properties properties;

	private App aplicacion;
	private Item totalizadorItem;

	private List<Item> listaDependenciasDeTicketsASubir;

	private SVNLogEntry logEntry;

	public SVNLogEntry getLogEntry() {
		return logEntry;
	}

	public void setLogEntry(SVNLogEntry logEntry) {
		this.logEntry = logEntry;
	}

	public Item getTotalizadorItem() {
		return totalizadorItem;
	}

	public void setTotalizadorItem(Item totalizadorItem) {
		this.totalizadorItem = totalizadorItem;
	}

	public App getAplicacion() {
		return aplicacion;
	}

	public void setAplicacion(App aplicacion) {
		this.aplicacion = aplicacion;
	}

	public ServiceMercurio(Service servicio) throws ServiceException {

		this.servicio = servicio;

		properties = new Properties();

		try {

			classLoader = getClass().getClassLoader();
			properties.load(classLoader.getResourceAsStream("mercurio.properties"));

		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			throw new ServiceException(e.getLocalizedMessage(), e);
		}

		workingCopy = (String) properties.get("working.copy");

		totalizadorItem = new Item(0);
	}

	public boolean isCritico(String path) {

		try {
			FileReader file = new FileReader(properties.getProperty("ficheros.criticos.filename"));
			BufferedReader lector = new BufferedReader(file);
			List<String> ficherosCriticos = new LinkedList<String>();
			String cadena;
			while ((cadena = lector.readLine()) != null) {
				ficherosCriticos.add(cadena);
				// System.out.println(cadena);
			}
			lector.close();
			for (String ficheroCritico : ficherosCriticos) {
				if (path.contains(ficheroCritico.substring(1).replace("\\", "/")))
					return true;
			}
			return false;

		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			throw new ServiceException(e.getLocalizedMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, Item> extraer() {

		DefaultSVNOptions options = new DefaultSVNOptions();
		SVNClientManager clientManager = SVNClientManager.newInstance(options, servicio.getRepoUser(),
				servicio.getRepoPass());

		long startRevision = Long.valueOf(properties.getProperty("ultima.revision.subida").toString()) + 1;
		long endRevision = -1; // HEAD (the latest) revision

		SVNRepository repository = null;

		HashMap<String, Item> ficheros = new HashMap<String, Item>();

		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(servicio.getSrcRepoURL()));

			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(servicio.getRepoUser(),
					servicio.getRepoPass().toCharArray());

			repository.setAuthenticationManager(authManager);

			Collection<Object> logEntries = null;

			logEntries = repository.log(new String[] { "" }, null, startRevision, endRevision, true, true);
			Iterator<Object> entries = logEntries.iterator();
			if (entries.hasNext()) {
				if (logEntry == null) {
					logEntry = (SVNLogEntry) entries.next();
				} else {
					while (entries.hasNext()) {
						SVNLogEntry auxiliar = (SVNLogEntry) entries.next();
						if (auxiliar.equals(logEntry))
							break;
					}
					logEntry = (SVNLogEntry) entries.next();
				}
				System.out.println("---------------------------------------------");
				System.out.println("revision: " + logEntry.getRevision());
				System.out.println("author: " + logEntry.getAuthor());
				System.out.println("date: " + logEntry.getDate());
				System.out.println("log message: " + logEntry.getMessage());

				if (logEntry.getChangedPaths().size() > 0) {
					Set<String> changedPathsSet = logEntry.getChangedPaths().keySet();

					ficheros.clear();

					for (Iterator<String> changedPaths = changedPathsSet.iterator(); changedPaths.hasNext();) {
						SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry.getChangedPaths()
								.get(changedPaths.next());
						String path = entryPath.getPath().substring(10);
						Item item = new Item(path, String.valueOf(logEntry.getRevision()));
						item.getAuthor().set(logEntry.getAuthor());
						item.getTipoSVN().set(String.valueOf(entryPath.getType()));
						ficheros.put(entryPath.getPath(), item);
					}
				}
				// }
				for (Entry<String, Item> fichero : ficheros.entrySet()) {
					System.out.println(" " + fichero.getValue().getTipoSVN() + " " + fichero.getValue().getPath() + " "
							+ fichero.getValue().getRevision() + " " + fichero.getValue().getAuthor());
				}
				totalizadorItem.getTotalFicheros().set(totalizadorItem.getTotalFicheros().get() + ficheros.size());
				totalizadorItem.getPath().set(String.valueOf(totalizadorItem.getTotalFicheros().get()));
				System.out.println("Se subirán un total de " + ficheros.size() + " ficheros");
				System.out.print("¿Desea continuar con la operación? (s/n) ->");
			}

			return ficheros;
		} catch (SVNException e) {
			e.printStackTrace();
			return ficheros;
		} finally {
			clientManager.dispose();
		}

	}

	public void export(List<Item> items) {

		DefaultSVNOptions options = new DefaultSVNOptions();
		SVNClientManager clientManager = SVNClientManager.newInstance(options, servicio.getRepoUser(),
				servicio.getRepoPass());

		SVNUpdateClient updateClient = clientManager.getUpdateClient();
		updateClient.setIgnoreExternals(false);

		for (Item item : items) {

			try {

				SVNURL url = SVNURL.parseURIEncoded(servicio.getSrcRepoURL() + item.getPath().get());
				File path = new File(workingCopy + item.getPath().get().replaceAll("fgGesper", "figesper"));
				SVNRevision revision = SVNRevision.parse(item.getRevision().get());
				updateClient.doExport(url, path, revision, revision, "native", true, SVNDepth.INFINITY);
				item.getExported().set(true);

			} catch (SVNException e) {
				LOGGER.error(e.getLocalizedMessage());
			}
		}

		clientManager.dispose();
	}

	public void commit(List<Item> items, String message) throws ServiceException {

		DefaultSVNOptions options = new DefaultSVNOptions();
		SVNClientManager clientManager = SVNClientManager.newInstance(options, servicio.getRepoUser(),
				servicio.getRepoPass());

		SVNWCClient sVNWCClient = clientManager.getWCClient();

		SVNStatusClient statusClient = clientManager.getStatusClient();
		statusClient.setIgnoreExternals(true);

		SVNCommitClient commitClient = clientManager.getCommitClient();
		commitClient.setIgnoreExternals(false);

		List<File> ficheros = new ArrayList<File>(items.size());

		for (int p = 0; p < items.size(); p++) {

			File path = new File(workingCopy + items.get(p).getPath().get().replaceAll("fgGesper", "figesper"));

			if (!isCritico(path.getPath())) {
				String tipo = items.get(p).getTipoSVN().get();
				if (tipo != null && tipo.equals("D")) {
					if (path.exists()) {
						try {
							sVNWCClient.doDelete(path, false, false);
						} catch (SVNException e) {
							e.printStackTrace();
						}
						ficheros.add(path);
					}
				} else if (tipo != null && tipo.equals("A")) {
					try {
						sVNWCClient.doAdd(path, false, false, false, SVNDepth.INFINITY, false, false);
					} catch (SVNException e) {
						e.printStackTrace();
					}
					ficheros.add(path);
				} else {
					ficheros.add(path);
				}
			}
		}

		try {

			commitClient.doCommit(ficheros.toArray(new File[ficheros.size()]), false, message, null, null, false, true,
					SVNDepth.INFINITY);

			logEntry = null;

			properties.setProperty("ultima.revision.subida", items.get(0).getRevision().get());
			File f = new File("./src/main/resources/mercurio.properties");
			OutputStream fichProp;
			fichProp = new FileOutputStream(f);
			properties.store(fichProp, "");
			System.out.println("Commit finalizado con éxito.");

		} catch (SVNException | IOException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			throw new ServiceException(e.getLocalizedMessage(), e);
		}

		clientManager.dispose();
	}
}
