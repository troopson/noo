/**
 * 
 */
package noo.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import noo.util.S;
 

/**
 * @author 瞿建军    troopson@163.com
 * 2018年3月27日
 */
@Component 
public class SQLHolder {

	private boolean loaded = false;
	private final Map<String,String> sqls = new HashMap<>();
	 
	
	public String getSQL(String key) {
		if(!this.loaded)
			this.load();
		if(key!=null && !key.endsWith(".sql"))
			key=key+".sql";
		return sqls.get(key);
	}
	
	
	public synchronized void load() {
		
		if(loaded)
			return;
		
		loaded = true;
		
		this.findInner(); 
		 	
	}

	
	private void findInner() {
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            //获取所有匹配的文件
            Resource[] resources = resolver.getResources("/sqls/**/*.sql");
            for(Resource resource : resources) {
                //获得文件流，因为在jar文件中，不能直接通过文件资源路径拿到文件，但是可以在jar包中拿到文件流
                InputStream stream = resource.getInputStream();
                
                String filename = resource.getFilename();
                String content = S.readAndCloseInputStream(stream, "UTF-8");
                this.sqls.put(filename, content);
            }
        } catch (IOException e) {
        }
	}
	
	
}
