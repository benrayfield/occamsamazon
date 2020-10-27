/** Ben F Rayfield offers this software opensource MIT license */
package mutable.util;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** FIXME, dont just implement get and put. theres iterator and putAll etc */
public class FunctionMap<K,W,V> extends HashMap<K,W> implements Map<K,W>{
	
	protected Function<W,V> toV;
	
	protected Function<V,W> fromV;
	
	protected Map<K,V> wrap;
	
	public FunctionMap(Function<W,V> toV, Function<V,W> fromV, Map<K,V> wrap){
		this.toV = toV;
		this.fromV = fromV;
		this.wrap = wrap;
	}

	public W get(Object key){
		return fromV.apply(wrap.get(key));
	}
	
	public W put(K key, W value){
		return fromV.apply(wrap.put(key, toV.apply(value)));
	}
	
	public int size(){
		return wrap.size();
	}

}
