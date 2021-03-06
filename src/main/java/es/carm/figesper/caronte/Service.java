package es.carm.figesper.caronte;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
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

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TextInputDialog;

@SuppressWarnings("unused")
public class Service {

	private static final Logger LOGGER = Logger.getLogger(Service.class);

	private String repoUser;
	private String repoPass;

	private String svnInventoryPath;
	private String svnSeguimientoPath;

	private String srcWorkingCopy;
	private String srcRepoURL;
	private String srcRepoURLPro;

	private String dstWorkingCopy;
	private String dstRepoURL;

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

	private String mercurioWorkingCopy;
	private String mercurioWorkingCopyPro;
	SVNLogEntry logEntryMercurio = null;
	SVNLogEntry logEntryMercurioPro = null;

	private ClassLoader classLoader;
	private SortedProperties properties;
	private InputStream ficherosCriticos;

	private int numFilas;
	private int numPaso;

	private App aplicacion;
	private Item totalizadorItem;

	private List<Item> listaDependenciasDeTicketsASubir;
	private List<Item> listaBBDD;
	private List<Item> listaOtros;

	public Item getTotalizadorItem() {
		return totalizadorItem;
	}

	public void setTotalizadorItem(Item totalizadorItem) {
		this.totalizadorItem = totalizadorItem;
	}

	public App getAplicacion() {
		return aplicacion;
	}

	public void setAplicacion(App app) {
		this.aplicacion = app;
	}

	public Service() throws ServiceException {

		properties = new SortedProperties();

		try {

			classLoader = getClass().getClassLoader();
			properties.load(classLoader.getResourceAsStream("caronte.properties"));

		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			throw new ServiceException(e.getLocalizedMessage(), e);
		}

		repoUser = (String) properties.get("repo.user");
		repoPass = (String) properties.get("repo.pass");

		svnInventoryPath = (String) properties.get("svn.inventory.path");

		svnSeguimientoPath = (String) properties.get("svn.seguimiento.path");

		srcWorkingCopy = (String) properties.get("src.working.copy");
		srcRepoURL = (String) properties.get("src.repo.url");
		srcRepoURLPro = (String) properties.get("pro.src.repo.url");

		dstWorkingCopy = (String) properties.get("dst.working.copy");
		dstRepoURL = (String) properties.get("dst.repo.url");

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

		mercurioWorkingCopy = (String) properties.get("mercurio.working.copy");
		mercurioWorkingCopyPro = (String) properties.get("pro.mercurio.working.copy");

		totalizadorItem = new Item(0);

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
				Iterator iterator = sheet.getRowIterator();
				while (iterator.hasNext()) {
					org.odftoolkit.simple.table.Row row = (org.odftoolkit.simple.table.Row) iterator.next();

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
			//throw new ServiceException(e.getLocalizedMessage(), e);
		}

		return items;
	}

