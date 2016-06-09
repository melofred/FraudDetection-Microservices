package io.pivotal.demo

import scala.collection.mutable.ListBuffer

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.rdd._
import org.apache.spark.sql.expressions.WindowSpec
import org.apache.spark.sql.functions._
import org.apache.spark.mllib.rdd.RDDFunctions._
import scala.collection.immutable.StringOps

import org.springframework.cloud.CloudFactory

import io.pivotal.demo.model.CustomerPortifolio
import scala.collection.immutable.Nil
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.springframework.context.annotation.Configuration
import org.springframework.cloud.service.common.RedisServiceInfo
import com.redislabs.provider.redis.toRedisContext





/**
 * @author fmelo
 */

object ClusteringService {

  def HOME_LOCATION_WINDOW_SIZE = 25
  def TRAINSET_SIZE = 100000
  def NUMBER_OF_ACCOUNTS = 30 

  def NUMBER_OF_CLUSTERS = 3 // Low Risk, Medium-Low, High Risk
  

  //val cloud = new CloudFactory().getCloud();
  //val redisInfo = cloud.getServiceInfo("redis").asInstanceOf[RedisServiceInfo]
  
  
  val conf = new SparkConf().setMaster("local[*]")
                .setAppName("FraudDetection")
                .set("spark.eventLog.enabled", "false")
         //               .set("redis.host", redisInfo.getHost)
        //.set("redis.port", redisInfo.getPort.toString())
        //.set("redis.auth", redisInfo.getPassword)

                .set("redis.host", "redis.local.pcfdev.io")
                .set("redis.port", "36263")
                .set("redis.auth", "70ab1ec8-9b07-4311-b12c-f4f6e78c2101")
  
                
                
  val sc = new SparkContext(conf);
  
    
  
  val sqlContext = new SQLContext(sc)
  
//  val coordinates = loadCountiesCoordinates()
  
//  val transactionsQuery = "(select t.id as transaction_id, transaction_value, account_id, ts_millis, p.id as device_id, location from transaction t INNER JOIN pos_device p ON (t.device_id = p.id)) as transaction_info"
  
  val transactionsQuery = "(select distinct on (t.id) account_id, location, latitude, longitude, transaction_value, ts_millis, device_id, t.id as transaction_id from transaction t INNER JOIN pos_device p ON (t.device_id = p.id) INNER JOIN zip_codes z ON (upper(regexp_replace(p.location, '\\s+County', '')) = upper(z.county || ', ' || z.name) ) where latitude IS NOT NULL and longitude IS NOT NULL and account_id<="+NUMBER_OF_ACCOUNTS+" order by t.id desc LIMIT "+TRAINSET_SIZE+") as transaction_info"
  
  
  val transactionsDF = sqlContext.read.format("jdbc").options(Map(
          "url" -> "jdbc:postgresql://127.0.0.1:5432/gemfire?user=pivotal&password=pivotal",          
          "driver" -> "org.postgresql.Driver",
          "dbtable" -> transactionsQuery,
          "partitionColumn" -> "account_id",
          "lowerBound" -> "0",
          "upperBound" -> String.valueOf(NUMBER_OF_ACCOUNTS),
          "numPartitions" -> "1"))
          .load()
         
  def loadHomeLocations(): DataFrame = {
    
      val locationsQuery = "(select distinct on (account_id) account_id, location, count(location), latitude, longitude from transaction t INNER JOIN pos_device p ON (t.device_id = p.id) INNER JOIN (select latitude, longitude, county, name, count(zip) from zip_codes group by county, name, latitude, longitude) z ON (upper(regexp_replace(p.location, '\\s+County', '')) = upper(z.county || ', ' || z.name) ) where account_id<="+NUMBER_OF_ACCOUNTS+" group by account_id, location, latitude, longitude order by account_id, count(location) desc, location ) as home_locations"
      // currently takes the location of the largest number of transactions as "home location"
      
      val locations = sqlContext.read.format("jdbc").options(Map(
          "url" -> "jdbc:postgresql://127.0.0.1:5432/gemfire?user=pivotal&password=pivotal",
          "dbtable" -> locationsQuery,
          "partitionColumn" -> "account_id",
          "lowerBound" -> "0",
          "upperBound" -> String.valueOf(NUMBER_OF_ACCOUNTS),
          "numPartitions" -> "1"))
          .load()          
          
      locations.drop("count")
      
          
  }
  
