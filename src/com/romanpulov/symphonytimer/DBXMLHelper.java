package com.romanpulov.symphonytimer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

public class DBXMLHelper {
	
	private static DBXMLHelper dbXMLHelperInstance = null;
	private Context context;
	
	private DBXMLHelper(Context context) {
		this.context = context;
	}
	
	public static DBXMLHelper getInstance(Context context) {
		if (null == dbXMLHelperInstance) {
			dbXMLHelperInstance = new DBXMLHelper(context);			
		}		
		return dbXMLHelperInstance;
	}
	
	private String getTableItem(String tableName) {
		return tableName + "_item";
	}
	
	private void writeXmlTable(String tableName, XmlSerializer xmlSerializer) throws IOException {
		
		final String tableItem = getTableItem(tableName);
		
		List<DBHelper.RawRecItem> timers = DBHelper.getInstance(context).getRawTable(tableName);
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
	
	private void logXMLDocument(InputStream inputStream) throws XmlPullParserException, IOException {
		
		XmlPullParser xmlParser = Xml.newPullParser();
    	xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
    	xmlParser.setInput(inputStream, null);

    	//log everything in the xml
    	Log.d("DBXMLHelper_parseDBXML", "Log all xml ===================================================");
    	int eventType = xmlParser.getEventType();
    	while (eventType != XmlPullParser.END_DOCUMENT) {
    		
    		Log.d("DBXMLHelper_parseDBXML", "Name = " + xmlParser.getName() + ", event = " + eventType + ", text = " + xmlParser.getText());
    		
    		xmlParser.next();
    		eventType = xmlParser.getEventType();
    	}
		
	}
	
	public void parseDBXML(InputStream inputStream) {
		
		Map<String, List<DBHelper.RawRecItem>> tableData = new HashMap<String, List<DBHelper.RawRecItem>>();
		
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
        	int a1_s = 0;        	
        	while ((eventType != XmlPullParser.END_DOCUMENT) && (10000 > a1_s)) {
        		
        		switch (a1_s) {
        		case 0:
        			Log.d("DBXMLHelper_parseDBXML", "case = 0");
        			//starting        			
        			
                	//reading root
                	xmlParser.next();
                	//check for correct tag
                	xmlParser.require(XmlPullParser.START_TAG, null, DBOpenHelper.DATABASE_NAME);
                	//move to read tables
                	a1_s = 100;
                	break;
                	
        		case 100:
        			Log.d("DBXMLHelper_parseDBXML", "case = 100");
        			//searching for some table name
        			
        			xmlParser.next();
        			Log.d("DBXMLHelper_parseDBXML", "case = 100, event = " + xmlParser.getEventType() + ", name = " + xmlParser.getName());
        			
        			//getting table name
        			tableName = xmlParser.getName();
        			//checking name
        			if ((DBOpenHelper.TIMER_TABLE_NAME.equals(tableName)) || (DBOpenHelper.TIMER_HISTORY_TABLE_NAME.equals(tableName))) {
        				tableDataRecList = new ArrayList<DBHelper.RawRecItem>();
        				tableData.put(tableName, tableDataRecList);
        				tableItem = getTableItem(tableName);
        				a1_s = 200;

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
        			Log.d("DBXMLHelper_parseDBXML", "case = 200, tableName = " + tableName + ", tableItem = " + tableItem);
        			
        			//reading table item
        			xmlParser.next();
        			
        			Log.d("DBXMLHelper_parseDBXML", "case = 200, event = " + xmlParser.getEventType() + ", name = " + xmlParser.getName());

        			if ((XmlPullParser.START_TAG == xmlParser.getEventType()) && (tableItem.equals(xmlParser.getName()))) {
        				//reading table item attributes        				        				
        				a1_s = 300;
        			} else {
        				if ((XmlPullParser.END_TAG == xmlParser.getEventType())) {
        					//move to read next table
        					a1_s = 100;
        				} else {        				
	        				//no table closing tag found where expected
	        				a1_s = 10200;
        				}
        			}
        			
        			break;
        			
        		case 300:
        			Log.d("DBXMLHelper_parseDBXML", "case = 300, name = " + xmlParser.getName());
        			
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
        					
        					//move to read next table item
        					a1_s = 200;
        				} else {
        					//unexpected event
        					a1_s = 10300;
        				}
        			}
        			
        			break;
        		
        		case 400:
        			Log.d("DBXMLHelper_parseDBXML", "case = 400, fieldName = " + fieldName);
        			
        			//reading field text
        			xmlParser.next();
        			
        			//attribute name
        			if (XmlPullParser.TEXT == xmlParser.getEventType()) {
        				//read text
        				fieldValue = xmlParser.getText();
        				//
        				Log.d("DBXMLHelper_dataDBXML", "tableName = " + tableName + ", fieldName = " + fieldName + ", fieldValue = " + fieldValue);
        				tableDataRecItem = new DBHelper.RawRecItem();
        				tableDataRecItem.setFieldNameValue(fieldName, fieldValue);
        				a1_s = 500;
        				
        			} else {
        				//text not found where expected
        				a1_s = 10400;
        			}
        			
        			break;
        		
        		case 500:
        			Log.d("DBXMLHelper_parseDBXML", "case = 500, fieldName = " + fieldName + ", fieldValue = " + fieldValue);
        			
        			//reading text closing tag
        			xmlParser.next();
        			
        			if (XmlPullParser.END_TAG == xmlParser.getEventType()) {
        				//move to next attribute
        				tableDataRecList.add(tableDataRecItem);
        				a1_s = 300;
        			} else {
        				//text closing tag not found
        				a1_s = 10500;
        			}
        			
        			break;
        			
        		}
        		
        	}
        	
        	Log.d("DBXMLHelper_parseDBXML", "Exiting with state =" + a1_s);
        	
        	//for test purposes
        	logXMLDocument(inputStream);
        	
        } catch (XmlPullParserException e) {
        	
        	e.printStackTrace();
        	
        }
        catch (IOException e) {
        	
        	e.printStackTrace();
        	
        }
	}

}
