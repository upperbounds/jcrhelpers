name := "jcrhelpers"

version := "1.0"

organization := "CFM"

scalaVersion := "2.9.1"        

libraryDependencies ++= Seq("javax.jcr" % "jcr" % "2.0" % "compile",
                            "org.apache.jackrabbit" % "jackrabbit-core" % "2.3.4" % "compile" withSources(),   
                            "org.apache.jackrabbit" % "jackrabbit-jcr2dav" % "2.3.4" % "compile", 
                            "org.apache.jackrabbit" % "jackrabbit-jcr-rmi" % "2.3.4" % "compile",
                            "org.slf4j" % "slf4j-simple" % "1.6.4" % "compile"
)