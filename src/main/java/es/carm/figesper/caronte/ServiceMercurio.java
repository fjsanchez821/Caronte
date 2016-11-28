package es.carm.figesper.caronte;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Table;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusClient;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TextInputDialog;

@SuppressWarnings("unused")
public class ServiceMercurio {

	private static final Logger LOGGER = Logger.getLogger(ServiceMercurio.class);

	private String svnInventoryPath;
	private String svnSeguimientoPath;

	private String srcWorkingCopy;
	private String srcRepoURL;
	private String srcRepoUser;
	private String srcRepoPass;

	private String dstWorkingCopy;
	private String dstRepoURL;
	private String dstRepoUser;
	private String dstRepoPass;

	private int excelPasarCol;
	private int excelPathCol;
	private int excelRevisionCol;
	private int excelInitRow;

	private int odsPasarCol;
	private int odsGlpiCol;
	private int odsTituloCol;
	private int odsAsignadoCol;
	private int odsTipoCol;
	private int odsVencimientoCol;
	private int odsResponsableCol;
	private int odsEstadoCol;
	private int odsEntornoCol;
	private int odsValidadoCol;
	private int odsInitRow;

	private ClassLoader classLoader;
	private Properties properties;

	private int numFilas;
	private int numPaso;

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

	public ServiceMercurio() throws ServiceException {

		properties = new Properties();

		try {

			classLoader = getClass().getClassLoader();
			properties.load(classLoader.getResourceAsStream("mercurio.properties"));

		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			throw new ServiceException(e.getLocalizedMessage(), e);
		}

		dstRepoUser = (String) properties.get("dst.repo.user");

		svnInventoryPath = (String) properties.get("svn.inventory.path");

		svnSeguimientoPath = (String) properties.get("svn.seguimiento.path");

		srcWorkingCopy = (String) properties.get("src.working.copy");
		srcRepoURL = (String) properties.get("src.repo.url");
		srcRepoUser = (String) properties.get("src.repo.user");
		srcRepoPass = (String) properties.get("src.repo.pass");

		dstWorkingCopy = (String) properties.get("dst.working.copy");
		dstRepoURL = (String) properties.get("dst.repo.url");

		dstRepoPass = (String) properties.get("dst.repo.pass");

		excelPasarCol = Integer.parseInt((String) properties.get("excel.pasar.col")) - 1;
		excelPathCol = Integer.parseInt((String) properties.get("excel.path.col")) - 1;
		excelRevisionCol = Integer.parseInt((String) properties.get("excel.revision.col")) - 1;
		excelInitRow = Integer.parseInt((String) properties.get("excel.initial.row")) - 1;

		odsPasarCol = Integer.parseInt((String) properties.get("ods.pasar.col")) - 1;
		odsGlpiCol = Integer.parseInt((String) properties.get("ods.glpi.col")) - 1;
		odsTituloCol = Integer.parseInt((String) properties.get("ods.titulo.col")) - 1;
		odsAsignadoCol = Integer.parseInt((String) properties.get("ods.asignado.col")) - 1;
		odsTipoCol = Integer.parseInt((String) properties.get("ods.tipo.col")) - 1;
		odsVencimientoCol = Integer.parseInt((String) properties.get("ods.vencimiento.col")) - 1;
		odsResponsableCol = Integer.parseInt((String) properties.get("ods.responsable.col")) - 1;
		odsEstadoCol = Integer.parseInt((String) properties.get("ods.estado.col")) - 1;
		odsEntornoCol = Integer.parseInt((String) properties.get("ods.entorno.col")) - 1;
		odsValidadoCol = Integer.parseInt((String) properties.get("ods.validado.col")) - 1;
		odsInitRow = Integer.parseInt((String) properties.get("ods.initial.row")) - 1;

		totalizadorItem = new Item(0);
	}

	public String getSrcRepoUser() {
		return srcRepoUser;
	}

	public String getSvnInventoryPath() {
		return svnInventoryPath;
	}

	public String getSvnSeguimientoPath() {
		return svnSeguimientoPath;
	}

