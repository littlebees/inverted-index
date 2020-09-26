package org.commoncrawl.invertedindex;
import java.util.List;
import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.*;
import java.nio.file.StandardOpenOption;
import java.nio.file.*;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Indexer {
	static class HTMLFile
	{
		static class HtmlParser
		{
			private Document doc;
			public String getTitle()
			{
				return doc.select("h1").first().text();
			}
			
			public Elements getContent()
			{
				return doc.select("p");
			}
			// =======================
			public void parseByURL(String url) throws IOException
			{
				doc = Jsoup.connect(url).get();
			}
			public void parseByString(String html)
			{
				doc = Jsoup.parse(html);
			}
		};
		private HtmlParser _parser = new HtmlParser();
		public void parseHTML(String html)
		{
			_parser.parseByString(html);
		}
		public void parseURL(String url) throws IOException
		{
			_parser.parseByURL(url);
		}
		public List<Tokener> getTokenedHTML() throws IOException
		{
			ArrayList<Tokener> ans = new ArrayList<Tokener>();
			Elements elems = _parser.getContent();
			for(Element elem : elems)
				ans.add(new Tokener(elem.text()));
			return ans;
		}
	}
	private HTMLFile _html = new HTMLFile();
        private InvertedIndex _theIndex = new InvertedIndex();
        private List<InvertedIndex> _iiBuffer = new ArrayList<InvertedIndex>();
        private final int _threshold = 5;
        private int _count = 0;
        private int _fileCount = 0;
        //public SecondaryAccessor _hdd;

        public void addDocument(List<Tokener> tt) throws IOException
        {
                _iiBuffer.add(InvertedIndex.makeIIfromNewFile(tt, _fileCount));
                _count++;
                _fileCount++;
                if(_count >= _threshold)
                        flushiiBuffer(); 
        }

	public void addDocument(String html) throws IOException
        {
                _html.parseHTML(html);
		addDocument(_html.getTokenedHTML());
        }
        public void flushiiBuffer() throws IOException //a.k.a buffer merge with HD's II
        {
                for(InvertedIndex ii : _iiBuffer)
                        _theIndex.merge(ii);
                outputFile();
                _count = 0;
                _iiBuffer.clear();
        }
	public String toString()
	{
		return _theIndex.toString();
	}

        public void outputFile() throws IOException
        {
                Path dict = validateFile("dictionary.txt");
                Path index = validateFile("index.txt");
                Set<Map.Entry<String ,Postings>> set = _theIndex.getSet();
                for(Map.Entry<String ,Postings>entry : set){
                    String key = entry.getKey();
                    Postings val = entry.getValue();
                    Files.write(dict, (key + "\n").getBytes(), StandardOpenOption.APPEND);
                    Files.write(index, (val + "\n").getBytes(), StandardOpenOption.APPEND);
                }
        }

        public void AnswerQuestion() throws IOException
        {
            flushiiBuffer();
            outputFile();
        }

        private Path validateFile(String path) throws IOException
        {
            Path p = Paths.get(path);
            Files.write(p, "".getBytes());
            return p;
        }
}