	// funcion para parsear del EXCEL pendiente paso a produccion la hoja de
	// BBDD
	public List<Item> parseBBDD(File file) throws ServiceException {

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
				Iterator iterator = sheet.getRowIterator();
				while (iterator.hasNext()) {
					org.odftoolkit.simple.table.Row row = (org.odftoolkit.simple.table.Row) iterator.next();

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

	public List<Item> parseOtros(File file) throws ServiceException {

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
				Iterator iterator = sheet.getRowIterator();
				while (iterator.hasNext()) {
					org.odftoolkit.simple.table.Row row = (org.odftoolkit.simple.table.Row) iterator.next();

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

	List<Item> leerPendienteAProduccion(int pestana) {
		List<Item> items = new ArrayList<Item>();

		try {
			File file = new File(properties.getProperty("pendiente.paso.path") + "/"
					+ properties.getProperty("pendiente.paso.filename"));
			SpreadsheetDocument sheetDocument = SpreadsheetDocument.loadDocument(file);
			Table sheet = sheetDocument.getSheetByIndex(pestana);
			Iterator iterator = sheet.getRowIterator();
			while (iterator.hasNext()) {
				org.odftoolkit.simple.table.Row row = (org.odftoolkit.simple.table.Row) iterator.next();

				if (row.getRowIndex() < 2) {
					continue;
				}
				String glpi = row.getCellByIndex(0).getStringValue();
				if (glpi.isEmpty()) {
					if (row.getCellByIndex(1).getStringValue().isEmpty()) {
						break;
					}
				}
				Item item;
				// Se hace un switch segun la pagina del EXCEL
				String path, version, autor, desarrollo, pruebas, rutaSVN;
				switch (pestana) {
				case 0:
					path = row.getCellByIndex(1).getStringValue();
					version = row.getCellByIndex(2).getStringValue();
					autor = row.getCellByIndex(3).getStringValue();
					item = new Item(glpi, path, version, autor);
					items.add(item);
					break;

				case 1:
					path = row.getCellByIndex(1).getStringValue();
					version = row.getCellByIndex(2).getStringValue();
					autor = row.getCellByIndex(3).getStringValue();
					desarrollo = row.getCellByIndex(4).getStringValue();
					pruebas = row.getCellByIndex(5).getStringValue();
					item = new Item(glpi, path, version, autor, desarrollo, pruebas);
					items.add(item);
					break;

				case 2:
					path = row.getCellByIndex(1).getStringValue();
					version = row.getCellByIndex(2).getStringValue();
					autor = row.getCellByIndex(3).getStringValue();
					desarrollo = row.getCellByIndex(4).getStringValue();
					pruebas = row.getCellByIndex(5).getStringValue();
					rutaSVN = row.getCellByIndex(6).getStringValue();
					item = new Item(glpi, path, version, autor, desarrollo, pruebas, rutaSVN);
					items.add(item);
					break;
				}

			}
			return items;

		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			throw new ServiceException(e.getLocalizedMessage(), e);
		}
	}

	private boolean isParaPasar(String glpi, HashMap<String, HashMap<String, Item>> ficherosExtraidos) {
		return ficherosExtraidos.keySet().contains(glpi);
	}

	public List<Item> comprobarDependencias(HashMap<String, HashMap<String, Item>> ficherosExtraidos) {
		List<Item> itemsDelPendiente = leerPendienteAProduccion(0);
		List<Item> itemsExtraidos = new ArrayList<Item>();
		for (HashMap<String, Item> ficheros : ficherosExtraidos.values()) {
			itemsExtraidos.addAll(ficheros.values());
		}
		listaDependenciasDeTicketsASubir = new ArrayList<Item>();
		for (Item itemExtraido : itemsExtraidos) {
			for (Item itemExtraido2 : itemsExtraidos) {
				if (itemExtraido2.getGlpi().get().equals("Total Ficheros")
						|| itemExtraido2.getGlpi().get().trim().equals(itemExtraido.getGlpi().get().trim())) {
					continue;
				}
				if (itemExtraido2.getPath().getValue().trim().equals(itemExtraido.getPath().getValue().trim())) {
					System.out.println("Dependencia de " + itemExtraido.getPath().getValue() + " con versión anterior "
							+ itemExtraido.getRevision().getValue() + " frente a la versión actual "
							+ itemExtraido2.getRevision().getValue());
					if (itemExtraido2.getGlpiDependiente() == null) {
						itemExtraido2.setGlpiDependiente(new SimpleStringProperty(itemExtraido.getGlpi().get()));
					} else {
						itemExtraido2.getGlpiDependiente().set(
								itemExtraido2.getGlpiDependiente().get() + ", " + itemExtraido.getGlpi().getValue());
					}
					if (itemExtraido2.getGlpiDependiente() != null
							&& !itemExtraido2.getGlpiDependiente().get().equals("")) {
						if (itemExtraido2.getRevisionDependiente() == null) {
							itemExtraido2
									.setRevisionDependiente(new SimpleStringProperty(itemExtraido.getRevision().get()));
						} else {
							itemExtraido2.getRevisionDependiente().set(itemExtraido2.getRevisionDependiente().get()
									+ ", " + itemExtraido.getRevision().getValue());
						}
					}
				}
				if (!listaDependenciasDeTicketsASubir.contains(itemExtraido2))
					listaDependenciasDeTicketsASubir.add(itemExtraido2);
			}
		}
		for (Item itemDelPendiente : itemsDelPendiente) {
			if (!isParaPasar(itemDelPendiente.getGlpi().getValue().trim(), ficherosExtraidos)) {
				for (Item itemExtraido : itemsExtraidos) {
					if (itemExtraido.getGlpi().get().equals("Total Ficheros"))
						continue;
					if (itemExtraido.getPath().getValue().equals(itemDelPendiente.getPath().getValue())) {
						// TODO
						if (Integer.parseInt(itemExtraido.getRevision().getValue()) > Integer
								.parseInt(itemDelPendiente.getRevision().getValue())) {
							System.out.println("Dependencia de " + itemDelPendiente.getPath().getValue()
									+ " con versión anterior " + itemDelPendiente.getRevision().getValue()
									+ " frente a la versión actual " + itemExtraido.getRevision().getValue());
							if (itemExtraido.getGlpiDependiente() == null) {
								itemExtraido
										.setGlpiDependiente(new SimpleStringProperty(itemDelPendiente.getGlpi().get()));
							} else {
								itemExtraido.getGlpiDependiente().set(itemExtraido.getGlpiDependiente().get() + ", "
										+ itemDelPendiente.getGlpi().getValue());
							}
							if (itemExtraido.getGlpiDependiente() != null
									&& !itemExtraido.getGlpiDependiente().get().equals("")) {
								if (itemExtraido.getRevisionDependiente() == null) {
									itemExtraido.setRevisionDependiente(
											new SimpleStringProperty(itemDelPendiente.getRevision().get()));
								} else {
									itemExtraido.getRevisionDependiente()
											.set(itemExtraido.getRevisionDependiente().get() + ", "
													+ itemDelPendiente.getRevision().getValue());
								}
							}
						}
					}
				}
			}
		}
		return listaDependenciasDeTicketsASubir;
	}

	private void generarCabecera(XSSFWorkbook libro, XSSFSheet hoja) {
		Row titulo = hoja.createRow(numFilas);
		Cell celdaTitulo = titulo.createCell(0);
		hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

		CellStyle estiloTitulo = libro.createCellStyle();
		estiloTitulo.setFillForegroundColor(IndexedColors.BROWN.getIndex());
		XSSFFont fuente = libro.createFont();
		fuente.setColor(IndexedColors.WHITE.getIndex());
		estiloTitulo.setFont(fuente);
		estiloTitulo.setFillPattern(CellStyle.SOLID_FOREGROUND);
		estiloTitulo.setAlignment(CellStyle.ALIGN_CENTER);
		estiloTitulo.setBorderBottom(CellStyle.BORDER_THIN);
		estiloTitulo.setBorderTop(CellStyle.BORDER_THIN);
		estiloTitulo.setBorderRight(CellStyle.BORDER_THIN);
		estiloTitulo.setBorderLeft(CellStyle.BORDER_THIN);
		celdaTitulo.setCellStyle(estiloTitulo);
		SimpleDateFormat formateadorFecha = new SimpleDateFormat("dd/MM/yyyy");
		celdaTitulo.setCellValue("PASOS A PRODUCCION REALIZADO A FECHA DE: " + formateadorFecha.format(new Date())
				+ " (PASE Nº" + numPaso + ")");
		numFilas++;
		Row encabezado = hoja.createRow(numFilas);
		Cell celdaEncabezado = encabezado.createCell(0);
		CellStyle estiloEncabezado = libro.createCellStyle();
		estiloEncabezado.setFillForegroundColor(IndexedColors.GOLD.getIndex());
		estiloEncabezado.setFillPattern(CellStyle.SOLID_FOREGROUND);
		estiloEncabezado.setAlignment(CellStyle.ALIGN_CENTER);
		estiloEncabezado.setBorderBottom(CellStyle.BORDER_THIN);
		estiloEncabezado.setBorderTop(CellStyle.BORDER_THIN);
		estiloEncabezado.setBorderRight(CellStyle.BORDER_THIN);
		estiloEncabezado.setBorderLeft(CellStyle.BORDER_THIN);
		celdaEncabezado.setCellStyle(estiloEncabezado);
		celdaEncabezado.setCellValue("GLPI");
		celdaEncabezado = encabezado.createCell(1);
		celdaEncabezado.setCellStyle(estiloEncabezado);
		celdaEncabezado.setCellValue("FICHERO");
		celdaEncabezado = encabezado.createCell(2);
		celdaEncabezado.setCellStyle(estiloEncabezado);
		celdaEncabezado.setCellValue("VERSIÓN");
		celdaEncabezado = encabezado.createCell(3);
		celdaEncabezado.setCellStyle(estiloEncabezado);
		celdaEncabezado.setCellValue("PROGRAMADOR");
		numFilas++;
		hoja.setColumnWidth(0, 2500);
		hoja.setColumnWidth(1, 25000);
		hoja.setColumnWidth(2, 2500);
		hoja.setColumnWidth(3, 4500);
	}

	private void generarCabeceraBBDD(XSSFWorkbook libro, XSSFSheet hoja) {
		numFilas = 0;
		Row titulo = hoja.createRow(numFilas);
		Cell celdaTitulo = titulo.createCell(0);
		hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
		CellStyle estiloTitulo = libro.createCellStyle();
		estiloTitulo.setFillForegroundColor(IndexedColors.BROWN.getIndex());
		XSSFFont fuente = libro.createFont();
		fuente.setColor(IndexedColors.WHITE.getIndex());
		estiloTitulo.setFont(fuente);
		estiloTitulo.setFillPattern(CellStyle.SOLID_FOREGROUND);
		estiloTitulo.setAlignment(CellStyle.ALIGN_CENTER);
		estiloTitulo.setBorderBottom(CellStyle.BORDER_THIN);
		estiloTitulo.setBorderTop(CellStyle.BORDER_THIN);
		estiloTitulo.setBorderRight(CellStyle.BORDER_THIN);
		estiloTitulo.setBorderLeft(CellStyle.BORDER_THIN);
		celdaTitulo.setCellStyle(estiloTitulo);
		SimpleDateFormat formateadorFecha = new SimpleDateFormat("dd/MM/yyyy");
		celdaTitulo.setCellValue("PASOS A PRODUCCION REALIZADO A FECHA DE: " + formateadorFecha.format(new Date())
				+ " (PASE Nº" + numPaso + ")");
		numFilas++;
		Row encabezado = hoja.createRow(numFilas);
		Cell celdaEncabezado = encabezado.createCell(0);
		CellStyle estiloEncabezado = libro.createCellStyle();
		estiloEncabezado.setFillForegroundColor(IndexedColors.GOLD.getIndex());
		estiloEncabezado.setFillPattern(CellStyle.SOLID_FOREGROUND);
		estiloEncabezado.setAlignment(CellStyle.ALIGN_CENTER);
		estiloEncabezado.setBorderBottom(CellStyle.BORDER_THIN);
		estiloEncabezado.setBorderTop(CellStyle.BORDER_THIN);
		estiloEncabezado.setBorderRight(CellStyle.BORDER_THIN);
		estiloEncabezado.setBorderLeft(CellStyle.BORDER_THIN);
		celdaEncabezado.setCellStyle(estiloEncabezado);
		celdaEncabezado.setCellValue("GLPI");
		celdaEncabezado = encabezado.createCell(1);
		celdaEncabezado.setCellStyle(estiloEncabezado);
		celdaEncabezado.setCellValue("SCRIPT");
		celdaEncabezado = encabezado.createCell(2);
		celdaEncabezado.setCellStyle(estiloEncabezado);
		celdaEncabezado.setCellValue("VERSIÓN");
		celdaEncabezado = encabezado.createCell(3);
		celdaEncabezado.setCellStyle(estiloEncabezado);
		celdaEncabezado.setCellValue("USUARIO");

		hoja.setColumnWidth(0, 2500);
		hoja.setColumnWidth(1, 25000);
		hoja.setColumnWidth(2, 2500);
		hoja.setColumnWidth(3, 4500);
	}

	private void generarCabeceraOtros(XSSFWorkbook libro, XSSFSheet hoja) {
		numFilas = 0;
		Row titulo = hoja.createRow(numFilas);
		Cell celdaTitulo = titulo.createCell(0);
		hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

		CellStyle estiloTitulo = libro.createCellStyle();
		estiloTitulo.setFillForegroundColor(IndexedColors.BROWN.getIndex());
		XSSFFont fuente = libro.createFont();
		fuente.setColor(IndexedColors.WHITE.getIndex());
		estiloTitulo.setFont(fuente);
		estiloTitulo.setFillPattern(CellStyle.SOLID_FOREGROUND);
		estiloTitulo.setAlignment(CellStyle.ALIGN_CENTER);
		estiloTitulo.setBorderBottom(CellStyle.BORDER_THIN);
		estiloTitulo.setBorderTop(CellStyle.BORDER_THIN);
		estiloTitulo.setBorderRight(CellStyle.BORDER_THIN);
		estiloTitulo.setBorderLeft(CellStyle.BORDER_THIN);
		celdaTitulo.setCellStyle(estiloTitulo);
		SimpleDateFormat formateadorFecha = new SimpleDateFormat("dd/MM/yyyy");
		celdaTitulo.setCellValue("PASOS A PRODUCCION REALIZADO A FECHA DE: " + formateadorFecha.format(new Date())
				+ " (PASE Nº" + numPaso + ")");
		numFilas++;
		Row encabezado = hoja.createRow(numFilas);
		Cell celdaEncabezado = encabezado.createCell(0);
		CellStyle estiloEncabezado = libro.createCellStyle();
		estiloEncabezado.setFillForegroundColor(IndexedColors.GOLD.getIndex());
		estiloEncabezado.setFillPattern(CellStyle.SOLID_FOREGROUND);
		estiloEncabezado.setAlignment(CellStyle.ALIGN_CENTER);
		estiloEncabezado.setBorderBottom(CellStyle.BORDER_THIN);
		estiloEncabezado.setBorderTop(CellStyle.BORDER_THIN);
		estiloEncabezado.setBorderRight(CellStyle.BORDER_THIN);
		estiloEncabezado.setBorderLeft(CellStyle.BORDER_THIN);
		celdaEncabezado.setCellStyle(estiloEncabezado);
		celdaEncabezado.setCellValue("GLPI");
		celdaEncabezado = encabezado.createCell(1);
		celdaEncabezado.setCellStyle(estiloEncabezado);
		celdaEncabezado.setCellValue("SCRIPT");
		celdaEncabezado = encabezado.createCell(2);
		celdaEncabezado.setCellStyle(estiloEncabezado);
		celdaEncabezado.setCellValue("VERSIÓN");
		celdaEncabezado = encabezado.createCell(3);
		celdaEncabezado.setCellStyle(estiloEncabezado);
		celdaEncabezado.setCellValue("USUARIO");
		celdaEncabezado = encabezado.createCell(4);
		numFilas++;
		hoja.setColumnWidth(0, 2500);
		hoja.setColumnWidth(1, 25000);
		hoja.setColumnWidth(2, 2500);
		hoja.setColumnWidth(3, 4500);
	}

	private int getNumPaso() {

		int numPaso = 0;
		// JAVI descomentar esto
		File directorio = new File(properties.getProperty("svn.inventory.path"));
		File ficheros[] = directorio.listFiles();
		for (File file : ficheros) {
			if (file.isDirectory() && file.getName().contains("Paso")) {
				String cadenas[] = file.getName().split(" ");
				try {
					numPaso = Integer.valueOf(cadenas[1]);
				} catch (Exception e) {
					// Si no hace bien la conversión porque hay algún carácter
					// extraño no hacer nada
				}
			}
		}
		return ++numPaso;
	}

	public boolean exportarAFichero(FilteredList<Item> filteredList) {

		listaBBDD = leerPendienteAProduccion(1);
		listaOtros = leerPendienteAProduccion(2);

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {

					numPaso = getNumPaso();
					TextInputDialog dialog = new TextInputDialog(String.valueOf(numPaso));
					dialog.setTitle("Introducción del Número de Paso");
					dialog.setHeaderText("");
					dialog.setContentText("Introduzca el Número de Paso a Producción:");

					Optional<String> result = dialog.showAndWait();
					result.ifPresent(paso -> numPaso = Integer.valueOf(paso));

					if (result.isPresent()) {

						aplicacion.getPb().setProgress(aplicacion.getPb().getProgress() + 0.5);
						aplicacion.getPi().setProgress(aplicacion.getPi().getProgress() + 0.5);

						SimpleDateFormat formatoFechaConsecutiva = new SimpleDateFormat("yyyyMMdd");
						String fechaPaso = formatoFechaConsecutiva.format(new Date());

						String directorio = properties.getProperty("svn.inventory.path") + "/Paso " + numPaso + " "
								+ fechaPaso;

						File dir = new File(directorio);
						dir.mkdir();

						aplicacion.getPb().setProgress(aplicacion.getPb().getProgress() + 0.25);
						aplicacion.getPi().setProgress(aplicacion.getPi().getProgress() + 0.25);

						String fichero = "PaP " + numPaso + " " + fechaPaso + ".xlsx";

						XSSFWorkbook libro = new XSSFWorkbook();

						// Creamos las 3 hojas y generamos sus encabezados
						XSSFSheet hoja = libro.createSheet("Ficheros");
						generarCabecera(libro, hoja);

						XSSFSheet hojaBBDD = libro.createSheet("BBDD");
						generarCabeceraBBDD(libro, hojaBBDD);

						XSSFSheet hojaOtros = libro.createSheet("Otros");
						generarCabeceraOtros(libro, hojaOtros);

						numFilas = 2;
						for (Item item : filteredList) {
							Row fila = hoja.createRow(numFilas);

							Cell celda = fila.createCell(0);
							CellStyle estilo = libro.createCellStyle();
							estilo.setBorderBottom(CellStyle.BORDER_THIN);
							estilo.setBorderTop(CellStyle.BORDER_THIN);
							estilo.setBorderRight(CellStyle.BORDER_THIN);
							estilo.setBorderLeft(CellStyle.BORDER_THIN);
							celda.setCellStyle(estilo);
							celda.setCellValue(item.getGlpi().getValue());
							celda = fila.createCell(1);
							celda.setCellStyle(estilo);
							celda.setCellValue(item.getPath().getValue());
							celda = fila.createCell(2);
							celda.setCellStyle(estilo);
							celda.setCellValue(item.getRevision().getValue());
							celda = fila.createCell(3);
							celda.setCellStyle(estilo);
							celda.setCellValue(item.getAuthor().getValue());
							numFilas++;

						}

						numFilas = 2;
						for (Item item : listaBBDD) {
							for (Item itemFicheros : filteredList) {
								if (item.getGlpi().getValue().equals(itemFicheros.getGlpi().getValue())) {
									Row fila = hojaBBDD.createRow(numFilas);

									Cell celda = fila.createCell(0);
									CellStyle estilo = libro.createCellStyle();
									estilo.setBorderBottom(CellStyle.BORDER_THIN);
									estilo.setBorderTop(CellStyle.BORDER_THIN);
									estilo.setBorderRight(CellStyle.BORDER_THIN);
									estilo.setBorderLeft(CellStyle.BORDER_THIN);
									celda.setCellStyle(estilo);
									celda.setCellValue(item.getGlpi().getValue());
									celda = fila.createCell(1);
									celda.setCellStyle(estilo);
									celda.setCellValue(item.getPath().getValue());
									celda = fila.createCell(2);
									celda.setCellStyle(estilo);
									celda.setCellValue(item.getRevision().getValue());
									celda = fila.createCell(3);
									celda.setCellStyle(estilo);
									celda.setCellValue(item.getAuthor().getValue());
									celda = fila.createCell(4);
									numFilas++;
									break;
								}
							}
						}

						numFilas = 2;
						for (Item item : listaOtros) {
							for (Item itemFicheros : filteredList) {
								if (item.getGlpi().getValue().equals(itemFicheros.getGlpi().getValue())) {
									Row fila = hojaOtros.createRow(numFilas);

									Cell celda = fila.createCell(0);
									CellStyle estilo = libro.createCellStyle();
									estilo.setBorderBottom(CellStyle.BORDER_THIN);
									estilo.setBorderTop(CellStyle.BORDER_THIN);
									estilo.setBorderRight(CellStyle.BORDER_THIN);
									estilo.setBorderLeft(CellStyle.BORDER_THIN);
									celda.setCellStyle(estilo);
									celda.setCellValue(item.getGlpi().getValue());
									celda = fila.createCell(1);
									celda.setCellStyle(estilo);
									celda.setCellValue(item.getPath().getValue());
									celda = fila.createCell(2);
									celda.setCellStyle(estilo);
									celda.setCellValue(item.getRevision().getValue());
									celda = fila.createCell(3);
									celda.setCellStyle(estilo);
									celda.setCellValue(item.getAuthor().getValue());
									celda = fila.createCell(4);
									numFilas++;
									break;
								}
							}
						}

						hoja.autoSizeColumn(1); // Dejamos el autosize para la
												// columna del
												// fichero por si el nombre es
												// más
												// largo
						FileOutputStream out = new FileOutputStream(new File(directorio + "/" + fichero));
						libro.write(out);

						out.close();

						aplicacion.getPb().setProgress(aplicacion.getPb().getProgress() + 0.25);
						aplicacion.getPi().setProgress(aplicacion.getPi().getProgress() + 0.25);
						System.out.println("Excel escrito satisfactoriamente...");
					} else {
						aplicacion.getPb().setProgress(1);
					}
				} catch (Exception e) {
					LOGGER.error(e.getLocalizedMessage(), e);
				}
			}
		});

		return true;
	}

	public HashMap<String, Item> extraer(String glpi) {

		DefaultSVNOptions options = new DefaultSVNOptions();
		SVNClientManager clientManager = SVNClientManager.newInstance(options, repoUser, repoPass);

		//FIXME cambiar con la versión que se quiera
		long startRevision = 280;
		long endRevision = -1; // HEAD (the latest) revision

		SVNRepository repository = null;

		HashMap<String, Item> ficheros = new HashMap<String, Item>();

		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(srcRepoURL));

			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(repoUser,
					repoPass.toCharArray());

			repository.setAuthenticationManager(authManager);

			Collection logEntries = null;

			logEntries = repository.log(new String[] { "" }, null, startRevision, endRevision, true, true);
			for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
				SVNLogEntry logEntry = (SVNLogEntry) entries.next();
				// if (logEntry.getMessage().contains(glpi)) {
				if (logEntry.getMessage().startsWith("GLPI " + glpi)
						|| logEntry.getMessage().startsWith("GLPI  " + glpi)
						|| logEntry.getMessage().startsWith("GLPI: " + glpi)
						|| logEntry.getMessage().startsWith(glpi + " ")
						|| logEntry.getMessage().startsWith(glpi + ".")
						|| logEntry.getMessage().startsWith("GLPI - " + glpi)
						|| logEntry.getMessage().startsWith("GPLI " + glpi)) {
					System.out.println("---------------------------------------------");
					System.out.println("revision: " + logEntry.getRevision());
					System.out.println("author: " + logEntry.getAuthor());
					System.out.println("date: " + logEntry.getDate());
					System.out.println("log message: " + logEntry.getMessage());

					if (logEntry.getChangedPaths().size() > 0) {
						Set changedPathsSet = logEntry.getChangedPaths().keySet();

						for (Iterator changedPaths = changedPathsSet.iterator(); changedPaths.hasNext();) {
							SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry.getChangedPaths()
									.get(changedPaths.next());
							String path = entryPath.getPath().substring(22);
							Item item = new Item(path, String.valueOf(logEntry.getRevision()));
							item.getGlpi().set(glpi);
							item.getAuthor().set(logEntry.getAuthor());
							ficheros.put(entryPath.getPath(), item);
						}
					}
				}
			}
			for (Entry<String, Item> fichero : ficheros.entrySet()) {
				System.out.println("GLPI " + fichero.getValue().getGlpi().getValue() + ": " + fichero.getValue().getPath().getValue() + " "
						+ "(" + fichero.getValue().getRevision().getValue() + ") " + fichero.getValue().getAuthor().getValue());
			}
			totalizadorItem.getTotalFicheros().set(totalizadorItem.getTotalFicheros().get() + ficheros.size());
			totalizadorItem.getPath().set(String.valueOf(totalizadorItem.getTotalFicheros().get()));
			return ficheros;
		} catch (SVNException e) {
			e.printStackTrace();
			return ficheros;
		} finally {
			clientManager.dispose();
		}
	}

	public void history() {

		DefaultSVNOptions options = new DefaultSVNOptions();
		SVNClientManager clientManager = SVNClientManager.newInstance(options, repoUser, repoPass);

		long startRevision = 17400;
		long endRevision = -1; // HEAD (the latest) revision

		SVNRepository repository = null;

		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(srcRepoURL));

			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(repoUser,
					repoPass.toCharArray());

			repository.setAuthenticationManager(authManager);

			Collection logEntries = null;

			logEntries = repository.log(new String[] { "" }, null, startRevision, endRevision, true, true);
			for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
				SVNLogEntry logEntry = (SVNLogEntry) entries.next();
				if (logEntry.getMessage().contains("272524")) {
					System.out.println("---------------------------------------------");
					System.out.println("revision: " + logEntry.getRevision());
					System.out.println("author: " + logEntry.getAuthor());
					System.out.println("date: " + logEntry.getDate());
					System.out.println("log message: " + logEntry.getMessage());

					if (logEntry.getChangedPaths().size() > 0) {
						Set changedPathsSet = logEntry.getChangedPaths().keySet();

						for (Iterator changedPaths = changedPathsSet.iterator(); changedPaths.hasNext();) {
							SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry.getChangedPaths()
									.get(changedPaths.next());
							// if (entryPath.getPath().contains("ROROr")) {
							// encontrado = true;
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
		SVNClientManager clientManager = SVNClientManager.newInstance(options, repoUser, repoPass);

		SVNUpdateClient updateClient = clientManager.getUpdateClient();
		updateClient.setIgnoreExternals(false);

		for (Item item : items) {

			try {

				SVNURL url = SVNURL.parseURIEncoded(srcRepoURL + item.getPath().get());
				File path = new File(srcWorkingCopy + item.getPath().get());
				SVNRevision revision = SVNRevision.parse(item.getRevision().get());
				updateClient.doExport(url, path, revision, revision, "native", true, SVNDepth.INFINITY);
				item.getExported().set(true);

			} catch (SVNException e) {
				LOGGER.error(e.getLocalizedMessage());
			}
		}

		clientManager.dispose();
	}

	public void move(List<Item> items) {

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
		SVNClientManager clientManager = SVNClientManager.newInstance(options, repoUser, repoPass);

		SVNCommitClient commitClient = clientManager.getCommitClient();
		commitClient.setIgnoreExternals(false);

		File[] paths = new File[items.size()];

		for (int p = 0; p < items.size(); p++) {

			File path = new File(dstWorkingCopy + items.get(p).getPath().get());
			paths[p] = path;
		}

		try {

			commitClient.doCommit(paths, false, message, null, null, false, false, SVNDepth.INFINITY);

		} catch (SVNException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			throw new ServiceException(e.getLocalizedMessage(), e);
		}

		clientManager.dispose();
	}

	public boolean isCritico(String path) {

		try {
			ficherosCriticos = classLoader.getResourceAsStream("ficherosCriticos.txt");
			InputStreamReader ficheros = new InputStreamReader(ficherosCriticos);
			BufferedReader lector = new BufferedReader(ficheros);
			List<String> ficherosCriticos = new LinkedList<String>();
			String cadena;
			while ((cadena = lector.readLine()) != null) {
				ficherosCriticos.add(cadena);
				// System.out.println(cadena);
			}
			lector.close();
			for (String ficheroCritico : ficherosCriticos) {
				if (path.contains(ficheroCritico.substring(1)))
					return true;
			}
			return false;

		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			throw new ServiceException(e.getLocalizedMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, Item> extraerMercurio() {

		DefaultSVNOptions options = new DefaultSVNOptions();
		SVNClientManager clientManager = SVNClientManager.newInstance(options, repoUser, repoPass);

		long startRevision = Long.valueOf(properties.getProperty("ultima.revision.subida").toString()) + 1;
		long endRevision = -1; // HEAD (the latest) revision

		SVNRepository repository = null;

		HashMap<String, Item> ficheros = new HashMap<String, Item>();

		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(srcRepoURL));

			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(repoUser,
					repoPass.toCharArray());

			repository.setAuthenticationManager(authManager);

			Collection<Object> logEntries = null;

			logEntries = repository.log(new String[] { "" }, null, startRevision, endRevision, true, true);
			Iterator<Object> entries = logEntries.iterator();
			if (entries.hasNext()) {
				if (logEntryMercurio == null) {
					logEntryMercurio = (SVNLogEntry) entries.next();
				} else {
					while (entries.hasNext()) {
						SVNLogEntry auxiliar = (SVNLogEntry) entries.next();
						if (auxiliar.equals(logEntryMercurio))
							break;
					}
					logEntryMercurio = (SVNLogEntry) entries.next();
				}
				System.out.println("---------------------------------------------");
				System.out.println("revision: " + logEntryMercurio.getRevision());
				System.out.println("author: " + logEntryMercurio.getAuthor());
				System.out.println("date: " + logEntryMercurio.getDate());
				System.out.println("log message: " + logEntryMercurio.getMessage());

				if (logEntryMercurio.getChangedPaths().size() > 0) {
					Set<String> changedPathsSet = logEntryMercurio.getChangedPaths().keySet();

					ficheros.clear();

					for (Iterator<String> changedPaths = changedPathsSet.iterator(); changedPaths.hasNext();) {
						SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntryMercurio.getChangedPaths()
								.get(changedPaths.next());
						String path = entryPath.getPath().substring(10);
						Item item = new Item(path, String.valueOf(logEntryMercurio.getRevision()));
						item.getAuthor().set(logEntryMercurio.getAuthor());
						item.getTipoSVN().set(String.valueOf(entryPath.getType()));
						ficheros.put(entryPath.getPath(), item);
					}
				}
				// }
				for (Entry<String, Item> fichero : ficheros.entrySet()) {
					System.out.println(" " + fichero.getValue().getTipoSVN().get() + " " + fichero.getValue().getPath().get() + " "
							+ fichero.getValue().getRevision().get() + " " + fichero.getValue().getAuthor().get());
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

	public void exportMercurioPro(List<Item> items) {

		DefaultSVNOptions options = new DefaultSVNOptions();
		SVNClientManager clientManager = SVNClientManager.newInstance(options, repoUser, repoPass);

		SVNUpdateClient updateClient = clientManager.getUpdateClient();
		updateClient.setIgnoreExternals(false);

		for (Item item : items) {

			try {

				SVNURL url = SVNURL.parseURIEncoded(srcRepoURLPro + item.getPath().get());
				File path = new File(mercurioWorkingCopyPro + item.getPath().get().replaceAll("fgGesper", "figesper"));
				SVNRevision revision = SVNRevision.parse(item.getRevision().get());
				updateClient.doExport(url, path, revision, revision, "native", true, SVNDepth.INFINITY);
				item.getExported().set(true);

			} catch (SVNException e) {
				LOGGER.error(e.getLocalizedMessage());
			}
		}

		clientManager.dispose();
	}

	public void commitMercurioPro(List<Item> items, String message) throws ServiceException {

		DefaultSVNOptions options = new DefaultSVNOptions();
		SVNClientManager clientManager = SVNClientManager.newInstance(options, repoUser, repoPass);

		SVNWCClient sVNWCClient = clientManager.getWCClient();

		SVNLogEntry logEntry = null;

		SVNStatusClient statusClient = clientManager.getStatusClient();
		statusClient.setIgnoreExternals(true);

		SVNCommitClient commitClient = clientManager.getCommitClient();
		commitClient.setIgnoreExternals(false);

		List<File> ficheros = new ArrayList<File>(items.size());
		List<String> ficherosCriticosDetectados = new ArrayList<String>();

		for (int p = 0; p < items.size(); p++) {

			File path = new File(mercurioWorkingCopyPro + items.get(p).getPath().get().replaceAll("fgGesper", "figesper"));

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
			else {
				ficherosCriticosDetectados.add(path.getPath());
			}
		}

		if (ficheros != null && !ficheros.isEmpty()) {
			try {
				commitClient.doCommit(ficheros.toArray(new File[ficheros.size()]), false, message, null, null, false,
						true, SVNDepth.INFINITY);
				
				logEntryMercurioPro = null;
				
				System.out.println("Commit finalizado con éxito.");

			} catch (SVNException e) {
				LOGGER.error(e.getLocalizedMessage(), e);
				throw new ServiceException(e.getLocalizedMessage(), e);
			}
		}
		
		if (ficherosCriticosDetectados != null && !ficherosCriticosDetectados.isEmpty()) {
			for (String fichero : ficherosCriticosDetectados) {
				System.out.println("¡¡ATENCIÓN!! Fichero crítico detectado: " + fichero);
			}
		}
		
		try {
			
			logEntryMercurioPro = null;
			
			properties.setProperty("pro.ultima.revision.subida", items.get(0).getRevision().get());
			File f = new File("./src/main/resources/caronte.properties");
			OutputStream fichProp;
			fichProp = new FileOutputStream(f);
			properties.store(fichProp, "");
			System.out.println("Actualizado properties con última revisión.");

		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			throw new ServiceException(e.getLocalizedMessage(), e);
		}
		

		clientManager.dispose();
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String, Item> extraerMercurioPro() {

		DefaultSVNOptions options = new DefaultSVNOptions();
		SVNClientManager clientManager = SVNClientManager.newInstance(options, repoUser, repoPass);

		long startRevision = Long.valueOf(properties.getProperty("pro.ultima.revision.subida").toString()) + 1;
		long endRevision = -1; // HEAD (the latest) revision

		SVNRepository repository = null;

		HashMap<String, Item> ficheros = new HashMap<String, Item>();

		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(srcRepoURLPro));

			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(repoUser,
					repoPass.toCharArray());

			repository.setAuthenticationManager(authManager);

			Collection<Object> logEntries = null;

			logEntries = repository.log(new String[] { "" }, null, startRevision, endRevision, true, true);
			Iterator<Object> entries = logEntries.iterator();
			if (entries.hasNext()) {
				if (logEntryMercurioPro == null) {
					logEntryMercurioPro = (SVNLogEntry) entries.next();
				} else {
					while (entries.hasNext()) {
						SVNLogEntry auxiliar = (SVNLogEntry) entries.next();
						if (auxiliar.equals(logEntryMercurioPro))
							break;
					}
					logEntryMercurioPro = (SVNLogEntry) entries.next();
				}
				System.out.println("---------------------------------------------");
				System.out.println("revision: " + logEntryMercurioPro.getRevision());
				System.out.println("author: " + logEntryMercurioPro.getAuthor());
				System.out.println("date: " + logEntryMercurioPro.getDate());
				System.out.println("log message: " + logEntryMercurioPro.getMessage());

				if (logEntryMercurioPro.getChangedPaths().size() > 0) {
					Set<String> changedPathsSet = logEntryMercurioPro.getChangedPaths().keySet();

					ficheros.clear();

					for (Iterator<String> changedPaths = changedPathsSet.iterator(); changedPaths.hasNext();) {
						SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntryMercurioPro.getChangedPaths()
								.get(changedPaths.next());
						String path = entryPath.getPath().substring(10);
						Item item = new Item(path, String.valueOf(logEntryMercurioPro.getRevision()));
						item.getAuthor().set(logEntryMercurioPro.getAuthor());
						item.getTipoSVN().set(String.valueOf(entryPath.getType()));
						ficheros.put(entryPath.getPath(), item);
					}
				}
				// }
				for (Entry<String, Item> fichero : ficheros.entrySet()) {
					System.out.println(" " + fichero.getValue().getTipoSVN().get() + " " + fichero.getValue().getPath().get() + " "
							+ fichero.getValue().getRevision().get() + " " + fichero.getValue().getAuthor().get());
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

	public void exportMercurio(List<Item> items) {

		DefaultSVNOptions options = new DefaultSVNOptions();
		SVNClientManager clientManager = SVNClientManager.newInstance(options, repoUser, repoPass);

		SVNUpdateClient updateClient = clientManager.getUpdateClient();
		updateClient.setIgnoreExternals(false);

		for (Item item : items) {

			try {

				SVNURL url = SVNURL.parseURIEncoded(srcRepoURL + item.getPath().get());
				File path = new File(mercurioWorkingCopy + item.getPath().get().replaceAll("fgGesper", "figesper"));
				SVNRevision revision = SVNRevision.parse(item.getRevision().get());
				updateClient.doExport(url, path, revision, revision, "native", true, SVNDepth.INFINITY);
				item.getExported().set(true);

			} catch (SVNException e) {
				LOGGER.error(e.getLocalizedMessage());
			}
		}

		clientManager.dispose();
	}

	public void commitMercurio(List<Item> items, String message) throws ServiceException {

		DefaultSVNOptions options = new DefaultSVNOptions();
		SVNClientManager clientManager = SVNClientManager.newInstance(options, repoUser, repoPass);

		SVNWCClient sVNWCClient = clientManager.getWCClient();

		SVNLogEntry logEntry = null;

		SVNStatusClient statusClient = clientManager.getStatusClient();
		statusClient.setIgnoreExternals(true);

		SVNCommitClient commitClient = clientManager.getCommitClient();
		commitClient.setIgnoreExternals(false);

		List<File> ficheros = new ArrayList<File>(items.size());
		List<String> ficherosCriticosDetectados = new ArrayList<String>();

		for (int p = 0; p < items.size(); p++) {

			File path = new File(mercurioWorkingCopy + items.get(p).getPath().get().replaceAll("fgGesper", "figesper"));

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
			else {
				ficherosCriticosDetectados.add(path.getPath());
			}
		}

		if (ficheros != null && !ficheros.isEmpty()) {
			try {
				commitClient.doCommit(ficheros.toArray(new File[ficheros.size()]), false, message, null, null, false,
						true, SVNDepth.INFINITY);
				
				logEntryMercurio = null;
				
				System.out.println("Commit finalizado con éxito.");

			} catch (SVNException e) {
				LOGGER.error(e.getLocalizedMessage(), e);
				throw new ServiceException(e.getLocalizedMessage(), e);
			}
		}
		
		if (ficherosCriticosDetectados != null && !ficherosCriticosDetectados.isEmpty()) {
			for (String fichero : ficherosCriticosDetectados) {
				System.out.println("¡¡ATENCIÓN!! Fichero crítico detectado: " + fichero);
			}
		}
		
		try {
			
			logEntryMercurio = null;
			
			properties.setProperty("ultima.revision.subida", items.get(0).getRevision().get());
			File f = new File("./src/main/resources/caronte.properties");
			OutputStream fichProp;
			fichProp = new FileOutputStream(f);
			properties.store(fichProp, "");
			System.out.println("Actualizado properties con última revisión.");

		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			throw new ServiceException(e.getLocalizedMessage(), e);
		}
		

		clientManager.dispose();
	}


	public String getRepoUser() {
		return repoUser;
	}

	public String getRepoPass() {
		return repoPass;
	}

	public String getSrcRepoURL() {
		return srcRepoURL;
	}

	public SVNLogEntry getLogEntryMercurio() {
		return logEntryMercurio;
	}
	
	public SVNLogEntry getLogEntryMercurioPro() {
		return logEntryMercurioPro;
	}
}