	public List<Item> parse(File file) throws ServiceException {

		List<Item> items = new ArrayList<Item>();

		try {

			String extension = FilenameUtils.getExtension(file.getPath());
			if (extension.equals("xlsx")) {
				XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));

				XSSFSheet sheet = workbook.getSheetAt(0);

				for (Row row : sheet) {

					if (row.getRowNum() < excelInitRow) {
						continue;
					}

					String path = row.getCell(excelPathCol).getStringCellValue();
					Double revision = row.getCell(excelRevisionCol).getNumericCellValue();

					if (path.isEmpty()) {
						break;
					}

					items.add(new Item(path, String.valueOf(revision.intValue())));
				}

				workbook.close();
			} else {
				SpreadsheetDocument sheetDocument = SpreadsheetDocument.loadDocument(file);
				Table sheet = sheetDocument.getSheetByIndex(0);
				Iterator<org.odftoolkit.simple.table.Row> iterator = sheet.getRowIterator();
				while (iterator.hasNext()) {
					org.odftoolkit.simple.table.Row row = iterator.next();

					if (row.getRowIndex() < odsInitRow) {
						continue;
					}
					String glpi = row.getCellByIndex(odsGlpiCol).getStringValue();
					if (glpi.isEmpty()) {
						break;
					}
					String titulo = row.getCellByIndex(odsTituloCol).getStringValue();
					String asignado = row.getCellByIndex(odsAsignadoCol).getStringValue();
					String estado = row.getCellByIndex(odsEstadoCol).getStringValue();
					String entorno = row.getCellByIndex(odsEntornoCol).getStringValue();
					String validado = row.getCellByIndex(odsValidadoCol).getStringValue();
					String tipo = row.getCellByIndex(odsTipoCol).getStringValue();
					String vencimiento = row.getCellByIndex(odsVencimientoCol).getStringValue();
					String responsable = row.getCellByIndex(odsResponsableCol).getStringValue();
					String pasar = row.getCellByIndex(odsPasarCol).getStringValue();

					Item item = new Item(glpi, titulo, asignado, tipo, vencimiento, responsable, estado, entorno,
							validado);
					if (pasar.equals("S"))
						item.setSelected(new SimpleBooleanProperty(true));

					items.add(item);

				}

			}

		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			throw new ServiceException(e.getLocalizedMessage(), e);
		}

		return items;
	}

	private boolean isParaPasar(String glpi, HashMap<String, HashMap<String, Item>> ficherosExtraidos) {
		return ficherosExtraidos.keySet().contains(glpi);
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
		SVNClientManager clientManager = SVNClientManager.newInstance(options, srcRepoUser, srcRepoPass);

		long startRevision = Long.valueOf(properties.getProperty("ultima.revision.subida").toString()) + 1;
		long endRevision = -1; // HEAD (the latest) revision

		SVNRepository repository = null;

		HashMap<String, Item> ficheros = new HashMap<String, Item>();

		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(srcRepoURL));

			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(srcRepoUser,
					srcRepoPass.toCharArray());

			repository.setAuthenticationManager(authManager);

			Collection<Object> logEntries = null;

			logEntries = repository.log(new String[] { "" }, null, startRevision, endRevision, true, true);
			// for (Iterator entries = logEntries.iterator();
			// entries.hasNext();) {
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
						ficheros.put(entryPath.getPath(), item);
					}
				}
				// }
				for (Entry<String, Item> fichero : ficheros.entrySet()) {
					System.out.println(" " + fichero.getValue().getGlpi() + " " + fichero.getValue().getPath() + " "
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

	@SuppressWarnings("unchecked")
	public void history() {

		DefaultSVNOptions options = new DefaultSVNOptions();
		SVNClientManager clientManager = SVNClientManager.newInstance(options, srcRepoUser, srcRepoPass);

		long startRevision = 17400;
		long endRevision = -1; // HEAD (the latest) revision

		SVNRepository repository = null;

		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(srcRepoURL));

			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(srcRepoUser,
					srcRepoPass.toCharArray());

			repository.setAuthenticationManager(authManager);

			Collection<Object> logEntries = null;

			logEntries = repository.log(new String[] { "" }, null, startRevision, endRevision, true, true);
			for (Iterator<Object> entries = logEntries.iterator(); entries.hasNext();) {
				SVNLogEntry logEntry = (SVNLogEntry) entries.next();
				if (logEntry.getMessage().contains("272524")) {
					System.out.println("---------------------------------------------");
					System.out.println("revision: " + logEntry.getRevision());
					System.out.println("author: " + logEntry.getAuthor());
					System.out.println("date: " + logEntry.getDate());
					System.out.println("log message: " + logEntry.getMessage());

					if (logEntry.getChangedPaths().size() > 0) {
						Set<String> changedPathsSet = logEntry.getChangedPaths().keySet();

						for (Iterator<String> changedPaths = changedPathsSet.iterator(); changedPaths.hasNext();) {
							SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry.getChangedPaths()
									.get(changedPaths.next());
							System.out.println(" " + entryPath.getType() + " " + entryPath.getPath()
									+ ((entryPath.getCopyPath() != null) ? " (from " + entryPath.getCopyPath()
											+ " revision " + entryPath.getCopyRevision() + ")" : ""));
						}
					}
				}
			}
		} catch (SVNException e) {
			e.printStackTrace();
		}

		clientManager.dispose();
	}

	public void export(List<Item> items) {

		DefaultSVNOptions options = new DefaultSVNOptions();
		SVNClientManager clientManager = SVNClientManager.newInstance(options, srcRepoUser, srcRepoPass);

		SVNUpdateClient updateClient = clientManager.getUpdateClient();
		updateClient.setIgnoreExternals(false);

		for (Item item : items) {

			try {

				SVNURL url = SVNURL.parseURIEncoded(srcRepoURL + item.getPath().get());
				File path = new File(srcWorkingCopy + item.getPath().get().replaceAll("fgGesper", "figesper"));
				SVNRevision revision = SVNRevision.parse(item.getRevision().get());
				updateClient.doExport(url, path, revision, revision, "native", true, SVNDepth.INFINITY);
				item.getExported().set(true);

			} catch (SVNException e) {
				LOGGER.error(e.getLocalizedMessage());
			}
		}

		clientManager.dispose();
	}

	public void move(Collection<Item> items) {

		for (Item item : items) {

			try {

				Path srcPath = FileSystems.getDefault().getPath(srcWorkingCopy + item.getPath().get());
				Path dstPath = FileSystems.getDefault().getPath(dstWorkingCopy + item.getPath().get());
				if (Files.exists(dstPath, LinkOption.NOFOLLOW_LINKS)) {
					Files.setAttribute(dstPath, "dos:readonly", false);
				}
				Files.move(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);
				item.getMoved().set(true);

			} catch (IOException e) {
				LOGGER.error(e.getLocalizedMessage());
			}
		}
	}

	public void commit(List<Item> items, String message) throws ServiceException {

		DefaultSVNOptions options = new DefaultSVNOptions();
		SVNClientManager clientManager = SVNClientManager.newInstance(options, dstRepoUser, dstRepoPass);

		SVNStatusClient statusClient = clientManager.getStatusClient();
		statusClient.setIgnoreExternals(true);

		SVNCommitClient commitClient = clientManager.getCommitClient();
		commitClient.setIgnoreExternals(false);

		File[] paths = new File[items.size()];

		for (int p = 0; p < items.size(); p++) {

			File path = new File(dstWorkingCopy + items.get(p).getPath().get().replaceAll("fgGesper", "figesper"));
			SVNStatus estado = null;
			try {
				estado = statusClient.doStatus(path, false);
			} catch (SVNException e1) {
				e1.printStackTrace();
			}
			if (!isCritico(path.getPath())) {
				if (!estado.isVersioned()) {
					try {
						SVNCommitInfo info = commitClient.doImport(path,
								SVNURL.parseURIEncoded(
										dstRepoURL + items.get(p).getPath().get().replaceAll("fgGesper", "figesper")),
								message, null, false, true, SVNDepth.INFINITY);
					} catch (SVNException | EmptyStackException e) {
						e.printStackTrace();
					}

				}
				paths[p] = path;
			}
		}

		try {

			commitClient.doCommit(paths, false, message, null, null, false, true, SVNDepth.INFINITY);

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
