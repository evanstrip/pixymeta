package pixy.meta.iptc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pixy.meta.Metadata;
import pixy.meta.MetadataType;
import pixy.meta.iptc.IPTCDataSet;
import pixy.meta.iptc.IPTCReader;
import cafe.io.IOUtils;

public class IPTC extends Metadata {
	private IPTCReader reader;
	private Map<String, List<IPTCDataSet>> datasetMap;
	
	public static void showIPTC(byte[] iptc) {
		if(iptc != null && iptc.length > 0) {
			IPTCReader reader = new IPTCReader(iptc);
			try {
				reader.read();
				reader.showMetadata();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void showIPTC(InputStream is) {
		try {
			showIPTC(IOUtils.inputStreamToByteArray(is));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public IPTC() {
		super(MetadataType.IPTC, null);
		datasetMap =  new HashMap<String, List<IPTCDataSet>>();
	}
	
	public IPTC(byte[] data) {
		super(MetadataType.IPTC, data);
		reader = new IPTCReader(data);
	}
	
	public void addDataSet(IPTCDataSet dataSet) {
		if(datasetMap != null) {
			String name = dataSet.getName();
			if(datasetMap.get(name) == null) {
				List<IPTCDataSet> list = new ArrayList<IPTCDataSet>();
				list.add(dataSet);
				datasetMap.put(name, list);
			} else if(dataSet.allowMultiple()) {
				datasetMap.get(name).add(dataSet);
			}
		}
	}

	/**
	 * Get a string representation of the IPTCDataSet associated with the key
	 *  
	 * @param key the name for the IPTCDataSet
	 * @return a String representation of the IPTCDataSet, separated by ";"
	 */
	public String getAsString(String key) {
		// Retrieve the IPTCDataSet list associated with this key
		// Most of the time the list will only contain one item
		List<IPTCDataSet> list = getDataSet(key);
		
		String value = "";
	
		if(list != null) {
			if(list.size() == 1) {
				value = list.get(0).getDataAsString();
			} else {
				for(int i = 0; i < list.size() - 1; i++)
					value += list.get(i).getDataAsString() + ";";
				value += list.get(list.size() - 1).getDataAsString();
			}
		}
			
		return value;
	}
	
	/**
	 * Get all the IPTCDataSet as a map for this IPTC data
	 * 
	 * @return a map with the key for the IPTCDataSet name and a list of IPTCDataSet as the value
	 */
	public Map<String, List<IPTCDataSet>> getDataSet() {
		if(datasetMap != null)
			return datasetMap;
		return reader.getDataSet();
	}
	
	/**
	 * Get a list of IPTCDataSet associated with a key
	 * 
	 * @param key name of the data set
	 * @return a list of IPTCDataSet associated with the key
	 */
	public List<IPTCDataSet> getDataSet(String key) {
		return getDataSet().get(key);
	}
	
	public IPTCReader getReader() {
		return reader;
	}
	
	public void showMetadata() {
		if(datasetMap != null){
			// Print multiple entry IPTCDataSet
			for(List<IPTCDataSet> iptcs : datasetMap.values()) {
				for(IPTCDataSet iptc : iptcs)
					iptc.print();
			}
		} else
			super.showMetadata();
	}
}