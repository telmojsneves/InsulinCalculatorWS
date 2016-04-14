javac server/*.java
wsgen -cp . server.InsulinWS
java -cp . server.InsulinEndpoint

