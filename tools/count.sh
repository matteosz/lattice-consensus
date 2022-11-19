total=0
find ../logs/ -type f -name "*.output" | while read FILE; do
     count=$(grep -c ^ < "$FILE")
     echo "$FILE has $count lines"
     let total=total+count
done
echo TOTAL LINES COUNTED:  $total