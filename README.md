# Run

### Input Files
In order to run our program you should have 2 files:

1. Dataset for Server side:

    * the name and path of the file should be replaced at line 10 in Server.java.

    * the dataset format should be: (Timestamp) (Car ID) (X) (Y) (Velocity)

      note: the delimiter should be space.
    
2. Target list for Client side:

    * the target list file is being created at the creation of the database, so in order to create target list file you should follow the next steps:

         - change the name and path of the target list file at line 11 in Server.java to the relevant name and path.

         - set a breaking point at line 67 in WorkingWithDataset.java.

         - run the program in debug mode until you get to the breaking point - the target list was created.

    * you should also change the name and path of the target list file at line 219 in Client2D.java to the relevant name and path.

### Output File

   There is one output file from our program that contains the attack results and the time it took, the name and path of this file should be replaced at line 302 in Client2D.java.


