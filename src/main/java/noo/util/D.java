package noo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/*
 * 
    TimeZone.getTimeZone("Asia/Shanghai")
    TimeZone.getTimeZone("America/Los_Angeles")
 * 
 */

/**
* @author  瞿建军      
* 
* 创建时间： 2016年6月12日  下午5:43:43
* 
*/
public class D {
	public static final String DATE_TIME_FMT = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_FMT = "yyyy-MM-dd";

	public static Date toDate(String dateStr) {
		return toDate(dateStr, null);
	}
	
	public static String defaultTimeZone() {
		return TimeZone.getDefault().getID();
	}
	
		
	public static Date toTimeZoneDate(String dateStr,String timezone) {
		if(S.isBlank(timezone)) {
			return toDate(dateStr, null);
		}
		TimeZone t= TimeZone.getTimeZone(timezone);
		return toDate(dateStr, t);
	}
	
	public static Date toDate(String dateStr,TimeZone timezone) {
		if (dateStr.length() == 10) {
			dateStr = dateStr + " 00:00:00";
		}
		
		return toDate(dateStr, timezone, DATE_TIME_FMT);
	}
	
	public static Date toDate(String dateStr,TimeZone timezone, String fmt) {
		try {
			if (!S.isBlank(dateStr)) {				
				SimpleDateFormat sdf = new SimpleDateFormat(fmt);
				if(timezone!=null) {
					sdf.setTimeZone(timezone);
				}
				Date date = sdf.parse(dateStr);
				return date;
			}

		} catch (ParseException e) {
			//e.printStackTrace();
		}
		return null;
	}
	
	
	public static String toTimeZone(Date date,String timezone,String Fmt){
		SimpleDateFormat sdf = new SimpleDateFormat(Fmt);
		if(S.isNotBlank(timezone)) {
			sdf.setTimeZone(TimeZone.getTimeZone(timezone));
		}
		return sdf.format(date);
	}
	
	public static String toTimeZone(Date date,String timezone){
		return toTimeZone(date,timezone,DATE_TIME_FMT);		
	}

	public static String today() {
		LocalDate today = LocalDate.now();
		return today.format(DateTimeFormatter.ofPattern(DATE_FMT));
	}
	
	public static Date todayStart() {
		LocalDate today = LocalDate.now(); 
		ZoneId zone = ZoneId.systemDefault();     
	    return Date.from(today.atStartOfDay().atZone(zone).toInstant()); 
	}
	
	public static Date todayEnd() {
		LocalDate today = LocalDate.now(); 
		ZoneId zone = ZoneId.systemDefault();     
	    return Date.from(today.plusDays(1).atStartOfDay().atZone(zone).toInstant().minusMillis(1)); 
	}
	
	public static String now(){
		LocalDateTime today = LocalDateTime.now();
		return today.format(DateTimeFormatter.ofPattern(DATE_TIME_FMT));
	}

	public static int diff(Date destday, Date nowday) {
		ZoneId z =  ZoneId.systemDefault();
		LocalDate n = LocalDateTime.ofInstant(nowday.toInstant(),z).toLocalDate();
		LocalDate d = LocalDateTime.ofInstant(destday.toInstant(),z).toLocalDate();
		
		long l = ChronoUnit.DAYS.between(n,d);
		return new Long(l).intValue();
	
	}
	
	public static boolean is1EarlyThan2(Date d1,Date d2){
		if(d1.getTime()<d2.getTime()) {
			return true;
		}
		return false;
	}

	//================================================
	
	public static String preDay() {
		Date c = desDay(new Date());
		return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
	}
	
	public static String nextDay() {
		Date c = addDay(new Date());
		return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
	}
	
	public static Date addDay(Date d){
		return offsetDay(d, 1,null);
	}
	public static Date addDay(Date d, String timezone){
		return offsetDay(d, 1,timezone);
	}
	
	public static Date desDay(Date d){
		return offsetDay(d, -1,null);
	}
	
	public static Date desDay(Date d, String timezone){
		return offsetDay(d, -1,timezone);
	}
	
