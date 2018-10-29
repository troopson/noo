/**
 * 
 */
package noo.event;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.CommandLineRunner;

import noo.util.S;
import noo.util.SpringContext;

/**
 * @author 瞿建军       Email: troopson@163.com
 * 2016年10月5日
 */
public class ListenerPool implements CommandLineRunner{
	
	public static final Log log = LogFactory.getLog(ListenerPool.class);

	private static ListenerPool pool;
	
	private static final ExecutorService ES = new ThreadPoolExecutor(2, 60,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1024), new ThreadPoolExecutor.AbortPolicy());
	
	private static ListenerPool getPool() {
		if(pool==null) {
			pool=SpringContext.getBean(ListenerPool.class);
		}
		return pool;
	}
 
	
	private Map<String,List<Listener>> listens=null;
	
	//======================================================================
	 
	
	//异步
	public static final void doEvent(String evnetName,Event e){
		ES.execute(()->callEvent(evnetName,e));		
	}
	
	public static final Object callEvent(String evnetName,Event e){
		if(S.isBlank(evnetName)) {
			return null;
		}
		
		ListenerPool p = getPool();
		if(p.listens==null) {
			p.init();
		}
		
		List<Listener> mlist = p.listens.get(evnetName);
		
		if(mlist==null || mlist.isEmpty()) {
			return null;
		}
		
		for(Listener u : mlist) {
			u.invoke(e);
		}
		
		return e.getResult();
		
	}
	
	
	
	@Override
	public void run(String... args) throws Exception {
		
		try {
			init();
		}catch(java.lang.IllegalStateException e) {
			
		}
		
				
	}
	
	private void init() {
		
		this.listens = new HashMap<>();
		
		Map<String, Listener> i =SpringContext.getBeansOfType(Listener.class);
		for(Listener o: i.values()){
			  String[] eventNames= o.on();
			  if(eventNames==null || eventNames.length ==0) {
				continue;
			}
			  for(String ae: eventNames) {
				this.putListen2Pool(ae, o);
			}
				  
		
		}
			
		log.info("Event listens initialized, event type amount: "+ this.listens.size() );

	}
	
	
	private void putListen2Pool(String eventName, Listener j){
		
		List<Listener> l = this.listens.get(eventName);
		if(l==null){
			l=new LinkedList<>();
			this.listens.put(eventName, l);
		}
		l.add(j);
		
	}
	
	

}
