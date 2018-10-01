#!/bin/bash

# 313 types
FILES="/media/olibroe/DATA/Ubuntu_Data/imlfiles/*.sf"

EXECUTIONS=10
COUNTER="java -Xmx12g -Xms12g -XX:MaxHeapFreeRatio=100 -jar NodeCounter.jar"
COMMAND="java -Xmx12g -Xms12g -XX:MaxHeapFreeRatio=100 -jar RemoveFieldPerformance.jar"

rtype="oconstructor"
rfield="isexplicit"
outfile="./performance_removeField_${rfield}_of_${rtype}.csv"
echo "file,objects,removeTime,totalTime" >> $outfile
for filename in $FILES; do
    basename=$(basename $filename)
    echo $basename
    objCount=$($COUNTER $filename)
    for ((i=0; i < $EXECUTIONS; i++)); do
        
        start=$(date +%s%N)
        result=$($COMMAND $filename $rfield $rtype)
        end=$(date +%s%N)
            
        diff=$(echo "($end - $start)" | bc)
            
        echo "$basename,$objCount,$result,$diff" >> $outfile
    done
done