	public static Date offsetDay(Date d, int i, String timezone){		
		ZoneId zid = S.isBlank(timezone)?ZoneId.systemDefault():ZoneId.of(timezone);
		LocalDateTime  ld = LocalDateTime.ofInstant(d.toInstant(),zid);
		if(i>0) {
			ld=ld.plusDays(i);
		} else {
			ld=ld.minusDays(-1*i);
		}
		return Date.from(ld.atZone(zid).toInstant());
		
	}
	public static Date offsetHour(Date d, int i){
		return translation(d, Calendar.HOUR_OF_DAY, i,null);
	}
	public static Date offsetHour(Date d, int i, String timezone){
		return translation(d, Calendar.HOUR_OF_DAY, i,timezone);
	}	
	
	private static Date translation(Date d, int field, int value, String timezone){
		Calendar c = Calendar.getInstance();
		if(S.isNotBlank(timezone)) {
			c.setTimeZone(TimeZone.getTimeZone(timezone));
		}
		c.setTime(d);
		c.add(field, value);
		return c.getTime();
	}
	
	public static Date offsetMinute(Date d, int minute){
		long nl = d.getTime();
		nl = 1000*60*minute+nl;
		Date n = new Date(nl);
		return n;		
	}
	
	public static Date offsetSecond(Date d, int second){
		long nl = d.getTime();
		nl = 1000*second+nl;
		Date n = new Date(nl);
		return n;		
	}
	
	public static int diffHours(Date early, Date later){
		Long l = (later.getTime()-early.getTime())/(1000*60*60) ;
		return l.intValue();
	}
		
	//=============================================================
	
