Stuff to make dealing with JCR storage easier
==============================

Prerequisites 
install sbt 

in the project root run:
sbt console

Getting a session

    import cfm._; import cfm.Repo._
    val s = new DavRepoAdaptor("http://localhost:4502/crx/server", "crx.default", "admin", "admin").s


Searching 

    val q = "//nodename"
    
    //accessing any iterator method on a QueryResult will execute the query
    s.xpath(q).limit(10).offset(10).foreach(println)

