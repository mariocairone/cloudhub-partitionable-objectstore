package com.mariocairone.mule.objectstore;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.RequestContext;
import org.mule.api.MuleEvent;
import org.mule.api.store.ObjectStoreException;
import org.mule.api.store.PartitionableExpirableObjectStore;
import org.mule.util.store.AbstractMonitoredObjectStore;

@SuppressWarnings({ "unchecked", "deprecation" })
public class PartitionableMonitorObjectStore<T extends Serializable> extends AbstractMonitoredObjectStore<T> {

	private static final Logger logger = Logger.getLogger(PartitionableMonitorObjectStore.class);

	public PartitionableExpirableObjectStore<T> localObjectStore = null;

	private String partition = null;

	public boolean contains(Serializable key) throws ObjectStoreException {
		logger.debug("contains - key=" + key);
		return this.localObjectStore.contains(key, partition);
	}

	public void store(Serializable key, T value) throws ObjectStoreException {
		CacheTSEntry thisCacheEntry = new CacheTSEntry((MuleEvent) value, System.currentTimeMillis());

		logger.info(
				"store - key=" + key + "; value=" + value + ", MuleContext=" + ((MuleEvent) value).getMuleContext());

		this.localObjectStore.store(key, (T) thisCacheEntry.toString(), partition);
	}

	public T retrieve(Serializable key) throws ObjectStoreException {
		logger.info("retrieve - key=" + key);

		String retrievedObject = (String) this.localObjectStore.retrieve(key, partition);

		logger.info("Raw value retrieved from cache: " + retrievedObject);

		CacheTSEntry thisCachedEntry = new CacheTSEntry(retrievedObject);

		logger.info("Constructed cache entry =" + key + ",value=" + thisCachedEntry.getThisMuleEvent() + ", TimeStamp="
				+ thisCachedEntry.getCreationTimeStamp());

		return (T) getEventFromPayload(thisCachedEntry.getThisMuleEvent(), RequestContext.getEvent());

	}

	public MuleEvent getEventFromPayload(String payload, MuleEvent originalEvent)

	{
		MuleEvent thisMuleEvent = null;

		logger.info("getEventFromPayload (" + payload + "), originalEvent=" + originalEvent);

		try {

			DefaultMuleMessage messageWithContext = new DefaultMuleMessage(payload, context);

			MuleEvent muleEvent = new DefaultMuleEvent(messageWithContext, originalEvent);

			muleEvent.setMessage(messageWithContext);

			thisMuleEvent = muleEvent;

		} catch (Exception e) {
			logger.error("Thrown error:" + e.getMessage());
			e.printStackTrace();
		}

		logger.info("Created event=" + thisMuleEvent);

		return thisMuleEvent;

	}

	public T remove(Serializable key) throws ObjectStoreException {
		logger.debug("remove - key=" + key);
		Serializable value = null;
		value = this.localObjectStore.remove(key, partition);

		return (T) this.getEventFromPayload((String) value, RequestContext.getEvent());
	}

	public boolean isPersistent() {
		logger.debug("isPersistent : " + this.localObjectStore.isPersistent());
		return this.localObjectStore.isPersistent();
	}

	public void clear() throws ObjectStoreException {
		logger.debug("Clearing objectstore - " + this.localObjectStore);
		this.localObjectStore.clear(partition);
	}

	protected void expire() {

		logger.info("expire - entryTTL=" + this.entryTTL + ", expirationInterval=" + this.expirationInterval);
		int expiredEntries = 0;
		try {
			int currentSize = 0;
			List<Serializable> keys = this.localObjectStore.allKeys();
			if (keys != null) {
				currentSize = keys.size();
			}

			if ((this.entryTTL > 0) && (currentSize != 0)) {
				for (Serializable key : keys) {
					CacheTSEntry thisCacheEntry = new CacheTSEntry(
							(String) this.localObjectStore.retrieve(key, partition));

					String value = thisCacheEntry.getThisMuleEvent();
					long TS = thisCacheEntry.getCreationTimeStamp();

					logger.info("Retrieved entry - key=" + key + "; value=" + value + ", TimeStamp Written=" + TS);

					long elapsedTime = System.currentTimeMillis() - TS;

					if (elapsedTime > this.entryTTL) {
						logger.info("Removing expired entry from objectStore.  Key=" + key + ", exceeded time="
								+ elapsedTime);
						remove(key);
						expiredEntries++;
					}

				}
			}

			if (logger.isDebugEnabled())
				logger.debug("Expired " + expiredEntries + " old entries");
		} catch (Exception e) {
			logger.error("Error occured during CustomlocalObjectStore.expire()" + e.getCause());
			e.printStackTrace();
		}
	}

	public PartitionableExpirableObjectStore<T> getLocalObjectStore() {
		return localObjectStore;
	}

	public void setLocalObjectStore(PartitionableExpirableObjectStore<T> localObjectStore) throws ObjectStoreException {
		this.localObjectStore = localObjectStore;
		open(localObjectStore);

	}

	private void open(PartitionableExpirableObjectStore<T> localObjectStore) throws ObjectStoreException {

		if (partition != null && localObjectStore != null) {
			logger.info("Opening localObjectStore => " + localObjectStore + String.format(" [%s]", partition));
			localObjectStore.open(partition);
		}
	}

	public String getPartition() {
		return partition;
	}

	public void setPartition(String partition) throws ObjectStoreException {
		this.partition = partition;
		open(localObjectStore);
	}

}