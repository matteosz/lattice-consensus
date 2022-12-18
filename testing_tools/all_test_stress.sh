#!/bin/sh

# Case 1

proc=2; prop=100000; vs=10; ds=20; t=15; c=1;
echo "Processes: " $proc;
echo "VS=" $vs "DS=" $ds;
echo "Time: " $t "s";
../build/cleanup.sh;
../build/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 stress_perf.py agreement -r ../build/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
#wc -l ../logs/*.output;
for i in $(seq -f "%02g" $proc); do
  touch ../logs/proc$i.output;
done;
node --max-old-space-size=8192 checkLattice.js ../logs/proc*.output ../logs/proc*.config;
python3 checkLattice.py ../logs ../logs $proc $prop;
sleep 10;

# Case 2

#proc=2; prop=10000000; vs=10; ds=20; t=60; c=1;
#echo "Processes: " $proc;
#echo "VS=" $vs "DS=" $ds;
#echo "Time: " $t "s";
#../build/cleanup.sh;
#../build/build.sh > /dev/null;
#if [ $c = 1 ]; then 
#rm ../logs/*;
#else
#cd ../logs;
#rm !*.config|"hosts";
#cd ../tools;
#fi;
#python3 stress_perf.py agreement -r ../build/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
#sleep 10;
##wc -l ../logs/*.output;
#for i in $(seq -f "%02g" $proc); do
#  touch ../logs/proc$i.output;
#done;
#node --max-old-space-size=8192 checkLattice.js ../logs/proc*.output ../logs/proc*.config;
#python3 checkLattice.py ../logs ../logs $proc $prop;
#sleep 10;

# Case 3

proc=15; prop=1000000; vs=5; ds=10; t=180; c=1;
echo "Processes:" $proc;
echo "VS:" $vs "DS:" $ds;
echo "Time:" $t;
../build/cleanup.sh;
../build/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 stress_perf.py agreement -r ../build/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
#wc -l ../logs/*.output;
for i in $(seq -f "%02g" $proc); do
  touch ../logs/proc$i.output;
done;
node --max-old-space-size=8192 checkLattice.js ../logs/proc*.output ../logs/proc*.config;
python3 checkLattice.py ../logs ../logs $proc $prop;
echo "Sleeping 10s";
sleep 10;

# Case 4

proc=15; prop=10000; vs=30; ds=200; t=60; c=1;
echo "Processes:" $proc;
echo "VS:" $vs "DS:" $ds;
echo "Time:" $t;
../build/cleanup.sh;
../build/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 stress_perf.py agreement -r ../build/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
#wc -l ../logs/*.output;
for i in $(seq -f "%02g" $proc); do
  touch ../logs/proc$i.output;
done;
node --max-old-space-size=8192 checkLattice.js ../logs/proc*.output ../logs/proc*.config;
python3 checkLattice.py ../logs ../logs $proc $prop;
echo "Sleeping 10s";
sleep 10;

# Case 5

proc=20; prop=10000; vs=500; ds=1024; t=90; c=1;
echo "Processes: " $proc;
echo "VS=" $vs "DS=" $ds;
echo "Time: " $t "s";
../build/cleanup.sh;
../build/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 stress_perf.py agreement -r ../build/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
#wc -l ../logs/*.output;
for i in $(seq -f "%02g" $proc); do
  touch ../logs/proc$i.output;
done;
node --max-old-space-size=8192 checkLattice.js ../logs/proc*.output ../logs/proc*.config;
python3 checkLattice.py ../logs ../logs $proc $prop;
echo "Sleeping 10s";
sleep 10;

# Case 6

proc=30; prop=100000; vs=10; ds=50; t=60; c=1;
echo "Processes:" $proc;
echo "VS:" $vs "DS:" $ds;
echo "Time:" $t;
../build/cleanup.sh;
../build/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 stress_perf.py agreement -r ../build/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
#wc -l ../logs/*.output;
for i in $(seq -f "%02g" $proc); do
  touch ../logs/proc$i.output;
done;
node --max-old-space-size=8192 checkLattice.js ../logs/proc*.output ../logs/proc*.config;
python3 checkLattice.py ../logs ../logs $proc $prop;
sleep 10;
# Case 7

proc=33; prop=10000; vs=50; ds=500; t=30; c=1;
echo "Processes:" $proc;
echo "VS:" $vs "DS:" $ds;
echo "Time:" $t;
../build/cleanup.sh;
../build/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 stress_perf.py agreement -r ../build/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
#wc -l ../logs/*.output;
for i in $(seq -f "%02g" $proc); do
  touch ../logs/proc$i.output;
done;
node --max-old-space-size=8192 checkLattice.js ../logs/proc*.output ../logs/proc*.config;
python3 checkLattice.py ../logs ../logs $proc $prop;
sleep 10;

# Case 8

