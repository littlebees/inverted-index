package org.commoncrawl.invertedindex;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.io.StringReader;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.tokenattributes.*;

public class Tokener {
        private StandardAnalyzer analyzer = new StandardAnalyzer();
        private List<String> _tokens = new ArrayList<String>(100);
        private int _totalTokens = 0;
        private int _cursor = 0;
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
