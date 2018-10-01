#!/bin/bash

# 313 types
FILES="/media/olibroe/DATA/Ubuntu_Data/imlfiles/*.sf"
# 167 types
FILES2="/media/olibroe/DATA/Ubuntu_Data/imlfiles2/*.sf"

EXECUTIONS=10
COUNTER="java -Xmx12g -Xms12g -XX:MaxHeapFreeRatio=100 -jar NodeCounter.jar"
COMMAND="java -Xmx12g -Xms12g -XX:MaxHeapFreeRatio=100 -jar RemoveTypePerformance.jar"


rtype="sloc"
outfile="./performance_removeType_${rtype}_313.csv"
echo "file,removedObjects,removeTime,totalTime" >> $outfile
for filename in $FILES; do
    basename=$(basename $filename)
    echo $basename
    objCount=$($COUNTER $filename $rtype)
    for ((i=0; i < $EXECUTIONS; i++)); do
        
        start=$(date +%s%N)
        result=$($COMMAND $filename $rtype)
        end=$(date +%s%N)
            
        diff=$(echo "($end - $start)" | bc)
            
        echo "$basename,$objCount,$result,$diff" >> $outfile
    done
done

rtype="value"
outfile="./performance_removeType_${rtype}_313.csv"
echo "file,removedObjects,removeTime,totalTime" >> $outfile
for filename in $FILES; do
    basename=$(basename $filename)
    echo $basename
    objCount=$($COUNTER $filename $rtype)
    for ((i=0; i < $EXECUTIONS; i++)); do
        
        start=$(date +%s%N)
        result=$($COMMAND $filename $rtype)
        end=$(date +%s%N)
            
        diff=$(echo "($end - $start)" | bc)
            
        echo "$basename,$objCount,$result,$diff" >> $outfile
    done
done

rtype="value"
outfile="./performance_removeType_${rtype}_167.csv"
echo "file,removedObjects,removeTime,totalTime" >> $outfile
for filename in $FILES2; do
    basename=$(basename $filename)
    echo $basename
    objCount=$($COUNTER $filename $rtype)
    for ((i=0; i < $EXECUTIONS; i++)); do
        
        start=$(date +%s%N)
        result=$($COMMAND $filename $rtype)
        end=$(date +%s%N)
            
        diff=$(echo "($end - $start)" | bc)
            
        echo "$basename,$objCount,$result,$diff" >> $outfile
    done
done
