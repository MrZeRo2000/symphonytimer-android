package com.romanpulov.symphonytimer;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
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
	
	private void writeXmlTable(String tableName, XmlSerializer xmlSerializer) throws IOException {
		
		final String tableItem = tableName + "_item";
		
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
		//StringWriter writer = new StringWriter();		
		
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
			
		}
		
		//return writer.toString();
		
	}
	
	public String getDBXML () {
		
		StringWriter stringWriter = new StringWriter();
		writeDBXML(stringWriter);
		return stringWriter.toString();
		
	}

}
