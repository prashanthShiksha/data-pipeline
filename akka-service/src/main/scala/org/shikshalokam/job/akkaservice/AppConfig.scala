package org.shikshalokam.job.akkaservice

import com.typesafe.config.{Config, ConfigFactory}
import java.io.File
import java.nio.file.{Files, Paths}

object AppConfig {
  private val defaultPaths = List(
    "../../unified-pipeline.conf"
  )
  
  private val configPath = sys.env.getOrElse("CONFIG_FILE", 
    defaultPaths.find(p => Files.exists(Paths.get(p))).getOrElse("unified-pipeline.conf")
  )

  private val rootConfig = ConfigFactory.parseFile(new File(configPath)).resolve()
  val config: Config = rootConfig.getConfig("akka-service").withFallback(rootConfig).withFallback(ConfigFactory.load())
}
