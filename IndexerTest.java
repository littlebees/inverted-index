import java.util.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.io.*;
import static java.lang.System.out;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.tokenattributes.*;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
class Indexer
{
	public HTMLFile _html = new HTMLFile();
        public InvertedIndex _theIndex = new InvertedIndex();
        public List<InvertedIndex> _iiBuffer = new ArrayList<InvertedIndex>();
        public final int _threshold = 5;
        public int _count = 0;
        public int _fileCount = 0;
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
                Set<Map.Entry<String ,Postings>> set = _theIndex._dict.entrySet();
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

class HtmlParser
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
}

class HTMLFile
{
	public HtmlParser _parser = new HtmlParser();
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

class Tokener
{
        public StandardAnalyzer analyzer = new StandardAnalyzer();
        public List<String> _tokens = new ArrayList<String>(100);
        public int _totalTokens = 0;
        public int _cursor = 0;
	public Tokener(String s) throws IOException
	{
		tokenString(s);
	}
        public void tokenString(String text) throws IOException
        {
                _totalTokens = 0;
                _cursor = 0;
                _tokens.clear();
               // String text = "Lucene is simple yet powerful java based search library.";
                TokenStream stream = analyzer.tokenStream(null, new StringReader(text));
                CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
                stream.reset();
                while (stream.incrementToken()) {
                        _tokens.add(cattr.toString());
                        _totalTokens++;
                }
                stream.end();
                stream.close();
        }

        public boolean hasNext()
        {
                return _cursor < _totalTokens;
        }

        public String getNext()
        {
                if(hasNext())
                {
                        String ans = _tokens.get(_cursor);
                        _cursor++;
                        return ans;
                }
                return null;
        }
}    
class InvertedIndex
{
        public Hashtable<String, Postings> _dict = new Hashtable<String, Postings>();
        // ===============
        public String toString()
        {
                String ans = "";
                for(Postings p : _dict.values())
                        ans = ans + p + "\n";
                return ans;
        }
        // ==============
        public void merge(InvertedIndex ii)
        {
                Set<String> keys = ii._dict.keySet();
                for(String token : keys)
                {
                        if(_dict.containsKey(token))
                        {
                                _dict.get(token).merge(ii._dict.get(token));
                        }
                        else
                        {
                                _dict.put(token, ii._dict.get(token));
                        }
                }
                
        }

        public void addToken(String token,int docId, int pos)
        {
                if(_dict.containsKey(token))
                {
                        Postings list = _dict.get(token);
                        list.Add(docId, pos);
                }
                else
                {
                        Postings list = new Postings(token, new Posting(docId, pos));
                        _dict.put(token, list);
                }
        }
        
        public static InvertedIndex makeIIfromNewFile(List<Tokener> tt, int docId)
        {
                InvertedIndex ii = new InvertedIndex();
                int pos = 0;
                for(Tokener t : tt)
                        pos = addByTokener(ii, t, docId, pos);
                return ii;
        }

        private static int addByTokener(InvertedIndex ii, Tokener t, int docId, int pos)
        {
                while(t.hasNext())
                {
                        ii.addToken(t.getNext(), docId, pos);
                        pos++;
                }
                return pos;
        }
}

class Postings
{
        public String _term;
        public int _totalCount;
        public List<Posting> _postings;
        // ==================
        public Postings(String token, Posting p)
        {
                _term = token;
                _totalCount = 1;
                _postings = new ArrayList<Posting>(Arrays.asList(p));
        }
        // ==================
        public String toString()
        {
                String ans = _term + ", " + _totalCount + ":\n";
                String pos = "<";
                int count = _postings.size();
                for(int i=0;i<count-1;i++)
                        pos = pos + (_postings.get(i) + ";\n");
                pos = pos + (_postings.get(count-1) + ">");
                return ans + pos;
        }
        // ==================
        public void merge(Postings p)
        {
                List<Posting> newList = new ArrayList<Posting>();
                int i = 0;
                int j = 0;
                int lenA = _postings.size();
                int lenB = p._postings.size();
                while(i < lenA || j < lenB)
                {
                        if(i < lenA && j < lenB && _postings.get(i)._docId == p._postings.get(j)._docId)
                        {
                                _postings.get(i).merge(p._postings.get(j));
                                newList.add(_postings.get(i));
                                i++;
                                j++;
                        }
                        else if(j >= lenB || i < lenA && _postings.get(i)._docId < p._postings.get(j)._docId)
                        {
                                newList.add(_postings.get(i));
                                i++;
                        }
                        else if(i >= lenA || j < lenB && p._postings.get(j)._docId < _postings.get(i)._docId)
                        {
                                newList.add(p._postings.get(j));
                                j++;
                        }
                        else
                                throw new RuntimeException("Should not be here");
                }
                _totalCount += p._totalCount;
                _postings = newList;
        }

        public void Add(int docId, int pos)
        {
                _totalCount++;
                int count = _postings.size();
                for(int i = 0;i<count;i++)
                        if(_postings.get(i)._docId > docId)
                        {
                                _postings.add(i, new Posting(docId, pos));
                                return ;
                        }
                        else if(_postings.get(i)._docId == docId)
                        {
                                _postings.get(i).Add(pos);
                                return ;
                        }
                _postings.add(new Posting(docId, pos));
        }
}

class Posting
{
        public int _docId;
        public int _count;
        public List<Integer> _positions;
        // ==================
        public Posting(int id, int pos)
        {
                _docId = id;
                _count = 1;
                _positions = new ArrayList<Integer>(Arrays.asList(pos));
        }
        // ==================
        public String toString()
        {
                String ans = _docId + ", " + _count + ": ";
                String pos = "<";
                for(int i=0;i<_count-1;i++)
                        pos = pos + (_positions.get(i) + ", ");
                pos = pos + (_positions.get(_count-1) + ">");
                return ans + pos;
        }

        public void Add(int pos)
        {
                _count++;
                _positions.add(pos);
        }
        // ==================
        public void merge(Posting p)
        {
                _count += p._count;
                _positions.addAll(p._positions);
                Collections.sort(_positions);
        }
}

class IndexerTest
{
        public static void main(String[] arg) throws IOException
        {
                String simpleHTML = "<p>hi my name is Feng. This is a demo. Feng Cheng</p>";
                Indexer indexer = new Indexer();
                indexer.addDocument(simpleHTML);
                indexer.addDocument(simpleHTML);
                indexer.addDocument(simpleHTML);
                indexer.addDocument(simpleHTML);
                indexer.addDocument(simpleHTML);
                indexer.AnswerQuestion(); 
        }
}
