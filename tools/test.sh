#!/bin/sh
proc=128; prop=100; vs=5; ds=5; t=120;
echo $"Processes:" $proc;
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
rm ../logs/*;
python3 performance.py agreement -r ../template_java/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t > /dev/null;
sleep 10;
wc -l ../logs/*.output;
python3 lattice_validater.py ../logs ../logs $proc $prop;

sleep 5;