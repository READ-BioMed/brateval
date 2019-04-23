package au.com.nicta.csp.brateval;
import java.util.LinkedList;
import java.util.Iterator;

public class HierList<E>
{
	private class Elem {
		int level;
		E entry;
		Elem(int l, E e) {entry=e; level=l;}
	}
	LinkedList<Elem> elems = new LinkedList<Elem>();

	public interface Visitor<X> {
		void pre (int currLevel, X curr, X parent);
		void post(int currLevel, X curr, X parent);
	}

	void add(int l, E e) {
		elems.add(new Elem(l, e));
	}

	Elem traverse(Visitor<E> v, Iterator<Elem> j, Elem e0, E parent) {
			//if (j < entries.size()) return j;
			//Visitor a = new Visitor(entities[j], parent);
			v.pre(e0.level, e0.entry, parent);

			Elem e = null;
			if (j.hasNext()) {
                e = j.next();
                do
                {
					if(e.level <= e0.level && parent != null) break;
					e = traverse(v, j, e, e0.entry);
				}
                while (e != null);
			}
			v.post(e0.level, e0.entry, parent);
			return e;
	}
	
	public void traverse(Visitor<E> v)
	{
		Iterator<Elem> j = elems.iterator();
		if (j.hasNext())
				traverse(v, j, j.next(), null);
	}
}
