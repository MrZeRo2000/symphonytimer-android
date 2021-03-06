package com.romanpulov.symphonytimer.helper.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.Xml;

import com.romanpulov.symphonytimer.helper.db.DBHelper;
import com.romanpulov.symphonytimer.helper.db.DBOpenHelper;

public class DBXMLHelper {
	
	//private static DBXMLHelper dbXMLHelperInstance = null;
	private final Context mContext;
	
	public DBXMLHelper(Context context) {
		this.mContext = context;
	}
	
	/*
	public static DBXMLHelper getInstance(Context context) {
		if (null == dbXMLHelperInstance) {
			dbXMLHelperInstance = new DBXMLHelper(context);			
		}		
		return dbXMLHelperInstance;
	}
	*/
	
	private String getTableItem(String tableName) {
		return tableName + "_item";
	}
	
	private void writeXmlTable(String tableName, XmlSerializer xmlSerializer) throws IOException {
		
		final String tableItem = getTableItem(tableName);
		
		List<DBHelper.RawRecItem> timers = DBHelper.getInstance(mContext).getBackupTable(tableName);
		xmlSerializer.startTag("", tableName);
		
		for (DBHelper.RawRecItem timerRecItem: timers) {
			//start rec
			xmlSerializer.startTag(null, tableItem );
			
			Map<String, String> fields = timerRecItem.getFields();
			for (Map.Entry<String, String> fieldEntry : fields.entrySet()) {
				
				String value = fieldEntry.getValue();
				if (null != value) {
					//start field
					xmlSerializer.startTag(null, fieldEntry.getKey());
					//text
					xmlSerializer.text(value);
					//end field
					xmlSerializer.endTag(null, fieldEntry.getKey());
				}
			}				
			
			//end rec
			xmlSerializer.endTag(null, tableItem );
		}
		
		//end timers
		xmlSerializer.endTag("", tableName);	
		
		
	}
	
	public void writeDBXML(Writer writer) {
		
		XmlSerializer xmlSerializer = Xml.newSerializer();		
		
		try {
			
			// set writer
			xmlSerializer.setOutput(writer);
		
			//start document
			xmlSerializer.startDocument("UTF-8", true);
			//start root
			xmlSerializer.startTag("", DBOpenHelper.DATABASE_NAME);
	
			//timers
			writeXmlTable(DBOpenHelper.TIMER_TABLE_NAME, xmlSerializer);
			//history
			writeXmlTable(DBOpenHelper.TIMER_HISTORY_TABLE_NAME, xmlSerializer);
			
			//end root
			xmlSerializer.endTag("", DBOpenHelper.DATABASE_NAME);
			// end document
			xmlSerializer.endDocument();
			
		} catch( IOException e)	 {
			
			e.printStackTrace();
			
		}
		
	}
	
	public String getDBXML () {
		
		StringWriter stringWriter = new StringWriter();
		writeDBXML(stringWriter);
		return stringWriter.toString();
		
	}
	
	// this one is for debugging purposes only
	@SuppressWarnings("unused")
	private void logXMLDocument(InputStream inputStream) throws XmlPullParserException, IOException {
		XmlPullParser xmlParser = Xml.newPullParser();
    	xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
    	xmlParser.setInput(inputStream, null);

    	//log everything in the xml
    	int eventType = xmlParser.getEventType();
    	while (eventType != XmlPullParser.END_DOCUMENT) {
    		xmlParser.next();
    		eventType = xmlParser.getEventType();
    	}
	}
	
