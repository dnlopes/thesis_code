#!/bin/bash

#This command line runs the TPC-W Browsing Mix with 30 EB's, output file run1.m
#300 seconds of ramp-up time, 1000 seconds of exection, and 300 seconds of ramp
#down time. http://whitelace.ece.wisc.edu:8085/ is used as the web server
#prefix for all requests. There are 10000 items in the database, and a standard
#Think time of multiplier of 1.0.

#java -mx512M rbe.RBE -EB rbe.EBTPCW1Factory 100 -OUT browsing.m -RU 300 -MI 3000 -RD 300 -WWW http://localhost:8080/tpcw/ -CUST 144000 -ITEM 10000 -TT 1.0

#This command line does the same, using the TPC-W Shopping Mix, and no think
#time.
#java -mx512M rbe.RBE -EB rbe.EBTPCW2Factory 100 -OUT shopping.m -RU 300 -MI 3000 -RD 300 -WWW http://localhost:8080/tpcw/ -CUST 144000 -ITEM 10000 -TT 1.0

#This command line does the same, using the TPC-W Ordering Mix
#java -mx512M rbe.RBE -EB rbe.EBTPCW3Factory 2 -OUT ordering2.m -RU 300 -MI 3000 -RD 300 -WWW http://localhost:8080/tpcw/ -CUST 144000 -ITEM 10000 -TT 1.0


#Browsing Mix = rbe.EBTPCW1Factory
#Shopping Mix = rbe.EBTPCW2Factory
#Ordering Mix = rbe.EBTPCW3Factory

java rbe.RBE -EB rbe.EBTPCW1Factory 10 -OUT run1.m -GETIM false -RU 30 -MI 60 -RD 1 -WWW http://localhost:8080/tpcw/
-CUST 100 -ITEM 10000
