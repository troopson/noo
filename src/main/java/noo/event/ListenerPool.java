/**
 * 
 */
package noo.event;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

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
	
	private static ListenerPool getPool() {
		if(pool==null)
			pool=SpringContext.getBean(ListenerPool.class);
		return pool;
	}
 
	
	private Map<String,List<Listener>> listens=null;
	
	//======================================================================
	 
	
	//异步
	public static final void doEvent(String evnetName,Event e){
		Executors.newSingleThreadExecutor().execute(new Runnable(){

			@Override
			public void run() {
				
				callEvent(evnetName,e);
			}
			
		});		
	}
	
	public static final Object callEvent(String evnetName,Event e){
		if(S.isBlank(evnetName))
			return null;
		
		ListenerPool p = getPool();
		if(p.listens==null)
			p.init();
		
		List<Listener> mlist = p.listens.get(evnetName);
		
		if(mlist==null || mlist.isEmpty())
			return null;
		
		for(Listener u : mlist)
			u.done(e);
		
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
			  if(eventNames==null || eventNames.length ==0)
				  continue;
			  for(String ae: eventNames)
				  this.putListen2Pool(ae, o);
				  
		
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
