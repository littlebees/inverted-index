package org.commoncrawl.examples;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.charset.*;
import java.io.*;

import org.apache.commons.io.IOUtils;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReaderFactory;

import org.commoncrawl.invertedindex.Indexer;


public class WARCReaderTest {    
        public static String getHTMLfromRaw(String raw)
        {
            if(raw.contains("<html>") && raw.contains("</html>"))
                return raw.substring(raw.indexOf("<html>"), raw.indexOf("</html>"));
            else
                return null;
        }
    
	public static void main(String[] args) throws FileNotFoundException, IOException 
        {
                System.out.println("Type DataSource Path Or just hit enter to use default data");
                //Scanner in = new Scanner(System.in);
                BufferedReader buf = new BufferedReader (new InputStreamReader (System.in));
                String input = buf.readLine();
                String fn = input.isEmpty() ? "TestingData/02.warc.gz" : input;
                System.out.println("Process: " + fn);
		FileInputStream is = new FileInputStream(fn);
		ArchiveReader ar = WARCReaderFactory.get(fn, is, true);
		Indexer _index = new Indexer();
		int i = 0;
                System.out.println("Start Index");
            for (ArchiveRecord r : ar) 
            {
                byte[] rawData = IOUtils.toByteArray(r, r.available());
                String content = new String(rawData);
                String html = getHTMLfromRaw(content);
                
                if(html != null)
                    _index.addDocument(html);
                else
                    continue;
            }
                System.out.println("Finish Index");
	}
}
