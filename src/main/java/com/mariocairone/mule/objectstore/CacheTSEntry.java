package com.mariocairone.mule.objectstore;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.mule.api.MuleEvent;

public class CacheTSEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(CacheTSEntry.class);

	private String thisMuleEvent;
	private long creationTimeStamp;

	public CacheTSEntry(MuleEvent thisMuleEvent, long creationTimeStamp) {

		super();

		try {

			this.thisMuleEvent = thisMuleEvent.getMessage().getPayloadAsString();
			this.creationTimeStamp = creationTimeStamp;

			logger.info("Created new " + this);

		} catch (Exception e) {
			logger.error("Error creating CustoCacheTSEntry:" + e.getMessage(),e);
		}

	}

	public String getThisMuleEvent() {
		return thisMuleEvent;
	}

	public void setThisMuleEvent(String thisMuleEvent) {
		this.thisMuleEvent = thisMuleEvent;
	}

	public long getCreationTimeStamp() {
		return creationTimeStamp;
	}

	public void setCreationTimeStamp(long creationTimeStamp) {
		this.creationTimeStamp = creationTimeStamp;
	}

	
	public CacheTSEntry(String thisCacheEntry) {

		int thisStartIndex = thisCacheEntry.indexOf('[') + 1;
		int thisEndIndex = thisCacheEntry.indexOf(']');
		int thisStartPayloadIndex = thisCacheEntry.indexOf(':') + 1;

		this.setCreationTimeStamp(Long.valueOf(thisCacheEntry.subSequence(thisStartIndex, thisEndIndex).toString()));

		this.setThisMuleEvent(thisCacheEntry.substring(thisStartPayloadIndex));

	}

	public String toString() {
		try {

			return "[" + String.valueOf(this.getCreationTimeStamp()) + "]:" + this.getThisMuleEvent();

			// return this.getThisMuleEvent().getMessage().getPayloadAsString();

		} catch (Exception e) {
			logger.error(e.getMessage(),e);

			return "";
		}
	}

}
