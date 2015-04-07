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
