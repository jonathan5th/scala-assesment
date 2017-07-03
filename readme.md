#Wehkamp Assesment - Valentin Dominte
This is the assessment project for 'Full stack Developer' position.
It consists of a REST API for a shopping basket using Scala and Akka.

To run it you need Java 8 and Sbt installed. Use `sbt test` or `sbt run`
in the root folder to run the test or start the app respectively.

It will expose a REST API on localhost:5000 which can handle the following requests:
- GET and POST requests on path "api/shoppingbasket/items" for fetching the current 
list of items in the basket and adding a new item
- DELETE requests on path "api/shoppingbasket/items/:id" to delete an item by id
