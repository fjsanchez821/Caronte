package es.carm.figesper.caronte;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Item {

	private SimpleBooleanProperty selected;
	private SimpleBooleanProperty exported;
	private SimpleBooleanProperty moved;
	private SimpleStringProperty path;
	private SimpleStringProperty revision;
	private SimpleStringProperty author;
	private SimpleStringProperty glpi;
	private SimpleStringProperty titulo;
	private SimpleStringProperty tipo;
	private SimpleStringProperty vencimiento;
	private SimpleStringProperty responsable;
	private SimpleStringProperty asignado;
	private SimpleStringProperty estado;
	private SimpleStringProperty entorno;
	private SimpleStringProperty validado;
	private SimpleBooleanProperty newItem;
	private SimpleIntegerProperty totalFicheros;
	private SimpleStringProperty anotado; // Marca si está en ambos excels

	private SimpleStringProperty desarrollo;
	private SimpleStringProperty pruebas;
	private SimpleStringProperty rutaSVN;

	// Dependencia (Si existe)
	private SimpleStringProperty revisionDependiente;
	private SimpleStringProperty glpiDependiente;

	public Item(String path, String revision) {

		this.selected = new SimpleBooleanProperty(true);
		this.exported = new SimpleBooleanProperty(false);
		this.moved = new SimpleBooleanProperty(false);
		this.path = new SimpleStringProperty(path);
		this.revision = new SimpleStringProperty(revision);
		this.glpi = new SimpleStringProperty("");
		this.author = new SimpleStringProperty("");
		this.newItem = new SimpleBooleanProperty(false);
	}

	public Item(String glpi, String path, String revision, String author) {

		this.selected = new SimpleBooleanProperty(true);
		this.exported = new SimpleBooleanProperty(false);
		this.moved = new SimpleBooleanProperty(false);
		this.path = new SimpleStringProperty(path);
		this.revision = new SimpleStringProperty(revision);
		this.glpi = new SimpleStringProperty(glpi);
		this.author = new SimpleStringProperty(author);
		this.newItem = new SimpleBooleanProperty(false);
	}

	public Item(String glpi, String titulo, String asignado, String tipo, String vencimiento, String responsable,
			String estado, String entorno, String validado) {
		this.glpi = new SimpleStringProperty(glpi);
		this.titulo = new SimpleStringProperty(titulo);
		this.tipo = new SimpleStringProperty(tipo);
		this.vencimiento = new SimpleStringProperty(vencimiento);
		this.responsable = new SimpleStringProperty(responsable);
		this.asignado = new SimpleStringProperty(asignado);
		this.estado = new SimpleStringProperty(estado);
		this.entorno = new SimpleStringProperty(entorno);
		this.validado = new SimpleStringProperty(validado);
		this.selected = new SimpleBooleanProperty(false);
		this.newItem = new SimpleBooleanProperty(false);
	}

	// Item para BBDD
	public Item(String glpi, String path, String revision, String autor, String desarrollo, String pruebas) {
		this.glpi = new SimpleStringProperty(glpi);
		this.path = new SimpleStringProperty(path);
		this.revision = new SimpleStringProperty(revision);
		this.author = new SimpleStringProperty(autor);
		this.desarrollo = new SimpleStringProperty(desarrollo);
		this.pruebas = new SimpleStringProperty(pruebas);
		this.newItem = new SimpleBooleanProperty(false);
	}

	// Item para otros
	public Item(String glpi, String path, String revision, String autor, String desarrollo, String pruebas,
			String rutaSVN) {
		this.glpi = new SimpleStringProperty(glpi);
		this.path = new SimpleStringProperty(path);
		this.revision = new SimpleStringProperty(revision);
		this.author = new SimpleStringProperty(autor);
		this.desarrollo = new SimpleStringProperty(desarrollo);
		this.pruebas = new SimpleStringProperty(pruebas);
		this.rutaSVN = new SimpleStringProperty(rutaSVN);
		this.newItem = new SimpleBooleanProperty(false);
	}

	public Item(Integer total) {
		this.glpi = new SimpleStringProperty("Total Ficheros");
		this.totalFicheros = new SimpleIntegerProperty(total);
		this.path = new SimpleStringProperty("");
		this.selected = new SimpleBooleanProperty(false);
		this.newItem = new SimpleBooleanProperty(false);
	}

	public SimpleBooleanProperty getSelected() {
		return selected;
	}

	public void setSelected(SimpleBooleanProperty selected) {
		this.selected = selected;
	}

	public SimpleBooleanProperty getExported() {
		return exported;
	}

	public void setExported(SimpleBooleanProperty exported) {
		this.exported = exported;
	}

	public SimpleBooleanProperty getMoved() {
		return moved;
	}

	public void setMoved(SimpleBooleanProperty moved) {
		this.moved = moved;
	}

	public SimpleStringProperty getPath() {
		return path;
	}

	public void setPath(SimpleStringProperty path) {
		this.path = path;
	}

	public SimpleStringProperty getRevision() {
		return revision;
	}

	public void setRevision(SimpleStringProperty revision) {
		this.revision = revision;
	}

	public SimpleBooleanProperty getNewItem() {
		return newItem;
	}

	public void setNewItem(SimpleBooleanProperty newItem) {
		this.newItem = newItem;
	}

	public SimpleStringProperty getGlpi() {
		return glpi;
	}

	public void setGlpi(SimpleStringProperty glpi) {
		this.glpi = glpi;
	}

	public SimpleStringProperty getTitulo() {
		return titulo;
	}

	public void setTitulo(SimpleStringProperty titulo) {
		this.titulo = titulo;
	}

	public SimpleStringProperty getAsignado() {
		return asignado;
	}

	public void setAsignado(SimpleStringProperty asignado) {
		this.asignado = asignado;
	}

	public SimpleStringProperty getEstado() {
		return estado;
	}

	public void setEstado(SimpleStringProperty estado) {
		this.estado = estado;
	}

	public SimpleStringProperty getEntorno() {
		return entorno;
	}

	public void setEntorno(SimpleStringProperty entorno) {
		this.entorno = entorno;
	}

	public SimpleStringProperty getValidado() {
		return validado;
	}

	public void setValidado(SimpleStringProperty validado) {
		this.validado = validado;
	}

	public SimpleStringProperty getTipo() {
		return tipo;
	}

	public void setTipo(SimpleStringProperty tipo) {
		this.tipo = tipo;
	}

	public SimpleStringProperty getVencimiento() {
		return vencimiento;
	}

	public void setVencimiento(SimpleStringProperty vencimiento) {
		this.vencimiento = vencimiento;
	}

	public SimpleStringProperty getResponsable() {
		return responsable;
	}

	public void setResponsable(SimpleStringProperty responsable) {
		this.responsable = responsable;
	}

	public SimpleStringProperty getAuthor() {
		return author;
	}

	public void setAuthor(SimpleStringProperty author) {
		this.author = author;
	}

	public SimpleStringProperty getRevisionDependiente() {
		return revisionDependiente;
	}

	public void setRevisionDependiente(SimpleStringProperty revisionDependiente) {
		this.revisionDependiente = revisionDependiente;
	}

	public SimpleStringProperty getGlpiDependiente() {
		return glpiDependiente;
	}

	public void setGlpiDependiente(SimpleStringProperty glpiDependiente) {
		this.glpiDependiente = glpiDependiente;
	}

	public SimpleIntegerProperty getTotalFicheros() {
		return totalFicheros;
	}

	public void setTotalFicheros(SimpleIntegerProperty totalFicheros) {
		this.totalFicheros = totalFicheros;
	}

	public SimpleStringProperty getAnotado() {
		return anotado;
	}

	public void setAnotado(SimpleStringProperty anotado) {
		this.anotado = anotado;
	}

	public SimpleStringProperty getDesarrollo() {
		return desarrollo;
	}

	public void setDesarrollo(SimpleStringProperty desarrollo) {
		this.desarrollo = desarrollo;
	}

	public SimpleStringProperty getPruebas() {
		return pruebas;
	}

	public void setPruebas(SimpleStringProperty pruebas) {
		this.pruebas = pruebas;
	}

	public SimpleStringProperty getRutaSVN() {
		return rutaSVN;
	}

	public void setRutaSVN(SimpleStringProperty rutaSVN) {
		this.rutaSVN = rutaSVN;
	}

}
