#!/bin/sh
proc=40; prop=100; vs=50; ds=700; t=300; c=1;
echo $"Processes:" $proc;
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
shopt -s extglob;
if [ $c = 1 ]; then 
rm ../logs/*;
else
cd ../logs
rm !(*.config|"hosts")
cd ../tools;
fi;
shopt -u extglob;

python3 performance.py agreement -r ../template_java/run.sh -l ../logs -p $proc -n $prop -v $vs -d $ds -t $t -c $c > /dev/null;
sleep 10;
wc -l ../logs/*.output;
python3 validator2.py ../logs ../logs $proc $prop;