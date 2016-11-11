package es.carm.figesper.caronte;

import javafx.scene.control.Dialog;
import javafx.scene.control.ProgressBar;
import javafx.stage.StageStyle;

public class ProgressDialog extends Dialog<Void> {

	private ProgressBar progressBar;

	public ProgressDialog(String title, String message) {

		setTitle(title);
		setHeaderText(message);
		initStyle(StageStyle.UTILITY);

		progressBar = new ProgressBar(0);
		getDialogPane().setContent(progressBar);
	}

	public void setValueProgress(double value) {
		this.progressBar.setProgress(value);
	}
}