	public static String strD(Date d){
		SimpleDateFormat df = new SimpleDateFormat(DATE_FMT);
		return df.format(d);
	}
	public static String strDT(Date d){
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FMT);
		return sdf.format(d);
	}

	public static String format(Date d, String patten) {
		if(d==null) {
			return "";
		}
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return new SimpleDateFormat(patten).format(c.getTime());
	}

	public static int getHours(Date timestamp) {
		Calendar c = Calendar.getInstance();
		c.setTime(timestamp);
		return c.get(Calendar.HOUR_OF_DAY);
	}
	
	public static int getMinute(Date timestamp) {
		Calendar c = Calendar.getInstance();
		c.setTime(timestamp);
		return c.get(Calendar.MINUTE);
	}
	
	public static Date dayStart(Date date) {
		ZoneId zone = ZoneId.systemDefault(); 
        Instant instant = date.toInstant(); 
	    LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
	    LocalDate localDate = localDateTime.toLocalDate();  
	    return Date.from(localDate.atStartOfDay().atZone(zone).toInstant()); 
	}
	
	public static Date dayEnd(Date date) {
		ZoneId zone = ZoneId.systemDefault(); 
        Instant instant = date.toInstant(); 
	    LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
	    LocalDate localDate = localDateTime.toLocalDate();    
	    return Date.from(localDate.atStartOfDay().atZone(zone).toInstant().minusMillis(1)); 
	}
	
	
	public static Date firstDayOfMonth(Date date) {
		ZoneId zone = ZoneId.systemDefault(); 
	    Instant instant = date.toInstant(); 
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
		LocalDate localDate = localDateTime.toLocalDate(); 
		LocalDate lastDay =localDate.with(TemporalAdjusters.firstDayOfMonth());
	    return Date.from(lastDay.atStartOfDay().atZone(zone).toInstant()); 
	}
	
	public static Date lastDayOfMonth(Date date) {
		ZoneId zone = ZoneId.systemDefault(); 
	    Instant instant = date.toInstant(); 
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
		LocalDate localDate = localDateTime.toLocalDate(); 
		LocalDate lastDay =localDate.with(TemporalAdjusters.lastDayOfMonth());
	    return Date.from(lastDay.atStartOfDay().atZone(zone).toInstant()); 
	}
	
	public static Date firstDayOfWeek(Date date) {
		ZoneId zone = ZoneId.systemDefault(); 
	    Instant instant = date.toInstant(); 
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
		LocalDate localDate = localDateTime.toLocalDate(); 
		LocalDate weekDate =localDate.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
	    return Date.from(weekDate.atStartOfDay().atZone(zone).toInstant()); 
	}
	
	public static Date lastDayOfWeek(Date date) {
		ZoneId zone = ZoneId.systemDefault(); 
	    Instant instant = date.toInstant(); 
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
		LocalDate localDate = localDateTime.toLocalDate(); 
		LocalDate weekDate =localDate.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
	    return Date.from(weekDate.atStartOfDay().atZone(zone).toInstant()); 
	}
	

	public static void main(String[] args) throws ParseException {
	
		System.out.println("diff: "+diff( toDate("2017-09-20 00:00:01"),toDate("2017-08-14 00:00:01")));
		System.out.println(diff(toDate("2017-11-25 00:00:01"), new Date()));
		Date now = new Date();
		System.out.println(D.strDT(now)+"  "+today());
		long l =1492672580000L;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(l);
		System.out.println(strDT(c.getTime()));
		
		System.out.println(is1EarlyThan2(toDate("2017-11-10 00:00:01"), new Date()));
		
		System.out.println(D.strDT(D.desDay(now)));
		
		Calendar ca = Calendar.getInstance();
		ca.setTimeInMillis(System.currentTimeMillis());
		System.out.println("before:"+ca.get(Calendar.MINUTE));
		ca.add(Calendar.MINUTE, -10);
		System.out.println("after:"+ca.get(Calendar.MINUTE));
		Date d2=new Date(c.getTimeInMillis());
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FMT);
		
		System.out.println("add 10 minutes:"+D.strDT(d2)+"   "+sdf.format(d2));
		System.out.println(D.strDT(D.translation(now, Calendar.MINUTE, 10,null)));
		
		System.out.println("------------------------------------------------------");
		
		System.out.println(D.strDT(now)+"   "+now.getTime());
		long nl = now.getTime();
		nl = 1000*60*10+nl;
		Date n = new Date(nl);
		System.out.println(D.strDT(n)+"   "+n.getTime());
		System.out.println(D.strDT(D.offsetMinute(now,10)));
		
		System.out.println("------------------------------------------------------");
		
		System.out.println(D.strDT(D.translation(now, Calendar.HOUR_OF_DAY, 1,null)));
		
		System.out.println("------------------------------------------------------");
		
		Date d3=new Date(c.getTimeInMillis());
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
		
		System.out.println(sdf2.format(d3));
		
		System.out.println("------------------------------------------------------");
		
		TimeZone t= TimeZone.getTimeZone("America/Los_Angeles");
		TimeZone t2= TimeZone.getTimeZone("Asia/Shanghai");
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf3.setTimeZone(t);
		Date d = sdf3.parse("2017-11-04 15:01:01");
		System.out.println(D.strDT(d));
		
		SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf4.setTimeZone(t2);
		Date d2t = sdf4.parse("2017-11-04 15:01:01");
		System.out.println(D.strDT(d2t));
		
		
		System.out.println(D.diffHours(D.toDate("2017-11-13 11:30:00"), new Date()));
		
		
		//String s2 ="21/Oct/2017:03:00:01 -0700";
		SimpleDateFormat sdf41 = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss",Locale.ENGLISH);
		sdf4.setTimeZone(t2);
		Date d2t1 = sdf41.parse("21/Oct/2017:02:03:01 -0700");
		System.out.println(D.strDT(d2t1));		
		
		System.out.println(D.getMinute(d2t1));
		
		Date d99 = D.toDate("2017-09-09");
		System.out.println(D.strD(D.offsetDay(d99, -1,null)));
		
		System.out.println(D.todayStart());
		System.out.println(D.todayEnd());
		System.out.println(D.dayStart(D.toDate("2017-11-13 11:30:00")));
		System.out.println(D.dayEnd(D.toDate("2017-11-13 11:30:00")));
		
	}

}
