/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
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
package pl.baczkowicz.mqttspy.storage.summary;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.messages.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.ui.properties.SubscriptionTopicSummaryProperties;
import pl.baczkowicz.spy.common.generated.FormatterDetails;

/**
 * This class is responsible for managing counts of messages for each topic. It
 * is responsible for adding new topic entries and storing the formatting
 * settings.
 */
public class TopicSummary extends TopicMessageCount
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(TopicSummary.class);
	
	protected Map<String, SubscriptionTopicSummaryProperties> topicToSummaryMapping = new HashMap<>();
	
	protected FormatterDetails messageFormat;

	private int maxPayloadLength;

	public TopicSummary(final String name, final int maxPayloadLength)
	{
		super(name);
		this.maxPayloadLength = maxPayloadLength;
	}
	
	public void clear()
	{
		synchronized (topicToSummaryMapping)
		{
			super.clear();
			topicToSummaryMapping.clear();
		}
	}
	
	public void removeMessage(final FormattedMqttMessage message)
	{
		synchronized (topicToSummaryMapping)
		{
			final SubscriptionTopicSummaryProperties item = topicToSummaryMapping.get(message.getTopic());
	
			// There should be something in
			if (item != null)
			{
				item.setCount(item.countProperty().intValue() - 1);
			}
			else
			{
				logger.error("[{}] Found empty value for topic {}", name, message.getTopic());
			}
		}
	}
	
	public SubscriptionTopicSummaryProperties addMessage(final FormattedMqttMessage message, final AtomicBoolean newAdded)
	{
		synchronized (topicToSummaryMapping)
		{
			SubscriptionTopicSummaryProperties item = topicToSummaryMapping.get(message.getTopic());
	
			if (item == null)
			{
				item = new SubscriptionTopicSummaryProperties(false, 1, message, maxPayloadLength);
				topicToSummaryMapping.put(message.getTopic(), item);
				newAdded.set(true);
			}
			else
			{
				item.setCount(item.countProperty().intValue() + 1);	
				item.setMessage(message);				
			}
			
			logger.trace("[{}] has {} messages", name, item.countProperty().intValue());
			
			return item;
		}				
	}

	public void setFormatter(final FormatterDetails messageFormat)
	{
		this.messageFormat = messageFormat;		
	}
}
