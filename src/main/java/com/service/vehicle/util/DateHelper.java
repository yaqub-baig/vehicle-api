package com.service.vehicle.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateHelper {
	
	public static Date dbTimeStampp() {
		SimpleDateFormat sdfAmerica = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		TimeZone tzAmerica = TimeZone.getTimeZone("America/New_York");
		sdfAmerica.setTimeZone(tzAmerica);
		Date date = new Date();
		String dateAmerica = sdfAmerica.format(date);
		Date dateInAmerica = null;
		try {
			dateInAmerica = formatter.parse(dateAmerica);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateInAmerica;
	}
	
	
	public static Date getVerifyEndDate() {
		SimpleDateFormat sdfAmerica = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date date = new Date();
		Date dateInAmerica = null;
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, 1);
		String strDate = sdfAmerica.format(c.getTime());
		try {
			dateInAmerica = formatter.parse(strDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateInAmerica;
	}
	
	public static Date getDateWith90Days() {
		SimpleDateFormat sdfAmerica = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date date = new Date();
		Date dateInAmerica = null;
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, 90);
		String strDate = sdfAmerica.format(c.getTime());
		try {
			dateInAmerica = formatter.parse(strDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateInAmerica;
	}
	
	

}
