#!/bin/bash

FILES="/media/olibroe/DATA/Ubuntu_Data/imlfiles/*.sf"
EXECUTIONS=100
COUNTER="java -Xmx12g -Xms12g -XX:MaxHeapFreeRatio=100 -jar NodeCounter.jar"

HEAPSIZES=(14)

roots="imlgraph metainformation"
for heapsize in "${HEAPSIZES[@]}"; do
    echo "Heapsize $heapsize GB"
    
    outfile="./performance_GC_${heapsize}G.csv"
    command="java -Xmx${heapsize}g -Xms${heapsize}g -XX:MaxHeapFreeRatio=100 -jar GcPerformance.jar"

    echo "file,objectCount,gcTime,totalTime" >> $outfile
    for filename in $FILES; do
        basename=$(basename $filename)
        echo $basename
        objCount=$($COUNTER $filename)
        for ((i=0; i < $EXECUTIONS; i++)); do
        
            start=$(date +%s%N)
            result=$($command $filename $roots)
            end=$(date +%s%N)
            
            diff=$(echo "($end - $start)" | bc)
            
            echo "$basename,$objCount,$result,$diff" >> $outfile
        done
    done
done


roots=""
heapsize=14
outfile="./performance_GC_${heapsize}G_noRoots.csv"
command="java -Xmx${heapsize}g -Xms${heapsize}g -XX:MaxHeapFreeRatio=100 -jar GcPerformance.jar"

echo "file,objectCount,gcTime,totalTime" >> $outfile
for filename in $FILES; do
    basename=$(basename $filename)
    echo $basename
    objCount=$($COUNTER $filename)
    for ((i=0; i < $EXECUTIONS; i++)); do
        
        start=$(date +%s%N)
        result=$($command $filename $roots)
        end=$(date +%s%N)
            
        diff=$(echo "($end - $start)" | bc)
            
        echo "$basename,$objCount,$result,$diff" >> $outfile
    done
done