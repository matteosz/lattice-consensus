echo "10 Processes:";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
rm ../logs/*;
./performance.py -r ../template_java/run.sh -l ../logs -p 10 -m 1000000 -t 60;
sleep 2;
wc -l ../logs/*.output;
python3 validater.py;

sleep 5;

echo "29 Processes:";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
rm ../logs/*;
./performance.py -r ../template_java/run.sh -l ../logs -p 29 -m 1000000 -t 60;
sleep 2;
wc -l ../logs/*.output;
python3 validater.py;

sleep 5;

echo "65 Processes:";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
rm ../logs/*;
./performance.py -r ../template_java/run.sh -l ../logs -p 65 -m 1000000 -t 60;
sleep 2;
wc -l ../logs/*.output;
python3 validater.py;

sleep 5;

echo "100 Processes:";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
rm ../logs/*;
./performance.py -r ../template_java/run.sh -l ../logs -p 100 -m 1000000 -t 60;
sleep 2;
wc -l ../logs/*.output;
python3 validater.py;

sleep 5;

echo "128 Processes:";
../template_java/cleanup.sh;
../template_java/build.sh > /dev/null;
rm ../logs/*;
./performance.py -r ../template_java/run.sh -l ../logs -p 128 -m 1000000 -t 60;
sleep 2;
wc -l ../logs/*.output;
python3 validater.py;