  def loadDeviceLocations(): DataFrame = {
    
       val devicesQuery = "(select distinct on (id) id as device_id, location, latitude, longitude from pos_device p INNER JOIN (select latitude, longitude, county, name from zip_codes group by county, name, latitude, longitude) z ON (upper(regexp_replace(p.location, '\\s+County', '')) = upper(z.county || ', ' || z.name) ) group by device_id, location, latitude, longitude order by device_id, location ) as device_locations"
    
       sqlContext.read.format("jdbc").options(Map(
          "url" -> "jdbc:postgresql://127.0.0.1:5432/gemfire?user=pivotal&password=pivotal",
          "dbtable" -> devicesQuery,
          "partitionColumn" -> "device_id",
          "lowerBound" -> "0",
          "upperBound" -> "4000",
          "numPartitions" -> "1"))
          .load()          
          
   
          
       
          
       
  }
  
  
          /*
  def loadCountiesCoordinates(): DataFrame = {
    
      val csvSchema = StructType(Array(
          StructField("zip", StringType, true),
          StructField("latitude", DoubleType, true),
          StructField("longitude", DoubleType, true),
          StructField("city", StringType, true),
          StructField("state", StringType, true),
          StructField("county", StringType, true),    
          StructField("state_long", StringType, true)
          ))    
    
       val coordinates = sqlContext.read
            .format("com.databricks.spark.csv")
            .option("header", "true") // Use first line of all files as header
            .schema(csvSchema)
            .load("/Users/fmelo/Documents/workspace-sts/ClusteringService/src/main/resources/zip_codes_states.csv")      
            .drop("zip").drop("city").drop("state").distinct()
            
            
        val filteredCoordinates = coordinates
            .withColumn("county_location", upper(concat_ws(", ", coordinates("county"), coordinates("state_long"))))
            .drop("county").drop("state_long").cache()
            
        val coordinateRDD = filteredCoordinates.filter( coordinates("latitude").isNotNull && coordinates("longitude").isNotNull).map { x =>       
          (x.getString(x.fieldIndex("county_location")), x.getDouble(x.fieldIndex("latitude"))+":"+x.getDouble(x.fieldIndex("longitude")) )
        }        
              
        sc.toRedisKV(coordinateRDD)            
            
        return filteredCoordinates
            
  }*/
/*  
  def distanceBetweenLocations(location1: String, location2: String): Double = {

    val loc1 = coordinates.filter(coordinates("county_location") === location1)
    
    if (loc1.count()==0 || loc1.first()==null || loc1.first().anyNull) return 0
    val loc1Row = loc1.first()
    
    
    val lat1 = loc1Row.getDouble(loc1Row.fieldIndex("latitude"))
    val long1 = loc1Row.getDouble(loc1Row.fieldIndex("longitude"))
    
    val loc2 = coordinates.filter(coordinates("county_location") === location2)


    if (loc2.count()==0 || loc2.first()==null || loc2.first().anyNull) return 0
    val loc2Row = loc2.first()
    
    val lat2 = loc2Row.getDouble(loc2Row.fieldIndex("latitude"))
    val long2 = loc2Row.getDouble(loc2Row.fieldIndex("longitude"))
    
    return Util.calculateDistance(lat1, long1, lat2, long2)
    
  }
  */
  /*
  def convertLocation(dataframe: DataFrame): DataFrame = {
    
    dataframe
          .withColumn("_location", upper(trim(regexp_replace(dataframe("location"), "\\s+County", ""))))
          .drop("location")
          .withColumnRenamed("_location", "location")
  }*/
  
  def train(): String = {
    

    //val transactions = convertLocation(transactionsDF.sort(desc("ts_millis")).limit(TRAINSET_SIZE)).cache()
    
    val transactions = transactionsDF.sort(desc("ts_millis")).limit(TRAINSET_SIZE).cache()
    
    val deviceLocations = loadDeviceLocations()
    
    sc.toRedisKV(deviceLocations.map { x =>  
         ("device::"+x.getLong(x.fieldIndex("device_id")), x.getDouble(x.fieldIndex("latitude"))+":"+x.getDouble(x.fieldIndex("longitude"))   )  // device_id x location
       })
    
    val homeLocations = loadHomeLocations()
    
    val homeLocationsRowsMap = homeLocations.map { x =>  
      (x.getLong(0), x) // account_id x row     
    }.collect().toMap
    
    val homeLocationsKV = homeLocations.map { x =>  
      ("home::"+x.getLong(0),  x.getDouble(x.fieldIndex("latitude"))+":"+x.getDouble(x.fieldIndex("longitude"))   )  // home::account x location
    }
    
    sc.toRedisKV(homeLocationsKV)
    
    val homeLocationsKVMap = homeLocationsKV.collect().toMap
    

    val trainSet = transactions.map { x => 
    
      val customerId = x.getLong(x.fieldIndex("account_id"))
            
      val transactionLocation = x.getString(x.fieldIndex("location"))
      
      val homeLocation = homeLocationsRowsMap.get(customerId).get
      
      val latitude = x.getDouble(x.fieldIndex("latitude"))
      val longitude = x.getDouble(x.fieldIndex("longitude"))
      val homeLatitude = if (homeLocation.anyNull) latitude else homeLocation.fieldIndex("latitude") 
      val homeLongitude = if (homeLocation.anyNull) longitude else homeLocation.fieldIndex("longitude")
      
      val distance = Util.calculateDistance(latitude, 
                                            longitude, 
                                            homeLatitude,
                                            homeLongitude                                         
                                            )
      
      val transactionValue = x.getAs[java.math.BigDecimal](x.fieldIndex("transaction_value"))
      
      Vectors.dense(distance, transactionValue.doubleValue())
    }
        
    val scaler = new StandardScaler().fit(trainSet)
    
    val kMeansModel = KMeans.train(
                      scaler.transform(trainSet), NUMBER_OF_CLUSTERS, 20)

    return  kMeansModel.toPMML()
    
  
    
    
  }
    

  
}
