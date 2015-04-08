package com.romanpulov.symphonytimer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
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
	
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    return result;
	}
	
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	 }
	
	public void parseDBXML(InputStream inputStream) {
		
        XmlPullParser xmlParser = Xml.newPullParser();
        
        try {
        	
        	xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        	xmlParser.setInput(inputStream, null);

        	/*
        	//reading root
        	xmlParser.nextTag();
        	//check for correct tag
        	xmlParser.require(XmlPullParser.START_TAG, null, DBOpenHelper.DATABASE_NAME);
        	
        	
        	int eventType = xmlParser.getEventType();
        	while (eventType != XmlPullParser.END_DOCUMENT) {
        		
        		if (DBOpenHelper.TIMER_TABLE_NAME.equalsIgnoreCase(xmlParser.getName())) {
        		
        			Log.d("DBXMLHelper_parseDBXML", "Name = " + xmlParser.getName());
        			xmlParser.next();
        			xmlParser.require(XmlPullParser.START_TAG, null, getTableItem(DBOpenHelper.TIMER_TABLE_NAME));
        			Log.d("DBXMLHelper_parseDBXML", "Require table item passed" );
        			
        			while (xmlParser.next() != XmlPullParser.END_TAG) {
        				
        		        if (xmlParser.getEventType() != XmlPullParser.START_TAG) {
        		            continue;
        		        }
        		        
        		        String name = xmlParser.getName();        		        
        		        
        		        Log.d("DBXMLHelper_parseDBXML", "Inside name = " + name );
        		        
        		        
        		        if (name.equals("title")) {
        		        	
        		        	xmlParser.require(XmlPullParser.START_TAG, null, "title");
        		        	String title = readText(xmlParser);
        		        	xmlParser.require(XmlPullParser.END_TAG, null, "title");
        		        	
        		        	Log.d("DBXMLHelper_parseDBXML", "Read title:" + title );
        		        	
        		        } else {
        		            skip(xmlParser);
        		        }
        		        
        		    } 
        			
        		}
        		
        		eventType = xmlParser.next();
        	}
        	*/
        	
        	
        	String tableName = null;
        	String tableItem = null;
        	String fieldName = null;
        	String fieldValue = null;
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
        				a1_s = 300;
        			} else {
        				//text closing tag not found
        				a1_s = 10500;
        			}
        			
        			break;
        			
        		}
        		
        		
        	}
        	
        	Log.d("DBXMLHelper_parseDBXML", "Exiting with state =" + a1_s);
        	
        	
        	//log everything in the xml
        	Log.d("DBXMLHelper_parseDBXML", "Log all xml ===================================================");
        	eventType = xmlParser.getEventType();
        	while (eventType != XmlPullParser.END_DOCUMENT) {
        		
        		Log.d("DBXMLHelper_parseDBXML", "Name = " + xmlParser.getName() + ", event = " + eventType + ", text = " + xmlParser.getText());
        		
        		xmlParser.next();
        		eventType = xmlParser.getEventType();
        	}

        	
        	/*
        	
        	while (xmlParser.next() != XmlPullParser.END_TAG) {
        		
        		if (xmlParser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }        		
        		
        		String name = xmlParser.getName();
        		Log.d("DBXMLHelper_parseDBXML", "Name = " + name);        		
        		
        	}
        	*/
        	
        	
        } catch (XmlPullParserException e) {
        	
        }
        catch (IOException e) {
        	
        }
	}

}
