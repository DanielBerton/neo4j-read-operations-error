# neo4j-read-operations-error

Using the org.neo4j.kernel.api ReadOperation, the method relationshipQueryIndex needs as parameter endNode and startNode, if we don't 
have these parameters we have to pass -1 to ignore the parameter. 
If we pass -1 on the startNode it works but if we pass it on endNode it doesn't work.
With any other number it works.

This is the dataset statement :

CREATE (joe:Person:Hipster {name:'Joe',age:42})-[checkin:CHECKIN {on:'2015-12-01'}]->(philz:Place {name:'Philz'} )RETURN *

the result of the test should be 

name : Joe

node label : Place

But if we pass null on the endNode parameter :

ReadOperation.relationshipQueryIndex(type, query, db, from.getId(), null);

it doesn't work.

