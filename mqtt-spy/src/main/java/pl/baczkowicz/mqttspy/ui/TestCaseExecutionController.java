/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.mqttspy.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.events.EventManager;
import pl.baczkowicz.mqttspy.testcases.TestCaseStatus;
import pl.baczkowicz.mqttspy.ui.properties.TestCaseStepProperties;

/**
 * Controller for the search window.
 */
public class TestCaseExecutionController extends AnchorPane implements Initializable
{
	final static Logger logger = LoggerFactory.getLogger(TestCaseExecutionController.class);	

	private EventManager eventManager;

	private ConfigurationManager configurationManager;
	
	@FXML
	private TableView<TestCaseStepProperties> stepsView;
	
	@FXML
	private TableColumn<TestCaseStepProperties, String> stepNumberColumn;
	
	@FXML
	private TableColumn<TestCaseStepProperties, String> descriptionColumn;
	
	@FXML
	private TableColumn<TestCaseStepProperties, TestCaseStatus> statusColumn;
	
	@FXML
	private TableColumn<TestCaseStepProperties, String> infoColumn;
	
	public void initialize(URL location, ResourceBundle resources)
	{
		stepNumberColumn.setCellValueFactory(new PropertyValueFactory<TestCaseStepProperties, String>("stepNumber"));
		
		descriptionColumn.setCellValueFactory(new PropertyValueFactory<TestCaseStepProperties, String>("description"));
		
		statusColumn.setCellValueFactory(new PropertyValueFactory<TestCaseStepProperties, TestCaseStatus>("status"));
		statusColumn.setCellFactory(new Callback<TableColumn<TestCaseStepProperties, TestCaseStatus>, TableCell<TestCaseStepProperties, TestCaseStatus>>()
		{
			public TableCell<TestCaseStepProperties, TestCaseStatus> call(
					TableColumn<TestCaseStepProperties, TestCaseStatus> param)
			{
				final TableCell<TestCaseStepProperties, TestCaseStatus> cell = new TableCell<TestCaseStepProperties, TestCaseStatus>()
				{
					@Override
					public void updateItem(TestCaseStatus item, boolean empty)
					{
						super.updateItem(item, empty);
						
						if (!isEmpty())
						{
							setGraphic(getIconForStatus(item));
						}
						else
						{
							setGraphic(null);
						}
					}
				};
				cell.setAlignment(Pos.TOP_CENTER);
				cell.setPadding(new Insets(0, 0, 0, 0));
				
				return cell;
			}
		});
		
		infoColumn.setCellValueFactory(new PropertyValueFactory<TestCaseStepProperties, String>("executionInfo"));
		
		// Note: important - without that, cell height goes nuts with progress indicator
		stepsView.setFixedCellSize(24);
		
		// TODO: for testing only
		stepsView.setTableMenuButtonVisible(true);		
	}	

	public void init()
	{
		//
	}	
	
	public void display(final ObservableList<TestCaseStepProperties> items)
	{
		stepsView.getItems().clear();
		
		stepsView.setItems(items);
	}
	
	public Node getIconForStatus(final TestCaseStatus status)
	{
		String iconName = null;
		
		switch (status)
		{
			case ACTIONED:
				iconName = "testcase_actioned.png";
				break;
			case ERROR:
				iconName = "testcase_error.png";
				break;
			case FAILED:
				iconName = "testcase_fail.png";
				break;
			case IN_PROGRESS:
			{
				final ProgressIndicator progressIndicator = new ProgressIndicator();
				progressIndicator.setMaxSize(18, 18);
				progressIndicator.setPadding(new Insets(0, 0, 0, 0));				
				return progressIndicator;
			}
			case NOT_RUN:
				break;
			case PASSED:
				iconName = "testcase_pass.png";
				break;
			case SKIPPED:
				iconName = "testcase_skipped.png";
				break;
			default:
				break;		
		}
	
		if (iconName != null)
		{
			final ImageView imageView = new ImageView (new Image(getClass().getResourceAsStream("/images/" + iconName)));
			return imageView;
		}
		
		return null;
	}
	
	// ===============================
	// === Setters and getters =======
	// ===============================
	
	public void setEventManager(final EventManager eventManager)
	{
		this.eventManager = eventManager;
	}

	public void setConfingurationManager(final ConfigurationManager configurationManager)
	{
		this.configurationManager = configurationManager;
	}
}