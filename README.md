# CloudHub Partitionable ObjectStore

The recommended approach to implement a distributed cache in CloudHub, synchronized for all the workers, is to use the default CloudHub persistent Object Store (Using Object Store V2).
The [Mulesoft Knowledge Base][f338c593] article describes such solution and provide a sample implementation.

The implementation provided in the article, however, do not support partitions, so if we define multiple caches for different purposes all the entries will be stored in the same default partition.

The PartitionableMonitorObjectStore allows to use a different partition for each cache. This will offer a better experience when you need to search or manually expire a particular key.
The image below shows an example of how it will look like in the Runtime Manager console:
![Object Store](images/ObjectStoreView.png)

## Configuration

### Parameters

| Name               | Required | Type                              | Default | Description                                                                                         |
|:-------------------|:---------|:----------------------------------|:--------|:----------------------------------------------------------------------------------------------------|
| partition          | true     | String                            |         | the Partition name                                                                                  |
| localObjectStore   | true     | PartitionableExpirableObjectStore |         | ObjectStore instance                                                                                |
| entryTTL           | false    | Integer                           | -1      | The time-to-live for each entry, specified in milliseconds.                                         |
| maxEntries         | false    | Integer                           | 4000    | The maximum number of entries that this store keeps around.                                         |
| expirationInterval | false    | Integer                           | 1000    | The interval for periodic bounded size enforcement and entry expiration, specified in milliseconds. |


### Example

```xml
<ee:object-store-caching-strategy  name="MyCacheStrategy"  doc:name="Caching Strategy" keyGenerationExpression="#[flowVars.cacheKey]">
	<custom-object-store class="com.mariocairone.mule.objectstore.PartitionableMonitorObjectStore">
        <spring:property name="partition" value="MY_PARTITION"/>                
        <spring:property name="localObjectStore" ref="_defaultUserObjectStore"/>
        <spring:property name="entryTTL" value="${cache.entry.ttl}"/>
		<spring:property name="maxEntries" value="${cache.max.entries}"/>
		<spring:property name="expirationInterval" value="${cache.expiration.interval}"/>
    </custom-object-store>
</ee:object-store-caching-strategy>
```
[f338c593]: https://help.mulesoft.com/s/article/How-to-implement-a-distributed-cache-on-CloudHub-using-ObjectStore "Distributed Cache on CloudHub"
