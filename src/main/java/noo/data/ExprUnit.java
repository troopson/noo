/**
 * 
 */
package noo.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import noo.json.JsonArray;
import noo.json.JsonObject;
import noo.util.BigDecimalUtil;
import noo.util.S;
import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.eval.Variable;
import parsii.tokenizer.ParseException;

/**
 * @author qujianjun   troopson@163.com
 * Nov 4, 2019 
 */
public class ExprUnit implements IProcessUnit {

	
	private Map<String,Object[]> exprs;
	
	
	public void addExpr(String destField, String expr, int scale) throws ParseException {
		if(S.isBlank(destField) || S.isBlank(expr))
			return;
		if(exprs==null)
			this.exprs = new HashMap<>();
		
		Scope scope = new Scope();
		Expression an_expr = Parser.parse(expr, scope);
		Collection<Variable> cvs = scope.getLocalVariables();
		exprs.put(destField, new Object[] {an_expr, cvs,scale});
		
	}
	
	@Override
	public void before(JsonArray ja) { 
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processRow(JsonObject j) {
		if(exprs==null)
			return;
		
		try {
			exprs.forEach((k,v)->{
				Object[] vs = (Object[])v;
				Expression an_expr = (Expression)vs[0];
				Collection<Variable> cvs = (Collection<Variable>)vs[1];
				int scale = (int)vs[2];
				cvs.forEach(p->{
					String name = p.getName();
					p.setValue(j.getDouble(name));
				});
				double value = an_expr.evaluate();
				double result = BigDecimalUtil.round(value, scale, BigDecimal.ROUND_HALF_UP);
				j.put(k, result);
			});
		} catch (Exception e) {  
			throw new RuntimeException(e);
		}
		 
	}

	@Override
	public void end(JsonArray ja) { 
		
	}

}
