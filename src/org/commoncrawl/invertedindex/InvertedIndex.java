package org.commoncrawl.invertedindex;
import java.util.*;

public class InvertedIndex {
        private Hashtable<String, Postings> _dict = new Hashtable<String, Postings>();
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

	public Set<Map.Entry<String ,Postings>> getSet()
	{
		return _dict.entrySet();
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
                        Postings list = new Postings(token, new Postings.Posting(docId, pos));
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