	public int parseDBXML(InputStream inputStream, Map<String, List<DBHelper.RawRecItem>> tableData ) {
		int res = 0;
		int a1_s = 0; 
		
        XmlPullParser xmlParser = Xml.newPullParser();
        
        try {
        	xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        	xmlParser.setInput(inputStream, null);
        	
        	String tableName = null;
        	String tableItem = null;
        	String fieldName = null;
        	String fieldValue = null;
        	List<DBHelper.RawRecItem> tableDataRecList = null;
        	DBHelper.RawRecItem tableDataRecItem = null;
        	
        	int eventType = xmlParser.getEventType();
        	      	
        	while ((eventType != XmlPullParser.END_DOCUMENT) && (10000 > a1_s)) {
        		
        		switch (a1_s) {
        		case 0:
        			//starting
                	//reading root
                	xmlParser.next();
                	//check for correct tag
                	xmlParser.require(XmlPullParser.START_TAG, null, DBOpenHelper.DATABASE_NAME);
                	//move to read tables
                	a1_s = 100;
                	break;
                	
        		case 100:
        			//searching for some table name
        			xmlParser.next();

        			//looking for table name name
        			if ((XmlPullParser.START_TAG == xmlParser.getEventType())) {
            			//getting table name
            			tableName = xmlParser.getName();
            			
            			if (null == tableName) {
            				
            				//table name not found
            				a1_s = 10100;
            				
            			} else {
            				//create data structure for table item
	        				tableDataRecList = new ArrayList<>();
	        				//table item name
	        				tableItem = getTableItem(tableName);
	        				
	        				//move to reading table item	        				
	        				a1_s = 200;
            			}
        			} else {
        				if ((XmlPullParser.END_TAG == xmlParser.getEventType()))  {
        					//finished reading tables
        					a1_s = 10001;
        				} else {
	        				// no table name found
	        				a1_s = 10100;
        				}
        			}
        			break;     		
        		case 200:
        			//reading table item
        			xmlParser.next();
        			
        			if ((XmlPullParser.START_TAG == xmlParser.getEventType()) && (tableItem.equals(xmlParser.getName()))) {
        				//create new record item
        				tableDataRecItem = /*DBHelper.getInstance(context).*/ new DBHelper.RawRecItem();
        				//move to reading table item attributes
        				a1_s = 300;
        			} else {
        				if ((XmlPullParser.END_TAG == xmlParser.getEventType())) {
        					//put read data
        					tableData.put(tableName, tableDataRecList);
        					
        					//move to read next table        					
        					a1_s = 100;
        				} else {
	        				//no table closing tag found where expected
	        				a1_s = 10200;
        				}
        			}
        			
        			break;
        		case 300:
        			//reading table item attributes
        			xmlParser.next();
        			
        			//attribute title
        			if (XmlPullParser.START_TAG == xmlParser.getEventType()) {
        				fieldName = xmlParser.getName();
        				//move to read text
        				a1_s = 400;
        			} else {
        				//start tag not found where expected
        				if (XmlPullParser.END_TAG == xmlParser.getEventType()) {
        					//save record item
        					tableDataRecList.add(tableDataRecItem);
        					//move to read next table item
        					a1_s = 200;
        				} else {
        					//unexpected event
        					a1_s = 10300;
        				}
        			}
        			break;
        		case 400:
        			//reading field text
        			xmlParser.next();
        			//attribute name
        			if (XmlPullParser.TEXT == xmlParser.getEventType()) {
        				//read text
        				fieldValue = xmlParser.getText();
        				//
        				tableDataRecItem.putFieldNameValue(fieldName, fieldValue);
        				a1_s = 500;
        			} else {
        				//text not found where expected
        				a1_s = 10400;
        			}
        			
        			break;
        		
        		case 500:
        			//reading text closing tag
        			xmlParser.next();
        			if (XmlPullParser.END_TAG == xmlParser.getEventType()) {
        				//move to next attribute
        				a1_s = 300;
        			} else {
        				//text closing tag not found
        				a1_s = 10500;
        			}
        			break;
        		}
        	}
        	//for test purposes
        	//logXMLDocument(inputStream);
        	
        } catch (XmlPullParserException e) {
        	e.printStackTrace();
        	res = 20000;
        }
        catch (IOException e) {
        	e.printStackTrace();
        	res = 30000;
        }
        res = (a1_s > 10001) ? a1_s : 0;
        return res;
	}
}
