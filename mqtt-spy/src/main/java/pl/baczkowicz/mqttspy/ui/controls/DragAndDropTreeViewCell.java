/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */

package pl.baczkowicz.mqttspy.ui.controls;

import javafx.event.EventHandler;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.configuration.ConfigurationManager;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionDetails;
import pl.baczkowicz.mqttspy.configuration.ConfiguredConnectionGroupDetails;
import pl.baczkowicz.mqttspy.configuration.generated.ConnectionGroupReference;
import pl.baczkowicz.mqttspy.configuration.generated.ConnectionReference;
import pl.baczkowicz.mqttspy.ui.EditConnectionsController;
import pl.baczkowicz.mqttspy.ui.properties.ConnectionTreeItemProperties;

public class DragAndDropTreeViewCell extends TreeCell<ConnectionTreeItemProperties>
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(DragAndDropTreeViewCell.class);

	private ConnectionTreeItemProperties item;
	
	public DragAndDropTreeViewCell(final TreeView<ConnectionTreeItemProperties> treeView, final EditConnectionsController controller)
	{	
		DragAndDropTreeViewCell thisCell = this;
		
		setOnDragDetected(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(final MouseEvent event)
			{
				if (item == null)
				{
					return;
				}

				logger.debug("Drag detected on item = " + item);
				final Dragboard dragBoard = startDragAndDrop(TransferMode.MOVE);
				final ClipboardContent content = new ClipboardContent();
				content.put(DataFormat.PLAIN_TEXT, item.getId());
				dragBoard.setContent(content);
				event.consume();
			}
		});
		
		setOnDragEntered(event -> 
		{
			if (event.getGestureSource() != thisCell
					&& event.getDragboard().hasString())
			{
				setOpacity(0.3);
			}
		});

		setOnDragExited(event -> 
		{
			if (event.getGestureSource() != thisCell
					&& event.getDragboard().hasString())
			{
				setOpacity(1);
			}
		});
		
		setOnDragDone(new EventHandler<DragEvent>()
		{
			@Override
			public void handle(final DragEvent dragEvent)
			{
				logger.debug("Drag done on item = " + item);
				dragEvent.consume();
			}
		});

		setOnDragOver(new EventHandler<DragEvent>()
		{
			@Override
			public void handle(final DragEvent dragEvent)
			{
				logger.debug("Drag over on item = " + item);
				if (dragEvent.getDragboard().hasString())
				{
					final String idToMove = dragEvent.getDragboard().getString();
					if (!idToMove.equals(item.getId()))
					{
						dragEvent.acceptTransferModes(TransferMode.MOVE);
					}
				}
				dragEvent.consume();
			}
		});

		setOnDragDropped(new EventHandler<DragEvent>()
		{
			@Override
			public void handle(DragEvent dragEvent)
			{
				logger.debug("Drag dropped on item = " + item);
				
				if (item == null)
				{
					return;
				}
				
				final String idToMove = dragEvent.getDragboard().getString();
				
				// Only move if the new parent is not the current item and the new item is a group
				if (!idToMove.equals(item.getId()))
				{					
					final TreeItem<ConnectionTreeItemProperties> treeItemToMove = findNode(treeView.getRoot(), idToMove);
					final ConnectionTreeItemProperties treeItemPropertiesToMove = treeItemToMove.getValue();
					
					TreeItem<ConnectionTreeItemProperties> newParentTreeItem = findNode(treeView.getRoot(), item.getId());
					TreeItem<ConnectionTreeItemProperties> requestedNewParentTreeItem = newParentTreeItem; 
					
					if (checkIfParentIsSubitem(newParentTreeItem, treeItemToMove))						
					{
						return;
					}
					
					// Remove from the old parent
					treeItemToMove.getParent().getChildren().remove(treeItemToMove);
					
					// Re-map helper refs
					treeItemPropertiesToMove.getParent().getChildren().remove(treeItemPropertiesToMove);
					int insertIndex = newParentTreeItem.getChildren().size();
					
					// Regroup
					if (item.isGroup())
					{
						
					}
					else
					{
						// Reorder
						newParentTreeItem = newParentTreeItem.getParent();
						insertIndex = newParentTreeItem.getChildren().indexOf(requestedNewParentTreeItem);
					}
					
					final ConnectionTreeItemProperties newParentTreeItemProperties = newParentTreeItem.getValue();
					final ConnectionTreeItemProperties oldParentTreeItemProperties = treeItemPropertiesToMove.getParent();
					final ConfiguredConnectionGroupDetails newParentGroup = newParentTreeItemProperties.getGroup();
					final ConfiguredConnectionGroupDetails oldParentGroup = oldParentTreeItemProperties.getGroup();
					
					// Add to the new parent
					newParentTreeItem.getChildren().add(insertIndex, treeItemToMove);
					
					// Re-map helper refs
					newParentTreeItemProperties.getChildren().add(insertIndex, treeItemPropertiesToMove);
					treeItemPropertiesToMove.setParent(newParentTreeItemProperties);					
					
					// Re-map connections and groups
					if (treeItemPropertiesToMove.isGroup())
					{
						final ConfiguredConnectionGroupDetails groupToMove = treeItemPropertiesToMove.getGroup();
						
						// Remove old child
						ConfiguredConnectionGroupDetails.removeFromGroup(groupToMove, oldParentGroup);			
											
						// Set new parent
						groupToMove.setGroup(new ConnectionGroupReference(newParentTreeItemProperties.getGroup()));
						
						// Add new child
						newParentGroup.getSubgroups().add(new ConnectionGroupReference(groupToMove));
						
						// checkGroupForParentChanges(groupToMove);
						groupToMove.setGroupingModified(true);
						oldParentGroup.setGroupingModified(true);
						newParentGroup.setGroupingModified(true);
					}
					else
					{
						final ConfiguredConnectionDetails connectionToMove = treeItemPropertiesToMove.getConnection();
						
						// Remove old child
						ConfiguredConnectionDetails.removeFromGroup(connectionToMove, oldParentGroup);
						
						// Set new parent
						connectionToMove.setGroup(new ConnectionGroupReference(newParentTreeItemProperties.getGroup()));
						
						// Add new child
						newParentGroup.getConnections().add(insertIndex, new ConnectionReference(connectionToMove));						

						connectionToMove.setGroupingModified(true);
						oldParentGroup.setGroupingModified(true);
						newParentGroup.setGroupingModified(true);
						//checkConnectionForParentChanges(connectionToMove);
					}

					newParentTreeItem.setExpanded(true);
					
					// Select the current item
					getTreeView().getSelectionModel().select(treeItemToMove);
				}
				dragEvent.consume();				
				controller.listConnections();
			}
		});
	}
	
	private boolean checkIfParentIsSubitem(
			TreeItem<ConnectionTreeItemProperties> newParent,
			TreeItem<ConnectionTreeItemProperties> treeItemToMove)
	{
		newParent = newParent.getParent();
		while (newParent != null)
		{
			if (newParent.equals(treeItemToMove))
			{
				logger.warn("Cannot move the object it its child!");
				return true;
			}
			
			newParent = newParent.getParent();
		}
		
		return false;
	}	

	private TreeItem<ConnectionTreeItemProperties> findNode(
			final TreeItem<ConnectionTreeItemProperties> currentNode,
			final String idToSearch)
	{
		TreeItem<ConnectionTreeItemProperties> result = null;
		if (currentNode.getValue().getId().equals(idToSearch))
		{
			result = currentNode;
		}
		else if (!currentNode.isLeaf())
		{
			for (TreeItem<ConnectionTreeItemProperties> child : currentNode.getChildren())
			{
				result = findNode(child, idToSearch);
				if (result != null)
				{
					break;
				}
			}
		}
		return result;
	}

	@Override
	protected void updateItem(final ConnectionTreeItemProperties item, final boolean empty)
	{
		super.updateItem(item, empty);
		this.item = item;
		
		if (item == null)
		{
			setText(null);
			setGraphic(null);
		}
		else
		{
			ImageView image;
			
			if (!item.isGroup())
			{
				image = new ImageView(new Image(EditConnectionsController.class.getResource("/images/mqtt-icon.png").toString()));
			}		
			else
			{
				if (item.getGroup().getID().equals(ConfigurationManager.DEFAULT_GROUP))
				{
					setDisclosureNode(null);
				}
				
				if (item.getChildren().isEmpty())
				{
					image = new ImageView(new Image(EditConnectionsController.class.getResource("/images/folder-grey.png").toString()));
				}
				else
				{
					image = new ImageView(new Image(EditConnectionsController.class.getResource("/images/folder-yellow.png").toString()));
				}
			}
			
			image.setFitHeight(18);
			image.setFitWidth(18);
			
			setText(item.getName());
			setGraphic(image);
		}
	}
}
