package io.pivotal.demo

object Util {
  
  
  def calculateDistance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double = {
    
		  def R = 6371 // Radius of the earth in km
		  val dLat = deg2rad(lat2-lat1);  // deg2rad below
		  val dLon = deg2rad(long2-long1); 
		  val a = 
		    Math.sin(dLat/2) * Math.sin(dLat/2) +
		    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * 
		    Math.sin(dLon/2) * Math.sin(dLon/2)
		    ; 
		  val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		  val d = R * c; // Distance in km
		  d
  }
  
  def deg2rad(deg: Double):Double = {    
    deg * (Math.PI/180);    
  }
  
}