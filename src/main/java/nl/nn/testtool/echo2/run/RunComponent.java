/*
   Copyright 2018-2019 Nationale-Nederlanden

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package nl.nn.testtool.echo2.run;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TooManyListenersException;

import echopointng.ProgressBar;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.FillImageBorder;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.filetransfer.UploadSelect;
import nl.nn.testtool.MetadataExtractor;
import nl.nn.testtool.Report;
import nl.nn.testtool.TestTool;
import nl.nn.testtool.echo2.BaseComponent;
import nl.nn.testtool.echo2.BeanParent;
import nl.nn.testtool.echo2.Echo2Application;
import nl.nn.testtool.echo2.RunPane;
import nl.nn.testtool.echo2.reports.MessageComponent;
import nl.nn.testtool.echo2.reports.ReportUploadListener;
import nl.nn.testtool.echo2.reports.ReportsComponent;
import nl.nn.testtool.echo2.util.Download;
import nl.nn.testtool.echo2.util.PopupWindow;
import nl.nn.testtool.run.ReportRunner;
import nl.nn.testtool.run.RunResult;
import nl.nn.testtool.storage.CrudStorage;
import nl.nn.testtool.storage.Storage;
import nl.nn.testtool.storage.StorageException;
import nl.nn.testtool.transform.ReportXmlTransformer;

/**
 * @author m00f069
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class RunComponent extends BaseComponent implements BeanParent, ActionListener {
	private static final long serialVersionUID = 1L;
	private TestTool testTool;
	private Storage debugStorage; // TODO juiste naam? overal consequent doen?
	private CrudStorage runStorage; // TODO juiste naam? overal consequent doen?
	private Echo2Application echo2Application;
	private TreePane treePane;
	private ProgressBar progressBar;
	private ReportRunner reportRunner;
	private TextField pathTextField;
	private ReportXmlTransformer reportXmlTransformer = null;
	private WindowPane uploadWindow;
	private UploadSelect uploadSelect;
	private int numberOfComponentsToSkipForRowManipulation = 0;
	private String lastDisplayedPath;
	private BeanParent beanParent;

	public RunComponent() {
		super();
	}

	public void setTestTool(TestTool testTool) {
		this.testTool = testTool;
	}

	public void setDebugStorage(Storage debugStorage) {
		this.debugStorage = debugStorage;
	}

	public void setRunStorage(CrudStorage runStorage) {
		this.runStorage = runStorage;
	}

	public void setReportXmlTransformer(ReportXmlTransformer reportXmlTransformer) {
		this.reportXmlTransformer = reportXmlTransformer;
	}

	/**
	 * @see nl.nn.testtool.echo2.Echo2Application#initBean()
	 */
	public void initBean() {
		super.initBean();

		// Construct

		// TODO code voor aanmaken upload window en ander zaken gaan delen met ReportsComponent
		Column uploadColumn = new Column();

		uploadWindow = new WindowPane();
		uploadWindow.setVisible(false);
		uploadWindow.setTitle("Upload");
		uploadWindow.setTitleBackground(Echo2Application.getButtonBackgroundColor());
		uploadWindow.setBorder(new FillImageBorder(Echo2Application.getButtonBackgroundColor(), new Insets(0, 0, 0, 0), new Insets(0, 0, 0, 0)));
		uploadWindow.setWidth(new Extent(350));
		uploadWindow.setHeight(new Extent(110));
		uploadWindow.setInsets(new Insets(10, 0, 10, 0));
		uploadWindow.add(uploadColumn);
		uploadWindow.setDefaultCloseOperation(WindowPane.HIDE_ON_CLOSE);
		uploadWindow.init();

		Row buttonRow = Echo2Application.getNewRow();

		Button refreshButton = new Button("Refresh");
		refreshButton.setActionCommand("Refresh");
		refreshButton.addActionListener(this);
		Echo2Application.decorateButton(refreshButton);
		buttonRow.add(refreshButton);

		Button runSelectedButton = new Button("Run");
		runSelectedButton.setActionCommand("RunSelected");
		runSelectedButton.addActionListener(this);
		Echo2Application.decorateButton(runSelectedButton);
		buttonRow.add(runSelectedButton);

		Button resetSelectedButton = new Button("Reset");
		resetSelectedButton.setActionCommand("Reset");
		resetSelectedButton.addActionListener(this);
		Echo2Application.decorateButton(resetSelectedButton);
		buttonRow.add(resetSelectedButton);

		Button selectAllButton = new Button("Select all");
		selectAllButton.setActionCommand("SelectAll");
		selectAllButton.addActionListener(this);
		Echo2Application.decorateButton(selectAllButton);
		buttonRow.add(selectAllButton);

		Button deselectAllButton = new Button("Deselect all");
		deselectAllButton.setActionCommand("DeselectAll");
		deselectAllButton.addActionListener(this);
		Echo2Application.decorateButton(deselectAllButton);
		buttonRow.add(deselectAllButton);

		Button moveSelectedButton = new Button("Move");
		moveSelectedButton.setActionCommand("MoveSelected");
		moveSelectedButton.addActionListener(this);
		Echo2Application.decorateButton(moveSelectedButton);
		buttonRow.add(moveSelectedButton);

		Button copySelectedButton = new Button("Copy");
		copySelectedButton.setActionCommand("CopySelected");
		copySelectedButton.addActionListener(this);
		Echo2Application.decorateButton(copySelectedButton);
		buttonRow.add(copySelectedButton);

		Button deleteSelectedButton = new Button("Delete");
		deleteSelectedButton.setActionCommand("DeleteSelected");
		deleteSelectedButton.addActionListener(this);
		Echo2Application.decorateButton(deleteSelectedButton);
		buttonRow.add(deleteSelectedButton);

		Button downloadAllButton = new Button("Download all");
		downloadAllButton.setActionCommand("DownloadAll");
		downloadAllButton.addActionListener(this);
		Echo2Application.decorateButton(downloadAllButton);
		buttonRow.add(downloadAllButton);

		Button prepareUploadButton = new Button("Upload...");
		prepareUploadButton.setActionCommand("OpenUploadWindow");
		Echo2Application.decorateButton(prepareUploadButton);
		prepareUploadButton.addActionListener(this);
		buttonRow.add(prepareUploadButton);

		progressBar = new ProgressBar();
		buttonRow.add(progressBar);
		reportRunner = new ReportRunner();
		reportRunner.setTestTool(testTool);

		Row uploadSelectRow = new Row();

		ReportUploadListener reportUploadListener = new ReportUploadListener();
		reportUploadListener.setRunComponent(this);
		reportUploadListener.setStorage(runStorage);

		uploadSelect = new UploadSelect();
		uploadSelect.setEnabledSendButtonText("Upload");
		uploadSelect.setDisabledSendButtonText("Upload");
		try {
			uploadSelect.addUploadListener(reportUploadListener);
		} catch (TooManyListenersException e) {
			displayAndLogError(e);
		}

		Row pathRow = Echo2Application.getNewRow();
		pathRow.setInsets(new Insets(0, 5, 0, 5));

		pathRow.add(new Label("Move/Copy to:"));

		pathTextField = new TextField();
		pathTextField.setWidth(new Extent(400));
		pathRow.add(pathTextField);

		// Wire

		uploadSelectRow.add(new Label("Upload"));
		uploadSelectRow.add(uploadSelect);
		uploadColumn.add(uploadSelectRow);

		add(buttonRow);
		numberOfComponentsToSkipForRowManipulation++;

		add(pathRow);
		numberOfComponentsToSkipForRowManipulation++;

		add(errorLabel);
		numberOfComponentsToSkipForRowManipulation++;

		add(okayLabel);
		numberOfComponentsToSkipForRowManipulation++;
	}

	/**
	 * @see nl.nn.testtool.echo2.Echo2Application#initBean()
	 */
	public void initBean(BeanParent beanParent) {
		this.beanParent = beanParent;
		this.echo2Application = Echo2Application.getEcho2Application(beanParent, this);
		echo2Application.getContentPane().add(uploadWindow);
		RunPane runPane = (RunPane)beanParent.getBeanParent();
		treePane = runPane.getTreePane();
		reportRunner.setSecurityContext(echo2Application);
	}

	public BeanParent getBeanParent() {
		return beanParent;
	}

	public void display(String path, Set<String> selectedStorageIds) {
		while (getComponentCount() > numberOfComponentsToSkipForRowManipulation) {
			remove(numberOfComponentsToSkipForRowManipulation);
		}
		List<List<Object>> metadata = new ArrayList<List<Object>>();
		List<String> metadataNames = new ArrayList<String>();
		metadataNames.add("storageId");
		metadataNames.add("path");
		metadataNames.add("name");
		metadataNames.add("description");
		List<String> searchValues = new ArrayList<String>();
		searchValues.add(null);
		searchValues.add("[" + path + "*]");
		searchValues.add(null);
		searchValues.add(null);
		try {
			metadata = runStorage.getMetadata(-1, metadataNames, searchValues, MetadataExtractor.VALUE_TYPE_STRING);
		} catch (StorageException e) {
			displayAndLogError(e);
		}
		if (path.equals("/")) {
			Iterator<Integer> iterator = treePane.getReportsWithDirtyPaths().iterator();
			while (iterator.hasNext()) {
				Integer storageId = (Integer)iterator.next();
				try {
					for (int i = 0; i < metadata.size(); i++) {
						List<Object> metadataRecord = metadata.get(i);
						if (metadataRecord.get(0).equals(storageId.toString())) {
							metadata.remove(i);
							i--;
						}
					}
					List<Object> metadataRecord = new ArrayList<Object>();
					metadataRecord.add(storageId.toString());
					metadataRecord.add("/");
					Report report = runStorage.getReport(storageId);
					metadataRecord.add(report.getName());
					metadataRecord.add(report.getDescription());
					metadata.add(metadataRecord);
				} catch (NumberFormatException e) {
					displayAndLogError(e);
				} catch (StorageException e) {
					displayAndLogError(e);
				}
			}
		}
		boolean directChildReportsPresent = false;
		Collections.sort(metadata, new MetadataComparator());
		Iterator<List<Object>> metadataIterator;
		// First direct child's (path.equals(metadataPath))
		metadataIterator = metadata.iterator();
		while (metadataIterator.hasNext()) {
			List<Object> metadataRecord = (List<Object>)metadataIterator.next();
			String metadataPath = (String)metadataRecord.get(1);
			if (path.equals(metadataPath)) {
				boolean selected;
				if (selectedStorageIds != null) {
					selected = selectedStorageIds.contains(metadataRecord.get(0));
				} else {
					selected = true;
				}
				displayReport(metadataRecord, selected);
				directChildReportsPresent = true;
			}
		}
		// Then remaining child's (!path.equals(metadataPath))
		metadataIterator = metadata.iterator();
		while (metadataIterator.hasNext()) {
			List<Object> metadataRecord = (List<Object>)metadataIterator.next();
			String metadataPath = (String)metadataRecord.get(1);
			if (!path.equals(metadataPath)) {
				boolean selected;
				if (selectedStorageIds != null) {
					selected = selectedStorageIds.contains(metadataRecord.get(0));
				} else {
					selected = !directChildReportsPresent;
				}
				displayReport(metadataRecord, selected);
			}
		}
		pathTextField.setText(path);
		lastDisplayedPath = path;
		// Update progress bar also when node in tree is selected
		updateProgressBar();
	}

	private void displayReport(List<Object> metadataRecord, boolean selected) {
		String storageId = (String)metadataRecord.get(0);
		String path = (String)metadataRecord.get(1);
		String name = (String)metadataRecord.get(2);
		String description = (String)metadataRecord.get(3);
		displayReport(storageId, path, name, description, selected);
	}

	private void displayReport(String storageId, String path, String name, String description, boolean selected) {
		Row row = Echo2Application.getNewRow();
		row.setId(storageId);
		row.setInsets(new Insets(0, 5, 0, 0));

		CheckBox checkBox = new CheckBox("");
		checkBox.setSelected(selected);
		row.add(checkBox);

		Button button = new Button("Run");
		button.setActionCommand("Run");
		button.addActionListener(this);
		Echo2Application.decorateButton(button);
		row.add(button);

		button = new Button("Open");
		button.setActionCommand("Open");
		button.addActionListener(this);
		Echo2Application.decorateButton(button);
		row.add(button);

		button = new Button("Compare");
		button.setActionCommand("Compare");
		button.addActionListener(this);
		button.setVisible(false);
		Echo2Application.decorateButton(button);
		row.add(button);

		button = new Button("Replace");
		button.setActionCommand("Replace");
		button.addActionListener(this);
		button.setVisible(false);
		Echo2Application.decorateButton(button);
		row.add(button);

		Label label = new Label(path + name);
		RunResult runResult = reportRunner.getResults().get(Integer.parseInt(storageId));
		if (runResult != null) {
			if (runResult.errorMessage != null) {
				label = Echo2Application.createErrorLabel();
				label.setText(runResult.errorMessage);
			} else {
				Report runResultReport = getRunResultReport(runResult.correlationId);
				if (runResultReport == null) {
					label = Echo2Application.createErrorLabel();
					label.setText("Result report not found. Report generator not enabled?");
				} else {
					Report report = null;
					try {
						report = runStorage.getReport(Integer.parseInt(storageId));
					} catch (StorageException e) {
						displayAndLogError(e);
					}
					if (report != null) {
						String stubInfo = "";
						if (!"Never".equals(report.getStubStrategy())) {
							stubInfo = " (" + report.getStubStrategy() + ")";
						}
						label.setText(path + name + " (" + (report.getEndTime() - report.getStartTime()) + " >> "
								+ (runResultReport.getEndTime() - runResultReport.getStartTime()) + " ms)" + stubInfo);
						report.setGlobalReportXmlTransformer(reportXmlTransformer);
						runResultReport.setGlobalReportXmlTransformer(reportXmlTransformer);
						runResultReport.setTransformation(report.getTransformation());
						runResultReport.setReportXmlTransformer(report.getReportXmlTransformer());
						if (report.toXml().equals(runResultReport.toXml())) {
							label.setForeground(Echo2Application.getNoDifferenceFoundTextColor());
						} else {
							label.setForeground(Echo2Application.getDifferenceFoundTextColor());
						}
						Button compareButton = (Button)row.getComponent(3);
						compareButton.setVisible(true);
						Button replaceButton = (Button)row.getComponent(4);
						replaceButton.setVisible(true);
					}
				}
			}
		}
		row.add(label);
		add(row);

		// TODO runStorage.getMetadata geeft blijkbaar "null" terug, fixen
		if (description != null && !"".equals(description) && !"null".equals(description)) {
			Column descriptionColumn = new Column();
			descriptionColumn.setInsets(new Insets(0, 5, 0, 0));
			MessageComponent.updateMessageColumn(description, descriptionColumn);
			add(descriptionColumn);
		}
	}

	/**
	 * @see nextapp.echo2.app.event.ActionListener#actionPerformed(nextapp.echo2.app.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		hideMessages();
		if (e.getActionCommand().equals("Refresh")) {
			refresh();
		} else if (e.getActionCommand().equals("Reset")) {
			displayError(reportRunner.reset());
			refresh();
		} else if (e.getActionCommand().equals("SelectAll") || e.getActionCommand().equals("DeselectAll")) {
			for (Row row : getReportRows()) {
				CheckBox checkbox = (CheckBox)row.getComponent(0);
				if (e.getActionCommand().equals("SelectAll")) {
					checkbox.setSelected(true);
				} else {
					checkbox.setSelected(false);
				}
			}
		} else if (e.getActionCommand().equals("RunSelected")) {
			if (minimalOneSelected()) {
				List<Row> rows = new ArrayList<Row>();
				for (Row row : getReportRows()) {
					CheckBox checkbox = (CheckBox)row.getComponent(0);
					if (checkbox.isSelected()) {
						rows.add(row);
					}
				}
				List<Report> reports = new ArrayList<Report>();
				for (Row row : rows) {
					Report report = getReport(row);
					if (report != null) {
						reports.add(report);
					}
				}
				String errorMessage = reportRunner.run(reports, true, false);
				if (errorMessage == null) {
					displayOkay("Report runner started, use Refresh to see results");
				} else {
					displayError(errorMessage);
				}
			}
		} else if (e.getActionCommand().equals("DownloadAll")) {
			displayAndLogError(Download.download(runStorage));
		} else if (e.getActionCommand().equals("OpenUploadWindow")) {
			uploadWindow.setVisible(true);
		} else if (e.getActionCommand().equals("DeleteSelected")) {
			if (minimalOneSelected()) {
				List<String> actionLabels = new ArrayList<String>();
				List<String> actionCommands = new ArrayList<String>();
				List<ActionListener> actionListeners = new ArrayList<ActionListener>();
				actionLabels.add("Yes, delete all selected reports");
				actionCommands.add("DeleteOk");
				actionListeners.add(this);
				actionLabels.add("No, cancel this action");
				actionCommands.add("DeleteCancel");
				actionListeners.add(this);
				PopupWindow popupWindow = new PopupWindow("",
						"Are you sure you want to delete all selected reports?", 450, 100,
						actionLabels, actionCommands, actionListeners);
				echo2Application.getContentPane().add(popupWindow);
			}
		} else if (e.getActionCommand().equals("DeleteOk")) {
			for (Row row : getReportRows()) {
				CheckBox checkbox = (CheckBox)row.getComponent(0);
				if (checkbox.isSelected()) {
					Report report = getReport(row);
					if (report != null) {
						String errorMessage = Echo2Application.delete(runStorage, report);
						if (errorMessage == null) {
							remove(row);
							treePane.getReportsWithDirtyPaths().remove(report.getStorageId());
						} else {
							displayAndLogError(errorMessage);
						}
					}
				}
			}
			refresh();
		} else if (e.getActionCommand().equals("MoveSelected")) {
			if (minimalOneSelected()) {
				String newPath = normalizePath(pathTextField.getText());
				for (Row row : getReportRows()) {
					CheckBox checkbox = (CheckBox)row.getComponent(0);
					if (checkbox.isSelected()) {
						movePath(row, newPath);
					}
				}
				treePane.redisplayReports(newPath, null);
			}
		} else if (e.getActionCommand().equals("CopySelected") || e.getActionCommand().equals("CopyPathOk")) {
			if (minimalOneSelected()) {
				String newPath = normalizePath(pathTextField.getText());
				if (newPath.equals(lastDisplayedPath) && !e.getActionCommand().equals("CopyPathOk")) {
					List<String> actionLabels = new ArrayList<String>();
					List<String> actionCommands = new ArrayList<String>();
					List<ActionListener> actionListeners = new ArrayList<ActionListener>();
					actionLabels.add("Yes, duplicate reports");
					actionCommands.add("CopyPathOk");
					actionListeners.add(this);
					actionLabels.add("No, cancel this action");
					actionCommands.add("CopyPathCancel");
					actionListeners.add(this);
					PopupWindow popupWindow = new PopupWindow("",
							"Are you sure you want to copy to the same folder?", 375, 100,
							actionLabels, actionCommands, actionListeners);
					echo2Application.getContentPane().add(popupWindow);
				} else {
					copyPath(newPath);
					treePane.redisplayReports(newPath, null);
				}
			}
		} else if (e.getActionCommand().equals("Run")) {
			Button button = (Button)e.getSource();
			Row row = (Row)button.getParent();
			Report report = getReport(row);
			if (report != null) {
				List<Report> reports = new ArrayList<Report>();
				reports.add(report);
				displayError(reportRunner.run(reports, false, true));
				refresh();
			}
		} else if (e.getActionCommand().equals("Open")
				|| e.getActionCommand().equals("Compare")) {
			Button button = (Button)e.getSource();
			Row row = (Row)button.getParent();
			Report report = getReport(row);
			report.setGlobalReportXmlTransformer(reportXmlTransformer);
			Integer storageId = new Integer(row.getId());
			RunResult runResult = reportRunner.getResults().get(storageId);
			if (e.getActionCommand().equals("Open")) {
				echo2Application.openReport(report, ReportsComponent.OPEN_REPORT_ALLOWED);
			} else {
				Report runResultReport = getRunResultReport(runResult.correlationId);
				if (runResultReport != null) {
					runResultReport.setGlobalReportXmlTransformer(reportXmlTransformer);
					runResultReport.setTransformation(report.getTransformation());
					runResultReport.setReportXmlTransformer(report.getReportXmlTransformer());
					echo2Application.openReportCompare(report, runResultReport);
				}
			}
		} else if (e.getActionCommand().equals("Delete")
				|| e.getActionCommand().equals("Replace")) {
			Button button = (Button)e.getSource();
			Row row = (Row)button.getParent();
 			Report report = getReport(row);
			if (report != null) {
				String errorMessage = null;
				Report runResultReport = null;
				if (e.getActionCommand().equals("Replace")) {
					Integer storageId = new Integer(row.getId());
					runResultReport = getRunResultReport(reportRunner.getResults().get(storageId).correlationId);
					runResultReport.setName(report.getName());
					runResultReport.setDescription(report.getDescription());
					runResultReport.setPath(report.getPath());
					runResultReport.setTransformation(report.getTransformation());
					runResultReport.setReportXmlTransformer(report.getReportXmlTransformer());
					errorMessage = Echo2Application.store(runStorage, runResultReport);
					reportRunner.getResults().remove(storageId);
					row.setId(runResultReport.getStorageId().toString());
					row.getComponent(4).setVisible(false);
					row.getComponent(5).setVisible(false);
					row.remove(6);
					String path = runResultReport.getPath();
					String name = runResultReport.getName();
					if (path == null || !path.equals(normalizePath(path))) {
						path = "/";
					}
					row.add(new Label(path + name));
				}
				if (errorMessage == null) {
					errorMessage = Echo2Application.delete(runStorage, report);
					if (errorMessage == null) {
						if (treePane.getReportsWithDirtyPaths().remove(report.getStorageId())
								&& e.getActionCommand().equals("Replace")) {
							treePane.getReportsWithDirtyPaths().add(runResultReport.getStorageId());
						}
						if (e.getActionCommand().equals("Delete")) {
							remove(row);
						}
					}
				}
				displayAndLogError(errorMessage);
			}
		}
		updateProgressBar();
	}

	private void updateProgressBar() {
		progressBar.setMaximum(reportRunner.getMaximum());
		progressBar.setValue(reportRunner.getProgressValue());
		progressBar.setToolTipText(reportRunner.getProgressValue() + " / " + reportRunner.getMaximum());
	}

	public void refresh() {
		treePane.redisplayReports(lastDisplayedPath, getSelectedStorageIds());
	}

	private List<Row> getReportRows() {
		List<Row> result = new ArrayList<Row>();
		for (int i = numberOfComponentsToSkipForRowManipulation; i < getComponentCount(); i++) {
			Component component = getComponent(i);
			// Ignore TextArea's for Reports with a description
			if (component instanceof Row) {
				result.add((Row)component);
			}
		}
		return result;
	}

	private Set<String> getSelectedStorageIds() {
		Set<String> selectedStorageIds = new HashSet<String>();
		for (Row row : getReportRows()) {
			CheckBox checkbox = (CheckBox)row.getComponent(0);
			if (checkbox.isSelected()) {
				selectedStorageIds.add(row.getId());
			}
		}
		return selectedStorageIds;
	}

	private boolean minimalOneSelected() {
		if (getSelectedStorageIds().size() > 0) {
			return true;
		} else {
			displayError("No reports selected");
			return false;
		}
	}

	private Report getReport(Row row) {
		Integer storageId = new Integer(row.getId());
		return echo2Application.getReport(runStorage, storageId, this);
	}

	private Report getRunResultReport(String runResultCorrelationId) {
		Report report = null;
		try {
			report = ReportRunner.getRunResultReport(debugStorage, runResultCorrelationId);
		} catch(StorageException storageException) {
			displayAndLogError(storageException);
		}
		return report;
	}

	public static String normalizePath(String path) {
		for (int i = 0; i < path.length(); i++) {
			// Be on the safe side for now
			if (!Character.isLetterOrDigit(path.charAt(i)) && "/ -_.()".indexOf(path.charAt(i)) == -1) {
				if (path.length() > i + 1) {
					path = path.substring(0, i) + path.substring(i + 1);
					i--;
				} else {
					path = path.substring(0, i);
				}
			}
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		if (!path.endsWith("/")) {
			path = path + "/";
		}
		while (path.indexOf("//") != -1) {
			path = path.substring(0, path.indexOf("//")) + path.substring(path.indexOf("//") +1);
		}
		return path;
	}

	private void movePath(Row row, String path) {
		Report report = getReport(row);
		if (report != null) {
			report.setPath(path);
			try {
				runStorage.update(report);
			} catch (StorageException e) {
				displayAndLogError(e);
			}
		}
	}

	private void copyPath(String newPath) {
		for (Row row : getReportRows()) {
			CheckBox checkbox = (CheckBox)row.getComponent(0);
			if (checkbox.isSelected()) {
				copyPath(row, newPath);
			}
		}
	}

	private void copyPath(Row row, String newPath) {
		Report report = getReport(row);
		if (report != null) {
			Integer storageId = new Integer(row.getId());
			log.debug("Copy report " + storageId + " from '" + report.getPath() + "' to '" + newPath + "'");
			Report clone;
			try {
				clone = (Report)report.clone();
				clone.setPath(newPath);
				try {
					runStorage.store(clone);
				} catch (StorageException e) {
					displayAndLogError(e);
				}
			} catch (CloneNotSupportedException e) {
				displayAndLogError(e);
			}
		}
	}

	public WindowPane getUploadOptionsWindow() {
		return uploadWindow;
	}

}

class MetadataComparator implements Comparator<List<Object>> {

	public int compare(List<Object> arg0, List<Object> arg1) {
		String string0 = (String)arg0.get(1) + (String)arg0.get(2);
		String string1 = (String)arg1.get(1) + (String)arg1.get(2);
		return string0.compareTo(string1);
	}
	
}
