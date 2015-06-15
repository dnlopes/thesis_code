# Set terminal & output
set terminal postscript eps enhanced color solid font 'Helvetica,18'

# Output
set output outputfile

# Axis
#set yrange [0:30]
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
set key top right

# Plot type
set style data linespoints

# define grid
set style line 12 lc rgb '#808080' lt 0 lw 1
set grid back ls 12

# Lines
#set style line 1 linecolor rgb '#0060ad' linetype 1 linewidth 2 pointtype 1 pointsize 1.5   # --- blue
# green = #82CA4A
# red = #dd181f
# blue = #0060ad

set style line 1 lc rgb '#dd181f' lt 1 lw 2 pt 6 ps 2
set style line 2 lc rgb '#dd181f' lt 1 lw 2 pt 7 ps 2

set style line 3 lc rgb '#0060ad' lt 1 lw 2 pt 8 ps 2
set style line 4 lc rgb '#0060ad' lt 1 lw 2 pt 9 ps 2.5

set style line 5 lc rgb '#82CA4A' lt 1 lw 2 pt 4 ps 2
set style line 6 lc rgb '#82CA4A' lt 1 lw 2 pt 5 ps 2

set datafile separator ','

plot data1 every ::1 using 2:3 with linespoints ls 5 title 'Middleware 3-replica', data2 every ::1 using 2:3 with linespoints ls 6 title 'Middleware 5-replica', data3 every ::1 using 2:3 with linespoints ls 3 title 'Galera Cluster 3-replica', data4 every ::1 using 2:3 with linespoints ls 4 title 'Galera Cluster 5-replica', data5 every ::1 using 2:3 with linespoints ls 1 title 'MySQL Cluster 3-replica', data6 every ::1 using 2:3 with linespoints ls 2 title 'MySQL Cluster 5-replica',