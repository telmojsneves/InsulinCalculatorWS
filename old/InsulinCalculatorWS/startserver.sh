javac server/*.java
wsgen -cp . server.InsulinDoseCalculator
java -cp . server.InsulinEndpoint

