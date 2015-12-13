# Set terminal & output
set terminal postscript eps enhanced color solid font 'Helvetica,25'

# Output
set output outputfile

# Axis
#set yrange [0:400]
set ylabel "Latency (ms)"
#set xrange [0:100000]
set xlabel "Throughput (txn/s)"
set xtics nomirror rotate by -30 scale 0.5
set xtics font "Helvetica,16"

# Key
#set key default
#set key box
#set key inside vert
#set key bottom right
set key top left

# Plot type
set style data linespoints

# define grid
set style line 12 lc rgb '#808080' lt 0 lw 1
set grid back ls 12

# Lines
#set style line 1 linecolor rgb '#0060ad' linetype 1 linewidth 2 pointtype 1 pointsize 1.5   # --- blue
set style line 1 lc rgb '#0060ad' lt 1 lw 2 pt 1 ps 2
set style line 2 lc rgb '#0060ad' lt 1 lw 2 pt 2 ps 2
set style line 3 lc rgb '#dd181f' lt 1 lw 2 pt 3 ps 2
set style line 4 lc rgb '#dd181f' lt 1 lw 2 pt 4 ps 2
set style line 5 lc rgb '#0060ad' lt 1 lw 2 pt 5 ps 2
set style line 6 lc rgb '#82CA4A' lt 1 lw 2 pt 6 ps 2

set datafile separator ','

plot data1 every 6 using ($3/60):7 with linespoints ls 5 title 'WeakDB-5R', data2 every 6 using ($3/60):7 with linespoints ls 3 title 'WeakDB-4R', data3 every 6 using ($3/60):7 with linespoints ls 7 title 'WeakDB-3R', data4 every 6 using ($3/60):7 with linespoints ls 9 title 'WeakDB-2R', data5 every 6 using ($3/60):7 with linespoints ls 13 title 'Galera-2R SUM', data6 every 6 using ($3/60):7 with linespoints ls 11 title 'Galera-2R NO_SUM', data7 every 6 using ($3/60):7 with linespoints ls 12 title 'Galera-3R NO_SUM'
