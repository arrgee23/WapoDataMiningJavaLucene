package trec;

import java.io.File;
import java.io.IOException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.rowset.serial.SerialClob;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingJsonFactory;

public class DBSave {

	public static void main(String[] args) {
		// Class.forName(xxx) loads the jdbc classes and
		// creates a drivermanager class factory
		try {
			Class.forName(Constants.dbClassName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Properties for user and password. Here the user and password are both 'paulr'
		Properties p = new Properties();
		p.put("user", Constants.DB_USER);
		p.put("password", Constants.DB_PASSWORD);

		// Now try to connect
		Connection connect = null;
		try {
			connect = DriverManager.getConnection(Constants.CONNECTION, p);
			PreparedStatement preparedStatement = null;
			Report r = null;

			JsonFactory f = new MappingJsonFactory();
			JsonParser jp = null;
			try {
				File fl = new File(Constants.FILE_NAME2);
				jp = f.createJsonParser(fl);
			} catch (JsonParseException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			//Clob c = new SerialClob(new char[170000]);
			preparedStatement = connect
					.prepareStatement("insert into  wapo.reports values (?, ?, ?, ?, ? , ?, ?, ?) ON DUPLICATE KEY UPDATE url = 'duplicate'");
			int indexCount = 0;
			int indexCountBkup = indexCount;
			for (int i = 0; i < Constants.TOTAL_DOC_NO; i++) {

				r = ParsingOnly.readOneReport3(jp);

				
				
				if (r == null)
					continue;// Add fields to the document
				// PreparedStatements can use variables and are more efficient
				
				indexCount++;
				/*
				 * if(indexCount<=194074) continue;
				 * 
				 * try { c.setString(1, r.content); } catch(Exception e) {
				 * System.out.println(i); System.out.println(r.content.length()); }
				 */
				r.id = r.id.substring(0,Math.min(r.id.length(), 100-1));//.replace("\'", "").replace("\"", "");
				r.url=r.url.substring(0,Math.min(r.url.length(), 100-1));
				r.title = r.title.substring(0,Math.min(r.title.length(), 8000-1));
				r.author = r.author.substring(0,Math.min(r.author.length(), 200-1));
				r.date = r.date.substring(0,Math.min(r.date.length(), 100-1));
				r.source=r.source.substring(0,Math.min(r.source.length(), 100-1));
				r.articleType = r.articleType.substring(0,Math.min(r.articleType.length(), 100-1));
				r.content = r.content.substring(0,Math.min(r.content.length(), 8000-1));
				
				preparedStatement.setString(1, r.id);
				preparedStatement.setString(2, r.url);
	            preparedStatement.setString(3, r.title);
	            preparedStatement.setString(4, r.author);
	            preparedStatement.setString(5, r.date);
	            preparedStatement.setString(6, r.source);
	            preparedStatement.setString(7,r.articleType );
	            preparedStatement.setString(8, r.content);
	            //preparedStatement.setClob(8,c);
	            preparedStatement.addBatch();

	            if((indexCount%5000) == 0) {
	            	preparedStatement.executeBatch();
	            	System.out.printf("Indexed %d documents\n",indexCount);
	            	indexCountBkup = indexCount;
	            	
	            }
	            
			}
			// one last batch add
			if(indexCount>indexCountBkup) {
            	preparedStatement.executeBatch();
            	System.out.printf("Indexed %d documents\n",indexCount);
            }
			System.out.println("It works !");

			connect.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
