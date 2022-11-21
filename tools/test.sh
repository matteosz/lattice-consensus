../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
rm ../logs/*;
./performance.py -r ../template_java/run.sh -l ../logs -p 128 -m 1000000 -t 60;
#sleep 10;
wc -l ../logs/*.output;
python3 validater.py;

sleep 10;

../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
rm ../logs/*;
./performance.py -r ../template_java/run.sh -l ../logs -p 10 -m 1000000 -t 30;
#sleep 10;
wc -l ../logs/*.output;
python3 validater.py;