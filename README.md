# Privacy Vulnerabilities of Dataset Anonymization Techniques
In recent years there is a broad discussion about the field of user privacy and security. Security and privacy vulnerabilities are being discovered on a near-daily basis, all over the world. That is the main reason and motivation behind this project.

In this project, we will present techniques to attack and defend a dataset with strong privacy and anonymization security. We will show that information about specific data can be extracted even if the query provided by the database returns valid answers only for a predefined number of elements, K, or more.

The project relies on creating a database from the provided dataset and implementing a valid query system. Once a database created the attacker will execute the attack and will extract illegal information from the database in general and about a specific target individually.

## Implemantation
Currently we are using a fixed version of "Kolntrace Project": http://kolntrace.project.citi-lab.fr

We have changed the data so that cars velocity will be random.
In addition for purpose of comfort we are generating a list of all cars without their velocity so that our attack algorithm can extract them from the constructed database. 

We have created a Server that manages all the work with the data set and creates interface to the client.

Our server implements the database and creates a query that return the average velocity of the cars in a given range in a given timestamp if and on if there are K or more cars that setisfy this query.

The client can "connect" to the server and ask the query. Our client will abuse the amount of possible queries to receive information about all of the cars.
### Database for 1D
Our database is an implementation of travel tree and HashMap.
The hash map contains hash for all our timestamps and each timestamp key contains BST(Binary Search Tree) as a value.

Node:
    
    double key - x coordinate
    double value - velocity of the car
    Node left, right - has the next Node object


### Algorithm for 1D
As an attacker we are running from the "Client" 
* First, we try to find the first good answer from the server, a "bad answer" is no answer from the server due to inappropriate query. The query-set of this step is an incrementing range (xl,xh) that holds the lower value(xl)  and incrementing the higher value (xh = xh+1). The initiate values of xl and xh is vm.
* Second, after we got a legal answer, we increment the min value of the range by 1 (xl = xl+1), and start a query-set of range (xl, xh) when xl and xh holding the updated values. The query-set is like in above, incrementing range (xl,xh) that holds its lower value(xl) and incrementing the higher value(xh = xh+1).
* Third, we save the legal answer from previous step (Sav1), we will use that later.
* Fourth, we query the server with the specific query of range (vm,xh) which xh is the updated value, and save the server answer (Sav2). In this point we already know that we are asking about k+1 elements so we will get answer for sure.
* The last step is calculating (k+1)*Sav2 – k*Sav1. That equation will provide us an illegal answer about our victim.
