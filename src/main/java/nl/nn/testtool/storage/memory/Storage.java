/*
   Copyright 2018 Nationale-Nederlanden

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
package nl.nn.testtool.storage.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.nn.testtool.MetadataExtractor;
import nl.nn.testtool.Report;
import nl.nn.testtool.storage.StorageException;
import nl.nn.testtool.util.SearchUtil;

/**
 * @author m00f069
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Storage implements nl.nn.testtool.storage.CrudStorage {
	protected String name;
	protected Map reports;
	protected List storageIds;
	protected List metadata;
	protected int storageId = 0;
	protected MetadataExtractor metadataExtractor;
	
	public Storage() {
		reports = new HashMap();
		storageIds = new ArrayList();
		metadata = new ArrayList();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setMetadataExtractor(MetadataExtractor metadataExtractor) {
		this.metadataExtractor = metadataExtractor;
	}

	public synchronized void store(Report report) {
		report.setStorage(this);
		report.setStorageId(new Integer(storageId++));
		reports.put(report.getStorageId(), report);
		storageIds.add(report.getStorageId());
		metadata.add(new HashMap());
	}

	public void update(Report report) throws StorageException {
		reports.put(report.getStorageId(), report);
		int i = storageIds.indexOf(report.getStorageId());
		metadata.remove(i);
		metadata.add(i, new HashMap());
	}

	public void delete(Report report) throws StorageException {
		reports.remove(report);
		int i = storageIds.indexOf(report.getStorageId());
		storageIds.remove(report.getStorageId());
		metadata.remove(i);
	}

	public void storeWithoutException(Report report) {
		store(report);
	}

	public int getSize() {
		return storageIds.size();
	}

	public synchronized List getStorageIds() {
		return new ArrayList(storageIds);
	}

	public synchronized List getMetadata(int numberOfRecords, List metadataNames,
			List searchValues, int metadataValueType) {
		List result = new ArrayList();
		for (int i = 0; i < metadata.size() && (numberOfRecords == -1 || i < numberOfRecords); i++) {
			Map metadataRecord = (Map)metadata.get(i);
			List resultRecord = new ArrayList();
			Iterator metadataNamesIterator = metadataNames.iterator();
			while (metadataNamesIterator.hasNext()) {
				String metadataName = (String)metadataNamesIterator.next();
				Object metadataValue;
//				if (!metadataRecord.keySet().contains(metadataName)) {
					Report report = getReport((Integer)storageIds.get(i));
					metadataValue = metadataExtractor.getMetadata(report,
							metadataName, metadataValueType);
					metadataRecord.put(metadataName, metadataValue);
//				} else {
//					// TODO hier wordt geen rekening gehouden met metadataValueType
//					metadataValue = metadataRecord.get(metadataName);
//				}
				resultRecord.add(metadataValue);
			}
			if (SearchUtil.matches(resultRecord, searchValues)) {
				result.add(resultRecord);
			}
		}
		return result;
	}

	public List getTreeChildren(String path) {
		// TODO implementeren?
		return new ArrayList();
	}

	public List getStorageIds(String path) throws StorageException {
		// TODO implementeren?
		return new ArrayList();
	}

	public synchronized Report getReport(Integer storageId) {
		return (Report)reports.get(storageId);
	}

	public String getErrorMessage() {
		return null;
	}

	public void close() {
	}

	public int getFilterType(String column) {
		return FILTER_RESET;
	}

	public List getFilterValues(String column) throws StorageException {
		return null;
	}

	public String getUserHelp(String column) {
		return SearchUtil.getUserHelp();
	}
}
