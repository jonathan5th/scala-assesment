#Wehkamp Assesment - Valentin Dominte

This is the assessment project for 'Java / Scala Developer' position.
It consists of a REST API for a shopping basket using Scala and Akka.

To run it you need Java 8 and Sbt installed. Use `sbt test` or `sbt run`
in the root folder to run the test or start the app respectively.

It will expose a REST API on localhost:5000 which can handle the following requests:
- GET requests on path "api/shoppingbasket/items" for fetching the current list of 
items in the basket 
- POST requests on path "api/shoppingbasket/items" for adding a new item with a JSON 
containing two parameters: the "productId" as string and the "quantity" as numeric field
- DELETE requests on path "api/shoppingbasket/items/:id" to delete an item by id
