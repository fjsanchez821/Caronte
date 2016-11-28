package es.carm.figesper.caronte;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;

import javax.swing.Timer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class App extends Application {

	// Declaración de atributos principales

	private Service service;
	private HashMap<String, HashMap<String, Item>> ficherosExtraidos;

	ServiceMercurio servicioMercurio = new ServiceMercurio();
	private HashMap<String, Item> ficherosMercurio;

	private Stage primaryStage; // Contenedor principal de las pantallas
								// (CANVAS)
	private BorderPane root; // Panel principal de la primera pantalla
	private Scene scene; // Primera pantalla
	private BorderPane root2; // Panel principal de la segunda pantalla
	private Scene scene2; // Segunda pantalla
	private Stage secondaryStage;
	private BorderPane root3; // Panel principal de la ventana emergente
	private Scene scene3; // Ventana Emergente

	// Componentes primera pantalla

	private VBox topContainer;

	private File file;

	private MenuBar menuBar;

	private Label lblOdsPath;
	private TextField txtOdsPath;
	private Button btnOdsPath;

	private TableView<Item> tblItems;
	private ObservableList<Item> itemList;
	private CheckBox chkAll;

	private Button btnExtraer;
	private Button btnActualizar;
	private Label lblMessage;
	private TextField txtMessage;
	private Button btnCommit;
	private Button btnExportarAFichero;

	private Properties properties;

	// Componentes segunda pantalla

	// private VBox topContainer2;

	// private File file2;

	private MenuBar menuBar2;

	// private Label lblExcelPath;
	// private TextField txtExcelPath;
	// private Button btnExcelPath;

	private TableView<Item> tblItems2;
	// private ObservableList<Item> itemList2;
	private CheckBox chkAll2;

	private Button btnExport;
	private Button btnMove;
	// private Label lblMessage2;
	// private TextField txtMessage2;
	// private Button btnCommit2;
	private Button btnVolver;

	private ClassLoader classLoader;

	private ProgressBar pb;
	private ProgressIndicator pi;
	// private Slider slider;
	private HBox hb;

	private Timer timer;
	private Timer timer2;

	private FilteredList<Item> listaAExportar;
	private List<Item> listaDependenciasDeTicketsASubir;
	private List<String> listaRutasInsertadas;
	private List<Item> listaComprobarRutas;

	private MenuBar menuBarOpciones;
	private Menu menuPaso;
	private Menu menuSync;
	private MenuItem itmPaso;
	private MenuItem itmSync;

	private boolean eventoFinalizado = true;

	public BorderPane getRoot() {
		return root;
	}

	public Timer getTimer() {
		return timer;
	}

	public ProgressBar getPb() {
		return pb;
	}

	public void setPb(ProgressBar pb) {
		this.pb = pb;
	}

	public ProgressIndicator getPi() {
		return pi;
	}

	public void setPi(ProgressIndicator pi) {
		this.pi = pi;
	}

	public App() {
		service = new Service();
		service.setAplicacion(this);
		ficherosExtraidos = new HashMap<String, HashMap<String, Item>>();
	}

	// Inicialización de la aplicación
	@Override
	public void start(Stage primaryStage) throws Exception {

		this.primaryStage = primaryStage;

		root = new BorderPane();
		scene = new Scene(root, 1500, 800);
		root2 = new BorderPane();
		scene2 = new Scene(root2, 1500, 800);
		root3 = new BorderPane();
		scene3 = new Scene(root3, 300, 100);

		scene.getStylesheets().add(this.getClass().getResource("console.css").toExternalForm());

		secondaryStage = new Stage();

		primaryStage.setScene(scene);
		primaryStage.setTitle("Caronte");
		primaryStage.show();

		initComponents(root);

	}

	private void initComponents(BorderPane root) {

		VBox topContainer = new VBox();
		root.setTop(topContainer);

		root.setMaxHeight(25);

		menuBar = new MenuBar();
		Menu menuGLPI = new Menu("GLPI");
		menuBar.getMenus().add(menuGLPI);

		menuBarOpciones = new MenuBar();
		menuBarOpciones.prefWidthProperty().bind(primaryStage.widthProperty());

		root.setTop(menuBarOpciones);

		menuPaso = new Menu("Paso");
		menuSync = new Menu("Sync");

		itmPaso = new MenuItem("Paso");
		itmSync = new MenuItem("Sync");

		menuPaso.getItems().addAll(itmPaso);
		menuSync.getItems().addAll(itmSync);

		menuBarOpciones.getMenus().addAll(menuPaso, menuSync);

		itmSync.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				initSync(root);
				lblOdsPath.setText("item");
			}
		});

		BorderPane pnlOdsPath = new BorderPane();// Contenedor de centralArriba
													// y centralCentro
		BorderPane centralArriba = new BorderPane();// para la barra del
													// buscador
		BorderPane centralCentro = new BorderPane();// para la lista de ficheros

		pnlOdsPath.setPadding(new Insets(1));
		root.setCenter(pnlOdsPath);

		pnlOdsPath.setTop(centralArriba);
		pnlOdsPath.setCenter(centralCentro);

		lblOdsPath = new Label("Seguimientos Tickets");
		lblOdsPath.setPadding(new Insets(3, 5, 0, 0));
		centralArriba.setLeft(lblOdsPath);

		txtOdsPath = new TextField();
		txtOdsPath.setEditable(false);
		centralArriba.setCenter(txtOdsPath);

		btnOdsPath = new Button("...");
		centralArriba.setRight(btnOdsPath);

		btnOdsPath.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Abrir Archivo de Seguimiento de Tickets");
				ArrayList<String> listExtensions = new ArrayList<>();
				listExtensions.add("*.xlsx");
				listExtensions.add("*.ods");
				FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Seguimiento", listExtensions);
				fileChooser.getExtensionFilters().add(filter);
				fileChooser.setInitialDirectory(new File(service.getSvnSeguimientoPath()));
				file = fileChooser.showOpenDialog(primaryStage);
				if (file != null) {
					try {
						txtOdsPath.setText(file.getAbsolutePath());
						itemList.addAll(service.parse(file));
						chkAll.setDisable(false);
						chkAll.setSelected(false);
					} catch (ServiceException e) {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Exception");
						alert.setHeaderText(e.getClass().getSimpleName());
						alert.setContentText(e.getMessage());
						alert.showAndWait();
					}
				}
			}
		});

		// --

		tblItems = new TableView<Item>();
		tblItems.setEditable(true);
		itemList = FXCollections.observableArrayList();

		chkAll = new CheckBox();
		chkAll.setDisable(true);
		chkAll.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				for (Item item : itemList) {
					item.getSelected().set(newValue);
				}
			}
		});

		TableColumn<Item, Boolean> selected = new TableColumn<Item, Boolean>();
		selected.setGraphic(chkAll);
		selected.setSortable(false);
		selected.setCellValueFactory(c -> c.getValue().getSelected());
		selected.setCellFactory(tc -> new CheckBoxTableCell<Item, Boolean>());
		selected.prefWidthProperty().set(30);
		tblItems.getColumns().add(selected);

		TableColumn<Item, String> glpi = new TableColumn<Item, String>("GLPI");
		glpi.setCellValueFactory(c -> c.getValue().getGlpi());
		glpi.prefWidthProperty().set(70); // bind(tblItems.widthProperty().subtract(600));
		tblItems.getColumns().add(glpi);

		TableColumn<Item, String> titulo = new TableColumn<Item, String>("Título");
		titulo.setSortable(false);
		titulo.setEditable(false);
		titulo.setCellValueFactory(c -> c.getValue().getTitulo());
		titulo.prefWidthProperty().set(500);
		tblItems.getColumns().add(titulo);

		TableColumn<Item, String> tipo = new TableColumn<Item, String>("Tipo");
		tipo.setSortable(false);
		tipo.setEditable(false);
		tipo.setCellValueFactory(c -> c.getValue().getTipo());
		tipo.prefWidthProperty().set(100);
		tblItems.getColumns().add(tipo);

		TableColumn<Item, String> vencimiento = new TableColumn<Item, String>("Vencimiento");
		vencimiento.setSortable(false);
		vencimiento.setEditable(false);
		vencimiento.setCellValueFactory(c -> c.getValue().getVencimiento());
		vencimiento.prefWidthProperty().set(100);
		tblItems.getColumns().add(vencimiento);

		TableColumn<Item, String> responsable = new TableColumn<Item, String>("Responsable");
		responsable.setSortable(false);
		responsable.setEditable(false);
		responsable.setCellValueFactory(c -> c.getValue().getResponsable());
		responsable.prefWidthProperty().set(100);
		tblItems.getColumns().add(responsable);

		TableColumn<Item, String> asignado = new TableColumn<Item, String>("Asignado a");
		asignado.setSortable(false);
		asignado.setEditable(false);
		asignado.setCellValueFactory(c -> c.getValue().getAsignado());
		asignado.prefWidthProperty().set(100);
		tblItems.getColumns().add(asignado);

		TableColumn<Item, String> estado = new TableColumn<Item, String>("Estado");
		estado.setCellValueFactory(c -> c.getValue().getEstado());
		estado.prefWidthProperty().set(100);
		tblItems.getColumns().add(estado);

		TableColumn<Item, String> entorno = new TableColumn<Item, String>("Entorno");
		entorno.setCellValueFactory(c -> c.getValue().getEntorno());
		entorno.prefWidthProperty().set(100);
		tblItems.getColumns().add(entorno);

		TableColumn<Item, String> validado = new TableColumn<Item, String>("Validado");
		validado.setCellValueFactory(c -> c.getValue().getValidado());
		validado.prefWidthProperty().set(70);
		tblItems.getColumns().add(validado);

		tblItems.setItems(itemList);

		pnlOdsPath.setCenter(tblItems);

		// --

		BorderPane pnlBottom = new BorderPane();
		root.setBottom(pnlBottom);

		GridPane pnlActions = new GridPane();
		pnlActions.setHgap(5);
		pnlActions.setPadding(new Insets(5));

		btnExtraer = new Button("Extraer ficheros");
		pnlActions.add(btnExtraer, 0, 0);
		btnExtraer.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				FilteredList<Item> filteredList = itemList.filtered(new Predicate<Item>() {

					@Override
					public boolean test(Item item) {

						if (item.getSelected().get()) {
							return true;
						}

						return false;
					}
				});

				for (Item item : filteredList) {
					ficherosExtraidos.put(item.getGlpi().getValue(), service.extraer(item.getGlpi().getValue()));
				}
				listaDependenciasDeTicketsASubir = service.comprobarDependencias(ficherosExtraidos);
				listaRutasInsertadas = new ArrayList<String>();
				btnExtraer.setDisable(true);
				initComponents2(root2);
				scene2.setRoot(root2);
				primaryStage.setScene(scene2);
				primaryStage.show();

			}
		});

		btnActualizar = new Button("Actualizar lista");
		pnlActions.add(btnActualizar, 1, 0);
		btnActualizar.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// Aqui hay que volver a leer fichero y pintar los elementos en
				// la ventana principal

				itemList.removeAll(itemList);

				file = new File(
						properties.getProperty("svn.seguimiento.path") + "/" + "GLPI FIGESPER - CARM C.HACIENDA "
								+ new SimpleDateFormat("yyyyMM").format(new Date()) + ".ods");

				itemList.addAll(service.parse(file));
			}
		});
		pnlBottom.setLeft(pnlActions);

		BorderPane pnlBottomRight = new BorderPane();
		pnlBottomRight.setDisable(true);
		pnlBottomRight.setPadding(new Insets(5));
		pnlBottom.setCenter(pnlBottomRight);

		lblMessage = new Label("Mensaje");
		lblMessage.setPadding(new Insets(3, 5, 0, 10));
		pnlBottomRight.setLeft(lblMessage);

		txtMessage = new TextField();
		pnlBottomRight.setCenter(txtMessage);

		// Inicializamos la primera ventana con los valores del fichero de
		// segumientos actual
		properties = new Properties();
		try {

			classLoader = getClass().getClassLoader();
			properties.load(classLoader.getResourceAsStream("caronte.properties"));

		} catch (IOException e) {
			System.out.println("Falla aqui");
			throw new ServiceException(e.getLocalizedMessage(), e);
		}

		file = new File(properties.getProperty("svn.seguimiento.path") + "/" + "GLPI FIGESPER - CARM C.HACIENDA "
				+ new SimpleDateFormat("yyyyMM").format(new Date()) + ".ods");

		itemList.addAll(service.parse(file));
		chkAll.setDisable(false);
		chkAll.setSelected(false);
	}

	private void initSync(BorderPane root) {

		btnExtraer.setVisible(false);
		btnActualizar.setVisible(false);
		lblMessage.setVisible(false);
		txtMessage.setVisible(false);

		TextArea areaTextoConsola = new TextArea();
		BorderPane pnlBottom = new BorderPane();
		root.setCenter(pnlBottom);

		// Comentar/Descomentar estas 4 lineas para que los mensajes
		// desaparezcan/aparezcan por consola
		Console consola = new Console(areaTextoConsola);
		PrintStream ps = new PrintStream(consola, true);
		System.setOut(ps);
		System.setErr(ps);
		pnlBottom.setCenter(areaTextoConsola);

		menuBarOpciones = new MenuBar();
		menuBarOpciones.prefWidthProperty().bind(primaryStage.widthProperty());

		root.setTop(menuBarOpciones);

		menuPaso = new Menu("Paso");
		menuSync = new Menu("Sync");

		itmPaso = new MenuItem("Paso");
		itmSync = new MenuItem("Sync");

		menuPaso.getItems().addAll(itmPaso);
		menuSync.getItems().addAll(itmSync);

		menuBarOpciones.getMenus().addAll(menuPaso, menuSync);

		menuPaso.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				initComponents(root);
				lblOdsPath.setText("item");
			}
		});

		lanzarHiloMercurio(areaTextoConsola);

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				areaTextoConsola.requestFocus();
			}
		});
	}

	private void lanzarHiloMercurio(TextArea txtArea) {
		if (servicioMercurio.getSrcRepoUser().equals("pgm18e")) {
			Thread t = new Thread(() -> {
				if (eventoFinalizado) {
					ficherosMercurio = servicioMercurio.extraer();
					if (txtArea.getText().equals("") || ficherosMercurio.size() == 0)
						txtArea.appendText("@Mercurio>");
					eventoFinalizado = false;
				}
			});
			t.run();
		}
	}

	private class Console extends OutputStream {
		private TextArea txtArea;

		public Console(TextArea txtArea) {
			this.txtArea = txtArea;
			txtArea.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
				try {
					if (servicioMercurio.getSrcRepoUser().equals("pgm18e")) {
						switch (keyEvent.getCode()) {
						case ENTER:
							String cadena = txtArea.getText().replaceAll("\n", "");
							if (cadena != null) {
								try {
									char res = new String(cadena.substring(cadena.length() - 1, cadena.length()))
											.toCharArray()[0];

									if (res == 's' || res == 'S') {
										List<Item> lista = new LinkedList<Item>();
										lista.addAll(ficherosMercurio.values());
										servicioMercurio.export(lista);
										servicioMercurio.commit(lista, servicioMercurio.getLogEntry().getMessage());
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							eventoFinalizado = true;
							lanzarHiloMercurio(txtArea);
							break;
						default:
							break;
						}
					}
				} catch (Exception e) {
					System.out.println("La excepción da aquí.");
					e.printStackTrace();
				}
			});
		}

		@Override
		public void write(int i) throws IOException {

			txtArea.appendText(String.valueOf((char) i));
		}

	}

	private void initComponents2(BorderPane root) {

		VBox topContainer2 = new VBox();
		root.setTop(topContainer);

		menuBar2 = new MenuBar();
		Menu menuGLPI = new Menu("GLPI");
		menuBar2.getMenus().add(menuGLPI);
		topContainer2.getChildren().addAll(menuBar2);

		itemList = FXCollections.observableArrayList();
		for (HashMap<String, Item> ficheros : ficherosExtraidos.values()) {
			itemList.addAll(ficheros.values());
		}

		listaRutasInsertadas.clear();

		tblItems2 = new TableView<Item>();
		tblItems2.setEditable(true);

		chkAll2 = new CheckBox();
		chkAll2.setDisable(true);
		chkAll2.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				for (Item item : itemList) {
					item.getSelected().set(newValue);
				}
			}
		});

		TableColumn<Item, Boolean> selected = new TableColumn<Item, Boolean>();
		selected.setGraphic(chkAll2);
		selected.setSortable(false);
		selected.setCellValueFactory(c -> c.getValue().getSelected());
		selected.setCellFactory(tc -> new CheckBoxTableCell<Item, Boolean>());
		selected.prefWidthProperty().set(30);
		tblItems2.getColumns().add(selected);

		TableColumn<Item, String> glpi = new TableColumn<Item, String>("GLPI");
		glpi.setCellValueFactory(c -> c.getValue().getGlpi());
		glpi.prefWidthProperty().set(100); // .bind(tblItems.widthProperty().subtract(100));
		tblItems2.getColumns().add(glpi);

		TableColumn<Item, String> path = new TableColumn<Item, String>("Path");
		path.setCellValueFactory(c -> c.getValue().getPath());
		path.prefWidthProperty().set(600);
		tblItems2.getColumns().add(path);

		TableColumn<Item, String> revision = new TableColumn<Item, String>("Revisión");
		revision.setCellValueFactory(c -> c.getValue().getRevision());
		revision.prefWidthProperty().set(70);
		tblItems2.getColumns().add(revision);

		TableColumn<Item, String> author = new TableColumn<Item, String>("Autor");
		author.setCellValueFactory(c -> c.getValue().getAuthor());
		author.prefWidthProperty().set(70);
		tblItems2.getColumns().add(author);

		TableColumn<Item, Boolean> exported = new TableColumn<Item, Boolean>("E");
		exported.setSortable(false);
		exported.setEditable(false);
		exported.setCellValueFactory(c -> c.getValue().getExported());
		exported.setCellFactory(tc -> new CheckBoxTableCell<Item, Boolean>());
		exported.prefWidthProperty().set(30);
		tblItems2.getColumns().add(exported);

		TableColumn<Item, Boolean> moved = new TableColumn<Item, Boolean>("M");
		moved.setSortable(false);
		moved.setEditable(false);
		moved.setCellValueFactory(c -> c.getValue().getMoved());
		moved.setCellFactory(tc -> new CheckBoxTableCell<Item, Boolean>());
		moved.prefWidthProperty().set(30);
		tblItems2.getColumns().add(moved);

		listaComprobarRutas = service.leerPendienteAProduccion(0);// Aqui se
																	// leen las
																	// rutas de
																	// los
																	// ficheros
																	// para
																	// pasar a
																	// produccion

		for (Item itemLista : itemList) {// Lista con los elementos a modificar
			itemLista.setSelected(new SimpleBooleanProperty(false));
			itemLista.setAnotado(new SimpleStringProperty("Path"));
			for (Item itemExcel : listaComprobarRutas) { // Lista con los
															// elementos de la
															// excel "Pendiente
															// paso a
															// produccion"
				if (itemExcel.getPath().toString().equalsIgnoreCase(itemLista.getPath().toString())) {
					itemLista.setAnotado(new SimpleStringProperty("Revisión"));
					if (itemExcel.getRevision().toString().equalsIgnoreCase(itemLista.getRevision().toString())) {

						itemLista.setAnotado(new SimpleStringProperty(""));
						itemLista.setSelected(new SimpleBooleanProperty(true));
					}
				}
				/*
				 * if(itemLista.getRevisionDependiente()!=null){
				 * System.out.println(Integer.parseInt(itemLista.
				 * getRevisionDependiente().getValue().toString())); }
				 */
			}
			if (itemLista.getGlpiDependiente() != null) {
				if (!(itemLista.getGlpiDependiente().getValue().toString()).equalsIgnoreCase("")) {
					itemLista.setAnotado(new SimpleStringProperty(""));
				}
			}
		}

		for (Item itemLista : itemList) {
			for (Item itemAux : itemList) {
					if (itemLista.getPath().equals(itemAux.getPath())) {
						if (Integer.parseInt(itemLista.getRevision().getValue()) > Integer
								.parseInt(itemAux.getRevision().getValue())) {
							System.out.println("Dentro2");
							itemLista.setSelected(new SimpleBooleanProperty(true));
							itemAux.setSelected(new SimpleBooleanProperty(false));
						}
					}
			}
		}

		TableColumn<Item, String> glpiDependiente = new TableColumn<Item, String>("GLPIs Dependientes");
		glpiDependiente.setCellValueFactory(c -> c.getValue().getGlpiDependiente());
		glpiDependiente.prefWidthProperty().set(250);
		glpiDependiente.setCellFactory(c -> {
			return new TableCell<Item, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {

					@SuppressWarnings("unchecked")
					TableRow<Item> currentRow = getTableRow();

					super.updateItem(item, empty);

					if (getItem() != null)
						setText(currentRow.getItem().getGlpiDependiente().get());
					else {
						setText("");
						if (currentRow.getIndex() % 2 == 0)
							currentRow.setStyle(
									"-fx-background-color:white; .table-row-cell:selected {     -fx-background-color: brown;     -fx-text-inner-color: white;}");
						else
							currentRow.setStyle(
									"-fx-background-color:whitesmoke .table-row-cell:selected {     -fx-background-color: brown;     -fx-text-inner-color: white;}");
					}
					setGraphic(null);

					if (!isEmpty() && item != null) {

						if (item.equals(""))
							currentRow.setStyle("-fx-background-color:lightcoral");
						else {
							boolean seAnula = false;
							for (Item item2 : listaDependenciasDeTicketsASubir) {
								if (item2.getGlpi().getValue().equals(item))
									seAnula = true;
							}
							if (seAnula) {
								currentRow.setStyle("-fx-background-color:green");
								if (listaRutasInsertadas.contains(currentRow.getItem().getPath().getValue())
										&& !mayorVersion(currentRow.getItem().getPath().getValue(),
												currentRow.getItem().getRevision().getValue())) {
									itemList.get(itemList.indexOf(currentRow.getItem())).getSelected().set(false);
									currentRow.setDisable(true);
								} else {
									listaRutasInsertadas.add(currentRow.getItem().getPath().getValue());
								}
							} else
								currentRow.setStyle("-fx-background-color:red");
						}
					}
				}

				private boolean mayorVersion(String path, String revision) {
					int mayor = 0;
					for (HashMap<String, Item> ficheros : ficherosExtraidos.values()) {
						for (Item item : ficheros.values()) {
							if (item.getPath().getValue().equals(path)) {
								if (Integer.parseInt(item.getRevision().getValue()) > mayor)
									mayor = Integer.parseInt(item.getRevision().getValue());
							}
						}
					}
					return (Integer.parseInt(revision) == mayor);
				}
			};
		});
		tblItems2.getColumns().add(glpiDependiente);

		TableColumn<Item, String> revisionDependiente = new TableColumn<Item, String>("Versiones Dependientes");
		revisionDependiente.setCellValueFactory(c -> c.getValue().getRevisionDependiente());
		revisionDependiente.prefWidthProperty().set(200);
		tblItems2.getColumns().add(revisionDependiente);

		TableColumn<Item, String> anotado = new TableColumn<Item, String>("Anotado");
		anotado.setSortable(false);
		anotado.setEditable(false);

		// Poner a true que no está en ambos excels

		// listaDependenciasDeTicketsASubir//Lista del excel pendiente
		// produccion

		// Comprueba que ruta y versión coinciden

		String num1, num2;

		for (int i = 0; i < itemList.size() - 1; ++i) {// Bucle hecho para
														// comprobar la mayor
														// revision que haya en
														// el excel
			for (int j = i + 1; j < itemList.size(); ++j) {
				if ((itemList.get(i).getPath().getValue().toString()
						.equalsIgnoreCase(itemList.get(j).getPath().getValue().toString()))) {
					num1 = itemList.get(i).getRevision().getValue().toString();
					num2 = itemList.get(j).getRevision().getValue().toString();
					if (Integer.parseInt(num1) > Integer.parseInt(num2)) {
						itemList.get(j).setSelected(new SimpleBooleanProperty(false));
						itemList.get(j).setAnotado(new SimpleStringProperty(""));
					}

				}
			}
		}

		anotado.setCellValueFactory(c -> c.getValue().getAnotado());
		anotado.setCellFactory(c -> {
			return new TableCell<Item, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					@SuppressWarnings("unchecked")
					TableRow<Item> currentRow = getTableRow();
					super.updateItem(item, empty);

					if (getItem() != null) {
						setText(currentRow.getItem().getAnotado().getValue());
						if ((currentRow.getItem().getAnotado().getValue().equalsIgnoreCase("path")
								|| currentRow.getItem().getAnotado().getValue().equalsIgnoreCase("Revisión"))) {
							currentRow.setStyle("-fx-background-color:orange");
						}
					}
				}
			};
		});

		anotado.prefWidthProperty().set(150);
		tblItems2.getColumns().add(anotado); // revisionDependiente.setCellValueFactory(c
												// ->
												// c.getValue().getRevisionDependiente());

		itemList.add(service.getTotalizadorItem()); // Añadimos el totalizador

		tblItems2.setItems(itemList);

		tblItems2.getSortOrder().add(glpi);
		tblItems2.sort();

		root.setCenter(tblItems2);

		// --

		BorderPane pnlBottom = new BorderPane();
		root.setBottom(pnlBottom);

		GridPane pnlActions = new GridPane();
		pnlActions.setHgap(5);
		pnlActions.setPadding(new Insets(5));

		btnExport = new Button("Export items");
		pnlActions.add(btnExport, 0, 0);
		btnExport.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				FilteredList<Item> filteredList = itemList.filtered(new Predicate<Item>() {

					@Override
					public boolean test(Item item) {

						if (item.getSelected().get()) {
							return true;
						}

						return false;
					}
				});

				service.export(filteredList);
				btnExport.setDisable(true);
				btnMove.setDisable(false);
			}
		});

		btnMove = new Button("Move items");
		btnMove.setDisable(true);
		pnlActions.add(btnMove, 1, 0);
		btnMove.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				FilteredList<Item> filteredList = itemList.filtered(new Predicate<Item>() {

					@Override
					public boolean test(Item item) {

						if (item.getSelected().get() && item.getExported().get()) {
							return true;
						}

						return false;
					}
				});

				service.move(filteredList);
				btnMove.setDisable(true);
				txtMessage.setDisable(false);
				btnCommit.setDisable(false);
			}
		});

		pnlBottom.setLeft(pnlActions);

		BorderPane pnlBottomRight = new BorderPane();
		pnlBottomRight.setDisable(true);
		pnlBottomRight.setPadding(new Insets(5));
		pnlBottom.setCenter(pnlBottomRight);

		lblMessage = new Label("Message");
		// lblMessage.setDisable(true);
		lblMessage.setPadding(new Insets(3, 5, 0, 10));
		pnlBottomRight.setLeft(lblMessage);

		txtMessage = new TextField();
		// txtMessage.setDisable(true);
		pnlBottomRight.setCenter(txtMessage);

		btnCommit = new Button("Commit");
		BorderPane.setAlignment(btnCommit, Pos.CENTER);
		// btnCommit.setDisable(true);
		pnlBottomRight.setRight(btnCommit);
		btnCommit.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				FilteredList<Item> filteredList = itemList.filtered(new Predicate<Item>() {

					@Override
					public boolean test(Item item) {

						if (item.getSelected().get() && item.getExported().get() && item.getMoved().get()) {
							return true;
						}

						return false;
					}
				});

				service.commit(filteredList, txtMessage.getText());
			}
		});

		btnVolver = new Button("Volver");
		// pnlActions.add(btnVolver, 3, 0);
		BorderPane.setAlignment(btnVolver, Pos.CENTER);
		// btnCommit.setDisable(true);
		pnlBottom.setRight(btnVolver);
		btnVolver.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				service.setTotalizadorItem(new Item(0));
				file = null;
				ficherosExtraidos.clear();
				primaryStage.setScene(scene);
				initComponents(getRoot());

			}
		});

		btnExportarAFichero = new Button("Exportar a Fichero Excel");
		pnlActions.add(btnExportarAFichero, 2, 0);
		btnExportarAFichero.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				FilteredList<Item> filteredList = itemList.filtered(new Predicate<Item>() {

					@Override
					public boolean test(Item item) {

						if (item.getSelected().get()) {
							return true;
						}

						return false;
					}
				});

				listaAExportar = filteredList;

				secondaryStage.setScene(scene3);
				secondaryStage.setTitle("Progreso");
				secondaryStage.show();
				initComponents3(root3);
				timer2.start();
			}
		});

	}

	private void initComponents3(BorderPane root) {

		root.setTop(topContainer);

		pb = new ProgressBar(0);
		pi = new ProgressIndicator(0);

		timer = new Timer(1000, new ActionListener() {

			@Override
			public void actionPerformed(java.awt.event.ActionEvent arg0) {
				pb.setProgress(pb.getProgress() + 0.02);
				pi.setProgress(pi.getProgress() + 0.02);
				if (pb.getProgress() == 1) {
					timer.stop();
					secondaryStage.hide();
				} else {
					timer.restart();
				}
			}
		});

		timer2 = new Timer(100, new ActionListener() {

			@Override
			public void actionPerformed(java.awt.event.ActionEvent arg0) {
				service.exportarAFichero(listaAExportar);
				timer2.stop();
			}
		});

		hb = new HBox();
		hb.setLayoutY(35);
		hb.setSpacing(5);
		hb.setAlignment(Pos.TOP_CENTER);
		hb.getChildren().addAll(pb, pi);
		scene3.setRoot(hb);
	}

	public static void main(String[] args) {
		// Service servicio = new Service();
		// File fichero = new File(
		// "T:\\DGI\\Que\\SIC\\Proyectos\\Gesper\\GP METAENLACE\\Seguimiento
		// Tickets\\GLPI - 2016\\GLPI FIGESPER - CARM C.HACIENDA 201605.ods");
		// List<Item> items = servicio.parse(fichero);
		// for (Item item : items) {
		// servicio.extraer(item.getGlpi().getValue());
		// }
		launch(args);
	}
}