proc=51; prop=100000; vs=5; ds=10; t=30; c=1;
echo "Processes:" $proc;
echo "VS:" $vs "DS:" $ds;
echo "Time:" $t;
../build/cleanup.sh;
../build/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 stress_perf.py agreement -r ../build/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
#wc -l ../logs/*.output;
for i in $(seq -f "%02g" $proc); do
  touch ../logs/proc$i.output;
done;
node --max-old-space-size=8192 checkLattice.js ../logs/proc*.output ../logs/proc*.config;
python3 checkLattice.py ../logs ../logs $proc $prop;
sleep 10;

# Case 9

proc=70; prop=1000; vs=300; ds=350; t=60; c=1;
echo "Processes:" $proc;
echo "VS:" $vs "DS:" $ds;
echo "Time:" $t;
../build/cleanup.sh;
../build/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 stress_perf.py agreement -r ../build/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
#wc -l ../logs/*.output;
for i in $(seq -f "%02g" $proc); do
  touch ../logs/proc$i.output;
done;
node --max-old-space-size=8192 checkLattice.js ../logs/proc*.output ../logs/proc*.config;
python3 checkLattice.py ../logs ../logs $proc $prop;
sleep 10;

# Case 10

proc=100; prop=50000; vs=3; ds=5; t=60; c=1;
echo "Processes:" $proc;
echo "VS:" $vs "DS:" $ds;
echo "Time:" $t;
../build/cleanup.sh;
../build/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 stress_perf.py agreement -r ../build/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
#wc -l ../logs/*.output;
for i in $(seq -f "%02g" $proc); do
  touch ../logs/proc$i.output;
done;
node --max-old-space-size=8192 checkLattice.js ../logs/proc*.output ../logs/proc*.config;
python3 checkLattice.py ../logs ../logs $proc $prop;
sleep 10;

# Case 11

proc=100; prop=1000; vs=50; ds=200; t=60; c=1;
echo "Processes:" $proc;
echo "VS:" $vs "DS:" $ds;
echo "Time:" $t;
../build/cleanup.sh;
../build/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 stress_perf.py agreement -r ../build/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
#wc -l ../logs/*.output;
for i in $(seq -f "%02g" $proc); do
  touch ../logs/proc$i.output;
done;
node --max-old-space-size=8192 checkLattice.js ../logs/proc*.output ../logs/proc*.config;
python3 checkLattice.py ../logs ../logs $proc $prop;
sleep 10;

# Case 12

proc=100; prop=500; vs=100; ds=500; t=90; c=1;
echo "Processes:" $proc;
echo "VS:" $vs "DS:" $ds;
echo "Time:" $t;
../build/cleanup.sh;
../build/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 stress_perf.py agreement -r ../build/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
#wc -l ../logs/*.output;
for i in $(seq -f "%02g" $proc); do
  touch ../logs/proc$i.output;
done;
node --max-old-space-size=8192 checkLattice.js ../logs/proc*.output ../logs/proc*.config;
python3 checkLattice.py ../logs ../logs $proc $prop;
sleep 10;

# Case 13

proc=128; prop=10000; vs=5; ds=10; t=90; c=1;
echo "Processes:" $proc;
echo "VS:" $vs "DS:" $ds;
echo "Time:" $t;
../build/cleanup.sh;
../build/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 stress_perf.py agreement -r ../build/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
#wc -l ../logs/*.output;
for i in $(seq -f "%02g" $proc); do
  touch ../logs/proc$i.output;
done;
node --max-old-space-size=8192 checkLattice.js ../logs/proc*.output ../logs/proc*.config;
python3 checkLattice.py ../logs ../logs $proc $prop;
sleep 10;

# Case 14

proc=128; prop=500; vs=150; ds=500; t=120; c=1;
echo "Processes:" $proc;
echo "VS:" $vs "DS:" $ds;
echo "Time:" $t;
../build/cleanup.sh;
../build/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 stress_perf.py agreement -r ../build/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
#wc -l ../logs/*.output;
for i in $(seq -f "%02g" $proc); do
  touch ../logs/proc$i.output;
done;
node --max-old-space-size=8192 checkLattice.js ../logs/proc*.output ../logs/proc*.config;
python3 checkLattice.py ../logs ../logs $proc $prop;
sleep 10;

# Case 15

proc=128; prop=500; vs=500; ds=1024; t=180; c=1;
echo "Processes:" $proc;
echo "VS:" $vs "DS:" $ds;
echo "Time:" $t;
../build/cleanup.sh;
../build/build.sh > /dev/null;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs;
rm !*.config|"hosts";
cd ../tools;
fi;
python3 stress_perf.py agreement -r ../build/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
#wc -l ../logs/*.output;
for i in $(seq -f "%02g" $proc); do
  touch ../logs/proc$i.output;
done;
node --max-old-space-size=8192 checkLattice.js ../logs/proc*.output ../logs/proc*.config;
python3 checkLattice.py ../logs ../logs $proc $prop;