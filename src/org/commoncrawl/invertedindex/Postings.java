package org.commoncrawl.invertedindex;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.*;

public class Postings {
	static class Posting
	{
		private int _docId;
		private int _count;
		private List<Integer> _positions;
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
        private String _term;
        private int _totalCount;
        private List<Posting> _postings;
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
