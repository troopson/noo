/**
 * 
 */
package noo.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * @author qujianjun   troopson@163.com
 * 2018年6月7日 
 */
public class MyJsonConverter extends JsonSerializer<IJson>  {

	@Override
	public void serialize(IJson value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeObject(value.convertToJson());
		 
	}

	

	
}
